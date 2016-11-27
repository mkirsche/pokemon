package map;

import input.ControlKey;
import util.Point;

public enum Direction {
    RIGHT('r', 1, 0, ControlKey.RIGHT),
    UP('u', 0, -1, ControlKey.UP),
    LEFT('l', -1, 0, ControlKey.LEFT),
    DOWN('d', 0, 1, ControlKey.DOWN);

    public final char character;
    public final int dx;
    public final int dy; // TODO: Change to just a delta point
    private final ControlKey key;
    public Direction opposite; // Really this should be final but it won't let me include this in the constructor

    Direction(char character, int dx, int dy, ControlKey key) {
        this.character = character;

        this.dx = dx;
        this.dy = dy;

        this.key = key;
    }

    public static final char WAIT_CHARACTER = 'w';

    // This is dumb fuck Java
    static {
        RIGHT.opposite = LEFT;
        UP.opposite = DOWN;
        LEFT.opposite = RIGHT;
        DOWN.opposite = UP;
    }

    public Point getDeltaPoint() {
        return new Point(dx, dy);
    }

    public ControlKey getKey() {
        return this.key;
    }
}
