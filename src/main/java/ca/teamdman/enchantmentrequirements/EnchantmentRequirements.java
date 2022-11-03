package ca.teamdman.enchantmentrequirements;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EnchantmentRequirements.MODID)
public class EnchantmentRequirements {
	public static final  String MODID      = "enchantmentrequirements";
	private static final Logger LOGGER     = LogUtils.getLogger();
	public EnchantmentRequirements() {}
}
