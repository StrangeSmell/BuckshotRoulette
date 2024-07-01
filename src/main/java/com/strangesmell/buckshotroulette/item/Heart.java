package com.strangesmell.buckshotroulette.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.swing.plaf.basic.BasicComboBoxUI;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;
import static com.strangesmell.buckshotroulette.block.TableBlock.uuid;

public class Heart extends Item {
    public Heart(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        itemstack.shrink(1);
        if(level.isClientSide){
            InteractionResultHolder.consume(player.getItemInHand(hand));
        }else{
            double amount = 0;
            if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid) != null) {
                amount = player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid).getAmount();
                player.getAttribute(Attributes.MAX_HEALTH).removePermanentModifier(uuid);
            }
            AttributeModifier modifier = new AttributeModifier(uuid, MODID + "modifier", amount + 1, AttributeModifier.Operation.ADDITION);
            player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(modifier);

        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));

    }
}
