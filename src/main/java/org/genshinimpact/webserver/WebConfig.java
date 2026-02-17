package org.genshinimpact.webserver;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class WebConfig {
    public MainConfig mainConfig;
    public MDKConfig mdkConfig;
    public BoxConfig boxConfig;
    public EnumMap<ClientType, List<String>> extensionList;
    public List<RegionConfig> regionConfig = List.of();

    public static class MainConfig {
        public int springbootPort = 8881;
        public String mongodbUrl = "mongodb://localhost:27017";
    }

    public static class MDKConfig {
        public String announce_url = "https://sdk.hoyoverse.com/hk4e/announcement/index.html?sdk_presentation_style=fullscreen&announcement_version=2.43&sdk_screen_transparent=true&game_biz=hk4e_global&auth_appid=announcement&game=hk4e#/";
        public Boolean enable_qrcode_login = true;
        public Boolean enable_ysdk_guard = true;
        public Boolean enable_announce_pic_popup = true;
        public Boolean enable_user_center = true;
        public Boolean enable_appsflyer = false;
        public Boolean enable_ugc_protocol = false;
        public Boolean enable_jpush = false;
        public Boolean enable_email_captcha = true;
        public Boolean enable_mtt = true;
        public Boolean enable_ps_bind_account = true;
        public Boolean enable_flash_login = true;
        public Boolean enable_cx_bind_account = true;
        public Boolean enable_douyin_flash_login = true;
        public Boolean enable_guest = true;
        public Boolean enable_server_guest = true;
        public Boolean enable_regist = true;
        public Boolean enable_firebase = true;
        public Boolean enable_firebase_device_switch = true;
        public Boolean enable_crash_sdk = false;
        public Integer push_alias_type = 2;
        public String ignore_versions = "";
        public Boolean fetch_instance_id = false;
        public Boolean enable_logo_18 = false;
        public Integer enable_logo_18_height = 0;
        public Integer enable_logo_18_width = 0;
        public Boolean bbs_auth_login = false;
        public List<String> bbs_auth_login_ignore = List.of();
        public Boolean hoyolab_auth_login = false;
        public List<String> hoyolab_auth_login_ignore = List.of();
        public Boolean hoyoplay_auth_login = false;
        public Boolean enable_age_gate = false;
        public List<String> enable_age_gate_ignore = List.of();
        public List<String> thirdparty = List.of();
        public Map<String, Map<String, Object>> thirdparty_login_configs = Map.of();
        public List<JsonNode> firebase_blacklist_devices = List.of();
        public List<JsonNode> firebase_blacklist_lowenddevices = List.of();
    }

    public static class BoxConfig {
        public String precache_worker_url = "https://webstatic.mihoyo.com/sw.html";
        public Boolean precache_worker_url_enable = false;
        public Boolean enable_attribution = true;
        public Boolean enable_bind_google_sdk_order = true;
        public Boolean enable_google_cancel_callback = true;
        public Boolean enable_google_credential_login = false;
        public Boolean enable_httpdns = true;
        public Integer enable_httpdns_cache_expire_time = 30;
        public Integer enable_httpdns_keep_alive_time = 5;
        public Boolean enable_twitter_v2 = false;
        public Boolean enable_oaid = true;
        public Boolean enable_oaid_call_hms = true;
        public Boolean enable_oaid_multi_process = true;
        public Boolean enable_register_autologin = true;
        public Boolean enable_new_register_page = false;
        public Boolean enable_telemetry_h5log = true;
        public Boolean enable_telemetry_data_upload = true;
        public Boolean enable_user_center_v2 = true;
        public Boolean enable_list_price_tierv2 = false;
        public Boolean enable_os_new_rsa_lib = true;
        public Boolean enable_available_rom_v2 = false;
        public Boolean enable_login_flow_notification = true;
        public Boolean enable_spint_prodqa_realname = false;
        public Boolean enable_web_dpi = false;
        public Boolean enable_apm_sdk = true;
        public Boolean enable_kcp_connection = true;
        public Boolean enable_modify_real_name_other_verify = false;
        public Boolean enable_email_register_hide = false;
        public Boolean enable_email_bind_skip = true;
        public Boolean enable_email_bind_remind = true;
        public Integer enable_email_bind_remind_interval = 5;
        public Boolean enable_h5_cashier = false;
        public Integer enable_h5_cashier_timeout = 0;
        public Boolean enable_pay_platform_block_h5_cashier = false;
        public Integer enable_pay_platform_h5_loading_limit = 3;
        public Boolean enable_login_record_check = false;
        public Boolean enable_account_list_page = true;
        public Boolean enable_new_forgotpwd_page = true;
        public Boolean enable_web_apmconfig_crash_capture = false;
        public Boolean enable_webview_rendermethod_config_legacy = false;
        public Boolean enable_appsflyer_config = true;
        public Boolean enable_watermark_web_notice = true;
        public Boolean enable_alipay_recommend = true;
        public Boolean enable_consent_by_country = false;
        public List<String> set_consent_false_country;
        public Boolean enable_domain_region = true;
        public Boolean disable_try_verify = true;
        public NetworkReportConfig network_report_config = new NetworkReportConfig();
        public H5LogFilterConfig h5log_filter_config = new H5LogFilterConfig();
        public KibanaPCConfig kibana_pc_config = new KibanaPCConfig();
        public PS4BindMobileConfig ps4_bind_mobile_config = new PS4BindMobileConfig();
        public PorteCnConfig porte_cn_config = new PorteCnConfig();
        public PorteOsKibanaConfig porte_os_kibana_box = new PorteOsKibanaConfig();
        public Integer console_login_method = 0;

        public static class NetworkReportConfig {
            public Integer enable = 1;
            public List<Integer> status_codes = List.of(200);
            public List<String> url_paths = List.of("combo/postman/device/setAlias");
        }

        public static class H5LogFilterConfig {
            public Function function = new Function();
            public static class Function {
                public List<String> event_name = List.of();
            }
        }

        public static class KibanaPCConfig {
            public Integer enable = 1;
            public String level = "Debug";
            public List<String> modules = List.of();
        }

        public static class PS4BindMobileConfig {
            public Boolean enable = true;
            public Integer cd = 10;
            public Boolean show_skip;
        }

        public static class PorteCnConfig {
            public Boolean downgrade = true;
            public Integer disabledVersionsBelow = 0;
            public List<String> disabledDeviceModels = List.of();
        }

        public static class PorteOsKibanaConfig {
            public boolean enabled = true;
            public int minUploadEventTriggerSize = 10;
            public int uploadEventPageSize = 50;
            public int minimumUploadInterval = 10;
            public List<String> disabledPaths = List.of();
        }
    }

    public static class RegionConfig {
        public String name;
        public String title;
        public String regionType;
        public String dispatchUrl;
        public String dispatchIp;
        public Integer dispatchPort;
        public List<String> dispatchVersions;
        public String dispatchTicket;
        public ResourceConfig resourceConfig;
        public JsonNode encryptedConfig;
        public Maintenance maintenanceConfig = new Maintenance();

        public static class ResourceConfig {
            public String area_type = "";
            public String game_biz = "";
            public String data_url = "";
            public String resource_url = "";
            public String next_resource_url = "";
            public String resource_url_bak = "";
            public Integer client_data_version = 0;
            public Integer client_silence_data_version = 0;
            public String client_version_suffix = "";
            public String client_silence_version_suffix = "";
            public String pay_callback_url = "";
            public String cdkey_url = "";
            public String privacy_policy_url = "";
            public String account_bind_url = "";
            public String official_community_url = "";
            public String handbook_url = "";
            public String feedback_url = "";
            public String bulletin_url = "";
            public String user_center_url = "";
            public JsonNode client_data_md5 = null;
            public JsonNode client_silence_data_md5 = null;
            public ResourceVersionConfig res_version_config = new ResourceVersionConfig();
            public ResourceVersionConfig next_res_version_config = new ResourceVersionConfig();
            public static class ResourceVersionConfig {
                public boolean re_login = true;
                public JsonNode md5 = null;
                public int version = 0;
                public String release_total_size = "0";
                public String version_suffix = "";
                public String branch = "";
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