package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.lang.Transition;

public class TaskNodeFigure extends AbstractUmlFigure {

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        if (transition != null && transition.isTimerTransition()) {
            return new Point(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getTransitionPoint(transition, x, y);
    }

    @Override
    public void fill(Graphics2D graphics) {
        Rectangle rect = getRectangle();
        if (node.isGraphMinimizedView()) {
            graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
        } else {
            graphics.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 10);
        }
        if (!node.isGraphMinimizedView() && hasTimer) {
            graphics.fillOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
        }
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        Rectangle rect = getRectangle();
        if (node.isGraphMinimizedView()) {
            graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
        } else {
            graphics.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 10);
        }
        if (!node.isGraphMinimizedView()) {
            drawActions(graphics);
            drawTextInfo(graphics, 1);
        }
        if (!node.isGraphMinimizedView() && hasTimer) {
            // Clean area for timer
            Color orig = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.fillOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
            graphics.setColor(orig);

            // Draw timer
            graphics.drawOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
            graphics.drawLine(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE,
                    coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE + 5);
            graphics.drawLine(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE,
                    coords[0] + DrawProperties.GRID_SIZE + 5, coords[1] + coords[3] - DrawProperties.GRID_SIZE - 5);
        }
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        return getRectangle();
    }

    @Override
    public Rectangle getRectangle() {
        if (node.isGraphMinimizedView()) {
            return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
        }
        return new Rectangle(coords[0] + DrawProperties.GRID_SIZE, coords[1], coords[2] - DrawProperties.GRID_SIZE,
                coords[3] - DrawProperties.GRID_SIZE);
    }

}
