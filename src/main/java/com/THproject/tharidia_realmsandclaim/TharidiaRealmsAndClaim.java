package com.THproject.tharidia_realmsandclaim;

import com.THproject.tharidia_realmsandclaim.claim.ClaimRegistry;
import com.THproject.tharidia_realmsandclaim.client.gui.ClaimScreen;
import com.THproject.tharidia_realmsandclaim.client.gui.PietroScreen;
import com.THproject.tharidia_realmsandclaim.command.ClaimCommands;
import com.THproject.tharidia_realmsandclaim.command.ClaimAdminCommands;
import com.THproject.tharidia_realmsandclaim.config.CropProtectionConfig;
import com.THproject.tharidia_realmsandclaim.event.ClaimProtectionHandler;
import com.THproject.tharidia_realmsandclaim.event.ClaimExpirationHandler;
import com.THproject.tharidia_realmsandclaim.event.RealmPlacementHandler;
import com.THproject.tharidia_realmsandclaim.gui.ClaimMenu;
import com.THproject.tharidia_realmsandclaim.gui.PietroMenu;
import com.THproject.tharidia_realmsandclaim.network.ClaimOwnerSyncPacket;
import com.THproject.tharidia_realmsandclaim.network.HierarchySyncPacket;
import com.THproject.tharidia_realmsandclaim.network.RealmSyncPacket;
import com.THproject.tharidia_realmsandclaim.network.UpdateHierarchyPacket;
import com.THproject.tharidia_realmsandclaim.realm.RealmManager;
import com.THproject.tharidia_realmsandclaim.client.ClientPacketHandler;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.THproject.tharidia_realmsandclaim.block.PietroBlock;
import com.THproject.tharidia_realmsandclaim.block.ClaimBlock;
import com.THproject.tharidia_realmsandclaim.block.entity.PietroBlockEntity;
import com.THproject.tharidia_realmsandclaim.block.entity.ClaimBlockEntity;
import com.THproject.tharidia_realmsandclaim.item.PietroBlockItem;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TharidiaRealmsAndClaim.MODID)
public class TharidiaRealmsAndClaim {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tharidia_realmsandclaim";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "tharidia_realmsandclaim" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "tharidia_realmsandclaim" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold BlockEntities which will all be registered
    // under the "tharidia_realmsandclaim" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be
    // registered under the "tharidia_realmsandclaim" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);
    // Create a Deferred Register to hold MenuTypes which will all be registered
    // under the "tharidia_realmsandclaim" namespace
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister
            .create(BuiltInRegistries.MENU, MODID);


