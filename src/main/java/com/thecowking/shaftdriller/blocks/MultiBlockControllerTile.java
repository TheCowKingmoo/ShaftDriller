package com.thecowking.shaftdriller.blocks;

import com.thecowking.shaftdriller.blocks.drill.Drill;
import com.thecowking.shaftdriller.blocks.drill.DrillFrameBlockBlock;
import com.thecowking.shaftdriller.blocks.drill.DrillFrameTile;
import com.thecowking.shaftdriller.tools.CustomEnergyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.antlr.v4.runtime.misc.Pair;
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

    private ItemStackHandler itemHandler = createHandler();
    protected CustomEnergyStorage energyStorage = createEnergy();

    private List<DrillFrameTile> multiBlockTracker;
    private BlockPos next;
    private boolean stopFlag = false;

    //holds the "highest" value corner of the multi block
    protected int cornerX;
    protected int cornerZ;

    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    public LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    public LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    public MultiBlockControllerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public int getEnergyLevel()  {return energyStorage.getEnergyStored();}
    public IItemHandler getItemHandler()  {return itemHandler;}

    public boolean isFormed(World worldIn)  {return worldIn.getBlockState(pos).get(Drill.FORMED);}
    public void setFormed(World worldIn, boolean b)  {worldIn.setBlockState(pos, getBlockState().with(Drill.FORMED, b));}

    /*
    controls forming of multi block. calls cleanup methods if failure
 */
    public void tryToFormMultiBlock(World worldIn, BlockPos pos) {
        boolean complted = formMultiBlock(worldIn, pos);
        if(!complted)  {
            cleanUpFrame();
        }  else  {
            formFrame();
        }
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

    public boolean formMultiBlock(World worldIn, BlockPos pos) {
        if (worldIn.isRemote) {
            return false;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos neighbor1 = new BlockPos(x + 1, y, z);
        BlockPos neighbor2 = new BlockPos(x - 1, y, z);
        BlockPos neighbor3 = new BlockPos(x, y, z + 1);
        BlockPos neighbor4 = new BlockPos(x, y, z - 1);

        // sued to keep track of what block we are on
        BlockPos current = null;
        multiBlockTracker = new ArrayList<>();


        // used to indicate what direction to move
        // 1 = +x, 2 = -x, 3 = +z, 4 = -z
        int move = 0;

        // chain to find where to start
        if(worldIn.getBlockState(neighbor1).getBlock() instanceof DrillFrameBlockBlock) {
            current = neighbor1;
            move = 1;
        }  else if(worldIn.getBlockState(neighbor2).getBlock() instanceof DrillFrameBlockBlock) {
            current = neighbor2;
            move = 2;
        }  else if(worldIn.getBlockState(neighbor3).getBlock() instanceof DrillFrameBlockBlock) {
            current = neighbor3;
            move = 3;
        }  else if(worldIn.getBlockState(neighbor4).getBlock() instanceof DrillFrameBlockBlock) {
            current = neighbor4;
            move = 4;
        }  else  {
            // cannot find a frame block next to controller
            LOGGER.info("first bit fail");
            return false;
        }

        next = current;

        // run down the direction until we do not hit a frame block
        Pair<BlockPos, Integer> pair = moveUpLength(worldIn, current, move, false);
        current = pair.a;
        // mark down the first corner of the multi block
        int currentX = current.getX();
        int currentZ = current.getZ();

        if(pair.b == -1)  {
            // move up passed an error
            LOGGER.info("first move up fail");
            return false;
        }

        // we started from controller so we need to add second half + size of controller
        int length = 2 * pair.b + 1;

        // loop to find the remaining sides
        for(int i = 0; i < 3; i++)  {
            LOGGER.info("loop turn + moveup");
            LOGGER.info(i);

            move = turnDirection(worldIn, current, move, length);
            if(move == -1)  {
                LOGGER.info("turn couldn't find another block ");
                return false;
            }
            pair = moveUpLength(worldIn, current, move, false);
            current = pair.a;
            int checkLength = pair.b;

            if(length != checkLength)  {
                // the length calc'd earlier does not match this sides length
                LOGGER.info("length was incorrect");
                LOGGER.info(checkLength);
                LOGGER.info(length);
                return false;
            }

            //find largest corner value to calc where to start mining
            if(currentX <= current.getX() && currentZ <= current.getZ())  {
                currentX = current.getX();
                currentZ = current.getZ();
            }

        }

        move = turnDirection(worldIn, current, move, length);
        pair = moveUpLength(worldIn, current, move, false);
        int checkLength = pair.b;

        if(checkLength == (length - 1) / 2)  {
            cornerX = currentX;
            cornerZ = currentZ;
            setFormed(worldIn, true);
            LOGGER.info("multiblock formed");
            return true;
        }  else  {
            //last leg failed
            return false;
        }
    }

    private void cleanUpFrame()  {
        if(multiBlockTracker != null)  {
            for(int i = 0; i < multiBlockTracker.size(); i++)  {
                DrillFrameTile current = multiBlockTracker.get(i);
                current.destroyMultiBlock();
            }
        }
        multiBlockTracker = null;
    }

    private void formFrame()  {
        if(multiBlockTracker != null)  {
            for(int i = 0; i < multiBlockTracker.size(); i++)  {
                DrillFrameTile current = multiBlockTracker.get(i);
                current.setupMultiBlock(pos);
            }
        }
        multiBlockTracker = null;
    }

    // checks to make sure the sides are not equal to frame block
    private boolean checkSides(World worldIn, BlockPos current, int move)  {
        BlockPos findDirection = null;
        if(move == 1 || move == 2)  {
            findDirection = new BlockPos(current.getX(), current.getY(), current.getZ()+1);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return false;
            }
            findDirection = new BlockPos(current.getX(), current.getY(), current.getZ()-1);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return false;
            }
        }  else if(move ==3 || move ==4) {
            findDirection = new BlockPos(current.getX()+1, current.getY(), current.getZ());
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return false;
            }
            findDirection = new BlockPos(current.getX()-1, current.getY(), current.getZ());
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return false;
            }
        }
        return true;
    }


    /*
        When we wish to go up a new side we use this function to find out what direction to head.
        This will simply check out a passed in blockpos's neighbors and return that into move
     */
    private int turnDirection(World worldIn, BlockPos current, int move, int length)  {
        LOGGER.info(move);
        BlockPos findDirection = null;
        // go along the z axis as we know the turn has to be on the x if we come from z
        if(move == 1 || move == 2)  {
            findDirection = new BlockPos(current.getX(), current.getY(), current.getZ()+1);
            LOGGER.info(findDirection);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return 3;
            }
            findDirection = new BlockPos(current.getX(), current.getY(), current.getZ()-1);
            LOGGER.info(findDirection);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return 4;
            }
            // go along the x axis
        }  else if(move ==3 || move ==4) {
            findDirection = new BlockPos(current.getX()+1, current.getY(), current.getZ());
            LOGGER.info(findDirection);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return 1;
            }
            findDirection = new BlockPos(current.getX()-1, current.getY(), current.getZ());
            LOGGER.info(findDirection);
            if(worldIn.getBlockState(findDirection).getBlock() instanceof DrillFrameBlockBlock)  {
                return 2;
            }
        }
        return -1;
    }

    /*
        Travels in a line until the next element is not a frame block or we hit max size.
        return will be the last block in the line and the length of the side
     */
    private Pair<BlockPos, Integer> moveUpLength(World worldIn, BlockPos current, int move, boolean lastLeg)  {
        int sizeLength = 0;
        int sideCount = 0;
        BlockPos old = current;
        while(worldIn.getBlockState(current).getBlock() instanceof DrillFrameBlockBlock && sizeLength < MAX_MULTIBLOCK_SIZE)  {
            // set the neighbor of the block

            old = current;

            switch (move)  {
                case 1:
                    current = new BlockPos(current.getX()+1, current.getY(), current.getZ());
                    break;
                case 2:
                    current = new BlockPos(current.getX()-1, current.getY(), current.getZ());
                    break;
                case 3:
                    current = new BlockPos(current.getX(), current.getY(), current.getZ()+1);
                    break;
                case 4:
                    current = new BlockPos(current.getX(), current.getY(), current.getZ()-1);
                    break;
                default:
                    if(lastLeg)  {
                        return new Pair<BlockPos, Integer>(current, move);
                    }
                    return new Pair<BlockPos, Integer>(current, -1);
            }
            // check if there are no frame blocks along the line
            if(sizeLength != 0)  {
                if(!(checkSides(worldIn, current, move)))  {
                    sideCount++;
                }
            }
            DrillFrameTile oldTile = ((DrillFrameTile)worldIn.getTileEntity(old));
            multiBlockTracker.add(oldTile);
            oldTile.setNext(current);
            sizeLength++;
        }
        // check if we found an extra frame block along one of the sides
        if(sideCount > 1)  {
            return new Pair<BlockPos, Integer>(old, -1);
        }
        return new Pair<BlockPos, Integer>(old, sizeLength);
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
        BlockPos current = next;
        DrillFrameTile currentFrame = ((DrillFrameTile)worldIn.getTileEntity(current));
        while(currentFrame != null)  {
            current = currentFrame.getNext();
            currentFrame.destroyMultiBlock();
            currentFrame = ((DrillFrameTile)worldIn.getTileEntity(current));
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
        if (cap.equals(CapabilityEnergy.ENERGY)) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }





}
