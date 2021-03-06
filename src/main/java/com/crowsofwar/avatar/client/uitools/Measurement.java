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
package com.crowsofwar.avatar.client.uitools;

import static com.crowsofwar.avatar.client.uitools.ScreenInfo.screenHeight;
import static com.crowsofwar.avatar.client.uitools.ScreenInfo.screenWidth;

/**
 * A measurement that can either be in screen pixels or percentage of total
 * screen. Measurements keep track of x and y values, and are immutable.
 * 
 * @author CrowsOfWar
 */
public class Measurement {
	
	private final Frame frame;
	private final float x, y;
	
	private Measurement(Frame frame, float x, float y) {
		this.frame = frame;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the x-value of the measurement in pixels.
	 */
	public float xInPixels() {
		return x;
	}
	
	/**
	 * Returns the y-value of the measurement in pixels.
	 */
	public float yInPixels() {
		return y;
	}
	
	/**
	 * Returns the x-value of the measurement in percentage of screen width from
	 * 0-100.
	 */
	public float xInPercent() {
		return x / screenWidth() * 100;
	}
	
	/**
	 * Returns the y-value of the measurement in percentage of screen height
	 * from 0-100.
	 */
	public float yInPercent() {
		return y / screenHeight() * 100;
	}
	
	/**
	 * Returns a new measurement scaled by the given factor.
	 */
	public Measurement times(float scl) {
		return new Measurement(frame, x * scl, y * scl);
	}
	
	/**
	 * Returns a new measurement based off of this coordinates plus the other
	 * coordinates.
	 */
	public Measurement plus(Measurement m) {
		return new Measurement(frame, this.x + m.x, this.y + m.y);
	}
	
	public static Measurement fromPixels(float x, float y) {
		return fromPixels(Frame.SCREEN, x, y);
	}
	
	public static Measurement fromPixels(Frame frame, float x, float y) {
		return new Measurement(frame, x, y);
	}
	
	public static Measurement fromPercent(float pctX, float pctY) {
		return fromPercent(Frame.SCREEN, pctX, pctY);
	}
	
	/**
	 * Percent is from 0-100.
	 */
	public static Measurement fromPercent(Frame frame, float pctX, float pctY) {
		Measurement dim = frame.getDimensions();
		return new Measurement(frame, dim.x * pctX / 100, dim.y * pctY / 100);
	}
	
}
