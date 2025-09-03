package org.emilieps.game.packets.base;

// Imports
import static org.emilieps.data.PacketIdentifiers.PACKET_HEAD;
import static org.emilieps.data.PacketIdentifiers.PACKET_MAGIC;
import java.io.ByteArrayOutputStream;
import lombok.Data;
import org.emilieps.Application;
import org.emilieps.library.EncryptionLib;

// Protocol buffers
import generated.emilieps.protobuf.PacketHeadOuterClass.PacketHead;

@Data
public class OutboundPacket {
    /**
     * The ID of the packet
     */
    private final int id;

    /**
     * The packet's header
     */
    private byte[] header = new byte[0];

    /**
     * The packet's payload
     */
    private byte[] data = new byte[0];

    /**
     * Should build the packet header.
     */
    private boolean shouldBuildHeader = false;

    /**
     * Should use the dispatch key to encryption the packet.
     */
    private boolean useDispatchKey = false;

    /**
     * Creates a new outbound packet by id.
     * @param id The packet id.
     */
    public OutboundPacket(int id) {
        this.id = id;
    }

    /**
     * Creates a new outbound packet by id.
     * @param id The packet id.
     * @param shouldBuildHeader Should build the packet header.
     */
    public OutboundPacket(int id, boolean shouldBuildHeader) {
        this.id = id;
        this.shouldBuildHeader = shouldBuildHeader;
    }

    /**
     * Creates a new outbound packet by id.
     * @param id The packet id.
     * @param seq The packet header's sequence id.
     */
    public OutboundPacket(int id, int seq) {
        this.id = id;
        this.buildHeader(seq);
    }

    /**
     * Creates a new packet by id.
     * @param id The packet id.
     * @param shouldUseDispatchKey Should use the dispatch key for encryption.
     * @param shouldBuildHeader Should build the packet header.
     */
    public OutboundPacket(int id, boolean shouldUseDispatchKey, boolean shouldBuildHeader) {
        this.id = id;
        this.useDispatchKey = shouldUseDispatchKey;
        this.shouldBuildHeader = shouldBuildHeader;
    }

    /**
     * Builds the packet.
     * @return A bytearray that is the packet.
     */
    public byte[] build() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2 + 2 + 2 + 4 + this.header.length + this.data.length + 2);
        baos.write((byte) ((PACKET_HEAD >>> 8) & 0xff));
        baos.write((byte) (PACKET_HEAD & 0xff));
        baos.write((byte) ((this.id >>> 8) & 0xff));
        baos.write((byte) (this.id & 0xff));
        baos.write((byte) ((this.header.length >>> 8) & 0xff));
        baos.write((byte) (this.header.length & 0xff));
        baos.write((byte) ((this.data.length >>> 24) & 0xff));
        baos.write((byte) ((this.data.length >>> 16) & 0xff));
        baos.write((byte) ((this.data.length >>> 8) & 0xff));
        baos.write((byte) (this.data.length & 0xff));
        baos.writeBytes(this.header);
        baos.writeBytes(this.data);
        baos.write((byte) ((PACKET_MAGIC >>> 8) & 0xff));
        baos.write((byte) (PACKET_MAGIC & 0xff));

        byte[] bytes = baos.toByteArray();
        if (Application.getApplicationConfig().enable_encryption) {
            EncryptionLib.performXor(bytes, (useDispatchKey) ? EncryptionLib.getDispatchKey() : EncryptionLib.getSecretKey());
        }

        return bytes;
    }

    /**
     * Builds the packet's header.
     * @param clientSequence The client's next sequence.
     */
    public void buildHeader(int clientSequence) {
        if(this.header.length != 0 && clientSequence == 0)
            return;

        this.header = PacketHead.newBuilder().setClientSequenceId(clientSequence).setSentMs(System.currentTimeMillis()).build().toByteArray();
    }
}