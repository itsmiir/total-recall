package com.miir.totalrecall.entity;

import com.miir.totalrecall.TotalRecall;
import com.miir.totalrecall.entity.effect.RecallEffect;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

public class RecallPearlEntity extends EnderPearlEntity {
    public RecallPearlEntity(World world, LivingEntity owner) {
        super(world, owner);
    }

    public RecallPearlEntity(EntityType<RecallPearlEntity> type, World world) {
        super(type, world);
    }



    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity target && this.getOwner() instanceof LivingEntity owner && owner.world instanceof ServerWorld world) {
            FabricDimensions.teleport(target, world, new TeleportTarget(owner.getPos(), target.getVelocity(), target.getYaw(), target.getPitch()));
        }
        super.onEntityHit(entityHitResult);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        Entity e = this.getOwner();
        if (e instanceof LivingEntity entity) {
            int i = -1;
            if (entity.hasStatusEffect(RecallEffect.RECALL_EFFECT)) {
                StatusEffectInstance sei = entity.getStatusEffect(RecallEffect.RECALL_EFFECT);
                i = sei.getAmplifier();
            }
            RecallEffect.inflict(entity,20 * TotalRecall.RECALL_PEARL_SECONDS, i + 1);
            super.onCollision(hitResult);
        }
    }
}
