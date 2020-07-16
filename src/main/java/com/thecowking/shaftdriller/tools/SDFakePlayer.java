package com.thecowking.shaftdriller.tools;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class SDFakePlayer extends FakePlayer {
    private static final UUID uuid = UUID.fromString("e754f74d-d67f-4a55-9884-dec0b8903e8e");

    private static GameProfile PROFILE = new GameProfile(uuid, "[ShaftDriller]");

    public SDFakePlayer(ServerWorld worldIn) {
        super(worldIn, PROFILE);
    }

    @Override
    protected void playEquipSound(ItemStack stack) {
    }

    public void destroyBlock(World worldIn, BlockPos pos)  {
        ForgeHooks.onPickBlock(new BlockRayTraceResult(new Vec3d(0, 0, 0), Direction.DOWN, pos, false), this, worldIn);
    }

    public boolean placeBlock(World world, BlockPos pos, ItemStack stack) {
        this.setHeldItem(Hand.MAIN_HAND, stack);
        return ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(this, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(0, 0, 0), Direction.DOWN, pos, false))) == ActionResultType.SUCCESS;
    }
}

