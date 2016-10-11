package com.crowsofwar.avatar.client.render;

import org.joml.Matrix4d;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

import com.crowsofwar.avatar.common.entity.ControlPoint;
import com.crowsofwar.avatar.common.entity.EntityArc;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class RenderArc extends Render {
	
	private final RenderManager renderManager;
	
	/**
	 * @param renderManager
	 */
	protected RenderArc(RenderManager renderManager) {
		super(renderManager);
		this.renderManager = renderManager;
	}
	
	/**
	 * Whether to render with full brightness.
	 */
	private boolean renderBright;
	
	@Override
	public final void doRender(Entity entity, double xx, double yy, double zz, float p_76986_8_,
			float partialTicks) {
		
		double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		
		EntityArc arc = (EntityArc) entity;
		
		for (int i = 1; i < arc.getControlPoints().length; i++) {
			renderSegment(arc, arc.getLeader(i), arc.getControlPoint(i), renderPosX, renderPosY, renderPosZ);
		}
		
	}
	
	private void renderSegment(EntityArc arc, ControlPoint leader, ControlPoint point, double renderPosX,
			double renderPosY, double renderPosZ) {
		double x = leader.getXPos() - TileEntityRendererDispatcher.staticPlayerX;
		double y = leader.getYPos() - TileEntityRendererDispatcher.staticPlayerY;
		double z = leader.getZPos() - TileEntityRendererDispatcher.staticPlayerZ;
		
		Vector from = new Vector();
		Vector to = point.position().minus(leader.position());
		
		Vector diff = to.minus(from);
		
		double ySize = 1;
		int textureRepeat = 2;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(getTexture());
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		if (renderBright) GlStateManager.disableLighting();
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		// double size = arc.width / 2;
		double sizeLeader = point.size() / 2;
		double sizePoint = leader.size() / 2;
		
		double distance = to.sqrMagnitude();
		double sizeMultiplier = distance < 1 ? -.7 * distance + 1.7 : 1 / Math.sqrt(distance);
		sizeLeader *= sizeMultiplier;
		sizePoint *= sizeMultiplier;
		
		Vector lookingEuler = Vector.getRotations(from, to);
		
		double u1 = ((arc.ticksExisted / 20.0) % 1);
		double u2 = (u1 + 0.5);
		
		GlStateManager.color(1, 1, 1);
		
		// Make 'back' matrix, face it forward
		Matrix4d mat = new Matrix4d();
		mat.rotate((float) -lookingEuler.y(), 0, 1, 0);
		mat.rotate((float) lookingEuler.x(), 1, 0, 0);
		double dist = leader.getDistance(point);
		
		Vector4d t_v1 = new Vector4d(-sizeLeader, sizeLeader, dist, 1).mul(mat);
		Vector4d t_v2 = new Vector4d(sizeLeader, sizeLeader, dist, 1).mul(mat);
		Vector4d t_v3 = new Vector4d(sizePoint, sizePoint, 0, 1).mul(mat);
		Vector4d t_v4 = new Vector4d(-sizePoint, sizePoint, 0, 1).mul(mat);
		
		Vector4d b_v1 = new Vector4d(-sizeLeader, -sizeLeader, dist, 1).mul(mat);
		Vector4d b_v2 = new Vector4d(sizeLeader, -sizeLeader, dist, 1).mul(mat);
		Vector4d b_v3 = new Vector4d(sizePoint, -sizePoint, 0, 1).mul(mat);
		Vector4d b_v4 = new Vector4d(-sizePoint, -sizePoint, 0, 1).mul(mat);
		
		// Draw top segment
		drawQuad(2, t_v1, t_v2, t_v3, t_v4, u1, 0, u2, 1);
		// Draw bottom segment
		drawQuad(2, b_v1, b_v2, b_v3, b_v4, u1, 0, u2, 1);
		// Draw right segment(+x)
		drawQuad(2, t_v2, b_v2, b_v3, t_v3, u1, 0, u2, 1);
		// Draw left segment(-x)
		drawQuad(2, t_v1, b_v1, b_v4, t_v4, u1, 0, u2, 1);
		
		onDrawSegment(arc, leader, point);
		
		GlStateManager.disableBlend();
		if (renderBright) GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected final ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return null;
	}
	
	private void drawQuad(int normal, Vector pos1, Vector pos2, Vector pos3, Vector pos4, double u1,
			double v1, double u2, double v2) {
		
		Tessellator t = Tessellator.getInstance();
		VertexBuffer vb = t.getBuffer();
		
		if (normal == 0 || normal == 2) {
			
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			vb.pos(pos1.x(), pos1.y(), pos1.z()).tex(u2, v1).endVertex();
			vb.pos(pos2.x(), pos2.y(), pos2.z()).tex(u2, v2).endVertex();
			vb.pos(pos3.x(), pos3.y(), pos3.z()).tex(u1, v2).endVertex();
			vb.pos(pos4.x(), pos4.y(), pos4.z()).tex(u1, v1).endVertex();
			t.draw();
			
			// t.startDrawingQuads();
			// t.addVertexWithUV(pos1.xCoord, pos1.yCoord, pos1.zCoord, u2, v1); // 1
			// t.addVertexWithUV(pos2.xCoord, pos2.yCoord, pos2.zCoord, u2, v2); // 2
			// t.addVertexWithUV(pos3.xCoord, pos3.yCoord, pos3.zCoord, u1, v2); // 3
			// t.addVertexWithUV(pos4.xCoord, pos4.yCoord, pos4.zCoord, u1, v1); // 4
			// t.draw();
		}
		if (normal == 1 || normal == 2) {
			
			vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			vb.pos(pos1.x(), pos1.y(), pos1.z()).tex(u2, v1).endVertex();
			vb.pos(pos4.x(), pos4.y(), pos4.z()).tex(u1, v1).endVertex();
			vb.pos(pos3.x(), pos3.y(), pos3.z()).tex(u1, v2).endVertex();
			vb.pos(pos2.x(), pos2.y(), pos2.z()).tex(u2, v2).endVertex();
			t.draw();
			
			// t.startDrawingQuads();
			// t.addVertexWithUV(pos1.xCoord, pos1.yCoord, pos1.zCoord, u2, v1); // 1
			// t.addVertexWithUV(pos4.xCoord, pos4.yCoord, pos4.zCoord, u1, v1); // 4
			// t.addVertexWithUV(pos3.xCoord, pos3.yCoord, pos3.zCoord, u1, v2); // 3
			// t.addVertexWithUV(pos2.xCoord, pos2.yCoord, pos2.zCoord, u2, v2); // 2
			// t.draw();
		}
	}
	
	private void drawQuad(int normal, Vector4d pos1, Vector4d pos2, Vector4d pos3, Vector4d pos4, double u1,
			double v1, double u2, double v2) {
		drawQuad(normal, new Vector(pos1.x, pos1.y, pos1.z), new Vector(pos2.x, pos2.y, pos2.z),
				new Vector(pos3.x, pos3.y, pos3.z), new Vector(pos4.x, pos4.y, pos4.z), u1, v1, u2, v2);
	}
	
	protected abstract ResourceLocation getTexture();
	
	protected void onDrawSegment(EntityArc arc, ControlPoint first, ControlPoint second) {
		
	}
	
	/**
	 * Render this arc to disregard the actual lighting and instead render with full brightness
	 */
	protected void enableFullBrightness() {
		renderBright = true;
	}
	
}
