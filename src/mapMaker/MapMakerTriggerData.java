package mapMaker;

import main.Global;
import mapMaker.dialogs.EventTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.WildBattleTriggerEditDialog;
import mapMaker.dialogs.WildBattleTriggerOptionsDialog;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.LocationTriggerMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import pattern.generic.SinglePointTriggerMatcher;
import pattern.map.AreaMatcher;
import pattern.map.EventMatcher;
import pattern.map.ItemMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.map.NPCMatcher;
import pattern.map.WildBattleMatcher;
import util.DrawUtils;
import util.FileIO;
import util.JsonUtils;
import util.Point;
import util.PokeString;
import util.StringUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	private Set<AreaMatcher> areaData;
	private Set<LocationTriggerMatcher> entities;

	// Have the triggers been saved or have they been edited?
	private boolean triggersSaved;

	private MapMaker mapMaker;

	MapMakerTriggerData(MapMaker mapMaker) {
		initialize(mapMaker);

		// Force creation of mapName.txt file
		triggersSaved = false;
	}

	MapMakerTriggerData(MapMaker mapMaker, String mapTriggerFileName) {
		initialize(mapMaker);

		String fileText = FileIO.readEntireFileWithoutReplacements(mapTriggerFileName, false);
		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapTriggerFileName, fileText);
		this.areaData = new HashSet<>(mapDataMatcher.getAreas());
		this.entities = new HashSet<>(mapDataMatcher.getAllEntities());

		triggersSaved = true;
	}

	private void initialize(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		this.areaData = new HashSet<>();
		this.entities = new HashSet<>();
	}

	boolean isSaved() {
		return triggersSaved;
	}

	void saveTriggers(String mapFileName) {
		if (triggersSaved) {
			return;
		}

		triggersSaved = true;

		Set<String> entityNames = new HashSet<>();
		entities.forEach(matcher -> getUniqueEntityName(matcher, entityNames));

		MapDataMatcher mapDataMatcher = new MapDataMatcher(
				areaData,
				entities
		);

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(JsonUtils.getJson(mapDataMatcher)));
	}

	private String getUniqueEntityName(LocationTriggerMatcher matcher, Set<String> entityNames) {
		TriggerModelType type = matcher.getTriggerModelType();
		String basicEntityName = matcher.getBasicName();

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
		matcher.setTriggerName(uniqueEntityName);
		entityNames.add(uniqueEntityName);

		return uniqueEntityName;
	}

	private String getEntityNameFormat(String baseName) {
		if (StringUtils.isNullOrEmpty(baseName)) {
			baseName = "Nameless";
		}

		baseName = baseName.replaceAll("\\s", "");
		baseName = PokeString.removeSpecialSymbols(baseName);
		return baseName;
	}

	void moveTriggerData(Point delta) {
		for (LocationTriggerMatcher matcher : this.entities) {
			matcher.addDelta(delta);
		}

		triggersSaved = false;
	}

	void drawTriggers(Graphics2D g2d, Point mapLocation) {
		for (LocationTriggerMatcher entity : this.entities) {
			TriggerModelType triggerModelType = entity.getTriggerModelType();

			if (entity instanceof SinglePointTriggerMatcher) {
				Point point = ((SinglePointTriggerMatcher)entity).getLocation();

				BufferedImage image = null;
				switch (triggerModelType) {
					case MAP_TRANSITION:
						Point exit = ((MapTransitionMatcher)entity).getExitLocation();
						if (exit != null) {
							BufferedImage exitImage = TriggerModel.getMapExitImage(mapMaker);
							DrawUtils.drawTileImage(g2d, exitImage, exit, mapLocation);
						}
						break;
					case NPC:
						NPCMatcher npc = (NPCMatcher) entity;
						// TODO: This should be in a method
						image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * npc.getSpriteIndex() + 1 + npc.getDirection().ordinal());
						break;
				}

				if (image == null) {
					image = triggerModelType.getImage(mapMaker);
				}

				DrawUtils.drawTileImage(g2d, image, point, mapLocation);
			} else if (entity instanceof MultiPointTriggerMatcher) {
				List<Point> entityLocation = ((MultiPointTriggerMatcher) entity).getLocation();
				for (Point point : entityLocation) {
					BufferedImage image = triggerModelType.getImage(mapMaker);
					DrawUtils.drawTileImage(g2d, image, point, mapLocation);
				}
			} else {
				Global.error("Unknown entity matcher class " + entity.getClass().getSimpleName());
			}
		}
	}

	void placeTrigger(Point location) {

		// TODO: Ask user if they would like to place over
		LocationTriggerMatcher placeableTrigger = mapMaker.getPlaceableTrigger();
		placeableTrigger.addPoint(location);
		this.entities.add(placeableTrigger);
		System.out.println("Entity placed at (" + location.x + ", " + location.y + ").");

		triggersSaved = false;
	}

	boolean createTrigger(TriggerModelType type) {
		mapMaker.clearPlaceableTrigger();

		LocationTriggerMatcher trigger = getTriggerFromDialog(type, null);

		mapMaker.setPlaceableTrigger(trigger);

		return trigger != null;
	}

	public void editTrigger(LocationTriggerMatcher trigger) {
		LocationTriggerMatcher newTrigger = getTriggerFromDialog(trigger.getTriggerModelType(), trigger);

		// Update entity list
		if (newTrigger != null) {
			this.entities.remove(trigger);
			this.entities.add(newTrigger);

			newTrigger.setLocation(trigger);

			triggersSaved = false;
		}
	}

	private LocationTriggerMatcher getTriggerFromDialog(TriggerModelType triggerModelType, LocationTriggerMatcher oldTrigger) {
		switch (triggerModelType) {
			case ITEM:
				return new ItemEntityDialog((ItemMatcher)oldTrigger, mapMaker).getMatcher(mapMaker);
			case NPC:
				return new NPCEntityDialog((NPCMatcher)oldTrigger, mapMaker).getMatcher(mapMaker);
			case TRIGGER_ENTITY:
				// TODO: Need a new edit and dialog
				return new EventTriggerDialog((EventMatcher)oldTrigger).getMatcher(mapMaker);
			case MAP_TRANSITION:
				return new MapTransitionDialog((MapTransitionMatcher)oldTrigger, mapMaker).getMatcher(mapMaker);
			case WILD_BATTLE:
				if (oldTrigger == null) {
					return new WildBattleTriggerOptionsDialog(this.getWildBattleTriggers()).getMatcher(mapMaker);
				} else {
					return new WildBattleTriggerEditDialog((WildBattleMatcher)oldTrigger, -1).getMatcher(mapMaker);
				}
			case EVENT:
				return new EventTriggerDialog((EventMatcher)oldTrigger).getMatcher(mapMaker);
			default:
				Global.error("Unknown trigger model type " + triggerModelType);
				return null;
		}
	}

	private List<WildBattleMatcher> getWildBattleTriggers() {
		return this.entities
				.stream()
				.filter(entity -> entity instanceof WildBattleMatcher)
				.map(entity -> (WildBattleMatcher)entity)
				.collect(Collectors.toList());
	}

	public List<LocationTriggerMatcher> getEntitiesAtLocation(Point location) {
		return this.entities
				.stream()
				.filter(entity -> entity.isAtLocation(location))
				.collect(Collectors.toList());
	}

	public void removeTrigger(LocationTriggerMatcher trigger) {
		this.entities.removeIf(matcher -> trigger == matcher);

		triggersSaved = false;
	}

	public void moveTrigger(LocationTriggerMatcher trigger) {
		removeTrigger(trigger);
		mapMaker.setPlaceableTrigger(trigger);
	}
}
