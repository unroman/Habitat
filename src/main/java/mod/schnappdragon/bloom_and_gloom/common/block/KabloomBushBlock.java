package mod.schnappdragon.bloom_and_gloom.common.block;

import mod.schnappdragon.bloom_and_gloom.common.entity.projectile.KabloomFruitEntity;
import mod.schnappdragon.bloom_and_gloom.core.registry.BGItems;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class KabloomBushBlock extends BushBlock implements IGrowable {
    protected static final VoxelShape AGE_0_SHAPE = Block.makeCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D);
    protected static final VoxelShape AGE_1_SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 6.0D, 13.0D);
    protected static final VoxelShape AGE_2_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);
    protected static final VoxelShape AGE_3_7_SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);

    public static final IntegerProperty AGE = BlockStateProperties.AGE_0_7;

    public KabloomBushBlock() {
        super(AbstractBlock.Properties
                .create(Material.PLANTS)
                .hardnessAndResistance(0.0F, 0.0F)
                .sound(SoundType.PLANT)
                .doesNotBlockMovement()
                .notSolid()
                .setAllowsSpawn((a,b,c,d) -> false));

        this.setDefaultState(
                this.stateContainer.getBaseState()
                        .with(AGE, 0)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(AGE)) {
            case 0:
                return AGE_0_SHAPE;
            case 1:
                return AGE_1_SHAPE;
            case 2:
                return AGE_2_SHAPE;
            default:
                return AGE_3_7_SHAPE;
        }
    }

    @ParametersAreNonnullByDefault
    public boolean propagatesSkylightDown(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return true;
    }

    /*
     * Kabloom Bush Function Methods
     */


    @Override
    @ParametersAreNonnullByDefault
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity && entityIn.getType() != EntityType.BEE || entityIn instanceof ProjectileEntity || entityIn instanceof FallingBlockEntity) {
            if (entityIn instanceof LivingEntity && state.get(AGE) > 0)
                entityIn.setMotionMultiplier(state, new Vector3d(0.8F, 0.75D, 0.8F));
            if (state.get(AGE) == 7) {
                dropFruit(state, worldIn, pos, true);
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!(state.get(AGE) == 7) && player.getHeldItem(handIn).getItem() == Items.BONE_MEAL) {
            return ActionResultType.PASS;
        } else if (state.get(AGE) == 7) {
            if (player.getHeldItem(handIn).getItem() instanceof ShearsItem) {
                spawnAsEntity(worldIn, pos, new ItemStack(BGItems.KABLOOM_FRUIT.get(), 1));
                player.getHeldItem(handIn);
                worldIn.setBlockState(pos, state.with(AGE, 3), 2);
            }
            else
                dropFruit(state, worldIn, pos, true);
            return ActionResultType.SUCCESS;
        }
        else
            return ActionResultType.FAIL;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.get(AGE) == 7) {
            ItemStack held = player.getHeldItemMainhand();
            if (held.getItem() instanceof HoeItem && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, held) > 0)
                spawnAsEntity(worldIn, pos, new ItemStack(BGItems.KABLOOM_FRUIT.get(), 1));
            else
                dropFruit(state, worldIn, pos, false);
        }
    }

    private void dropFruit(BlockState state, World worldIn, BlockPos pos, boolean replaceBush) {
        if (replaceBush)
            worldIn.setBlockState(pos, state.with(AGE, 3), 2);
        worldIn.addEntity(new KabloomFruitEntity(worldIn, pos.getX() + 0.5F, pos.getY() + 0.6F, pos.getZ() + 0.5F));
    }

    /*
     * Growth Methods
     */

    public boolean ticksRandomly(BlockState state) {
        return state.get(AGE) <= 7;
    }

    @ParametersAreNonnullByDefault
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (state.get(AGE) < 7 && ForgeHooks.onCropsGrowPre(worldIn, pos, state,random.nextInt(5) == 0)) {
            worldIn.setBlockState(pos, state.with(AGE, state.get(AGE) + 1), 2);
            ForgeHooks.onCropsGrowPost(worldIn, pos, state);
        }
        else if (state.get(AGE) == 7 && ForgeHooks.onCropsGrowPre(worldIn, pos, state,random.nextInt(10) == 0)) {
            dropFruit(state, worldIn, pos, true);
            ForgeHooks.onCropsGrowPost(worldIn, pos, state);
        }
    }

    @ParametersAreNonnullByDefault
    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(AGE) < 7;
    }

    @ParametersAreNonnullByDefault
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @ParametersAreNonnullByDefault
    public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
        worldIn.setBlockState(pos, state.with(AGE, Math.min(7, state.get(AGE) + 1)), 2);
    }

    /*
     * Plant Method
     */

    @Override
    @ParametersAreNonnullByDefault
    public BlockState getPlant(IBlockReader world, BlockPos pos) {
        return getDefaultState();
    }

    @Override
    public PlantType getPlantType(IBlockReader world, BlockPos pos) {
        return PlantType.PLAINS;
    }

    /*
     * Block Flammability Methods
     */

    @Override
    public int getFlammability(BlockState state, IBlockReader worldIn, BlockPos pos, Direction face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader worldIn, BlockPos pos, Direction face) {
        return 60;
    }

    /*
     * Pathfinding Method
     */

    @Nullable
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
        return (entity instanceof BeeEntity) ? PathNodeType.OPEN : PathNodeType.DAMAGE_OTHER;
    }
}