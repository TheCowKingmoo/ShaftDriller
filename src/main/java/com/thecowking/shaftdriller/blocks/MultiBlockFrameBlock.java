package com.thecowking.shaftdriller.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraftforge.common.ToolType;

public class MultiBlockFrameBlock extends Block implements IMultiBlockFrameBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public MultiBlockFrameBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .harvestTool(ToolType.PICKAXE)
        );
        setDefaultState(this.getDefaultState().with(FORMED, false));
    }
}
