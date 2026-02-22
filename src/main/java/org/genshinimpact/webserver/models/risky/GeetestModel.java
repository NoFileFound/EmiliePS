package org.genshinimpact.webserver.models.risky;

@SuppressWarnings("unused")
public class GeetestModel {
    public Integer success;
    public String gt;
    public String challenge;
    public Integer is_new_captcha;

    public GeetestModel(String gt, String challenge) {
        this.success = (challenge.isEmpty() ? 0 : 1);
        this.gt = gt;
        this.challenge = challenge;
        this.is_new_captcha = 1;
    }
}