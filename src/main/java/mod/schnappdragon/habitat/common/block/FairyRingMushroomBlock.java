package mod.schnappdragon.habitat.common.block;

import mod.schnappdragon.habitat.common.block.state.properties.HabitatBlockStateProperties;
import mod.schnappdragon.habitat.core.Habitat;
import mod.schnappdragon.habitat.core.registry.HabitatParticleTypes;
import mod.schnappdragon.habitat.core.registry.HabitatSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

public class FairyRingMushroomBlock extends BushBlock implements BonemealableBlock {
    protected static final VoxelShape[] SHAPE = {Block.box(6.0D, 0.0D, 6.0D, 10.0D, 13.0D, 10.0D), Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)};

    public static final IntegerProperty MUSHROOMS = HabitatBlockStateProperties.MUSHROOMS_1_4;

    public FairyRingMushroomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MUSHROOMS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MUSHROOMS);
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE[state.getValue(MUSHROOMS) - 1];
    }

    public void animateTick(BlockState state, Level worldIn, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(9 - state.getValue(MUSHROOMS)) == 0)
            worldIn.addParticle(HabitatParticleTypes.FAIRY_RING_SPORE.get(), pos.getX() + rand.nextDouble(), pos.getY() + rand.nextDouble(), pos.getZ() + rand.nextDouble(), rand.nextGaussian() * 0.01D, 0.0D, rand.nextGaussian() * 0.01D);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this))
            return blockstate.setValue(MUSHROOMS, Math.min(4, blockstate.getValue(MUSHROOMS) + 1));

        return super.getStateForPlacement(context);
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(MUSHROOMS) < 4 || super.canBeReplaced(state, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).isSolidRender(worldIn, pos.below());
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (player.getItemInHand(handIn).canPerformAction(ToolActions.SHEARS_HARVEST) && state.getValue(MUSHROOMS) > 1) {
            popResource(worldIn, pos, new ItemStack(defaultBlockState().getBlock()));
            player.getItemInHand(handIn).hurtAndBreak(1, player, (playerIn) -> {
                playerIn.broadcastBreakEvent(handIn);
            });
            worldIn.gameEvent(player, GameEvent.SHEAR, pos);
            worldIn.setBlock(pos, state.setValue(MUSHROOMS, state.getValue(MUSHROOMS) - 1), 2);
            worldIn.playSound(null, pos, HabitatSoundEvents.FAIRY_RING_MUSHROOM_SHEAR.get(), SoundSource.BLOCKS, 1.0F, 0.8F + worldIn.random.nextFloat() * 0.4F);
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }

        return super.use(state, worldIn, pos, player, handIn, hit);
    }


    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(MUSHROOMS) < 4;
    }

    public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        if (state.getValue(MUSHROOMS) < 4 && ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt(25) == 0)) {
            worldIn.setBlock(pos, state.setValue(MUSHROOMS, state.getValue(MUSHROOMS) + 1), 2);
            ForgeHooks.onCropsGrowPost(worldIn, pos, state);
        }
    }

    public boolean isValidBonemealTarget(BlockGetter worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        return state.getValue(MUSHROOMS) != 4 || rand.nextFloat() < (worldIn.getBlockState(pos.below()).is(BlockTags.MUSHROOM_GROW_BLOCK) ? 0.8F : 0.4F);
    }

    public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        if (state.getValue(MUSHROOMS) < 4)
            worldIn.setBlock(pos, state.setValue(MUSHROOMS, Math.min(4, state.getValue(MUSHROOMS) + Mth.nextInt(rand, 1, 2))), 2);
        else
            growHugeMushroom(worldIn, rand, pos, state);
    }

    private void growHugeMushroom(ServerLevel world, RandomSource rand, BlockPos pos, BlockState state) {
        world.removeBlock(pos, false);
        ConfiguredFeature<?, ?> configuredfeature = world.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).get(new ResourceLocation(Habitat.MODID, "huge_fairy_ring_mushroom"));

        if (!configuredfeature.place(world, world.getChunkSource().getGenerator(), rand, pos))
            world.setBlock(pos, state, 3);
    }
}