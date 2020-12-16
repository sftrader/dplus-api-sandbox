package com.disney.aesandbox.token;

import com.disney.aesandbox.objmapping.TokenClaims;
import com.disney.aesandbox.objmapping.TokenHeader;
import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;
import com.disney.aesandbox.keymgmt.KeyDataHolder;
import com.disney.aesandbox.keymgmt.VerificationKeys;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * This token creation/validation service uses ordinary JDK objects to create and validate JWTs.  While there
 * are more full-featured open-source libraries providing JWT capabilities, for simple token creation and
 * validation plain JDK objects are sufficient, so getting approvals for open-source packages is less necessary.
 */
public class TokenUtils {

    /**
     * This method takes a Java object representation of JSON claims (Jackson-compatible) and creates
     * a signed JWT.
     *
     * @param claims
     * @return
     */
    public static String newToken(TokenClaims claims) {

        try {
            KeyDataHolder holder = getPrivateSigningKey();
            TokenHeader header = new TokenHeader();
            header.setKid(holder.getRsaKey().getKid());
            ObjectMapper mapper = new ObjectMapper();
            String headerJson = mapper.writeValueAsString(header);
            String claimsJson = mapper.writeValueAsString(claims);

            String encodedHeader = new String(Base64.getUrlEncoder().withoutPadding().encode(headerJson.getBytes(US_ASCII)));
            String encodedClaims = new String(Base64.getUrlEncoder().withoutPadding().encode(claimsJson.getBytes(US_ASCII)));
            String signedHash = sign(encodedHeader + "." + encodedClaims, holder.getPrivateKey());

            return encodedHeader + "." + encodedClaims + "." + signedHash;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Return a private signing key (only private keys should be used for signing).
     * @return
     */
    private static KeyDataHolder getPrivateSigningKey() {
        return DemoOnlyTransientKeyManager.INSTANCE.getPrivateKeyForSigning();
    }

    /**
     * Decode a signed JWT into a Jackson-compatible Java object created from the claims JSON.  Validate the
     * claims against an available JWKS, or flag as not validated if validation fails.
     *
     * FIXME: Currently only the internally-generated JWKS is supported, TODO: enable external public key
     * JWKSs to be used.
     *
     * @param token
     * @return
     */
    public static TokenClaims decodeToClaims(String token, VerificationKeys vKeys) {
        try {
            String[] parts = token.split("\\.");

            // Three parts: part 0 is the header, part 1 is the claims, part 2 is the signature
            ObjectMapper mapper = new ObjectMapper();
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            TokenHeader header = mapper.reader().readValue(headerJson, TokenHeader.class);
            String claimsJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            TokenClaims claims = mapper.reader().readValue(claimsJson, TokenClaims.class);

            int i = 0;
            boolean isValid = false;
            boolean validSig = false;

            // Validate against the key with matching kid
            PublicKey pk = vKeys.getVerificationKey(header.getKid());
            isValid = (pk != null);
            if (isValid) {
                // To sign, we take the first two segments (dot-separated), get the bytes, and use that to
                // feed the Signature object
                validSig = isValid ? validateSig(parts[0] + "." + parts[1], parts[2], pk) : false;
            }

            if (validSig) {
                System.out.println("The token signature vas validated with key ID " + header.getKid() + ".");
            } else {
                System.out.println("The token signature with key ID " + header.getKid() + " could not be validated with any known key.");
            }

            return claims;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a URL-safe Base64-encoded signed text string (i.e. sign, then encode)
     *
     * @param plainText
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(plainText.getBytes(US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sig.sign());
    }

    /**
     * Validate that a signed, encoded string came from given source text.
     * @param toBeValidated The source text
     * @param signedEncoded The signed, validated string that was supposedly created from the source text previously
     * @param publicKey The public key counterpart to the private key supposedly used to create the signedEncoded string
     * @return
     */
    public static boolean validateSig(String toBeValidated, String signedEncoded, PublicKey publicKey) {
        // To validate, we put the first two segments back together separated by a period, get the
        // bytes from that string, and use those bytes to feed the Signature instance
        // for verification
        try {
            byte[] bytesFromSignedEncoded = Base64.getUrlDecoder().decode(signedEncoded);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(toBeValidated.getBytes(US_ASCII));

            return sig.verify(bytesFromSignedEncoded);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
