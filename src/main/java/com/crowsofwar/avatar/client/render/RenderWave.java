/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.avatar.client.render;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.crowsofwar.avatar.common.entity.EntityWave;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class RenderWave extends Render<EntityWave> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("avatarmod",
			"textures/entity/water.png");
	
	/**
	 * @param renderManager
	 */
	public RenderWave(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityWave entity, double x, double y, double z, float entityYaw,
			float partialTicks) {
		
		float size = entity.getWaveSize();
		
		float fx = (float) x;
		float fy = (float) y;
		float fz = (float) z;
		
		Tessellator t = Tessellator.getInstance();
		BufferBuilder vb = t.getBuffer();
		
		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
		
		// Please see notebook for diagrams
		
		Matrix4f matrix = new Matrix4f();
		matrix.translate(fx, fy + 1, fz);
		matrix.rotate((float) Math.toRadians(entity.rotationYaw), 0, -1, 0);
		matrix.translate(-1.5f, 0, -entity.width / 2);
		matrix.scale(size, size / 6f, 1);
		
		Vector4f a = new Vector4f(1, 1, 0.5f, 1).mul(matrix);
		Vector4f b = new Vector4f(0, 1, 0.5f, 1).mul(matrix);
		Vector4f c = new Vector4f(0, 0, 0, 1).mul(matrix);
		Vector4f d = new Vector4f(1, 0, 0, 1).mul(matrix);
		Vector4f e = new Vector4f(0, 0, 1, 1).mul(matrix);
		Vector4f f = new Vector4f(1, 0, 1, 1).mul(matrix);
		
		drawFace(a, d, c, b);
		drawFace(b, e, f, a);
		
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityWave entity) {
		return null;
	}
	
	private void drawFace(Vector4f pos1, Vector4f pos2, Vector4f pos3, Vector4f pos4) {
		Tessellator t = Tessellator.getInstance();
		BufferBuilder vb = t.getBuffer();
		
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(pos1.x, pos1.y, pos1.z).tex(0, 0).endVertex();
		vb.pos(pos2.x, pos2.y, pos2.z).tex(0, 1).endVertex();
		vb.pos(pos3.x, pos3.y, pos3.z).tex(1, 1).endVertex();
		vb.pos(pos4.x, pos4.y, pos4.z).tex(1, 0).endVertex();
		t.draw();
		
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		vb.pos(pos4.x, pos4.y, pos4.z).tex(1, 0).endVertex();
		vb.pos(pos3.x, pos3.y, pos3.z).tex(1, 1).endVertex();
		vb.pos(pos2.x, pos2.y, pos2.z).tex(0, 1).endVertex();
		vb.pos(pos1.x, pos1.y, pos1.z).tex(0, 0).endVertex();
		t.draw();
		
	}
	
}
