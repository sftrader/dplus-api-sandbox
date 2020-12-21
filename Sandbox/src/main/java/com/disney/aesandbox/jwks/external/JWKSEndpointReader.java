package com.disney.aesandbox.jwks.external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JWKSEndpointReader {

    private String endpoint;

    public JWKSEndpointReader(String jwksEndpointURI) {
        endpoint = jwksEndpointURI;
    }

    public String getContent() {

        String result = null;
        HttpURLConnection conn = null;

        try {

            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("Error return status from HTTP: " + status);
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                result = content.toString();
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {
                conn.disconnect();
            } catch (Exception e2) {
                // do nothing
            }
        }

        return result;
    }
}
