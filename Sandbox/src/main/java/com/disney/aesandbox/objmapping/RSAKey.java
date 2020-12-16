package com.disney.aesandbox.objmapping;

public class RSAKey {

    private String kty;
    private String n;
    private String e;
    private String alg;
    private String kid;

    public RSAKey() {
        kty = "RSA";
        alg = "RS256";
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String toString() {
        return "RSAKey: id=" + getKid();
    }

    public int hashCode() {
        return getKid().hashCode();
    }

    public boolean equals(RSAKey other) {
        try {
            return getKid().equals(((RSAKey) other).getKid());
        } catch (ClassCastException cce) {
            return false;
        }
    }
 }
