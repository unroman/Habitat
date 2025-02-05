package mod.schnappdragon.habitat.client.renderer.entity;

import mod.schnappdragon.habitat.client.model.PasserineModel;
import mod.schnappdragon.habitat.client.renderer.HabitatModelLayers;
import mod.schnappdragon.habitat.client.renderer.entity.layers.PasserineEyesLayer;
import mod.schnappdragon.habitat.common.entity.animal.Passerine;
import mod.schnappdragon.habitat.core.Habitat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PasserineRenderer extends MobRenderer<Passerine, PasserineModel<Passerine>> {
    public static final ResourceLocation PASSERINE_BERDLY_LOCATION = new ResourceLocation(Habitat.MODID, "textures/entity/passerine/berdly.png");
    public static final ResourceLocation PASSERINE_GOLDFISH_LOCATION = new ResourceLocation(Habitat.MODID, "textures/entity/passerine/goldfish.png");
    public static final ResourceLocation PASSERINE_TURKEY_LOCATION = new ResourceLocation(Habitat.MODID, "textures/entity/passerine/turkey.png");

    public PasserineRenderer(EntityRendererProvider.Context context) {
        super(context, new PasserineModel<>(context.bakeLayer(HabitatModelLayers.PASSERINE)), 0.25F);
        this.addLayer(new PasserineEyesLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Passerine passerine) {
        if (passerine.isBerdly())
            return PASSERINE_BERDLY_LOCATION;
        else if (passerine.isGoldfish())
            return PASSERINE_GOLDFISH_LOCATION;
        else if (passerine.isTurkey())
            return PASSERINE_TURKEY_LOCATION;

        return passerine.getVariant().texture();
    }

    @Override
    public float getBob(Passerine passerine, float partialTicks) {
        if (passerine.isFlying()) {
            float f = Mth.lerp(partialTicks, passerine.initialFlap, passerine.flap);
            float f1 = Mth.lerp(partialTicks, passerine.initialFlapSpeed, passerine.flapSpeed);
            return (Mth.sin(f) + 1.0F) * f1;
        } else {
            return super.getBob(passerine, partialTicks);
        }
    }
}