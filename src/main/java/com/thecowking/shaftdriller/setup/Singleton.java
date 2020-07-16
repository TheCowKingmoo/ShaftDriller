package com.thecowking.shaftdriller.setup;

import com.thecowking.shaftdriller.blocks.drill.DrillFrameBlock;
import com.thecowking.shaftdriller.tools.SDFakePlayer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class Singleton {
    public static FakePlayer getFakePlayer(World worldIn, BlockPos pos) {
        FakePlayer player = new SDFakePlayer((ServerWorld) worldIn);
        if (player != null) player.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), 90, 90);
        return player;
    }
}
