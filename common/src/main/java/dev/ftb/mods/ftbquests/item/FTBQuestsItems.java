package dev.ftb.mods.ftbquests.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.block.FTBQuestsBlocks;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.item.ScreenBlockItem.ScreenSize;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FTBQuestsItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(FTBQuests.MOD_ID, Registries.ITEM);

	public static final RegistrySupplier<Item> BOOK = ITEMS.register("book", QuestBookItem::new);
	public static final RegistrySupplier<Item> LOOTCRATE = ITEMS.register("lootcrate", LootCrateItem::new);
	public static final RegistrySupplier<Item> MISSING_ITEM = ITEMS.register("missing_item", MissingItem::new);
	public static final RegistrySupplier<Item> CUSTOM_ICON = ITEMS.register("custom_icon", CustomIconItem::new);

	private static RegistrySupplier<Item> blockItem(String id, Supplier<Block> b) {
		return ITEMS.register(id, () -> new BlockItem(b.get(), defaultProps()));
	}

	private static RegistrySupplier<Item> blockItemFor(String id, Supplier<BlockItem> bi) {
		return ITEMS.register(id, bi);
	}

	public static final RegistrySupplier<Item> BARRIER = ITEMS.register("barrier", QuestBarrierBlockItem::new);
	public static final RegistrySupplier<Item> STAGE_BARRIER = ITEMS.register("stage_barrier", StageBarrierBlockItem::new);
	public static final RegistrySupplier<Item> DETECTOR = blockItem("detector", FTBQuestsBlocks.DETECTOR);
	public static final RegistrySupplier<Item> LOOT_CRATE_OPENER = blockItem("loot_crate_opener", FTBQuestsBlocks.LOOT_CRATE_OPENER);

	public static final RegistrySupplier<Item> TASK_SCREEN_1 = blockItemFor("screen_1",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_1.get(), ScreenSize.ONE_X_ONE));
	public static final RegistrySupplier<Item> TASK_SCREEN_3 = blockItemFor("screen_3",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_3.get(), ScreenSize.THREE_X_THREE));
	public static final RegistrySupplier<Item> TASK_SCREEN_5 = blockItemFor("screen_5",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_5.get(), ScreenSize.FIVE_X_FIVE));
	public static final RegistrySupplier<Item> TASK_SCREEN_7 = blockItemFor("screen_7",
			() -> new ScreenBlockItem(FTBQuestsBlocks.TASK_SCREEN_7.get(), ScreenSize.SEVEN_X_SEVEN));


	public static void register() {
		ITEMS.register();
	}

	public static final RegistrySupplier<CreativeModeTab> CREATIVE_TAB = RegistrarManager.get(FTBQuests.MOD_ID)
			.get(Registries.CREATIVE_MODE_TAB)
			.register(new ResourceLocation(FTBQuests.MOD_ID, "default"), FTBQuestsItems::buildDefaultTab);

	public static Item.Properties defaultProps() {
		return new Item.Properties();
	}

	private static CreativeModeTab buildDefaultTab() {
		List<ItemStack> baseItems = Stream.of(
				BOOK,
				BARRIER,
				STAGE_BARRIER,
				DETECTOR,
				LOOT_CRATE_OPENER,
				TASK_SCREEN_1,
				TASK_SCREEN_3,
				TASK_SCREEN_5,
				TASK_SCREEN_7
		).map(item -> new ItemStack(item.get())).toList();

		return CreativeTabRegistry.create(builder -> builder.title(Component.translatable("ftbquests"))
				.icon(() -> new ItemStack(BOOK.get()))
				.displayItems((params, output) -> {
					// base items, always present
					output.acceptAll(baseItems);
					// dynamically add loot crates based on current reward tables
					if (ClientQuestFile.exists()) {
						ClientQuestFile.INSTANCE.getRewardTables().forEach(table -> {
							if (table.getLootCrate() != null) {
								output.accept(table.getLootCrate().createStack());
							}
						});
					}
				})
		);
	}
}