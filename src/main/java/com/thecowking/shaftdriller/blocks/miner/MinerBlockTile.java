package com.thecowking.shaftdriller.blocks.miner;

import com.thecowking.shaftdriller.tools.CustomEnergyStorage;
import com.thecowking.shaftdriller.tools.MinerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.thecowking.shaftdriller.setup.Registration.MINER_BLOCK_TILE;

public class MinerBlockTile extends TileEntity implements ITickableTileEntity {

    private ItemStackHandler itemHandler = createHandler();
    private CustomEnergyStorage energyStorage = createEnergy();

    //TODO - del constants
    private int numSlots = 8;
    private int energyPerOperation = 1000;
    private int opsPerTick = 4;

    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    private int floor = 0;
    private boolean running = false;
    private int blocksMined = 1; //start at one to make sure we do not mine ourself
    private boolean stopFlag = false;

    private boolean itemFullFlag = false;
    private boolean lowPowerFlag = false;
    private boolean hardStopFlag = false;
    private boolean powered = false;

    private boolean firstredstoneFlag = false;
    private boolean redstoneFlag = false;

    private int counter = 0;


    private List<ItemStack> backlog;  // a list of items that the miner could not insert into storage
    private static final Logger LOGGER = LogManager.getLogger();


    public MinerBlockTile() {
        super(MINER_BLOCK_TILE.get());
        LOGGER.info("create miner block tile");
        backlog = new ArrayList<>();

    }

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }

        if(counter < opsPerTick )  {
            counter++;
            return;
        }  else  {
            counter = 0;
        }

        // check if items are still not inserted into miner slots
        if(!backlog.isEmpty())  {
            // this will set flags and attempt to insert
            backlog = insertListStack(backlog);
        }

        //redstone check
        powered = world.isBlockPowered(this.pos);

        if(!powered)  {
            turnOff();
            redstoneFlag = false;
            return;
        }  else if(!firstredstoneFlag)  {
            redstoneFlag = true;
            firstredstoneFlag = true;
        }  else if(!redstoneFlag)  {
            redstoneFlag = true;
            hardStopFlag = false;
            blocksMined = 1;
        }

        // basically the flag to say we have finished to bedrock
        if(hardStopFlag)  {
            return;
        }




        if(backlog.isEmpty())  {
            itemFullFlag = false;
        }

        // check if we have enough power to move on
        if(energyStorage.getEnergyStored() < energyPerOperation)  {
            lowPowerFlag = true;
            turnOff();
        }  else  {
            lowPowerFlag = false;
        }

        if(itemFullFlag || lowPowerFlag || !redstoneFlag)  {
            return;
        }

        mine();

    }

    private void turnOn()  {
        running = true;
        world.setBlockState(this.pos, world.getBlockState(this.pos).with(BlockStateProperties.POWERED, true));
    }
    private void turnOff()  {
        if(running)  {
            world.setBlockState(this.pos, world.getBlockState(this.pos).with(BlockStateProperties.POWERED, false));
        }
        running = false;
    }

    private List<ItemStack> mine()  {
        energyStorage.consumeEnergy(energyPerOperation);
        if(!running)  {
            turnOn();
        }

        LOGGER.info("mine");

        int currentY = this.pos.getY() - blocksMined;

        // grab the block at the next location to mine
        BlockPos pos = new BlockPos(this.pos.getX(), currentY, this.pos.getZ());


        LOGGER.info(pos.toString());

        // currently no silk touch options
        List<ItemStack> drops = MinerUtil.getDrops((ServerWorld) world, pos, false, this.pos);
        BlockState blockState = world.getBlockState(pos);
        world.removeBlock(pos, false);
        //world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(blockState));

        drops = insertListStack(drops);
        blocksMined++;

        if(currentY-1 <= floor)  {
            turnOff();
            hardStopFlag = true;
        }

        return drops;
    }

    private List<ItemStack> insertListStack(List<ItemStack> items)  {
        List<ItemStack> leftovers = new ArrayList<>();
        for(int i = 0; i < items.size(); i++)  {
            ItemStack item = items.get(i);
            // insertItem returns what it could not insert
            for(int j = 0; j < numSlots && item != null && !item.isEmpty(); j++)  {
                item = itemHandler.insertItem(j, item, false);
            }
            if(item != null && !item.isEmpty())  {
                stopFlag = true;
                leftovers.add(item);
            }
        }
        markDirty();
        return leftovers;
    }


    // used whenever data is sync'd on disk

    @Override
    public void read(CompoundNBT tag) {
        //TileEntity te = world.getTileEntity(pos);
        //if(te != null)  {
        //    LazyOptional<IEnergyStorage> capability = te.getCapability(CapabilityEnergy.ENERGY);
        //    capability.ifPresent(handler -> handler.receiveEnergy(10, false));
        //    Integer energy = capability.map(handler -> handler.getEnergyStored()).orElse(0);
       // }

        itemHandler.deserializeNBT(tag.getCompound("inv"));
        energyStorage.deserializeNBT(tag.getCompound("energy"));

        super.read(tag);
    }

    // only called when chunk is marked dirty
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("inv", itemHandler.serializeNBT());
        tag.put("energy", energyStorage.serializeNBT());
        return super.write(tag);
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
