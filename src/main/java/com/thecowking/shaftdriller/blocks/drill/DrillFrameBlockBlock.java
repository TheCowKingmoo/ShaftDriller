package com.thecowking.shaftdriller.blocks.drill;

import com.thecowking.shaftdriller.blocks.MultiBlockFrameBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DrillFrameBlockBlock extends MultiBlockFrameBlock {

    public DrillFrameBlockBlock() {super();}

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        if(!world.isRemote)  {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof DrillFrameTile) {
                DrillFrameTile drillFrameTile = (DrillFrameTile) tileEntity;
                BlockPos controllerPos = drillFrameTile.getController();
                if(controllerPos != null)  {
                    // TODO - error handle
                    BlockState controllerState = world.getBlockState(controllerPos);
                    DrillControllerBlock controllerBlock = (DrillControllerBlock) controllerState.getBlock();
                    controllerBlock.onBlockActivated(controllerState, world, controllerPos, player, hand, trace);
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if(!worldIn.isRemote)  {
            if (te instanceof DrillFrameTile) {
                DrillFrameTile drillFrameTile = (DrillFrameTile) te;
                BlockPos controllerPos = drillFrameTile.getController();
                if (controllerPos != null) {
                    TileEntity controllerTile = worldIn.getTileEntity(controllerPos);
                    if( controllerTile instanceof DrillControllerTile)  {
                        DrillControllerTile drillControllerTile = (DrillControllerTile) controllerTile;
                        drillControllerTile.breakMultiBlock(worldIn);
                    }  else  {
                        LOGGER.info("controllerTile not isntance of DrillCoreTile");
                    }

                }  else  {
                    LOGGER.info("controllerPos = null");
                }
            }  else  {
                LOGGER.info("not instance of DrillFrameTile");
            }
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> list, ITooltipFlag flags) {
        list.add(new TranslationTextComponent("message.drill_frame_block"));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    // creates the tile entity
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DrillFrameTile();
    }

}
