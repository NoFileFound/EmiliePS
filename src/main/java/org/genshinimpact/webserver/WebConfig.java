package org.genshinimpact.webserver;

// Imports
import java.util.EnumMap;
import java.util.List;
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class WebConfig {
    public MainConfig mainConfig;
    public MDKConfig mdkConfig;
    public BoxConfig boxConfig;
    public EnumMap<ClientType, List<String>> extensionList;

    public static class MainConfig {
        public int springbootPort = 8881;
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
        public Integer push_alias_type = 2;
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
}