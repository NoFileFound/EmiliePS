package org.genshinimpact.webserver.responses;

public class PreCacheResponse {
    public Vals vals;

    public PreCacheResponse(String url, String enable) {
        this.vals = new Vals(url, enable);
    }

    public static class Vals {
        public String url;
        public String enable;

        public Vals(String url, String enable) {
            this.url = url;
            this.enable = enable;
        }
    }
}