package org.genshinimpact.gameserver.packets.send.player;

// Imports
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.OpenStateUpdateNotifyOuterClass.OpenStateUpdateNotify;

import java.util.HashMap;

public class SendOpenStateUpdateNotify implements SendPacket {
    private final byte[] data;

    public SendOpenStateUpdateNotify() {
        var proto = OpenStateUpdateNotify.newBuilder().putAllOpenStateMap(new HashMap<>() {{
            put(92, 1); put(15, 1); put(4803, 1); put(3305, 1); put(1005, 1); put(51, 1); put(59, 1); put(800, 1);
            put(6017, 1); put(4809, 1); put(3, 1); put(61, 1); put(5215, 1); put(4003, 1); put(4801, 1); put(5308, 1);
            put(901, 1); put(4202, 1); put(3702, 1); put(44, 0); put(5015, 1); put(4608, 0); put(4808, 1); put(5713, 1);
            put(5816, 1); put(4700, 1); put(1409, 1); put(2101, 1); put(4603, 1); put(4526, 1); put(1301, 1); put(2104, 1);
            put(5402, 1); put(2000, 1); put(53, 1); put(6115, 1); put(10, 1); put(4614, 1); put(16, 1); put(5705, 0);
            put(5003, 0); put(2103, 1); put(1407, 1); put(4525, 0); put(1405, 1); put(4602, 1); put(4304, 1); put(4519, 1);
            put(5715, 1); put(3003, 1); put(802, 1); put(4616, 1); put(5006, 1); put(5103, 1); put(35, 1); put(1006, 1);
            put(2501, 0); put(5214, 1); put(2200, 1); put(2, 1); put(2400, 1); put(1402, 1); put(4109, 1); put(3303, 1);
            put(4807, 1); put(4201, 1); put(2201, 1); put(5403, 1); put(5210, 1); put(4002, 1); put(4811, 1); put(4805, 1);
            put(5601, 0); put(1012, 1); put(40, 1); put(5208, 1); put(19, 1); put(5004, 1); put(3309, 1); put(4404, 1);
            put(7, 0); put(5010, 0); put(1408, 1); put(4511, 1); put(5007, 1); put(4400, 1); put(4505, 1); put(39, 1);
            put(52, 1); put(60, 1); put(1103, 1); put(86, 1); put(5322, 1); put(5211, 1); put(4810, 1); put(6101, 1);
            put(2500, 1); put(3301, 1); put(4001, 1); put(21, 1); put(78, 1); put(1104, 1); put(4706, 1); put(5716, 1);
            put(4509, 1); put(4802, 1); put(5811, 1); put(14, 1); put(4705, 1); put(4813, 1); put(1600, 1); put(5014, 1);
            put(3311, 1); put(4104, 1); put(4008, 1); put(50, 1); put(4204, 1); put(4108, 1); put(4806, 1); put(5008, 1);
            put(4504, 1); put(3000, 1); put(4601, 1); put(1503, 1); put(6113, 1); put(5002, 1); put(1009, 1); put(4607, 1);
            put(1002, 1); put(5712, 1); put(4200, 1); put(3600, 1); put(4520, 1); put(23, 1); put(6, 1); put(4402, 1);
            put(5401, 0); put(29, 1); put(74, 1); put(3308, 1); put(2106, 1); put(25, 1); put(3701, 1); put(5311, 1);
            put(3002, 1); put(12, 1); put(27, 1); put(1403, 1); put(3300, 1); put(3704, 1); put(5209, 1); put(77, 1);
            put(33, 1); put(2600, 1); put(2100, 1); put(1102, 1); put(4512, 1); put(903, 1); put(5005, 1); put(1501, 1);
            put(31, 1); put(64, 1); put(5702, 0); put(1412, 1); put(66, 1); put(1700, 1); put(1001, 1); put(57, 1);
            put(5001, 1); put(3304, 1); put(4703, 1); put(65, 1); put(5310, 1); put(5018, 1); put(4005, 0); put(4207, 1);
            put(3307, 1); put(6015, 1); put(13, 1); put(37, 1); put(4105, 1); put(84, 1); put(1410, 1); put(1502, 1);
            put(4401, 0); put(49, 1); put(4514, 0); put(4004, 1); put(1010, 1); put(902, 1); put(11, 1); put(4708, 1);
            put(1406, 1); put(4821, 1); put(6016, 0); put(1505, 1); put(45, 1); put(1008, 1); put(58, 1); put(47, 1);
            put(5502, 1); put(2801, 1); put(3001, 1); put(4510, 1); put(5009, 1); put(4502, 1); put(4000, 1); put(4206, 1);
            put(4513, 0); put(55, 1); put(8, 1); put(56, 1); put(4702, 1); put(4823, 1); put(900, 1); put(36, 1);
            put(5101, 1); put(5104, 1); put(801, 1); put(1, 1); put(30, 1); put(1401, 1); put(1300, 1); put(18, 1);
            put(4009, 1); put(4, 1); put(5, 1); put(5701, 0); put(4604, 1); put(5213, 1); put(5105, 1); put(1504, 1);
            put(2403, 1); put(4403, 1); put(5503, 1); put(2105, 1); put(83, 1); put(85, 1); put(5504, 1); put(1100, 1);
            put(1500, 1); put(4709, 1); put(803, 1); put(4704, 1); put(22, 1); put(5216, 1); put(5318, 1); put(4804, 1);
            put(4820, 1); put(28, 1); put(1404, 1); put(4107, 1); put(1004, 1); put(1003, 1); put(4503, 1); put(32, 1);
            put(1007, 1); put(54, 1); put(38, 1); put(90, 1); put(62, 1); put(4701, 1);
        }});

        this.data = proto.build().toByteArray();
    }

    @Override
    public int getCode() {
        return PacketIdentifiers.Send.OpenStateUpdateNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}

/// TODO: FINISH