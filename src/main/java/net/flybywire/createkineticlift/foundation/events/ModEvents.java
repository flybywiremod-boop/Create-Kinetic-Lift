package net.flybywire.createkineticlift.foundation.events;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.content.turbofan.TurbofanStructuralBlockEntity;
import net.flybywire.createkineticlift.foundation.data.CKLDamageTypeTagGen;
import net.flybywire.createkineticlift.foundation.data.CKLGeneratedEntriesProvider;
import net.flybywire.createkineticlift.foundation.data.CKLRegistrateTags;
import net.flybywire.createkineticlift.registries.CKLKeys;
import net.flybywire.createkineticlift.registries.CKLSoundEvents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CreateKineticLift.MOD_ID)
public class ModEvents {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void gatherData(GatherDataEvent event) {
		CKLRegistrateTags.addGenerators();

		CreateKineticLift.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
			JsonElement jsonElement = FilesHelper.loadJsonResource("assets/createkineticlift/lang/en_us.json");
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
				provider.add(entry.getKey(), entry.getValue().getAsString());

			CKLKeys.provideLang(provider::add);
			CKLSoundEvents.provideLang(provider::add);
		});

		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeClient(), CKLSoundEvents.provider(generator));

		CKLGeneratedEntriesProvider generatedEntriesProvider =
			new CKLGeneratedEntriesProvider(output, lookupProvider);

		lookupProvider = generatedEntriesProvider.getRegistryProvider();

		generator.addProvider(event.includeServer(), generatedEntriesProvider);
		generator.addProvider(event.includeServer(),
			new CKLDamageTypeTagGen(output, lookupProvider, existingFileHelper));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		TurbofanStructuralBlockEntity.registerCapabilities(event);
	}
}
