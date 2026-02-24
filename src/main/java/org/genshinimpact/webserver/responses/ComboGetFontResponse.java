package org.genshinimpact.webserver.responses;

// Imports
import java.util.List;

public class ComboGetFontResponse {
    public List<Font> fonts;

    public ComboGetFontResponse() {
        this.fonts = List.of();
    }

    public ComboGetFontResponse(List<Font> fonts) {
        this.fonts = fonts;
    }

    public static class Font {
        public String font_id;
        public Integer app_id;
        public String name;
        public String url;
        public String md5;

        public Font(String font_id, Integer app_id, String name, String url, String md5) {
            this.font_id = font_id;
            this.app_id = app_id;
            this.name = name;
            this.url = url;
            this.md5 = md5;
        }
    }
}