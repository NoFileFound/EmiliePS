package org.genshinimpact.webserver.utils;

// Imports
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import org.genshinimpact.webserver.SpringBootApp;

public final class SMSUtils {
    /**
     * Sends a sms message to the specified number.
     * @param phoneNumber The specified number.
     * @param verCode The verification code to send.
     */
    public static void sendSMS(String phoneNumber, String verCode) {
        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", SpringBootApp.getWebConfig().SMS.accessId, SpringBootApp.getWebConfig().SMS.accessSecret);
            IAcsClient client = new DefaultAcsClient(profile);

            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phoneNumber);
            request.setSignName(SpringBootApp.getWebConfig().SMS.signature);
            request.setTemplateCode("Yuanshen");
            request.setTemplateParam("{\"code\":\"" + verCode + "\"}");

            SendSmsResponse response = client.getAcsResponse(request);
            if(!"OK".equals(response.getCode())) {
                throw new RuntimeException("Failed to send SMS: " + response.getMessage());
            }
        } catch(Exception e) {
            throw new RuntimeException("Error sending SMS", e);
        }
    }
}