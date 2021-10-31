package mod.schnappdragon.habitat.common.block;

import mod.schnappdragon.habitat.core.registry.HabitatBlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class HabitatStandingSignBlock extends StandingSignBlock {
    public HabitatStandingSignBlock(Properties properties, WoodType woodTypeIn) {
        super(properties, woodTypeIn);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return HabitatBlockEntityTypes.SIGN.get().create();
    }
}