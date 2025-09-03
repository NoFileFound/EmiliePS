package org.emilieps.webroutes.mdk;

// Imports
import static org.emilieps.data.HttpRetcode.RETCODE_SUCC;
import static org.emilieps.data.HttpRetcode.RET_PARAMETER_ERROR;
import java.util.LinkedHashMap;
import org.emilieps.Application;
import org.emilieps.data.webserver.Response;
import org.emilieps.data.enums.RegionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Libraries
import org.emilieps.library.MongodbLib;

@RestController
@RequestMapping(value = {"hk4e_global/mdk/agreement/api", "mdk/agreement/api"}, produces = "application/json")
public final class Agreement implements Response {
    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/agreement/api/getAgreementInfos">https://devapi-takumi.mihoyo.com/mdk/agreement/api/getAgreementInfos</a><br><br>
     *  Description: Fetches the marketing agreements (Available only in overseas version).<br><br>
     *  Method: GET<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code biz_key} — The game's specified region version. (hk4e_cn/hk4e_global)</li>
     *          <li>{@code country_code} — The client's country code.</li>
     *          <li>{@code token} — The client's session token.</li>
     *          <li>{@code uid} — The client's account id..</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @GetMapping("getAgreementInfos")
    public ResponseEntity<LinkedHashMap<String, Object>> SendAgreementInfos(RegionType biz_key, String country_code, String token, String uid, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        if(biz_key == null || biz_key == RegionType.REGION_UNKNOWN) {
            return ResponseEntity.ok(this.makeResponse(RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        var agreements = Application.getHttpConfig().marketing_agreements;
        if(!uid.isEmpty()) {
            var account = MongodbLib.findAccountById(Long.valueOf(uid));
            if(account == null || !account.getGameToken().equals(token) || !account.getCountryCode().equals(country_code)) {
                return ResponseEntity.ok(this.makeResponse(RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
            }

            agreements.removeIf(agreement -> account.getAcceptedAgreements().contains(agreement.agreement_id + "_" + agreement.agreement_version));
            agreements.removeIf(agreement -> account.getDeniedAgreements().contains(agreement.agreement_id + "_" + agreement.agreement_version));
        }

        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>() {{
            put("marketing_agreements", agreements);
        }}));
    }

    /**
     *  Source: <a href="https://devapi-takumi.mihoyo.com/mdk/agreement/api/operateAgreement">https://devapi-takumi.mihoyo.com/mdk/agreement/api/operateAgreement</a><br><br>
     *  Description: Operates an agreement (Available only in overseas version).<br><br>
     *  Method: POST<br>
     *  Content-Type: application/json<br><br>
     *  Parameters:<br>
     *        <ul>
     *          <li>{@code agreement_id} — The agreement's id.</li>
     *          <li>{@code agreement_version} — The agreement's version.</li>
     *          <li>{@code operation} — The operation's name.</li>
     *          <li>{@code uid} — The client's account id..</li>
     *          <li>{@code token} — The client's session token.</li>
     *        </ul>
     *  Headers:
     *        <ul>
     *          <li>{@code x-rpc-language} — The client's system language iso2 code.</li>
     *        </ul>
     */
    @PostMapping("operateAgreement")
    public ResponseEntity<LinkedHashMap<String, Object>> SendOperateAgreement(@RequestBody OperateAgreementModel body, @RequestHeader(value = "x-rpc-language", required = false) String lang) {
        var agreement = Application.getHttpConfig().marketing_agreements.stream().filter(a -> a.agreement_id == body.agreement_id && a.agreement_version == body.agreement_version).findFirst().orElse(null);
        if(agreement == null) {
            return ResponseEntity.ok(this.makeResponse(RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(!body.operation.equals("DENY") && !body.operation.equals("ACCEPT")) {
            return ResponseEntity.ok(this.makeResponse(RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
        }

        if(!body.uid.isEmpty() && !body.token.isEmpty()) {
            var account = MongodbLib.findAccountById(Long.valueOf((body.uid)));
            if(account == null || !account.getGameToken().equals(body.token)) {
                return ResponseEntity.ok(this.makeResponse(RET_PARAMETER_ERROR, Application.getTranslations().get(lang, "retcode_parameter_error"), null));
            }

            if(body.operation.equals("ACCEPT")) {
                account.getAcceptedAgreements().add(agreement.agreement_id + "_" + agreement.agreement_version);
				if(Application.getApplicationConfig().is_debug) {
					Application.getLogger().info(Application.getTranslations().get("console", "user_agreed_marketing_agreements", account.get_id(), agreement.agreement_id, agreement.agreement_version));
				}
            } else {
                account.getDeniedAgreements().add(agreement.agreement_id + "_" + agreement.agreement_version);
				if(Application.getApplicationConfig().is_debug) {
					Application.getLogger().info(Application.getTranslations().get("console", "user_denied_marketing_agreements", account.get_id(), agreement.agreement_id, agreement.agreement_version));
				}
            }

            account.save();
        }

        return ResponseEntity.ok(this.makeResponse(RETCODE_SUCC, "OK", new LinkedHashMap<>()));
    }


    // Classes
    public static class OperateAgreementModel {
        public int agreement_id;
        public int agreement_version;
        public String operation;
        public String uid;
        public String token;
    }
}