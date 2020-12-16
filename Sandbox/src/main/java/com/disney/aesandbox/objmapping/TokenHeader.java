package com.disney.aesandbox.objmapping;

public class TokenHeader {

    private String alg;
    private String kid;

    public TokenHeader() {
        setAlg("RS256");
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
}
