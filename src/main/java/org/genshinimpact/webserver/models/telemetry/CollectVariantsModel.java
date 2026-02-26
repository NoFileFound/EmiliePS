package org.genshinimpact.webserver.models.telemetry;

// Imports
import java.util.List;

public class CollectVariantsModel {
    public String productname;
    public Integer version;
    public List<Shader> shaders;

    public static class Shader {
        public String shaderName;
        public List<ShaderVariant> variants;

        public static class ShaderVariant {
            public String keywords;
            public Integer passType;
        }
    }
}