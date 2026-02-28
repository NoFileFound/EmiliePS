package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Crc32Encode extends ChannelOutboundHandlerAdapter {
    private final CRC32 crc32 = new CRC32();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        ByteBuf data = datagramPacket.content();
        ByteBuffer byteBuffer = data.nioBuffer(ChannelConfig.crc32Size,data.readableBytes()-ChannelConfig.crc32Size);
        crc32.reset();
        crc32.update(byteBuffer);
        long checksum = crc32.getValue();
        data.setIntLE(0, (int) checksum);
        ctx.write(datagramPacket, promise);
    }
}