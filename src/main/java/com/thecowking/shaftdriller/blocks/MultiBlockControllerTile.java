package com.thecowking.shaftdriller.blocks;

import com.thecowking.shaftdriller.ShaftDriller;
import com.thecowking.shaftdriller.blocks.drill.Drill;
import com.thecowking.shaftdriller.blocks.drill.DrillControllerBlock;
import com.thecowking.shaftdriller.blocks.drill.DrillFrameBlock;
import com.thecowking.shaftdriller.blocks.drill.DrillFrameTile;
import com.thecowking.shaftdriller.setup.Registration;
import com.thecowking.shaftdriller.setup.Singleton;
import com.thecowking.shaftdriller.tools.CustomEnergyStorage;
import com.thecowking.shaftdriller.tools.CustomFluidStorage;
import com.thecowking.shaftdriller.tools.SDFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiBlockControllerTile extends TileEntity implements IMultiBlockControllerTile {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_MULTIBLOCK_SIZE = 64;
    private final int NUMSLOTS = 8;

    private static final String NBT_CORNERX = "cornerX";
    private static final String NBT_CORNERZ = "cornerZ";

    private int tankSize = 1000;

    private List<DrillFrameTile> multiBlockTracker;
    private BlockPos next;
    private boolean stopFlag = false;

    //holds the "lowsest" value corner of the multi block
    protected int cornerX;
    protected int cornerZ;

    /*

     */

    private int lengthOffsetEnergyIn = -1*Drill.DRILL_SIZE/2;
    private int widthOffsetEnergyIn = 0;
    private int lengthOffsetItemOut = -1*Drill.DRILL_SIZE/2;;
    private int widthOffsetItemOut = Drill.DRILL_SIZE/2;
    private int lengthOffsetRedstoneIn = Drill.DRILL_SIZE/2;
    private int widthOffsetRedstoneIn = 0;
    private int lengthOffsetRedstoneOut = Drill.DRILL_SIZE/2;
    private int widthOffsetRedstoneOut = Drill.DRILL_SIZE/2;;
    private int lengthOffsetFluidIn = -1*Drill.DRILL_SIZE/2;
    private int widthOffsetFluidIn = Drill.DRILL_SIZE-1;
    private int lengthOffsetFluidOut = 0;
    private int widthOffsetFluidOut = Drill.DRILL_SIZE-1;


    private BlockPos redstoneInputBlock;
    private BlockPos redstoneOutputBlock;
    private BlockPos energyInputBlock;
    private BlockPos fluidInputBlock;
    private BlockPos fluidOutputBlock;
    private BlockPos itemInputBlock;


    private ItemStackHandler itemHandler = createHandler();
    protected CustomEnergyStorage energyStorage = createEnergy();

    public LazyOptional<CustomFluidStorage> fluidTank = LazyOptional.of(this::createTank);
    public LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    public LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);


    public MultiBlockControllerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public int getEnergyLevel()  {return energyStorage.getEnergyStored();}
    public IItemHandler getItemHandler()  {return itemHandler;}

    public boolean isFormed(World worldIn)  {return worldIn.getBlockState(pos).get(Drill.FORMED);}
    public Direction getDirectionFacing(World worldIn)  {return worldIn.getBlockState(pos).get(BlockStateProperties.FACING);}
    public void setFormed(World worldIn, boolean b)  {worldIn.setBlockState(pos, getBlockState().with(Drill.FORMED, b));}

    /*
    controls forming of multi block. calls cleanup methods if failure
 */
    public void tryToFormMultiBlock(World worldIn, BlockPos pos) {
        if(formMultiBlock())  {
            createMultiBlock();
            setFormed(worldIn, true);

            LOGGER.info("assign energy job");
            assignJob(getDrillFrameTile(getEnergyInputBlockPos()), Drill.JOB_ENERGY_IN);
            LOGGER.info("assign fluid in job");
            assignJob(getDrillFrameTile(getFluidInputBlockPos()), Drill.JOB_FLUID_IN);
            LOGGER.info("assign fluid out job");
            assignJob(getDrillFrameTile(getFluidOutputBlockPos()), Drill.JOB_FLUID_OUT);
            LOGGER.info("assign item in job");
            assignJob(getDrillFrameTile(getItemInputBlockPos()), Drill.JOB_ITEM_IN);

        }  else  {
            cleanUpFrame();
        }
    }

    public DrillFrameTile getDrillFrameTile(BlockPos posIn)  {
        LOGGER.info("checking pos " + posIn);
        TileEntity te = world.getTileEntity(posIn);
        if(te != null)  {
            if( te instanceof DrillFrameTile)  {
                return (DrillFrameTile) te;
            }  else  {
                LOGGER.info("getDrillFrameTile got a non DrillFrameTile at" + te.getPos());
            }
        }  else {
          LOGGER.info("getDrillFrameTile got a non entity pos");
        }
        return null;
    }

    public void assignJob(DrillFrameTile dFT, String job)  {
        if(dFT == null)  {
            LOGGER.info("DrillFrameTile gotten is null!");
        }  else  {
            LOGGER.info("Block " + dFT.getBlockPos() + " has been assigned " + job);
            dFT.setJob(job);
        }
    }


    /*
      used to attempt to form a multi block structure
     */

    public boolean formMultiBlock()  {
        if(world.isRemote)  {return false;}

        // check that we have the proper materials to construct
        if(itemHandler.getStackInSlot(0).isEmpty())  {
            LOGGER.info("stack is empty");
            return false;
        }

        if(!(Block.getBlockFromItem(itemHandler.getStackInSlot(0).getItem()) instanceof DrillFrameBlock))  {
            LOGGER.info("not the right block");
            return false;
        }
        if(itemHandler.getStackInSlot(0).getCount() < 43 )  {
            LOGGER.info("too low of a count");
            return false;
        }

        BlockPos startPos = findStartPosition();
        LOGGER.info(startPos);

        // Check for blocks sitting in 5x5x5 cube in and around controller.
        // as this starts from the NorthWestern corner and is a cube, all we need to do
        // is increment XZ values to get the entire cube
        for(int i = 0; i < 5; i++)  {
            for(int j = 0; j < 5; j++)  {
                for(int k = 0; k < 5; k++)  {
                    BlockPos currentPos = new BlockPos(startPos.getX()+i, startPos.getY()+j, startPos.getZ()+k);
                    Block currentBlock = world.getBlockState(currentPos).getBlock();
                    BlockState currentState = world.getBlockState(currentPos);
                    if( currentState.hasTileEntity() || !(currentState.isAir(world, currentPos))) {
                        LOGGER.info("is not air");
                        LOGGER.info(currentBlock);
                        if(!(isCorrectFrameBlocK(currentPos)))  {
                            LOGGER.info("is not frame");
                            if(!(currentPos.equals(this.pos)))  {
                                LOGGER.info("Incorrect Block at");
                                LOGGER.info(currentPos);
                                LOGGER.info(pos);
                                return false;
                            }
                        }
                    }
                }
            }
        }

        multiBlockTracker = new ArrayList<>();
        // If we are here then we know there is nothing but air+controller in the 5x5x5 cube
        // build frame
        for(int y = 0; y < 5; y++)  {
            for(int x = 0; x < 5; x++)  {
                for(int z = 0; z < 5; z++)  {
                    if(((z == 0 || x == 0 || z==4 || x == 4)) && (y == 0 || y == 4)  ||
                            (z==0 && x==0) || (z==4 && x==4) || (z == 0 && x == 4) || (z == 4 && x == 0))  {
                        BlockPos currentPos = new BlockPos(startPos.getX()+x, startPos.getY()+y, startPos.getZ()+z);
                        if(world.getBlockState(currentPos).getBlock() == Blocks.AIR)  {
                            SDFakePlayer fakePlayer = (SDFakePlayer) Singleton.getFakePlayer(world, currentPos);
                            fakePlayer.placeBlock(world, currentPos, itemHandler.getStackInSlot(0));
                            TileEntity te = world.getTileEntity(currentPos);
                            if(te != null)  {
                                if(te instanceof DrillFrameTile)  {
                                    multiBlockTracker.add((DrillFrameTile) te);
                                }  else  {
                                    LOGGER.info("TE WAS NOT FRAME");
                                }
                            }  else  {
                                LOGGER.info("TE WAS NULL");
                            }

                        }
                    }
                }
            }
        }
        return true;
    }


    public boolean isCorrectFrameBlocK (BlockPos currentPos)  {
        return (world.getBlockState(currentPos).getBlock() instanceof DrillFrameBlock);
    }

    /*
      West = -x
      East = +X
      North = -Z
      South = +Z
      this function will return the North-Western corner of the multi block to be formed
     */

    public BlockPos findStartPosition()  {
        Direction facing = getDirectionFacing(world);
        BlockPos start = null;
        if(facing == Direction.NORTH)  {
            start = new BlockPos(pos.getX()-(Drill.DRILL_SIZE/2), pos.getY(), pos.getZ());
        } else if(facing == Direction.SOUTH)  {
            start = new BlockPos(pos.getX()-(Drill.DRILL_SIZE/2), pos.getY(), pos.getZ()-(Drill.DRILL_SIZE)+1);
        } else if(facing == Direction.WEST)  {
            start = new BlockPos(pos.getX(), pos.getY(), pos.getZ()-(Drill.DRILL_SIZE/2));
        } else if(facing == Direction.EAST)  {
            start = new BlockPos(pos.getX()-(Drill.DRILL_SIZE)+1, pos.getY(), pos.getZ()-(Drill.DRILL_SIZE/2));
        }  else  {
            LOGGER.info("findStartPosition got a null direction!");
            return null;
        }
        cornerX = start.getX();
        cornerZ = start.getZ();
        return start;
    }

    @Override
    public void openGUI(World worldIn, BlockPos pos, PlayerEntity player, IMultiBlockControllerTile tileEntity) {

    }
        /*
        Attempts to form the multi-block starting from the controller.
        Will check controllers neighbors and jump into a line from there.
        After if comes to the end of the line it will then look at neighbors for the next
        frame and run down that line as well and so on until we "turn" four times
     */
    private void cleanUpFrame()  {
        if(multiBlockTracker != null)  {
            for(int i = 0; i < multiBlockTracker.size(); i++)  {
                DrillFrameTile current = multiBlockTracker.get(i);
                current.destroyMultiBlock();
            }
        }
        multiBlockTracker = null;
    }

    private void createMultiBlock()  {
        if(multiBlockTracker != null)  {
            for(int i = 0; i < multiBlockTracker.size(); i++)  {
                DrillFrameTile current = multiBlockTracker.get(i);
                current.setupMultiBlock(pos);
            }
        }
        multiBlockTracker = null;
    }

    /*
        returns if the held item used to right click on controller is valid to form multiblock
     */
    public boolean isValidMultiBlockFormer(Item item)  {
        LOGGER.info("isValidMultiBlockFormer");
        if(item == Items.STICK)  {
            return true;
        }
        return false;
    }

    /*
        use neighbors to destroy multiblock
     */
    public void breakMultiBlock(World worldIn)  {
        if(!worldIn.isRemote)  {
            return;
        }
        LOGGER.info("breakMultiBlock");

        if(! (isFormed(worldIn)) )  {
            return;
        }
        setFormed(worldIn, false);

        BlockPos startPos = findStartPosition();
        for(int y = 0; y < 5; y++)  {
            for(int x = 0; x < 5; x++)  {
                for(int z = 0; z < 5; z++)  {
                    if(((z == 0 || x == 0 || z==4 || x == 4)) && (y == 0 || y == 4)  ||
                            (z==0 && x==0) || (z==4 && x==4) || (z == 0 && x == 4) || (z == 4 && x == 0))  {
                        BlockPos currentPos = new BlockPos(startPos.getX()+x, startPos.getY()+y, startPos.getZ()+z);
                        TileEntity te = world.getTileEntity(currentPos);
                        if(te != null && te instanceof DrillFrameTile)  {
                            DrillFrameTile drillFrameTile = (DrillFrameTile) te;
                            drillFrameTile.destroyMultiBlock();
                        }
                    }
                }
            }
        }
    }

    /*
        insert mined blocks into container
     */
    public List<ItemStack> insertListStack(List<ItemStack> items)  {
        List<ItemStack> leftovers = new ArrayList<>();
        for(int i = 0; i < items.size(); i++)  {
            ItemStack item = items.get(i);
            // insertItem returns what it could not insert
            for(int j = 0; j < NUMSLOTS && item != null && !item.isEmpty(); j++)  {
                item = getItemHandler().insertItem(j, item, false);
            }
            if(item != null && !item.isEmpty())  {
                stopFlag = true;
                leftovers.add(item);
            }
        }
        markDirty();
        return leftovers;
    }

    @Override
    public void read(CompoundNBT tag) {
        itemHandler.deserializeNBT(tag.getCompound("inv"));
        energyStorage.deserializeNBT(tag.getCompound("energy"));
        cornerX = tag.getInt(NBT_CORNERX);
        cornerZ = tag.getInt(NBT_CORNERZ);
        super.read(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        tag.put("inv", itemHandler.serializeNBT());
        tag.put("energy", energyStorage.serializeNBT());
        tag.putInt(NBT_CORNERX, cornerX);
        tag.putInt(NBT_CORNERZ, cornerZ);
        return tag;
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(64) {

            @Override
            protected void onContentsChanged(int slot) {
                // To make sure the TE persists when the chunk is saved later we need to
                // mark it dirty every time the item handler changes
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    protected CustomFluidStorage createTank() {
        return new CustomFluidStorage(tankSize, this);
    }


    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(10000, 1000) {
            @Override
            protected void onEnergyChanged() {
                markDirty();
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
           return handler.cast();
        }
        //if (cap.equals(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
        //    return this.fluidTank.cast();

        //}
        //if (cap.equals(CapabilityEnergy.ENERGY)) {
        //    return energy.cast();
       // }
        return super.getCapability(cap, side);
    }

    public BlockPos getItemInputBlockPos()  {
        if(itemInputBlock == null)  {
            itemInputBlock = getBlockWithOffset(lengthOffsetItemOut, widthOffsetItemOut);
        }
        return itemInputBlock;
    }

    public BlockPos getRedstoneInputBlockPos()  {
        if(redstoneInputBlock == null)  {
            redstoneInputBlock = getBlockWithOffset(lengthOffsetRedstoneIn, widthOffsetRedstoneIn);
        }
        return redstoneInputBlock;
    }

    public BlockPos getRedstoneOutputBlockPos()  {
        if(redstoneOutputBlock == null)  {
            redstoneOutputBlock = getBlockWithOffset(lengthOffsetRedstoneOut, widthOffsetRedstoneOut);
        }
        return redstoneOutputBlock;
    }

    public BlockPos getFluidInputBlockPos()  {
        if(fluidInputBlock == null)  {
            fluidInputBlock = getBlockWithOffset(lengthOffsetFluidIn, widthOffsetFluidIn);
        }
        return fluidInputBlock;
    }

    public BlockPos getFluidOutputBlockPos()  {
        if(fluidOutputBlock == null)  {
            fluidOutputBlock = getBlockWithOffset(lengthOffsetFluidOut, widthOffsetFluidOut);
        }
        return fluidOutputBlock;
    }

    public BlockPos getEnergyInputBlockPos()  {
        if(energyInputBlock == null)  {
            energyInputBlock = getBlockWithOffset(lengthOffsetEnergyIn, widthOffsetEnergyIn);
        }
        return energyInputBlock;
    }

    public BlockPos getBlockWithOffset(int length, int width)  {
        int x = this.pos.getX();
        int z = this.pos.getZ();
        Direction facing = getDirectionFacing(world);

        if(Direction.NORTH == facing) {
            x += length;
            z += width;
        }  else if(Direction.SOUTH == facing)  {
            x += length;
            z -= width;
        }  else if(Direction.WEST == facing)  {
            x += width;
            z += length;
        }  else if(Direction.EAST == facing)  {
            x -= width;
            z += length;
        }  else  {
            // somehow go a bad direction
            LOGGER.info("getRedstoneOutBlockPos got a bad direction");
            return null;
        }
        return new BlockPos(x, pos.getY(), z);
    }

    public void setDirty(boolean b)  {
        if(b)  {
            markDirty();
        }
    }





}
