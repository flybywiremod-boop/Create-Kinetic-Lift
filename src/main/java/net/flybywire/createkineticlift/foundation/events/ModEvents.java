package net.flybywire.createkineticlift.foundation.events;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID)
public class ModEvents {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void gatherData(GatherDataEvent event) {

		CreateKineticLift.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {

			JsonElement jsonElement = FilesHelper.loadJsonResource("assets/createkineticlift/lang/en_us.json");
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
				provider.add(entry.getKey(), entry.getValue().getAsString());
		});
	}
}
