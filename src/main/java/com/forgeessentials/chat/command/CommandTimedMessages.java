package com.forgeessentials.chat.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.chat.ModuleChat;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.commands.ParserCommandBase;
import com.forgeessentials.core.misc.TaskRegistry;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.moduleLauncher.config.ConfigSaver;
import com.forgeessentials.util.CommandParserArgs;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.forgeessentials.util.output.LoggingHandler;
import com.google.gson.JsonParseException;

public class CommandTimedMessages extends ParserCommandBase implements Runnable
{

    public static final String CATEGORY = ModuleChat.CONFIG_CATEGORY + "_TimedMessage";

    public static final String MESSAGES_HELP = "Each line is 1 message. \nYou can use scripting arguments and color codes. "
            + "\nUsing json messages (tellraw) is also supported";

    public static final String[] MESSAGES_DEFAULT = new String[] { "This server runs ForgeEssentials server management mod" };

    protected static List<String> messages = new ArrayList<>();

    protected static int interval;

    protected static boolean shuffle;

    protected static boolean enabled;

    protected List<Integer> messageOrder = new ArrayList<>();

    protected int currentIndex;

    public CommandTimedMessages()
    {
        // CONFIG ForgeEssentials.getConfigManager().registerLoader(ModuleChat.CONFIG_FILE, this);
    }

    @Override
    public String getPrimaryAlias()
    {
        return "timedmessage";
    }

    @Override
    public String[] getDefaultSecondaryAliases()
    {
        return new String[] { "tm" };
    }

    @Override
    public String getPermissionNode()
    {
        return "fe.chat.timedmessage";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public void parse(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isEmpty())
        {
            arguments.confirm("/tm add <message>: Add a message");
            arguments.confirm("/tm list: Delete a message");
            arguments.confirm("/tm delete <id>: Delete a message");
            arguments.confirm("/tm send <id>: Send a message");
            arguments.confirm("/tm interval <sec>: Set message interval");
            arguments.confirm("/tm shuffle <true|false>: Enable/disable shuffling of messages");
            return;
        }

        arguments.tabComplete("add", "list", "delete", "interval", "random");
        String sumCmd = arguments.remove().toLowerCase();
        switch (sumCmd)
        {
        case "add":
            parseAdd(arguments);
            break;
        case "list":
            parseList(arguments);
            break;
        case "delete":
            parseDelete(arguments);
            break;
        case "send":
            parseSend(arguments);
            break;
        case "interval":
            parseInterval(arguments);
            break;
        case "shuffle":
            parseShuffle(arguments);
            break;
        default:
            throw new TranslatedCommandException(FEPermissions.MSG_UNKNOWN_SUBCOMMAND, sumCmd);
        }
    }

    public void parseAdd(CommandParserArgs arguments)
    {
        if (arguments.isTabCompletion)
            return;
        if (arguments.isEmpty())
        {
            arguments.confirm("/timedmessage add <message...>: Add a timed message");
            return;
        }
        String message = arguments.toString();
        addMessage(message);
        arguments.confirm("Added new message:");
        arguments.sendMessage(formatMessage(message));
        // ForgeEssentials.getConfigManager().save(ModuleChat.CONFIG_FILE);
        save();
    }

    public void parseList(CommandParserArgs arguments)
    {
        if (arguments.isTabCompletion)
            return;
        arguments.confirm("List of messages:");
        for (int i = 0; i < messages.size(); i++)
            arguments.sendMessage(new TranslationTextComponent(String.format("%d: %s", i, formatMessage(messages.get(i)))));
    }

