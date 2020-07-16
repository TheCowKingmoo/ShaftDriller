package com.thecowking.shaftdriller.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMultiBlockControllerTile {
    boolean isFormed(World worldIn);
    void setFormed(World worldIn, boolean b);
    boolean isValidMultiBlockFormer(Item item);
    void tryToFormMultiBlock(World worldIn, BlockPos pos);
    void openGUI(World worldIn, BlockPos pos, PlayerEntity player, IMultiBlockControllerTile tileEntity);

    void setDirty(boolean b);
}
