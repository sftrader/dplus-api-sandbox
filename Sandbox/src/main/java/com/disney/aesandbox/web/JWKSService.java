package com.disney.aesandbox.web;

import com.disney.aesandbox.keymgmt.DemoOnlyTransientKeyManager;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/jwks")
public class JWKSService {

    // Publishes the public key (Base64 URL encoded) to the /pk endpoint, so that client apps can
    // verify signed content from these services.
    @Get(produces = MediaType.TEXT_PLAIN)
    public String index() {
        return DemoOnlyTransientKeyManager.INSTANCE.getJsonPublicKeySet();
    }

    @Get(uri = "/rotate", produces = MediaType.TEXT_PLAIN)
    public String rotate() {
        DemoOnlyTransientKeyManager.INSTANCE.rotateKeys();
        return index();
    }
}
