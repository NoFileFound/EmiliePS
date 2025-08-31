package org.emilieps.properties.configs;

// Imports
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import org.emilieps.Application;
import org.emilieps.properties.Property;
import org.emilieps.libraries.JsonLoader;

public final class RegionConfig implements Property {
    private RegionConfigClass regionInstance;

    @Override
    public Object getInstance() {
        return this.regionInstance;
    }

    @Override
    public void loadFile() {
        this.regionInstance = JsonLoader.loadJson("region.json", RegionConfigClass.class);
        if(this.regionInstance == null) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "failedtoinitfile", "region.json"));
            this.regionInstance = new RegionConfigClass();
        }
    }

    @Override
    public void saveFile() {

    }

    public static class RegionConfigClass {
        public ArrayList<RegionClass> regions = new ArrayList<>();
        public boolean enable_login_pc = true;
        public ObjectNode custom_config = (ObjectNode) JsonLoader.parseJsonSafe("{\"sdkenv\":1}");

        // Classes
        public static class RegionClass {
            public String gateserver_name = "os_localhost";
            public String gateserver_title = "Localhost";
            public String gateserver_type = "DEV_PUBLIC";
            public String gateserver_domain_name = "";
            public String gateserver_ip = "127.0.0.1";
            public String gateserver_ipv6_ip = "";
            public Integer gateserver_port = 8882;
            public String gateserver_version = "5.0.0";
            public String connect_gate_ticket = "";
            public ResourceConfig resource_config = new ResourceConfig();
            public ObjectNode custom_config = (ObjectNode) JsonLoader.parseJsonSafe("{}");
            public Maintenance maintenance = new Maintenance();

            public static class ResourceConfig {
                public String area_type = "CN";
                public String game_biz = "hk4e_cn";
                public String data_url = "https://autopatchcn.yuanshen.com/client_design_data/5.0_live";
                public String resource_url = "https://autopatchcn.yuanshen.com/client_game_res/5.0_live";
                public String next_resource_url = "https://autopatchcn.yuanshen.com/client_game_res/5.0_live";
                public String resource_url_bak = "5.0_live";
                public Integer client_data_version = 26487341;
                public Integer client_silence_data_version = 0;
                public String client_version_suffix = "57a90bbd52";
                public String client_silence_version_suffix = "";
                public String pay_callback_url = "http://10.101.11.129:22601/recharge";
                public String cdkey_url = "https://public-operation-hk4e.mihoyo.com/common/apicdkey/api/exchangeCdkey?sign_type=2&auth_appid=apicdkey&authkey_ver=1";
                public String privacy_policy_url = "https://user.mihoyo.com/#/about/privacyInGame?app_id=4&lang=zh-cn&biz=hk4e_cn";
                public String account_bind_url = "https://user.mihoyo.com";
                public String official_community_url = "https://act.mihoyo.com/ys/event/community-content-collection/index.html?game_biz=hk4e_cn&sign_type=2&auth_appid=contenthub&authkey_ver=1&utm_source=game&utm_medium=ys&utm_campaign=menu&red_point=2214,2215";
                public String handbook_url = "https://webstatic.mihoyo.com/ys/event/blue-post/index.html?gamewebview=1&page_sn=b480280b939e4bbd&mode=fullscreen&game_biz=hk4e_cn&sign_type=2&auth_appid=bluepost&authkey_ver=1&utm_source=game&utm_medium=ys&utm_campaign=hot";
                public String feedback_url = "https://webstatic.mihoyo.com/csc-service-center-fe/index.html?goToHall=true&sign_type=2&auth_appid=csc&authkey_ver=1&win_direction=portrait&login_type=account&page_id=1";
                public String bulletin_url = "";
                public String user_center_url = "";
                public ArrayNode client_data_md5 = (ArrayNode) JsonLoader.parseJsonSafe("[{\"remoteName\":\"data_versions\",\"md5\":\"88a0d1f6825ec3b6aaf9ea39a02f78da\",\"hash\":\"a72baf3b5c76f0ac\",\"fileSize\":68545},{\"remoteName\":\"data_versions_medium\",\"md5\":\"9429b4e9dd8cbdaf19c41ff05f18b384\",\"hash\":\"a79950c775cf1630\",\"fileSize\":6662}]");
                public ObjectNode client_silence_data_md5 = (ObjectNode) JsonLoader.parseJsonSafe("{\"remoteName\": \"data_versions\", \"md5\": \"8ae3d12ddeffa27349ab306ce05ec0b7\", \"hash\": \"ef8cb53633584c7a\", \"fileSize\": 522}");
                public ResourceVersionConfig res_version_config = new ResourceVersionConfig();
                public ResourceVersionConfig next_res_version_config = new ResourceVersionConfig();
                public static class ResourceVersionConfig {
                    public boolean re_login = true;
                    public ObjectNode md5 = (ObjectNode) JsonLoader.parseJsonSafe("{\"remoteName\": \"base_revision\", \"md5\": \"149cad27b543e345df504a496949ec7d\", \"fileSize\": 19}");
                    public int version = 26458901;
                    public String release_total_size = "0";
                    public String version_suffix = "befdda25ff";
                    public String branch = "5.0_live";
                }
            }

            public static class Maintenance {
                public String url;
                public Integer startDate;
                public Integer endDate;
                public String msg;
            }
        }
    }
}