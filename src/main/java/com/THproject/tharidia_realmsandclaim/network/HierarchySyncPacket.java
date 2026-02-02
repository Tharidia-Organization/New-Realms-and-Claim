package com.THproject.tharidia_realmsandclaim.network;

import com.THproject.tharidia_realmsandclaim.block.entity.PietroBlockEntity;
import com.THproject.tharidia_realmsandclaim.realm.HierarchyRank;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim.MODID;

/**
 * Packet sent from server to client to sync hierarchy data for a realm
 */
public record HierarchySyncPacket(Map<UUID, Integer> hierarchyData, Map<UUID, String> playerNames, UUID ownerUUID, String ownerName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HierarchySyncPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "hierarchy_sync"));

    public static final StreamCodec<ByteBuf, HierarchySyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public HierarchySyncPacket decode(ByteBuf buf) {
            int count = ByteBufCodecs.INT.decode(buf);
            Map<UUID, Integer> hierarchyData = new HashMap<>();
            Map<UUID, String> playerNames = new HashMap<>();

            for (int i = 0; i < count; i++) {
                String uuidStr = ByteBufCodecs.STRING_UTF8.decode(buf);
                UUID uuid = UUID.fromString(uuidStr);
                int rankLevel = ByteBufCodecs.INT.decode(buf);
                String playerName = ByteBufCodecs.STRING_UTF8.decode(buf);
                hierarchyData.put(uuid, rankLevel);
                playerNames.put(uuid, playerName);
            }

            String ownerUUIDStr = ByteBufCodecs.STRING_UTF8.decode(buf);
            UUID ownerUUID = UUID.fromString(ownerUUIDStr);
            String ownerName = ByteBufCodecs.STRING_UTF8.decode(buf);

            return new HierarchySyncPacket(hierarchyData, playerNames, ownerUUID, ownerName);
        }

        @Override
        public void encode(ByteBuf buf, HierarchySyncPacket packet) {
            ByteBufCodecs.INT.encode(buf, packet.hierarchyData.size());

            for (Map.Entry<UUID, Integer> entry : packet.hierarchyData.entrySet()) {
                UUID uuid = entry.getKey();
                ByteBufCodecs.STRING_UTF8.encode(buf, uuid.toString());
                ByteBufCodecs.INT.encode(buf, entry.getValue());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.playerNames.getOrDefault(uuid, "Unknown"));
            }

            ByteBufCodecs.STRING_UTF8.encode(buf, packet.ownerUUID.toString());
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.ownerName);
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Creates a hierarchy sync packet from a PietroBlockEntity
     */
    public static HierarchySyncPacket fromPietroBlock(PietroBlockEntity pietroBlock) {
        Map<UUID, Integer> hierarchyData = new HashMap<>();
        Map<UUID, String> playerNames = new HashMap<>();

        // Get server player list for name lookups
        var playerList = ServerLifecycleHooks.getCurrentServer() != null
                ? ServerLifecycleHooks.getCurrentServer().getPlayerList()
                : null;

        for (Map.Entry<UUID, HierarchyRank> entry : pietroBlock.getAllPlayerHierarchies().entrySet()) {
            UUID uuid = entry.getKey();
            hierarchyData.put(uuid, entry.getValue().getLevel());

            // Try to get player name
            String name = "Unknown";
            if (playerList != null) {
                var player = playerList.getPlayer(uuid);
                if (player != null) {
                    name = player.getName().getString();
                } else {
                    // Try to get from game profile cache
                    var profileCache = ServerLifecycleHooks.getCurrentServer().getProfileCache();
                    if (profileCache != null) {
                        var profile = profileCache.get(uuid);
                        if (profile.isPresent()) {
                            name = profile.get().getName();
                        }
                    }
                }
            }
            playerNames.put(uuid, name);
        }

        return new HierarchySyncPacket(hierarchyData, playerNames, pietroBlock.getOwnerUUID(), pietroBlock.getOwnerName());
    }
}
