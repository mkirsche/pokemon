package gui.view.map;

import battle.Battle;
import gui.GameData;
import gui.TileSet;
import gui.view.View;
import gui.view.ViewMode;
import main.Game;
import main.Global;
import map.AreaData;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.TerrainType;
import map.entity.Entity;
import map.entity.movable.MovableEntity;
import map.entity.movable.PlayerEntity;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class MapView extends View {

	private static final int AREA_NAME_ANIMATION_LIFESPAN = 2000;

    private static final PathDirection[] deltaDirections = {
			PathDirection.WAIT,
			PathDirection.LEFT,
			PathDirection.UP
    };

	private String currentMapName;
	private AreaData currentArea;
	private MapData currentMap;
	private SoundTitle currentMusicTitle;

	private Point start;
	private Point end;
	private Point draw;
	
	private int areaDisplayTime;

	private VisualState state;
	private MessageUpdate currentMessage;
	
	public MapView() {
		currentMapName = StringUtils.empty();
		setState(VisualState.MAP);

		areaDisplayTime = 0;

		start = new Point();
		end = new Point();
		draw = new Point();
	}

	boolean isState(VisualState state) {
		return this.state == state;
	}

	void setState(VisualState newState) {
		this.state = newState;
	}

	MessageUpdate getCurrentMessage() {
		return this.currentMessage;
	}

	void resetCurrentMessage() {
		this.currentMessage = null;
	}

	TerrainType getTerrain() {
		return this.currentArea.getTerrain();
	}

	@Override
	public void draw(Graphics g) {
		DrawUtils.fillCanvas(g, Color.BLACK);

		drawTiles(g);

		if (currentArea != null) {
			currentArea.getWeather().draw(g);
		}

		// Area Transition
		if (areaDisplayTime > 0) {
			drawAreaTransitionAnimation(g);
		}

		state.draw(g, this);
	}

	private void drawTiles(Graphics g) {
		GameData data = Game.getData();
		TileSet mapTiles = data.getMapTiles();

		// Background
		for (int y = start.y; y < end.y; y++) {
			for (int x = start.x; x < end.x; x++) {
				int bgTile = currentMap.getBgTile(x,y);
				if (TileSet.isValidMapTile(bgTile)) {
					BufferedImage img = mapTiles.getTile(bgTile);
					DrawUtils.drawTileImage(g, img, x, y, draw);
				}
			}
		}

		// Foreground
		for (int y = start.y; y < end.y; y++) {
			for (int x = start.x; x < end.x; x++) {

				// Draw foreground tiles
				int fgTile = currentMap.getFgTile(x, y);
				if (TileSet.isValidMapTile(fgTile)) {
					BufferedImage img = mapTiles.getTile(fgTile);
					DrawUtils.drawTileImage(g, img, x, y, draw);
				}

				// Draw entities
				// Check for entities above and to the left of this location to see if they just moved out and draw them again.
				for (PathDirection pathDirection : deltaDirections) {
					Point delta = pathDirection.getDeltaPoint();
					Point newPoint = Point.add(delta, x, y);

					Entity newPointEntity = currentMap.getEntity(newPoint);
					if (newPointEntity == null) {
						continue;
					}

					// TODO: I'm getting really confused about this whole check up and left only thing what is happening
					// If entity is a movable entity and they are moving right or down, do not draw them again.
					if (newPointEntity instanceof MovableEntity) {
						Direction transitionDirection = ((MovableEntity)newPointEntity).getDirection();
						if (!delta.isZero() && (transitionDirection == Direction.RIGHT || transitionDirection == Direction.DOWN)) {
							continue;
						}
					}
					// Not a movable entity, only draw once.
					else if (!delta.isZero()) {
						continue;
					}

					// TODO: Checking zero logic seems like it can be simplified
					newPointEntity.draw(g, draw, !delta.isZero());
				}
			}
		}
	}

	private void drawAreaTransitionAnimation(Graphics g) {
		int fontSize = 30;
		
		int insideWidth = FontMetrics.getSuggestedWidth(currentArea.getAreaName(), fontSize);
		int insideHeight = FontMetrics.getSuggestedHeight(fontSize);
		
		int borderSize = 2;
		int graySize = 10;
		
		int totalWidth = borderSize*2 + graySize*2 + insideWidth;
		int totalHeight = borderSize*2 + graySize*2 + insideHeight;
		
		int yValue = 0;
		
		// Calculate exit location
		if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN < .2) {
			yValue = -1*(int)(((AREA_NAME_ANIMATION_LIFESPAN - areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + (2*graySize)));
		}
		// Calculate entrance location
		else if (areaDisplayTime/(double)AREA_NAME_ANIMATION_LIFESPAN > .8) {
			yValue = -1*(int)(((areaDisplayTime)/(double)AREA_NAME_ANIMATION_LIFESPAN - 4/5.0) * 5 * (insideHeight + 2*graySize));
		}
		
		// Black border
		g.setColor(Color.BLACK);
		g.fillRect(0, yValue, totalWidth, totalHeight);

		// Light grey border
		g.setColor(new Color(195, 195, 195));
		g.fillRect(borderSize, yValue + borderSize, insideWidth + 2*graySize, insideHeight + 2*graySize);

		// White inside
		g.setColor(Color.WHITE);
		g.fillRect(borderSize + graySize, yValue + graySize + borderSize, insideWidth, insideHeight);

		g.setColor(Color.BLACK);
		FontMetrics.setFont(g, fontSize);
		DrawUtils.drawCenteredString(g, currentArea.getAreaName(), 0, yValue, totalWidth, totalHeight);
	}

	@Override
	public void update(int dt) {
		CharacterData player = Game.getPlayer();
		PlayerEntity playerEntity = player.getEntity();

		boolean showMessage = !this.isState(VisualState.BATTLE);

		checkMapReset();
		
		if (areaDisplayTime > 0) {
			areaDisplayTime -= dt;
		}
		
		// New area
		AreaData area = currentMap.getArea(player.getLocation());
		String areaName = area.getAreaName();

		// If new area has a new name, display the area name animation
		if (currentArea != null && !StringUtils.isNullOrEmpty(areaName) && !areaName.equals(currentArea.getAreaName())) {
			areaDisplayTime = AREA_NAME_ANIMATION_LIFESPAN;
		}

		player.setAreaName(areaName);
		currentArea = area;

		// Queue to play new area's music.
		SoundTitle areaMusic = area.getMusic();
		if (currentMusicTitle != areaMusic) {
			currentMusicTitle = areaMusic;
			playAreaMusic();
		}

		this.state.update(dt, this);

		Point tilesLocation = Point.scaleDown(Global.GAME_SIZE, Global.TILE_SIZE);

		this.draw = playerEntity.getDrawLocation();
		this.start = Point.scaleDown(Point.negate(this.draw), Global.TILE_SIZE);
		this.end = Point.add(this.start, tilesLocation, new Point(6, 6)); // TODO: What is the 6, 6 all about?

		// Update each non-player entity on the map
		currentMap.updateEntities(dt, this);

		if (this.isState(VisualState.MAP)) {
			playerEntity.update(dt, currentMap, this);
        }

        boolean emptyMessage = this.currentMessage == null || StringUtils.isNullOrEmpty(this.currentMessage.getMessage());
		if (showMessage && emptyMessage && Messages.hasMessages()) {
			cycleMessage();
			if (this.currentMessage != null && this.currentMessage.getUpdateType() != Update.ENTER_BATTLE) {
				setState(VisualState.MESSAGE);
			}
		}
	}

	private void checkMapReset() {
		GameData data = Game.getData();
		CharacterData player = Game.getPlayer();

		if (!currentMapName.equals(player.getMapName()) || player.mapReset()) {
			currentMapName = player.getMapName();
			currentMap = data.getMap(currentMapName);

			if (player.mapReset()) {
				player.setMapReset(false);
				currentMap.setCharacterToEntrance();
			}

			currentMap.populateEntities();
			setState(VisualState.MAP);
		}
	}

	void cycleMessage() {
		currentMessage = Messages.getNextMessage();

		// Check if the next message is a trigger and execute if it is
		if (currentMessage.trigger()) {
			Trigger trigger = Game.getData().getTrigger(currentMessage.getTriggerName());
			if (trigger.isTriggered()) {
				trigger.execute();
				if (!this.isState(VisualState.MESSAGE)) {
					currentMessage = null;
				}

			}
		}
	}

	private void playAreaMusic() {
		final SoundTitle music;
		if (currentMusicTitle != null) {
			music = currentMusicTitle;
		}
		else {
			if(currentArea != null) {
				System.err.println("No music specified for current area " + currentArea.getAreaName() + ".");
			}
			music = SoundTitle.DEFAULT_TUNE;
		}

		SoundPlayer.soundPlayer.playMusic(music);
	}

	public void setBattle(Battle battle, boolean seenWild) {
		this.setState(VisualState.BATTLE);
		VisualState.setBattle(battle, seenWild, this.getTerrain());
	}

	@Override
	public ViewMode getViewModel() {
		return ViewMode.MAP_VIEW;
	}

	@Override
	public void movedToFront() {
		playAreaMusic();
	}
}
