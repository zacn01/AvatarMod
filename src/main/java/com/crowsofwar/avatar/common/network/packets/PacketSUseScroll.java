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
package com.crowsofwar.avatar.common.network.packets;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.network.PacketRedirector;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class PacketSUseScroll extends AvatarPacket<PacketSUseScroll> {
	
	private BendingAbility ability;
	
	public PacketSUseScroll() {}
	
	public PacketSUseScroll(BendingAbility ability) {
		this.ability = ability;
	}
	
	@Override
	public void avatarFromBytes(ByteBuf buf) {
		ability = BendingManager.getAbility(buf.readInt());
	}
	
	@Override
	public void avatarToBytes(ByteBuf buf) {
		buf.writeInt(ability.getId());
	}
	
	@Override
	protected Side getRecievedSide() {
		return Side.SERVER;
	}
	
	@Override
	protected com.crowsofwar.avatar.common.network.packets.AvatarPacket.Handler<PacketSUseScroll> getPacketHandler() {
		return PacketRedirector::redirectMessage;
	}
	
	public BendingAbility getAbility() {
		return ability;
	}
	
}
