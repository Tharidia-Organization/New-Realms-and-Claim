package com.THproject.tharidia_realmsandclaim.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "tharidia_realmsandclaim", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientKeyHandler {
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player == null) {
            return;
        }
        
        // Check if toggle claim boundaries key was pressed
        if (KeyBindings.TOGGLE_CLAIM_BOUNDARIES.consumeClick()) {
            ClaimBoundaryRenderer.toggleBoundaries();
            RealmBoundaryRenderer.toggleBoundaries();
        }
    }
}
