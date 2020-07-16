package com.thecowking.shaftdriller.blocks.drill;


import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.thecowking.shaftdriller.setup.Registration.DRILL_TILE;

public class DrillTile extends TileEntity  {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String NBT_CX = "CX";
    private static String NBT_CY = "CY";
    private static String NBT_CZ = "CZ";

    private BlockPos controllerPos;

    public DrillTile() {
        super(DRILL_TILE.get());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()  {return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(),
            getPos().getX()+25, getPos().getY()+25, getPos().getZ()+25);
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

    public void destroyMultiBlock()  {
        if(world.isRemote)  {
            return;
        }
        getControllerTile().breakMultiBlock(world);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if(tag.contains(NBT_CX))  {
            controllerPos = new BlockPos(tag.getInt(NBT_CX), tag.getInt(NBT_CY), tag.getInt(NBT_CZ));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        if(controllerPos != null)  {
            tag.putInt(NBT_CX, controllerPos.getX());
            tag.putInt(NBT_CY, controllerPos.getY());
            tag.putInt(NBT_CZ, controllerPos.getZ());
        }
        return tag;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        DrillControllerTile drillControllerTile = getControllerTile();
        if(drillControllerTile != null)  {
            if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
                return drillControllerTile.handler.cast();
            }
            if (cap.equals(CapabilityEnergy.ENERGY)) {
                return drillControllerTile.energy.cast();
            }
        }
        return super.getCapability(cap, side);
    }
}
