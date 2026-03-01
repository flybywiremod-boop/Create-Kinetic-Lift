package net.flybywire.createkineticlift.content.turbofan;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.flybywire.createkineticlift.content.turbofan.blades.TurbofanBladeItem;
import net.flybywire.createkineticlift.registries.KineticBlockEntities;
import net.flybywire.createkineticlift.registries.KineticShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TurbofanIntakeBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<TurbofanBlockEntity> {

    public TurbofanIntakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder
                .add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KineticShapes.TURBOFAN_INTAKE.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KineticShapes.TURBOFAN_INTAKE.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TurbofanBlockEntity turbofanBE))
            return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() instanceof TurbofanBladeItem) {
            if (!level.isClientSide) {
                if (turbofanBE.addBlade(heldItem)) {
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.0f, 1.2f);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (heldItem.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack removedBlade = turbofanBE.removeBlade();
                if (!removedBlade.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(removedBlade);
                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == getBlockEntityType()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }

    @Override
    public Class<TurbofanBlockEntity> getBlockEntityClass() {
        return TurbofanBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TurbofanBlockEntity> getBlockEntityType() {
        return KineticBlockEntities.TURBOFAN.get();
    }
}