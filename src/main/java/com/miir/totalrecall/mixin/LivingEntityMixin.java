package com.miir.totalrecall.mixin;

import com.miir.totalrecall.RecallAccessor;
import com.miir.totalrecall.entity.effect.RecallEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements RecallAccessor {
    private final Stack<Vec3d> totalrecall_recallPositions = new Stack<>();
    private final Stack<RegistryKey<World>> totalrecall_recallDimensions = new Stack<>();
    private final Stack<Integer> totalrecall_recallPrevTicks = new Stack<>();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void totalrecall_serializeRecallPosition(NbtCompound nbt, CallbackInfo ci) {
        int l = totalrecall_recallPositions.size();
        if (l > 0) {
            List<Long> x = new ArrayList<>();
            List<Long> y = new ArrayList<>();
            List<Long> z = new ArrayList<>();
            for (Vec3d pos : totalrecall_recallPositions) {
                x.add(Double.doubleToLongBits(pos.x));
                y.add(Double.doubleToLongBits(pos.y));
                z.add(Double.doubleToLongBits(pos.z));
            }
            StringBuilder s = new StringBuilder();
            for (RegistryKey<World> dim :
                    totalrecall_recallDimensions) {
                s.append(dim.getValue().toString());
                s.append(",");
            }
            nbt.putLongArray("TotalRecall_RecallPositionsX", x);
            nbt.putLongArray("TotalRecall_RecallPositionsY", y);
            nbt.putLongArray("TotalRecall_RecallPositionsZ", z);
            nbt.putString("TotalRecall_RecallDimensions", s.toString());
            nbt.putIntArray("TotalRecall_RecallPrevTicks", new ArrayList<>(totalrecall_recallPrevTicks));
        }
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void totalrecall_deserializeRecallPosition(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("TotalRecall_RecallDimensions")) {
            String[] dims = StringUtils.split(nbt.getString("TotalRecall_RecallDimensions"), ",");
            long[] xl = nbt.getLongArray("TotalRecall_RecallPositionsX");
            long[] yl = nbt.getLongArray("TotalRecall_RecallPositionsY");
            long[] zl = nbt.getLongArray("TotalRecall_RecallPositionsZ");
            int[] prevTicks = nbt.getIntArray("TotalRecall_RecallPrevTicks");
            for (int i = 0; i < xl.length; i++) {
                double x = Double.longBitsToDouble(xl[i]);
                double y = Double.longBitsToDouble(yl[i]);
                double z = Double.longBitsToDouble(zl[i]);
                this.totalrecall_recallPositions.push(new Vec3d(x, y, z));
            }
            for (String s : dims) {
                if (s.equals("")) continue;
                this.totalrecall_recallDimensions.push(RegistryKey.of(RegistryKeys.WORLD, new Identifier(s)));
            }
            for (int i :
                    prevTicks) {
                this.totalrecall_recallPrevTicks.push(i);
            }
        }
    }
    @Override
    public RecallEffect.Recollection totalrecall_popRecollection() {
        return new RecallEffect.Recollection(this.totalrecall_recallPositions.pop(), this.totalrecall_recallDimensions.pop(), this.totalrecall_recallPrevTicks.pop());
    }
    @Override
    public void totalrecall_pushRecollection(RecallEffect.Recollection recollection) {
        this.totalrecall_recallPositions.push(recollection.pos());
        this.totalrecall_recallDimensions.push(recollection.key());
        this.totalrecall_recallPrevTicks.push(recollection.remainingTicks());
    }

    @Override
    public RecallEffect.Recollection totalrecall_peekRecollection() {
        return new RecallEffect.Recollection(this.totalrecall_recallPositions.peek(), this.totalrecall_recallDimensions.peek(), this.totalrecall_recallPrevTicks.peek());
    }

    @Override
    public void totalrecall_clearRecollections() {
        this.totalrecall_recallPrevTicks.clear();
        this.totalrecall_recallDimensions.clear();
        this.totalrecall_recallPositions.clear();
    }
    @Override
    public int totalrecall_getStackLength() {
        return this.totalrecall_recallPositions.size();
    }
}
