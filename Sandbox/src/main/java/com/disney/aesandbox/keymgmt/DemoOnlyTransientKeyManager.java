package com.disney.aesandbox.keymgmt;

import com.disney.aesandbox.commandline.Logger;
import com.disney.aesandbox.objmapping.Keys;
import com.disney.aesandbox.objmapping.RSAKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class generates transient keypairs.  DO NOT USE IN PRODUCTION!  Transient keypairs are
 * useless in production because once the process that generates the keypair shuts down the
 * keys are no longer valid -- in this case "transient" refers to the fact that there is NO STORAGE
 * provided for the keys.  Proper keypair use in production requires storing private keys
 * securely and publishing the public keys.
 *
 * This class manages the set of keys by controlling rotation and key generation for
 * both in-process and web use.  Internal processes will get private key info to sign
 * tokens, external web processes will query for a JWKS in order to verify token signatures.
 *
 * To modify this class for production use, some suitable storage (secure, mutable) for key information
 * must be implemented so that the key set survives restarts of the web process.  Basically the KeyDataHolder
 * array or an equivalent must be implemented on some filesystem or other storage.
 *
 * This class mimics the documented Disney-recommended key rotation policy, but for testing purposes uses a
 * time unit of MINUTES instead of DAYS, so unless the time unit is changed the key will rotate every 90
 * minutes and the documented time for hanging onto a rotated key will use MINUTES as the time unit.  This makes
 * it easier to observe the intended effects of key rotation.
 */
public class DemoOnlyTransientKeyManager {

    // Instance object used for synchronization
    private Object holderLock;

    // State information for the set of keys
    private KeyDataHolder standbyKey;
    private KeyDataHolder primary;
    private List<KeyDataHolder> verificationKeys;
    private KeyRotationManager rotator;

    private KeyPairGenerator kpg;

    private Logger logger;

    private TimeBasedKeyRotationPolicy rotationPolicy;

    // Housekeeping thread pool
    private ScheduledExecutorService execSvc;

    // Singleton instance
    public static final DemoOnlyTransientKeyManager INSTANCE;

    static {
        // Singleton, instantiation relies on the ClassLoader lock
        INSTANCE = new DemoOnlyTransientKeyManager();
    }

    private DemoOnlyTransientKeyManager() {

        // Very important semaphore for changing key values
        holderLock = new Object();

        logger = Logger.newBasicLogger();

        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
        } catch (NoSuchAlgorithmException nsa) {
            // Can't happen -- RSA is guaranteed to be in the JDK.  However, if it ever did happen, it would be an
            // absolutely fatal error.
            nsa.printStackTrace();
            System.err.flush();
            System.exit(1);
        }

        // FIXME: testing configuration by default uses very short intervals for key rotation
        rotationPolicy = TimeBasedKeyRotationPolicy.TEST;
        //rotationPolicy = TimeBasedKeyRotationPolicy.PRODUCTION;

        // At any given moment, there MUST be:
        // - One primary signing key
        // there MAY be:
        // - One newly-introduced ("standby") key, which will become the primary key after some interval
        // - A small number of keys that might be used for verification, but not used for signing (these are
        //      keys that were formerly primary signing keys but have been rotated out)
        verificationKeys = new ArrayList<>();
        primary = makeKeyDataHolder();
        standbyKey = null;

        // Initialize the background rotation thread object, instructing it to introduce a new key at the appropriate time
        rotator = new KeyRotationManager();
        long now = System.currentTimeMillis();
        RotationEvent evt = new RotationEvent(RotationEventType.INTRO, now + rotationPolicy.getTimeDelayBeforeIntroducingNewStandby());
        logger.logVerbose("Time is " + new Date() + ", new key should be introduced at " +
                new Date(now + rotationPolicy.getTimeDelayBeforeIntroducingNewStandby()));
        rotator.addEvent(evt);

        // Kick off the background thread handling the rotation (Runnable) object.
        execSvc = Executors.newSingleThreadScheduledExecutor();
        execSvc.scheduleAtFixedRate(rotator, rotationPolicy.getPollingInterval(), rotationPolicy.getPollingInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * Return a public key set in JSON format, suitable for an HTTP response.  This implementation builds the
     * JSON return object for every call, with no caching.
     *
     */
    public String getJsonPublicKeySet() {
        return getPrettyPrintedJsonPublicKeySet();
    }

    /**
     * Return a public key set in JSON format, pretty-printed.  This implementation builds the
     * JSON return object for every call, with no caching.
     *
     */
    public String getPrettyPrintedJsonPublicKeySet() {

        // Jackson ObjectMapper implementation
        Keys keys = new Keys();

        keys.setKeys(makeRSAKeyArray());
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(keys);
        } catch (Exception e) {
            // disaster
            e.printStackTrace();
        }

        return json;
    }

