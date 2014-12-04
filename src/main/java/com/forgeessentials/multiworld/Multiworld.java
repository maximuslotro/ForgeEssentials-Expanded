package com.forgeessentials.multiworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;

import com.forgeessentials.commons.selections.WarpPoint;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.data.v2.DataManager;
import com.forgeessentials.util.FunctionHelper;
import com.google.gson.annotations.Expose;

/**
 * 
 * @author Olee
 */
public class Multiworld {
    
    protected String name;

    protected int dimensionId;

    protected String provider;

    protected String worldType;

    protected List<String> biomes = new ArrayList<String>();

    protected long seed;

    protected GameType gameType = GameType.SURVIVAL;

    protected EnumDifficulty difficulty = EnumDifficulty.NORMAL;

    protected boolean allowHostileCreatures = true;

    protected boolean allowPeacefulCreatures = true;

    protected boolean mapFeaturesEnabled = true;

    @Expose(serialize = false)
    protected boolean worldLoaded;

    @Expose(serialize = false)
    protected boolean error;

    @Expose(serialize = false)
    protected int providerId;

    @Expose(serialize = false)
    protected WorldType worldTypeObj;

    public Multiworld(String name, String provider, String worldType, long seed)
    {
        this.name = name;
        this.provider = provider;
        this.worldType = worldType;

        this.seed = seed;
        this.gameType = MinecraftServer.getServer().getGameType();
        this.difficulty = MinecraftServer.getServer().func_147135_j();
        this.allowHostileCreatures = true;
        this.allowPeacefulCreatures = true;
    }

    public Multiworld(String name, String provider, String worldType)
    {
        this(name, provider, worldType, new Random().nextLong());
    }

    public void removeAllPlayersFromWorld()
    {
        WorldServer overworld = MinecraftServer.getServer().worldServerForDimension(0);
        List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : players)
        {
            if (player.dimension == dimensionId)
            {
                teleport(player, overworld);
            }
        }
    }

    public void updateWorldSettings()
    {
        if (!worldLoaded)
            return;
        WorldServer worldServer = getWorldServer();
        worldServer.difficultySetting = difficulty;
        worldServer.setAllowedSpawnTypes(allowHostileCreatures, allowPeacefulCreatures);
    }

    public String getName()
    {
        return name;
    }

    public WorldServer getWorldServer()
    {
        if (!worldLoaded)
            return null;
        return DimensionManager.getWorld(dimensionId);
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public int getProviderId()
    {
        return providerId;
    }

    public String getProvider()
    {
        return provider;
    }

    public List<String> getBiomes()
    {
        return biomes;
    }

    public boolean isError()
    {
        return error;
    }

    public boolean isLoaded()
    {
        return worldLoaded;
    }

    public long getSeed()
    {
        return seed;
    }

    public GameType getGameType()
    {
        return gameType;
    }

    public void setGameType(GameType gameType)
    {
        this.gameType = gameType;
    }

    public EnumDifficulty getDifficulty()
    {
        return difficulty;
    }

    public void setDifficulty(EnumDifficulty difficulty)
    {
        this.difficulty = difficulty;
        updateWorldSettings();
    }

    public boolean isAllowHostileCreatures()
    {
        return allowHostileCreatures;
    }

    public void setAllowHostileCreatures(boolean allowHostileCreatures)
    {
        this.allowHostileCreatures = allowHostileCreatures;
        updateWorldSettings();
    }

    public boolean isAllowPeacefulCreatures()
    {
        return allowPeacefulCreatures;
    }

    public void setAllowPeacefulCreatures(boolean allowPeacefulCreatures)
    {
        this.allowPeacefulCreatures = allowPeacefulCreatures;
        updateWorldSettings();
    }

    protected void save()
    {
        DataManager.getInstance().save(this, this.name);
    }

    protected void delete()
    {
        DataManager.getInstance().delete(this.getClass(), name);
    }

    /**
     * Teleport the player to the multiworld
     */
    public void teleport(EntityPlayerMP player)
    {
        teleport(player, getWorldServer());
    }

    /**
     * Teleport the player to the multiworld
     */
    public static void teleport(EntityPlayerMP player, WorldServer world)
    {
        if (player.worldObj.provider.dimensionId != world.provider.dimensionId)
        {
            displayDepartMessage(player);
            
            // ChunkCoordinates spawn = world.getSpawnPoint();
            int x = (int) Math.floor(player.posX);
            int z = (int) Math.floor(player.posZ);
            int y = FunctionHelper.placeInWorld(world, x, (int) player.posY, z);
            
            WarpPoint target = new WarpPoint(world.provider.dimensionId, x + 0.5, y + 0.5, z + 0.5, player.cameraPitch, player.cameraYaw);
            TeleportHelper.teleport(player, target);

            displayWelcomeMessage(player);
        }
    }

    public static void displayDepartMessage(EntityPlayerMP player)
    {
//        String msg = player.worldObj.provider.getDepartMessage();
//        if (msg == null)
//            msg = "Leaving the Overworld.";
//        if (player.dimension > 1 || player.dimension < -1)
//            msg += " (#" + player.dimension + ")";
//        player.addChatMessage(new ChatComponentText(msg));
    }

    public static void displayWelcomeMessage(EntityPlayerMP player)
    {
//        String msg = player.worldObj.provider.getWelcomeMessage();
//        if (msg == null)
//            msg = "Entering the Overworld.";
//        if (player.dimension > 1 || player.dimension < -1)
//            msg += " (#" + player.dimension + ")";
//        player.addChatMessage(new ChatComponentText(msg));
    }

}