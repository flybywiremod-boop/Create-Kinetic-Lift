package net.flybywire.createkineticlift.foundation.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.createmod.catnip.config.ConfigBase;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID)
public class CKLConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS =
		new EnumMap<>(ModConfig.Type.class);

	private static CKLServer server;

	public static CKLServer server() {
		return server;
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type type) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(type, config);

		return config;
	}

	public static void register(ModContainer container) {
		server = register(CKLServer::new, ModConfig.Type.SERVER);

		for (Map.Entry<ModConfig.Type, ConfigBase> entry : CONFIGS.entrySet())
			container.registerConfig(entry.getKey(), entry.getValue().specification);
	}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading event) {
		for (ConfigBase config : CONFIGS.values()) {
			if (config.specification == event.getConfig().getSpec())
				config.onLoad();
		}
	}

	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event) {
		for (ConfigBase config : CONFIGS.values()) {
			if (config.specification == event.getConfig().getSpec())
				config.onReload();
		}
	}
}
