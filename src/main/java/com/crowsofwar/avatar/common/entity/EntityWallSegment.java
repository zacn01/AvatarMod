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

import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.Bender;
import com.crowsofwar.avatar.common.data.ctx.BenderInfo;
import com.crowsofwar.avatar.common.entity.data.OwnerAttribute;
import com.crowsofwar.avatar.common.entity.data.SyncableEntityReference;
import com.crowsofwar.avatar.common.entity.data.WallBehavior;
import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.gorecore.util.Vector;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import static com.crowsofwar.avatar.common.bending.BendingAbility.ABILITY_WALL;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.gorecore.util.GoreCoreNBTUtil.nestedCompound;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class EntityWallSegment extends AvatarEntity implements IEntityAdditionalSpawnData {
	
	public static final int SEGMENT_HEIGHT = 5;
	
	private static final DataParameter<Integer> SYNC_WALL = EntityDataManager
			.createKey(EntityWallSegment.class, DataSerializers.VARINT);
	private static final DataParameter<WallBehavior> SYNC_BEHAVIOR = EntityDataManager
			.createKey(EntityWallSegment.class, WallBehavior.SERIALIZER);
	private static final DataParameter<BenderInfo> SYNC_OWNER = EntityDataManager
			.createKey(EntityWallSegment.class, AvatarDataSerializers.SERIALIZER_BENDER);
	
	private static final DataParameter<Optional<IBlockState>>[] SYNC_BLOCKS_DATA;
	static {
		SYNC_BLOCKS_DATA = new DataParameter[SEGMENT_HEIGHT];
		for (int i = 0; i < SEGMENT_HEIGHT; i++) {
			SYNC_BLOCKS_DATA[i] = EntityDataManager.createKey(EntityWallSegment.class,
					DataSerializers.OPTIONAL_BLOCK_STATE);
		}
	}
	
	private final SyncableEntityReference<EntityWall> wallReference;
	/**
	 * direction that all wall-segments are facing towards. Only set on server.
	 */
	private EnumFacing direction;
	private int offset;
	
	private final OwnerAttribute ownerAttribute;
	
	public EntityWallSegment(World world) {
		super(world);
		this.wallReference = new SyncableEntityReference<>(this, SYNC_WALL);
		this.setSize(.9f, 5);
		this.ownerAttribute = new OwnerAttribute(this, SYNC_OWNER);
	}
	
	@Override
	public void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_WALL, -1);
		for (DataParameter<Optional<IBlockState>> sync : SYNC_BLOCKS_DATA)
			dataManager.register(sync, Optional.of(Blocks.STONE.getDefaultState()));
		dataManager.register(SYNC_BEHAVIOR, new WallBehavior.Rising());
	}
	
	public EntityWall getWall() {
		return wallReference.getEntity();
	}
	
	/**
	 * Allows this segment to reference the wall, and allows the wall to
	 * reference this segment.
	 */
	public void attachToWall(EntityWall wall) {
		wallReference.setEntity(wall);
		wall.addSegment(this);
	}
	
	@Override
	public EntityLivingBase getOwner() {
		return ownerAttribute.getOwner();
	}
	
	public void setOwner(EntityLivingBase owner) {
		ownerAttribute.setOwner(owner);
	}
	
	public IBlockState getBlock(int i) {
		IBlockState state = dataManager.get(SYNC_BLOCKS_DATA[i]).orNull();
		return state == null ? Blocks.AIR.getDefaultState() : state;
	}
	
	public void setBlock(int i, IBlockState block) {
		dataManager.set(SYNC_BLOCKS_DATA[i],
				block == null ? Optional.of(Blocks.AIR.getDefaultState()) : Optional.of(block));
	}
	
	public WallBehavior getBehavior() {
		return dataManager.get(SYNC_BEHAVIOR);
	}
	
	public void setBehavior(WallBehavior behavior) {
		dataManager.set(SYNC_BEHAVIOR, behavior);

		// Remove "drop wall" statCtrl if the wall is dropping
		if (behavior instanceof WallBehavior.Drop) {
			if (getOwner() != null) {
				Bender.create(getOwner()).getData().removeStatusControl(StatusControl.DROP_WALL);
			}
		}
		
	}
	
	public void setDirection(EnumFacing dir) {
		this.direction = dir;
	}
	
	public int getBlocksOffset() {
		return offset;
	}
	
	public void setBlocksOffset(int offset) {
		this.offset = offset;
	}
	
	// Expose setSize method so AbilityWall can call it
	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
	}
	
	@Override
	public void setDead() {
		super.setDead();
		if (getWall() != null) getWall().setDead();
	}
	
	/**
	 * Drops any blocks contained by this segment
	 */
	public void dropBlocks() {
		for (int i = 0; i < SEGMENT_HEIGHT; i++) {
			IBlockState state = getBlock(i);
			if (state.getBlock() != Blocks.AIR)
				world.setBlockState(new BlockPos(this).up(i + getBlocksOffset()), state);
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		ignoreFrustumCheck = true;
		velocity().setX(0);
		velocity().setZ(0);
		WallBehavior next = (WallBehavior) getBehavior().onUpdate(this);
		if (getBehavior() != next) setBehavior(next);
	}
	
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand stack) {
		if (!this.isDead && !world.isRemote && player.capabilities.isCreativeMode && player.isSneaking()) {
			setDead();
			dropBlocks();
			setBeenAttacked();
			return true;
		}
		return false;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		wallReference.readFromNBT(nestedCompound(nbt, "Parent"));
		ownerAttribute.load(nbt);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		wallReference.writeToNBT(nestedCompound(nbt, "Parent"));
		ownerAttribute.save(nbt);
	}
	
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}
	
	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeFloat(height);
		buf.writeInt(offset);
	}
	
	@Override
	public void readSpawnData(ByteBuf buf) {
		setSize(width, buf.readFloat());
		offset = buf.readInt();
	}
	
	@Override
	public void addVelocity(double x, double y, double z) {}
	
	@Override
	protected void onCollideWithEntity(Entity entity) {
		
		// Note... only called server-side
		double amt = 0.4;
		
		boolean ns = direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH;
		if (ns) {
			if (entity.posZ > this.posZ) {
				entity.posZ = this.posZ + 1.1;
			} else {
				amt = -amt;
				entity.posZ = this.posZ - 1.1;
			}
		} else {
			if (entity.posX > this.posX) {
				entity.posX = this.posX + 1.1;
			} else {
				amt = -amt;
				entity.posX = this.posX - 1.1;
			}
		}
		
		if (ns) {
			entity.motionZ = amt;
		} else {
			entity.motionX = amt;
		}
		
		entity.motionY = .25;
		
		entity.isAirBorne = true;
		if (entity instanceof EntityPlayerMP) {
			((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityVelocity(entity));
		}
		if (entity instanceof AvatarEntity) {
			Vector velocity = ((AvatarEntity) entity).velocity();
			if (ns)
				velocity.setZ(amt);
			else
				velocity.setX(amt);
		}
		
		if (entity instanceof AvatarEntity) {
			
			AvatarEntity avEnt = (AvatarEntity) entity;
			avEnt.onCollideWithSolid();
			
			if (avEnt.tryDestroy()) {
				entity.setDead();
				if (getOwner() != null) {
					BendingData data = ownerAttribute.getOwnerBender().getData();
					data.getAbilityData(ABILITY_WALL).addXp(SKILLS_CONFIG.wallBlockedAttack);
				}
			}
			
		}
		
	}
	
	@Override
	protected boolean canCollideWith(Entity entity) {
		
		boolean notWall = !(entity instanceof EntityWall) && !(entity instanceof EntityWallSegment);
		
		boolean friendlyProjectile = false;
		if (getOwner() != null) {
			AbilityData data = Bender.create(getOwner()).getData().getAbilityData(ABILITY_WALL);
			if (data.isMaxLevel() && data.getPath() == AbilityTreePath.FIRST) {
				
				friendlyProjectile = entity instanceof AvatarEntity
						&& ((AvatarEntity) entity).getOwner() == this.getOwner();
				
			}
		}
		
		return notWall && !friendlyProjectile;
		
	}
	
	@Override
	public boolean tryDestroy() {
		return false;
	}
	
}
