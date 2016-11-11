package mapMaker.data;

import item.Item;
import map.MapData;
import map.MapMetaData.MapDataType;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.TriggerEntityData;
import map.entity.npc.NPCEntityData;
import map.triggers.EventTrigger;
import map.triggers.MapTransitionTrigger;
import map.triggers.TriggerData;
import map.triggers.WildBattleTrigger;
import mapMaker.MapMaker;
import mapMaker.dialogs.EventTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.TransitionBuildingMainSelectDialog;
import mapMaker.dialogs.TransitionBuildingTransitionDialog;
import mapMaker.dialogs.TriggerEntityDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.DrawUtils;
import util.FileIO;
import util.Point;
import util.PokeString;
import util.StringUtils;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	private static final Pattern blockPattern = Pattern.compile("((NPC|Item|Trigger|MapEntrance|TriggerData)\\s+)?(\\w+)\\s*\\{([^}]*)\\}");

	// Old trigger data
	private Map<Integer, String> triggers;

	// New trigger data
	private Map<Integer, TriggerData> triggerDataOnMap;

	// Entities
	// Holds a list of entities for every location. Some entities only appear
	// given a condition, allowing multiple entities to be at one location at a time
	private Map<Integer, List<EntityData>> entities;
	
	// Names of entities on map so no two entities have the same name
	private Set<String> entityNames;

	// Names of all the triggers placed on the map
	private Set<String> triggerNames;

	// Locations of the entrances for the map
	private Map<Integer, String> mapEntrances;

	// List of wild battle trigger data
	private Map<String, TriggerData> wildBattleTriggers;

	// List of map transition trigger data
	private Map<String, TriggerData> mapTransitionTriggers;

	// Data structure to hold all triggers not placed on the map
	private static MapTriggerData mapTriggers;

	// Data structure to hold pokemon center transitions
	private static PokeCenterTransitionData pokeCenterTransitionData;

	// Data structure to hold all the map entrance names for each map
	private static Map<String, Set<String>> allMapEntrances;

	private static TransitionBuildingData transitionBuildingData;

	// Have the triggers been saved or have they been edited?
	private boolean triggersSaved;

	private MapMaker mapMaker;

	public MapMakerTriggerData(MapMaker mapMaker) {
		initialize(mapMaker);

		// Force creation of mapName.txt file
		triggersSaved = false;
	}

	public MapMakerTriggerData(MapMaker mapMaker, File mapTriggerFile) {
		initialize(mapMaker);

		String fileText = FileIO.readEntireFileWithoutReplacements(mapTriggerFile, false);
		String currentMapName = mapMaker.getCurrentMapName();
		Dimension currentMapSize = mapMaker.getCurrentMapSize();

		Matcher m = blockPattern.matcher(fileText);
		while (m.find()) {
			String name = m.group(3);
			if (m.group(1) == null) { // trigger
				Scanner in = new Scanner(m.group(4));
				while (in.hasNext()) {
					String[] xr = in.next().split("-");
					String[] yr = in.next().split("-");
					
					int x1 = Integer.parseInt(xr[0]);
					int y1 = Integer.parseInt(yr[0]);
					int x2 = xr.length == 2 ? Integer.parseInt(xr[1]) : x1;
					int y2 = yr.length == 2 ? Integer.parseInt(yr[1]) : y1;
					
					for (int x = x1; x <= x2; x++) {
						for (int y = y1; y <= y2; y++) {
							triggers.put(getMapIndex(x, y), name);
						}
					}
				}
				
				in.close();
			}
			else {
				EntityData entity;
				switch (m.group(2)) {
					case "NPC":
						entity = new NPCEntityData(name, m.group(4));
						addEntityAtLocation(getMapIndex(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					case "Item":
						entity = new ItemEntityData(name, m.group(4));
						addEntityAtLocation(getMapIndex(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					case "Trigger":
						entity = new TriggerEntityData(name, m.group(4));
						addEntityAtLocation(getMapIndex(entity.x, entity.y), entity);
						entityNames.add(name);
						break;
					// Trigger Data and other items.
					case "MapEntrance":
						mapEntrances.put(MapData.getMapEntranceLocation(m.group(4), (int) currentMapSize.getWidth()), name);
						addMapEntranceNameToMap(currentMapName, name);
						break;
					case "TriggerData":
						triggerNames.add(name);

						TriggerData triggerData = new TriggerData(name, m.group(4));
						for (Integer loc : triggerData.getPoints((int) currentMapSize.getWidth())) {
							triggerDataOnMap.put(loc, triggerData);
						}

						// TODO: Add each type of trigger data to specific data structure
						if (triggerData.triggerType.equals("WildBattle")) {
							wildBattleTriggers.put(triggerData.name, triggerData);
						}

						if (triggerData.triggerType.equals("MapTransition")) {
							mapTransitionTriggers.put(triggerData.name, triggerData);
						}

						break;
				}
			}
		}

		triggersSaved = true;
	}

	private void initialize(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		triggers = new HashMap<>();
		triggerDataOnMap = new HashMap<>();

		entities = new HashMap<>();
		entityNames = new HashSet<>();

		mapEntrances = new HashMap<>();
		mapTransitionTriggers = new HashMap<>();

		wildBattleTriggers = new HashMap<>();

		triggerNames = new HashSet<>();

		mapTriggers = new MapTriggerData(mapMaker, mapMaker.getCurrentMapName());

		if (pokeCenterTransitionData == null) {
			pokeCenterTransitionData = new PokeCenterTransitionData(mapMaker);
		}

		if (allMapEntrances == null) {
			allMapEntrances = new HashMap<>();
			allMapEntrances.put(mapMaker.getCurrentMapName(), new HashSet<>());
		}

		if (transitionBuildingData == null) {
			transitionBuildingData = new TransitionBuildingData(mapMaker);
		}
	}

	public void reload() {
		transitionBuildingData = new TransitionBuildingData(mapMaker);
	}

	private void readMapEntrancesForMap(String mapName) {
		if (!allMapEntrances.containsKey(mapName)) {

			Set<String> entranceNames = new HashSet<>();

			File mapTextFile = mapMaker.getMapTextFile(mapName);
			String fileText = FileIO.readEntireFileWithoutReplacements(mapTextFile, false);

			Matcher m = blockPattern.matcher(fileText);
			while (m.find()) {
				if (m.group(1) != null && m.group(2).equals("MapEntrance")) {
					String name = m.group(3);
					entranceNames.add(name);
				}
			}

			allMapEntrances.put(mapName, entranceNames);
		}
	}

	public String[] getMapEntrancesForMap(String mapName) {
		readMapEntrancesForMap(mapName);

		String[] names = new String[allMapEntrances.get(mapName).size()];
		return allMapEntrances.get(mapName).toArray(names);
	}

	private void addMapEntranceNameToMap(String mapName, String entranceName) {
		readMapEntrancesForMap(mapName);

		Set<String> entranceNames = allMapEntrances.get(mapName);
		entranceNames.add(entranceName);
	}

	private void removeMapEntranceNameFromMap(String mapName, String entranceName) {
		readMapEntrancesForMap(mapName);

		Set<String> entranceNames = allMapEntrances.get(mapName);
		entranceNames.remove(entranceName);
	}

	private void addEntityAtLocation(Integer location, EntityData entity) {
		if (!entities.containsKey(location)) {
			entities.put(location, new ArrayList<>());
		}
		
		entities.get(location).add(entity);
	}

	private List<EntityData> getEntitiesAtLocation(Integer location) {
		if (!entities.containsKey(location)) {
			entities.put(location, new ArrayList<>());
		}
		
		return entities.get(location);
	}

	private EntityData getEntityAtLocationWithName(Integer location, String name) {
		List<EntityData> entitiesArrayList = getEntitiesAtLocation(location);

		for (EntityData entity : entitiesArrayList) {
			if (entity.name.equals(name)) {
				return entity;
			}
		}
		
		return null;
	}

	private EntityData removeEntityAtLocationWithName(Integer location, String name) {
		List<EntityData> entitiesArrayList = getEntitiesAtLocation(location);
		for (int currEntity = 0; currEntity < entitiesArrayList.size(); ++currEntity) {
			if (entitiesArrayList.get(currEntity).name.equals(name)) {
				return entitiesArrayList.remove(currEntity);
			}
		}
		
		return null;
	}

	public boolean isSaved() {
		return triggersSaved;
	}

	public void saveTriggers(File mapTriggerFile) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		pokeCenterTransitionData.save();
		transitionBuildingData.save();

		mapTriggers.save();

		Map<String, List<Integer>> triggersByName = new HashMap<>();
		for (Integer location : triggers.keySet()) {
			String name = triggers.get(location);
			if (!triggersByName.containsKey(name)) {
				triggersByName.put(name, new LinkedList<>());
			}

			triggersByName.get(name).add(location);
		}

		// Put each value from triggerDataOnMap into triggerDataSet
		Set<TriggerData> triggerDataSet = new HashSet<>();
		triggerDataOnMap.values().stream()
				.filter(triggerData -> !triggerDataSet.contains(triggerData))
				.forEach(triggerDataSet::add);

		try {
			// TODO: FileIO
			// Create file if it doesn't exist
			if (!mapTriggerFile.exists()) {
				mapTriggerFile.getParentFile().mkdirs();
				mapTriggerFile.createNewFile();
			}

			FileWriter writer = new FileWriter(mapTriggerFile);

			// Save old format of location and trigger name
			for (String triggerName : triggersByName.keySet()) {
				List<Integer> list = triggersByName.get(triggerName);

				writer.write(triggerName + " {\n");

				for (Integer locationIndex : list) {
					Point location = Point.getPointAtIndex(locationIndex, mapMaker.getCurrentMapSize().width);
					writer.write("\t" + location + "\n");
				}
				
				writer.write("}\n\n");
			}

			// Save all entities
			for (List<EntityData> entityList : entities.values()) {
				for (EntityData ed : entityList) {
					writer.write(ed.entityDataAsString() + "\n");
				}
			}

			// Save all map entrances
			for (Integer locationIndex : mapEntrances.keySet()) {
				String entrance = mapEntrances.get(locationIndex);
				Point location = Point.getPointAtIndex(locationIndex, mapMaker.getCurrentMapSize().width);

				writer.write("MapEntrance " + entrance + " {\n");
				writer.write("\tx: " + location.x + "\n");
				writer.write("\ty: " + location.y + "\n");
				writer.write("}\n\n");
			}

			// Save all trigger data items
			for (TriggerData td : triggerDataSet) {
				writer.write(td.triggerDataAsString() + "\n");
			}

			writer.close();

		}
		catch (IOException ex) {
			ex.printStackTrace(); // TODO: Global.error
		}
	}

	public void moveTriggerData(Point delta, int newWidth) {
		triggersSaved = false;

		Map<Integer, String> tempTriggers = new HashMap<>();

		for (Integer locationIndex : triggers.keySet()) {
			Point location = Point.getPointAtIndex(locationIndex, newWidth);
			location.add(delta);

			tempTriggers.put(location.getIndex(newWidth), triggers.get(locationIndex));
		}

		triggers = tempTriggers;

		for (List<EntityData> entityList : entities.values()) {
			for (EntityData ed : entityList) {
				ed.x += delta.x;
				ed.y += delta.y;
			}
		}

		Map<Integer, String> tempMapEntrances = new HashMap<>();

		for (Integer locationIndex : mapEntrances.keySet()) {
			String entrance = mapEntrances.get(locationIndex);

			Point location = Point.getPointAtIndex(locationIndex, newWidth);
			location.add(delta);

			tempMapEntrances.put(location.getIndex(newWidth), entrance);
		}

		mapEntrances = tempMapEntrances;

		Set<String> updatedTriggerData = new HashSet<>();
		Map<Integer, TriggerData> tempTriggerData = new HashMap<>();

		triggerDataOnMap.values().stream()
				.filter(triggerData -> !updatedTriggerData.contains(triggerData.name))
				.forEach(triggerData -> {
			updatedTriggerData.add(triggerData.name);
			triggerData.updatePoints(delta);

			for (Integer loc : triggerData.getPoints(newWidth)) {
				tempTriggerData.put(loc, triggerData);
			}
		});

		triggerDataOnMap = tempTriggerData;
	}

	public void drawTriggers(Graphics2D g2d, Point mapLocation) {
		int mapWidth = mapMaker.getCurrentMapSize().width;

		// Draw all old trigger types
		for (Integer locationIndex : triggers.keySet()) {
			Point location = Point.getPointAtIndex(locationIndex, mapWidth);
			DrawUtils.outlineTileRed(g2d, location, mapLocation);
		}

		// Draw all map entrances
		for (Integer locationIndex : mapEntrances.keySet()) {
			Point location = Point.getPointAtIndex(locationIndex, mapWidth);
			BufferedImage image = TriggerModelType.MAP_ENTRANCE.getImage(mapMaker);
			DrawUtils.drawTileImage(g2d, image, location, mapLocation);
		}

		// Draw all trigger data
		for (Integer locationIndex : triggerDataOnMap.keySet()) {
			TriggerData triggerData = triggerDataOnMap.get(locationIndex);
			Point location = Point.getPointAtIndex(locationIndex, mapWidth);

			final TriggerModelType triggerModelType;
			switch (triggerData.triggerType) {
				case "MapTransition":
					// If pokecenter transition
					if (triggerData.triggerContents.contains("nextMap: PokeCenter")) {
						triggerModelType = TriggerModelType.POKE_CENTER;
					} else if (triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
						// NOTE: Technically there was a different one for vertical but I don't care since it's getting deleted
						triggerModelType = TriggerModelType.TRANSITION_BUILDING;
					} else {
						triggerModelType = TriggerModelType.MAP_EXIT;
					}
					break;
				case "WildBattle": {
					triggerModelType = TriggerModelType.WILD_BATTLE;
					break;
				}
				case "Event": {
					DrawUtils.outlineTileRed(g2d, location, mapLocation);
					triggerModelType = TriggerModelType.EVENT;
					break;
				}
				default:
					DrawUtils.outlineTileRed(g2d, location, mapLocation);
					triggerModelType = null;
					break;
			}

			if (triggerModelType != null) {
				BufferedImage image = triggerModelType.getImage(mapMaker);
				DrawUtils.drawTileImage(g2d, image, location, mapLocation);
			}
		}

		// Draw all entities
		// Unnecessary to draw all entities at each location. Only first entity needed?
		for (Entry<Integer, List<EntityData>> entry : entities.entrySet()) {
			Point location = Point.getPointAtIndex(entry.getKey(), mapWidth);
			List<EntityData> entityList = entry.getValue();

			for (EntityData ed : entityList) {

				final BufferedImage image;
				if (ed instanceof ItemEntityData) {
					image = TriggerModelType.ITEM.getImage(mapMaker);
				}
				else if (ed instanceof NPCEntityData) {
					NPCEntityData npc = (NPCEntityData) ed;

					// TODO: This should be in a function
					image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * npc.spriteIndex + 1 + npc.defaultDirection.ordinal());

				}
				else if (ed instanceof TriggerEntityData) {
					image = TriggerModelType.TRIGGER_ENTITY.getImage(mapMaker);
				} else {
					image = null;
				}

				if (image != null) {
					DrawUtils.drawTileImage(g2d, image, location, mapLocation);
				}
			}
		}

	}

	public Point getPointFromIndex(int locationIndex) {
		return Point.getPointAtIndex(locationIndex, this.mapMaker.getCurrentMapSize().width);
	}

	public int getMapIndex(int x, int y) {
		return Point.getIndex(x, y, this.mapMaker.getCurrentMapSize().width);
	}

	public int getMapIndex(Point location) {
		return getMapIndex(location.x, location.y);
	}

	// TODO: holy hell this method needs to be split
	public void placeTrigger(Point location) {
		int index = getMapIndex(location);
		int x = location.x;
		int y = location.y;

		// TODO: Ask user if they would like to place over

		// System.out.println("Place trigger " + placeableTrigger.name + " "
		// +placeableTrigger.triggerType);

		if (mapMaker.isPlaceableTriggerType(PlaceableTrigger.TriggerType.Entity)) {
			EntityData entity = mapMaker.getPlaceableTrigger().entity;

			entity.x = x;
			entity.y = y;

			addEntityAtLocation(index, entity);

			entityNames.add(entity.name);
			System.out.println("Entity " + entity.name + " placed at (" + entity.getX() + ", " + entity.getY() + ").");

			mapMaker.clearPlaceableTrigger();
			triggersSaved = false;
		}
		else if (mapMaker.isPlaceableTriggerType(PlaceableTrigger.TriggerType.MapEntrance)) {

			String placeableTriggerName = mapMaker.getPlaceableTrigger().name;

			mapEntrances.put(index, placeableTriggerName);
			addMapEntranceNameToMap(mapMaker.getCurrentMapName(), placeableTriggerName);

			mapMaker.clearPlaceableTrigger();
			triggersSaved = false;
		}
		else if (mapMaker.isPlaceableTriggerType(PlaceableTrigger.TriggerType.TriggerData)) {

			triggersSaved = false;

			TriggerData triggerData = mapMaker.getPlaceableTrigger().triggerData;

			triggerData.addPoint(x, y);
			triggerDataOnMap.put(index, triggerData);

			// TODO: Switch
			if (triggerData.triggerType.equals("WildBattle")) {
				// TODO: What happens here?
			}
			else if (triggerData.triggerType.equals("MapTransition")) {
				// If pokecenter transition
				if (triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					String entranceName;
					Integer entranceLocation = getMapIndex(x, y + 1);

					if (mapEntrances.containsKey(entranceLocation)) {
						entranceName = mapEntrances.get(entranceLocation);
					}
					else {
						entranceName = "PokeCenter";
						int number = 1;
						while (mapEntrances.containsValue(String.format("%s%02d", entranceName, number))) {
							++number;
						}

						entranceName = String.format("%s%02d", entranceName, number);
						mapEntrances.put(entranceLocation, entranceName);
					}

					triggerData.triggerContents = triggerData.triggerContents.replace("@entranceName", entranceName);
					pokeCenterTransitionData.add(mapMaker.getCurrentMapName(), entranceName);
				}
				// If transition building transition
				else if (triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					boolean isMap1 = true;

					PlaceableTrigger placeableTrigger = mapMaker.getPlaceableTrigger();

					if (mapMaker.getCurrentMapName().equals(placeableTrigger.transitionBuildingPair.map2)
							&& placeableTrigger.transitionBuildingPair.map2Entrance == null) {
						isMap1 = false;
					}

					int number = 1;
					String mapEntranceName = "";

					int directionIndex = 0;
					if (!placeableTrigger.transitionBuildingPair.horizontal) {
						directionIndex = 2;
					}

					if (!isMap1) {
						++directionIndex;
					}

					String direction = TransitionBuildingData.directions[directionIndex];

					do {
						mapEntranceName = String.format("TransitionBuilding%s%sDoor%02d", (placeableTrigger.transitionBuildingPair.horizontal ? "H" : "V"), direction, number++);
					} while (mapEntrances.containsValue(mapEntranceName));

					// System.out.println(mapEntranceName);

					TransitionBuildingPair pair;

					if (placeableTrigger.transitionBuildingPair.map1Entrance == null
							&& placeableTrigger.transitionBuildingPair.map2Entrance == null) {

						// Create
						pair = transitionBuildingData.addIncompleteTransition(
								placeableTrigger.transitionBuildingPair.horizontal,
								placeableTrigger.transitionBuildingPair.map1,
								placeableTrigger.transitionBuildingPair.map2,
								isMap1,
								mapEntranceName);

						// Get pair number and replace in map transition trigger
						String mapTriggerName = mapMaker.getCurrentMapName() + "_to_" + pair.getPairName() + "_" + TransitionBuildingData.directions[directionIndex] + "Door";

						placeableTrigger.triggerData.name = placeableTrigger.name = mapTriggerName;
						placeableTrigger.triggerData.triggerContents = placeableTrigger.triggerData.triggerContents.replace("@pairNumber", String.format("%02d", pair.pairNumber));
					}
					else {
						pair = transitionBuildingData.updateIncompleteTransition(placeableTrigger.transitionBuildingPair, mapEntranceName);
					}

					// Update map area
					int area = mapMaker.getTile(location, MapDataType.AREA);
					if (isMap1) {
						pair.area1 = area;
					}
					else {
						pair.area2 = area;
					}

					// TODO: This can probobbly be generalized
					if (placeableTrigger.transitionBuildingPair.horizontal) {
						placeableTrigger.triggerData.addPoint(x, y - 1);
						triggerDataOnMap.put(getMapIndex(x, y - 1), placeableTrigger.triggerData);

						Integer entranceLocation = getMapIndex(x + (isMap1 ? 1 : -1), y);

						mapEntrances.put(entranceLocation, mapEntranceName);
					}
					else {
						placeableTrigger.triggerData.addPoint(x - 1, y);
						triggerDataOnMap.put(getMapIndex(x - 1, y), placeableTrigger.triggerData);
						placeableTrigger.triggerData.addPoint(x + 1, y);
						triggerDataOnMap.put(getMapIndex(x + 1, y), placeableTrigger.triggerData);

						Integer entranceLocation = getMapIndex(x, y + (isMap1 ? -1 : 1));

						mapEntrances.put(entranceLocation, mapEntranceName);
					}
				}

				// Normal map transition Trigger
				// else {}

				mapTransitionTriggers.put(mapMaker.getPlaceableTrigger().name, triggerData);
			}

			triggerNames.add(triggerData.name);
		}
	}

	// Used for moving triggers
	public TriggerModelType getTriggerModelType(PlaceableTrigger trigger) {
		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			if (trigger.entity instanceof ItemEntityData) {
				return TriggerModelType.ITEM;
			}

			if (trigger.entity instanceof NPCEntityData) {
				return TriggerModelType.NPC;
			}

			if (trigger.entity instanceof TriggerEntityData) {
				return TriggerModelType.TRIGGER_ENTITY;
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
			if (trigger.triggerData.triggerType.equals("WildBattle")) {
				return TriggerModelType.WILD_BATTLE;
			}
			else if (trigger.triggerData.triggerType.equals("MapTransition")) {
				// If pokecenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					return TriggerModelType.POKE_CENTER;
				}
				else if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					return TriggerModelType.TRANSITION_BUILDING;
				}
				else {
					return TriggerModelType.TRIGGER_ENTITY;
				}
			}
			// else if (trigger.triggerData.triggerType.equals("Group")) {
			// return 8;
			// }
			else if (trigger.triggerData.triggerType.equals("Event")) {
				return TriggerModelType.EVENT;
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger)
		{
			// TODO?
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {

			if (trigger.name.startsWith("TransitionBuilding")) {
				return TriggerModelType.TRANSITION_BUILDING;
			}
			else if (trigger.name.startsWith("PokeCenter")) {
				return TriggerModelType.POKE_CENTER;
			}

			return TriggerModelType.MAP_ENTRANCE;
		}

		return null;
	}

	public boolean createTrigger(TriggerModelType type) {
		mapMaker.clearPlaceableTrigger();

		PlaceableTrigger trigger = null;

		// TODO: Show popup asking user about specific trigger being placed.
		switch (type) {
			case ITEM:
				System.out.println("Item");
				trigger = editItem(null);
				break;
			case NPC:
				System.out.println("NPC");
				trigger = editNPC(null);
				break;
			case TRIGGER_ENTITY:
				System.out.println("Trigger Entity");
				trigger = editTriggerEntity(null);
				break;
			case WILD_BATTLE:
				System.out.println("Wild Battle");
				trigger = wildBattleTriggerOptions();
				break;
			case MAP_EXIT:
				System.out.println("Exit");
				trigger = editMapTransition(null);
				break;
			case MAP_ENTRANCE:
				System.out.println("Entrance");
				trigger = editMapEntrance(null);
				break;
			case POKE_CENTER:
				System.out.println("PokeCenter");
				trigger = createPokeCenterTransition();
				break;
			case TRANSITION_BUILDING:
				System.out.println("Transition Building");
				trigger = transitionBuildingTransitionsOptions();
				break;
			case EVENT:
				System.out.println("Event");
				trigger = editEventTrigger(null);
				break;
		}

		mapMaker.setPlaceableTrigger(trigger);

		return trigger != null;
	}

	public void editTrigger(PlaceableTrigger trigger) {
		PlaceableTrigger newTrigger = null;

		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			entityNames.remove(trigger.entity.name);

			if (trigger.entity instanceof ItemEntityData) {
				newTrigger = editItem((ItemEntityData) trigger.entity);
			}
			else if (trigger.entity instanceof NPCEntityData) {
				newTrigger = editNPC((NPCEntityData) trigger.entity);
			}
			else if (trigger.entity instanceof TriggerEntityData) {
				newTrigger = editTriggerEntity((TriggerEntityData) trigger.entity);
			}

			// Update entity list
			if (newTrigger != null) {
				entityNames.add(newTrigger.entity.name);

				removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
				addEntityAtLocation(trigger.location, newTrigger.entity);

				newTrigger.entity.x = trigger.entity.x;
				newTrigger.entity.y = trigger.entity.y;

				triggersSaved = false;
			}
			// Add entity name back to list
			else {
				entityNames.add(trigger.entity.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
			triggerNames.remove(trigger.triggerData.name);

			if (trigger.triggerData.triggerType.equals("MapTransition")) {
				// If pokecenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					System.out.println("PokeCenter transition triggers cannot be edited.");
				}
				// TODO: Transition Buildings
				else if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					System.out.println("Transition building transition triggers cannot be edited... yet");
				}
				else {
					newTrigger = editMapTransition(new MapTransitionTrigger(trigger.name, trigger.triggerData.triggerContents));
				}
			}
			else if (trigger.triggerData.triggerType.equals("WildBattle")) {
				newTrigger = editWildBattleTrigger(new WildBattleTrigger(trigger.name, trigger.triggerData.triggerContents));
			}
			else if (trigger.triggerData.triggerType.equals("Event")) {
				newTrigger = editEventTrigger(new EventTrigger(trigger.name, trigger.triggerData.triggerContents));
				// TODO
			}
			// else if (trigger.triggerData.triggerType.isEmpty()) { }

			if (newTrigger != null) {
				triggerNames.add(newTrigger.triggerData.name);

				triggersSaved = false;

				// Loop through all of previous and replace with new
				renameTriggerData(trigger.triggerData, newTrigger.triggerData);
			}
			else {
				triggerNames.add(trigger.triggerData.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger)
		{
			// Not planning on making editable
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {
			if (trigger.name.startsWith("TransitionBuilding") || trigger.name.startsWith("PokeCenter")) {
				System.out.println("This map entrance cannot be edited");
			}
			else {
				newTrigger = editMapEntrance(trigger.name);

				if (newTrigger != null)
				{
					mapEntrances.remove(trigger.name);
					mapEntrances.put(trigger.location, newTrigger.name);

					triggersSaved = false;
				}
			}
		}
	}

	public void renameTriggerData(TriggerData prev, TriggerData updated) {
		updated.points = prev.points;

		for (Integer loc : updated.getPoints(mapMaker.getCurrentMapSize().width)) {
			triggerDataOnMap.put(loc, updated);
		}

		wildBattleTriggers.remove(prev.name);
		wildBattleTriggers.put(updated.name, updated);
	}

	public PlaceableTrigger[] getTrigger(Point location) {
		int value = getMapIndex(location);

		List<PlaceableTrigger> triggersList = getEntitiesAtLocation(value).stream()
				.map(entityData -> new PlaceableTrigger(entityData, value))
				.collect(Collectors.toList());


		if (triggerDataOnMap.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(triggerDataOnMap.get(value), value));
		}

		if (triggers.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(PlaceableTrigger.TriggerType.OldTrigger, triggers.get(value), value));
		}

		if (mapEntrances.containsKey(value)) {
			triggersList.add(new PlaceableTrigger(PlaceableTrigger.TriggerType.MapEntrance, mapEntrances.get(value), value));
		}

		PlaceableTrigger[] triggersArray = new PlaceableTrigger[triggersList.size()];
		triggersList.toArray(triggersArray);
		return triggersArray;
	}

	public boolean hasTrigger(Point location) {
		int value = getMapIndex(location);
		return !entities.get(value).isEmpty()
				|| triggerDataOnMap.containsKey(value)
				|| triggers.containsKey(value)
				|| mapEntrances.containsKey(value);
	}

	public void removeTrigger(PlaceableTrigger trigger) {
		Point location = this.getPointFromIndex(trigger.location);

		if (trigger.triggerType == PlaceableTrigger.TriggerType.Entity) {
			removeEntityAtLocationWithName(trigger.location, trigger.entity.name);
			entityNames.remove(trigger.entity.name);
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.TriggerData) {
			// TODO: Remove from specific data structures when no points left on map.
			if (trigger.triggerData.triggerType.equals("MapTransition")) {

				// PokeCenter transition
				if (trigger.triggerData.triggerContents.contains("nextMap: PokeCenter")) {
					Integer trigLocation = getMapIndex(location.x, location.y + 1);
					String mapEntranceName = mapEntrances.remove(trigLocation);

					removeMapEntranceNameFromMap(mapMaker.getCurrentMapName(), mapEntranceName);
					pokeCenterTransitionData.remove(mapMaker.getCurrentMapName(), mapEntranceName);
				}

				// Transition buildings
				if (trigger.triggerData.triggerContents.contains("nextMap: TransitionBuilding")) {
					Matcher nameMatcher = TransitionBuildingData.transitionBuildingTransitionNamePattern.matcher(trigger.name);
					nameMatcher.find();

					String map1 = nameMatcher.group(2);
					String map2 = nameMatcher.group(3);

					boolean isMap1 = mapMaker.getCurrentMapName().equals(map1);

					TransitionBuildingPair pair = transitionBuildingData.removeTransitionOnMap(nameMatcher.group(1).equals("H"), map1, map2, Integer.parseInt(nameMatcher.group(4)), isMap1);

					// Moving item, save to replace after removing from data
					// structure.


					int[] locations = trigger.triggerData.getPoints(mapMaker.getCurrentMapSize().width);
					trigger.triggerData.points.clear();

					Arrays.sort(locations);

					// Remove locations from map
					for (int currLocation : locations) {
						triggerDataOnMap.remove(currLocation);
					}

					Point middle = this.getPointFromIndex(locations[1]);

					// Remove map entrance
					if (pair.horizontal) {
						Integer trigLocation = getMapIndex(middle.x + (isMap1 ? 1 : -1), middle.y);
						removeMapEntranceNameFromMap(mapMaker.getCurrentMapName(), mapEntrances.remove(trigLocation));
					}
					else {
						Integer trigLocation = getMapIndex(middle.x, middle.y + (isMap1 ? -1 : 1));
						removeMapEntranceNameFromMap(mapMaker.getCurrentMapName(), mapEntrances.remove(trigLocation));
					}
				}

				trigger.triggerData.removePoint(location);

				if (trigger.triggerData.points.size() == 0) {
					mapTransitionTriggers.remove(trigger.triggerData.name);
				}
			}
			else if (trigger.triggerData.triggerType.equals("Event")) {
				// TODO
			}
			else if (trigger.triggerData.triggerType.equals("WildBattle") && trigger.triggerData.points.size() == 0) {
				// Don't remove completely. This will keep the trigger in the
				// options select menu.
				// wildBattleTriggers.remove(trigger.name);
			}

			// else if (trigger.triggerData.triggerType.isEmpty() &&
			// trigger.triggerData.points.size() == 0) {}

			triggerDataOnMap.remove(trigger.location);
			trigger.triggerData.removePoint(location);

			if (trigger.triggerData.points.size() == 0) {
				triggerNames.remove(trigger.triggerData.name);
			}
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.OldTrigger) {
			triggers.remove(trigger.location);
		}
		else if (trigger.triggerType == PlaceableTrigger.TriggerType.MapEntrance) {
			if (trigger.name.startsWith("TransitionBuilding")) {

				// Remove all items
				// Check direction based on name (east/west...)
				for (int currDirection = 0; currDirection < TransitionBuildingData.directions.length; ++currDirection) {

					// Direction found
					if (trigger.name.contains(TransitionBuildingData.directions[currDirection])) {
						int dx = (currDirection < 2 ? currDirection * 2 - 1 : 0);
						int dy = (currDirection < 2 ? 0 : (currDirection - 2) * -2 + 1);

						int x = location.x;
						int y = location.y;
						System.out.println((x + dx) + " " + (y + dy));

						// Get the map transition for this transition building
						// transition group
						Integer locationIndex = getMapIndex(x + dx, y + dy);
						TriggerData td = triggerDataOnMap.get(locationIndex);

						PlaceableTrigger pt = new PlaceableTrigger(td, locationIndex);

						// If moving, save new trigger instead


						// Remove transition building transition
						// Remove map entrance by removing all the transition building transitions.
						removeTrigger(pt);

						return;
					}
				}
			}
			else if (trigger.name.startsWith("PokeCenter")) {
				// If deleting an entrance used by a pokecenter transition
				int x = location.x; // Fuck it this is getting deleted anyways fuck everything
				int y = location.y;
				Integer trigLocation = getMapIndex(x, y - 1);
				if (triggerDataOnMap.containsKey(trigLocation)
						&& triggerDataOnMap.get(trigLocation).triggerContents.contains("nextMap: PokeCenter")) {
					PlaceableTrigger pt = new PlaceableTrigger(triggerDataOnMap.get(trigLocation), trigLocation);

					
					removeTrigger(pt);
				}
			}

			mapEntrances.remove(trigger.location);
			removeMapEntranceNameFromMap(mapMaker.getCurrentMapName(), trigger.name);
		}

		triggersSaved = false;
	}

	public void moveTrigger(PlaceableTrigger trigger) {
		mapMaker.setPlaceableTrigger(trigger);
		removeTrigger(trigger);
		System.out.println(mapMaker.getPlaceableTrigger().name);
	}

	private boolean dialogOption(String name, JComponent... inputs) {
		Object[] options = { "Done", "Cancel" };
		int results = JOptionPane.showOptionDialog(
				mapMaker,
				inputs,
				name,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]
		);

		return results == JOptionPane.YES_OPTION;
	}

	private String getEntityNameFormat(String baseName) {
		if (StringUtils.isNullOrEmpty(baseName)) {
			baseName = "Nameless";
		}

		baseName = baseName.replaceAll("\\s", "");
		baseName = PokeString.removeSpecialSymbols(baseName);
		return baseName;
	}

	private String getUniqueEntityName(TriggerModelType type, String basicEntityName) {
		String typeName = getEntityNameFormat(type.getName());
		basicEntityName = getEntityNameFormat(basicEntityName);

		int number = 1;
		String uniqueEntityName;

		// Loop until valid name is created
		do {
			uniqueEntityName = String.format("%s_%s_%s_%02d",
					mapMaker.getCurrentMapName(), typeName, basicEntityName, number++);
		} while (entityNames.contains(uniqueEntityName));

		System.out.println(uniqueEntityName);

		return uniqueEntityName;
	}

	private PlaceableTrigger editItem(ItemEntityData item) {
		ItemEntityDialog itemDialog = new ItemEntityDialog(mapMaker);
		if (item != null) {
			itemDialog.setItem(item);
		}

		if (!dialogOption("Item Editor", itemDialog)) {
			return null;
		}

		String itemType = itemDialog.getItemName();
		if (!Item.isItem(itemType)) {
			return null;
		}

		String itemEntityName = getUniqueEntityName(TriggerModelType.ITEM, itemType);
		ItemEntityData newItem = itemDialog.getItem(itemEntityName);

		return new PlaceableTrigger(newItem);
	}

	private PlaceableTrigger editNPC(NPCEntityData npcData) {
		NPCEntityDialog npcDialog = new NPCEntityDialog(mapMaker);
		if (npcData != null) {
			// TODO: wtf
			npcDialog.setNPCData(npcData, npcData.name.replaceAll("^" + mapMaker.getCurrentMapName() + "_NPC_|_\\d + $", ""));
		}

		if (!dialogOption("NPC Editor", npcDialog)) {
			return null;
		}

		NPCEntityData newEntity = npcDialog.getNPC();
		newEntity.name = getUniqueEntityName(TriggerModelType.NPC, newEntity.name);

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger editTriggerEntity(TriggerEntityData triggerEntity) {
		TriggerEntityDialog triggerDialog = new TriggerEntityDialog();
		if (triggerEntity != null) {
			triggerDialog.setTriggerEntity(triggerEntity, triggerEntity.name.replaceAll("^" + mapMaker.getCurrentMapName() + "_TriggerEntity_|_\\d + $", ""));
		}

		if (!dialogOption("Trigger Entity Editor", triggerDialog)) {
			return null;
		}

		TriggerEntityData newEntity = triggerDialog.getTriggerEntity();
		newEntity.name = getUniqueEntityName(TriggerModelType.TRIGGER_ENTITY, newEntity.name);

		return new PlaceableTrigger(newEntity);
	}

	private PlaceableTrigger wildBattleTriggerOptions() {
		WildBattleTriggerOptionsDialog wildBattleTriggerOptions = new WildBattleTriggerOptionsDialog(wildBattleTriggers, this);

		if (!dialogOption("Wild Battle Trigger Options", wildBattleTriggerOptions)) {
			return null;
		}

		if (wildBattleTriggerOptions.comboBox.getItemCount() == 0) {
			return null;
		}

		String wildBattleTriggerName = (String) wildBattleTriggerOptions.comboBox.getSelectedItem();
		TriggerData td = wildBattleTriggers.get(wildBattleTriggerName);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editWildBattleTrigger(WildBattleTrigger wildBattleTrigger) {
		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		dialog.initialize(wildBattleTrigger);

		if (!dialogOption("Wild Battle Trigger Editor", dialog)) {
			return null;
		}

		TriggerData td = dialog.getTriggerData();

		// System.out.println(wildBattleTrigger.getName() +" " +td.name + " " +
		// td.triggerType +" " + td.triggerContents);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapTransition(MapTransitionTrigger mapTransitionTrigger) {
		MapTransitionDialog mapTransitionDialog = new MapTransitionDialog(mapMaker, this);
		if (mapTransitionTrigger != null) {
			mapTransitionDialog.setMapTransition(mapTransitionTrigger);
		}

		if (!dialogOption("Map Transition Editor", mapTransitionDialog)) {
			return null;
		}

		String mapDestination = mapTransitionDialog.getDestination();
		if (mapDestination.isEmpty() || mapTransitionDialog.getMapEntrance().isEmpty()) {
			return null;
		}

		String mapTriggerName = getUniqueEntityName(TriggerModelType.MAP_ENTRANCE, mapDestination);
		TriggerData td = mapTransitionDialog.getTriggerData(mapTriggerName);

		return new PlaceableTrigger(td);
	}

	private PlaceableTrigger editMapEntrance(String entrance) {
		String originalEntrance = entrance;

		entrance = JOptionPane.showInputDialog(mapMaker, "Please specify an entrance name:", entrance);
		if (entrance != null) {
			entrance = entrance.trim().replace(' ', '_');
		}

		// Do not allow entrances to begin with "TransitionBuilding" or "PokeCenter"
		while (!StringUtils.isNullOrEmpty(entrance) &&
				!entrance.equals(originalEntrance) &&
				(mapEntrances.containsValue(entrance) ||
						entrance.startsWith("PokeCenter") ||
						entrance.startsWith("TransitionBuilding")
				)) {
			if (entrance.startsWith("PokeCenter")) {
				entrance = JOptionPane.showInputDialog(mapMaker, "Map entrances cannot start with \"PokeCenter\".\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}
			else if (entrance.startsWith("TransitionBuilding")) {
				entrance = JOptionPane.showInputDialog(mapMaker, "Map entrances cannot start with \"TransitionBuilding\".\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}
			else if (mapEntrances.containsValue(entrance)) {
				entrance = JOptionPane.showInputDialog(mapMaker, "The entrance \"" + entrance + "\" already exist.\nPlease specify a different entrance name:", entrance.replace('_', ' '));
			}

			if (entrance != null) {
				entrance = entrance.trim().replace(' ', '_');
			}
		}

		if (StringUtils.isNullOrEmpty(entrance) || entrance.equals(originalEntrance)) {
			return null;
		}

		return new PlaceableTrigger(PlaceableTrigger.TriggerType.MapEntrance, entrance);
	}

	private PlaceableTrigger createPokeCenterTransition() {
		int number = 1;
		String mapTriggerName;
		do {
			mapTriggerName = String.format("from_%s_to_%s_%02d", mapMaker.getCurrentMapName(), "PokeCenter", number++);
		} while (mapTransitionTriggers.containsKey(mapTriggerName));

		TriggerData transition = new TriggerData(mapTriggerName, "MapTransition\n" + "\tglobal: MapGlobal_toPokeCenterFromEntrance_" + "@entranceName" + "\n" + "\tnextMap: PokeCenter\n" + "\tmapEntrance: " + "FrontDoor" + "\n");

		return new PlaceableTrigger(transition);
	}

	private PlaceableTrigger transitionBuildingTransitionsOptions() {
		TransitionBuildingPair[] pairs = transitionBuildingData.getIncompleteTransitionPairsForMap(mapMaker.getCurrentMapName());

		String[] pairLabels = new String[pairs.length];

		for (int currPair = 0; currPair < pairs.length; ++currPair) {
			pairLabels[currPair] = "(" + (pairs[currPair].horizontal ? "H" : "V") + ") " + (mapMaker.getCurrentMapName().equals(pairs[currPair].map2) ? pairs[currPair].map1 + (pairs[currPair].horizontal ? " to the East" : " to the North") : pairs[currPair].map2 + (pairs[currPair].horizontal ? " to the West" : " to the South"));
		}

		TransitionBuildingMainSelectDialog transitionBuildingMainSelectDialog = new TransitionBuildingMainSelectDialog(pairLabels);

		Object[] options = {"Create", "Place", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, transitionBuildingMainSelectDialog, "Transition Building Options", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		// System.out.println(results);

		if (results == JOptionPane.CLOSED_OPTION || results == 2) {
			return null;
		}

		// Place Button
		if (results == 1 && pairs.length > 0) {
			int index = transitionBuildingMainSelectDialog.getSelectedIndex();

			// System.out.println(index);

			int directionIndex = 0;
			if (!pairs[index].horizontal) {
				directionIndex = 2;
			}

			if (mapMaker.getCurrentMapName().equals(pairs[index].map2)) {
				++directionIndex;
			}

			String mapTriggerName = mapMaker.getCurrentMapName() + "_to_" + pairs[index].getPairName() + "_" + TransitionBuildingData.directions[directionIndex] + "Door";

			String direction = TransitionBuildingData.directions[directionIndex];

			TriggerData td = new TriggerData(mapTriggerName, "MapTransition\n" + "\tglobal: MapGlobal_TransitionPair" + String.format("%02d", pairs[index].pairNumber) + "\n" + "\tnextMap: " + "TransitionBuilding" + (pairs[index].horizontal ? "H" : "V") + "\n" + "\tmapEntrance: " + direction + "Door" + "\n");

			PlaceableTrigger pt = new PlaceableTrigger(td);
			pt.transitionBuildingPair = pairs[index];

			return pt;
		}
		// Create button
		else if (results == 0) {
			return editTransitionBuildingTransition();
		}

		return null;
	}

	// TODO: Make editable for adding conditions for each transition
	private PlaceableTrigger editTransitionBuildingTransition() {
		TransitionBuildingTransitionDialog transitionBuildingTransitionDialog = new TransitionBuildingTransitionDialog(mapMaker, this, mapMaker.getCurrentMapName());

		// if (mapTransitionTrigger != null)
		// {
		// mapTransitinoDialog.setMapTransition(mapTransitionTrigger);
		// }

		JComponent[] inputs = new JComponent[] {transitionBuildingTransitionDialog};

		Object[] options = {"Place", "Cancel"};

		int results = JOptionPane.showOptionDialog(mapMaker, inputs, "Transition Building Transition Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		TransitionBuildingPair pair = transitionBuildingTransitionDialog.getTransitionPair();

		if (results == JOptionPane.CLOSED_OPTION || results == 1 || pair == null) {
			return null;
		}

		int directionIndex = 0;
		if (!pair.horizontal) {
			directionIndex = 2;
		}

		if (mapMaker.getCurrentMapName().equals(pair.map2)) {
			++directionIndex;
		}

		String direction = TransitionBuildingData.directions[directionIndex];

		TriggerData td = new TriggerData(null, // mapTriggerName
				"MapTransition\n" + "\tglobal: MapGlobal_TransitionPair@pairNumber\n" + "\tnextMap: " + "TransitionBuilding" + (pair.horizontal ? "H" : "V") + "\n" + "\tmapEntrance: " + direction + "Door" + "\n");

		PlaceableTrigger pt = new PlaceableTrigger(td);
		pt.transitionBuildingPair = pair;

		return pt;
	}

	private PlaceableTrigger editEventTrigger(EventTrigger eventTrigger) {
		EventTriggerDialog eventTriggerDialog = new EventTriggerDialog();

		// TODO: this is all wrong and I don't feel like fixing it right now
		if (eventTrigger != null) {
			String name = eventTrigger.getName();
			String prefix = mapMaker.getCurrentMapName() + "_EventTrigger_";

			String actualName = name.substring(prefix.length(), name.length() - 3);
			System.out.println(actualName);

			eventTriggerDialog.setEventTrigger(eventTrigger, actualName);
		}

		if (!dialogOption("Event Trigger Editor", eventTriggerDialog)) {
			return null;
		}

		String eventTriggerName = eventTriggerDialog.getEventTriggerName();
		if (eventTriggerName.isEmpty()) {
			return null;
		}

		// loop until valid name is created.
		String triggerName = getUniqueEntityName(TriggerModelType.EVENT, eventTriggerName);
		TriggerData td = eventTriggerDialog.getTriggerData(triggerName);

		return new PlaceableTrigger(td);
	}
}
