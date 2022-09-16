package ca.teamdman.enchantmentrequirements;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EnchantmentRequirements.MODID)
public class EnchantmentRequirements
{
    public static final String MODID = "enchantmentrequirements";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EnchantmentRequirements()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

//    // You can use SubscribeEvent and let the Event Bus discover methods to call
//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event)
//    {
//        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
//    }
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof EnchantedBookItem)) return;
        var enchants = EnchantmentHelper.getEnchantments(event.getItemStack());
        if (enchants.size() != 1) return;
        var entry = enchants.entrySet().iterator().next();
        var ench = entry.getKey();
        var level = entry.getValue();
        var minCost = ench.getMinCost(level);
        var maxCost = ench.getMaxCost(level);
        event.getToolTip().add(Component.translatable("tooltip.enchantmentrequirements.cost", minCost, maxCost));
    }
}
