package com.thecowking.shaftdriller.blocks.drill;


import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.thecowking.shaftdriller.setup.Registration.DRILL_FRAME_TILE;

public class DrillFrameTile extends TileEntity  {
    private static final Logger LOGGER = LogManager.getLogger();



    private static String NBT_CX = "CX";
    private static String NBT_CY = "CY";
    private static String NBT_CZ = "CZ";
    private static String NBT_JOB = "JOB";

    private BlockPos controllerPos;
    private String job;

    public DrillFrameTile() {
        super(DRILL_FRAME_TILE.get());
    }
    public BlockPos getController()  {return controllerPos;}
    public void setController(BlockPos pos)  {controllerPos = pos;}

    public DrillControllerTile getControllerTile()  {
        if(controllerPos != null) {
            TileEntity te = this.world.getTileEntity(controllerPos);
            if (te instanceof DrillControllerTile) {
                DrillControllerTile drillControllerTile = (DrillControllerTile) te;
                return drillControllerTile;
            }
        }
        return null;
    }

    public void setupMultiBlock(BlockPos posIn)  {
        world.setBlockState(this.pos, this.getBlockState().with(DrillFrameBlock.FORMED, true));
        setController(posIn);
    }
    public void destroyMultiBlock()  {
        if(world.isRemote)  {
            return;
        }
        clearNBT();
        setFormed(world, false);
    }
    public void setFormed(World worldIn, boolean b)  {worldIn.setBlockState(this.pos, this.getBlockState().with(DrillFrameBlock.FORMED, b));}

    public void setJob(String job)  {
        this.job = job;
    }

    public void clearNBT()  {
        controllerPos = null;
        job = null;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if(tag.contains(NBT_CX))  {
            controllerPos = new BlockPos(tag.getInt(NBT_CX), tag.getInt(NBT_CY), tag.getInt(NBT_CZ));
        }
        if (tag.contains(NBT_JOB)) {
            job = tag.getString(NBT_JOB);
        }
    }

    public BlockPos getBlockPos()  {return pos;}



    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        if(controllerPos != null)  {
            tag.putInt(NBT_CX, controllerPos.getX());
            tag.putInt(NBT_CY, controllerPos.getY());
            tag.putInt(NBT_CZ, controllerPos.getZ());
        }
        if(job != null)  {
            tag.putString(NBT_JOB, job);
        }
        return tag;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        DrillControllerTile drillControllerTile = getControllerTile();
        if(drillControllerTile != null)  {
            if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) && job == Drill.JOB_ITEM_IN) {
                return drillControllerTile.handler.cast();
            }
            if (cap.equals(CapabilityEnergy.ENERGY) && job == Drill.JOB_ENERGY_IN) {
                return drillControllerTile.energy.cast();
            }

            if (cap.equals(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) && job == Drill.JOB_FLUID_OUT) {
                return drillControllerTile.fluidTank.cast();
            }
        }
        return super.getCapability(cap, side);
    }
}