    public void parseDelete(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isTabCompletion)
            return;
        if (arguments.isEmpty())
        {
            arguments.confirm("/timedmessage delete <index>: Delete a timed message");
            return;
        }
        int index = arguments.parseInt();
        if (index < 0 || index >= messages.size())
            throw new TranslatedCommandException("Index out of bounds");
        messages.remove(index);
        arguments.confirm("Removed message");
        // ForgeEssentials.getConfigManager().save(ModuleChat.CONFIG_FILE);
        save();
    }

    public void parseSend(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isTabCompletion)
            return;
        if (arguments.isEmpty())
        {
            arguments.confirm("/timedmessage send <index>: Send a timed message");
            return;
        }
        int index = arguments.parseInt();
        if (index < 0 || index >= messages.size())
            throw new TranslatedCommandException("Index out of bounds");
        broadcastMessage(index);
    }

    public void parseInterval(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isTabCompletion)
            return;
        if (arguments.isEmpty())
        {
            arguments.confirm("/tm interval <sec>: Set message interval (0 = disabled)");
            return;
        }
        setInterval(arguments.parseInt());
        // ForgeEssentials.getConfigManager().save(ModuleChat.CONFIG_FILE);
        save();
    }

    public void parseShuffle(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isEmpty())
        {
            arguments.confirm("/tm shuffle <true|false>: Enable/disable shuffling of messages");
            return;
        }
        boolean newShuffle = arguments.parseBoolean();
        if (arguments.isTabCompletion)
            return;
        if (newShuffle != shuffle)
        {
            shuffle = newShuffle;
            initMessageOrder();
            // ForgeEssentials.getConfigManager().save(ModuleChat.CONFIG_FILE);
            save();
        }
    }

    @Override
    public void run()
    {
        if (messages.isEmpty() || !enabled)
            return;
        if (currentIndex >= messages.size())
            currentIndex = 0;
        broadcastMessage(messageOrder.get(currentIndex));
        currentIndex++;
    }

    public void broadcastMessage(int index)
    {
        if (index >= 0 && index < messages.size())
            ChatOutputHandler.broadcast(formatMessage(messages.get(index)));
    }

    public void addMessage(String message)
    {
        messages.add(message);
        initMessageOrder();
    }

    public void initMessageOrder()
    {
        messageOrder.clear();
        for (int i = 0; i < messages.size(); i++)
            messageOrder.add(i);
        if (shuffle)
            Collections.shuffle(messageOrder);
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        if (interval < 0)
            interval = 0;
        if (CommandTimedMessages.interval == interval)
            return;
        if (CommandTimedMessages.interval > 0)
            TaskRegistry.remove(this);
        CommandTimedMessages.interval = interval;
        if (interval > 0)
            TaskRegistry.scheduleRepeated(this, interval * 1000);
    }

    public static ITextComponent formatMessage(String message)
    {
        message = ModuleChat.processChatReplacements(null, message);
        try
        {
            return ITextComponent.Serializer.fromJson(message);
        }
        catch (JsonParseException e)
        {
            if (message.contains("{"))
            {
                LoggingHandler.felog.warn("Error in timedmessage format: " + ExceptionUtils.getRootCause(e).getMessage());
            }
            return new StringTextComponent(message);
        }
    }

    static ForgeConfigSpec.IntValue FEinverval;
    static ForgeConfigSpec.BooleanValue FEenabled;
    static ForgeConfigSpec.BooleanValue FEshuffle;
    static ForgeConfigSpec.ConfigValue<String[]> FEmessages;

    public static void load(ForgeConfigSpec.Builder BUILDER)
    {
        BUILDER.comment("Automated spam").push(CATEGORY);
        FEinverval = BUILDER.comment("Interval in seconds. 0 to disable").defineInRange("inverval", 60, 0, Integer.MAX_VALUE);
        FEenabled = BUILDER.comment("Enable TimedMessages.").define("enabled", false);
        FEshuffle = BUILDER.comment("Shuffle messages").define("shuffle", false);
        FEmessages = BUILDER.comment(MESSAGES_HELP).define("messages", MESSAGES_DEFAULT);
        BUILDER.pop();
    }

    public static void bakeConfig(boolean reload)
    {
        setInterval(FEinverval.get());
        enabled = FEenabled.get();
        shuffle = FEshuffle.get();
        messages = new ArrayList<String>(Arrays.asList(FEmessages.get()));
        initMessageOrder();
    }

    public void save()
    {
        FEinverval.set(interval);
        FEshuffle.set(shuffle);
        FEmessages.set(messages.toArray(new String[messages.size()]));
    }
}
