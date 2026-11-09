package nukeduck.armorchroma;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class ArmorChromaRenderLayers {
   private static final BlendFunction MASKED_ICON_BLEND_FUNCTION = new BlendFunction(SourceFactor.DST_COLOR, DestFactor.ZERO);
   private static final RenderPipeline MASKED_ICON_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new RenderPipeline.Snippet[]{RenderPipelines.GUI_SNIPPET})
         .withLocation(Identifier.fromNamespaceAndPath("armorchroma", "pipeline/masked_icon"))
         .withDepthTestFunction(DepthTestFunction.EQUAL_DEPTH_TEST)
         .withBlend(MASKED_ICON_BLEND_FUNCTION)
         .build()
   );

   public static RenderPipeline getMaskedIcon() {
      return MASKED_ICON_PIPELINE;
   }
}
