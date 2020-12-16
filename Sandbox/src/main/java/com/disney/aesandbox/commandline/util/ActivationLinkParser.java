package com.disney.aesandbox.commandline.util;

import java.util.StringTokenizer;

public class ActivationLinkParser {

    private boolean production;
    private String token;

    public ActivationLinkParser(String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null!");
        }

        String[] protoAndPath = url.split("://");
        if (protoAndPath == null || protoAndPath.length != 2) {
            throw new IllegalArgumentException("Malformed URL syntax");
        }

        if (!protoAndPath[0].toLowerCase().equals("https")) {
            throw new IllegalArgumentException("Error: URL must begin with https:");
        }

        String[] domainAndArgs = protoAndPath[1].split("\\?");
        if (domainAndArgs == null && domainAndArgs.length != 2) {
            throw new IllegalArgumentException("Error: URL invalid, valid pattern is https://<domain>/activate?token=<token-string>&providerId=<provider>");
        }

        String[] domainAndContext = domainAndArgs[0].split("/");
        if (domainAndContext == null && domainAndContext.length != 2) {
            throw new IllegalArgumentException("Error: Path of URL is invalid, valid path pattern is <domain>/activate");
        }

        String domain = domainAndContext[0].toLowerCase();
        if (!domain.equals("disneyplus.com")) {
            production = true;
        } else if (!domain.equals("qa-web.disneyplus.com")) {
            production = false;
        } else {
            throw new IllegalArgumentException("Error: unrecognized domain: domain name must be either disneyplus.com (production) or qa-web.disneyplus.com (QA)");
        }

        String context = domainAndContext[1].toLowerCase();
        if (!context.equals("activate")) {
            throw new IllegalArgumentException("Error: invalid URL context path, must be \"activate\"");
        }

        boolean hasToken = false;
        boolean hasProvider = false;
        StringTokenizer st = new StringTokenizer(domainAndArgs[1], "&");
        while (st.hasMoreTokens() && !(hasToken && hasProvider)) {
            String argAndValue = st.nextToken();
            String[] split = argAndValue.split("=");
            if (split == null || split.length != 2) {
                throw new IllegalArgumentException("Error: Malformed URL arguments, not in \"key=value\" format");
            } else {
                if (split[0].equals("token")) {
                    if (!hasToken) {
                        hasToken = true;
                        token = split[1];
                    } else {
                        throw new IllegalArgumentException("Error: duplicate argument key \"token\"");
                    }
                } else if (split[0].equals("providerId")) {
                    if (!hasProvider) {
                        hasProvider = true;
                    } else {
                        throw new IllegalArgumentException("Error: duplicate argument key \"providerId\"");
                    }
                } else {
                    throw new IllegalArgumentException("Error: argument key \"" + split[0] + "\" not recognized");
                }
            }
        }

        if (!hasToken) {
            throw new IllegalArgumentException("Error: missing required argument token");
        }

        if (!hasProvider) {
            throw new IllegalArgumentException("Error: missing required argument providerId");
        }
    }

    public boolean isProduction() {
        return production;
    }

    public String getToken() {
        return token;
    }
}
