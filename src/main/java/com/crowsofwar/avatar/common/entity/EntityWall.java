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

package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.entity.data.SyncableEntityReference;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static com.crowsofwar.gorecore.util.GoreCoreNBTUtil.nestedCompound;
import static java.lang.Math.abs;
import static net.minecraft.util.EnumFacing.NORTH;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class EntityWall extends AvatarEntity {
	
	private static final DataParameter<Integer> SYNC_DIRECTION = EntityDataManager.createKey(EntityWall.class,
			DataSerializers.VARINT);
	private static final DataParameter<Integer>[] SYNC_SEGMENTS;
	static {
		SYNC_SEGMENTS = new DataParameter[5];
		for (int i = 0; i < SYNC_SEGMENTS.length; i++) {
			SYNC_SEGMENTS[i] = EntityDataManager.createKey(EntityWall.class, DataSerializers.VARINT);
		}
	}
	
	/**
	 * All the segments in this wall. MUST be fixed-length, as the data
	 * parameters must be same for both sides.
	 */
	private final SyncableEntityReference<EntityWallSegment>[] segments;
	
	private int nextSegment = 0;
	
	/**
	 * @param world
	 */
	public EntityWall(World world) {
		super(world);
		this.segments = new SyncableEntityReference[5];
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new SyncableEntityReference(this, SYNC_SEGMENTS[i]);
		}
		setSize(0, 0);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_DIRECTION, NORTH.ordinal());
		for (int i = 0; i < SYNC_SEGMENTS.length; i++) {
			dataManager.register(SYNC_SEGMENTS[i], -1);
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// Sync y-velocity with slowest moving wall segment
		// Also calculate lowest top pos of each segment
		double slowest = Integer.MAX_VALUE;
		double lowest = Integer.MAX_VALUE;
		for (SyncableEntityReference<EntityWallSegment> ref : segments) {
			EntityWallSegment seg = ref.getEntity();
			if (seg != null) {

				if (abs(seg.velocity().y()) < abs(slowest)) {
					slowest = seg.velocity().y();
				}

				double topPos = seg.position().y() + seg.height;
				if (topPos < lowest) {
					lowest = topPos;
				}

			}
		}
		
		// Now sync all wall segment speeds
		// Also sync all segment pos to the lowest height
		for (SyncableEntityReference<EntityWallSegment> ref : segments) {
			EntityWallSegment seg = ref.getEntity();
			if (seg != null) {

				seg.velocity().setY(slowest);
				seg.position().setY(lowest - seg.height);

			}
		}
		
		velocity().set(Vector.ZERO);
		this.noClip = true;
		move(MoverType.SELF, 0, slowest / 20, 0);
	}
	
	/**
	 * To be used ONLY by {@link EntityWallSegment}
	 */
	void addSegment(EntityWallSegment segment) {
		segments[nextSegment].setEntity(segment);
		nextSegment++;
	}
	
	public EntityWallSegment getSegment(int i) {
		return segments[i].getEntity();
	}
	
	public EnumFacing getDirection() {
		return EnumFacing.values()[dataManager.get(SYNC_DIRECTION)];
	}
	
	public void setDirection(EnumFacing direction) {
		if (direction.getAxis().isVertical())
			throw new IllegalArgumentException("Cannot face up/down: " + direction);
		this.dataManager.set(SYNC_DIRECTION, direction.ordinal());
	}
	
	@Override
	public void setDead() {
		for (SyncableEntityReference<EntityWallSegment> ref : segments) {
			// don't use setDead() as that will trigger this being called again
			EntityWallSegment entity = ref.getEntity();
			if (entity != null) {
				// Avoid setDead() as that will call wall.setDead()
				entity.isDead = true;
				entity.dropBlocks();
			}
		}
		super.setDead();
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		for (int i = 0; i < segments.length; i++)
			segments[i].readFromNBT(nestedCompound(nbt, "Wall" + i));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		for (int i = 0; i < segments.length; i++)
			segments[i].writeToNBT(nestedCompound(nbt, "Wall" + i));
	}
	
	@Override
	protected boolean canCollideWith(Entity entity) {
		return super.canCollideWith(entity) && !(entity instanceof EntityWall)
				&& !(entity instanceof EntityWallSegment);
	}
	
	@Override
	public boolean tryDestroy() {
		return false;
	}
	
}