    /**
     * This method handles key rotation initiated by either a timer or by a user action.
     */
    public void rotateKeys() {
        promote();
    }

    /**
     * Part of the Disney-standard key rotation policy: before a key is promoted to primary signing key by the standard
     * timer process, it should be introduced into the set for some amount of time to allow any caches of the keyset
     * to be refreshed with the new key in the set before it is used for signing.  Clients of a JWKS endpoint should
     * cache keysets so that they don't hit JWKS endpoints too much, but the caching should not be for any long amount
     * of time.  Crucially, key cache times should be significantly shorter than the time between a new key introduction
     * and its promotion to primary signing key (the "standby" time).
     */
    private void introduceNewKey() {
        synchronized (holderLock) {
            if (standbyKey == null) {
                standbyKey = makeKeyDataHolder();

                // When introducing a new key, set a timer for that key to be promoted to primary.  This is done so
                // that the key exists in the verification set for some amount of time before it is used for signing,
                // which is useful for when clients cache keyset results.  When clients use caching, then may not
                // have a refreshed set of keys for verification for some time, so that if they receive a JWT signed
                // with a brand-new key they might fail to verify it.
                RotationEvent evt = new RotationEvent(RotationEventType.PROMOTE,
                        System.currentTimeMillis() + rotationPolicy.getDelayBeforePromotionToPrimary());
                logger.logVerbose("New key " + standbyKey + " introduced, will be promoted to primary at " +
                        new Date(System.currentTimeMillis() + rotationPolicy.getDelayBeforePromotionToPrimary()));
                rotator.addEvent(evt);
            } else {
                // Atypical case where we want to introduce a new key, but there is already one on standby.  The known
                // use case for this is when a forced key rotation has been done.  Other cases are likely bugs.
                logger.log("Attempting to introduce new key when there is already a standby primary, nothing will be done.");
                logger.log("\tThis is expected after a forced key rotation event, unexpected otherwise.");
            }
        }
    }

    /**
     * Promote: ensure a standby key exists.  Make the standby key the primary signing key.  The previous
     * primary signing key becomes inactive in the verification set until it expires based on the key
     * rotation policy in effect.
     */
    private void promote() {
        synchronized (holderLock) {
            if (standbyKey == null) {
                // This commonly happens when doing user-initiated key rotations.
                standbyKey = makeKeyDataHolder();
            }

            logger.logVerbose("Key promotion event at " + new Date() + ":\n\told primary=" + primary + "\n\tstandby=" + standbyKey +
                    ", standby will become new primary.");
            primary = standbyKey;
            standbyKey = null;
        }

        // When promoting, set a timer so that a new key will be introduced with enough lead time (see comment
        // in introduceNewKey for why this is important).
        RotationEvent evt = new RotationEvent(RotationEventType.INTRO,
                System.currentTimeMillis() + rotationPolicy.getTimeDelayBeforeIntroducingNewStandby());
        logger.logVerbose("Time is " + new Date() + ", new key should be introduced at " +
                new Date(System.currentTimeMillis() + rotationPolicy.getTimeDelayBeforeIntroducingNewStandby()));
        rotator.addEvent(evt);
    }

    /**
     * Convenience method to make a holder object for storing all of the key data necessary for rotating keys,
     * returning JSON key objects, getting quick access to a key ID, etc.  Any new key created is automatically
     * added to the set used to verify tokens.  Any other semantics for the new key is the responsibility of
     * the caller.
     *
     * @return A holder object.
     */
    private KeyDataHolder makeKeyDataHolder() {
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
        String id = UUID.randomUUID().toString();
        RSAKey jsonKeyObj = makeRSAKey(pub, id);

        KeyDataHolder result = new KeyDataHolder(pub, priv, jsonKeyObj);

        // All keys created may be used for verification until expired
        synchronized (holderLock) {
            verificationKeys.add(result);
        }

        return result;
    }

    /**
     * Convenience method to make a JSON object representing a RSA public key.
     *
     * @param pk  A valid RSA public key.
     * @return An object used by the Jackson ObjectMapper to create a JSON string that can be passed over the web
     */
    private RSAKey makeRSAKey(RSAPublicKey pk, String id) {
        RSAKey result = new RSAKey();
        result.setKty(pk.getAlgorithm()); // getAlgorithm() returns kty not algorithm

        result.setKid(id);
        result.setN(Base64.getUrlEncoder().encodeToString(pk.getModulus().toByteArray()));
        result.setE(Base64.getUrlEncoder().encodeToString(pk.getPublicExponent().toByteArray()));
        result.setAlg("RS256");  // See above comment for setKty; getAlgorithm() does not return what you think it should

        return result;
    }

