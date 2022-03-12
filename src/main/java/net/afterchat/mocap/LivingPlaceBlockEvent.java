/**
 * The Mocap mod is a Minecraft mod aimed at film-makers who wish to
 * record the motion of their character and replay it back via a scripted
 * entity. It allows you to create a whole series of composite characters
 * for use in your own videos without having to splash out on extra accounts
 * or enlist help.
 * 
 * Copyright (C) 2013-2014 Echeb Keso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.*
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.afterchat.mocap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LivingPlaceBlockEvent extends LivingEvent {
	public ItemStack theItem;
	public int xCoord;
	public int yCoord;
	public int zCoord;

	/*public LivingPlaceBlockEvent(EntityLivingBase entity, ItemStack theItem,
			int x, int y, int z) {
		super(entity);
		this.theItem = theItem;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
	}*/

	/**
	 * another constructor for the injection to work easily
	 */
	public LivingPlaceBlockEvent(EntityLivingBase entity, ItemStack theItem, BlockPos pos) {
		super(entity);
		this.theItem = theItem;
		this.xCoord = pos.getX();
		this.yCoord = pos.getY();
		this.zCoord = pos.getZ();
	}
}
