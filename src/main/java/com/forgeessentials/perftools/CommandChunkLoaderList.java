package com.forgeessentials.perftools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.Ticket;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import com.forgeessentials.core.commands.BaseCommand;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.google.common.collect.HashMultimap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandChunkLoaderList extends BaseCommand
{
    public CommandChunkLoaderList(String name, int permissionLevel, boolean enabled)
    {
        super(name, permissionLevel, enabled);
    }

    @Override
    public String getPermissionNode()
    {
        return "fe.perftools.chunkloaderlist";
    }

    @Override
    public String getPrimaryAlias()
    {
        return "chunkloaderlist";
    }

    @Override
    public int processCommandPlayer(CommandContext<CommandSource> ctx, Object... params) throws CommandSyntaxException
    {
        String key = "*";
        if (args.length != 0)
        {
            String target = "";
            for (String s : args)
            {
                target = target + " " + s;
            }
            target = target.substring(1).trim();

            List<String> allUsernames = Arrays.asList(server.getPlayerList().getAvailablePlayerDat());
            for (String username : allUsernames)
            {
                if (username.equalsIgnoreCase(target))
                {
                    key = "p:" + username;
                    break;
                }
            }

            for (ModContainer mod : Loader.instance().getActiveModList())
            {
                if (mod.getName().equalsIgnoreCase(target))
                {
                    key = "m:" + mod.getModId();
                }
                else if (mod.getModId().equalsIgnoreCase(target))
                {
                    key = "m:" + mod.getModId();
                }
            }
        }
        list(sender, key);
    }

    @Override
    public int processCommandConsole(CommandContext<CommandSource> ctx, Object... params) throws CommandSyntaxException
    {
        list(sender, "*");
    }

    private void list(CommandSource sender, String key)
    {
        for (int i : DimensionManager.getIDs())
        {
            list(sender, i, key);
        }
    }

    private void list(CommandSource sender, int dim, String key)
    {
        ServerWorld world = DimensionManager.getWorld(dim);

        HashMultimap<String, Ticket> modTickets = HashMultimap.create();
        HashMultimap<String, Ticket> playerTickets = HashMultimap.create();

        for (Ticket ticket : ForgeChunkManager.getPersistentChunksFor(world).values())
        {
            if (ticket.isPlayerTicket())
            {
                playerTickets.put(ticket.getPlayerName(), ticket);
            }
            else
            {
                modTickets.put(ticket.getModId(), ticket);
            }
        }

        if (modTickets.isEmpty() && playerTickets.isEmpty())
        {
            return;
        }

        if (!key.equals("*"))
        {
            ChatOutputHandler.chatNotification(sender, TextFormatting.UNDERLINE + "ChunkLoaders for " + key.split(":", 2)[1] + ":");
        }

        ChatOutputHandler.chatNotification(sender, "Dim " + world.provider.getDimensionType().getName() + ":");

        if (key.startsWith("p:") || key.equals("*"))
        {
            for (String username : playerTickets.keySet())
            {
                if (key.replace("p:", "").equalsIgnoreCase(username) || key.equals("*"))
                {
                    if (key.equals("*"))
                    {
                        ChatOutputHandler.chatNotification(sender, username);
                    }

                    HashSet<ChunkPos> chunks = new HashSet<ChunkPos>();

                    for (Ticket ticket : playerTickets.get(username))
                    {
                        for (Object obj : ticket.getChunkList())
                        {
                            chunks.add((ChunkPos) obj);
                        }
                    }

                    for (ChunkPos coords : chunks)
                    {
                        ChatOutputHandler.chatNotification(sender, coords.x + " : " + coords.z);
                    }
                }
            }
        }

        if (key.startsWith("m:") || key.equals("*"))
        {
            for (String modID : modTickets.keySet())
            {
                if (key.equals("*"))
                {
                    ChatOutputHandler.chatNotification(sender, modID);
                }
                HashSet<ChunkPos> chunks = new HashSet<ChunkPos>();

                for (Ticket ticket : playerTickets.get(modID))
                {
                    for (Object obj : ticket.getChunkList())
                    {
                        chunks.add((ChunkPos) obj);
                    }
                }

                for (ChunkPos coords : chunks)
                {
                    ChatOutputHandler.chatNotification(sender, coords.x + " : " + coords.z);
                }
            }
        }
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            ArrayList<String> options = new ArrayList<String>();
            for (String s : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getAvailablePlayerDat())
            {
                options.add(s);
            }
            for (ModContainer mod : Loader.instance().getActiveModList())
            {
                options.add(mod.getName());
            }

            return getListOfStringsMatchingLastWord(args, options);
        }
        return null;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> setExecution()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
