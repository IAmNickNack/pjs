package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DrawingOperations;

public class DefaultDrawingOperations implements DrawingOperations {

    public static final int PAGE_HEIGHT = 8;

    private final DisplayOperations displayOperations;

    public DefaultDrawingOperations(DisplayOperations displayOperations) {
        this.displayOperations = displayOperations;
    }

    @Override
    public void drawPixel(Point point) {
        var page = point.y() / PAGE_HEIGHT;
        var offset = point.y() % PAGE_HEIGHT;
        var column = point.x();

        displayOperations.orData(page, column, new byte[] {(byte) (1 << offset)}, 0, 1);
    }

    @Override
    public void drawLine(Point start, Point end) {
        var dx = end.x() - start.x();
        var dy = end.y() - start.y();

        if (dx == 0) {
            for (int y = start.y(); y <= end.y(); y++) {
                drawPixel(new Point(start.x(), y));
            }
        } else if (dy == 0) {
            for (int x = start.x(); x <= end.x(); x++) {
                drawPixel(new Point(x, start.y()));
            }
        } else if (dx > 0) {
            for (int x = start.x(); x < end.x(); x++) {
                var y = start.y() + (x - start.x()) * dy / dx;
                drawPixel(new Point(x, y));
            }
        } else {
            for (int x = start.x(); x > end.x(); x--) {
                var y = start.y() + (x - start.x()) * dy / dx;
                drawPixel(new Point(x, y));
            }
        }
    }

    @Override
    public void drawCircle(Point center, int radius) {
        int x = 0;
        int y = radius;
        int d = 1 - radius;   // decision parameter

        plot8WaySymmetry(center.x(), center.y(), x, y);

        while (x < y) {
            x++;

            if (d < 0) {
                // midpoint is inside the circle
                d += 2 * x + 1;
            } else {
                // midpoint is outside or on the circle
                y--;
                d += 2 * (x - y) + 1;
            }

            plot8WaySymmetry(center.x(), center.y(), x, y);
        }
    }

    private void plot8WaySymmetry(int cx, int cy, int x, int y) {
        drawPixel(cx + x, cy + y);
        drawPixel(cx - x, cy + y);
        drawPixel(cx + x, cy - y);
        drawPixel(cx - x, cy - y);
        drawPixel(cx + y, cy + x);
        drawPixel(cx - y, cy + x);
        drawPixel(cx + y, cy - x);
        drawPixel(cx - y, cy - x);
    }

    @Override
    public void drawEllipse(Point center, int rx, int ry) {
        int x = 0;
        int y = ry;

        long rx2 = (long) rx * rx;
        long ry2 = (long) ry * ry;

        long dx = 0;
        long dy = 2 * rx2 * y;

        // Region 1 decision parameter
        long d1 = ry2 - rx2 * ry + rx2 / 4;

        // Region 1: slope > -1
        while (dx < dy) {
            plot4WaySymmetry(center.x(), center.y(), x, y);

            x++;
            dx += 2 * ry2;

            if (d1 < 0) {
                d1 += dx + ry2;
            } else {
                y--;
                dy -= 2 * rx2;
                d1 += dx - dy + ry2;
            }
        }

        // Region 2 decision parameter
        long d2 = ry2 * ((long) x * x + x)
                + rx2 * ((long) (y - 1) * (y - 1))
                - rx2 * ry2;

        while (y >= 0) {
            plot4WaySymmetry(center.x(), center.y(), x, y);

            y--;
            dy -= 2 * rx2;

            if (d2 > 0) {
                d2 += rx2 - dy;
            } else {
                x++;
                dx += 2 * ry2;
                d2 += dx - dy + rx2;
            }
        }
    }

    private void plot4WaySymmetry(int cx, int cy, int x, int y) {
        drawPixel(cx + x, cy + y);
        drawPixel(cx - x, cy + y);
        drawPixel(cx + x, cy - y);
        drawPixel(cx - x, cy - y);
    }
}
