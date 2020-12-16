package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;
import com.disney.aesandbox.keymgmt.VerificationKeys;
import com.disney.aesandbox.token.TokenUtils;

import java.io.Console;
import java.util.concurrent.TimeUnit;

public class KeyRotationDemoTask extends CommandLineTask {

    public KeyRotationDemoTask() {
        super("Demo a key rotation test by using JWKs and tokens generated from this demo environment.");
    }

    @Override
    public void run() {
        Console cons = System.console();

        try {
            cons.writer().println("\nThis task will demonstrate proper key rotation semantics by performing a key rotation");
            cons.writer().println("\tand validating tokens using this demo environment.");
            cons.writer().println("\nThe JWKS from this demo environment before rotation:");
            cons.writer().println(DemoOnlyTransientKeyManager.INSTANCE.getPrettyPrintedJsonPublicKeySet());
            String tok = createTokenString();
            cons.writer().println("\nUsing the current JWKS, a sample token is\n" + tok);
            TokenUtils.decodeToClaims(tok, new VerificationKeys(DemoOnlyTransientKeyManager.INSTANCE.getJsonPublicKeySet()));
            DemoOnlyTransientKeyManager.INSTANCE.rotateKeys();
            cons.writer().println("\nAfter rotating keys, the new keyset is\n");
            cons.writer().println(DemoOnlyTransientKeyManager.INSTANCE.getPrettyPrintedJsonPublicKeySet());
            String newTok = createTokenString();
            cons.writer().println("\nA new sample token is\n" + newTok);
            TokenUtils.decodeToClaims(newTok, new VerificationKeys(DemoOnlyTransientKeyManager.INSTANCE.getJsonPublicKeySet()));
            cons.writer().println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createTokenString() {
        TokenClaims sampleTokenData = TokenClaims.newActivationClaimsFromJson();
        long nowInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        sampleTokenData.setIat(nowInSeconds);
        sampleTokenData.setExp(nowInSeconds + (60 * 10));

        return TokenUtils.newToken(sampleTokenData);
    }
}
