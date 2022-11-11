package ca.teamdman.enchantmentrequirements;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static final ForgeConfigSpec                      SPEC;
	public static final ForgeConfigSpec.ConfigValue<Integer> MIN_ENCHANTMENT_VALUE_TO_SHOW;
	public static final ForgeConfigSpec.Builder              BUILDER = new ForgeConfigSpec.Builder();

	static {
		MIN_ENCHANTMENT_VALUE_TO_SHOW = BUILDER.define("min_enchantment_value_to_show", 2);
		SPEC = BUILDER.build();
	}
}