package com.thecowking.shaftdriller.setup;

import com.thecowking.shaftdriller.blocks.drill.*;
import com.thecowking.shaftdriller.blocks.firstblock.FirstBlock;
import com.thecowking.shaftdriller.blocks.firstblock.FirstBlockContainer;
import com.thecowking.shaftdriller.blocks.firstblock.FirstBlockTile;
import com.thecowking.shaftdriller.blocks.miner.MinerBlock;
import com.thecowking.shaftdriller.blocks.miner.MinerBlockContainer;
import com.thecowking.shaftdriller.blocks.miner.MinerBlockTile;
import com.thecowking.shaftdriller.items.FirstItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.thecowking.shaftdriller.ShaftDriller.MODID;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    private static final DeferredRegister<ModDimension> DIMENSIONS = DeferredRegister.create(ForgeRegistries.MOD_DIMENSIONS, MODID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIMENSIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // Generator Block
    public static final RegistryObject<FirstBlock> FIRSTBLOCK = BLOCKS.register("firstblock", FirstBlock::new);
    public static final RegistryObject<Item> FIRSTBLOCK_ITEM = ITEMS.register("firstblock", () -> new BlockItem(FIRSTBLOCK.get(), new Item.Properties().group(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<TileEntityType<FirstBlockTile>> FIRSTBLOCK_TILE = TILES.register("firstblock", () -> TileEntityType.Builder.create(FirstBlockTile::new, FIRSTBLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<FirstBlockContainer>> FIRSTBLOCK_CONTAINER = CONTAINERS.register("firstblock", () -> IForgeContainerType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getEntityWorld();
        return new FirstBlockContainer(windowId, world, pos, inv, inv.player);
    }));

    // Miner Block
    public static final RegistryObject<MinerBlock> MINER_BLOCK = BLOCKS.register("miner_block", MinerBlock::new);
    public static final RegistryObject<Item> MINER_BLOCK_ITEM = ITEMS.register("miner_block", () -> new BlockItem(MINER_BLOCK.get(), new Item.Properties().group(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<TileEntityType<MinerBlockTile>> MINER_BLOCK_TILE = TILES.register("miner_block", () -> TileEntityType.Builder.create(MinerBlockTile::new, MINER_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<MinerBlockContainer>> MINER_BLOCK_CONTAINER = CONTAINERS.register("miner_block", () -> IForgeContainerType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getEntityWorld();
        return new MinerBlockContainer(windowId, world, pos, inv, inv.player);
    }));

    //Drill Core
    public static final RegistryObject<DrillControllerBlock> DRILL_CORE_BLOCK = BLOCKS.register("drill_core_block", DrillControllerBlock::new);
    public static final RegistryObject<Item> DRILL_CORE_ITEM = ITEMS.register("drill_core_block", () -> new BlockItem(DRILL_CORE_BLOCK.get(), new Item.Properties().group(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<TileEntityType<DrillControllerTile>> DRILL_CORE_TILE = TILES.register("drill_core_block", () -> TileEntityType.Builder.create(DrillControllerTile::new, DRILL_CORE_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<DrillContainer>> DRILL_CONTAINER = CONTAINERS.register("drill_core_block", () -> IForgeContainerType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getEntityWorld();
        return new DrillContainer(windowId, world, pos, inv, inv.player);
    }));


    //Drill Frame
    public static final RegistryObject<DrillFrameBlock> DRILL_FRAME_BLOCK = BLOCKS.register("drill_frame_block", DrillFrameBlock::new);
    public static final RegistryObject<Item> DRILL_FRAME_ITEM = ITEMS.register("drill_frame_block", () -> new BlockItem(DRILL_FRAME_BLOCK.get(), new Item.Properties().group(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<TileEntityType<DrillFrameTile>> DRILL_FRAME_TILE = TILES.register("drill_frame_block", () -> TileEntityType.Builder.create(DrillFrameTile::new, DRILL_FRAME_BLOCK.get()).build(null));


    public static final RegistryObject<FirstItem> FIRSTITEM = ITEMS.register("firstitem", FirstItem::new);



}
