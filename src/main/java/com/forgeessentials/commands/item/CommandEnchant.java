package com.forgeessentials.commands.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import org.apache.commons.lang3.StringUtils;

import com.forgeessentials.commands.ModuleCommands;
import com.forgeessentials.core.commands.ParserCommandBase;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.util.CommandParserArgs;

public class CommandEnchant extends ParserCommandBase
{
    private static final String PERM = ModuleCommands.PERM + ".enchant";

    @Override
    public String getPrimaryAlias()
    {
        return "enchant";
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionNode()
    {
        return PERM;
    }

    @Override
    public void parse(CommandParserArgs arguments) throws CommandException
    {
        ItemStack stack = arguments.senderPlayer.getMainHandItem();
        if (stack == ItemStack.EMPTY)
            throw new TranslatedCommandException("You are not holding a valid item");

        List<String> validEnchantmentNames = new ArrayList<>();
        Map<String, Enchantment> validEnchantments = new HashMap<>();
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS)
            if (enchantment != null && enchantment.canApplyAtEnchantingTable(stack))
            {
                String name = I18n.translateToLocal(enchantment.getName()).replaceAll(" ", "");
                validEnchantmentNames.add(name);
                validEnchantments.put(name.toLowerCase(), enchantment);
            }

        if (arguments.isEmpty())
        {
            if (arguments.isTabCompletion)
                return;
            arguments.confirm("Possible enchantments: %s", StringUtils.join(validEnchantmentNames, ", "));
            return;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        while (!arguments.isEmpty())
        {
            arguments.tabComplete(validEnchantmentNames);
            String name = arguments.remove();
            Enchantment enchantment = validEnchantments.get(name.toLowerCase());
            if (enchantment == null)
                throw new TranslatedCommandException("Invalid enchantment name %s!", name);

            if (arguments.isEmpty())
            {
                enchantments.put(enchantment, enchantment.getMaxLevel());
                break;
            }
            enchantments.put(enchantment, Math.min(enchantment.getMaxLevel(), arguments.parseInt()));
        }
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

}
