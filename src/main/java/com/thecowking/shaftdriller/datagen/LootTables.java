package com.thecowking.shaftdriller.datagen;

import com.thecowking.shaftdriller.setup.Registration;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(Registration.FIRSTBLOCK.get(), createStandardTable("firstblock", Registration.FIRSTBLOCK.get()));
        lootTables.put(Registration.MINER_BLOCK.get(), createStandardTable("miner_block", Registration.MINER_BLOCK.get()));
    }
}
