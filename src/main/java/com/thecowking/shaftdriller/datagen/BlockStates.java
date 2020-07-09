package com.thecowking.shaftdriller.datagen;

import com.thecowking.shaftdriller.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;

import java.util.function.Function;

public class BlockStates extends BlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, com.thecowking.shaftdriller.ShaftDriller.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFirstBlock();
        registerMinerBlock();
    }

    private void registerFirstBlock() {
        ResourceLocation txt = new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/firstblock");
        BlockModelBuilder modelFirstblock = models().cube("firstblock", txt, txt, new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/firstblock_front"), txt, txt, txt);
        BlockModelBuilder modelFirstblockPowered = models().cube("firstblock_powered", txt, txt, new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/firstblock_powered"), txt, txt, txt);
        orientedBlock(Registration.FIRSTBLOCK.get(), state -> {
            if (state.get(BlockStateProperties.POWERED)) {
                return modelFirstblockPowered;
            } else {
                return modelFirstblock;
            }
        });
    }

    private void registerMinerBlock() {
        ResourceLocation txt = new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/miner_block");
        BlockModelBuilder modelMinerblock = models().cube("miner_block", txt, txt, new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/Miner_block_front"), txt, txt, txt);
        BlockModelBuilder modelMinerblockPowered = models().cube("Miner_block_powered", txt, txt, new ResourceLocation(com.thecowking.shaftdriller.ShaftDriller.MODID, "block/miner_block_powered"), txt, txt, txt);
        orientedBlock(Registration.MINER_BLOCK.get(), state -> {
            if (state.get(BlockStateProperties.POWERED)) {
                return modelMinerblockPowered;
            } else {
                return modelMinerblock;
            }
        });
    }



    private void orientedBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
        getVariantBuilder(block)
                .forAllStates(state -> {
                    Direction dir = state.get(BlockStateProperties.FACING);
                    return ConfiguredModel.builder()
                            .modelFile(modelFunc.apply(state))
                            .rotationX(dir.getAxis() == Direction.Axis.Y ?  dir.getAxisDirection().getOffset() * -90 : 0)
                            .rotationY(dir.getAxis() != Direction.Axis.Y ? ((dir.getHorizontalIndex() + 2) % 4) * 90 : 0)
                            .build();
                });
    }

}
