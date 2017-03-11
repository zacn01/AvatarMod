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

package com.crowsofwar.avatar.common.bending.earth;

import static com.crowsofwar.avatar.common.bending.BendingAbility.ABILITY_PICK_UP_BLOCK;
import static com.crowsofwar.avatar.common.bending.StatusControl.CrosshairPosition.RIGHT_OF_CROSSHAIR;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.controls.AvatarControl.CONTROL_RIGHT_CLICK_DOWN;

import com.crowsofwar.avatar.common.bending.BendingController;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.bending.BendingType;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.entity.EntityFloatingBlock;
import com.crowsofwar.avatar.common.entity.data.FloatingBlockBehavior;
import com.crowsofwar.gorecore.util.Vector;
import com.crowsofwar.gorecore.util.VectorI;

import net.minecraft.util.EnumFacing;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class StatCtrlPlaceBlock extends StatusControl {
	
	public StatCtrlPlaceBlock() {
		super(1, CONTROL_RIGHT_CLICK_DOWN, RIGHT_OF_CROSSHAIR);
		
		requireRaytrace(-1, true);
		
	}
	
	@Override
	public boolean execute(AbilityContext ctx) {
		
		BendingController controller = (BendingController) BendingManager
				.getBending(BendingType.EARTHBENDING);
		
		BendingData data = ctx.getData();
		EarthbendingState ebs = (EarthbendingState) data.getBendingState(controller);
		
		EntityFloatingBlock floating = ebs.getPickupBlock();
		if (floating != null) {
			// TODO Verify look at block
			VectorI looking = ctx.getClientLookBlock();
			EnumFacing lookingSide = ctx.getLookSide();
			if (looking != null && lookingSide != null) {
				looking.offset(lookingSide);
				
				floating.setBehavior(new FloatingBlockBehavior.Place(looking.toBlockPos()));
				Vector force = looking.precision().minus(new Vector(floating));
				force.normalize();
				floating.velocity().add(force);
				ebs.dropBlock();
				
				controller.post(new FloatingBlockEvent.BlockPlaced(floating, ctx.getBenderEntity()));
				
				data.removeStatusControl(THROW_BLOCK);
				data.sync();
				
				data.getAbilityData(ABILITY_PICK_UP_BLOCK).addXp(SKILLS_CONFIG.blockPlaced);
				
				return true;
			}
			return false;
		}
		
		return true;
		
	}
	
}
