package com.THproject.tharidia_realmsandclaim.client;

import com.THproject.tharidia_realmsandclaim.Config;
import com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim;
import com.THproject.tharidia_realmsandclaim.network.ClaimOwnerSyncPacket;
import com.THproject.tharidia_realmsandclaim.network.RealmSyncPacket;
import com.THproject.tharidia_realmsandclaim.network.HierarchySyncPacket;
import com.mojang.logging.LogUtils;
import com.THproject.tharidia_realmsandclaim.block.entity.PietroBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientPacketHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<PietroBlockEntity> syncedRealms = new ArrayList<>();
    // Stores hierarchy data: Map of player UUID to rank level
    private static Map<UUID, Integer> cachedHierarchyData = new HashMap<>();
    // Stores player names: Map of player UUID to name
    private static Map<UUID, String> cachedPlayerNames = new HashMap<>();
    private static UUID cachedOwnerUUID = null;
    private static String cachedOwnerName = "";

    public static void handleClaimOwnerSync(ClaimOwnerSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(packet.pos());
                if (blockEntity instanceof PietroBlockEntity claimEntity) {
                    // Note: ClaimOwnerSyncPacket is for ClaimBlock, but seems misused here?
                    // Assuming it's for setting owner, but PietroBlockEntity doesn't have setOwnerUUID
                }
            }
        });
    }

    public static void handleRealmSync(RealmSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.info("[REALM SYNC] Received RealmSyncPacket with {} realms (fullSync: {})", 
                packet.realms().size(), packet.fullSync());
            if (Config.DEBUG_REALM_SYNC.get()) {
                LOGGER.info("[DEBUG] Current syncedRealms size before processing: {}", syncedRealms.size());
            }
            
            if (packet.fullSync()) {
                if (Config.DEBUG_REALM_SYNC.get()) {
                    LOGGER.info("[DEBUG] Full sync - clearing {} existing realms", syncedRealms.size());
                }
                syncedRealms.clear();
            }
            
            for (RealmSyncPacket.RealmData data : packet.realms()) {
                if (Config.DEBUG_REALM_SYNC.get()) {
                    LOGGER.info("[DEBUG] Syncing realm at {} with size {} and center chunk ({}, {})", 
                        data.pos(), data.realmSize(), data.centerChunkX(), data.centerChunkZ());
                }
                
                try {
                    Level level = Minecraft.getInstance().level;
                    net.minecraft.world.level.block.state.BlockState blockState = null;
                    if (level != null && level.isLoaded(data.pos())) {
                        blockState = level.getBlockState(data.pos());
                    }
                    
                    if (blockState == null || !blockState.is(TharidiaRealmsAndClaim.PIETRO.get())) {
                        blockState = TharidiaRealmsAndClaim.PIETRO.get().defaultBlockState();
                    }
                    
                    final net.minecraft.world.level.block.state.BlockState finalBlockState = blockState;
                    
                    PietroBlockEntity dummy = new PietroBlockEntity(data.pos(), finalBlockState) {
                        private final int syncedRealmSize = data.realmSize();
                        private final net.minecraft.world.level.ChunkPos syncedCenterChunk = 
                            new net.minecraft.world.level.ChunkPos(data.centerChunkX(), data.centerChunkZ());
                        
                        @Override
                        public int getRealmSize() {
                            return syncedRealmSize;
                        }

                        @Override
                        public String getOwnerName() {
                            return data.ownerName();
                        }
                        
                        @Override
                        public net.minecraft.world.level.ChunkPos getCenterChunk() {
                            return syncedCenterChunk;
                        }
                        
                        @Override
                        public net.minecraft.world.level.ChunkPos getMinChunk() {
                            int radius = syncedRealmSize / 2;
                            return new net.minecraft.world.level.ChunkPos(
                                syncedCenterChunk.x - radius, 
                                syncedCenterChunk.z - radius
                            );
                        }
                        
                        @Override
                        public net.minecraft.world.level.ChunkPos getMaxChunk() {
                            int radius = syncedRealmSize / 2;
                            return new net.minecraft.world.level.ChunkPos(
                                syncedCenterChunk.x + radius, 
                                syncedCenterChunk.z + radius
                            );
                        }
                        
                        @Override
                        public net.minecraft.world.level.ChunkPos getOuterLayerMinChunk() {
                            int outerRadius = (syncedRealmSize / 2) + 6;
                            return new net.minecraft.world.level.ChunkPos(
                                syncedCenterChunk.x - outerRadius,
                                syncedCenterChunk.z - outerRadius
                            );
                        }
                        
                        @Override
                        public net.minecraft.world.level.ChunkPos getOuterLayerMaxChunk() {
                            int outerRadius = (syncedRealmSize / 2) + 6;
                            return new net.minecraft.world.level.ChunkPos(
                                syncedCenterChunk.x + outerRadius,
                                syncedCenterChunk.z + outerRadius
                            );
                        }
                    };
                    dummy.centerChunk = new net.minecraft.world.level.ChunkPos(data.centerChunkX(), data.centerChunkZ());
                    
                    if (packet.fullSync()) {
                        syncedRealms.add(dummy);
                        if (Config.DEBUG_REALM_SYNC.get()) {
                            LOGGER.info("[DEBUG] Added realm at {} with size {}", data.pos(), data.realmSize());
                        }
                    } else {
                        boolean updated = false;
                        for (int i = 0; i < syncedRealms.size(); i++) {
                            PietroBlockEntity existing = syncedRealms.get(i);
                            if (existing.getBlockPos().equals(data.pos())) {
                                syncedRealms.set(i, dummy);
                                updated = true;
                                LOGGER.info("Updated existing realm at {} to size {}", data.pos(), data.realmSize());
                                break;
                            }
                        }
                        
                        if (!updated) {
                            syncedRealms.add(dummy);
                            if (Config.DEBUG_REALM_SYNC.get()) {
                                LOGGER.info("[DEBUG] Added new synced realm at {} with size {}", data.pos(), data.realmSize());
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to sync realm at {}: {}", data.pos(), e.getMessage(), e);
                }
            }
            LOGGER.info("[REALM SYNC] Sync completed. Total synced realms: {}", syncedRealms.size());
            if (Config.DEBUG_REALM_SYNC.get()) {
                LOGGER.info("[DEBUG] Checking visibility restoration: fullSync={}, boundariesWereVisible={}, syncedRealms.isEmpty()={}", 
                    packet.fullSync(), ClientConnectionHandler.boundariesWereVisible, syncedRealms.isEmpty());
            }
            
            if (packet.fullSync() && ClientConnectionHandler.boundariesWereVisible && !syncedRealms.isEmpty()) {
                RealmBoundaryRenderer.setBoundariesVisible(true);
                LOGGER.info("[REALM SYNC] Restored boundary visibility after full sync - {} realms loaded", syncedRealms.size());
                ClientConnectionHandler.boundariesWereVisible = false;
            } else if (packet.fullSync()) {
                LOGGER.info("[REALM SYNC] NOT restoring visibility: boundariesWereVisible={}, syncedRealms.isEmpty()={}", 
                    ClientConnectionHandler.boundariesWereVisible, syncedRealms.isEmpty());
            }
        });
    }
    
    public static void handleHierarchySync(HierarchySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.info("Received HierarchySyncPacket with {} players", packet.hierarchyData().size());

            cachedHierarchyData = new HashMap<>(packet.hierarchyData());
            cachedPlayerNames = new HashMap<>(packet.playerNames());
            cachedOwnerUUID = packet.ownerUUID();
            cachedOwnerName = packet.ownerName();

            LOGGER.info("Hierarchy data cached. Owner: {} ({})", cachedOwnerName, cachedOwnerUUID);
        });
    }
    
    public static Map<UUID, Integer> getCachedHierarchyData() {
        return new HashMap<>(cachedHierarchyData);
    }

    public static Map<UUID, String> getCachedPlayerNames() {
        return new HashMap<>(cachedPlayerNames);
    }

    public static String getCachedPlayerName(UUID uuid) {
        return cachedPlayerNames.getOrDefault(uuid, "Unknown");
    }

    public static UUID getCachedOwnerUUID() {
        return cachedOwnerUUID;
    }

    public static String getCachedOwnerName() {
        return cachedOwnerName;
    }

    public static void clearHierarchyCache() {
        cachedHierarchyData.clear();
        cachedPlayerNames.clear();
        cachedOwnerUUID = null;
        cachedOwnerName = "";
    }
}
