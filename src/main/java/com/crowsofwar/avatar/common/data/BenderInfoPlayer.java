package com.crowsofwar.avatar.common.data;

import com.crowsofwar.avatar.common.data.ctx.Bender;
import com.crowsofwar.avatar.common.data.ctx.BenderInfo;
import com.crowsofwar.gorecore.util.AccountUUIDs;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author CrowsOfWar
 */
public class BenderInfoPlayer extends BenderInfo {

	private UUID playerId;

	public BenderInfoPlayer(@Nonnull UUID playerId) {
		this.playerId = playerId;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Nullable
	@Override
	public UUID getId() {
		return playerId;
	}

	@Nullable
	@Override
	public Bender find(World world) {
		return Bender.get(AccountUUIDs.findEntityFromUUID(world, playerId));
	}
}