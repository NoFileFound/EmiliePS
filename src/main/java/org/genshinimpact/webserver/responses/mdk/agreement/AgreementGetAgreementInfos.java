package org.genshinimpact.webserver.responses.mdk.agreement;

// Imports
import java.util.List;
import org.genshinimpact.configs.WebConfig;

public class AgreementGetAgreementInfos {
    public List<WebConfig.AgreementConfig> marketing_agreements;

    public AgreementGetAgreementInfos(List<WebConfig.AgreementConfig> marketing_agreements) {
        this.marketing_agreements = marketing_agreements;
    }
}