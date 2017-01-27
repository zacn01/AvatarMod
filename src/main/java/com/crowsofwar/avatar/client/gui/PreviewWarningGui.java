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
package com.crowsofwar.avatar.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewWarningGui extends GuiScreen {
	
	@Override
	public void initGui() {
		this.buttonList.clear();
		
		this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height / 4 + 120 + 12,
				I18n.format("gui.toTitle", new Object[0])));
		
		this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 155 + 160, this.height / 4 + 120 + 12,
				I18n.format("menu.quit", new Object[0])));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			this.mc.displayGuiScreen(new GuiMainMenu());
		} else if (button.id == 1) {
			this.mc.shutdown();
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		// @formatter:off
		String[] lines = {
			"Warning: Avatar Mod Preview Version",
			"",
			"You are running a preview version of the Avatar Mod.",
			"This is made so fans can get a glimpse of upcoming releases,",
			"and so people can critique/suggest tweaks to the mod.",
			"",
			"While you are not required to, I would really appreciate it",
			"if you gave some feedback on the new changes in the form of",
			"an e-mail or a forum post. I'll use these suggestions in the",
			"OFFICIAL release to make the mod more fun.",
			"",
			"Thanks!!"
		};
		// @formatter:on
		
		int y = height / 4;
		for (String ln : lines) {
			drawString(fontRendererObj, ln, (width - fontRendererObj.getStringWidth(ln)) / 2, y, 0xffffff);
			y += fontRendererObj.FONT_HEIGHT;
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}