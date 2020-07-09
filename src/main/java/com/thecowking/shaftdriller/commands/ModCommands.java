package com.thecowking.shaftdriller.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdTut = dispatcher.register(
                Commands.literal(com.thecowking.shaftdriller.ShaftDriller.MODID)
                        .then(CommandTest.register(dispatcher))
        );
        dispatcher.register(Commands.literal("tut").redirect(cmdTut));
    }

}
