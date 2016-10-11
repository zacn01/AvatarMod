package com.crowsofwar.avatar.client.render;

import java.util.Random;

import com.crowsofwar.avatar.common.entity.ControlPoint;
import com.crowsofwar.avatar.common.entity.EntityArc;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class RenderAirGust extends RenderArc {
	
	public static final ResourceLocation TEXTURE = new ResourceLocation("avatarmod",
			"textures/entity/air-ribbon.png");
	
	private static final Random random = new Random();
	
	/**
	 * @param renderManager
	 */
	public RenderAirGust(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected void onDrawSegment(EntityArc arc, ControlPoint first, ControlPoint second) {
		
		World world = arc.worldObj;
		AxisAlignedBB boundingBox = first.getBoundingBox();
		double spawnX = boundingBox.minX + random.nextDouble() * (boundingBox.maxX - boundingBox.minX);
		double spawnY = boundingBox.minY + random.nextDouble() * (boundingBox.maxY - boundingBox.minY);
		double spawnZ = boundingBox.minZ + random.nextDouble() * (boundingBox.maxZ - boundingBox.minZ);
		world.spawnParticle(EnumParticleTypes.CLOUD, spawnX, spawnY, spawnZ, 0, 0, 0);
		
	}
	
	@Override
	protected ResourceLocation getTexture() {
		return TEXTURE;
	}
	
}
