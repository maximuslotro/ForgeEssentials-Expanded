package com.forgeessentials.commands.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import org.apache.commons.lang3.StringUtils;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.commands.ModuleCommands;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.util.output.ChatOutputHandler;

/**
 * Allows you to modify a bunch of interesting stuff...
 */

public class CommandCapabilities extends BaseCommand
{
    public static ArrayList<String> names;

    static
    {
        names = new ArrayList<>();
        names.add("disabledamage");
        names.add("isflying");
        names.add("allowflying");
        names.add("iscreativemode");
        names.add("allowedit");
    }

    @Override
    public String getPrimaryAlias()
    {
        return "capabilities";
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionNode()
    {
        return ModuleCommands.PERM + ".capabilities";
    }

    @Override
    public void registerExtraPermissions()
    {
        APIRegistry.perms.registerPermission(getPermissionNode() + ".others", DefaultPermissionLevel.OP, "Apply capabilities to others");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 3)
        {

        }

        if (args.length == 0)
        {
            ChatOutputHandler.chatNotification(sender, "Possible capabilities:");
            ChatOutputHandler.chatNotification(sender, StringUtils.join(names.toArray(), ", "));
        }
        else if (args.length == 1)
        {
            ServerPlayerEntity player = UserIdent.getPlayerByMatchOrUsername(sender, args[0]);
            if (player != null)
            {
                ChatOutputHandler.chatNotification(sender, Translator.format("Capabilities for %s:", player.getName()));
                ChatOutputHandler.chatNotification(sender, names.get(0) + " = " + player.abilities.invulnerable);
                ChatOutputHandler.chatNotification(sender, names.get(1) + " = " + player.abilities.flying);
                ChatOutputHandler.chatNotification(sender, names.get(2) + " = " + player.abilities.mayfly);
                ChatOutputHandler.chatNotification(sender, names.get(3) + " = " + player.gameMode.isCreative());
                ChatOutputHandler.chatNotification(sender, names.get(4) + " = " + player.abilities.mayBuild);
            }
            else
            {
                ChatOutputHandler.chatError(sender, String.format("Player %s does not exist, or is not online.", args[0]));
            }
        }
        else if (args.length == 2)
        {
            if (sender instanceof PlayerEntity && !PermissionAPI.hasPermission((PlayerEntity) sender, getPermissionNode() + ".others"))
                throw new TranslatedCommandException("You don't have permissions for that.");

            ServerPlayerEntity player = UserIdent.getPlayerByMatchOrUsername(sender, args[0]);
            if (player != null)
            {
                if (args[1].equalsIgnoreCase(names.get(0)))
                {
                    ChatOutputHandler.chatNotification(sender, player.getName() + " => " + names.get(0) + " = " + player.capabilities.disableDamage);
                }
                else if (args[1].equalsIgnoreCase(names.get(1)))
                {
                    ChatOutputHandler.chatNotification(sender, player.getName() + " => " + names.get(1) + " = " + player.capabilities.isFlying);
                }
                else if (args[1].equalsIgnoreCase(names.get(2)))
                {
                    ChatOutputHandler.chatNotification(sender, player.getName() + " => " + names.get(2) + " = " + player.capabilities.allowFlying);
                }
                else if (args[1].equalsIgnoreCase(names.get(3)))
                {
                    ChatOutputHandler.chatNotification(sender, player.getName() + " => " + names.get(3) + " = " + player.capabilities.isCreativeMode);
                }
                else if (args[1].equalsIgnoreCase(names.get(4)))
                {
                    ChatOutputHandler.chatNotification(sender, player.getName() + " => " + names.get(4) + " = " + player.capabilities.allowEdit);
                }
                else
                    throw new CommandException("Capability '%s' unknown.", args[1]);
            }
        }
        else if (args.length == 3)
        {
            if (sender instanceof PlayerEntity && !PermissionAPI.hasPermission((PlayerEntity) sender, getPermissionNode() + ".others"))
                throw new TranslatedCommandException("You don't have permissions for that.");
            ServerPlayerEntity player = UserIdent.getPlayerByMatchOrUsername(sender, args[0]);
            if (player != null)
            {
                if (args[1].equalsIgnoreCase(names.get(0)))
                {
                    boolean bln = Boolean.parseBoolean(args[2]);
                    player.abilities.invulnerable = bln;
                    ChatOutputHandler.chatNotification(sender, names.get(0) + " = " + player.abilities.invulnerable);
                }
                else if (args[1].equalsIgnoreCase(names.get(1)))
                {
                    boolean bln = Boolean.parseBoolean(args[2]);
                    player.abilities.flying = bln;
                    ChatOutputHandler.chatNotification(sender, names.get(1) + " = " + player.abilities.flying);
                }
                else if (args[1].equalsIgnoreCase(names.get(2)))
                {
                    boolean bln = Boolean.parseBoolean(args[2]);
                    player.abilities.mayfly = bln;
                    ChatOutputHandler.chatNotification(sender, names.get(2) + " = " + player.abilities.mayfly);
                }
                else if (args[1].equalsIgnoreCase(names.get(3)))
                {
                    boolean bln = Boolean.parseBoolean(args[2]);
                    player.capabilities.isCreativeMode = bln;
                    ChatOutputHandler.chatNotification(sender, names.get(3) + " = " + player.gameMode.isCreative());
                }
                else if (args[1].equalsIgnoreCase(names.get(4)))
                {
                    boolean bln = Boolean.parseBoolean(args[2]);
                    player.abilities.mayBuild = bln;
                    ChatOutputHandler.chatNotification(sender, names.get(4) + " = " + player.abilities.mayBuild);
                }
                else
                    throw new CommandException("command.capabilities.capabilityUnknown", args[1]);
                player.sendPlayerAbilities();
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return matchToPlayers(args);
        }
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, names);
        }
        else if (args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        else
        {
            return null;
        }
    }

}
