package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.jwks.external.JWKSEndpointReader;
import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;
import com.disney.aesandbox.keymgmt.VerificationKeys;
import com.disney.aesandbox.token.TokenUtils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;

public class ValidateSetEntitlementTokenTask extends CommandLineTask {

    private static final String[] SET_ENTITLEMENT_AUD = { "set.entitlement.disneyplus.com"};

    public ValidateSetEntitlementTokenTask() {
        super("Validate a token intended for a PUT (i.e. set) entitlement call.");
    }

    @Override
    public void run() {

        try {

            Console cons = System.console();
            cons.writer().println("\nThis task will validate an input token.");
            cons.writer().println("Input the full URL to a valid JWKS endpoint containing a JWK that will validate the token.");
            cons.writer().print("To use the built-in default JWKS running in this process, simply press <RETURN> witnout inputting any URL: ");
            cons.writer().flush();
            String uri = cons.readLine();
            String keyJson = null;
            if (uri == null || uri.trim().length() == 0) {
                keyJson = DemoOnlyTransientKeyManager.INSTANCE.getJsonPublicKeySet();
            } else {
                JWKSEndpointReader endpointReader = new JWKSEndpointReader(uri);
                keyJson = endpointReader.getContent();
            }

            VerificationKeys vKeys = new VerificationKeys(keyJson);

            cons.writer().println("");
            cons.writer().print("Paste a SET entitlement token string to validate: ");
            cons.writer().flush();
            String token = null;
            try {
                token = getInput();
            } catch (Exception e) {
                e.printStackTrace();
            }

            TokenClaims claims = TokenUtils.decodeToClaims(token, vKeys);
            boolean isValid = (claims != null) && claims.validateClaims(cons, SET_ENTITLEMENT_AUD, false);

            if (!isValid) {
                cons.writer().println("\nErrors found, the token will not work.\n");
            } else {
                cons.writer().println("\nEntitlement token successfully validated.\n");
            }

        } catch (Exception e) {

            e.printStackTrace();

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
