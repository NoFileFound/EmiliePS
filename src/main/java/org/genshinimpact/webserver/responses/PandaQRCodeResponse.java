package org.genshinimpact.webserver.responses;

public class PandaQRCodeResponse {
    public String stat;
    public Payload payload;

    public PandaQRCodeResponse(String stat) {
        this.stat = stat;
        this.payload = new Payload("Raw");
    }

    public PandaQRCodeResponse(String stat, String payload) {
        this.stat = stat;
        this.payload = new Payload("Account", payload, "");
    }

    public static class Payload {
        public String proto;
        public String raw; /// TODO: "{\"uid\":\"123\",\"mid\":\"123_mhy\",\"is_v2_token\":true,\"token\":\"v2_123\",\"is_bbs\":\"true\"}"
        public String ext;
        ///  TODO: realname_info

        public Payload(String proto) {
            this.proto = proto;
            this.raw = "";
            this.ext = "";
        }

        public Payload(String proto, String raw, String ext) {
            this.proto = proto;
            this.raw = raw;
            this.ext = ext;
        }
    }
}