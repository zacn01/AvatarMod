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
package com.crowsofwar.avatar.common.bending.water;

import static com.crowsofwar.gorecore.util.Vector.getEntityPos;
import static com.crowsofwar.gorecore.util.Vector.getRotationTo;
import static java.lang.Math.toDegrees;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingAi;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.Bender;
import com.crowsofwar.avatar.common.entity.AvatarEntity;
import com.crowsofwar.avatar.common.entity.EntityWaterArc;
import com.crowsofwar.avatar.common.entity.data.WaterArcBehavior;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class AiWaterArc extends BendingAi {
	
	private int timeExecuting;
	
	/**
	 * @param ability
	 * @param entity
	 * @param bender
	 */
	protected AiWaterArc(BendingAbility ability, EntityLiving entity, Bender bender) {
		super(ability, entity, bender);
		timeExecuting = 0;
		setMutexBits(3);
	}
	
	@Override
	protected void startExec() {}
	
	@Override
	public boolean shouldContinueExecuting() {
		
		EntityLivingBase target = entity.getAttackTarget();
		if (target == null) return false;
		
		Vector targetRotations = getRotationTo(getEntityPos(entity), getEntityPos(target));
		entity.rotationYaw = (float) toDegrees(targetRotations.y());
		entity.rotationPitch = (float) toDegrees(targetRotations.x());
		
		entity.getLookHelper().setLookPosition(target.posX, target.posY + target.getEyeHeight(), target.posZ,
				10, 10);
		
		if (timeExecuting == 20) {
			BendingData data = bender.getData();
			data.chi().setMaxChi(10);
			data.chi().setTotalChi(10);
			data.chi().setAvailableChi(10);
			execAbility();
			data.setAbilityCooldown(80);
		}
		
		if (timeExecuting >= 80) {
			BendingData data = bender.getData();
			execStatusControl(StatusControl.THROW_WATER);
			timeExecuting = 0;
			return false;
		} else {
			return true;
		}
		
	}
	
	@Override
	protected boolean shouldExec() {
		EntityLivingBase target = entity.getAttackTarget();
		return target != null && entity.getDistanceSqToEntity(target) > 4 * 4
				&& bender.getData().getAbilityCooldown() == 0;
	}
	
	@Override
	public void updateTask() {
		timeExecuting++;
	}
	
	@Override
	public void resetTask() {
		
		EntityWaterArc water = AvatarEntity.lookupEntity(entity.world, EntityWaterArc.class, //
				arc -> arc.getBehavior() instanceof WaterArcBehavior.PlayerControlled
						&& arc.getOwner() == entity);
		
		if (water != null) {
			water.setDead();
			bender.getData().removeStatusControl(StatusControl.THROW_WATER);
		}
		
	}
	
}
