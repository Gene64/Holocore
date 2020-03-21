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
package com.projectswg.holocore.resources.support.objects.swg.creature;

import com.projectswg.common.data.CRC;
import com.projectswg.common.data.HologramColour;
import com.projectswg.common.data.encodables.mongo.MongoData;
import com.projectswg.common.data.encodables.mongo.MongoPersistable;
import com.projectswg.common.network.NetBuffer;
import com.projectswg.common.network.NetBufferStream;
import com.projectswg.common.network.packets.swg.zone.object_controller.BuffAddUpdate;
import com.projectswg.common.network.packets.swg.zone.object_controller.BuffRemoveUpdate;
import com.projectswg.common.persistable.Persistable;
import com.projectswg.holocore.resources.gameplay.player.group.GroupInviterData;
import com.projectswg.holocore.resources.support.data.collections.SWGList;
import com.projectswg.holocore.resources.support.data.collections.SWGMap;
import com.projectswg.holocore.resources.support.data.persistable.SWGObjectFactory;
import com.projectswg.holocore.resources.support.global.network.BaselineBuilder;
import com.projectswg.holocore.resources.support.global.player.Player;
import com.projectswg.holocore.resources.support.objects.Equipment;
import com.projectswg.holocore.resources.support.objects.swg.SWGObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.attributes.AttributesMutable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class CreatureObjectSharedNP implements Persistable, MongoPersistable {
	
	private final CreatureObject obj;
	
	private transient GroupInviterData inviterData	= new GroupInviterData(0, null, "", 0);
	private transient long groupId			= 0;
	
	private short	level					= 1;
	private int		levelHealthGranted		= 0;
	private String	animation				= "";
	private String	moodAnimation			= "neutral";
	private long equippedWeapon		= 0;
	private int		guildId					= 0;
	private long 	lookAtTargetId			= 0;
	private long 	intendedTargetId		= 0;
	private byte	moodId					= 0;
	private int 	performanceCounter		= 0;
	private int 	performanceId			= 0;
	private String 	costume					= "";
	private boolean visible					= true;
	private boolean performing				= false;
	private CreatureDifficulty	difficulty	= CreatureDifficulty.NORMAL;
	private HologramColour hologramColour	= HologramColour.DEFAULT;
	private boolean shownOnRadar			= true;
	private boolean beast					= false;
	
	private AttributesMutable	attributes;
	private AttributesMutable	maxAttributes;
	private SWGList<Equipment>	equipmentList 	= SWGList.Companion.createEncodableList(6, 23, Equipment::new);
	private SWGList<Equipment>	appearanceList 	= SWGList.Companion.createEncodableList(6, 33, Equipment::new);
	
	private SWGMap<CRC, Buff>	buffs			= new SWGMap<>(6, 26);
	
	public CreatureObjectSharedNP(CreatureObject obj) {
		this.obj = obj;
		this.attributes = new AttributesMutable(obj, 6, 21);
		this.maxAttributes = new AttributesMutable(obj, 6, 22);
		initCurrentAttributes();
		initMaxAttributes();
	}
	
	public void addEquipment(SWGObject obj, SWGObject target) {
		if (getEquipment(obj) != null)
			return;
		synchronized (equipmentList) {
			equipmentList.add(new Equipment(obj));
			equipmentList.sendDeltaMessage(target);
		}
	}
	
	public void removeEquipment(SWGObject obj, SWGObject target) {
		Equipment e = getEquipment(obj);
		if (e == null)
			return;
		synchronized (equipmentList) {
			equipmentList.remove(e);
			equipmentList.sendDeltaMessage(target);
		}
	}
	
	public Equipment getEquipment(SWGObject obj) {
		synchronized (equipmentList) {
			for (Equipment equipment : equipmentList) {
				if (equipment.getObjectId() == obj.getObjectId()) {
					return equipment;
				}
			}
		}
		return null;
	}
	
	public void addAppearanceItem(SWGObject obj, SWGObject target) {
		synchronized (appearanceList) {
			appearanceList.add(new Equipment(obj));
			appearanceList.sendDeltaMessage(target);
		}
	}
	
	public void removeAppearanceItem(SWGObject obj, SWGObject target) {
		Equipment e = getEquipment(obj);
		if (e == null)
			return;
		synchronized (appearanceList) {
			appearanceList.remove(e);
			appearanceList.sendDeltaMessage(target);
		}
	}
	
	public Equipment getAppearance(SWGObject obj) {
		synchronized (appearanceList) {
			for (Equipment equipment : appearanceList) {
				if (equipment.getObjectId() == obj.getObjectId()) {
					return equipment;
				}
			}
		}
		return null;
	}
	
	public SWGList<Equipment> getEquipmentList() {
		return equipmentList;
	}
	
	public SWGList<Equipment> getAppearanceList() {
		return appearanceList;
	}
	
	public void setGuildId(int guildId) {
		this.guildId = guildId;
	}
	
	public void setLevel(int level) {
		this.level = (short) level;
	}
	
	public void setLevelHealthGranted(int levelHealthGranted) {
		this.levelHealthGranted = levelHealthGranted;
	}
	
	public void setDifficulty(CreatureDifficulty difficulty) {
		this.difficulty = difficulty;
	}
	
	public void setMoodAnimation(String moodAnimation) {
		this.moodAnimation = moodAnimation;
	}
	
	public void setBeast(boolean beast) {
		this.beast = beast;
	}
	
	public void setEquippedWeapon(long weaponId) {
		this.equippedWeapon = weaponId;
	}
	
	public void setMoodId(byte moodId) {
		this.moodId = moodId;
	}
	
	public void setLookAtTargetId(long lookAtTargetId) {
		this.lookAtTargetId = lookAtTargetId;
	}
	
	public void setIntendedTargetId(long intendedTargetId) {
		this.intendedTargetId = intendedTargetId;
	}
	
	public void setPerformanceCounter(int performanceCounter) {
		this.performanceCounter = performanceCounter;
	}
	
	public void setPerformanceId(int performanceId) {
		this.performanceId = performanceId;
	}
	
	public int getGuildId() {
		return guildId;
	}
	
	public short getLevel() {
		return level;
	}
	
	public int getLevelHealthGranted() {
		return levelHealthGranted;
	}
	
	public CreatureDifficulty getDifficulty() {
		return difficulty;
	}
	
	public String getCostume() {
		return costume;
	}
	
	public void setCostume(String costume) {
		this.costume = costume;
	}
	
	public void updateGroupInviteData(Player sender, long groupId, String name) {
		inviterData.setName(name);
		inviterData.setSender(sender);
		inviterData.setId(groupId);
		inviterData.incrementCounter();
	}
	
	public long getGroupId() {
		return groupId;
	}
	
	public GroupInviterData getInviterData() {
		return inviterData;
	}
	
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	
	public String getAnimation() {
		return animation;
	}
	
	public byte getMoodId() {
		return moodId;
	}
	
	public long getLookAtTargetId() {
		return lookAtTargetId;
	}
	
	public long getIntendedTargetId() {
		return intendedTargetId;
	}
	
	public int getPerformanceCounter() {
		return performanceCounter;
	}
	
	public int getPerformanceId() {
		return performanceId;
	}
	
	public void setAnimation(String animation) {
		this.animation = animation;
	}
	
	public String getMoodAnimation() {
		return moodAnimation;
	}
	
	public boolean isBeast() {
		return beast;
	}
	
	public long getEquippedWeapon() {
		return equippedWeapon;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isPerforming() {
		return performing;
	}

	public void setPerforming(boolean performing) {
		this.performing = performing;
	}
	
	public HologramColour getHologramColor() {
		return hologramColour;
	}
	
	public void setHologramColour(HologramColour hologramColour) {
		this.hologramColour = hologramColour;
	}

	public boolean isShownOnRadar() {
		return shownOnRadar;
	}

	public void setShownOnRadar(boolean shownOnRadar) {
		this.shownOnRadar = shownOnRadar;
	}

	public int getHealth() {
		return attributes.getHealth();
	}
	
	public int getMaxHealth() {
		return maxAttributes.getHealth();
	}
	
	public int getAction() {
		return attributes.getAction();
	}
	
	public int getMaxAction() {
		return maxAttributes.getAction();
	}
	
	public int getMind() {
		return attributes.getMind();
	}
	
	public int getMaxMind() {
		return maxAttributes.getMind();
	}
	
	public void setHealth(int health) {
		attributes.setHealth(health);
	}
	
	public void modifyHealth(int mod) {
		attributes.modifyHealth(mod, maxAttributes.getHealth());
	}
	
	public void setMaxHealth(int maxHealth) {
		maxAttributes.setHealth(maxHealth);
	}
	
	public void setAction(int action) {
		attributes.setAction(action);
	}
	
	public void modifyAction(int mod) {
		attributes.modifyAction(mod, maxAttributes.getAction());
	}
	
	public void setMaxAction(int maxAction) {
		maxAttributes.setAction(maxAction);
	}
	
	public void setMind(int mind) {
		attributes.setMind(mind);
	}
	
	public void modifyMind(int mod) {
		attributes.modifyMind(mod, maxAttributes.getMind());
	}
	
	public void setMaxMind(int maxMind) {
		maxAttributes.setMind(maxMind);
	}
	
	public void putBuff(Buff buff, SWGObject target) {
		synchronized (buffs) {
			CRC crc = new CRC(buff.getCrc());
			assert !buffs.containsKey(crc) : "Cannot add a buff twice!";
			buffs.put(crc, buff);
			target.sendObservers(new BuffAddUpdate(target.getObjectId(), crc.getCrc(), buff.getDuration()));
			buffs.sendDeltaMessage(target);
		}
	}
	
	public Buff removeBuff(CRC buffCrc, SWGObject target) {
		synchronized (buffs) {
			Buff removedBuff = buffs.remove(buffCrc);
			if (removedBuff != null) {
				target.sendObservers(new BuffRemoveUpdate(target.getObjectId(), buffCrc.getCrc()));
				buffs.sendDeltaMessage(target);
			}
			
			return removedBuff;
		}
	}
	
	public Stream<Buff> getBuffEntries(Predicate<Buff> predicate) {
		synchronized (buffs) {
			return new ArrayList<>(buffs.values()).stream().filter(predicate);
		}
	}
	
	public void adjustBuffStackCount(CRC buffCrc, int adjustment, SWGObject target) {
		safeModifyBuff(buffCrc, target, buff -> buff.adjustStackCount(adjustment));
	}
	
	public void setBuffDuration(CRC buffCrc, int playTime, int duration, SWGObject target) {
		safeModifyBuff(buffCrc, target, buff -> {
			buff.setEndTime(playTime + duration);
			buff.setDuration(duration);
		});
	}
	
	private void safeModifyBuff(CRC buffCrc, SWGObject target, Consumer<Buff> operation) {
		synchronized (buffs) {
			Buff buff = buffs.get(buffCrc);
			Objects.requireNonNull(buff, "Buff cannot be null");
			operation.accept(buff);
			buffs.update(buffCrc);
			buffs.sendDeltaMessage(target);
		}
	}
	
	private void initMaxAttributes() {
		maxAttributes.setHealth(1000);
		maxAttributes.setAction(300);
		maxAttributes.setMind(300);
	}
	
	private void initCurrentAttributes() {
		attributes.setHealth(1000);
		attributes.setAction(300);
		attributes.setMind(300);
	}
	
	public void createBaseline6(Player target, BaselineBuilder bb) {
		bb.addShort(level); // 8
		bb.addInt(levelHealthGranted); // 9
		bb.addAscii(animation); // 10
		bb.addAscii(moodAnimation); // 11
		bb.addLong(equippedWeapon); // 12
		bb.addLong(groupId); // 13
		bb.addObject(inviterData); // 14
		bb.addInt(guildId); // 15
		bb.addLong(lookAtTargetId); // 16
		bb.addLong(intendedTargetId); // 17
		bb.addByte(moodId); // 18
		bb.addInt(performanceCounter); // 19
		bb.addInt(performanceId); // 20
		bb.addObject(attributes); // 21
		bb.addObject(maxAttributes); // 22
		bb.addObject(equipmentList); // 23
		bb.addAscii(costume); // 24
		bb.addBoolean(visible); // 25
		bb.addObject(buffs); // 26
		bb.addBoolean(performing); // 27
		bb.addByte(difficulty.getDifficulty()); // 28
		bb.addInt((hologramColour == null) ? -1 : hologramColour.getValue()); // Hologram Color -- 29
		bb.addBoolean(shownOnRadar); // 30
		bb.addBoolean(beast); // 31
		bb.addByte(0); // forceShowHam? -- 32
		bb.addObject(appearanceList); // 33
		bb.addLong(0); // decoy? -- 34
		
		bb.incrementOperandCount(27);
	}
	
	public void parseBaseline6(NetBuffer buffer) {
		level = buffer.getShort();
		levelHealthGranted = buffer.getInt();
		animation = buffer.getAscii();
		moodAnimation = buffer.getAscii();
		equippedWeapon = buffer.getLong();
		groupId = buffer.getLong();
		inviterData = buffer.getEncodable(GroupInviterData.class);
		guildId = buffer.getInt();
		lookAtTargetId = buffer.getLong();
		intendedTargetId = buffer.getLong();
		moodId = buffer.getByte();
		performanceCounter = buffer.getInt();
		performanceId = buffer.getInt();
		attributes.decode(buffer);
		maxAttributes.decode(buffer);
		equipmentList = SWGList.getSwgList(buffer, 6, 23, Equipment.class);
		costume = buffer.getAscii();
		visible = buffer.getBoolean();
		buffs = SWGMap.getSwgMap(buffer, 6, 26, CRC.class, Buff.class);
		performing = buffer.getBoolean();
		difficulty = CreatureDifficulty.getForDifficulty(buffer.getByte());
		hologramColour = HologramColour.getForValue(buffer.getInt());
		shownOnRadar = buffer.getBoolean();
		beast = buffer.getBoolean();
		buffer.getBoolean();
		appearanceList = SWGList.getSwgList(buffer, 6, 33, Equipment.class);
		buffer.getLong();
	}
	
	@Override
	public void saveMongo(MongoData data) {
		data.putInteger("level", level);
		data.putInteger("levelHealthGranted", levelHealthGranted);
		data.putString("animation", animation);
		data.putString("moodAnimation", moodAnimation);
		data.putInteger("guildId", guildId);
		data.putLong("lookAtTargetId", lookAtTargetId);
		data.putLong("intendedTargetId", intendedTargetId);
		data.putInteger("moodId", moodId);
		data.putString("costume", costume);
		data.putBoolean("visible", visible);
		data.putBoolean("shownOnRadar", shownOnRadar);
		data.putBoolean("beast", beast);
		data.putString("difficulty", difficulty.name());
		data.putString("hologramColor", hologramColour.name());
		data.putLong("equippedWeapon", equippedWeapon);
		data.putDocument("attributes", attributes);
		data.putDocument("maxAttributes", maxAttributes);
		data.putMap("buffs", buffs);
	}
	
	@Override
	public void readMongo(MongoData data) {
		buffs.clear();
		
		level = (short) data.getInteger("level", level);
		levelHealthGranted = data.getInteger("levelHealthGranted", levelHealthGranted);
		animation = data.getString("animation", animation);
		moodAnimation = data.getString("moodAnimation", moodAnimation);
		guildId = data.getInteger("guildId", guildId);
		lookAtTargetId = data.getLong("lookAtTargetId", lookAtTargetId);
		intendedTargetId = data.getLong("intendedTargetId", intendedTargetId);
		moodId = (byte) data.getInteger("moodId", moodId);
		costume = data.getString("costume", costume);
		visible = data.getBoolean("visible", visible);
		shownOnRadar = data.getBoolean("shownOnRadar", shownOnRadar);
		beast = data.getBoolean("beast", beast);
		difficulty = CreatureDifficulty.valueOf(data.getString("difficulty", difficulty.name()));
		hologramColour = HologramColour.valueOf(data.getString("hologramColor", hologramColour.name()));
		equippedWeapon = data.getLong("equippedWeapon", equippedWeapon);
		data.getDocument("attributes", attributes);
		data.getDocument("maxAttributes", maxAttributes);
		buffs.putAll(data.getMap("buffs", CRC.class, Buff.class));
	}
	
	@Override
	public void save(NetBufferStream stream) {
		stream.addByte(5);
		stream.addShort(level);
		stream.addInt(levelHealthGranted);
		stream.addAscii(animation);
		stream.addAscii(moodAnimation);
		stream.addInt(guildId);
		stream.addLong(lookAtTargetId);
		stream.addLong(intendedTargetId);
		stream.addByte(moodId);
		stream.addAscii(costume);
		stream.addBoolean(visible);
		stream.addBoolean(shownOnRadar);
		stream.addBoolean(beast);
		stream.addAscii(difficulty.name());
		stream.addAscii(hologramColour.name());
		stream.addLong(equippedWeapon);
		
		maxAttributes.save(stream);
		synchronized (buffs) {
			stream.addMap(buffs, (e) -> e.getValue().save(stream));
		}
	}
	
	@Override
	public void read(NetBufferStream stream) {
		switch(stream.getByte()) {
			case 0: readVersion0(stream); break;
			case 1: readVersion1(stream); break;
			case 2: readVersion2(stream); break;
			case 3: readVersion3(stream); break;
			case 4: readVersion4(stream); break;
			case 5: readVersion5(stream); break;
		}
		attributes.setHealth(maxAttributes.getHealth());
		attributes.setHealthRegen(maxAttributes.getHealthRegen());
		attributes.setAction(maxAttributes.getAction());
		attributes.setActionRegen(maxAttributes.getActionRegen());
		attributes.setMind(maxAttributes.getMind());
		attributes.setMindRegen(maxAttributes.getMindRegen());
	}
	
	private void readVersion0(NetBufferStream stream) {
		level = stream.getShort();
		levelHealthGranted = stream.getInt();
		animation = stream.getAscii();
		moodAnimation = stream.getAscii();
		guildId = stream.getInt();
		lookAtTargetId = stream.getLong();
		intendedTargetId = stream.getLong();
		moodId = stream.getByte();
		costume = stream.getAscii();
		visible = stream.getBoolean();
		shownOnRadar = stream.getBoolean();
		beast = stream.getBoolean();
		difficulty = CreatureDifficulty.valueOf(stream.getAscii());
		hologramColour = HologramColour.valueOf(stream.getAscii());
		if (stream.getBoolean())
			equippedWeapon = SWGObjectFactory.create(stream).getObjectId();
		readAttributes((byte) 0, attributes, stream);
		readAttributes((byte) 0, maxAttributes, stream);
	}
	
	private void readVersion1(NetBufferStream stream) {
		level = stream.getShort();
		levelHealthGranted = stream.getInt();
		animation = stream.getAscii();
		moodAnimation = stream.getAscii();
		guildId = stream.getInt();
		lookAtTargetId = stream.getLong();
		intendedTargetId = stream.getLong();
		moodId = stream.getByte();
		costume = stream.getAscii();
		visible = stream.getBoolean();
		shownOnRadar = stream.getBoolean();
		beast = stream.getBoolean();
		difficulty = CreatureDifficulty.valueOf(stream.getAscii());
		hologramColour = HologramColour.valueOf(stream.getAscii());
		if (stream.getBoolean())
			equippedWeapon = SWGObjectFactory.create(stream).getObjectId();
		readAttributes((byte) 1, maxAttributes, stream);
	}
	
	private void readVersion2(NetBufferStream stream) {
		readVersion1(stream);
		stream.getList((i) -> {
			CRC crc = new CRC();
			Buff buff = new Buff();
			
			crc.read(stream);
			buff.readOld(stream); // old buff persistence did not have version byte
			buff.setCrc(crc.getCrc());
			buffs.put(crc, buff);
		});
	}
	
	private void readVersion3(NetBufferStream stream) {
		readVersion1(stream);
		stream.getList((i) -> {
			Buff buff = new Buff();
			
			buff.read(stream);
			buffs.put(new CRC(buff.getCrc()), buff);
		});
	}
	
	private void readVersion4(NetBufferStream stream) {
		level = stream.getShort();
		levelHealthGranted = stream.getInt();
		animation = stream.getAscii();
		moodAnimation = stream.getAscii();
		guildId = stream.getInt();
		lookAtTargetId = stream.getLong();
		intendedTargetId = stream.getLong();
		moodId = stream.getByte();
		costume = stream.getAscii();
		visible = stream.getBoolean();
		shownOnRadar = stream.getBoolean();
		beast = stream.getBoolean();
		difficulty = CreatureDifficulty.valueOf(stream.getAscii());
		hologramColour = HologramColour.valueOf(stream.getAscii());
		equippedWeapon = stream.getLong();
		readAttributes((byte) 4, maxAttributes, stream);
		stream.getList((i) -> {
			Buff buff = new Buff();
			
			buff.read(stream);
			buffs.put(new CRC(buff.getCrc()), buff);
		});
	}
	
	private void readVersion5(NetBufferStream stream) {
		level = stream.getShort();
		levelHealthGranted = stream.getInt();
		animation = stream.getAscii();
		moodAnimation = stream.getAscii();
		guildId = stream.getInt();
		lookAtTargetId = stream.getLong();
		intendedTargetId = stream.getLong();
		moodId = stream.getByte();
		costume = stream.getAscii();
		visible = stream.getBoolean();
		shownOnRadar = stream.getBoolean();
		beast = stream.getBoolean();
		difficulty = CreatureDifficulty.valueOf(stream.getAscii());
		hologramColour = HologramColour.valueOf(stream.getAscii());
		equippedWeapon = stream.getLong();
		maxAttributes.read(stream);
		stream.getList((i) -> {
			Buff buff = new Buff();
			
			buff.read(stream);
			buffs.put(new CRC(buff.getCrc()), buff);
		});
	}
	
	private static void readAttributes(byte ver, AttributesMutable attributes, NetBufferStream stream) {
		if (ver <= 4) {
			int [] array = new int[6];
			stream.getList((i) -> array[i] = stream.getInt());
			attributes.setHealth(array[0]);
			attributes.setHealthRegen(array[1]);
			attributes.setAction(array[2]);
			attributes.setActionRegen(array[3]);
			attributes.setMind(array[4]);
			attributes.setMindRegen(array[5]);
		} else {
			attributes.read(stream);
		}
		
	}
	
}
