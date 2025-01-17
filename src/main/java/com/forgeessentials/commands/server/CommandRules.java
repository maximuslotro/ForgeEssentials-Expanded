package com.forgeessentials.commands.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.commands.ModuleCommands;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.commands.BaseCommand;
import com.forgeessentials.core.misc.FECommandManager.ConfigurableCommand;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.forgeessentials.util.output.LoggingHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandRules extends BaseCommand implements ConfigurableCommand
{

    public CommandRules(String name, int permissionLevel, boolean enabled)
    {
        super(name, permissionLevel, enabled);
    }

    public static final String[] autocomargs = { "add", "remove", "move", "change", "book" };
    public static ArrayList<String> rules;
    public static File rulesFile = new File(ForgeEssentials.getFEDirectory(), "rules.txt");

    public ArrayList<String> loadRules()
    {
        ArrayList<String> rules = new ArrayList<>();

        if (!rulesFile.exists())
        {
            LoggingHandler.felog.info("No rules file found. Generating with default rules..");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rulesFile))))
            {
                writer.write("# " + rulesFile.getName() + " | numbers are automatically added");
                writer.newLine();

                writer.write("Obey the Admins");
                rules.add("Obey the Admins");
                writer.newLine();

                writer.write("Do not grief");
                rules.add("Do not grief");
                writer.newLine();

                LoggingHandler.felog.info("Completed generating rules file.");
            }
            catch (IOException e)
            {
                LoggingHandler.felog.error("Error writing the Rules file: " + rulesFile.getName());
            }
        }
        else
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(rulesFile))))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("#"))
                        continue;
                    rules.add(line);
                }
            }
            catch (IOException e)
            {
                LoggingHandler.felog.error("Error writing the Rules file: " + rulesFile.getName());
            }
        }

        return rules;
    }

    public void saveRules()
    {
        try
        {
            LoggingHandler.felog.info("Saving rules");

            if (!rulesFile.exists())
            {
                rulesFile.createNewFile();
            }

            // create streams
            FileOutputStream stream = new FileOutputStream(rulesFile);
            OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
            BufferedWriter writer = new BufferedWriter(streamWriter);

            writer.write("# " + rulesFile.getName() + " | numbers are automatically added");
            writer.newLine();

            for (String rule : rules)
            {
                writer.write(rule);
                writer.newLine();
            }

            writer.close();
            streamWriter.close();
            stream.close();

            LoggingHandler.felog.info("Completed saving rules file.");
        }
        catch (IOException e)
        {
            LoggingHandler.felog.error("Error writing the Rules file: " + rulesFile.getName());
        }
    }

    @Override
    public String getPrimaryAlias()
    {
        return "rules";
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.ALL;
    }

    @Override
    public String getPermissionNode()
    {
        return ModuleCommands.PERM + ".rules";
    }

    @Override
    public void registerExtraPermissions()
    {
        APIRegistry.perms.registerPermission(getPermissionNode() + ".edit", DefaultPermissionLevel.OP, "Edit rules");
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> setExecution()
    {
        return null;
    }

    @Override
    public int processCommandPlayer(CommandContext<CommandSource> ctx, Object... params) throws CommandSyntaxException
    {
        if (args.length == 0)
        {
            for (String rule : rules)
            {
                ChatOutputHandler.chatNotification(sender, rule);
            }
            return;
        }
        else if (args[0].equalsIgnoreCase("book"))
        {
            CompoundNBT tag = new CompoundNBT();
            ListNBT pages = new ListNBT();

            HashMap<String, String> map = new HashMap<>();

            for (int i = 0; i < rules.size(); i++)
            {
                map.put(TextFormatting.UNDERLINE + "Rule #" + (i + 1) + "\n\n", TextFormatting.RESET + ChatOutputHandler.formatColors(rules.get(i)));
            }

            SortedSet<String> keys = new TreeSet<>(map.keySet());
            for (String name : keys)
            {
                StringNBT s = StringNBT.valueOf(name + map.get(name));
                pages.add(s);
            }

            tag.putString("author", "ForgeEssentials");
            tag.putString("title", "Rule Book");
            tag.put("pages", pages);

            ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
            is.setTagCompound(tag);
            sender.inventory.add(is);
            return;
        }
        else if (args.length == 1)
        {

            if (args[0].equalsIgnoreCase("help"))
            {
                ChatOutputHandler.chatNotification(sender, " - /rules [#]");
                if (PermissionAPI.hasPermission(sender, getPermissionNode() + ".edit"))
                {
                    ChatOutputHandler.chatNotification(sender, " - /rules &lt;#> [changedRule]");
                    ChatOutputHandler.chatNotification(sender, " - /rules add &lt;newRule>");
                    ChatOutputHandler.chatNotification(sender, " - /rules remove &lt;#>");
                    ChatOutputHandler.chatNotification(sender, " - /rules move &lt;#> &lt;#>");
                }
                return;
            }

            ChatOutputHandler.chatNotification(sender, rules.get(parseInt(args[0], 1, rules.size()) - 1));
            return;
        }

        if (!PermissionAPI.hasPermission(sender, getPermissionNode() + ".edit"))
            throw new TranslatedCommandException(
                    "You have insufficient permissions to do that. If you believe you received this message in error, please talk to a server admin.");

        int index;

        if (args[0].equalsIgnoreCase("remove"))
        {
            index = parseInt(args[1], 1, rules.size());

            rules.remove(index - 1);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %s removed", args[1]));
        }
        else if (args[0].equalsIgnoreCase("add"))
        {
            String newRule = "";
            for (int i = 1; i < args.length; i++)
            {
                newRule = newRule + args[i] + " ";
            }
            newRule = ChatOutputHandler.formatColors(newRule);
            rules.add(newRule);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule added as # %s.", args[1]));
        }
        else if (args[0].equalsIgnoreCase("move"))
        {
            index = parseInt(args[1], 1, rules.size());

            String temp = rules.remove(index - 1);

            index = parseInt(args[2], 1, Integer.MAX_VALUE);
            if (index < rules.size())
            {
                rules.add(index - 1, temp);
                ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %1$s moved to # %2$s", args[1], args[2]));
            }
            else
            {
                rules.add(temp);
                ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %1$s moved to last position.", args[1]));
            }
        }
        else if (args[0].equalsIgnoreCase("change"))
        {
            index = parseInt(args[1], 1, rules.size());

            String newRule = "";
            for (int i = 2; i < args.length; i++)
            {
                newRule = newRule + args[i] + " ";
            }
            newRule = ChatOutputHandler.formatColors(newRule);
            rules.set(index - 1, newRule);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rules # %1$s changed to '%2$s'.", index + "", newRule));
        }
        saveRules();
    }

    @Override
    public int processCommandConsole(CommandContext<CommandSource> ctx, Object... params) throws CommandSyntaxException
    {
        if (args.length == 0)
        {
            for (String rule : rules)
            {
                ChatOutputHandler.sendMessage(sender, rule);
            }
            return;
        }
        if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("help"))
            {
                ChatOutputHandler.chatConfirmation(sender, " - /rules [#]");
                ChatOutputHandler.chatConfirmation(sender, " - /rules &lt;#> [changedRule]");
                ChatOutputHandler.chatConfirmation(sender, " - /rules add &lt;newRule>");
                ChatOutputHandler.chatConfirmation(sender, " - /rules remove &lt;#>");
                ChatOutputHandler.chatConfirmation(sender, " - /rules move &lt;#> &lt;#>");

            }

            ChatOutputHandler.sendMessage(sender, rules.get(parseInt(args[0], 1, rules.size()) - 1));
            return;
        }

        int index;

        if (args[0].equalsIgnoreCase("remove"))
        {
            index = parseInt(args[1], 1, rules.size());

            rules.remove(index - 1);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %s removed", args[1]));
        }
        else if (args[0].equalsIgnoreCase("add"))
        {
            String newRule = "";
            for (int i = 1; i < args.length; i++)
            {
                newRule = newRule + args[i] + " ";
            }
            newRule = ChatOutputHandler.formatColors(newRule);
            rules.add(newRule);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule added as # %s.", args[1]));
        }
        else if (args[0].equalsIgnoreCase("move"))
        {
            index = parseInt(args[1], 1, rules.size());

            String temp = rules.remove(index - 1);

            index = parseInt(args[2], 1, Integer.MAX_VALUE);
            if (index < rules.size())
            {
                rules.add(index - 1, temp);
                ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %1$s moved to # %2$s", args[1], args[2]));
            }
            else
            {
                rules.add(temp);
                ChatOutputHandler.chatConfirmation(sender, Translator.format("Rule # %1$s moved to last position.", args[1]));
            }
        }
        else if (args[0].equalsIgnoreCase("change"))
        {
            index = parseInt(args[1], 1, rules.size());

            String newRule = "";
            for (int i = 2; i < args.length; i++)
            {
                newRule = newRule + args[i] + " ";
            }
            newRule = ChatOutputHandler.formatColors(newRule);
            rules.set(index - 1, newRule);
            ChatOutputHandler.chatConfirmation(sender, Translator.format("Rules # %1$s changed to '%2$s'.", index + "", newRule));
        }
        saveRules();
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, autocomargs);
        }
        else if (args.length == 2)
        {
            List<String> opt = new ArrayList<>();
            for (int i = 1; i < rules.size() + 1; i++)
            {
                opt.add(i + "");
            }
            return opt;
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("move"))
        {
            List<String> opt = new ArrayList<>();
            for (int i = 1; i < rules.size() + 2; i++)
            {
                opt.add(i + "");
            }
            return opt;
        }
        else
        {
            return null;
        }
    }

    static ForgeConfigSpec.ConfigValue<String> name;
    @Override
    public void loadConfig(ForgeConfigSpec.Builder BUILDER, String category)
    {
    	BUILDER.push(category);
    	name = BUILDER.comment("Name for rules file").define("filename", "rules.txt");
    	BUILDER.pop();
    }

    @Override
    public void loadData()
    {
        /* do nothing */
    }

    @Override
    public void bakeConfig(boolean reload)
    {
    	rulesFile = new File(ForgeEssentials.getFEDirectory(), name.get());
    	rules = loadRules();
    }
}
