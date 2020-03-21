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
package com.projectswg.holocore.services.gameplay.player.experience.skills;

import com.projectswg.common.data.RGB;
import com.projectswg.common.data.encodables.oob.StringId;
import com.projectswg.common.data.encodables.tangible.Race;
import com.projectswg.common.network.packets.swg.zone.PlayClientEffectObjectMessage;
import com.projectswg.common.network.packets.swg.zone.PlayMusicMessage;
import com.projectswg.common.network.packets.swg.zone.object_controller.ShowFlyText;
import com.projectswg.common.network.packets.swg.zone.object_controller.ShowFlyText.Scale;
import com.projectswg.holocore.intents.gameplay.player.badge.GrantBadgeIntent;
import com.projectswg.holocore.intents.gameplay.player.experience.LevelChangedIntent;
import com.projectswg.holocore.intents.gameplay.player.experience.skills.GrantSkillIntent;
import com.projectswg.holocore.intents.support.objects.items.CreateStaticItemIntent;
import com.projectswg.holocore.intents.support.objects.swg.ObjectCreatedIntent;
import com.projectswg.holocore.resources.support.data.server_info.loader.DataLoader;
import com.projectswg.holocore.resources.support.data.server_info.loader.RoadmapRewardLoader.RoadmapRewardInfo;
import com.projectswg.holocore.resources.support.data.server_info.loader.SkillTemplateLoader.SkillTemplateInfo;
import com.projectswg.holocore.resources.support.global.player.Player;
import com.projectswg.holocore.resources.support.objects.ObjectCreator;
import com.projectswg.holocore.resources.support.objects.swg.SWGObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.CreatureObject;
import com.projectswg.holocore.resources.support.objects.swg.player.PlayerObject;
import com.projectswg.holocore.resources.support.objects.swg.player.Profession;
import com.projectswg.holocore.services.support.objects.items.StaticItemService;
import me.joshlarson.jlcommon.control.IntentHandler;
import me.joshlarson.jlcommon.control.Service;
import me.joshlarson.jlcommon.log.Log;

import java.util.*;

/**
 * This is a service that listens for {@link LevelChangedIntent} and grants
 * everything linked to a skillbox.
 * @author Mads
 */
public final class SkillTemplateService extends Service {
	
	private final Map<String, String[]> badgeNames;

	public SkillTemplateService() {
		badgeNames = new HashMap<>();
		badgeNames.put("bounty_hunter_1a", 	new String[]{"new_prof_bountyhunter_master"});
		badgeNames.put("commando_1a", 		new String[]{"new_prof_commando_master"});
		badgeNames.put("entertainer_1a",	new String[]{"new_prof_social_entertainer_master"});
		badgeNames.put("force_sensitive_1a",new String[]{"new_prof_jedi_master"});
		badgeNames.put("medic_1a", 			new String[]{"new_prof_medic_master"});
		badgeNames.put("officer_1a", 		new String[]{"new_prof_officer_master"});
		badgeNames.put("smuggler_1a", 		new String[]{"new_prof_smuggler_master"});
		badgeNames.put("spy_1a", 			new String[]{"new_prof_spy_master"});
		// All traders become Master Merchants and Master Artisans
		badgeNames.put("trader_0a", 		new String[]{"new_prof_crafting_merchant_master", "new_prof_crafting_artisan_master", "new_prof_crafting_chef_master", "new_prof_crafting_tailor_master"});
		badgeNames.put("trader_0b", 		new String[]{"new_prof_crafting_merchant_master", "new_prof_crafting_artisan_master", "new_prof_crafting_architect_master"});
		badgeNames.put("trader_0c", 		new String[]{"new_prof_crafting_merchant_master", "new_prof_crafting_artisan_master", "new_prof_crafting_armorsmith_master", "new_prof_crafting_weaponsmith_master"});
		badgeNames.put("trader_0d", 		new String[]{"new_prof_crafting_merchant_master", "new_prof_crafting_artisan_master", "new_prof_crafting_droidengineer_master"});
	}
	
