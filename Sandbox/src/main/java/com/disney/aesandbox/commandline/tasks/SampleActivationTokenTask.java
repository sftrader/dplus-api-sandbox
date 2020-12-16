package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.token.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Console;
import java.io.File;
import java.util.concurrent.TimeUnit;

public abstract class SampleActivationTokenTask extends CommandLineTask {

    private long lifespanInSeconds;

    protected SampleActivationTokenTask(long lifespanInSeconds, String title) {
        super(title);
        this.lifespanInSeconds = lifespanInSeconds;
    }

    private long getLifespan() {
        return lifespanInSeconds;
    }

    @Override
    public void run() {
        try {
            TokenClaims sampleTokenData = TokenClaims.newActivationClaimsFromJson();
            long nowInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            sampleTokenData.setIat(nowInSeconds);
            sampleTokenData.setExp(nowInSeconds + getLifespan());
            Console cons = System.console();
            printJson(cons, sampleTokenData.toString());
            cons.writer().println("");
            String tokenString = TokenUtils.newToken(sampleTokenData);
            printToken(cons, tokenString);
            cons.writer().println("");

            String fullPath = (new File("Sandbox/src/main/resources/token/json/activation/default_values.objmapping")).getAbsolutePath();
            cons.writer().println("The token claims are from the sample settings in " + fullPath);
            cons.writer().println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printJson(Console cons, String json) {
        cons.writer().println("JSON source for token: ");

        try {
            // Pretty-print
            ObjectMapper mapper = new ObjectMapper();
            Object obj = mapper.readValue(json, Object.class);
            cons.writer().println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj));
        } catch (Exception e) {
            e.printStackTrace();
            cons.writer().println(json);
        }
    }

    private void printToken(Console cons, String token) {
        cons.writer().println("The signed, Base-64-encoded, URL-friendly token string is ");
        cons.writer().println(token);
        cons.writer().println("Try pasting the above string into http://jwt.io");
    }
}
