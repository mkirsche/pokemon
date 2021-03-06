package mapMaker;

import draw.TileUtils;
import main.Global;
import map.entity.movable.MovableEntity;
import mapMaker.dialogs.EventTriggerDialog;
import mapMaker.dialogs.ItemEntityDialog;
import mapMaker.dialogs.MapTransitionDialog;
import mapMaker.dialogs.MiscEntityDialog;
import mapMaker.dialogs.NPCEntityDialog;
import mapMaker.dialogs.wildbattle.FishingTriggerEditDialog;
import mapMaker.dialogs.wildbattle.FishingTriggerOptionsDialog;
import mapMaker.dialogs.wildbattle.WildBattleAreaDialog;
import mapMaker.dialogs.wildbattle.WildBattleTriggerOptionsDialog;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel;
import mapMaker.model.TriggerModel.TriggerModelType;
import pattern.generic.LocationTriggerMatcher;
import pattern.generic.MultiPointTriggerMatcher;
import pattern.generic.SinglePointTriggerMatcher;
import pattern.map.AreaMatcher;
import pattern.map.EventMatcher;
import pattern.map.FishingMatcher;
import pattern.map.ItemMatcher;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import pattern.map.MiscEntityMatcher;
import pattern.map.NPCMatcher;
import pattern.map.WildBattleAreaMatcher;
import util.FileIO;
import util.Point;
import util.PokeString;
import util.SerializationUtils;
import util.StringUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapMakerTriggerData {

	private final Set<AreaMatcher> areaData;
	private final Set<LocationTriggerMatcher> entities;

	private MapMaker mapMaker;
	private AreaMatcher defaultArea;

	private MapMakerTriggerData(MapMaker mapMaker) {
		this.mapMaker = mapMaker;

		this.areaData = new HashSet<>();
		this.entities = new HashSet<>();
	}

	MapMakerTriggerData(MapMaker mapMaker, AreaMatcher defaultArea) {
		this(mapMaker);

		this.defaultArea = defaultArea;
		this.addArea(defaultArea);
	}

	MapMakerTriggerData(MapMaker mapMaker, String mapTriggerFileName) {
		this(mapMaker);

		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapTriggerFileName);
		this.defaultArea = mapDataMatcher.getDefaultArea();
		this.areaData.addAll(mapDataMatcher.getAreas());
		this.entities.addAll(mapDataMatcher.getAllEntities());
	}

	void saveTriggers(String mapFileName) {
		// Collect and sort all the entities in a list
		List<LocationTriggerMatcher> entityList = entities.stream()
				.sorted(LocationTriggerMatcher.COMPARATOR)
				.collect(Collectors.toList());

		Set<String> entityNames = new HashSet<>();
		entityList.forEach(matcher -> getUniqueEntityName(matcher, entityNames));

		MapDataMatcher mapDataMatcher = new MapDataMatcher(areaData, entityList);

		FileIO.createFile(mapFileName);
		FileIO.overwriteFile(mapFileName, new StringBuilder(SerializationUtils.getJson(mapDataMatcher)));
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
					mapMaker.getCurrentMapName().getMapName(), typeName, basicEntityName, number++);
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
	}

	void drawTriggers(Graphics2D g2d, Point mapLocation) {
		for (LocationTriggerMatcher entity : this.entities) {
			TriggerModelType triggerModelType = entity.getTriggerModelType();

			if (entity instanceof SinglePointTriggerMatcher) {
				Point point = ((SinglePointTriggerMatcher)entity).getLocation();

				BufferedImage image = null;
				switch (triggerModelType) {
					case NPC:
						NPCMatcher npc = (NPCMatcher) entity;
						int imageIndex = MovableEntity.getTrainerSpriteIndex(npc.getSpriteIndex(), npc.getDirection());
						image = mapMaker.getTileFromSet(TileType.TRAINER, imageIndex);
						break;
				}

				if (image == null) {
					image = triggerModelType.getImage(mapMaker);
				}

				TileUtils.drawTileImage(g2d, image, point, mapLocation);
			} else if (entity instanceof MultiPointTriggerMatcher) {
				List<Point> entityLocation = ((MultiPointTriggerMatcher) entity).getLocation();
				for (Point point : entityLocation) {
					BufferedImage image = triggerModelType.getImage(mapMaker);
					TileUtils.drawTileImage(g2d, image, point, mapLocation);

					if (entity instanceof MapTransitionMatcher) {
						BufferedImage exitImage = TriggerModel.getMapExitImage(mapMaker);
						Point newLocation = Point.add(point, ((MapTransitionMatcher) entity).getDirection().getDeltaPoint());

						TileUtils.drawTileImage(g2d, exitImage, newLocation, mapLocation);
					}
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
		}
	}

	private LocationTriggerMatcher getTriggerFromDialog(TriggerModelType triggerModelType, LocationTriggerMatcher oldTrigger) {
		switch (triggerModelType) {
			case ITEM:
				return new ItemEntityDialog((ItemMatcher)oldTrigger).getMatcher(mapMaker);
			case HIDDEN_ITEM:
				return new ItemEntityDialog((ItemMatcher)oldTrigger).getMatcher(mapMaker).setHidden();
			case NPC:
				return new NPCEntityDialog((NPCMatcher)oldTrigger, mapMaker).getMatcher(mapMaker);
			case MISC_ENTITY:
				return new MiscEntityDialog((MiscEntityMatcher)oldTrigger).getMatcher(mapMaker);
			case MAP_TRANSITION:
				return new MapTransitionDialog((MapTransitionMatcher)oldTrigger, mapMaker).getMatcher(mapMaker);
			case EVENT:
				return new EventTriggerDialog((EventMatcher)oldTrigger).getMatcher(mapMaker);
			case WILD_BATTLE:
				if (oldTrigger == null) {
					return new WildBattleTriggerOptionsDialog(this.getWildBattleAreas()).getMatcher(mapMaker);
				} else {
					return new WildBattleAreaDialog((WildBattleAreaMatcher)oldTrigger).getMatcher(mapMaker);
				}
			case FISHING:
				if (oldTrigger == null) {
					return new FishingTriggerOptionsDialog(this.getFishingTriggers()).getMatcher(mapMaker);
				} else {
					return new FishingTriggerEditDialog((FishingMatcher) oldTrigger, -1).getMatcher(mapMaker);
				}
			default:
				Global.error("Unknown trigger model type " + triggerModelType);
				return null;
		}
	}

	private List<FishingMatcher> getFishingTriggers() {
		return this.entities
				.stream()
				.filter(entity -> entity instanceof FishingMatcher)
				.map(entity -> (FishingMatcher)entity)
				.collect(Collectors.toList());
	}

	private List<WildBattleAreaMatcher> getWildBattleAreas() {
		return this.entities
				.stream()
				.filter(entity -> entity instanceof WildBattleAreaMatcher)
				.map(entity -> (WildBattleAreaMatcher)entity)
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
	}

	public void moveTrigger(LocationTriggerMatcher trigger) {
		removeTrigger(trigger);
		mapMaker.setPlaceableTrigger(trigger);
	}

	public void addArea(AreaMatcher newArea) {
		this.areaData.add(newArea);
	}

	public AreaMatcher getDefaultArea() {
		return this.defaultArea;
	}

	public Set<AreaMatcher> getAreaData() {
		return this.areaData;
	}
}
