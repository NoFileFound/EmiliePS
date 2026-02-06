package org.genshinimpact.webserver.stores;

// Imports
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.genshinimpact.utils.CryptoUtils;
import org.springframework.stereotype.Component;

@Component
public class PandaQRCodesStore {
    private final Map<String, PandaQRCode> storage = new ConcurrentHashMap<>();

    /**
     * Generates a new QR code session for the given device.
     * @param deviceId The client's ticket id.
     */
    public PandaQRCode generateQRCode(String deviceId) {
        String ticketId = CryptoUtils.generateStringKey(24);
        var myQrCode = new PandaQRCode(ticketId, deviceId);
        this.storage.put(ticketId, myQrCode);
        return myQrCode;
    }

    /**
     * Returns the current status of the QR code.
     * @param ticketId The client's ticket id.
     * @return The current status of the QR code.
     */
    public int getQrCodeStatus(String ticketId) {
        var qrCode = this.storage.get(ticketId);
        if(qrCode == null) {
            return -1000;
        }

        if(qrCode.getTime() - System.currentTimeMillis() <= 0) {
            System.out.println("#5");
            this.storage.remove(ticketId);
            return -1001;
        }

        if(qrCode.state == PandaQRCode.QRCodeState.Scanned) {
            return -1002;
        }

        if(qrCode.state == PandaQRCode.QRCodeState.Confirmed) {
            return -1003;
        }

        return 0;
    }

    /**
     * Marks the QR code as scanned.
     * @param ticketId The client's ticket id.
     */
    public void setScannedQrCode(String ticketId) {
        this.storage.get(ticketId).state = PandaQRCode.QRCodeState.Scanned;
    }

    /**
     * Marks the QR code as confirmed.
     * @param ticketId The client's ticket id.
     */
    public void setConfirmedQrCode(String ticketId) {
        this.storage.get(ticketId).state = PandaQRCode.QRCodeState.Confirmed;
    }

    /**
     * Returns the serialized raw payload associated with the confirmed QR code.
     * @param ticketId The client's ticket id.
     * @return The JSON payload as a string.
     */
    public String getRawPayload(String ticketId) {
        /// TODO: Implement getRawPayload() in PandaQRCodesStore
        return "";
    }

    public static class PandaQRCode {
        @Getter private final String id;
        @Getter private final String deviceId;
        @Getter private final long time;
        private QRCodeState state;

        public PandaQRCode(String id, String deviceId) {
            this.id = id;
            this.deviceId = deviceId;
            this.time = System.currentTimeMillis() + 300000;
            this.state = QRCodeState.Init;
        }

        public enum QRCodeState {
            Init,
            Scanned,
            Confirmed
        }
    }
}