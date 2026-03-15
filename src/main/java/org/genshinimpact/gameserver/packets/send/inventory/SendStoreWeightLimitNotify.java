package org.genshinimpact.gameserver.packets.send.inventory;

// Imports
import org.genshinimpact.gameserver.ServerApp;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.StoreTypeOuterClass.StoreType;
import org.generated.protobuf.StoreWeightLimitNotifyOuterClass.StoreWeightLimitNotify;

public final class SendStoreWeightLimitNotify implements SendPacket {
    private final byte[] data;

    public SendStoreWeightLimitNotify() {
        var proto =
            StoreWeightLimitNotify.newBuilder()
                .setStoreType(StoreType.STORE_PACK)
                .setWeightLimit(ServerApp.getGameConfig().inventoryLimit.itemLimit)
                .setWeaponCountLimit(ServerApp.getGameConfig().inventoryLimit.weaponInventoryLimit)
                .setReliquaryCountLimit(ServerApp.getGameConfig().inventoryLimit.reliquaryInventoryLimit)
                .setMaterialCountLimit(ServerApp.getGameConfig().inventoryLimit.materialInventoryLimit)
                .setFurnitureCountLimit(ServerApp.getGameConfig().inventoryLimit.furnitureInventoryLimit)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.StoreWeightLimitNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}