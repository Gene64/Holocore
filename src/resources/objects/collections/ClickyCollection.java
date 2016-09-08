package resources.objects.collections;
/***********************************************************************************
 * Copyright (c) 2015 /// Project SWG /// www.projectswg.com                        *
 *                                                                                  *
 * ProjectSWG is the first NGE emulator for Star Wars Galaxies founded on           *
 * July 7th, 2011 after SOE announced the official shutdown of Star Wars Galaxies.  *
 * Our goal is to create an emulator which will provide a server for players to     *
 * continue playing a game similar to the one they used to play. We are basing      *
 * it on the final publish of the game prior to end-game events.                    *
 *                                                                                  *
 * This file is part of Holocore.                                                   *
 *                                                                                  *
 * -------------------------------------------------------------------------------- *
 *                                                                                  *
 * Holocore is free software: you can redistribute it and/or modify                 *
 * it under the terms of the GNU Affero General Public License as                   *
 * published by the Free Software Foundation, either version 3 of the               *
 * License, or (at your option) any later version.                                  *
 *                                                                                  *
 * Holocore is distributed in the hope that it will be useful,                      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                    *
 * GNU Affero General Public License for more details.                              *
 *                                                                                  *
 * You should have received a copy of the GNU Affero General Public License         *
 * along with Holocore.  If not, see <http://www.gnu.org/licenses/>.                *
 *                                                                                  *
 ***********************************************************************************/

/**
 * Created by Yakattak on 9/8/16.
 */
public class ClickyCollection {
    private String slotName;
    private String collectionName;
    private int objectId;
    private String iffTemplate;
    private String terrain;
    private double x;
    private double y;

    public ClickyCollection(String slotName, String collectionName, int objectId, String iffTemplate, String terrain, double x, double y) {
        this.slotName = slotName;
        this.collectionName = collectionName;
        this.objectId = objectId;
        this.iffTemplate = iffTemplate;
        this.terrain = terrain;
        this.x = x;
        this.y = y;
    }

    public String getSlotName() {
        return slotName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getIffTemplate() {
        return iffTemplate;
    }

    public String getTerrain() {
        return terrain;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
