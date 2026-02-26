package org.genshinimpact.webserver.responses.combo.box;

public class BoxPreCacheResponse {
    public Vals vals;

    public BoxPreCacheResponse(String url, String enable) {
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