package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.jwks.external.JWKSEndpointReader;
import com.disney.aesandbox.commandline.util.ActivationLinkParser;
import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;
import com.disney.aesandbox.keymgmt.VerificationKeys;
import com.disney.aesandbox.token.TokenUtils;

import java.io.*;

public class ValidateActivationLinkTask extends CommandLineTask {

    private static final String[] ACTIVATION_AUD = { "activation.disneyplus.com"};

    public ValidateActivationLinkTask() {
        super("Validate an activation URL");
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

            cons.writer().print("Paste an activation link to validate: ");
            cons.writer().flush();
            String url = null;
            ActivationLinkParser parser = null;
            try {
                url = getInput();
                parser = new ActivationLinkParser(url);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Errors found in activation link.");
                return;
            }

            TokenClaims claims = TokenUtils.decodeToClaims(parser.getToken(), vKeys);
            boolean isValid = (claims != null) && claims.validateClaims(cons, ACTIVATION_AUD, true);

            if (!isValid) {
                cons.writer().println("\nErrors found, the token will not work.\n");
            } else {
                cons.writer().println("\nActivation link successfully validated.\n");
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
