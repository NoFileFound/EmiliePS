package org.genshinhttpsrv.controllers.combo;

// Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.LinkedHashMap;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.api.Response;
import org.genshinhttpsrv.api.enums.ClientType;
import org.genshinhttpsrv.api.enums.RegionType;
import org.genshinhttpsrv.api.Retcode;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"combo/box/api/config", "takumi/combo/box/api/config"}, produces = "application/json")
public final class Box implements Response {
    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/drmSwitch">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/drmSwitch</a><br><br>
     *  Description: Fetches client configuration about the httpdns.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     *  @deprecated The httpdns config is now located in <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo</a>.
     */
    @GetMapping(value = "sdk/drmSwitch")
    @Deprecated(forRemoval = true)
    public ResponseEntity<LinkedHashMap<String, Object>> SendDrmSwitch(RegionType biz_key, ClientType client_type) {
        if(biz_key == null || client_type == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey", null));
        }

        if(biz_key == RegionType.REGION_UNKNOWN || client_type == ClientType.PLATFORM_UNKNOWN || client_type == ClientType.PLATFORM_WEB || client_type == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_PLATFORM_NO_CONFIG, "RetCode_NoConfig", null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_MODULE, "RetCode_InvalidModule", null));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-cn/porte">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-cn/porte</a><br><br>
     *  Description: Fetches client configuration about the blacklisted device list in the chinese version.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code client_type} — The client's platform type. (As a string)</li>
     *        </ul>
     */
    @GetMapping(value = "porte-cn/porte")
    public ResponseEntity<LinkedHashMap<String, Object>> SendPorte(String app_id, ClientType client_type) {
        if(app_id == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey", null));
        }

        if(client_type != ClientType.PLATFORM_ANDROID) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_PLATFORM_NO_CONFIG, "RetCode_NoConfig", null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<String, Object>() {{
            put("vals", new LinkedHashMap<String, Object>() {{
                put("cryptoConfig", Application.getPropertiesInfo().porte_cn_config);
            }});
        }}));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-os/kibana_box">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/porte-os/kibana_box</a><br><br>
     *  Description: Fetches client configuration about the kibana box.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code appId} — The application id.</li>
     *          <li>{@code platform} — The client's platform type. (As a string)</li>
     *        </ul>
     */
    @GetMapping(value = "porte-os/kibana_box")
    public ResponseEntity<LinkedHashMap<String, Object>> SendKibanaBox(String appId, String platform) throws JsonProcessingException {
        if(!Application.getPropertiesInfo().app_ids.isEmpty() && !Application.getPropertiesInfo().app_ids.contains(appId)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey", null));
        }

        if(!platform.equals("android") && !platform.equals("ios")) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_MODULE, "RetCode_InvalidModule", null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<String, Object>() {{
            put("vals", new LinkedHashMap<>() {{
                put("uploadEventPageSize", String.valueOf(Application.getPropertiesInfo().kibanabox_mobile.upload_event_page_size));
                put("minimumUploadInterval", String.valueOf(Application.getPropertiesInfo().kibanabox_mobile.minimum_upload_interval));
                put("disabledPaths", JsonLoader.toJson(Application.getPropertiesInfo().kibanabox_mobile.disabled_paths));
                put("enabled", String.valueOf(Application.getPropertiesInfo().kibanabox_mobile.enabled));
                put("minUploadEventTriggerSize", String.valueOf(Application.getPropertiesInfo().kibanabox_mobile.minimum_upload_event_trigger_size));
            }});
        }}));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sdk/combo</a><br><br>
     *  Description: Fetches client configuration about telemetry logging, real person verification, OAID and more.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "sdk/combo")
    public ResponseEntity<LinkedHashMap<String, Object>> SendCombo(RegionType biz_key, ClientType client_type) {
        if(biz_key == null || client_type == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey", null));
        }

        if(biz_key == RegionType.REGION_UNKNOWN || client_type == ClientType.PLATFORM_UNKNOWN || client_type == ClientType.PLATFORM_WEB || client_type == ClientType.PLATFORM_WAP) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_PLATFORM_NO_CONFIG, "RetCode_NoConfig", null));
        }

        LinkedHashMap<String, String> vals = new LinkedHashMap<>();
        if(client_type != ClientType.PLATFORM_PC) {
            vals.put("enable_apm_sdk", String.valueOf(Application.getPropertiesInfo().combo_config.enable_apm_sdk));
        }

        switch(client_type) {
            case PLATFORM_ANDROID, PLATFORM_ANDROIDCLOUD -> {
                vals.put("enable_attribution", String.valueOf(Application.getPropertiesInfo().combo_config.enable_attribution));
                vals.put("disable_try_verify", String.valueOf(Application.getPropertiesInfo().combo_config.disable_try_verify));
                vals.put("report_black_list", Application.getPropertiesInfo().combo_config.report_black_list.toString());
                if (biz_key == RegionType.REGION_OVERSEAS) {
                    vals.put("appsflyer_config", String.format("{\n  \"enabled\": %b\n}", Application.getPropertiesInfo().combo_config.enable_appsflyer_config));
                    vals.put("enable_twitter_v2", String.valueOf(Application.getPropertiesInfo().combo_config.enable_twitter_v2));
                    vals.put("enable_bind_google_sdk_order", String.valueOf(Application.getPropertiesInfo().combo_config.enable_bind_google_sdk_order));
                    vals.put("enable_google_credential_login", String.valueOf(Application.getPropertiesInfo().combo_config.enable_google_credential_login));
                    vals.put("enable_google_cancel_callback", String.valueOf(Application.getPropertiesInfo().combo_config.enable_google_cancel_callback));
                    vals.put("isGooglePayV2", "{\"whiteList\": [ { \"thousandRate\": 1}]}");
                } else {
                    vals.put("enable_oaid", String.valueOf(Application.getPropertiesInfo().combo_config.enable_oaid));
                    vals.put("oaid_call_hms", String.valueOf(Application.getPropertiesInfo().combo_config.oaid_call_hms));
                    vals.put("oaid_expire_time", Application.getPropertiesInfo().combo_config.oaid_expire_time.toString());
                    vals.put("oaid_multi_process", String.valueOf(Application.getPropertiesInfo().combo_config.oaid_multi_process));
                    vals.put("alipay_recommend", String.valueOf(Application.getPropertiesInfo().combo_config.alipay_recommend));
                    vals.put("watermark_enable_web_notice", String.valueOf(Application.getPropertiesInfo().combo_config.watermark_enable_web_notice));
                    vals.put("pay_platform_block_h5_cashier", String.valueOf(Application.getPropertiesInfo().combo_config.pay_platform_block_h5_cashier));
                    vals.put("pay_platform_h5_loading_limit", String.valueOf(Application.getPropertiesInfo().combo_config.pay_platform_h5_loading_limit));
                    vals.put("bili_pay_cancel_strings", Application.getPropertiesInfo().combo_config.bili_pay_cancel_strings.toString());
                }
            }
            case PLATFORM_IOS, PLATFORM_IOSCLOUD -> {
                vals.put("enable_attribution", String.valueOf(Application.getPropertiesInfo().combo_config.enable_attribution));
                if(biz_key == RegionType.REGION_OVERSEAS) {
                    vals.put("appsflyer_config", String.format("{\n  \"enabled\": %b\n}", Application.getPropertiesInfo().combo_config.enable_appsflyer_config));
                    vals.put("enable_os_new_rsa_lib", String.valueOf(Application.getPropertiesInfo().combo_config.enable_os_new_rsa_lib));
                    vals.put("enable_twitter_v2", String.valueOf(Application.getPropertiesInfo().combo_config.enable_twitter_v2));
                } else {
                    vals.put("enable_oaid", String.valueOf(Application.getPropertiesInfo().combo_config.enable_oaid));
                    vals.put("oaid_call_hms", String.valueOf(Application.getPropertiesInfo().combo_config.oaid_call_hms));
                    vals.put("oaid_expire_time", Application.getPropertiesInfo().combo_config.oaid_expire_time.toString());
                    vals.put("oaid_multi_process", String.valueOf(Application.getPropertiesInfo().combo_config.oaid_multi_process));
                    vals.put("enable_available_rom_v2", String.valueOf(Application.getPropertiesInfo().combo_config.enable_available_rom_v2));
                }
            }
            case PLATFORM_PS4, PLATFORM_PS5 -> {
                vals.put("enable_spint_prodqa_realname", String.valueOf(Application.getPropertiesInfo().combo_config.enable_spint_prodqa_realname));
                vals.put("console_login_method", String.valueOf(Application.getPropertiesInfo().combo_config.console_login_method));
                vals.put("kibana_pc_config", Application.getPropertiesInfo().combo_config.kibana_pc_config.toString());
            }
            case PLATFORM_PC, PLATFORM_PCCLOUD -> {
                vals.put("enable_web_dpi", String.valueOf(Application.getPropertiesInfo().combo_config.enable_web_dpi));
                vals.put("kcp_enable", String.valueOf(Application.getPropertiesInfo().combo_config.enable_kcp));
                vals.put("webview_apm_config", String.format("{ \"crash_capture_enable\": %s }", Application.getPropertiesInfo().combo_config.crash_capture_enable));
                vals.put("webview_rendermethod_config", String.format("{ \"useLegacy\": %s }", Application.getPropertiesInfo().combo_config.webview_rendermethod_config));
                vals.put("kibana_pc_config", Application.getPropertiesInfo().combo_config.kibana_pc_config.toString());
                if(biz_key == RegionType.REGION_OVERSEAS) {
                    vals.put("account_list_page_enable", String.valueOf(Application.getPropertiesInfo().combo_config.account_list_page_enable));
                    vals.put("new_forgotpwd_page_enable", String.valueOf(Application.getPropertiesInfo().combo_config.new_forgotpwd_page_enable));
                    vals.put("pay_payco_centered_host", Application.getPropertiesInfo().combo_config.pay_payco_centered_host);
                } else {
                    vals.put("login_record_config", String.format("{\"is_checked\":%s}", Application.getPropertiesInfo().combo_config.login_record_config));
                    vals.put("payment_cn_config", String.format("{ \"h5_cashier_enable\": %s, \"h5_cashier_timeout\": %s}", Application.getPropertiesInfo().combo_config.h5_cashier_enable ? 1 : 0, Application.getPropertiesInfo().combo_config.h5_cashier_timeout));
                }
            }
        }

        vals.put("enable_telemetry_h5log", String.valueOf(Application.getPropertiesInfo().combo_config.enable_telemetry_h5log));
        vals.put("enable_telemetry_data_upload", String.valueOf(Application.getPropertiesInfo().combo_config.enable_telemetry_data_upload));
        vals.put("h5log_config", String.format(" { \"enable\": %d, \"level\": \"%s\" } ", Application.getPropertiesInfo().combo_config.enable_telemetry_h5log ? 1 : 0, Application.getPropertiesInfo().is_debug ? "Debug" : "Info"));
        vals.put("httpdns_enable", String.valueOf(Application.getPropertiesInfo().httpdns.enabled));
        vals.put("httpdns_cache_expire_time", String.valueOf(Application.getPropertiesInfo().httpdns.cache_expire_time));
        vals.put("http_keep_alive_time", String.valueOf(Application.getPropertiesInfo().httpdns.keep_alive_time));
        vals.put("telemetry_config", String.format("{\n \"dataupload_enable\": %s\n}", (Application.getPropertiesInfo().combo_config.enable_telemetry_h5log ? 1 : 0)));
        vals.put("network_report_config", Application.getPropertiesInfo().combo_config.network_report_config.toString());
        vals.put("h5log_filter_config", Application.getPropertiesInfo().combo_config.h5log_filter_config.toString());
        if(biz_key == RegionType.REGION_OVERSEAS) {
            vals.put("enable_user_center_v2", String.valueOf(Application.getPropertiesInfo().combo_config.enable_user_center_v2));
            vals.put("disable_email_bind_skip", String.valueOf(Application.getPropertiesInfo().combo_config.disable_email_bind_skip));
            vals.put("email_bind_remind", String.valueOf(Application.getPropertiesInfo().combo_config.email_bind_remind));
            vals.put("email_bind_remind_interval", String.valueOf(Application.getPropertiesInfo().combo_config.email_bind_remind_interval));
            vals.put("enable_register_autologin", String.valueOf(Application.getPropertiesInfo().combo_config.enable_register_autologin));
            vals.put("new_register_page_enable", String.valueOf(Application.getPropertiesInfo().combo_config.enable_new_register_page));
            vals.put("list_price_tierv2_enable", String.valueOf(Application.getPropertiesInfo().combo_config.enable_list_price_tierv2));
        } else {
            vals.put("modify_real_name_other_verify", String.valueOf(Application.getPropertiesInfo().combo_config.modify_real_name_other_verify));
            vals.put("email_register_hide", String.valueOf(Application.getPropertiesInfo().combo_config.email_register_hide));
            vals.put("login_flow_notification", String.format("{\"enable\": %d, }", Application.getPropertiesInfo().combo_config.login_flow_notification ? 1 : 0));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<String, Object>() {{
            put("vals", vals);
        }}));
    }

    /**
     *  Source: <a href="https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sw/precache">https://devapi-static.mihoyo.com/takumi/combo/box/api/config/sw/precache</a><br><br>
     *  Description: Fetches client configuration about the service worker.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code client} — The client's platform type.</li>
     *        </ul>
     */
    @GetMapping(value = "sw/precache")
    public ResponseEntity<LinkedHashMap<String, Object>> SendPrecache(RegionType biz, ClientType client) {
        if(biz == null || client == null) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_INVALID_KEY, "RetCode_InvalidKey", null));
        }

        if(biz == RegionType.REGION_UNKNOWN || (client != ClientType.PLATFORM_IOS && client != ClientType.PLATFORM_ANDROID && client != ClientType.PLATFORM_PC)) {
            return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_COMBO_PLATFORM_NO_CONFIG, "RetCode_NoConfig", null));
        }

        return ResponseEntity.ok(this.makeResponse(Retcode.RETCODE_SUCC, "OK", new LinkedHashMap<String, Object>() {{
            put("data", new LinkedHashMap<>() {{
                put("enable", String.valueOf(Application.getPropertiesInfo().serviceworker.enabled));
                put("url", Application.getPropertiesInfo().serviceworker.url);
            }});
        }}));
    }
}