    // Creates a new Block with the id "tharidia_realmsandclaim:pietro", combining the
    // namespace and path
    public static final DeferredBlock<PietroBlock> PIETRO = BLOCKS.register("pietro", () -> new PietroBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 6.0F).noOcclusion()));
    // Creates a new BlockItem with the id "tharidia_realmsandclaim:pietro", combining the
    // namespace and path. Uses custom PietroBlockItem for GeckoLib inventory rendering.
    public static final DeferredItem<PietroBlockItem> PIETRO_ITEM = ITEMS.register("pietro",
            () -> new PietroBlockItem(PIETRO.get(), new Item.Properties()));
    // Creates a new Block with the id "tharidia_realmsandclaim:claim"
    public static final DeferredBlock<ClaimBlock> CLAIM = BLOCKS.register("claim",
            () -> new ClaimBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 6.0F)));
    // Creates a new BlockItem with the id "tharidia_realmsandclaim:claim"
    public static final DeferredItem<BlockItem> CLAIM_ITEM = ITEMS.registerSimpleBlockItem("claim", CLAIM);

    // Creates a new BlockEntityType for the Pietro block
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PietroBlockEntity>> PIETRO_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("pietro", () -> BlockEntityType.Builder.of(PietroBlockEntity::new, PIETRO.get()).build(null));
    // Creates a new BlockEntityType for the Claim block
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClaimBlockEntity>> CLAIM_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("claim", () -> BlockEntityType.Builder.of(ClaimBlockEntity::new, CLAIM.get()).build(null));

    // Creates a MenuType for the Claim GUI
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<ClaimMenu>> CLAIM_MENU = MENU_TYPES
            .register("claim_menu", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension
                    .create(ClaimMenu::new));

    // Creates a MenuType for the Pietro GUI
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<PietroMenu>> PIETRO_MENU = MENU_TYPES
            .register("pietro_menu", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension
                    .create(PietroMenu::new));

    // Creates a creative tab with the id "tharidia_realmsandclaim:tharidia_tab" for the mod
    // items, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THARIDIA_TAB = CREATIVE_MODE_TABS
            .register("tharidia_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tharidia_realmsandclaim")) // The language key for the title of your
                                                                               // CreativeModeTab
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> PIETRO_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PIETRO_ITEM.get());
                        output.accept(CLAIM_ITEM.get());
                    }).build());

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public TharidiaRealmsAndClaim(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register network packets
        modEventBus.addListener(this::registerPayloads);
        // Register client-side screen handlers (only on client)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerScreens);
        }

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entities get registered
        BLOCK_ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so menus get registered
        MENU_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class
        // (TharidiaRealmsAndClaim) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        // Register server stopping event
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        // Register the claim protection handler
        NeoForge.EVENT_BUS.register(ClaimProtectionHandler.class);
        // Register the claim expiration handler
        NeoForge.EVENT_BUS.register(ClaimExpirationHandler.class);
        // Register the realm placement handler
        NeoForge.EVENT_BUS.register(RealmPlacementHandler.class);

        // Log version for debugging
        LOGGER.info("=================================================");
        LOGGER.info("Tharidia - Realms and Claim v0.1.0-alpha LOADED");
        LOGGER.info("Features: Realm Management, Claims, Hierarchies, Protection");
        LOGGER.info("=================================================");

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Common setup
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        LOGGER.info("Registering network payloads (dist: {})", FMLEnvironment.dist);

        // Network payload registration for mod features

        if (FMLEnvironment.dist.isClient()) {

            registrar.playToClient(
                    ClaimOwnerSyncPacket.TYPE,
                    ClaimOwnerSyncPacket.STREAM_CODEC,
                    ClientPacketHandler::handleClaimOwnerSync);
            registrar.playToClient(
                    RealmSyncPacket.TYPE,
                    RealmSyncPacket.STREAM_CODEC,
                    ClientPacketHandler::handleRealmSync);
            registrar.playToClient(
                    HierarchySyncPacket.TYPE,
                    HierarchySyncPacket.STREAM_CODEC,
                    ClientPacketHandler::handleHierarchySync);

            // Register dummy handlers for server-bound packets (client-side only for handshake)
            // Note: All server-bound packets are registered below with actual handlers
            // No dummy handlers needed here as they're registered with real handlers

            LOGGER.info("Client packet handlers registered");
        } else {

            // Server-side packet registration

            // On server, register dummy handlers (packets won't be received here anyway)
            registrar.playToClient(
                    ClaimOwnerSyncPacket.TYPE,
                    ClaimOwnerSyncPacket.STREAM_CODEC,
                    (packet, context) -> {
                    });
            registrar.playToClient(
                    RealmSyncPacket.TYPE,
                    RealmSyncPacket.STREAM_CODEC,
                    (packet, context) -> {
                    });
            registrar.playToClient(
                    HierarchySyncPacket.TYPE,
                    HierarchySyncPacket.STREAM_CODEC,
                    (packet, context) -> {
                    });
            LOGGER.info("Server-side packet registration completed (dummy handlers)");
        }

        // Register server-bound packets (works on both sides)
        registrar.playToServer(
                UpdateHierarchyPacket.TYPE,
                UpdateHierarchyPacket.STREAM_CODEC,
                UpdateHierarchyPacket::handle);
    }

    private void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(CLAIM_MENU.get(), ClaimScreen::new);
        event.register(PIETRO_MENU.get(), PietroScreen::new);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Send a full sync on login - this will clear and replace all client-side realm
            // data
            syncAllRealmsToPlayer((ServerPlayer) event.getEntity(), serverLevel);
        }
    }

    /**
     * Sends all realm data to a specific player (full sync)
     */
    private void syncAllRealmsToPlayer(ServerPlayer player, net.minecraft.server.level.ServerLevel serverLevel) {
        List<RealmSyncPacket.RealmData> realmDataList = new ArrayList<>();
        List<PietroBlockEntity> allRealms = RealmManager.getRealms(serverLevel);

        for (PietroBlockEntity realm : allRealms) {
            RealmSyncPacket.RealmData data = new RealmSyncPacket.RealmData(
                    realm.getBlockPos(),
                    realm.getRealmSize(),
                    realm.getOwnerName(),
                    realm.getCenterChunk().x,
                    realm.getCenterChunk().z);
            realmDataList.add(data);
        }

        RealmSyncPacket packet = new RealmSyncPacket(realmDataList, true); // true = full sync
        PacketDistributor.sendToPlayer(player, packet);

        LOGGER.info("Synced {} realms to player {}", realmDataList.size(), player.getName().getString());
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Load claim registry from persistent storage
        net.minecraft.server.level.ServerLevel overworld = event.getServer()
                .getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null) {
            ClaimRegistry.loadFromPersistentStorage(overworld);
        } else {
            LOGGER.error("Could not load claim registry: overworld is null");
        }

    }

    /**
     * Called when the server is stopping
     */
    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        LOGGER.info("Server stopping, cleaning up resources...");
        LOGGER.info("Resource cleanup completed");
    }

    @SubscribeEvent
    public void onAddReloadListeners(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        event.addListener(new CropProtectionConfig());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ClaimCommands.register(event.getDispatcher());
        ClaimAdminCommands.register(event.getDispatcher());
    }

    /**
     * Helper method to create a ResourceLocation for this mod
     */
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
