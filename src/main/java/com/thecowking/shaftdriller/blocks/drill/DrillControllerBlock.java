package com.thecowking.shaftdriller.blocks.drill;

import com.thecowking.shaftdriller.blocks.MultiBlockControllerBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public class DrillControllerBlock extends MultiBlockControllerBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    // tells us if we have a multi-block formed



    public DrillControllerBlock() {
        super();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> list, ITooltipFlag flags) {
        list.add(new TranslationTextComponent("message.drill_core_block"));
    }

}
