package com.strangesmell.buckshotroulette;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class Util {
    public static ItemStack remove(int index, NonNullList<ItemStack> items){
        ItemStack itemStack = items.get(index);
        for(int i =index;i<items.size();i++){
            if(i==items.size()-1){
                items.set(i,ItemStack.EMPTY);
            }else{
                if(i+1==items.size()){
                    items.set(i,ItemStack.EMPTY);
                }else{
                    items.set(i,items.get(i+1));
                }
            }
        }
        return itemStack;
    }

    //example Mth.lerp(pPartialTick, tntStartTime/roundBeginTimeMax,oTntStartTime/roundBeginTimeMax)
    public static Vec2 move(float x, float z, float x2, float z2,float tick){
        float f1 =Mth.lerp(tick , x,x2);
        f1=f1*f1*f1;
        float f2 =Mth.lerp(tick , z,z2);
        f2=f2*f2*f2;
        return new Vec2(f1,f2);
    }

}
