package com.thecowking.shaftdriller.datagen;

import com.thecowking.shaftdriller.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;

public class Items extends ItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, com.thecowking.shaftdriller.ShaftDriller.MODID, existingFileHelper);
    }

    //TODO - add things here
    @Override
    protected void registerModels() {
        singleTexture(Registration.FIRSTITEM.get().getRegistryName().getPath(), new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "item/firstitem"));
        withExistingParent(Registration.FIRSTBLOCK_ITEM.get().getRegistryName().getPath(), new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/firstblock"));
    }
}
