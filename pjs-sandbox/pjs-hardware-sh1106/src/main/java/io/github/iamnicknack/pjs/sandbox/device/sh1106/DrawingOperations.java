package io.github.iamnicknack.pjs.sandbox.device.sh1106;

public interface DrawingOperations {

    void drawPixel(Point point);

    default void drawPixel(int x, int y) {
        drawPixel(new Point(x, y));
    }

    void drawLine(Point start, Point end);

    default void drawLine(int x1, int y1, int x2, int y2) {
        drawLine(new Point(x1, y1), new Point(x2, y2));
    }

    default void drawRectangle(Point topLeft, Point bottomRight) {
        var topRight = new Point(bottomRight.x, topLeft.y);
        var bottomLeft = new Point(topLeft.x, bottomRight.y);

        drawLine(topLeft, topRight);
        drawLine(topRight, bottomRight);
        drawLine(bottomRight, bottomLeft);
        drawLine(bottomLeft, topLeft);
    }

    default void drawRectangle(int x1, int y1, int x2, int y2) {
        drawRectangle(new Point(x1, y1), new Point(x2, y2));
    }

    void drawCircle(Point center, int radius);

    default void drawCircle(int x, int y, int radius) {
        drawCircle(new Point(x, y), radius);
    }

    void drawEllipse(Point center, int rx, int ry);

    default void drawEllipse(int x, int y, int rx, int ry) {
        drawEllipse(new Point(x, y), rx, ry);
    }

    record Point(int x, int y) { }
}
