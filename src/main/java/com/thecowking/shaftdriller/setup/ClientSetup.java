package com.thecowking.shaftdriller.setup;

import com.thecowking.shaftdriller.blocks.drill.DrillScreen;
import com.thecowking.shaftdriller.blocks.firstblock.FirstBlockScreen;
import com.thecowking.shaftdriller.blocks.miner.MinerBlockScreen;
import com.thecowking.shaftdriller.client.AfterLivingRenderer;
import com.thecowking.shaftdriller.client.InWorldRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = com.thecowking.shaftdriller.ShaftDriller.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registration.FIRSTBLOCK_CONTAINER.get(), FirstBlockScreen::new);
        ScreenManager.registerFactory(Registration.MINER_BLOCK_CONTAINER.get(), MinerBlockScreen::new);
        ScreenManager.registerFactory(Registration.DRILL_CONTAINER.get(), DrillScreen::new);
        MinecraftForge.EVENT_BUS.addListener(InWorldRenderer::render);
        MinecraftForge.EVENT_BUS.addListener(AfterLivingRenderer::render);
    }

    @SubscribeEvent
    public void onTooltipPre(RenderTooltipEvent.Pre event) {
        Item item = event.getStack().getItem();
        if (item.getRegistryName().getNamespace().equals(com.thecowking.shaftdriller.ShaftDriller.MODID)) {
            event.setMaxWidth(200);
        }
    }
}
