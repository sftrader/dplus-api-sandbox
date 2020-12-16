package com.disney.aesandbox.commandline.tasks;

import com.disney.aesandbox.commandline.CommandLineTask;
import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.token.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Console;
import java.io.File;

public class SampleEntitlementTokenTask extends CommandLineTask {

    public SampleEntitlementTokenTask() {
        super("Create Sample Entitlement Tokens (GET and PUT) using an internally-generated private key for signing.");
    }

    @Override
    public void run() {
        try {
            String getJson = TokenClaims.getDefaultGetEntitlementJson();
            String setJson = TokenClaims.getDefaultSetEntitlementJson();
            String bothJson = TokenClaims.getDefaultBothEntitlementJson();
            TokenClaims getToken = TokenClaims.newFromJson(getJson);
            TokenClaims setToken = TokenClaims.newFromJson(setJson);
            TokenClaims bothToken = TokenClaims.newFromJson(bothJson);
            String getPath = (new File("Sandbox/src/main/resources/token/json/entitlement/get/default_values.objmapping")).getAbsolutePath();
            String setPath = (new File("Sandbox/src/main/resources/token/json/entitlement/set/default_values.objmapping")).getAbsolutePath();
            String bothPath = (new File("Sandbox/src/main/resources/token/json/entitlement/default_values.objmapping")).getAbsolutePath();
            Console cons = System.console();

            printSection(cons, "JSON source for a GET entitlement token:", getJson, TokenUtils.newToken(getToken), getPath);
            printSection(cons, "JSON source for a SET/PUT entitlement token:", setJson, TokenUtils.newToken(setToken), setPath);
            printSection(cons, "JSON source for a token usable in either a GET or SET/PUT entitlement call:",
                    bothJson, TokenUtils.newToken(bothToken), bothPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printSection(Console cons, String title, String json, String tokenString, String sourcePath) {
        printJson(cons, title, json);
        cons.writer().println("");
        printToken(cons, tokenString);
        cons.writer().println("");
        cons.writer().println("The token claims are from the sample settings in " + sourcePath);
        cons.writer().println("");
    }

    private void printJson(Console cons, String title, String json) {
        cons.writer().println(title);
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
