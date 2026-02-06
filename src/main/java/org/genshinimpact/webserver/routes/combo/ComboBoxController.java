package org.genshinimpact.webserver.routes.combo;

// Imports
import static org.genshinimpact.webserver.enums.ClientType.*;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_SUCC;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_COMBO_INVALID_KEY;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_COMBO_NO_CONFIG;
import static org.genshinimpact.webserver.enums.Retcode.RETCODE_COMBO_INVALID_MODULE;
import java.util.LinkedHashMap;
import java.util.Map;
import org.genshinimpact.utils.JsonUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.responses.PreCacheResponse;
import org.genshinimpact.webserver.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"combo/box/api/config", "takumi/combo/box/api/config"}, produces = "application/json")
public final class ComboBoxController {
    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-os/kibana_box">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-os/kibana_box</a><br><br>
     *  Description: Fetches client configuration about the download resources on android platform.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code appId} — The application id.</li>
     *          <li>{@code client_type} — The client's platform type (As a string).</li>
     *        </ul>
     */
    @GetMapping(value = "porte-os/kibana_box")
    public ResponseEntity<Response<?>> SendPorteOsKibanaBox(String appId, String platform) {
        if(platform == null || platform.isBlank()) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey"));
        }

        if(!platform.equals("android")) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_NO_CONFIG, "RetCode_NoConfig"));
        }

        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", Map.of("vals", SpringBootApp.getWebConfig().boxConfig.porte_os_kibana_box)));
    }

    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-cn/porte">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-cn/porte</a><br><br>
     *  Description: Fetches client configuration about the blocked devices on android platform (Chinese only).<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code client_type} — The client's platform type (As a string).</li>
     *        </ul>
     */
    @GetMapping(value = "porte-cn/porte")
    public ResponseEntity<Response<?>> SendPorte(String app_id, String client_type) {
        if(client_type == null || client_type.isBlank()) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey"));
        }

        if(!client_type.equals("android")) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_NO_CONFIG, "RetCode_NoConfig"));
        }

        return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", Map.of("vals", Map.of("cryptoConfig", SpringBootApp.getWebConfig().boxConfig.porte_cn_config))));
    }

    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/drmSwitch">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/drmSwitch</a><br><br>
     *  Description: Fetches client configuration about the httpdns.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "sdk/drmSwitch")
    @Deprecated(forRemoval = true)
    public ResponseEntity<Response<?>> SendDrmSwitch(String biz_key, String client_type) {
        return ResponseEntity.ok(new Response<>(RETCODE_COMBO_INVALID_MODULE, "RetCode_InvalidModule"));
    }

    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo</a><br><br>
     *  Description: Fetches client configuration about the application.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "sdk/combo")
    public ResponseEntity<Response<?>> SendCombo(String biz_key, String client_type) {
        try {
            ClientType clientType = ClientType.fromValue(client_type);
            AppName appName = AppName.fromValue(biz_key);
            if(client_type == null || biz_key == null || clientType == ClientType.PLATFORM_UNKNOWN || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey"));
            }

            LinkedHashMap<String, String> vals = new LinkedHashMap<>();
            if(clientType != ClientType.PLATFORM_PC) {
                vals.put("enable_apm_sdk", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_apm_sdk));
            }

            switch(clientType) {
                case PLATFORM_ANDROID, PLATFORM_ANDROIDCLOUD -> {
                    vals.put("enable_consent_by_country", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_consent_by_country));
                    vals.put("set_consent_false_country", SpringBootApp.getWebConfig().boxConfig.set_consent_false_country.toString());
                    vals.put("set_consent_by_age_country", "{\"US\":[\"1\"]}");
                    vals.put("report_black_list", "{\"key\":[\"download_update_progress\"]}");
                    vals.put("enable_attribution", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_attribution));
                    vals.put("disable_try_verify", String.valueOf(SpringBootApp.getWebConfig().boxConfig.disable_try_verify));
                    vals.put("appsflyer_config", String.format("{\"enabled\":%b}", SpringBootApp.getWebConfig().boxConfig.enable_appsflyer_config));
                    vals.put("enable_twitter_v2", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_twitter_v2));
                    vals.put("enable_bind_google_sdk_order", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_bind_google_sdk_order));
                    vals.put("enable_google_credential_login", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_google_credential_login));
                    vals.put("enable_google_cancel_callback", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_google_cancel_callback));
                    vals.put("isGooglePayV2", "{\"whiteList\": [{\"thousandRate\": 1}]}");
                    vals.put("enable_oaid", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid));
                    vals.put("oaid_call_hms", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid_call_hms));
                    vals.put("oaid_expire_time", "{\"huawei\": 6000, \"honor\":6000}");
                    vals.put("oaid_multi_process", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid_multi_process));
                    vals.put("alipay_recommend", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_alipay_recommend));
                    vals.put("watermark_enable_web_notice", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_watermark_web_notice));
                    vals.put("pay_platform_block_h5_cashier", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_pay_platform_block_h5_cashier));
                    vals.put("pay_platform_h5_loading_limit", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_pay_platform_h5_loading_limit));
                    vals.put("bili_pay_cancel_strings", "[\"用户取消交易\"]");
                }
                case PLATFORM_IOS, PLATFORM_IOSCLOUD -> {
                    vals.put("enable_consent_by_country", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_consent_by_country));
                    vals.put("set_consent_false_country", SpringBootApp.getWebConfig().boxConfig.set_consent_false_country.toString());
                    vals.put("set_consent_by_age_country", "{\"US\":[\"1\"]}");
                    vals.put("enable_attribution", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_attribution));
                    vals.put("appsflyer_config", String.format("{\"enabled\": %b}", SpringBootApp.getWebConfig().boxConfig.enable_appsflyer_config));
                    vals.put("enable_os_new_rsa_lib", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_os_new_rsa_lib));
                    vals.put("enable_twitter_v2", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_twitter_v2));
                    vals.put("enable_oaid", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid));
                    vals.put("oaid_call_hms", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid_call_hms));
                    vals.put("oaid_expire_time", "{\"huawei\": 6000, \"honor\":6000}");
                    vals.put("oaid_multi_process", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_oaid_multi_process));
                    vals.put("enable_available_rom_v2", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_available_rom_v2));
                }
                case PLATFORM_PS4, PLATFORM_PS5 -> {
                    vals.put("domain_region_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_domain_region));
                    vals.put("enable_spint_prodqa_realname", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_spint_prodqa_realname));
                    vals.put("console_login_method", String.valueOf(SpringBootApp.getWebConfig().boxConfig.console_login_method));
                    vals.put("kibana_pc_config", SpringBootApp.getWebConfig().boxConfig.kibana_pc_config.toString());
                    vals.put("ps4_bind_mobile_config", SpringBootApp.getWebConfig().boxConfig.ps4_bind_mobile_config.toString());
                }
                case PLATFORM_PC, PLATFORM_PCCLOUD -> {
                    vals.put("domain_region_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_domain_region));
                    vals.put("kibana_pc_config", SpringBootApp.getWebConfig().boxConfig.kibana_pc_config.toString());
                    vals.put("enable_web_dpi", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_web_dpi));
                    vals.put("kcp_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_kcp_connection));
                    vals.put("webview_apm_config", String.format("{\"crash_capture_enable\":%s}", SpringBootApp.getWebConfig().boxConfig.enable_web_apmconfig_crash_capture));
                    vals.put("webview_rendermethod_config", String.format("{\"useLegacy\":%s}", SpringBootApp.getWebConfig().boxConfig.enable_webview_rendermethod_config_legacy));
                    vals.put("account_list_page_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_account_list_page));
                    vals.put("new_forgotpwd_page_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_new_forgotpwd_page));
                    vals.put("pay_payco_centered_host", "bill.payco.com");
                    vals.put("login_record_config", String.format("{\"is_checked\":%s}", SpringBootApp.getWebConfig().boxConfig.enable_login_record_check));
                    vals.put("payment_cn_config", String.format("{\"h5_cashier_enable\":%s,\"h5_cashier_timeout\":%s}", SpringBootApp.getWebConfig().boxConfig.enable_h5_cashier ? 1 : 0, SpringBootApp.getWebConfig().boxConfig.enable_h5_cashier_timeout));
                }
            }

            vals.put("enable_telemetry_h5log", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_telemetry_h5log));
            vals.put("enable_telemetry_data_upload", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_telemetry_data_upload));
            vals.put("h5log_config", String.format("{\"enable\": %d,\"level\":\"%s\"}", SpringBootApp.getWebConfig().boxConfig.enable_telemetry_h5log ? 1 : 0, "Debug"));
            vals.put("httpdns_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_httpdns));
            vals.put("httpdns_cache_expire_time", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_httpdns_cache_expire_time));
            vals.put("http_keep_alive_time", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_httpdns_keep_alive_time));
            vals.put("telemetry_config", String.format("{\"dataupload_enable\":%s}", (SpringBootApp.getWebConfig().boxConfig.enable_telemetry_h5log ? 1 : 0)));
            vals.put("network_report_config", JsonUtils.toJsonString(SpringBootApp.getWebConfig().boxConfig.network_report_config));
            vals.put("h5log_filter_config", JsonUtils.toJsonString(SpringBootApp.getWebConfig().boxConfig.h5log_filter_config));
            vals.put("enable_user_center_v2", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_user_center_v2));
            vals.put("disable_email_bind_skip", String.valueOf(!SpringBootApp.getWebConfig().boxConfig.enable_email_bind_skip));
            vals.put("email_bind_remind", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_email_bind_remind));
            vals.put("email_bind_remind_interval", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_email_bind_remind_interval));
            vals.put("enable_register_autologin", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_register_autologin));
            vals.put("new_register_page_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_new_register_page));
            vals.put("list_price_tierv2_enable", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_list_price_tierv2));
            vals.put("modify_real_name_other_verify", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_modify_real_name_other_verify));
            vals.put("email_register_hide", String.valueOf(SpringBootApp.getWebConfig().boxConfig.enable_email_register_hide));
            vals.put("login_flow_notification", String.format("{\"enable\":%d}", SpringBootApp.getWebConfig().boxConfig.enable_login_flow_notification ? 1 : 0));
            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", Map.of("vals", vals)));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_NO_CONFIG, "RetCode_NoConfig"));
        }
    }

    /**
     * Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sw/precache">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sw/precache</a><br><br>
     *  Description: Fetches client configuration about the service worker.<br><br>
     *  Methods: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz} — Game business identifier ({@code hk4e_global}, {@code hk4e_cn}).</li>
     *          <li>{@code client} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "sw/precache")
    public ResponseEntity<Response<?>> SendPreCache(String biz, String client) {
        try {
            ClientType clientType = ClientType.fromValue(client);
            AppName appName = AppName.fromValue(biz);
            if(biz == null || client == null || clientType == ClientType.PLATFORM_UNKNOWN || appName == AppName.APP_UNKNOWN) {
                return ResponseEntity.ok(new Response<>(RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey"));
            }

            if((clientType != ClientType.PLATFORM_IOS && clientType != PLATFORM_ANDROID && clientType != ClientType.PLATFORM_PC)) {
                return ResponseEntity.ok(new Response<>(RETCODE_COMBO_NO_CONFIG, "RetCode_NoConfig"));
            }

            return ResponseEntity.ok(new Response<>(RETCODE_SUCC, "OK", new PreCacheResponse(SpringBootApp.getWebConfig().boxConfig.precache_worker_url, String.valueOf(SpringBootApp.getWebConfig().boxConfig.precache_worker_url_enable))));
        } catch(Exception ignored) {
            return ResponseEntity.ok(new Response<>(RETCODE_COMBO_NO_CONFIG, "RetCode_NoConfig"));
        }
    }
}