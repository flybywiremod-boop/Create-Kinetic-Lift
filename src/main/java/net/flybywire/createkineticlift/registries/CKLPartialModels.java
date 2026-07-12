package net.flybywire.createkineticlift.registries;

import net.flybywire.createkineticlift.CreateKineticLift;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class CKLPartialModels {

	public static final PartialModel
		TURBOFAN_INTAKE_CONE = partial("regular_turbofan_intake_cone"),
		IRON_BLADE = partial("iron_blade");

//		HYDRAULIC_ACTUATOR = partial("hydraulic_actuator"),
//		DOUBLE_WHEEL = partial("double_wheel"),
//		NOSE_BIG_OLEO = partial("nose_big_oleo"),
//		NOSE_SMALL_OLEO = partial("nose_small_oleo"),
//		TRIPLE_WHEEL = partial("triple_wheel");


	private static PartialModel block(final String path) {
		return PartialModel.of(CreateKineticLift.asResource("block/" + path));
	}

	private static PartialModel entity(final String path) {
		return PartialModel.of(CreateKineticLift.asResource("entity/" + path));
	}

	private static PartialModel item(final String path) {
		return PartialModel.of(CreateKineticLift.asResource("item/" + path));
	}

	private static PartialModel partial(final String path) {
		return PartialModel.of(CreateKineticLift.asResource("partial/" + path));
	}

	public static void init() {
	}
}
