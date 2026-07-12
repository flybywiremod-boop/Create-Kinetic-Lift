package net.flybywire.createkineticlift.content.turbofan;

import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TurbofanExhaustBlockItem extends BlockItem {

	public TurbofanExhaustBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		Direction direction = context.getHorizontalDirection();
		BlockPos targetPos = (direction == clickedFace.getOpposite())
			? context.getClickedPos().relative(clickedFace)
			: context.getClickedPos().relative(clickedFace).relative(direction);
		InteractionResult result = super.place(BlockPlaceContext.at(context, targetPos, clickedFace));
		if (result == InteractionResult.FAIL && context.getLevel().isClientSide())
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBounds(context));
		return result;
	}

	@OnlyIn(Dist.CLIENT)
	public void showBounds(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		Direction direction = context.getHorizontalDirection();
		BlockPos targetPos = (direction == clickedFace.getOpposite())
			? context.getClickedPos().relative(clickedFace)
			: context.getClickedPos().relative(clickedFace).relative(direction);
		if (!(context.getPlayer() instanceof LocalPlayer localPlayer))
			return;
		Outliner.getInstance().showAABB(Pair.of("turbofan", targetPos), new AABB(targetPos).inflate(1))
			.colored(0xFF_ff5d6c);
		// The following text is context neutral and does not explicitly reference the Waterwheel, which makes it okay to use.
		// (This is the case for english, I don't know about the other languages so let's assume the translators did a good job.)
		CreateLang.translate("large_water_wheel.not_enough_space")
			.color(0xFF_ff5d6c)
			.sendStatus(localPlayer);
	}
}
