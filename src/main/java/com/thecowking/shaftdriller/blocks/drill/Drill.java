package com.thecowking.shaftdriller.blocks.drill;

import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class Drill  {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final int DRILL_SIZE = 5;
    public final VoxelShape DRILL_VOXEL = VoxelShapes.create(0, 0, 0, 25, 25, 25);
    public static final String JOB_ENERGY_IN = "E_IN";
    public static final String JOB_REDSTONE_IN = "R_IN";
    public static final String JOB_REDSTONE_OUT = "R_OUT";
    public static final String JOB_ITEM_IN = "I_IN";
    public static final String JOB_FLUID_IN = "F_IN";
    public static final String JOB_FLUID_OUT = "F_OUT";



}
