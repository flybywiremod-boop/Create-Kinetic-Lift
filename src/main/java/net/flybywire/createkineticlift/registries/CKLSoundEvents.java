package net.flybywire.createkineticlift.registries;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.flybywire.createkineticlift.CreateKineticLift;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class CKLSoundEvents {

	public static final Map<ResourceLocation, SoundEntry> ALL = new HashMap<>();

	public static final SoundEntry TURBOFAN_LOOP = create("turbofan_loop")
		.subtitle("Turbofan runs")
		.category(SoundSource.BLOCKS)
		.attenuationDistance(64)
		.build();

	private CKLSoundEvents() {
	}

	private static SoundEntryBuilder create(String name) {
		return create(CreateKineticLift.asResource(name));
	}

	public static SoundEntryBuilder create(ResourceLocation id) {
		return new SoundEntryBuilder(id);
	}

	public static void prepare() {
		for (SoundEntry entry : ALL.values())
			entry.prepare();
	}

	public static void register(RegisterEvent event) {
		event.register(Registries.SOUND_EVENT, helper -> {
			for (SoundEntry entry : ALL.values())
				entry.register(helper);
		});
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (SoundEntry entry : ALL.values())
			if (entry.hasSubtitle())
				consumer.accept(entry.getSubtitleKey(), entry.getSubtitle());
	}

	public static SoundEntryProvider provider(DataGenerator generator) {
		return new SoundEntryProvider(generator);
	}

	public static class SoundEntryProvider implements DataProvider {

		private final PackOutput output;

		public SoundEntryProvider(DataGenerator generator) {
			output = generator.getPackOutput();
		}

		@Override
		public CompletableFuture<?> run(CachedOutput cache) {
			return generate(output.getOutputFolder(), cache);
		}

		@Override
		public String getName() {
			return "Create: Kinetic Lift's Custom Sounds";
		}

		private CompletableFuture<?> generate(Path path, CachedOutput cache) {
			path = path.resolve("assets").resolve(CreateKineticLift.MOD_ID);

			JsonObject json = new JsonObject();

			ALL.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> entry.getValue().write(json));

			return DataProvider.saveStable(cache, json, path.resolve("sounds.json"));
		}
	}

	public static class SoundEntryBuilder {

		private final ResourceLocation id;
		private final List<ResourceLocation> variants = new ArrayList<>();

		private String subtitle = "unregistered";
		private SoundSource category = SoundSource.BLOCKS;
		private int attenuationDistance;

		private SoundEntryBuilder(ResourceLocation id) {
			this.id = id;
		}

		public SoundEntryBuilder subtitle(String subtitle) {
			this.subtitle = subtitle;
			return this;
		}

		public SoundEntryBuilder noSubtitle() {
			subtitle = null;
			return this;
		}

		public SoundEntryBuilder category(SoundSource category) {
			this.category = category;
			return this;
		}

		public SoundEntryBuilder attenuationDistance(int distance) {
			attenuationDistance = distance;
			return this;
		}

		public SoundEntryBuilder addVariant(String name) {
			return addVariant(CreateKineticLift.asResource(name));
		}

		public SoundEntryBuilder addVariant(ResourceLocation id) {
			variants.add(id);
			return this;
		}

		public SoundEntry build() {
			SoundEntry entry = new SoundEntry(id, variants, subtitle, category, attenuationDistance);
			ALL.put(id, entry);
			return entry;
		}
	}

	public static class SoundEntry {

		private final ResourceLocation id;
		private final List<ResourceLocation> variants;
		private final String subtitle;
		private final SoundSource category;
		private final int attenuationDistance;

		private DeferredHolder<SoundEvent, SoundEvent> event;

		private SoundEntry(ResourceLocation id, List<ResourceLocation> variants, String subtitle,
						   SoundSource category, int attenuationDistance) {
			this.id = id;
			this.variants = List.copyOf(variants);
			this.subtitle = subtitle;
			this.category = category;
			this.attenuationDistance = attenuationDistance;
		}

		private void prepare() {
			event = DeferredHolder.create(Registries.SOUND_EVENT, id);
		}

		private void register(RegisterEvent.RegisterHelper<SoundEvent> helper) {
			ResourceLocation location = event.getId();
			helper.register(location, SoundEvent.createVariableRangeEvent(location));
		}

		private void write(JsonObject json) {
			JsonObject entry = new JsonObject();
			JsonArray sounds = new JsonArray();

			sounds.add(createSoundDefinition(id));

			for (ResourceLocation variant : variants)
				sounds.add(createSoundDefinition(variant));

			entry.add("sounds", sounds);

			if (hasSubtitle())
				entry.addProperty("subtitle", getSubtitleKey());

			json.add(id.getPath(), entry);
		}

		private JsonObject createSoundDefinition(ResourceLocation soundId) {
			JsonObject sound = new JsonObject();
			sound.addProperty("name", soundId.toString());
			sound.addProperty("type", "file");

			if (attenuationDistance != 0)
				sound.addProperty("attenuation_distance", attenuationDistance);

			return sound;
		}

		public Holder<SoundEvent> getMainEventHolder() {
			return event;
		}

		public SoundEvent getMainEvent() {
			return event.get();
		}

		public ResourceLocation getId() {
			return id;
		}

		public String getSubtitleKey() {
			return id.getNamespace() + ".subtitle." + id.getPath();
		}

		public boolean hasSubtitle() {
			return subtitle != null;
		}

		public String getSubtitle() {
			return subtitle;
		}

		public SoundSource getCategory() {
			return category;
		}

		public void playOnServer(Level level, Vec3i pos) {
			playOnServer(level, pos, 1.0f, 1.0f);
		}

		public void playOnServer(Level level, Vec3i pos, float volume, float pitch) {
			play(level, null, pos, volume, pitch);
		}

		public void play(Level level, Player player, Vec3i pos, float volume, float pitch) {
			play(level, player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
		}

		public void play(Level level, Player player, Vec3 pos, float volume, float pitch) {
			play(level, player, pos.x, pos.y, pos.z, volume, pitch);
		}

		public void play(Level level, Player player, double x, double y, double z, float volume, float pitch) {
			level.playSound(player, x, y, z, event.get(), category, volume, pitch);
		}

		public void playAt(Level level, Vec3 pos, float volume, float pitch, boolean distanceDelay) {
			level.playLocalSound(pos.x, pos.y, pos.z, event.get(), category, volume, pitch, distanceDelay);
		}
	}
}
