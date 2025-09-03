package org.emilieps.config;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import org.emilieps.Application;
import org.emilieps.data.RegionClass;

// Libraries
import org.emilieps.library.JsonLib;

public final class HttpPropertiesConfig implements Property {
    private HttpConfigClass propertiesInstance;

    @Override
    public Object getInstance() {
        return this.propertiesInstance;
    }

    @Override
    public void loadFile() {
        this.propertiesInstance = JsonLib.loadJson("http_properties.json", HttpConfigClass.class);
        if(this.propertiesInstance == null) {
            this.propertiesInstance = new HttpConfigClass();
            Application.getLogger().warn(Application.getTranslations().get("console", "failedtoinitfile", "http_properties.json"));
        }
    }

    @Override
    public void saveFile() {

    }

    public static class HttpConfigClass {
        public GeetestInfo geetest = new GeetestInfo();
        public boolean enable_qrcode_login = true;
        public String announcement_url = "https://sdk.mihoyo.com/hk4e/announcement/index.html?sdk_presentation_style=fullscreen&sdk_screen_transparent=true&auth_appid=announcement&authkey_ver=1&game_biz=hk4e_cn&sign_type=2&version=2.40&game=hk4e#/";
        public int push_alias_type = 1;
        public String ignore_versions = "";
        public boolean disable_ysdk_guard = false;
        public boolean enable_announce_pic_popup = true;
        public boolean disable_regist = false;
        public boolean enable_heartbeat = false;
        public Integer heartbeat_internal = 0;
        public boolean disable_mmt = false;
        public boolean enable_ps_bind_account = false;
        public boolean initialize_firebase = false;
        public boolean firebase_blacklist_devices_switch = true;
        public int firebase_blacklist_devices_version = 1;
        public JsonNode firebase_blacklist_devices = JsonLib.parseJsonSafe("[]");
        public boolean server_guest = false;
        public boolean enable_guest_login = true;
        public int maximum_guests = 10;
        public boolean fetch_instance_id = false;
        public boolean enable_flash_login = false;
        public boolean enable_douyin_flash_login = false;
        public boolean enable_cx_bind_account = false;
        public boolean hoyoplay_auth_login = false;
        public boolean bbs_auth_login = false;
        public JsonNode bbs_auth_login_ignore = JsonLib.parseJsonSafe("[]");
        public boolean hoyolab_auth_login = false;
        public JsonNode hoyolab_auth_login_ignore = JsonLib.parseJsonSafe("[]");
        public boolean enable_age_gate = false;
        public JsonNode enable_age_gate_ignore = JsonLib.parseJsonSafe("[]");
        public ArrayList<String> thirdparty = new ArrayList<>();
        public JsonNode thirdparty_ignore = JsonLib.parseJsonSafe("{}");
        public JsonNode thirdparty_login_configs = JsonLib.parseJsonSafe("{}");
        public JsonNode functional_switch_configs = JsonLib.parseJsonSafe("{}");
        public AdultLogoOptions logo_eighteenplus = new AdultLogoOptions();
        public JsonNode porte_cn_config = JsonLib.parseJsonSafe("{ \"downgrade\": true, \"disabledVersionsBelow\": 9, \"disabledDeviceModels\": [ \"cloud\", \"arm64\", \"bilibili\", \"tap\", \"ido\", \"tianyiyun\"] }");
        public HttpDnsOptions httpdns = new HttpDnsOptions();
        public ServiceWorkerOptions serviceworker = new ServiceWorkerOptions();
        public KibanaBoxMobile kibanabox_mobile = new KibanaBoxMobile();
        public ComboConfig combo_config = new ComboConfig();
        public ArrayList<MarketingAgreement> marketing_agreements = new ArrayList<>();
        public ArrayList<String> app_ids = new ArrayList<>();
        public SwitchStatus switch_status_map = new SwitchStatus();
        public SMTP email_info;
        public ArrayList<RegionClass> regions = new ArrayList<>();
        public boolean enable_login_pc = true;
        public JsonNode custom_config = JsonLib.parseJsonSafe("{\"sdkenv\":1}");


        // Classes
        public static class GeetestInfo {
            public String gt = "";
            public String private_key = "";
        }

        public static class AdultLogoOptions {
            public boolean enabled = false;
            public int logo_height = 0;
            public int logo_width = 0;
        }

        public static class HttpDnsOptions {
            public boolean enabled = true;
            public int cache_expire_time = 60;
            public int keep_alive_time = 60;
        }

        public static class ServiceWorkerOptions {
            public boolean enabled = true;
            public String url = "https://sdk.mihoyo.com/sw.html";
        }

        public static class KibanaBoxMobile {
            public boolean enabled = true;
            public int minimum_upload_interval = 10;
            public int upload_event_page_size = 50;
            public int minimum_upload_event_trigger_size = 10;
            public ArrayList<String> disabled_paths = new ArrayList<>();
        }

