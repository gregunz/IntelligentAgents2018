package utils;

import java.util.Random;

public class Position2D {

    private final static Random r = new Random();

    private int x;
    private int y;

    public Position2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Position2D random(int limitX, int limitY) {
        return random(0, 0, limitX, limitY);
    }

    public static Position2D random(int fromX, int fromY, int toX, int toY) {
        int difX = toX - fromX;
        int difY = toY - fromY;

        return new Position2D(
                r.nextInt(difX) + fromX,
                r.nextInt(difY) + fromY
        );
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isDifferent(Position2D other) {
        return this.x != other.x || this.y != other.y;
    }
}
