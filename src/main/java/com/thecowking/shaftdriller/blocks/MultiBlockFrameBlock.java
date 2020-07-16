package com.thecowking.shaftdriller.blocks;

import com.thecowking.shaftdriller.blocks.drill.Drill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(REDSTONE);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    public boolean canProvidePower(BlockState state) {
        return true;
    }



}
