
/*
  First Block I have created. A lot of useless comments will be here which is more for me taking notes
  than of being of any actual use to anyone.

  notes were taken while watching mcjtys 2nd youtube video for 1.15
 */


package com.thecowking.shaftdriller.blocks.miner;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class MinerBlock extends Block {

    // set all basic properties of block
    public MinerBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .lightValue(14)                 // produces light when running
        );
    }

    // populate the tool tip
    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> list, ITooltipFlag flags) {
        // "message." will use en_us.json
        list.add(new TranslationTextComponent("message.miner_block"));
    }

    // important info on block - only one block will exist according to MC so to get a particular blocks value you need to use blockstate
    //  if block is powered return light value of block else no light
    @Override
    public int getLightValue(BlockState state) {
        return state.get(BlockStateProperties.POWERED) ? super.getLightValue(state) : 0;
    }

    // simple return
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    // diff between block and TE -> block can only have a few properties
    // TE can store many more such as power and is able to "tick"

    // creates the tile entity
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MinerBlockTile();
    }

    // gets the context for placing a block. this is key for getting a block with a front face to be placed
    // in a way that makes sense.
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        // will return the nearest direction the player is facing then grabbing the opposite of that
        return getDefaultState().with(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    // basically this function isn't supposed to be called directly by us (mcjty's words) which is why it is deprecated.
    // this may need refactoring.
    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        // when clicked we need to not only open a client side GUI but a server side one as well to rely info.
        if (!world.isRemote) {  //true if on server -> this method does nothing on client side
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof MinerBlockTile) {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("screen.shaftdriller.minerblock");
                    }

                    @Override
                    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new MinerBlockContainer(i, world, pos, playerInventory, playerEntity);
                    }
                };
                // ensures that packets are sent to client
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, tileEntity.getPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return ActionResultType.SUCCESS;
    }

    //
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }
}
