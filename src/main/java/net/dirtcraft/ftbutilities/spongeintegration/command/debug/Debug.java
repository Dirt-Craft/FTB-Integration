package net.dirtcraft.ftbutilities.spongeintegration.command.debug;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.world.ChunkTicketManager;

import java.util.Optional;

public class Debug implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success();
    }

    private static void test(){
        ChunkTicketManager ticketManager = Sponge.getServiceManager().provide(ChunkTicketManager.class).get();
        //ticketManager.
    }
}
