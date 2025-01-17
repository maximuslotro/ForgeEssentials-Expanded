package com.forgeessentials.commands.world;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.commands.ModuleCommands;
import com.forgeessentials.commons.selections.AreaShape;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.commands.BaseCommand;
import com.forgeessentials.core.misc.TaskRegistry;
import com.forgeessentials.core.misc.TaskRegistry.TickTask;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.util.CommandParserArgs;
import com.forgeessentials.util.ServerUtil;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.forgeessentials.worldborder.ModuleWorldBorder;
import com.forgeessentials.worldborder.WorldBorder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class CommandPregen extends BaseCommand implements TickTask
{

    public CommandPregen(String name, int permissionLevel, boolean enabled)
    {
        super(name, permissionLevel, enabled);
    }

    private boolean running = false;

    private ServerWorld world;

    private AreaShape shape;

    private int minX;

    private int minZ;

    private int minY;
    private int maxY;

    private int maxX;

    private int maxZ;

    private int centerX;

    private int centerZ;

    private int sizeX;

    private int sizeZ;

    private int x;

    private int z;

    private int totalTicks;

    private int totalChunks;

    @Override
    public String getPrimaryAlias()
    {
        return "pregen";
    }

    @Override
    public String[] getDefaultSecondaryAliases()
    {
        return new String[] { "filler" };
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
        return ModuleCommands.PERM + ".pregen";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> setExecution()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void parse(CommandParserArgs arguments) throws CommandException
    {
        if (arguments.isEmpty())
        {
            if (running)
            {
                arguments.confirm("Pregen running");
            }
            else
            {
                arguments.confirm("No pregen running");
                arguments.notify("/pregen start [dim]" + (ForgeEssentials.isCubicChunksInstalled ? " [minY] [maxY]" : ""));
                arguments.notify("/pregen status");
                arguments.notify("/pregen stop");
                arguments.notify("/pregen flush");
            }
            return;
        }

        arguments.tabComplete("start", "stop", "status", "flush");
        String subCmd = arguments.remove().toLowerCase();
        switch (subCmd)
        {
        case "start":
            parseStart(arguments);
            break;
        case "stop":
            parseStop(arguments);
            break;
        case "flush":
            flush(arguments);
            break;
        case "status":
            parseStatus(arguments);
            break;
        default:
            throw new TranslatedCommandException(FEPermissions.MSG_UNKNOWN_SUBCOMMAND, subCmd);
        }
    }

    /* ------------------------------------------------------------ */

    private void parseStart(CommandParserArgs arguments) throws CommandException
    {
        if (running)
        {
            arguments.error("Pregen already running");
            return;
        }

        world = null;
        world = arguments.parseWorld();

        WorldBorder border = ModuleWorldBorder.getInstance().getBorder(world);
        if (border == null)
            throw new TranslatedCommandException("No worldborder defined");

        centerX = border.getCenter().getX() / 16;
        centerZ = border.getCenter().getZ() / 16;
        sizeX = border.getSize().getX() / 16;
        sizeZ = border.getSize().getZ() / 16;
        minX = border.getArea().getLowPoint().getX() / 16;
        minZ = border.getArea().getLowPoint().getZ() / 16;
        maxX = border.getArea().getHighPoint().getX() / 16;
        maxZ = border.getArea().getHighPoint().getZ() / 16;

        if (ForgeEssentials.isCubicChunksInstalled)
        {
            try
            {
                minY = arguments.parseInt();
            }
            catch (TranslatedCommandException e)
            {
                minY = -8;
            }

            try
            {
                maxY = arguments.parseInt();
            }
            catch (TranslatedCommandException e)
            {
                maxY = 8;
            }
        }

        shape = border.getShape();

        x = minX - 1;
        z = minZ;
        running = true;
        totalTicks = 0;
        totalChunks = 0;

        TaskRegistry.schedule(this);
        arguments.confirm("Pregen started");
    }

    private void parseStop(CommandParserArgs arguments)
    {
        if (!running)
        {
            arguments.error("No pregen running");
            return;
        }
        running = false;
    }

    private void flush(CommandParserArgs arguments)
    {
        if (!running)
        {
            arguments.error("No pregen running");
            return;
        }
        ServerChunkProvider providerServer = world.getChunkSource();
        providerServer.chunkMap.queueUnloadAll();
        arguments.confirm("Queued all chunks for unloading");
    }

    private void parseStatus(CommandParserArgs arguments)
    {
        if (!running)
        {
            arguments.error("No pregen running");
            return;
        }
        ServerChunkProvider providerServer = (ServerChunkProvider) world.getChunkSource();
        arguments.confirm("Pregen: %d/%d chunks, tps:%.1f, lc:%d", totalChunks, sizeX * sizeZ * 4, ServerUtil.getTPS(), providerServer.getLoadedChunksCount());
    }

    @Override
    public boolean tick()
    {
        if (!running)
        {
            notifyPlayers("Pregen stopped");
            return true;
        }

        double tps = ServerUtil.getTPS();
        if (tps < 5)
        {
            return false;
        }

        totalTicks++;

        ServerChunkProvider providerServer = world.getChunkSource();

        if (totalTicks % 80 == 0)
            notifyPlayers(String.format("Pregen: %d/%d chunks, tps:%.1f, lc:%d", totalChunks, sizeX * sizeZ, tps,
                    providerServer.getLoadedChunksCount()));
        for (int i = 0; i < 1; i++)
        {
            int skippedChunks = 0;
            while (true)
            {
                totalChunks++;
                if (!next())
                {
                    running = false;
                    notifyPlayers("World pregen finished");
                    return true;
                }

                if (!ForgeEssentials.isCubicChunksInstalled || !CCPregenCompat.isCCWorld(world))
                {
                    if (RegionFileCache.getRegionFile(new ChunkPos( x, z)).doesChunkExist(new ChunkPos(x & 0x1F, z & 0x1F))
                            || (providerServer.chunkExists(x, z)))
                    {
                        skippedChunks++;
                        if (skippedChunks > 16 * 16)
                            break;
                        else
                            continue;
                    }

                    if (providerServer.getLoadedChunksCount() > 256)
                    {
                        providerServer.save(true);
                        providerServer.queueUnloadAll();
                    }
                    providerServer.provideChunk(x, z);
                }
                else
                {
                    boolean fullySkipped = true;
                    for (int y = minY; y <= maxY; y++)
                    {
                        fullySkipped &= !CCPregenCompat.genCube(world, providerServer, x, y, z);
                    }

                    if (fullySkipped)
                    {
                        skippedChunks++;
                        if (skippedChunks > 16 * 16)
                            break;
                        else
                            continue;
                    }
                }

                break;
            }
        }
        return false;
    }

    private boolean next()
    {
        switch (shape)
        {
        default:
        case BOX:
            if (++x > maxX)
            {
                x = minX;
                if (++z > maxZ)
                    return false;
            }
            return true;
        case CYLINDER:
        case ELLIPSOID:
            while (true)
            {
                if (++x > maxX)
                {
                    x = minX;
                    if (++z > maxZ)
                        return false;
                }
                double dx = (double) (centerX - x) / sizeX;
                double dz = (double) (centerZ - z) / sizeZ;
                if (dx * dx + dz * dz <= 1)
                    return true;
            }
        }
    }

    @Override
    public boolean editsBlocks()
    {
        return true;
    }

    public void notifyPlayers(String message)
    {
        for (ServerPlayerEntity player : ServerUtil.getPlayerList())
            if (APIRegistry.perms.checkPermission(player, getPermissionNode()))
                ChatOutputHandler.chatNotification(player, message);
    }

    /* ------------------------------------------------------------ */

    private static void saveChunk(ServerChunkProvider provider, Chunk chunk)
    {
        ChunkLoader loader = (ChunkLoader) provider.chunkMap;
        try
        {
            loader.saveChunk(provider.world, chunk);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
