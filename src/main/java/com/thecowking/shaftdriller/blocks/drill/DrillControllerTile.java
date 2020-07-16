package com.thecowking.shaftdriller.blocks.drill;

import com.thecowking.shaftdriller.blocks.IMultiBlockControllerTile;
import com.thecowking.shaftdriller.blocks.MultiBlockControllerTile;
import com.thecowking.shaftdriller.tools.MinerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

import static com.thecowking.shaftdriller.blocks.MultiBlockFrameBlock.REDSTONE;
import static com.thecowking.shaftdriller.setup.Registration.DRILL_CORE_TILE;

public class DrillControllerTile extends MultiBlockControllerTile implements ITickableTileEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String NBT_YMINED = "YLevelMined";
    private static String NBT_YCURRENT = "YCurrentLevel";
    private static String NBT_STUFFTOMINE = "stuffToMine";


    private BlockPos redstoneInputBlock;
    private BlockPos redstoneOutputBlock;
    private BlockPos energyInputBlock;
    private BlockPos fluidInputBlock;
    private BlockPos fluidOutputBlock;

    // if contoller is north then length = z axis
    // and width = x axis
    // signs are taken care of by method this is just absolute distance from controller

    private int lengthOffsetEnergyIn = 0;
    private int widthOffsetEnergyIn = 0;
    private int lengthOffsetItemOut = 0;
    private int widthOffsetItemOut = 0;
    private int lengthOffsetRedstoneIn = Drill.DRILL_SIZE/2;
    private int widthOffsetRedstoneIn = 0;
    private int lengthOffsetRedstoneOut = Drill.DRILL_SIZE/2;
    private int widthOffsetRedstoneOut = Drill.DRILL_SIZE/2;;
    private int lengthOffsetFluidIn = 0;
    private int widthOffsetFluidIn = 0;
    private int lengthOffsetFluidOut = 0;
    private int widthOffsetFluidOut = 0;

    private int yLevelMined = 1; //start at one to make sure we do not mine ourself
    private int yCurrentLevel = 0;
    private boolean stuffToMine = true;

    //how many blocks in a circle around center
    public static int SIZE_OF_DRILL = 1;

    //TODO - del constants
    private int energyPerMine = 1000;
    private int energyPerNoMine = 100;
    private int opsPerTick = 4;

    private int floor = 0;
    private boolean running = false;

    private boolean itemFullFlag = false;
    private boolean lowPowerFlag = false;
    private boolean powered = false;
    private boolean firstredstoneFlag = false;
    private boolean redstoneFlag = false;

    private int counter = 0;
    private List<ItemStack> backlog;  // a list of items that the miner could not insert into storage

    public DrillControllerTile() {
        super(DRILL_CORE_TILE.get());
        backlog = new ArrayList<>();
    }



    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }
        if(!isFormed(world))  {
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

        //Redstone Check
        BlockPos redstoneBlock = getRedstoneInputBlock();
        LOGGER.info(redstoneBlock);
        powered = world.isBlockPowered(redstoneBlock);

        if(!powered)  {
            turnOff();
            redstoneFlag = false;
            return;
        }  else if(!firstredstoneFlag)  {
            redstoneFlag = true;
            firstredstoneFlag = true;
        }  else if(!redstoneFlag)  {
            redstoneFlag = true;
            yLevelMined = 1;
        }

        if(backlog.isEmpty())  {
            itemFullFlag = false;
        }

        // check if we have enough power to move on
        if(getEnergyLevel() < energyPerMine)  {
            lowPowerFlag = true;
            turnOff();
        }  else  {
            lowPowerFlag = false;
        }

        if(itemFullFlag || lowPowerFlag || !redstoneFlag)  {
            return;
        }

        mine(stuffToMine);

    }

    /*
      turns the drill on
     */
    private void turnOn()  {
        running = true;
        world.setBlockState(this.pos, world.getBlockState(this.pos).with(BlockStateProperties.POWERED, true));
        LOGGER.info("redstone out");
        BlockPos r = getRedstoneOutputBlock();
        LOGGER.info(r);
        if(world.getBlockState(r).getBlock() instanceof DrillFrameBlock)  {
            world.setBlockState(r, world.getBlockState(r).with(REDSTONE, 15));
        }

    }
    /*
        turns the drill off
     */
    private void turnOff()  {
        if(running)  {
            world.setBlockState(this.pos, world.getBlockState(this.pos).with(BlockStateProperties.POWERED, false));
            BlockPos r = getRedstoneOutputBlock();

            if(world.getBlockState(r).getBlock() instanceof DrillFrameBlock)  {
                world.setBlockState(r, world.getBlockState(r).with(REDSTONE, 0));
            }


        }
        running = false;
    }

    /*
        used to find the next block in line to be mined
     */
    private BlockPos calcNextMineBlock()  {
        int currentY = this.pos.getY() - yLevelMined;
        int currentX = getMineStartX() + (yCurrentLevel % (2*SIZE_OF_DRILL + 1));
        int currentZ = getMineStartZ() + (yCurrentLevel / (2*SIZE_OF_DRILL + 1));
        return new BlockPos(currentX, currentY, currentZ);
    }

    private int getMineStartX()  { return cornerX+1; }
    private int getMineStartZ()  { return  cornerZ+1; }

    private List<ItemStack> mine(boolean realBlock)  {
        if(!running)  {
            turnOn();
        }

        // grab the block at the next location to mine
        BlockPos pos = calcNextMineBlock();

        LOGGER.info(pos.toString());

        // currently no silk touch options
        List<ItemStack> drops = null;
        if(realBlock)  {
            drops = MinerUtil.getDrops((ServerWorld) world, pos, false, this.pos);
        }  else  {
            drops = MinerUtil.getVoidDrops((ServerWorld) world, pos, false, this.pos);
        }

        if(drops == null || drops.size() == 0)  {
            //cannot mine this - use less energy
            energyStorage.consumeEnergy(energyPerNoMine);
        }  else  {
            if(realBlock)  {
                world.removeBlock(pos, false);
            }
            energyStorage.consumeEnergy(energyPerMine);
            drops = insertListStack(drops);
        }


        if(realBlock)  {
            yCurrentLevel++;
            if(yCurrentLevel >= 9)  {
                yCurrentLevel = 0;
                yLevelMined++;
            }

            // checks if we hit bedrock level
            if(this.pos.getY() - yLevelMined <= floor)  {
                turnOff();
                stuffToMine = false;
            }
        }
        return drops;
    }

    @Override
    public void read(CompoundNBT tag) {
        yLevelMined = tag.getInt(NBT_YMINED);
        yCurrentLevel = tag.getInt(NBT_YCURRENT);
        stuffToMine = tag.getBoolean(NBT_STUFFTOMINE);
        counter = tag.getInt("counter");
        super.read(tag);
    }

    @Override
    public void openGUI(World worldIn, BlockPos pos, PlayerEntity player, IMultiBlockControllerTile tileEntity)  {
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("screen.shaftdriller.drill_screen");
            }

            @Override
            public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new DrillContainer(i, worldIn, pos, playerInventory, playerEntity);
            }
        };
        NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, ((DrillControllerTile)tileEntity).getPos());
    }


    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);

        tag.putInt(NBT_YMINED, yLevelMined);
        tag.putInt(NBT_YCURRENT, yCurrentLevel);
        tag.putBoolean(NBT_STUFFTOMINE, stuffToMine);
        tag.putInt("counter", counter);
        return tag;
    }


    public BlockPos getRedstoneInputBlock()  {
        if(redstoneInputBlock == null)  {
            redstoneInputBlock = getBlockWithOffset(lengthOffsetRedstoneIn, widthOffsetRedstoneIn);
        }
        return redstoneInputBlock;
    }

    public BlockPos getRedstoneOutputBlock()  {
        if(redstoneOutputBlock == null)  {
            redstoneOutputBlock = getBlockWithOffset(lengthOffsetRedstoneOut, widthOffsetRedstoneOut);
        }
        return redstoneOutputBlock;
    }

    public BlockPos getFluidInputBlock()  {
        if(fluidInputBlock == null)  {
            fluidInputBlock = getBlockWithOffset(lengthOffsetFluidIn, widthOffsetFluidIn);
        }
        return fluidInputBlock;
    }

    public BlockPos getEnergyInputBlock()  {
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


}
