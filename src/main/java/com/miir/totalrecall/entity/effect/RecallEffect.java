package com.miir.totalrecall.entity.effect;

import com.miir.totalrecall.RecallAccessor;
import com.miir.totalrecall.TotalRecall;
import com.miir.totalrecall.mixin.StatusEffectInstanceAccessor;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.EmptyStackException;
import java.util.Optional;

public class RecallEffect extends StatusEffect {
    public static final RecallEffect RECALL_EFFECT = new RecallEffect(StatusEffectCategory.BENEFICIAL, 700881);
    protected RecallEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(TotalRecall.MOD_ID, "recall"), RECALL_EFFECT);
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }

    private static StatusEffectInstance addDuration(StatusEffectInstance instance, int duration) {
        StatusEffectInstance hidden = ((StatusEffectInstanceAccessor) instance).getHiddenEffect();
        if (hidden != null) {
            hidden = addDuration(hidden, duration);
            ((StatusEffectInstanceAccessor) instance).setHiddenEffect(hidden);
        }
        int d = instance.getDuration();
        ((StatusEffectInstanceAccessor) instance).setDuration(d + duration);
        return instance;
    }

    public static void inflict(LivingEntity entity, int duration, int amplifier) {
        if (amplifier == 0) ((RecallAccessor) entity).totalrecall_clearRecollections();
        Recollection r = new Recollection(
                entity.getPos(),
                entity.world.getRegistryKey(),
                entity.getStatusEffect(RECALL_EFFECT) == null ? 0 : entity.getStatusEffect(RECALL_EFFECT).getDuration()
        );
        ((RecallAccessor) entity).totalrecall_pushRecollection(r);
        StatusEffectInstance effectInstance = entity.getStatusEffect(RECALL_EFFECT);
        if (effectInstance != null) {
            entity.addStatusEffect(
                    new StatusEffectInstance(
                            RECALL_EFFECT, duration, amplifier,
                            false, true, true,
                            addDuration(effectInstance, duration),
                            Optional.empty()));
        } else {
            entity.addStatusEffect(new StatusEffectInstance(RECALL_EFFECT, duration, amplifier, false, true));
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
//        System.out.println(amplifier);
//        System.out.println(((RecallAccessor) entity).totalrecall_getStackLength());
        if (entity.getStatusEffect(RECALL_EFFECT) == null
                || (amplifier == ((RecallAccessor) entity).totalrecall_getStackLength() - 2)) { // ensure that the effect was not superseded

            if (entity.world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + entity.world.random.nextDouble() * 2.0, entity.getZ(), 32, 0, entity.world.random.nextGaussian(), 0, 1);
            }
            try {
                Recollection r = ((RecallAccessor) entity).totalrecall_popRecollection();
                if (entity.hasVehicle()) {
                    entity.dismountVehicle();
                }
                SoundEvent soundEvent = entity instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.PLAYERS, 1.0f, 1.0f);
                if (entity.world instanceof ServerWorld world) {
                    FabricDimensions.teleport(entity, world.getServer().getWorld(r.key()), new TeleportTarget(r.pos(), entity.getVelocity(), entity.getYaw(), entity.getPitch()));
                }
                entity.playSound(soundEvent, 1.0f, 1.0f);
                if (entity.getStatusEffect(RECALL_EFFECT) == null) {
                    ((RecallAccessor) entity).totalrecall_clearRecollections();
                }
            } catch (EmptyStackException e) {
                entity.playSound(SoundEvents.ENTITY_ENDERMITE_HURT, 1, 1);
            }
        }
    }

    // descriptive names < fun names
    public static record Recollection(Vec3d pos, RegistryKey<World> key, int remainingTicks) {}
}
