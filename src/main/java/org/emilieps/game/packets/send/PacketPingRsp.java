package org.emilieps.game.packets.send;

// Imports
import static org.emilieps.data.PacketRetcode.RET_SUCC;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.game.packets.base.OutboundPacket;

// Protocol buffers
import generated.emilieps.protobuf.PingRspOuterClass.PingRsp;

public final class PacketPingRsp extends OutboundPacket {
    /**
     * Creates a new response from the packet: Ping.
     *
     * @param client_time The client's last ping time.
     * @param sequence The client's last packet head sequence id.
     */
    public PacketPingRsp(int client_time, int sequence) {
        super(PacketIdentifiers.Send.PingRsp, sequence);
        this.setData(PingRsp.newBuilder().setRetcode(RET_SUCC.getValue()).setClientTime(client_time).setSeq(sequence).build().toByteArray());
    }
}