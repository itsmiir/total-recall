package com.miir.totalrecall.mixin;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin extends HandledScreen {
    public AbstractInventoryScreenMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }

    @Inject(method = "getStatusEffectDescription", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void totalrecall_extendPotionRomanNumerals(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir, MutableText mutableText) {
        if (statusEffect.getAmplifier() >= 10) {
            mutableText.append(" ").append(Text.literal(romanNumeral(statusEffect.getAmplifier() +1)));
        }
    }
    private static String romanNumeral(int num) {
        switch (num) {
            case 0:
                return "Nulla";
//            roman numerals don't have 0, but apparently they used that
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            case 50:
                return "L";
            case 100:
                return "C";
            case 500:
                return "D";
            case 1000:
                return "M";
            default:
                if (num > 3999) {
                    return ((Integer) num).toString();
                }
                return (
                        THOUS[num / 1000]
                                + HUNDS[num / 100 % 10]
                                + TENS[num / 10 % 10]
                                + ONES[num % 10]
                );
        }
    }
    private static final String[] THOUS = new String[] {
            "", "M", "MM", "MMM"
    };
    private static final String[] HUNDS = new String[] {
            "", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"
    };
    private static final String[] TENS = new String[] {
            "", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"
    };
    private static final String[] ONES = new String[] {
            "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"
    };
}
