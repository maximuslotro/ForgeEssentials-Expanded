package com.forgeessentials.multiworld;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

/**
 * 
 * @author Olee
 */
public class WorldServerMultiworld extends WorldServer {
    
    private Multiworld world;
    private MultiworldTeleporter worldTeleporter;

    public WorldServerMultiworld(MinecraftServer mcServer, ISaveHandler saveHandler, String worldname, int dimensionId, WorldSettings worldSettings,
            WorldServer worldServer, Profiler profiler, Multiworld world)
    {
        super(mcServer, saveHandler, worldname, dimensionId, worldSettings, profiler);
        this.mapStorage = worldServer.mapStorage;
        this.worldScoreboard = worldServer.getScoreboard();
        this.worldTeleporter = new MultiworldTeleporter(this);
        this.world = world;
    }

    @Override
    public Teleporter getDefaultTeleporter()
    {
        return this.worldTeleporter;
    }

    @Override
    protected void saveLevel() throws MinecraftException
    {
        this.perWorldStorage.saveAllData();
        this.saveHandler.saveWorldInfo(this.worldInfo);
    }

    public Multiworld getMultiworld()
    {
        return world;
    }
    
}