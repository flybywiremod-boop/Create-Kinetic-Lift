package net.flybywire.createkineticlift.block.custom;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.flybywire.createkineticlift.CreateKineticLift;
import net.flybywire.createkineticlift.entity.ModEntities;
import net.flybywire.createkineticlift.entity.custom.controlseat.ControlSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ControlBlock extends Block implements IWrenchable {
    public ControlBlock(Properties pProperties) {
        super(pProperties);
    }

    public BlockState rotate(BlockState pState, Rotation pRot) {
        return (BlockState) pState.setValue(FACING, pRot.rotate((Direction) pState.getValue(FACING)));
    }

    public static final DirectionProperty FACING;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    private static final VoxelShape NORTH_OFF = Block.box(0, 0, 0, 19, 13, 16);
    private static final VoxelShape EAST_OFF = Block.box(0, 0, 0, 16, 13, 19);
    private static final VoxelShape SOUTH_OFF = Block.box(-3, 0, 0, 16, 13, 16);
    private static final VoxelShape WEST_OFF = Block.box(0, 0, -3, 16, 13, 16);

    private static final VoxelShape NORTH_ON = Block.box(-3, 0, 0, 16, 13, 16);
    private static final VoxelShape EAST_ON = Block.box(0, 0, -3, 16, 13, 16);

    private static final VoxelShape SOUTH_ON = Block.box(0, 0, 0, 19, 13, 16);
    private static final VoxelShape WEST_ON = Block.box(0, 0, 0, 16, 13, 19);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        Boolean invertedModel = pState.getValue(INVERTED);
        if (invertedModel) {
            return switch (direction) {
                case NORTH -> NORTH_ON;
                case EAST -> EAST_ON;
                case SOUTH -> SOUTH_ON;
                case WEST -> WEST_ON;
                default -> Block.box(0, 0, 0, 1, 1, 1);
            };
        } else {
            return switch (direction) {
                case NORTH -> NORTH_OFF;
                case EAST -> EAST_OFF;
                case SOUTH -> SOUTH_OFF;
                case WEST -> WEST_OFF;
                default -> Block.box(0, 0, 0, 1, 1, 1);
            };
        }
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation((Direction) pState.getValue(FACING)));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {


        if (!pLevel.isClientSide) {


            AABB box = new AABB(pPos);

            if (pPlayer.getMainHandItem().is(AllItems.WRENCH.asItem())) {
                pLevel.setBlock(pPos, pState.setValue(INVERTED, !pState.getValue(INVERTED)), 3);
                AllSoundEvents.WRENCH_ROTATE.playOnServer(pLevel, pPos, 1.0F, Create.RANDOM.nextFloat() + 0.5F);
            } else if ((pLevel.getEntitiesOfClass(ControlSeatEntity.class, box).isEmpty())) {

                ServerLevel serverLevel = (ServerLevel) pLevel;

                ControlSeatEntity controlSeatEntity = ModEntities.CONSTROL_SEAT.get().create(serverLevel);


                if (controlSeatEntity != null) {

                    Direction direction = pState.getValue(FACING);
                    Boolean invertedModel = pState.getValue(INVERTED);

                    Vec3 pos = pPos.getCenter().add(0, -0.25f, 0);
                    Vec3 north = pos.add(0.25f, 0, 0);
                    Vec3 east = pos.add(0, 0, 0.25f);
                    Vec3 south = pos.add(-0.25f, 0, 0);
                    Vec3 west = pos.add(0, 0, -0.25f);
                    if (invertedModel == Boolean.FALSE) {
                        switch (direction) {
                            case NORTH -> controlSeatEntity.setPos(north);
                            case EAST -> controlSeatEntity.setPos(east);
                            case SOUTH -> controlSeatEntity.setPos(south);
                            case WEST -> controlSeatEntity.setPos(west);
                            default -> controlSeatEntity.setPos(pos);
                        }
                    } else {
                        switch (direction) {
                            case NORTH -> controlSeatEntity.setPos(south);
                            case EAST -> controlSeatEntity.setPos(west);
                            case SOUTH -> controlSeatEntity.setPos(north);
                            case WEST -> controlSeatEntity.setPos(east);
                            default -> controlSeatEntity.setPos(pos);
                        }
                    }
                    serverLevel.addFreshEntity(controlSeatEntity);

                    pPlayer.startRiding(controlSeatEntity, true);
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(INVERTED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(INVERTED);
    }

    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
    }
}