package org.genshinimpact.gameserver.enums;

public enum MpSettingType {
    MP_SETTING_NO_ENTER,
    MP_SETTING_ENTER_FREELY,
    MP_SETTING_ENTER_AFTER_APPLY;

    public int getValue() {
        return this.ordinal();
    }
}