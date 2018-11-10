package ru.runa.wfe.graph.image.util;

import java.awt.Dimension;
import java.awt.Point;

import ru.runa.wfe.graph.DrawProperties;

public class ActionUtils {
    public static final int ACTION_SIZE = 10;
    public static final int ACTION_DELIM = 4;

    public static Point getActionLocationOnTransition(int i, Point point0, Point point1, boolean fromTimer, boolean exclusive) {
        int shift = i * (ACTION_SIZE + ACTION_DELIM + 1) + ACTION_SIZE + 1;
        if (exclusive) {
            shift += 10;
        }
        if (fromTimer) {
            point0 = getPointOnLine(point0, point1, DrawProperties.GRID_SIZE);
        }
        Point location = getPointOnLine(point0, point1, shift);
        location.x -= ACTION_SIZE / 2;
        location.y -= ACTION_SIZE / 2;
        return location;
    }

    private static Point getPointOnLine(Point point0, Point point1, int shift) {
        Dimension diff = getDifference(point1, point0);
        Point dist = new Point(diff.width, diff.height);
        double distance = dist.distance(0, 0);
        double scaleFactor = shift / distance;
        Point translation = scale(dist, scaleFactor);
        Point location = new Point(point0);
        location.translate(translation.x, translation.y);
        return location;
    }

    private static Dimension getDifference(Point pt0, Point pt1) {
        return new Dimension(pt0.x - pt1.x, pt0.y - pt1.y);
    }

    private static Point scale(Point pt, double amount) {
        int x = (int) Math.floor(pt.x * amount);
        int y = (int) Math.floor(pt.y * amount);
        return new Point(x, y);
    }

    public static Point getActionLocationOnNode(int i, int[] coords, boolean xMinusGrid, boolean yMinusGrid) {
        // invert visuals
        int shift = (ACTION_DELIM + 2) + i * (ACTION_SIZE + (ACTION_DELIM + 2));
        Point p = new Point(coords[0] + coords[2] - shift - ACTION_SIZE, coords[1] + coords[3] - 3 * ACTION_SIZE / 2);
        if (xMinusGrid) {
            p.x -= DrawProperties.GRID_SIZE;
        }
        if (yMinusGrid) {
            p.y -= DrawProperties.GRID_SIZE;
        }
        return p;
    }

    public static boolean areActionsFitInLine(int actionsSize, Point point0, Point point1, boolean fromTimer, boolean exclusive) {
        int shift = actionsSize * (ACTION_SIZE + ACTION_DELIM);
        if (exclusive) {
            shift += 10;
        }
        double distance = point1.distance(point0);
        if (fromTimer) {
            distance -= DrawProperties.GRID_SIZE;
        }
        return shift < distance;
    }
}
