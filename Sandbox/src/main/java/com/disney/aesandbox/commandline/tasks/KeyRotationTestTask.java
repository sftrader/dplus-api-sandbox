package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.jwks.external.JWKSEndpointReader;
import com.disney.aesandbox.keymgmt.VerificationKeys;
import com.disney.aesandbox.objmapping.Keys;
import com.disney.aesandbox.objmapping.RSAKey;
import com.disney.aesandbox.token.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class KeyRotationTestTask extends CommandLineTask {

    public KeyRotationTestTask() {
        super("Test key rotation semantics by using input JWKS and token values.");
    }

    @Override
    public void run() {
        Console cons = System.console();

        cons.writer().println("\nThis task will test proper key rotation using a JWKS endpoint.");
        cons.writer().println("In order to successfully complete this task, you need to be able to:");
        cons.writer().println("\t- Specify a JWKS endpoint that can be reached over the open internet.");
        cons.writer().println("\t- Create signed tokens using private keys from that JWKS endpoint.");
        cons.writer().println("\t- Be able to rotate keys in the endpoint on demand.");
        cons.writer().print("Proceed (y/n)? ");
        cons.writer().flush();
        String input = cons.readLine().toLowerCase();
        if (input.equals("y")) {

            try {
                cons.writer().print("\nInput a URL to a valid JWKS endpoint: ");
                cons.writer().flush();
                String uri = cons.readLine();
                JWKSEndpointReader endpointReader = new JWKSEndpointReader(uri);
                String preRotateJwks = endpointReader.getContent();

                if (preRotateJwks != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    Keys preRotateKeySet = mapper.readValue(preRotateJwks, Keys.class);
                    if (preRotateKeySet != null && preRotateKeySet.getKeys().length > 0) {

                        printKeys(cons,"\nHere are the current key IDs from that endpoint:", preRotateKeySet);

                        cons.writer().print("\nInput (paste) a token (activation or entitlement) created from the current signing key belonging to the keyset: ");
                        cons.writer().flush();
                        String token = getInput();
                        if (token != null) {

                            cons.writer().println("");
                            TokenUtils.decodeToClaims(token, new VerificationKeys(preRotateKeySet));
                            cons.writer().print("\nNow rotate the keys.  Press <RETURN> when done...");
                            cons.writer().flush();
                            cons.readLine();

                            String postRotateJwks = endpointReader.getContent();
                            Keys postRotateKeySet = mapper.readValue(postRotateJwks, Keys.class);
                            printKeys(cons,"\nHere are the  key IDs after rotation:", postRotateKeySet);
                            if (validateKeySetsDifferent(preRotateKeySet, postRotateKeySet)) {
                                cons.writer().println("\nThe keysets are different, key rotation succeeded.");
                            } else {
                                cons.writer().println("\nThe keysets are NOT different, key rotation FAILED.");
                            }

                            cons.writer().println("\nAfter rotation, re-validating the token....");
                            TokenUtils.decodeToClaims(token, new VerificationKeys(postRotateKeySet));
                            cons.writer().println("");

                        } else {
                            System.err.println("Error: null token input!");
                        }
                    } else {
                        System.err.println("Empty or null keyset returned from " + uri);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateKeySetsDifferent(Keys set1, Keys set2) {

        boolean result = false;

        if (set1.getKeys().length == set2.getKeys().length) {
            Set<RSAKey> allKeys = new HashSet<>();
            for (RSAKey key : set1.getKeys()) {
                allKeys.add(key);
            }
            for (RSAKey key2 : set2.getKeys()) {
                allKeys.remove(key2);
            }
            result = (allKeys.size() == 0);
        } else {
            result = true;
        }

        return result;
    }

    private void printKeys(Console cons, String message, Keys keySet) {
        cons.writer().println(message);
        for (RSAKey keyObj : keySet.getKeys()) {
            cons.writer().println("\t" + keyObj.getKid());
        }
    }

    private String getInput() {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
