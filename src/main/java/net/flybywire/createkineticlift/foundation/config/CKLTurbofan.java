package net.flybywire.createkineticlift.foundation.config;

import net.createmod.catnip.config.ConfigBase;

public class CKLTurbofan extends ConfigBase {

	public final ConfigBool needsFuel =
		b(true, "needsFuel", Comments.needsFuel);

	public final ConfigFloat maxThrust =
		f(14500.0f, 0.0f, "maxThrust", "In pN", Comments.maxThrust);

	@Override
	public String getName() {
		return "turbofan";
	}

	private static class Comments {
		static String needsFuel =
			"Whether Turbofans require and consume fuel to operate.";

		static String maxThrust =
			"The maximum thrust produced by a Turbofan at full throttle and max blade efficiency.";
	}
}
