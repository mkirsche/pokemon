package map.triggers;

import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.entity.movable.NPCEntity;
import map.entity.movable.PlayerEntity;
import pattern.MoveNPCTriggerMatcher;
import trainer.CharacterData;
import util.JsonUtils;
import util.Point;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

class MoveNPCTrigger extends Trigger {
    private final MoveNPCTriggerMatcher matcher;

    MoveNPCTrigger(String contents, String condition) {
        super(TriggerType.MOVE_NPC, contents, condition);
        this.matcher = JsonUtils.deserialize(contents, MoveNPCTriggerMatcher.class);
    }

    @Override
    protected void executeTrigger() {
        CharacterData player = Game.getPlayer();
        PlayerEntity playerEntity = player.getEntity();
        playerEntity.stall();

        MapData map = Game.getData().getMap(player.getMapName());
        NPCEntity entity = (NPCEntity)map.getEntity(this.matcher.getNpcEntityName());

        String path = getPath(entity, map);
        if (path == null) {
            Global.error("Cannot find valid path to exit :(");
        }

        entity.setTempPath(path, HaltTrigger::resume);
        HaltTrigger.addHaltTrigger();
    }

    private String getPath(NPCEntity entity, MapData map) {
        Point start = entity.getLocation();
        Point end = matcher.endLocationIsPlayer()
                ? Game.getPlayer().getLocation()
                : map.getEntranceLocation(matcher.getEndEntranceName());

        Queue<PathState> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(new PathState(start, PathDirection.defaultPath()));
        visited.add(start.toString());

        while (!queue.isEmpty()) {
            PathState currentState = queue.poll();

            if (!matcher.endLocationIsPlayer() && end.equals(currentState.location)) {
                return currentState.path + map.getExitDirection(matcher.getEndEntranceName()).getCharacter();
            }

            for (Direction direction : Direction.values()) {
                if (matcher.endLocationIsPlayer()) {
                    Point newLocation = Point.add(currentState.location, direction.getDeltaPoint());
                    if (newLocation.equals(end)) {
                        return currentState.path;
                    }
                }

                Point newLocation = entity.getNewLocation(currentState.location, direction, map);
                if (newLocation == null) {
                    continue;
                }

                if (!visited.contains(newLocation.toString())) {
                    visited.add(newLocation.toString());
                    queue.add(new PathState(
                            newLocation,
                            currentState.path + direction.getPathDirection().getCharacter()
                    ));
                }
            }
        }

        return null;
    }

    private class PathState {
        Point location;
        String path;

        private PathState(Point location, String path) {
            this.location = location;
            this.path = path;
        }
    }

}
