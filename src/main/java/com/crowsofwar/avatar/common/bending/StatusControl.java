package com.crowsofwar.avatar.common.bending;

import java.util.function.Consumer;

import com.crowsofwar.avatar.common.controls.AvatarControl;

/**
 * Describes a temporary effect where a callback listener is added to a control event. The listener
 * then will perform certain actions associated with that control.
 * <p>
 * For example, the player receives a place-block Status Control, which subscribes to right-click.
 * The status control receives a callback whenever the player uses the right-click control. Then,
 * the status control is removed.
 * <p>
 * Status controls are stored in player-data, but are also sent to the client via packets, which
 * render over the crosshair.
 * 
 * @author CrowsOfWar
 */
public enum StatusControl {
	
	AIR_JUMP(ctx -> {
		System.out.println("jump " + ctx.getPlayerEntity());
	}, 0, AvatarControl.CONTROL_LEFT_CLICK); // TODO Add control_space
	
	private final int texture;
	private final Consumer<AbilityContext> callback;
	private final AvatarControl control;
	
	private StatusControl(Consumer<AbilityContext> callback, int texture, AvatarControl subscribeTo) {
		this.texture = texture;
		this.callback = callback;
		this.control = subscribeTo;
	}
	
	public int id() {
		return ordinal() + 1;
	}
	
	public AvatarControl getSubscribedControl() {
		return control;
	}
	
	public void execute(AbilityContext ctx) {
		callback.accept(ctx);
	}
	
	public static StatusControl lookup(int id) {
		return values()[id - 1];
	}
	
}