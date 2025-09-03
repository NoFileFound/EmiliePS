package org.emilieps.webroutes.combo;

// Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.emilieps.Application;
import org.emilieps.data.HttpRetcode;
import org.emilieps.data.webserver.Response;
import org.emilieps.data.enums.webserver.ApplicationId;
import org.emilieps.data.enums.AccountType;
import org.emilieps.data.enums.ChannelType;
import org.emilieps.data.enums.ClientType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.EncryptionLib;
import org.emilieps.library.JsonLib;
import org.emilieps.library.MongodbLib;

@RestController
@RequestMapping(value = {"hk4e_global/combo/granter", "hk4e_cn/combo/granter", "combo/granter", "takumi/hk4e_global/combo/granter", "takumi/hk4e_cn/combo/granter", "takumi/combo/granter"}, produces = "application/json")
public final class Granter implements Response {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getConfig">https://devapi-takumi.mihoyo.com/combo/granter/api/getConfig</a><br><br>
     *  Description: Fetches configuration about the game.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code client_type} — The client's platform type.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping(value = "api/getConfig")
    public ResponseEntity<LinkedHashMap<String, Object>> SendConfig(ApplicationId app_id, ChannelType channel_id, ClientType client_type, @RequestHeader(value = "x-rpc-language", required = false) String lang, HttpServletRequest request) {
        boolean isOverseas = request.getRequestURL().toString().contains("hk4e_global");
        if((app_id != ApplicationId.APP_GENSHIN && app_id != ApplicationId.APP_3NNN && app_id != ApplicationId.APP_CLOUDPLATFORM) || client_type == null || client_type == ClientType.PLATFORM_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_SYSTEM_ERROR, Application.getTranslations().get(lang, "retcode_system_error"), null));
        }

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("protocol", isOverseas && channel_id == ChannelType.CHANNEL_DEFAULT);
        data.put("qr_enabled", Application.getHttpConfig().enable_qrcode_login);
        data.put("log_level", (Application.getApplicationConfig().is_debug ? "DEBUG" : "INFO"));
        data.put("announce_url", Application.getHttpConfig().announcement_url);
        data.put("push_alias_type", Application.getHttpConfig().push_alias_type);
        data.put("disable_ysdk_guard", Application.getHttpConfig().disable_ysdk_guard);
        data.put("enable_announce_pic_popup", Application.getHttpConfig().enable_announce_pic_popup);
        data.put("app_name", isOverseas ? "原神海外" : "原神");
        if(client_type == ClientType.PLATFORM_PC || client_type == ClientType.PLATFORM_PCCLOUD) {
            data.put("qr_enabled_apps", new LinkedHashMap<>() {{
                put("bbs", !isOverseas);
                put("cloud", !isOverseas);
            }});
            data.put("qr_app_icons", new LinkedHashMap<>() {{
                put("app", "");
                put("bbs", "");
                put("cloud", "https://webstatic.mihoyo.com/upload/operation_location/2022/12/07/ec0f2514f044ac43754440241ab0b838_3962973103776517937.png");
            }});
        } else {
            data.put("qr_enabled_apps", null);
            data.put("qr_app_icons", null);
        }
        data.put("qr_cloud_display_name", !isOverseas ? "云·原神" : "");
        data.put("qr_app_display_name", "");
        data.put("qr_bbs_display_name", "");
        data.put("enable_user_center", client_type != ClientType.PLATFORM_WEB);
        data.put("functional_switch_configs", Application.getHttpConfig().functional_switch_configs);
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", data));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getDynamicClientConfig">https://devapi-takumi.mihoyo.com/combo/granter/api/getDynamicClientConfig</a><br><br>
     *  Description: Fetches dynamic configuration about the game.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     */
    @GetMapping(value = "api/getDynamicClientConfig")
    public ResponseEntity<LinkedHashMap<String, Object>> SendDynamicClientConfig(@RequestParam Map<String, String> allParams) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        for (String key : allParams.keySet()) {
            data.put(key, false);
        }

        if(data.isEmpty()) {
            data.put("enable_consent_banner", false);
        }
        data.put("region_code", "");

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", data));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getFont">https://devapi-takumi.mihoyo.com/combo/granter/api/getFont</a><br><br>
     *  Description: Fetches the game fonts for special languages like chinese and japanese.<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping(value = "api/getFont")
    public ResponseEntity<LinkedHashMap<String, Object>> SendFont(ApplicationId app_id, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(app_id != ApplicationId.APP_GENSHIN && app_id != ApplicationId.APP_3NNN && app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_INVALID_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_app_invalid_error"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", Map.of(
                "fonts", List.of(
                        Map.of(
                                "font_id", 0,
                                "app_id", 0,
                                "name", "zh-cn.ttf",
                                "url", "https://sdk.hoyoverse.com/sdk-public/2024/12/31/ee21fab3128b390122431dfa967709a5_1886429408221468499.ttf",
                                "md5", "ee21fab3128b390122431dfa967709a5"
                        ),
                        Map.of(
                                "font_id", 0,
                                "app_id", 0,
                                "name", "ja.ttf",
                                "url", "https://sdk.hoyoverse.com/sdk-public/2024/12/31/1eb7f8fd3007c8d88272908f7f239ef8_3447476462374302156.ttf",
                                "md5", "1eb7f8fd3007c8d88272908f7f239ef8"
                        )
                )
        )));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/api/getProtocol">https://devapi-takumi.mihoyo.com/combo/granter/api/getProtocol</a><br><br>
     *  Description: Fetches information about the API protocol version.<br><br>
     *  Methods: GET, POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code language} — The client's operation system 2iso language code.</li>
     *          <li>{@code major} — A major.</li>
     *          <li>{@code minimum} — A minimum.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @RequestMapping(value = {"api/compareProtocolVersion", "api/getProtocol"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<LinkedHashMap<String, Object>> SendProtocol(@RequestBody(required = false) CompareProtocolVersionModel body, @RequestParam(required = false) ApplicationId app_id, @RequestParam(required = false) ChannelType channel_id, @RequestParam(required = false) String language, @RequestParam(required = false) Integer major, @RequestParam(required = false) Integer minimum, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        ApplicationId finalAppId = Optional.ofNullable(body).map(b -> b.app_id).orElse(app_id);
        ChannelType finalChannelId = Optional.ofNullable(body).map(b -> b.channel_id).orElse(channel_id);
        String finalLanguage = Optional.ofNullable(body).map(b -> b.language).orElse(language);
        Integer finalMajor = Optional.ofNullable(body).map(b -> b.major).orElse(major);
        Integer finalMinimum = Optional.ofNullable(body).map(b -> b.minimum).orElse(minimum);
        if((finalAppId != ApplicationId.APP_GENSHIN && finalAppId != ApplicationId.APP_3NNN && finalAppId != ApplicationId.APP_CLOUDPLATFORM) || finalChannelId == ChannelType.CHANNEL_UNKNOWN || finalLanguage == null || finalLanguage.isBlank()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PROTOCOL_FAILED, Application.getTranslations().get(lang, "retcode_protocol_failed"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("modified", true);
            put("protocol", new LinkedHashMap<>() {{
                put("id", 0);
                put("app_id", finalAppId);
                put("language", language);
                put("user_proto", "");
                put("priv_proto", "");
                put("major", finalMajor);
                put("minimum", finalMinimum);
                put("create_time", "0");
                put("teenager_proto", "");
                put("third_proto", "");
                put("full_priv_proto", "");
            }});
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/login/beforeVerify">https://devapi-takumi.mihoyo.com/combo/granter/login/beforeVerify</a><br><br>
     *  Description: Fetches the account's additional security features.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code account_id} — The account id.</li>
     *          <li>{@code combo_token} — The account session key.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "login/beforeVerify")
    public ResponseEntity<LinkedHashMap<String, Object>> SendBeforeVerify(@RequestBody BeforeVerifyModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.account_id == null || body.account_id.isEmpty() || body.combo_token == null || body.combo_token.isEmpty()) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        long accId;
        try {
            accId = Long.parseLong(body.account_id);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        var userObj = MongodbLib.findAccountById(accId);
        if (userObj == null || !body.combo_token.equals(userObj.getGameToken())) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslations().get(lang, "retcode_network_at_risk"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("is_heartbeat_required", userObj.getIsRequireHeartbeat());
            put("is_realname_required", userObj.getIsRequireRealname());
            put("is_guardian_required", userObj.getIsRequireGuardian());
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/login/v2/login">https://devapi-takumi.mihoyo.com/combo/granter/login/v2/login</a><br><br>
     *  Description: Verifies the login process.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *          <li>{@code data} — The provided information needed for verification, like user id and variable to check if its guest.</li>
     *          <li>{@code device} — The client's device id..</li>
     *          <li>{@code sign} — The signature to check if the provided data is correct.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = {"login/login", "login/v2/login"})
    public ResponseEntity<LinkedHashMap<String, Object>> SendLogin(@RequestBody LoginModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang, @RequestHeader(value = "x-rpc-game_biz", required = false) String game_biz, HttpServletRequest request) throws JsonProcessingException {
        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM || (game_biz == null || (!game_biz.equals("hk4e_cn") && !game_biz.equals("hk4e_global")))) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(body.channel_id == ChannelType.CHANNEL_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        JsonNode data_node = JsonLib.parseJsonSafe(body.data);
        if(data_node == null || !data_node.has("uid") || !data_node.has("guest")) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        String hmacSign = EncryptionLib.generateComboSignature(String.format("app_id=%s&channel_id=%s&data=%s&device=%s", body.app_id.getValue(), body.channel_id.getValue(), body.data, body.device), !request.getRequestURL().toString().contains("hk4e_cn"));
        if(hmacSign == null || !hmacSign.equals(body.sign)) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_CONFIGURATION_ERROR, Application.getTranslations().get(lang, "retcode_signature_error"), null));
        }

        Long uid = data_node.get("uid").asLong();
        String guest = data_node.get("guest").asText();
        var myAccount = MongodbLib.findAccountById(uid);
        if(myAccount == null || (guest.equals("true") && !myAccount.getIsGuest()) || (data_node.has("token") && !myAccount.getGameToken().equals(data_node.get("token").asText()))) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_NETWORK_AT_RISK, Application.getTranslations().get(lang, "retcode_network_at_risk"), null));
        }

        myAccount.setLastGameBiz(game_biz);
        myAccount.save();

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("combo_id", "0");
        data.put("open_id", String.valueOf(myAccount.get_id()));
        data.put("combo_token", myAccount.getGameToken());
        data.put("data", JsonLib.toJson(new LinkedHashMap<>() {{
            put("guest", guest);
            put("country", data_node.has("country_code") ? data_node.get("country_code").asText() : myAccount.getCountryCode());
            if(data_node.has("is_new_register")) put("is_new_register", data_node.get("is_new_register").asBoolean());
            if(data_node.has("ps_account_id")) put("ps_account_id", data_node.get("ps_account_id").asText());
            if(data_node.has("online_id")) put("online_id", data_node.get("online_id").asText());
            if(data_node.has("ext")) put("ext", data_node.get("ext").asText());
        }}));
        data.put("heartbeat", Application.getHttpConfig().enable_heartbeat);
        data.put("account_type", (guest.equals("true") ? AccountType.ACCOUNT_GUEST.getValue() : AccountType.ACCOUNT_NORMAL.getValue()));
        data.put("fatigue_remind", (myAccount.getFatigueRemind() == null) ? null : JsonLib.toJson(myAccount.getFatigueRemind()));
        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RETCODE_SUCC, "OK", data));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/combo/granter/login/webLogin">https://devapi-takumi.mihoyo.com/combo/granter/login/webLogin</a><br><br>
     *  Description: Verifies the login process.<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code app_id} — The application id.</li>
     *          <li>{@code channel_id} — The client's channel id.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping(value = "login/webLogin")
    public ResponseEntity<LinkedHashMap<String, Object>> SendWebLogin(@RequestBody WebLoginModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(body.app_id != ApplicationId.APP_GENSHIN && body.app_id != ApplicationId.APP_3NNN && body.app_id != ApplicationId.APP_CLOUDPLATFORM) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(body.channel_id == ChannelType.CHANNEL_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        return ResponseEntity.ok(this.makeResponse(HttpRetcode.RET_LOGIN_INVALID_ACCOUNT, Application.getTranslations().get(lang, "retcode_community_unavailable"), null));
    }


    // Classes
    public static class BeforeVerifyModel {
        public String account_id;
        public String combo_token;
    }

    public static class CompareProtocolVersionModel {
        public ApplicationId app_id;
        public ChannelType channel_id;
        public String language;
        public int major;
        public int minimum;
    }

    public static class LoginModel {
        public ApplicationId app_id;
        public ChannelType channel_id;
        public String data;
        public String device;
        public String sign;
    }

    public static class WebLoginModel {
        public ApplicationId app_id;
        public ChannelType channel_id;
    }
}