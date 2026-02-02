package com.THproject.tharidia_realmsandclaim.integration.jei;

import com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI integration plugin for Tharidia Realms and Claim.
 * Registers mod items with JEI for recipe viewing compatibility.
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(
            TharidiaRealmsAndClaim.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Register mod blocks as crafting catalysts (optional - shows them in crafting category)
        // The crafting recipes themselves are automatically detected by JEI
    }
}
