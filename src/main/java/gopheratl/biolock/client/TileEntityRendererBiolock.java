package gopheratl.biolock.client;

import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.util.BLLog;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TileEntityRendererBiolock extends TileEntitySpecialRenderer {

	static float texPixel=1.0f/16f;
	
	public TileEntityRendererBiolock()
	{
		super();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) 
	{
		TileEntityBioLock te=(TileEntityBioLock)tileEntity;
		Tessellator tessellator=Tessellator.instance;
		
		int bx=te.xCoord, by=te.yCoord, bz=te.zCoord;
		
		World world=te.getWorldObj();
		
		float brightness=BioLock.Blocks.bioLock.getLightValue(world, bx, by, bz);
		int light=world.getLightBrightnessForSkyBlocks(bx,by,bz,0);				
		
		tessellator.setColorOpaque_F(brightness,brightness,brightness);		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(light&0xffff),(float)(light>>16));
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y,(float)z);
		GL11.glTranslatef(.5f,0,.5f);
		GL11.glRotatef(te.getAngle(),0f,1f,0f);
		GL11.glTranslatef(-.5f,0,-.5f);
		
		int frame = te.clientFrameIndex;
		this.bindTexture(new ResourceLocation("biolock", "textures/blocks/biolock_front"+frame+".png"));
		
		tessellator.startDrawingQuads();
		tessellator.setNormal(0f, 0f, 1f);
		//inset face
		tessellator.addVertexWithUV( texPixel,    texPixel,    texPixel, texPixel,    1f-texPixel);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, texPixel, texPixel,    texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, texPixel, 1f-texPixel, texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    texPixel, 1f-texPixel, 1f-texPixel);

		
		//bottom lip front
		tessellator.addVertexWithUV( 0f,          0f,          0.001f,  0f,          0f);
		tessellator.addVertexWithUV( texPixel,    texPixel,    0.001f,  texPixel,    texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    0.001f,  1f-texPixel, texPixel);
		tessellator.addVertexWithUV( 1f,          0f,          0.001f,  1f,          0f);
		//top lip front
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, 0.001f,  texPixel,    1f-texPixel);
		tessellator.addVertexWithUV( 0f,          1f,          0.001f,  0f,          1f);
		tessellator.addVertexWithUV( 1f,          1f,          0.001f,  1f,          1f);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, 0.001f,   1f-texPixel, 1f-texPixel);
		//right lip front
		tessellator.addVertexWithUV( 0f,          0f,          0.001f,  0f,          0f);
		tessellator.addVertexWithUV( 0f,          1f,          0.001f,  0f,          1f);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, 0.001f,  texPixel,    1f-texPixel);
		tessellator.addVertexWithUV( texPixel,    texPixel,    0.001f,  texPixel,    texPixel);
		//left lip front
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    0.001f,  1f-texPixel, texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, 0.001f,  1f-texPixel, 1f-texPixel);
		tessellator.addVertexWithUV( 1f,          1f,          0.001f,  1f,          1f);
		tessellator.addVertexWithUV( 1f,          0f,          0.001f,  1f,          0f);

		//bottom lip inside
		tessellator.setNormal(0f,1f,0f);
		tessellator.addVertexWithUV( texPixel,    texPixel,    0f,       texPixel,    1f);
		tessellator.addVertexWithUV( texPixel,    texPixel,    texPixel, texPixel,    1f-texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    texPixel, 1f-texPixel, 1f-texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    0f,       1f-texPixel, 1f);
		//top lip inside
		tessellator.setNormal(0f,-1f,0f);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, texPixel, texPixel,    texPixel);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, 0f,       texPixel,    0f);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, 0f,       1f-texPixel, 0f);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, texPixel, 1f-texPixel, texPixel);
		//right lip inside
		tessellator.setNormal(1f,0f,0f);
		tessellator.addVertexWithUV( texPixel,    texPixel,    0f,       1f-texPixel, texPixel);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, 0f,       1f-texPixel, 1f-texPixel);
		tessellator.addVertexWithUV( texPixel,    1f-texPixel, texPixel, 1f,          1f-texPixel);
		tessellator.addVertexWithUV( texPixel,    texPixel,    texPixel, 1f,          texPixel);
		//left lip inside
		tessellator.setNormal(-1f,1f,0f);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    texPixel, 1f,          texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, texPixel, 1f,          1f-texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, 1f-texPixel, 0f,       1f-texPixel, 1f-texPixel);
		tessellator.addVertexWithUV( 1f-texPixel, texPixel,    0f,       1f-texPixel, texPixel);
		
		tessellator.draw();
				
		GL11.glPopMatrix();
	}

}
