package org.genshinimpact.gameserver.packets;

// Imports
import static org.genshinimpact.gameserver.packets.PacketIdentifiers.PACKET_HEAD;
import static org.genshinimpact.gameserver.packets.PacketIdentifiers.PACKET_MAGIC;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.genshinimpact.gameserver.connection.ClientSession;

@Data
@AllArgsConstructor
public final class InboundPacket {
    /**
     * The packet id.
     */
    private final int id;

    /**
     * The Session this packet was received from.
     */
    private final ClientSession session;

    /**
     * The packet header.
     */
    private final byte[] header;

    /**
     * The packet data.
     */
    private final byte[] data;

    /**
     * Creates a new Packet from a {@link ByteBuf}.
     * @param buffer The buffer to read bytes from.
     */
    public InboundPacket(ByteBuf buffer, ClientSession session) throws BadPacketException {
        this.session = session;
        int topMagic = buffer.readShort();
        if(topMagic != PACKET_HEAD) {
            throw new BadPacketException("Expected a top magic value of " + PACKET_HEAD + ", got " + topMagic);
        }

        this.id = buffer.readShort();
        int headerLength = buffer.readShort();
        int dataLength = buffer.readInt();
        this.header = new byte[headerLength];
        buffer.readBytes(header);
        this.data = new byte[dataLength];
        buffer.readBytes(data);
        int bottomMagic = buffer.readShort();
        if(bottomMagic != PACKET_MAGIC) {
            throw new BadPacketException("Expected a bottom magic value of " + PACKET_MAGIC + ", got " + bottomMagic);
        }
    }
}