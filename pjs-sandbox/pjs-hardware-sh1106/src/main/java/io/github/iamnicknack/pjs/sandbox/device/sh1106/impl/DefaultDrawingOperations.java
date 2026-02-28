package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DrawingOperations;

import java.util.Arrays;

/**
 * Naive implementation of {@link DrawingOperations} which are not optimised for the page layout of the SH1106.
 */
public class DefaultDrawingOperations implements DrawingOperations {

    public static final int PAGE_HEIGHT = 8;

    private final BufferedDisplayOperations displayOperations;

    public DefaultDrawingOperations(BufferedDisplayOperations displayOperations) {
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

        if (dx == 0 && dy == 0) {
            drawPixel(start);
        } else if (dx == 0) {
            drawVerticalLine(start.x(), start.y(), end.y());
        } else if (dy == 0) {
            drawHorizontalLine(start.x(), end.x(), start.y());
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

    public void drawHorizontalLine(int x1, int x2, int y) {
        var buffer = new byte[x2 - x1 + 1];
        var pixel = (byte) (1 << (y % 8));
        Arrays.fill(buffer, pixel);
        displayOperations.orData(y / PAGE_HEIGHT, x1, buffer, 0, buffer.length);
    }

    public void drawVerticalLine(int x, int y1, int y2) {
        long mask = 0xffffffffffffffffL;
        long low = mask << y1;
        long high = mask >>> (64 - y2);
        long value = low & high;

        for (int i = 0; i < 8 && value != 0; i++) {
            var pageValue = value & 0xff;
            if (pageValue != 0) {
                displayOperations.orData(i, x, new byte[] {(byte) pageValue}, 0, 1);
            }
            value >>= 8;
        }
    }

    @Override
    public void drawCircle(Point center, int radius) {
        renderCircle(center.addX(128), radius, this::plot8WaySymmetry);
    }

    @Override
    public void fillCircle(Point center, int radius) {
        renderCircle(center.addX(128), radius, this::fill8WaySymmetry);
    }

    private void renderCircle(Point center, int radius, PointsConsumer consumer) {
        int x = 0;
        int y = radius;
        int d = 1 - radius;   // decision parameter

        consumer.accept(center.x(), center.y(), x, y);

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

            consumer.accept(center.x(), center.y(), x, y);
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

    private void fill8WaySymmetry(int cx, int cy, int x, int y) {
        drawLine(cx - x, cy - y, cx - x, cy + y);
        drawLine(cx + x, cy - y, cx + x, cy + y);
        drawLine(cx - y, cy - x, cx - y, cy + x);
        drawLine(cx + y, cy - x, cx + y, cy + x);
    }

    @Override
    public void drawEllipse(Point center, int rx, int ry) {
        renderEllipse(center, rx, ry, this::plot4WaySymmetry);
    }

    private void renderEllipse(Point center, int rx, int ry, PointsConsumer consumer) {
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
            consumer.accept(center.x(), center.y(), x, y);

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
            consumer.accept(center.x(), center.y(), x, y);

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

    @FunctionalInterface
    private interface PointsConsumer {
        void accept(int x1, int y1, int x2, int y2);
    }
}
