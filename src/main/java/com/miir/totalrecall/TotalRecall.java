package com.miir.totalrecall;

import com.miir.totalrecall.entity.RecallPearlEntity;
import com.miir.totalrecall.entity.effect.RecallEffect;
import com.miir.totalrecall.item.RecallPearlItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TotalRecall implements ModInitializer {
    public static final String MOD_ID = "total-recall";

    public static final Item RECALL_PEARL_ITEM = new RecallPearlItem(new Item.Settings().maxCount(16));

    public static final int RECALL_PEARL_SECONDS = 10;
    public static EntityType<RecallPearlEntity> RECALL_PEARL_ENTITY;


    @Override
    public void onInitialize() {
        RecallEffect.register();
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "recall_pearl"), RECALL_PEARL_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.ENDER_PEARL, RECALL_PEARL_ITEM);
        });
        RECALL_PEARL_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier(MOD_ID, "recall_pearl"),
                FabricEntityTypeBuilder.<RecallPearlEntity>create(SpawnGroup.MISC, RecallPearlEntity::new)
                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                        .trackRangeBlocks(4).trackedUpdateRate(10)
                        .build());
    }
}