    /**
     * Basic data transformation method to create a Java object representation of the JSON
     * that will be returned by the JWKS endpoint.
     *
     * @return  An array of objects to pass to the Jackson ObjectMapper to be turned into JSON
     * for HTTP response output.
     */
    private RSAKey[] makeRSAKeyArray() {

        // Always make a copy of the outer array.  It is okay to share references of individual RSAKey JSON object
        // representations, but not the containing array
        KeyDataHolder[] currentState;

        // Snapshot copy made under synchronization, used as source data.  The verification key List is a list of all
        // keys that can possibly be used for verification, which includes standby, primary, and inactive keys.
        List<KeyDataHolder> currentVerificationKeys;
        synchronized (holderLock) {
            currentVerificationKeys = new ArrayList<>(verificationKeys);
        }

        // Copy into a JSON-friendly structure.  Note that this method is not a real-time method, it is intended
        // to only be called on a polling thread or as a test user action.
        int size = 1;   // Accounts for primary key, which must exist always
        RSAKey[] result = new RSAKey[currentVerificationKeys.size()];
        int resultIndex = 0;
        for (KeyDataHolder holder : currentVerificationKeys) {
            result[resultIndex++] = holder.getRsaKey();
        }

        return result;
    }

    /**
     * Return the current primary signing key, needed to sign JWTs.
     * @return
     */
    public KeyDataHolder getPrivateKeyForSigning() {
        synchronized (holderLock) {
            return primary;
        }
    }

    private enum RotationEventType { INTRO, PROMOTE };

    private class RotationEvent {
        RotationEventType eType;
        long doAt;

        RotationEvent(RotationEventType type, long doAt) {
            this.eType = type;
            this.doAt = doAt;
        }
    }

    /**
     * Key rotation thread: this class executes the key rotation policy in effect.
     */
    private class KeyRotationManager implements Runnable {

        private List<RotationEvent> events;
        private long lastRun;

        KeyRotationManager() {
            events = new ArrayList<>();
            lastRun = 0;
        }

        public void addEvent(RotationEvent evt) {
            synchronized (events) {
                events.add(evt);
            }
        }

        public void run() {
            try {
                long now = System.currentTimeMillis();

                if (lastRun > 0) {
                    // Flag situations where this thread job didn't run in the expected time window,
                    // using some reasonable threshold.
                    long timeDiff = now - lastRun;
                    if (timeDiff > (rotationPolicy.getPollingInterval() * 2)) {
                        logger.log("WARNING: polling thread not running at expected interval.  Key rotation events");
                        logger.log("\tare unlikely to happen in the expected time, but functionality should still be correct.");
                    }
                }

                // Grab whatever is on the event queue
                List<RotationEvent> eventsDue = new ArrayList<>();
                synchronized (events) {
                    for (Iterator<RotationEvent> evtIter = events.iterator(); evtIter.hasNext(); ) {
                        RotationEvent evt = evtIter.next();
                        if (evt.doAt < now) {
                            eventsDue.add(evt);
                            evtIter.remove();
                        }
                    }
                }

                // Process lifecycle events (intro, promote)
                for (RotationEvent evt : eventsDue) {
                    // Don't check time, that was done above
                    if (evt.eType.equals(RotationEventType.INTRO)) {
                        introduceNewKey();
                    } else if (evt.eType.equals(RotationEventType.PROMOTE)) {
                        promote();
                    }
                }

                // Get rid of expired inactives
                synchronized (verificationKeys) {
                    for (Iterator<KeyDataHolder> inactivesIter = verificationKeys.iterator(); inactivesIter.hasNext(); ) {
                        KeyDataHolder holder = inactivesIter.next();

                        // Even if the key is expired, don't delete a key if it is the primary signing key,
                        // but log an error
                        if (holder.getTimestamp() + rotationPolicy.getTimeUntilDeletion() < now) {
                            if (!holder.equals(primary)) {
                                inactivesIter.remove();
                                logger.logVerbose("Expired key with id " +
                                        holder.getRsaKey().getKid() + " deleted at " + new Date());
                            } else {
                                logger.logVerbose(new Date() + ": Primary signing key with id " +
                                        holder.getRsaKey().getKid() +
                                        " is expired, will not delete.  Key expiration is at " +
                                        new Date(holder.getTimestamp() + rotationPolicy.getTimeUntilDeletion()));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
