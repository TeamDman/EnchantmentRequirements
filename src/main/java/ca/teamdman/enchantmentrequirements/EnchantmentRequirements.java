package ca.teamdman.enchantmentrequirements;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(EnchantmentRequirements.MODID)
public class EnchantmentRequirements {
	public static final  String MODID      = "enchantmentrequirements";
	private static final Logger LOGGER     = LogUtils.getLogger();
	public EnchantmentRequirements() {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()->{
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
		});
	}

}
