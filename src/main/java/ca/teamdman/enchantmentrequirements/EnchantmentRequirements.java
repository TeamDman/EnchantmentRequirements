package ca.teamdman.enchantmentrequirements;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EnchantmentRequirements.MODID)
public class EnchantmentRequirements {
	public static final  String MODID      = "enchantmentrequirements";
	private static final Logger LOGGER     = LogUtils.getLogger();
	private              long   lastCheck  = 0;
	private              int    tablePower = 0;

	public EnchantmentRequirements() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		var stack = event.getItemStack();
		var level = Minecraft.getInstance().level;
		var tip   = event.getToolTip();
		var pos   = Minecraft.getInstance().hitResult == null ? BlockPos.ZERO : new BlockPos(Minecraft.getInstance().hitResult.getLocation());
		if (stack.isEmpty() || level == null || pos.equals(BlockPos.ZERO)) return;
		handleBlockPower(event, stack, level, pos, tip);
		handleBookLevel(event, stack, level, pos, tip);
		handleItemPower(event, stack, level, pos, tip);
		handleTablePower(event, stack, level, pos, tip);
	}

	private void handleItemPower(ItemTooltipEvent event, ItemStack stack, ClientLevel level, BlockPos pos, List<Component> tip) {
		int enchantValue = stack.getEnchantmentValue();
		if (enchantValue == 0) return;
		tip.add(Component.translatable("tooltip.enchantmentrequirements.value", enchantValue));
	}

	private void handleBlockPower(ItemTooltipEvent event, ItemStack stack, ClientLevel level, BlockPos pos, List<Component> tip) {
		if (!(stack.getItem() instanceof BlockItem blockItem)) return;
		var power = blockItem.getBlock()
				.defaultBlockState()
				.getEnchantPowerBonus(level, pos);
		if (power > 0) {
			tip.add(Component.translatable("tooltip.enchantmentrequirements.power", power));
		}
	}

	private void handleTablePower(ItemTooltipEvent event, ItemStack stack, Level level, BlockPos pos, List<Component> tip) {
		if (!(Minecraft.getInstance().screen instanceof EnchantmentScreen es)) return;
		recalculateTablePower(level, pos);
		tip.add(Component.translatable("tooltip.enchantmentrequirements.table_power", tablePower)
						.withStyle(ChatFormatting.DARK_AQUA));
		int itemPower = stack.getEnchantmentValue();
		if (itemPower == 0) return;
		var pPower = Math.min(tablePower, 15);
		/**
		 * Do the math from {@link EnchantmentHelper#getEnchantmentCost(RandomSource, int, int, ItemStack)}
		 */
		int jLower     = 1 + (pPower >> 1);
		int jUpper     = 8 + (pPower >> 1) + pPower;
		int cost0Lower = Math.max(jLower / 3, 1);
		int cost0Upper = Math.max(jUpper / 3, 1);
		int cost1Lower = Math.max(jLower * 2 / 3 + 1, 2);
		int cost1Upper = Math.max(jUpper * 2 / 3 + 1, 2);
		int cost2Lower = Math.max(Math.max(jLower, pPower * 2), 3);
		int cost2Upper = Math.max(Math.max(jUpper, pPower * 2), 3);
		tip.add(Component.translatable("tooltip.enchantmentrequirements.cost_0", cost0Lower, cost0Upper)
						.withStyle(ChatFormatting.DARK_AQUA));
		tip.add(Component.translatable("tooltip.enchantmentrequirements.cost_1", cost1Lower, cost1Upper)
						.withStyle(ChatFormatting.DARK_AQUA));
		tip.add(Component.translatable("tooltip.enchantmentrequirements.cost_2", cost2Lower, cost2Upper)
						.withStyle(ChatFormatting.DARK_AQUA));
	}

	private void recalculateTablePower(Level level, BlockPos pos) {
		if (System.currentTimeMillis() <= lastCheck + 1000) return;
		if (level == null) {
			tablePower = -1;
		} else {
			tablePower = 0;
			for (BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
				if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockpos)) {
					tablePower += level.getBlockState(pos.offset(blockpos))
							.getEnchantPowerBonus(level, pos.offset(blockpos));
				}
			}
		}
		lastCheck = System.currentTimeMillis();
	}

	private void handleBookLevel(ItemTooltipEvent event, ItemStack stack, ClientLevel level, BlockPos pos, List<Component> tip) {
		if (!(stack.getItem() instanceof EnchantedBookItem)) return;
		var enchants = EnchantmentHelper.getEnchantments(stack);
		if (enchants.size() != 1) return;
		var entry = enchants.entrySet()
				.iterator()
				.next();
		var ench      = entry.getKey();
		var enchLevel = entry.getValue();
		var minCost   = ench.getMinCost(enchLevel);
		var maxCost   = ench.getMaxCost(enchLevel);
		tip.add(Component.translatable("tooltip.enchantmentrequirements.cost", minCost, maxCost));

		// if we have an enchanting table open
		// determine if the enchantment on the book could be rolled for the current item
		if (!(Minecraft.getInstance().screen instanceof EnchantmentScreen es)) return;
		var enchanting = es.getMenu().slots.get(0)
				.getItem();
		if (enchanting.isEmpty()) return;
		var itemPower = enchanting.getEnchantmentValue();
		/**
		 * Do the math from {@link EnchantmentHelper#getEnchantmentCost(RandomSource, int, int, ItemStack)}
		 */
		if (itemPower <= 0) return;
		int pPower = Math.min(tablePower, 15);
		int jLower     = 1 + (pPower >> 1);
		int jUpper     = 8 + (pPower >> 1) + pPower;
		int cost0Lower = Math.max(jLower / 3, 1);
		int cost2Upper = Math.max(Math.max(jUpper, pPower * 2), 3);
		/**
		 * Do the math from {@link EnchantmentHelper#selectEnchantment(RandomSource, ItemStack, int, boolean)}
		 */
		int pLevelLower = cost0Lower + 1;
		int pLevelUpper = cost2Upper + 1 + itemPower / 2;
		float fLower = -0.15F;
		float fUpper = 0.15F;
		pLevelLower = Mth.clamp(Math.round(pLevelLower + pLevelLower * fLower), 1, Integer.MAX_VALUE);
		pLevelUpper = Mth.clamp(Math.round(pLevelUpper + pLevelUpper * fUpper), 1, Integer.MAX_VALUE);
		tip.add(Component.translatable("tooltip.enchantmentrequirements.true_cost", pLevelLower, pLevelUpper).withStyle(ChatFormatting.DARK_AQUA));

		if (!ench.canEnchant(enchanting) && !(enchanting.getItem() instanceof BookItem)) {
			tip.add(Component.translatable("tooltip.enchantmentrequirements.cant_enchant").withStyle(ChatFormatting.RED));
		}
	}
}
