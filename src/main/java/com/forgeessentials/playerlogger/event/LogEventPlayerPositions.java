package com.forgeessentials.playerlogger.event;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.forgeessentials.playerlogger.PlayerLoggerEvent;
import com.forgeessentials.playerlogger.entity.Action04PlayerPosition;

public class LogEventPlayerPositions extends PlayerLoggerEvent<Object>
{

    public LogEventPlayerPositions()
    {
        super(null);
    }

    @Override
    public void process(EntityManager em)
    {
        @SuppressWarnings("unchecked")
        List<ServerPlayerEntity> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
        date = new Date();

        for (Iterator<ServerPlayerEntity> it = players.iterator(); it.hasNext();)
        {
            ServerPlayerEntity player = it.next();

            // Action03PlayerEvent action = new Action03PlayerEvent();
            // action.type = PlayerEventType.MOVE;
            Action04PlayerPosition action = new Action04PlayerPosition();
            action.time = date;
            action.player = getPlayer(player);
            action.world = getWorld(player.level.dimension());
            action.x = (int) player.position().x;
            action.y = (int) player.position().y;
            action.z = (int) player.position().z;
            // em.persist(action);
        }
    }

}