        public static class ComboConfig {
            public boolean enable_apm_sdk = true;
            public boolean enable_attribution = true;
            public boolean enable_appsflyer_config = true;
            public boolean enable_kcp = true;
            public boolean enable_list_price_tierv2 = true;
            public boolean enable_new_register_page = true;
            public boolean enable_register_autologin = true;
            public boolean enable_telemetry_h5log = true;
            public boolean enable_telemetry_data_upload = true;
            public boolean enable_user_center_v2 = true;
            public boolean enable_spint_prodqa_realname = true;
            public boolean enable_os_new_rsa_lib = true;
            public boolean enable_twitter_v2 = true;
            public boolean enable_web_dpi = true;
            public boolean email_bind_remind = true;
            public int email_bind_remind_interval = 7;
            public boolean disable_email_bind_skip = false;
            public boolean disable_try_verify = false;
            public int console_login_method = 0;
            public boolean modify_real_name_other_verify = true;
            public boolean email_register_hide = false;
            public boolean enable_oaid = true;
            public boolean oaid_call_hms = true;
            public boolean oaid_multi_process = true;
            public JsonNode oaid_expire_time = JsonLib.parseJsonSafe("{\"huawei\":6000,\"honor\":6000}");
            public JsonNode bili_pay_cancel_strings = JsonLib.parseJsonSafe("[\"用户取消交易\"]\n");
            public boolean alipay_recommend = true;
            public boolean enable_bind_google_sdk_order = true;
            public boolean enable_google_credential_login = true;
            public boolean enable_google_cancel_callback = true;
            public boolean watermark_enable_web_notice = true;
            public boolean pay_platform_block_h5_cashier = true;
            public boolean h5_cashier_enable = true;
            public int h5_cashier_timeout = 3;
            public int pay_platform_h5_loading_limit = 3;
            public boolean enable_available_rom_v2 = true;
            public boolean account_list_page_enable = true;
            public boolean new_forgotpwd_page_enable = true;
            public boolean login_flow_notification = true;
            public String pay_payco_centered_host = "bill.payco.com";
            public boolean crash_capture_enable = true;
            public boolean webview_rendermethod_config = true;
            public boolean login_record_config = true;
            public JsonNode kibana_pc_config = JsonLib.parseJsonSafe("{ \"enable\": 1, \"level\": \"Info\", \"modules\": [\"download\"] }");
            public JsonNode network_report_config = JsonLib.parseJsonSafe("{ \"enable\": 1, \"status_codes\": [], \"url_paths\": [\"/dataUpload\",\"combo/postman/device/setAlias\"] }");
            public JsonNode h5log_filter_config = JsonLib.parseJsonSafe("{\n\t\"function\": {\n\t\t\"event_name\": [\"info_get_cps\", \"notice_close_notice\", \"info_get_uapc\", \"report_set_info\", \"info_get_channel_id\", \"info_get_sub_channel_id\"]\n\t}\n}");
            public JsonNode report_black_list = JsonLib.parseJsonSafe("{ \"key\": [\"download_update_progress\"] }");
        }

        public static class MarketingAgreement {
            public int agreement_id;
            public int agreement_version;
            public String content_url;
            public String show_dialog_reason;
            public String title;
            public String user_status;
        }

        public static class SwitchStatus {
            public boolean enable_ui_v2 = true;
            public JsonNode ui_v2_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_apple_login = true;
            public JsonNode apple_login_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_password_reset_entry = true;
            public JsonNode password_reset_entry_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_vn_real_name_v2 = false;
            public JsonNode vn_real_name_v2_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_common_question_entry = true;
            public JsonNode common_question_entry_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_pwd_login_tab = true;
            public JsonNode pwd_login_tab_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_vn_real_name = false;
            public JsonNode vn_real_name_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_firebase_return_unmasked_email = false;
            public JsonNode firebase_return_unmasked_email_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_google_login = true;
            public JsonNode google_login_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_bind_user_thirdparty_email = true;
            public JsonNode bind_user_thirdparty_email_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_third_party_bind_email = true;
            public JsonNode third_party_bind_email_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_bind_thirdparty = false;
            public JsonNode bind_thirdparty_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_user_name_bind_email = true;
            public JsonNode user_name_bind_email_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_account_register_tab = true;
            public JsonNode account_register_tab_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_twitter_login = true;
            public JsonNode twitter_login_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_facebook_login = true;
            public JsonNode facebook_login_disabled_versions = JsonLib.parseJsonSafe("[]");
            public boolean enable_marketing_authorization = true;
            public JsonNode marketing_authorization_disabled_versions = JsonLib.parseJsonSafe("[]");
        }

        public static class SMTP {
            public String smtpHost;
            public Integer smtpPort;
            public String smtpUsername;
            public String smtpPassword;
        }
    }
}