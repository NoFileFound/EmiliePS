package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import org.kcp.erasure.fec.Snmp;

public class KcpOutPutImp implements KcpOutput {
    @Override
    public void out(ByteBuf data, IKcp kcp) {
        Snmp.snmp.OutPkts.increment();
        Snmp.snmp.OutBytes.add(data.writerIndex());
        User user = kcp.getUser();
        user.getChannel().writeAndFlush(new DatagramPacket(data,user.getRemoteAddress(), user.getLocalAddress()));
    }
}