package com.miir.totalrecall.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatusEffectInstance.class)
public interface StatusEffectInstanceAccessor {
    @Accessor
    void setDuration(int duration);

    @Accessor
    StatusEffectInstance getHiddenEffect();

    @Accessor
    void setHiddenEffect(StatusEffectInstance hiddenEffect);
}
