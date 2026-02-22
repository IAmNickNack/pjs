package io.github.iamnicknack.pjs.sandbox.device.sh1106;

/**
 * Operations for drawing on a display.
 */
public interface DrawingOperations {

    /**
     * Draw a pixel at the given point.
     * @param point the point to draw the pixel at.
     */
    void drawPixel(Point point);

    /**
     * Draw a pixel at the given point.
     * @param x the x coordinate of the pixel.
     * @param y the y coordinate of the pixel.
     */
    default void drawPixel(int x, int y) {
        drawPixel(new Point(x, y));
    }

    /**
     * Draw a line between the two points.
     * @param start the start point.
     * @param end the end point.
     */
    void drawLine(Point start, Point end);

    /**
     * Draw a line between the two points.
     * @param x1 the x coordinate of the start point.
     * @param y1 the y coordinate of the start point.
     * @param x2 the x coordinate of the end point.
     * @param y2 the y coordinate of the end point.
     */
    default void drawLine(int x1, int y1, int x2, int y2) {
        drawLine(new Point(x1, y1), new Point(x2, y2));
    }

    /**
     * Draw a rectangle between the two points.
     * @param topLeft the top left point.
     * @param bottomRight the bottom right point.
     */
    default void drawRectangle(Point topLeft, Point bottomRight) {
        if (bottomRight.isLessThan(topLeft)) {
            throw new IllegalArgumentException("topLeft must be greater than or equal to bottomRight");
        }
        var topRight = new Point(bottomRight.x, topLeft.y);
        var bottomLeft = new Point(topLeft.x, bottomRight.y);

        drawLine(topLeft, topRight);
        drawLine(topRight, bottomRight);
        drawLine(bottomLeft, bottomRight);
        drawLine(topLeft, bottomLeft);
    }

    /**
     * Draw a rectangle between the two points.
     * @param x1 the x coordinate of the top left point.
     * @param y1 the y coordinate of the top left point.
     * @param x2 the x coordinate of the bottom right point.
     * @param y2 the y coordinate of the bottom right point.
     */
    default void drawRectangle(int x1, int y1, int x2, int y2) {
        drawRectangle(new Point(x1, y1), new Point(x2, y2));
    }


    /**
     * Fill the rectangle between the two points.
     * @param topLeft the top left point.
     * @param bottomRight the bottom right point.
     */
    default void fillRectangle(Point topLeft, Point bottomRight) {
        if (bottomRight.isLessThan(topLeft)) {
            throw new IllegalArgumentException("topLeft must be greater than or equal to bottomRight");
        }
        for (int x = topLeft.x; x <= bottomRight.x; x++) {
            drawLine(x, topLeft.y, x, bottomRight.y);
        }
    }

    /**
     * Fill the rectangle between the two points.
     * @param x1 the x coordinate of the top left point.
     * @param y1 the y coordinate of the top left point.
     * @param x2 the x coordinate of the bottom right point.
     * @param y2 the y coordinate of the bottom right point.
     */
    default void fillRectangle(int x1, int y1, int x2, int y2) {
        fillRectangle(new Point(x1, y1), new Point(x2, y2));
    }

    /**
     * Draw a circle centered at the given point.
     * @param centre the centre-point of the circle.
     * @param radius the radius of the circle.
     */
    void drawCircle(Point centre, int radius);

    /**
     * Draw a circle centered at the given point.
     * @param x the x coordinate of the centre-point.
     * @param y the y coordinate of the centre-point.
     * @param radius the radius of the circle.
     */
    default void drawCircle(int x, int y, int radius) {
        drawCircle(new Point(x, y), radius);
    }

    /**
     * Fill a circle centred at the given point.
     * @param centre the centre-point of the circle.
     * @param radius the radius of the circle.
     */
    void fillCircle(Point centre, int radius);

    /**
     * Fill a circle centred at the given point.
     * @param x the x coordinate of the centre-point.
     * @param y the y coordinate of the centre-point.
     * @param radius the radius of the circle.
     */
    default void fillCircle(int x, int y, int radius) {
        fillCircle(new Point(x, y), radius);
    }

    /**
     * Draw an ellipse centreed at the given point.
     * @param centre the centre-point of the ellipse.
     * @param rx the x radius of the ellipse.
     * @param ry the y radius of the ellipse.
     */
    void drawEllipse(Point centre, int rx, int ry);

    /**
     * Draw an ellipse centered at the given point.
     * @param x the x coordinate of the centre-point.
     * @param y the y coordinate of the centre-point.
     * @param rx the x radius of the ellipse.
     * @param ry the y radius of the ellipse.
     */
    default void drawEllipse(int x, int y, int rx, int ry) {
        drawEllipse(new Point(x, y), rx, ry);
    }

    /**
     * A point on the display.
     * @param x the x coordinate of the point.
     * @param y the y coordinate of the point.
     */
    record Point(int x, int y) {
        public boolean isLessThan(Point other) {
            return (this.x < other.x) || (this.y < other.y);
        }

        public Point addX(int x) {
            return new Point(this.x + x, this.y);
        }

        public Point addY(int y) {
            return new Point(this.x, this.y + y);
        }
    }
}
