/***********************************************************************************
 * Copyright (c) 2018 /// Project SWG /// www.projectswg.com                       *
 *                                                                                 *
 * ProjectSWG is the first NGE emulator for Star Wars Galaxies founded on          *
 * July 7th, 2011 after SOE announced the official shutdown of Star Wars Galaxies. *
 * Our goal is to create an emulator which will provide a server for players to    *
 * continue playing a game similar to the one they used to play. We are basing     *
 * it on the final publish of the game prior to end-game events.                   *
 *                                                                                 *
 * This file is part of Holocore.                                                  *
 *                                                                                 *
 * --------------------------------------------------------------------------------*
 *                                                                                 *
 * Holocore is free software: you can redistribute it and/or modify                *
 * it under the terms of the GNU Affero General Public License as                  *
 * published by the Free Software Foundation, either version 3 of the              *
 * License, or (at your option) any later version.                                 *
 *                                                                                 *
 * Holocore is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                  *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                   *
 * GNU Affero General Public License for more details.                             *
 *                                                                                 *
 * You should have received a copy of the GNU Affero General Public License        *
 * along with Holocore.  If not, see <http://www.gnu.org/licenses/>.               *
 ***********************************************************************************/
package com.projectswg.holocore.resources.support.global.network;

import com.projectswg.common.encoding.Encoder;
import com.projectswg.common.encoding.StringType;
import com.projectswg.common.network.packets.swg.zone.baselines.Baseline.BaselineType;
import com.projectswg.common.network.packets.swg.zone.deltas.DeltasMessage;
import com.projectswg.holocore.resources.support.global.player.Player;
import com.projectswg.holocore.resources.support.objects.swg.SWGObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.CreatureObject;

public class DeltaBuilder {
	
	public static void send(SWGObject object, BaselineType type, int num, int updateType, Object change) {
		send(object, type, num, updateType, (change instanceof byte[] ? (byte[]) change : Encoder.encode(change)));
	}
	
	public static void send(SWGObject object, BaselineType type, int num, int updateType, Object change, StringType strType) {
		send(object, type, num, updateType, (change instanceof byte[] ? (byte[]) change : Encoder.encode(change, strType)));
	}
	
	private static void send(SWGObject object, BaselineType type, int num, int updateType, byte [] data) {
		DeltasMessage delta = new DeltasMessage(object.getObjectId(), type, num, updateType, data);
		if (num == 3 || num == 6) { // Shared Objects
			for (CreatureObject observer : object.getObserverCreatures()) {
				observer.addDelta(delta);
			}
		} else {
			Player owner = object.getOwner();
			if (owner != null) {
				CreatureObject observerSelf = owner.getCreatureObject();
				if (observerSelf != null) {
					observerSelf.addDelta(delta);
				}
			}
		}
	}
	
}
