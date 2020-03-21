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
package com.projectswg.holocore.intents.gameplay.gcw.faction;

import com.projectswg.common.data.encodables.tangible.PvpStatus;
import com.projectswg.holocore.resources.support.data.server_info.loader.combat.FactionLoader.Faction;
import com.projectswg.holocore.resources.support.objects.swg.tangible.TangibleObject;
import me.joshlarson.jlcommon.control.Intent;

public class FactionIntent extends Intent {
	
	private TangibleObject target;
	private Faction newFaction;
	private FactionIntentType updateType;
	private PvpStatus newStatus;
	
	private FactionIntent(TangibleObject target) {
		this.target = target;
	}
	
	public FactionIntent(TangibleObject target, FactionIntentType updateType) {
		this(target);
		this.updateType = updateType;
	}
	
	public FactionIntent(TangibleObject target, Faction newFaction) {
		this(target, FactionIntentType.FACTIONUPDATE);
		this.newFaction = newFaction;
	}
	
	public FactionIntent(TangibleObject target, PvpStatus newStatus) {
		this(target, FactionIntentType.STATUSUPDATE);
		this.newStatus = newStatus;
	}
	
	public TangibleObject getTarget() {
		return target;
	}
	
	public Faction getNewFaction() {
		return newFaction;
	}
	
	public FactionIntentType getUpdateType() {
		return updateType;
	}
	
	public PvpStatus getNewStatus() {
		return newStatus;
	}
	
	public static void broadcastUpdateFlags(TangibleObject target) {
		new FactionIntent(target, FactionIntentType.FLAGUPDATE).broadcast();
	}
	
	public static void broadcastStartSwitch(TangibleObject target) {
		new FactionIntent(target, FactionIntentType.STATUSUPDATE).broadcast();
	}
	
	public static void broadcastUpdateFaction(TangibleObject target, Faction newFaction) {
		new FactionIntent(target, newFaction).broadcast();
	}
	
	public static void broadcastUpdateStatus(TangibleObject target, PvpStatus newStatus) {
		new FactionIntent(target, newStatus).broadcast();
	}
	
	public enum FactionIntentType {
		FLAGUPDATE,
		SWITCHUPDATE,
		STATUSUPDATE,
		FACTIONUPDATE // Is automatically set in the correct constructor, don't use manually.
	}
	
}
