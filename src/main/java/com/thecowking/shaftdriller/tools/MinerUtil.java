// Used Mekanism's MinerUtil as a template for this class


package com.thecowking.shaftdriller.tools;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MinerUtil {

    private static Item minerTool = Items.DIAMOND_PICKAXE;



    public static List<ItemStack> getDrops(ServerWorld world, BlockPos pos, boolean silk, BlockPos minerPosition) {
        BlockState state = world.getBlockState(pos);
        if (!(state.isAir(world, pos)) && state.getBlockHardness(world, pos) > -1) {
            ItemStack pickaxe = new ItemStack(minerTool);
            if (silk) {
                pickaxe.addEnchantment(Enchantments.SILK_TOUCH, 1);
            }
            LootContext.Builder lootContextBuilder = new LootContext.Builder(world)
                    .withRandom(world.rand)
                    .withParameter(LootParameters.POSITION, pos)
                    .withParameter(LootParameters.TOOL, pickaxe);
            return state.getDrops(lootContextBuilder);
        }
        return Collections.emptyList();
    }


    public static List<ItemStack> getVoidDrops(ServerWorld world, BlockPos pos, boolean silk, BlockPos minerPosition) {
        ItemStack item = new ItemStack(Items.COBBLESTONE);
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(item);
        return drops;
    }



    }
