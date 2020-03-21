/***********************************************************************************
 * Copyright (c) 2018 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * ProjectSWG is the first NGE emulator for Star Wars Galaxies founded on          *
 * July 7th, 2011 after SOE announced the official shutdown of Star Wars Galaxies. *
 * Our goal is to create an emulator which will provide a server for players to    *
 * continue playing a game similar to the one they used to play. We are basing     *
 * it on the final publish of the game prior to end-game events.                   *
 *                                                                                 *
 * This file is part of PSWGCommon.                                                *
 *                                                                                 *
 * --------------------------------------------------------------------------------*
 *                                                                                 *
 * PSWGCommon is free software: you can redistribute it and/or modify              *
 * it under the terms of the GNU Affero General Public License as                  *
 * published by the Free Software Foundation, either version 3 of the              *
 * License, or (at your option) any later version.                                 *
 *                                                                                 *
 * PSWGCommon is distributed in the hope that it will be useful,                   *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with PSWGCommon.  If not, see <http://www.gnu.org/licenses/>.             *
 ***********************************************************************************/

package com.projectswg.holocore.resources.support.objects.radial.pet;

import com.projectswg.common.data.radial.RadialItem;
import com.projectswg.common.data.radial.RadialOption;
import com.projectswg.holocore.intents.gameplay.world.travel.pet.DismountIntent;
import com.projectswg.holocore.intents.gameplay.world.travel.pet.MountIntent;
import com.projectswg.holocore.intents.gameplay.world.travel.pet.StoreMountIntent;
import com.projectswg.holocore.resources.support.global.player.Player;
import com.projectswg.holocore.resources.support.objects.radial.RadialHandlerInterface;
import com.projectswg.holocore.resources.support.objects.swg.SWGObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.CreatureObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.CreatureState;
import com.projectswg.holocore.resources.support.objects.swg.group.GroupObject;
import com.projectswg.holocore.services.support.objects.ObjectStorageService.ObjectLookup;

import java.util.Collection;

public class VehicleMountRadial implements RadialHandlerInterface {
	
	public VehicleMountRadial() {
		
	}
	
	@Override
	public void getOptions(Collection<RadialOption> options, Player player, SWGObject target) {
		CreatureObject creature = player.getCreatureObject();
		if (!(target instanceof CreatureObject) || !isValidTarget(creature, (CreatureObject) target))
			return;
		
		CreatureObject mount = (CreatureObject) target;
		
		options.add(RadialOption.create(RadialItem.EXAMINE));
		if (creature.isStatesBitmask(CreatureState.RIDING_MOUNT) && creature.getParent() == target)
			options.add(RadialOption.create(RadialItem.ITEM_USE, "@cmd_n:dismount"));
		else
			options.add(RadialOption.create(RadialItem.ITEM_USE, "@cmd_n:mount"));
		
		if (creature.getObjectId() == mount.getOwnerId())
			options.add(RadialOption.create(RadialItem.PET_STORE));
	}
	
	@Override
	public void handleSelection(Player player, SWGObject target, RadialItem selection) {
		CreatureObject creature = player.getCreatureObject();
		if (!(target instanceof CreatureObject) || !isValidTarget(creature, (CreatureObject) target))
			return;
		
		switch (selection) {
			case ITEM_USE:
				if (player.getCreatureObject().getParent() == target)
					DismountIntent.broadcast(creature, (CreatureObject) target);
				else
					MountIntent.broadcast(creature, (CreatureObject) target);
				break;
			case PET_STORE:
				StoreMountIntent.broadcast(creature, (CreatureObject) target);
				break;
			default:
				break;
		}
	}
	
	private static boolean isValidTarget(CreatureObject creature, CreatureObject mount) {
		// Owner of the vehicle
		if (creature.getObjectId() == mount.getOwnerId())
			return true;
		
		// Already mounted
		if (creature.getParent() == mount && mount.isStatesBitmask(CreatureState.MOUNTED_CREATURE) && creature.isStatesBitmask(CreatureState.RIDING_MOUNT))
			return true;
		
		// Within the same group
		GroupObject group = (GroupObject) ObjectLookup.getObjectById(creature.getGroupId());
		if (group == null || !group.getGroupMembers().values().contains(mount.getOwnerId())) {
			return false;
		}
		
		// Owner is already mounted
		return mount.getSlottedObject("rider") != null;
	}
	
}
