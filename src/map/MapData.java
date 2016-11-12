package map;

import gui.GameFrame;
import main.Game;
import main.Global;
import map.entity.Entity;
import map.entity.EntityAction;
import map.entity.EntityData;
import map.entity.ItemEntityData;
import map.entity.TriggerEntityData;
import map.entity.npc.NPCEntityData;
import map.triggers.Trigger;
import map.triggers.TriggerData;
import map.triggers.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.MapExitMatcher;
import pattern.AreaDataMatcher.TriggerMatcher;
import trainer.CharacterData;
import util.FileIO;
import util.Point;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MapData {
	public final String name;

	private final int[] bgTile;
	private final int[] fgTile;
	private final int[] walkMap;
	private final int[] areaMap;

	private final AreaData[] areaData;
	
	private final Map<Integer, String> triggers;
	private final Map<String, Integer> mapEntrances;

	private final int width;
	private final int height;
	
	private final List<EntityData> entities;
	
	public MapData(File file) {
		name = file.getName();
		
		String beginFilePath = FileIO.makeFolderPath(file.getPath()) + name;
		
		BufferedImage bgMap = FileIO.readImage(beginFilePath + "_bg.png");
		width = bgMap.getWidth();
		height = bgMap.getHeight();
		bgTile = bgMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage fgMap = FileIO.readImage(beginFilePath + "_fg.png");
		fgTile = fgMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage moveMap = FileIO.readImage(beginFilePath + "_move.png");
		walkMap = moveMap.getRGB(0, 0, width, height, null, 0, width);
		
		BufferedImage areaM;
		File areaMapFile = new File(beginFilePath + "_area.png");
		if (areaMapFile.exists()) {
			areaM = FileIO.readImage(areaMapFile);
			areaMap = areaM.getRGB(0, 0, width, height, null, 0, width);
		} else {
			areaMap = new int[0];
		}

		entities = new ArrayList<>();
		triggers = new HashMap<>();
		mapEntrances = new HashMap<>();

		File f = new File(beginFilePath + ".txt");
		String fileText = FileIO.readEntireFileWithReplacements(f, false);

		AreaDataMatcher areaDataMatcher = AreaDataMatcher.matchArea(beginFilePath + ".txt", fileText);
		this.areaData = areaDataMatcher.getAreaData();

		entities.addAll(areaDataMatcher.getNPCs()
				.stream()
				.map(NPCEntityData::new)
				.collect(Collectors.toList()));

		entities.addAll(areaDataMatcher.getItems()
				.stream()
				.map(ItemEntityData::new)
				.collect(Collectors.toList()));

		for (TriggerMatcher matcher : areaDataMatcher.getMiscEntities()) {
			entities.addAll(matcher.getLocation()
					.stream()
					.map(point -> new TriggerEntityData(point.x, point.y, matcher))
					.collect(Collectors.toList()));
		}

		for (MapExitMatcher matcher : areaDataMatcher.getMapExits()) {
            matcher.setMapName(this.name);

            List<Point> entrances = matcher.getEntrances();
            for (Point point : entrances) {
                mapEntrances.put(matcher.getName(), getMapIndex(point.x, point.y)); // TODO: Doesn't work correctly
            }

            List<Point> exits = matcher.getExits();
			for (Point point : exits) {
				Trigger trigger = TriggerType.MAP_TRANSITION.createTrigger(AreaDataMatcher.getJson(matcher), null);
				triggers.put(getMapIndex(point.x, point.y), trigger.getName());
			}
        }

		for (TriggerMatcher matcher : areaDataMatcher.getTriggerData()) {
			TriggerData triggerData = new TriggerData(matcher);
			Trigger trigger = EntityAction.addActionGroupTrigger(triggerData.name, triggerData.name, matcher.getActions());

			for (Point point : matcher.getLocation()) {
				triggers.put(getMapIndex(point.x, point.y), trigger.getName());
			}
		}
	}

	// TODO: move this to its own file
	public enum WalkType {
		WATER(0x0000FF), 
		WALKABLE(0xFFFFFF), 
		NOT_WALKABLE(0x000000), 
		HOP_DOWN(0x00FF00), 
		HOP_UP(0xFF0000), 
		HOP_LEFT(0xFFFF00), 
		HOP_RIGHT(0x00FFFF),
		STAIRS_UP_RIGHT(0xFF00FF),
		STAIRS_UP_LEFT(0xFFC800);
		
		private final int value;
		
		WalkType(int v) {
			value = v;
		}
	}

	public int getPlayerMapIndex() {
		CharacterData player = Game.getPlayer();
		return getMapIndex(player.locationX, player.locationY);
	}

	public int getMapIndex(int x, int y) {
		return getMapIndex(x, y, width);
	}

	public static Integer getMapIndex(int x, int y, int width) {
		return x + y*width;
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public int getBgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return bgTile[getMapIndex(x, y)];
	}
	
	public int getFgTile(int x, int y) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		return fgTile[getMapIndex(x, y)];
	}
	
	public WalkType getPassValue(int x, int y) {
		if (!inBounds(x, y)) {
			return WalkType.NOT_WALKABLE;
		}
		
		int val = walkMap[getMapIndex(x, y)]&((1<<24) - 1);
		for (WalkType t: WalkType.values()) {
			if (t.value == val) {
				return t;
			}
		}
		
		return WalkType.NOT_WALKABLE;
	}
	
	public AreaData getArea(int x, int y) {
		if (areaData.length == 1) {
			return areaData[0];
		}

		if (!inBounds(x, y) || areaMap == null) {
			return AreaData.VOID;
		}

		int areaColor = areaMap[getMapIndex(x, y)];
		for (AreaData data : areaData) {
			if (data.isColor(areaColor)) {
				return data;
			}
		}

		Global.error("No area found with color " + areaColor + " for map " + this.name);
		return AreaData.VOID;
	}
	
	public String trigger() {
		int val = getPlayerMapIndex();
		if (triggers.containsKey(val)) {
			return triggers.get(val);
		}
	
		return null;
	}
	
	public boolean setCharacterToEntrance(String entranceName) {
        if (mapEntrances.containsKey(entranceName)) {
			int entrance = mapEntrances.get(entranceName);
			int newY = entrance / width;
			int newX = entrance - newY * width;
			Game.getPlayer().setLocation(newX, newY);

			return true;
		}

		return false;
	}

	public Entity[][] populateEntities() {
		Entity[][] res = new Entity[width][height];
		entities.stream()
				.filter(data -> data.isEntityPresent() || GameFrame.GENERATE_STUFF)
				.forEach(data -> {
					Entity entity = data.getEntity();
					entity.reset();
					entity.addData();
					res[entity.getX()][entity.getY()] = entity;
				});
		
		return res;
	}
}
