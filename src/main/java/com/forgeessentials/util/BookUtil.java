package com.forgeessentials.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;

import com.forgeessentials.util.output.LoggingHandler;

public abstract class BookUtil
{

    public static void saveBookToFile(ItemStack book, File savefolder)
    {
        ListNBT pages;
        String filename = "";
        if (book != null)
        {
            if (book.hasTag())
            {
                if (book.getTag().contains("title") && book.getTag().contains("pages"))
                {
                    filename = book.getTag().getString("title") + ".txt";
                    pages = (ListNBT) book.getTag().get("pages");
                    File savefile = new File(savefolder, filename);
                    if (savefile.exists())
                    {
                        savefile.delete();
                    }
                    try
                    {
                        savefile.createNewFile();
                        try (BufferedWriter out = new BufferedWriter(new FileWriter(savefile)))
                        {
                            for (int c = 0; c < pages.size(); c++)
                            {
                                String line = pages.get(c).toString();
                                while (line.contains("\n"))
                                {
                                    out.write(line.substring(0, line.indexOf("\n")));
                                    out.newLine();
                                    line = line.substring(line.indexOf("\n") + 1);
                                }
                                if (line.length() > 0)
                                {
                                    out.write(line);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        LoggingHandler.felog.info("Something went wrong...");
                    }
                }
            }
        }
    }

    public static void getBookFromFile(PlayerEntity player, File file)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();
        if (file.isFile())
        {
            if (file.getName().contains(".txt"))
            {
                List<String> lines = new ArrayList<String>();
                try
                {
                    lines.add(TextFormatting.GREEN + "START" + TextFormatting.BLACK);
                    lines.add("");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                    {
                        String line = reader.readLine();
                        while (line != null)
                        {
                            while (line.length() > 21)
                            {
                                lines.add(line.substring(0, 20));
                                line = line.substring(20);
                            }
                            lines.add(line);
                            line = reader.readLine();
                        }
                        reader.close();
                    }
                    lines.add("");
                    lines.add(TextFormatting.RED + "END" + TextFormatting.BLACK);

                }
                catch (Exception e)
                {
                    LoggingHandler.felog.warn("Error reading script: " + file.getName());
                }
                int part = 0;
                int parts = lines.size() / 10 + 1;
                String filename = file.getName().replaceAll(".txt", "");
                if (filename.length() > 13)
                {
                    filename = filename.substring(0, 10) + "...";
                }
                while (lines.size() != 0)
                {
                    part++;
                    String temp = "";
                    for (int i = 0; i < 10 && lines.size() > 0; i++)
                    {
                        temp += lines.get(0) + "\n";
                        lines.remove(0);
                    }
                    map.put(TextFormatting.GOLD + " File: " + TextFormatting.GRAY + filename + TextFormatting.DARK_GRAY + "\nPart " + part + " of "
                            + parts + TextFormatting.BLACK + "\n\n", temp);
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", file.getName().replace(".txt", ""));
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

    public static void getBookFromFile(PlayerEntity player, File file, String title)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();
        if (file.isFile())
        {
            if (file.getName().contains(".txt"))
            {
                List<String> lines = new ArrayList<String>();
                try
                {
                    lines.add(TextFormatting.GREEN + "START" + TextFormatting.BLACK);
                    lines.add("");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                    {
                        String line = reader.readLine();
                        while (line != null)
                        {
                            while (line.length() > 21)
                            {
                                lines.add(line.substring(0, 20));
                                line = line.substring(20);
                            }
                            lines.add(line);
                            line = reader.readLine();
                        }
                    }
                    lines.add("");
                    lines.add(TextFormatting.RED + "END" + TextFormatting.BLACK);

                }
                catch (Exception e)
                {
                    LoggingHandler.felog.warn("Error reading script: " + file.getName());
                }
                int part = 0;
                int parts = lines.size() / 10 + 1;
                String filename = file.getName().replaceAll(".txt", "");
                if (filename.length() > 13)
                {
                    filename = filename.substring(0, 10) + "...";
                }
                while (lines.size() != 0)
                {
                    part++;
                    String temp = "";
                    for (int i = 0; i < 10 && lines.size() > 0; i++)
                    {
                        temp += lines.get(0) + "\n";
                        lines.remove(0);
                    }
                    map.put(TextFormatting.GOLD + " File: " + TextFormatting.GRAY + filename + TextFormatting.DARK_GRAY + "\nPart " + part + " of "
                            + parts + TextFormatting.BLACK + "\n\n", temp);
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", title);
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

    public static void getBookFromFileUnformatted(PlayerEntity player, File file)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();
        if (file.isFile())
        {
            if (file.getName().contains(".txt"))
            {
                List<String> lines = new ArrayList<String>();
                try
                {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                    {
                        String line = reader.readLine();
                        while (line != null)
                        {
                            lines.add(line);
                            line = reader.readLine();
                        }
                    }
                }
                catch (Exception e)
                {
                    LoggingHandler.felog.warn("Error reading book: " + file.getName());
                }
                while (lines.size() != 0)
                {
                    String temp = "";
                    for (int i = 0; i < 10 && lines.size() > 0; i++)
                    {
                        temp += lines.get(0) + "\n";
                        lines.remove(0);
                    }
                    map.put("", temp);
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", file.getName().replace(".txt", ""));
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

    public static void getBookFromFileUnformatted(PlayerEntity player, File file, String title)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();
        if (file.isFile())
        {
            if (file.getName().contains(".txt"))
            {
                List<String> lines = new ArrayList<String>();
                try
                {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                    {
                        String line = reader.readLine();
                        while (line != null)
                        {
                            lines.add(line);
                            line = reader.readLine();
                        }
                    }
                }
                catch (Exception e)
                {
                    LoggingHandler.felog.warn("Error reading book: " + file.getName());
                }
                while (lines.size() != 0)
                {
                    String temp = "";
                    for (int i = 0; i < 10 && lines.size() > 0; i++)
                    {
                        temp += lines.get(0) + "\n";
                        lines.remove(0);
                    }
                    map.put("", temp);
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", title);
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

    public static void getBookFromFolder(PlayerEntity player, File folder)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();

        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles)
        {
            if (file.isFile())
            {
                if (file.getName().contains(".txt"))
                {
                    List<String> lines = new ArrayList<String>();
                    try
                    {
                        lines.add(TextFormatting.GREEN + "START" + TextFormatting.BLACK);
                        lines.add("");
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                        {
                            String line = reader.readLine();
                            while (line != null)
                            {
                                while (line.length() > 21)
                                {
                                    lines.add(line.substring(0, 20));
                                    line = line.substring(20);
                                }
                                lines.add(line);
                                line = reader.readLine();
                            }
                        }
                        lines.add("");
                        lines.add(TextFormatting.RED + "END" + TextFormatting.BLACK);

                    }
                    catch (Exception e)
                    {
                        LoggingHandler.felog.warn("Error reading script: " + file.getName());
                    }
                    int part = 0;
                    int parts = lines.size() / 10 + 1;
                    String filename = file.getName().replaceAll(".txt", "");
                    if (filename.length() > 13)
                    {
                        filename = filename.substring(0, 10) + "...";
                    }
                    while (lines.size() != 0)
                    {
                        part++;
                        String temp = "";
                        for (int i = 0; i < 10 && lines.size() > 0; i++)
                        {
                            temp += lines.get(0) + "\n";
                            lines.remove(0);
                        }
                        map.put(TextFormatting.GOLD + " File: " + TextFormatting.GRAY + filename + TextFormatting.DARK_GRAY + "\nPart " + part
                                + " of " + parts + TextFormatting.BLACK + "\n\n", temp);
                    }
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", folder.getName());
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

    public static void getBookFromFolder(PlayerEntity player, File folder, String title)
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT pages = new ListNBT();

        HashMap<String, String> map = new HashMap<String, String>();

        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles)
        {
            if (file.isFile())
            {
                if (file.getName().contains(".txt"))
                {
                    List<String> lines = new ArrayList<String>();
                    try
                    {
                        lines.add(TextFormatting.GREEN + "START" + TextFormatting.BLACK);
                        lines.add("");
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
                        {
                            String line = reader.readLine();
                            while (line != null)
                            {
                                while (line.length() > 21)
                                {
                                    lines.add(line.substring(0, 20));
                                    line = line.substring(20);
                                }
                                lines.add(line);
                                line = reader.readLine();
                            }
                        }
                        lines.add("");
                        lines.add(TextFormatting.RED + "END" + TextFormatting.BLACK);

                    }
                    catch (Exception e)
                    {
                        LoggingHandler.felog.warn("Error reading script: " + file.getName());
                    }
                    int part = 0;
                    int parts = lines.size() / 10 + 1;
                    String filename = file.getName().replaceAll(".txt", "");
                    if (filename.length() > 13)
                    {
                        filename = filename.substring(0, 10) + "...";
                    }
                    while (lines.size() != 0)
                    {
                        part++;
                        String temp = "";
                        for (int i = 0; i < 10 && lines.size() > 0; i++)
                        {
                            temp += lines.get(0) + "\n";
                            lines.remove(0);
                        }
                        map.put(TextFormatting.GOLD + " File: " + TextFormatting.GRAY + filename + TextFormatting.DARK_GRAY + "\nPart " + part
                                + " of " + parts + TextFormatting.BLACK + "\n\n", temp);
                    }
                }
            }
        }

        SortedSet<String> keys = new TreeSet<String>(map.keySet());
        for (String name : keys)
        {
            pages.appendTag(new StringNBT(name + map.get(name)));
        }

        tag.putString("author", "ForgeEssentials");
        tag.putString("title", title);
        tag.put("pages", pages);

        ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
        is.setTag(tag);
        player.inventory.add(is);
    }

}
