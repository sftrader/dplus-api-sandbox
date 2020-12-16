package com.disney.aesandbox.keymgmt;

import com.disney.aesandbox.objmapping.Keys;
import com.disney.aesandbox.objmapping.RSAKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transformation class to simplify accessing public keys used for verifying signatures.
 */
public class VerificationKeys {

    private Map<String, PublicKey> keyMap;

    public VerificationKeys(String jwksJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Keys keySet = mapper.readValue(jwksJson, Keys.class);
        initFromKeys(keySet);
    }

    public VerificationKeys(Keys objectSource) throws Exception {
        initFromKeys(objectSource);
    }

    private void initFromKeys(Keys objectSource) throws Exception {
        keyMap = new HashMap<>(objectSource.getKeys().length);
        for (RSAKey key : objectSource.getKeys()) {
            keyMap.put(key.getKid(), makeKey(key.getN(), key.getE()));
        }
    }

    public PublicKey getVerificationKey(String kid) {
        return keyMap.get(kid);
    }

    private PublicKey makeKey(String mod, String exp) throws Exception {
        BigInteger modulus = new BigInteger(Base64.getUrlDecoder().decode(mod));
        BigInteger publicExp = new BigInteger(Base64.getUrlDecoder().decode(exp));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExp);
        KeyFactory fac = KeyFactory.getInstance("RSA");
        return fac.generatePublic(spec);
    }
}
