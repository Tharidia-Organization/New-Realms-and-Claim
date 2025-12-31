package com.THproject.tharidia_things;

import com.THproject.tharidia_things.command.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(TharidiaThings.MODID)
public class TharidiaThings {
    public static final String MODID = "tharidiathings";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TharidiaThings(IEventBus modEventBus, ModContainer modContainer) {
        // Register blocks
        BLOCKS.register(modEventBus);

        
        LOGGER.info("Tharidia Things - Realm & Claims Loaded");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Register commands here
        LOGGER.info("Commands registered");
    }

}
