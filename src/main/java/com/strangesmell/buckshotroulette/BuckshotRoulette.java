package com.strangesmell.buckshotroulette;

import com.mojang.datafixers.DSL;
import com.strangesmell.buckshotroulette.block.TableBlockEntity;
import com.strangesmell.buckshotroulette.entity.Dealer;
import com.strangesmell.buckshotroulette.entity.DealerRenderer;
import com.strangesmell.buckshotroulette.item.Heart;
import com.strangesmell.buckshotroulette.structure.Room;
import com.strangesmell.buckshotroulette.structure.RoomStructurePieces;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.strangesmell.buckshotroulette.block.TableBlock.uuid;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuckshotRoulette.MODID)
public class BuckshotRoulette
{
    public static final String MODID = "buckshotroulette";
    public static List<Item> items = new ArrayList<>();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> TableBlock = BLOCKS.register("table_block", com.strangesmell.buckshotroulette.block.TableBlock::new);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final RegistryObject<BlockEntityType<com.strangesmell.buckshotroulette.block.TableBlockEntity>> TableBlockEntity = BLOCK_ENTITIES.register("table_block_entity", () -> BlockEntityType.Builder.of(TableBlockEntity::new, TableBlock.get()).build(DSL.remainderType()));


    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> TableBlockItem = ITEMS.register("table_item", () -> new BlockItem(TableBlock.get(), new Item.Properties()));
    public static final RegistryObject<Item> Heart = ITEMS.register("heart", () -> new Heart( new Item.Properties()));

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<Dealer>> Dealer = ENTITIES.register("dealer",
            ()->EntityType.Builder.of(com.strangesmell.buckshotroulette.entity.Dealer::new, MobCategory.MONSTER)
                    .sized(0.4f,1.4f)
                    .build(new ResourceLocation(MODID,"dealer").toString())
            );

    public static final RegistryObject<Item> DealerEgg = ITEMS.register("dealer_egg", () -> new ForgeSpawnEggItem(Dealer,0x22b314,0x19732e,new Item.Properties()));


    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
    public static final RegistryObject<StructureType<Room>> ROOM = registerType("deliciousness",() -> () -> Room.CODEC);
    private static <P extends Structure> RegistryObject<StructureType<P>> registerType(String name, Supplier<StructureType<P>> factory) {
        return STRUCTURE_TYPES.register(name, factory);
    }

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
    public static final RegistryObject<StructurePieceType> RoomPieceType = registerPieceType("deliciousness", RoomStructurePieces.RoomStructurePiece::new);
    private static RegistryObject< StructurePieceType> registerPieceType(String name, StructurePieceType.StructureTemplateType structurePieceType) {
        return STRUCTURE_PIECE_TYPES.register(name.toLowerCase(Locale.ROOT), () -> structurePieceType);
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("buckshot_roulette_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> TableBlockItem.get().getDefaultInstance())
            .title( Component.translatable("BuckshotRoulette"))
            .displayItems((parameters, output) -> {
                output.accept(TableBlockItem.get());
                output.accept(Heart.get());
                output.accept(DealerEgg.get());
            }).build());




    public BuckshotRoulette()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(this);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::entityAttributeEvent);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        STRUCTURE_PIECE_TYPES.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
        }
        modEventBus.addListener(this::addCreative);
        initItem();
        forgeEventBus.addListener(this::playerEvent);

    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(Dealer.get(), DealerRenderer::new);
    }

    public void entityAttributeEvent(EntityAttributeCreationEvent event){
        event.put(Dealer.get(), com.strangesmell.buckshotroulette.entity.Dealer.setAttributes());
    }

    public void playerEvent(PlayerEvent.Clone event)
    {
        if(event.isWasDeath()){
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            if(oldPlayer.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid)!=null){
                newPlayer.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(oldPlayer.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid));
            }

        }


    }

    public void initItem(){
        items.add(Items.SPYGLASS);//放大镜=望远镜
        items.add(Items.COBWEB);//手铐=蜘蛛网
        items.add(Items.POTATO);//过期药品=未知土豆
        items.add(Items.BLACK_DYE);//逆转器=染料
        items.add(Items.APPLE);//香烟=苹果
        items.add(Items.GUNPOWDER);//火药=锯子
        items.add(Items.FISHING_ROD);//肾上腺素=鱼竿
        items.add(Items.OBSERVER);//手机=侦测器
        items.add(Items.PISTON);//啤酒=活塞
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            // event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
