package net.flybywire.createkineticlift.foundation.config;

import net.createmod.catnip.config.ConfigBase;

public class CKLServer extends ConfigBase {

	public final CKLTurbofan turbofan =
		nested(0, CKLTurbofan::new, Comments.turbofan);

	@Override
	public String getName() {
		return "server";
	}

	private static class Comments {
		static String turbofan = "Turbofan gameplay settings";
	}
}
