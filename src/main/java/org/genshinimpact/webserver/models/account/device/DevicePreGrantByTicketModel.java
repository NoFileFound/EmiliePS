package org.genshinimpact.webserver.models.account.device;

// Imports
import org.genshinimpact.webserver.enums.GrantType;

@SuppressWarnings("unused")
public class DevicePreGrantByTicketModel {
    public String action_ticket;
    public DeviceInfoModel device;
    public GrantType way;
}