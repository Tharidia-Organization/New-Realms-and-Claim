package com.THproject.tharidia_realmsandclaim.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "tharidia_realmsandclaim", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    
    public static final String KEY_CATEGORY = "key.categories.tharidia_realmsandclaim";
    
    public static final KeyMapping TOGGLE_CLAIM_BOUNDARIES = new KeyMapping(
        "key.tharidia_realmsandclaim.toggle_claim_boundaries",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B, // Default to 'B' key
        KEY_CATEGORY
    );
    
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CLAIM_BOUNDARIES);
    }
}
