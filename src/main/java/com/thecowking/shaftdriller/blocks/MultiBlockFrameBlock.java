package com.thecowking.shaftdriller.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.common.ToolType;

public class MultiBlockFrameBlock extends Block implements IMultiBlockFrameBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final IntegerProperty REDSTONE = BlockStateProperties.POWER_0_15;

    public MultiBlockFrameBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .harvestTool(ToolType.PICKAXE)
        );
        setDefaultState(this.getDefaultState().with(FORMED, false));
        setDefaultState(this.getDefaultState().with(BlockStateProperties.POWER_0_15, 0));
    }
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
        builder.add(REDSTONE);
    }
}
