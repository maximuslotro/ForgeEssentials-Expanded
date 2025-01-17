package com.forgeessentials.core.preloader.injections;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fe.event.entity.EntityAttackedEvent;

import com.forgeessentials.core.preloader.asminjector.CallbackInfo;
import com.forgeessentials.core.preloader.asminjector.annotation.At;
import com.forgeessentials.core.preloader.asminjector.annotation.Inject;
import com.forgeessentials.core.preloader.asminjector.annotation.Mixin;

@Mixin(exclude = { Entity.class })
public abstract class MixinEntity extends Entity
{

    public MixinEntity(EntityType<?> Entity, World p_i48580_2_)
    {
        super(Entity, p_i48580_2_);
    }

    @Inject(target = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", aliases = "attackEntityFrom=func_70097_a", at = @At("HEAD"))
    protected void hurt_event(DamageSource damageSource, float damage, CallbackInfo ci)
    {
        EntityAttackedEvent event = new EntityAttackedEvent(this, damageSource, damage);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            ci.doReturn(event.result);
        }
        damage = event.damage;
    }

}
