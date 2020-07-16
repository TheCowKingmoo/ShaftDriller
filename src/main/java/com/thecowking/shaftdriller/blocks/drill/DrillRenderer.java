package com.thecowking.shaftdriller.blocks.drill;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.thecowking.shaftdriller.ShaftDriller;
import com.thecowking.shaftdriller.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;



/*
  My understanding is that this is used to animation
 */

public class DrillRenderer extends TileEntityRenderer<DrillTile> {

    public static final ResourceLocation DRILL_TEXTURE = new ResourceLocation(ShaftDriller.MODID, "block/drill");

    public DrillRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }



    // yanked from mcjty tutorial - check RenderType.getSolid for the correct things to use here
    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .tex(u, v)
                .lightmap(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private static float diffFunction(long time, long delta, float scale) {
        long dt = time % (delta * 2);
        if (dt > delta) {
            dt = 2*delta - dt;
        }
        return dt * scale;
    }

    /*
      partialTicks - percent between ticks we are at
      matrixStackIn - represents the current pos and oreintation of the block relative to the 3d camera
                    - when camera moves render is at right spot
      bufferIn - used to render at the correct
      combinedLightIn - light level current
     */
    @Override
    public void render(DrillTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        // apply drills texture - represents a small square in the big ol atlas
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(DRILL_TEXTURE);

        // IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());  // whole block is transparent

        // IVertexBuilder builder = bufferIn.getBuffer(RenderType.getCutout());  // used for blocks with partial transparency

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());  //used to models

        // here we can animate
        //matrixStackIn.push();
        //matrixStackIn.translate();

        //add(...)
        //add
        //add
        //add

        //matrixStackIn.pop();








    }

    public static void register()  {
        ClientRegistry.bindTileEntityRenderer(Registration.DRILL_TILE.get(), DrillRenderer::new);
    }
}
