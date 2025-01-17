package com.forgeessentials.util;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.api.permissions.WorldZone;
import com.forgeessentials.commons.selections.WorldPoint;
import com.forgeessentials.core.commands.BaseCommand;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.context.IContext;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CommandParserArgs
{

    public final Command command;
    public final LinkedList<String> args;
    public final CommandSource sender;
    public final ServerPlayerEntity senderPlayer;
    public final UserIdent ident;
    public final boolean isTabCompletion;
    public final MinecraftServer server;
    public IContext context;

    public List<String> tabCompletion;

    public CommandParserArgs(Command command, String[] args, CommandSource sender, boolean isTabCompletion, MinecraftServer server)
    {
        this.command = command;
        this.args = new LinkedList<>(Arrays.asList(args));
        this.sender = sender;
        this.senderPlayer = (sender.getEntity() instanceof ServerPlayerEntity) ? (ServerPlayerEntity) sender.getEntity() : null;
        this.ident = (senderPlayer == null) ? (CommandUtils.GetSource(sender) instanceof DoAsCommandSender ? ((DoAsCommandSender) CommandUtils.GetSource(sender)).getUserIdent() : null) : UserIdent.get(senderPlayer);
        this.isTabCompletion = isTabCompletion;
        if (isTabCompletion)
            tabCompletion = new ArrayList<>();
        this.context = null;
        this.server = server;
    }

    public CommandParserArgs(Command command, String[] args, CommandSource sender, MinecraftServer server)
    {
        this(command, args, sender, false, server);
    }

    public void sendMessage(String message)
    {
        if (!isTabCompletion)
            ChatOutputHandler.sendMessage(sender, message);
    }

    public void sendMessage(ITextComponent message)
    {
        if (!isTabCompletion)
            ChatOutputHandler.sendMessage(sender, message);
    }

    public void confirm(String message, Object... args)
    {
        if (!isTabCompletion)
            ChatOutputHandler.chatConfirmation(sender, Translator.format(message, args));
    }

    public void notify(String message, Object... args)
    {
        if (!isTabCompletion)
            ChatOutputHandler.chatNotification(sender, Translator.format(message, args));
    }

    public void warn(String message, Object... args)
    {
        if (!isTabCompletion)
            ChatOutputHandler.chatWarning(sender, Translator.format(message, args));
    }

    public void error(String message, Object... args)
    {
        if (!isTabCompletion)
            ChatOutputHandler.chatError(sender, Translator.format(message, args));
    }

    public int size()
    {
        return args.size();
    }

    public String remove()
    {
        return args.remove();
    }

    public String peek()
    {
        return args.peek();
    }

    public String get(int index)
    {
        return args.get(index);
    }

    public boolean isEmpty()
    {
        return args.isEmpty();
    }

    public boolean hasPlayer()
    {
        return senderPlayer != null;
    }

    @Deprecated
    public UserIdent parsePlayer() throws CommandException
    {
        return parsePlayer(true, false);
    }

    @Deprecated
    public UserIdent parsePlayer(boolean mustExist) throws CommandException
    {
        return parsePlayer(mustExist, false);
    }

    public UserIdent parsePlayer(boolean mustExist, boolean mustBeOnline) throws CommandException
    {
        if (isTabCompletion && size() == 1)
        {
            tabCompletion = completePlayer(peek());
            throw new CancelParsingException();
        }
        if (isEmpty())
        {
            if (ident != null)
                return ident;
            else
                throw new TranslatedCommandException(FEPermissions.MSG_NOT_ENOUGH_ARGUMENTS);
        }
        else
        {
            String name = remove();
            if (name.equalsIgnoreCase("_ME_"))
            {
                if (senderPlayer == null)
                    throw new TranslatedCommandException("_ME_ cannot be used in console.");
                return ident;
            }
            else
            {
                UserIdent ident = UserIdent.get(name, sender, mustExist);
                if (mustExist && (ident == null || !ident.hasUuid()))
                    throw new TranslatedCommandException("Player %s not found", name);
                else if (mustBeOnline && !ident.hasPlayer())
                    throw new TranslatedCommandException("Player %s is not online", name);
                return ident;
            }
        }
    }

    public static List<String> completePlayer(String arg)
    {
        Set<String> result = new TreeSet<>();
        for (UserIdent knownPlayerIdent : APIRegistry.perms.getServerZone().getKnownPlayers())
        {
            if (BaseCommand.doesStringStartWith(arg, knownPlayerIdent.getUsernameOrUuid()))
                result.add(knownPlayerIdent.getUsernameOrUuid());
        }
        for (ServerPlayerEntity player : ServerUtil.getPlayerList())
        {
            if (BaseCommand.doesStringStartWith(arg, player.getName().getString()))
                result.add(player.getName().getString());
        }
        return new ArrayList<>(result);
    }

    public Item parseItem() throws CommandException
    {
        if (isTabCompletion && size() == 1)
        {
            for (Object item : ForgeRegistries.ITEMS.getKeys())
                if (item.toString().startsWith(peek()))
                    tabCompletion.add(item.toString());
            for (Object item : ForgeRegistries.ITEMS.getKeys())
                if (item.toString().startsWith("minecraft:" + peek()))
                    tabCompletion.add(item.toString().substring(10));
            throw new CancelParsingException();
        }
        String itemName = remove();
        Item item = CommandBase.getItemByText(sender, itemName);
        if (item == null)
            throw new TranslatedCommandException("Item %s not found", itemName);
        return item;
    }

    public Block parseBlock() throws CommandException
    {
        if (isTabCompletion && size() == 1)
        {
            for (Object block : ForgeRegistries.BLOCKS.getKeys())
                if (block.toString().startsWith(peek()))
                    tabCompletion.add(block.toString());
            for (Object block : ForgeRegistries.BLOCKS.getKeys())
                if (block.toString().startsWith("minecraft:" + peek()))
                    tabCompletion.add(block.toString().substring(10));
            throw new CancelParsingException();
        }
        String itemName = remove();
        return CommandBase.getBlockByText(sender, itemName);
    }

    public String parsePermission() throws CommandException
    {
        if (isTabCompletion && size() == 1)
        {
            String permission = peek();
            Set<String> permissionSet = APIRegistry.perms.getServerZone().getRootZone().enumRegisteredPermissions();
            Set<String> result = new TreeSet<>();
            for (String perm : permissionSet)
            {
                int nodeIndex = perm.indexOf('.', permission.length());
                if (nodeIndex >= 0)
                    perm = perm.substring(0, nodeIndex);
                if (BaseCommand.doesStringStartWith(permission, perm))
                    result.add(perm);
            }
            tabCompletion = new ArrayList<>(result);
            throw new CancelParsingException();
        }
        return remove();
    }

    public void checkPermission(String perm) throws CommandException
    {
        if (!isTabCompletion && sender != null && !hasPermission(perm))
            throw new TranslatedCommandException(FEPermissions.MSG_NO_COMMAND_PERM);
    }

    public boolean hasPermission(String perm)
    {
        try
        {
            if (sender.getPlayerOrException() instanceof PlayerEntity)
                return APIRegistry.perms.checkPermission(senderPlayer, perm);
            else
                return true;
        }
        catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void tabComplete(String... completionList) throws CancelParsingException
    {
        if (!isTabCompletion || args.size() != 1)
            return;
        tabCompletion.addAll(BaseCommand.getListOfStringsMatchingLastWord(args.peek(), completionList));
        throw new CancelParsingException();
    }

    public void tabComplete(Collection<String> completionList) throws CancelParsingException
    {
        if (!isTabCompletion || args.size() != 1)
            return;
        tabCompletion.addAll(BaseCommand.getListOfStringsMatchingLastWord(args.peek(), completionList));
        throw new CancelParsingException();
    }

    public void tabCompleteWord(String completion)
    {
        if (!isTabCompletion || args.size() != 1 || completion == null || completion.isEmpty())
            return;
        if (completion.startsWith(args.peek()))
            tabCompletion.add(completion);
    }

    public ServerWorld parseWorld() throws CommandException
    {
        if (isTabCompletion && size() == 1)
        {
            tabCompletion = BaseCommand.getListOfStringsMatchingLastWord(args.peek(), APIRegistry.namedWorldHandler.getWorldNames());
            throw new CancelParsingException();
        }
        if (isEmpty())
        {
            if (senderPlayer != null)
                return (ServerWorld) senderPlayer.getLevel();
            else
                throw new TranslatedCommandException(FEPermissions.MSG_NOT_ENOUGH_ARGUMENTS);
        }
        else
        {
            String name = remove();
            if (name.equalsIgnoreCase("here"))
            {
                if (senderPlayer == null)
                    throw new TranslatedCommandException("\"here\" cannot be used in console.");
                return (ServerWorld) senderPlayer.getLevel();
            }
            else
            {
                return APIRegistry.namedWorldHandler.getWorld(name);
            }
        }
    }

    public int parseInt() throws CommandException
    {
        String value = remove();
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new TranslatedCommandException("Invalid number: %s", value);
        }
    }

    public int parseInt(int min, int max) throws CommandException
    {
        String strValue = remove();
        try
        {
            int value = Integer.parseInt(strValue);
            if (value < min)
                throw new Exception("commands.generic.num.tooSmall" + strValue + Integer.toString(min));
            if (value > max)
                throw new Exception("commands.generic.num.tooBig" + strValue + Integer.toString(max));
            return value;
        }
        catch (Exception e)
        {
            throw new TranslatedCommandException("Invalid number: %s", strValue);
        }
    }

    public long parseLong() throws CommandException
    {
        String value = remove();
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            throw new TranslatedCommandException("Invalid number: %s", value);
        }
    }

    public double parseDouble() throws CommandException
    {
        return CommandUtils.parseDouble(remove());
    }

    public boolean parseBoolean() throws CommandException
    {
        String value = remove().toLowerCase();
        switch (value)
        {
        case "off":
        case "false":
        case "disable":
        case "disabled":
            return false;
        case "on":
        case "true":
        case "enable":
        case "enabled":
            return true;
        default:
            throw new TranslatedCommandException(FEPermissions.MSG_INVALID_ARGUMENT, value);
        }
    }

    public void requirePlayer() throws CommandException
    {
        if (senderPlayer == null)
            throw new TranslatedCommandException(FEPermissions.MSG_NO_CONSOLE_COMMAND);
    }

    public String[] toArray()
    {
        return args.toArray(new String[args.size()]);
    }

    @Override
    public String toString()
    {
        return StringUtils.join(args.toArray(), " ");
    }

    public WorldPoint getSenderPoint()
    {
        CommandSource s = sender != null ? sender : server;
        return new WorldPoint(s.getLevel(), s.getPosition());
    }

    public WorldZone getWorldZone() throws CommandException
    {
        if (senderPlayer == null)
            throw new TranslatedCommandException("Player needed");
        return APIRegistry.perms.getServerZone().getWorldZone(senderPlayer.getLevel());
    }

    public void needsPlayer() throws CommandException
    {
        if (senderPlayer == null)
            throw new TranslatedCommandException(FEPermissions.MSG_NO_CONSOLE_COMMAND);
    }

}
