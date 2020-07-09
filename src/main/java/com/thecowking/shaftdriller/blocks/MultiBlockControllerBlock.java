package com.thecowking.shaftdriller.blocks;

import com.thecowking.shaftdriller.blocks.drill.Drill;
import com.thecowking.shaftdriller.blocks.drill.DrillControllerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class MultiBlockControllerBlock extends Block implements IMultiBlockControllerBlock {
    public MultiBlockControllerBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .harvestTool(ToolType.PICKAXE)
        );
        setDefaultState(this.getDefaultState().with(Drill.FORMED, false));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    // creates the tile entity
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DrillControllerTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED, Drill.FORMED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        if (!worldIn.isRemote) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof IMultiBlockControllerTile) {
                IMultiBlockControllerTile controllerTile = (IMultiBlockControllerTile) tileEntity;
                if(controllerTile.isFormed(worldIn))  {
                    controllerTile.openGUI(worldIn, pos, player, controllerTile);
                }  else if(controllerTile.isValidMultiBlockFormer(player.getHeldItem(hand).getItem())) {
                    LOGGER.info("no gui- attempt to form");

                    // attempts to form the multi-block
                    controllerTile.tryToFormMultiBlock(worldIn, pos);
                }  else  {
                    return ActionResultType.FAIL;
                }
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if(!worldIn.isRemote)  {
            if( te != null) {
                if(te instanceof IMultiBlockControllerBlock)  {
                    DrillControllerTile drillControllerTile = (DrillControllerTile) te;
                    if(state.get(Drill.FORMED)) {
                        drillControllerTile.breakMultiBlock(worldIn);
                    }
                }  else  {
                    LOGGER.info("not an instance of");
                }
            }
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

}