	@IntentHandler
	private void handleLevelChangedIntent(LevelChangedIntent lci) {
		short oldLevel = lci.getPreviousLevel();
		short newLevel = lci.getNewLevel();
		CreatureObject creatureObject = lci.getCreatureObject();
		Player player = creatureObject.getOwner();
		long objectId = creatureObject.getObjectId();
		boolean skillUp = false;
		
		for (int level = oldLevel + 1; level <= newLevel; level++) {
			// Skills are only awarded every third or fourth level
			if ((level == 1 || level == 4 || level == 7 || level == 10) || ((level > 10) && (((level - 10) % 4) == 0))) {
				PlayerObject playerObject = creatureObject.getPlayerObject();
				Profession profession = playerObject.getProfession();
				SkillTemplateInfo templates = DataLoader.Companion.skillTemplates().getTemplateFromName(profession.getClientName());

				if (templates == null) {
					Log.w("%s tried to level up to %d with invalid profession %s", creatureObject, level, profession);
					return;
				}
				
				int skillIndex = (level <= 10) ? ((level - 1) / 3) : (((level - 10) / 4) + 3);

				String skillName = templates.getTemplates()[skillIndex];
				new GrantSkillIntent(GrantSkillIntent.IntentType.GRANT, skillName, creatureObject, true).broadcast();
				playerObject.setProfWheelPosition(skillName);

				// Grants a mastery collection badge, IF they qualify.
				grantMasteryBadge(creatureObject, profession, skillName);
				giveRewardItems(creatureObject, skillName);
				
				skillUp = true;
			}
		}
		
		String effectFile;
		String flyText;
		RGB flyTextColor;
		
		if (skillUp) {
			effectFile = "clienteffect/skill_granted.cef";
			flyText = "skill_up";
			flyTextColor = new RGB(0, 255, 0);
		} else {
			effectFile = "clienteffect/level_granted.cef";
			flyText = "level_up";
			flyTextColor = new RGB(0, 0, 255);
		}
		
		creatureObject.sendObservers(new PlayClientEffectObjectMessage(effectFile, "", objectId, ""));
		creatureObject.sendSelf(new ShowFlyText(objectId, new StringId("cbt_spam", flyText), Scale.LARGEST, flyTextColor));
		
		if (skillUp)
			creatureObject.sendSelf(new PlayMusicMessage(0, "sound/music_acq_bountyhunter.snd", 1, false));
	}

	private void giveRewardItems(CreatureObject creatureObject, String skillName) {
		RoadmapRewardInfo reward = DataLoader.Companion.roadmapRewards().getRewardBySkillName(skillName);
		if (reward == null)
			return; // No reward to give
		
		Race characterRace = creatureObject.getRace();
		String species = characterRace.getSpecies().toUpperCase(Locale.US);
		SWGObject inventory = creatureObject.getSlottedObject("inventory");
		String[] items;

		if (species.equals("ITHORIAN") && reward.getIthorianItems().length > 0)
			items = reward.getIthorianItems();
		else if (species.equals("WOOKIEE") && reward.getWookieeItems().length > 0)
			items = reward.getWookieeItems();
		else
			items = reward.getDefaultItems();

		Collection<String> staticItems = new ArrayList<>();
		
		for (String item : items) {
			if (item.endsWith(".iff")) {
				SWGObject nonStaticItem = ObjectCreator.createObjectFromTemplate(item);
				nonStaticItem.moveToContainer(inventory);
				new ObjectCreatedIntent(nonStaticItem).broadcast();
			} else {
				staticItems.add(item);
			}
		}
		
		// No reason to broadcast this intent if we don't need new static items anyways
		if (!staticItems.isEmpty())
			new CreateStaticItemIntent(creatureObject, inventory, new StaticItemService.LootBoxHandler(creatureObject), staticItems.toArray(String[]::new)).broadcast();
	}
	
	private void grantMasteryBadge(CreatureObject creature, Profession profession, String skillName) {
		Log.d("grantMasteryBadge - skillName: %s", skillName);
		
		if (!skillName.endsWith("_phase4_master")) {
			return;
		}
		
		String[] badges = badgeNames.get(profession.getClientName());
		
		if (badges == null) {
			Log.e("%s could not be granted a mastery badge because their profession %s is unrecognised", creature, profession);
			return;
		}
		
		for (String badgeName : badges) {
			new GrantBadgeIntent(creature, badgeName).broadcast();
			Log.i("Granting badge %s to %s", badgeName, creature);
		}
	}
	
}
