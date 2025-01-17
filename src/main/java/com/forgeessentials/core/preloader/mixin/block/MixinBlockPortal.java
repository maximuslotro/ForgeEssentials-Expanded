package com.forgeessentials.core.preloader.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fe.event.entity.EntityPortalEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NetherPortalBlock.class)
public class MixinBlockPortal
{

    @Overwrite
    public void onEntityCollidedWithBlock(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        if (!entityIn.isPassenger() && !entityIn.isVehicle())
        { // TODO: get target coordinates somehow
            if (!MinecraftForge.EVENT_BUS.post(new EntityPortalEvent(entityIn, worldIn, pos, entityIn.level, new BlockPos(0, 0, 0))))
            {
                entityIn.setPortal(pos);
            }
        }
    }

}
