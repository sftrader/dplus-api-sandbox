package com.disney.aesandbox.objmapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class TokenClaims {

    private Map<String, String> address;
    private String[] aud;
    private long exp;
    private long iat;
    private String iss;
    private String[] products;
    private String provider;
    private String sub;

    public static TokenClaims newActivationClaimsFromJson() {
        return newFromJson(getDefaultActivationJson());
    }

    public static TokenClaims newGetEntitlementClaimsFromJson() {
        return newFromJson(getDefaultGetEntitlementJson());
    }

    public static TokenClaims newSetEntitlementClaimsFromJson() {
        return newFromJson(getDefaultSetEntitlementJson());
    }

    public static TokenClaims newBothEntitlementClaimsFromJson() {
        return newFromJson(getDefaultBothEntitlementJson());
    }

    public static TokenClaims newFromJson(String json) {

        TokenClaims result = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(json, TokenClaims.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getDefaultActivationJson() {
        return getDefaultJson("Sandbox/src/main/resources/token/json/activation/default_values.json");
    }

    public static String getDefaultGetEntitlementJson() {
        return getDefaultJson("Sandbox/src/main/resources/token/json/entitlement/get/default_values.json");
    }

    public static String getDefaultSetEntitlementJson() {
        return getDefaultJson("Sandbox/src/main/resources/token/json/entitlement/set/default_values.json");
    }

    public static String getDefaultBothEntitlementJson() {
        return getDefaultJson("Sandbox/src/main/resources/token/json/entitlement/default_values.json");
    }

    private static String getDefaultJson(String path) {

        String result = null;

        try {
            File defaultJson = new File(path);
            LineNumberReader rdr = new LineNumberReader(new FileReader(defaultJson));
            StringBuilder sb = new StringBuilder();
            String line = null;
            do {
                line = rdr.readLine();
                if (line != null) {
                    sb.append(line);
                }
            } while (line != null);

            result = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean validateClaims(Console cons, String[] expectedAud, boolean isActivation) {

        boolean isValid = true;

        // Validate aud
        Object aud = getAud();
        if (aud != null) {
            if (aud instanceof String[]) {
                String[] audArray = (String[]) aud;
                if (audArray.length > 0) {
                    if (audArray.length >= expectedAud.length) {
                        for (String expectedAudStr : expectedAud) {
                            boolean foundAud = false;
                            for (String audStr : audArray) {
                                if (audStr.equals(expectedAudStr)) {
                                    foundAud = true;
                                }
                            }

                            if (!foundAud) {
                                cons.writer().println("Error: \"aud\" claims do not include " + expectedAudStr + ".");
                                isValid = false;
                            }
                        }
                    } else {
                        cons.writer().println("\"aud\" claims found, but the number of claims in the array is less than the expected number.");
                        cons.writer().println("  " + expectedAud.length + " \"aud\" claims were expected");
                        isValid = false;
                    }
                }
            } else if (aud instanceof String) {
                if (expectedAud.length == 1) {
                    String audStr = (String) aud;
                    if (!audStr.equals(expectedAud[0])) {
                        cons.writer().println("Error: \"aud\" claims do not include " + expectedAud[0] + ".");
                        isValid = false;
                    }
                } else {
                    cons.writer().println("Expected " + expectedAud.length + " \"aud\" claims, but found 1.");
                    isValid = false;
                }
            }
        } else {
            cons.writer().println("Missing \"aud\" claim in token");
            isValid = false;
        }

        // Validate products
        if (isActivation) {
            Object products = getProducts();
            if (products != null) {
                if (! (products instanceof String[])) {
                    cons.writer().println("\"products\" claims found, but the value must be a String array type");
                    isValid = false;
                }
            } else {
                cons.writer().println("Missing \"products\" claim in token");
                isValid = false;
            }
        }

        // Validate provider
        Object provider = getProvider();
        if (provider != null) {
            if (!(provider instanceof String)) {
                cons.writer().println("\"provider\" claims found, but the value must be a String type");
                isValid = false;
            }
        } else {
            cons.writer().println("Missing \"provider\" claim in token");
            isValid = false;
        }

        // Validate issuer (iss)
        Object iss = getIss();
        if (iss != null) {
            if (!(iss instanceof String)) {
                cons.writer().println("\"iss\" claims found, but the value must be a String type");
                isValid = false;
            }
        } else {
            cons.writer().println("Missing \"iss\" claim in token");
            isValid = false;
        }

        // Validate iat
        Object iat = getIat();
        long iatAsLong = Long.MIN_VALUE;
        if (iat != null) {
            if (iat instanceof Long) {
                iatAsLong = ((Long) iat).longValue() * 1000;
                long now = System.currentTimeMillis();
                if (now < iatAsLong) {
                    cons.writer().println("\"iat\" claim must be a date earlier than the current time");
                    isValid = false;
                }
            } else {
                cons.writer().println("\"iat\" claim is not a Long value");
                isValid = false;
            }
        } else {
            cons.writer().println("Missing \"iat\" claim in token");
            isValid = false;
        }

        // Validate exp
        Object exp = getExp();
        if (exp != null) {
            if (exp instanceof Long) {
                long expAsLong = ((Long) exp).longValue() * 1000;
                if (expAsLong < iatAsLong) {
                    cons.writer().println("\"exp\" claim must be a date later than the date indicated by the \"iat\" claim");
                    isValid = false;
                }
            } else {
                cons.writer().println("\"exp\" claim is not a Long value");
                isValid = false;
            }
        } else {
            cons.writer().println("Missing \"exp\" claim in token");
            isValid = false;
        }

        // Validate sub
        if (isActivation) {
            Object sub = getSub();
            if (sub != null) {
                if (!(sub instanceof String)) {
                    cons.writer().println("\"sub\" claims found, but the value must be a String type");
                    isValid = false;
                }
            } else {
                cons.writer().println("Missing \"sub\" claim in token");
                isValid = false;
            }
        }

        return isValid;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    public String[] getAud() {
        return aud;
    }

    public void setAud(String[] aud) {
        this.aud = aud;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String[] getProducts() {
        return products;
    }

    public void setProducts(String[] products) {
        this.products = products;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public Map<String, Object> toMap() {
        Map result = new HashMap();
        result.put("aud", getAud());
        result.put("address", getAddress());
        result.put("exp", getExp());
        result.put("iat", getIat());
        result.put("iss", getIss());
        result.put("products", getProducts());
        result.put("provider", getProvider());
        result.put("sub", getSub());

        return result;
    }

    public String toString() {
        // JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
