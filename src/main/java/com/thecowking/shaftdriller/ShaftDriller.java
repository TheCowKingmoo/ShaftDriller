package com.thecowking.shaftdriller;

import com.thecowking.shaftdriller.setup.ClientSetup;
import com.thecowking.shaftdriller.setup.Config;
import com.thecowking.shaftdriller.setup.ModSetup;
import com.thecowking.shaftdriller.setup.Registration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(com.thecowking.shaftdriller.ShaftDriller.MODID)
public class ShaftDriller {

    public static final String MODID = "shaftdriller";

    public ShaftDriller() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        Registration.init();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
    }
}
