package com.disney.aesandbox.keymgmt;

import java.util.concurrent.TimeUnit;

/**
 * This class represents a key rotation policy as specified in the DSS Partner Activation & Entitlement API
 * documentation.  The specific policy in that doc is over a 90-day period, this class just abstracts to
 * 90 time units of any duration -- the PRODUCTION version will be the full 90 days, but the TEST version will
 * use some shorter time unit.
 */
public class TimeBasedKeyRotationPolicy {

    private long untilPrimary;
    private long asPrimary;
    private long asInactive;
    private long pollingInterval;

    public static final TimeBasedKeyRotationPolicy PRODUCTION =
            new TimeBasedKeyRotationPolicy(
                    TimeUnit.DAYS.toMillis(3),      // Wait this long after introduction to become primary signing key
                    TimeUnit.DAYS.toMillis(80),     // Remain as primary signing key for this long
                    TimeUnit.DAYS.toMillis(7),      // After being rotated out as primary signing key, remain as validation key this long
                    TimeUnit.MINUTES.toMillis(10)); // Polling frequency

    public static final TimeBasedKeyRotationPolicy TEST =
            new TimeBasedKeyRotationPolicy(                 // See above comments for the meaning of the time
                    TimeUnit.MINUTES.toMillis(5),
                    TimeUnit.MINUTES.toMillis(25),
                    TimeUnit.MINUTES.toMillis(60),  // Abnormally long for production, okay in test
                    TimeUnit.MINUTES.toMillis(1));

    private TimeBasedKeyRotationPolicy(long untilPrimary, long asPrimary, long asInactive, long pollingInterval) {
        this.untilPrimary = untilPrimary;
        this.asPrimary = asPrimary;
        this.asInactive = asInactive;
        this.pollingInterval = pollingInterval;
    }

    public long getDelayBeforePromotionToPrimary() {
        return untilPrimary;
    }

    public long getTimeDelayBeforeIntroducingNewStandby() {
        return asPrimary - untilPrimary;
    }

    public long getTimeUntilDeletion() {
        return untilPrimary + asPrimary + asInactive;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }
}
