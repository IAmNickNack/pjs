package io.github.iamnicknack.pjs.sandbox.device.sh1106;

public interface DisplayOperations {

    /**
     * Clear the entire display.
     */
    void clear();

    /**
     * Clear a single page of the display.
     */
    void clearPage(int page);

    /**
     * Write to the display buffer at the specified location.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param data The data to write.
     * @param offset The offset within the data array to start writing from.
     * @param length The number of bytes to write.
     */
    void setData(int page, int column, byte[] data, int offset, int length);

    /**
     * Clear a section of the display buffer at the specified location.
     * @param page The page to clear.
     * @param column The column to start clearing from.
     * @param length The number of bytes to clear.
     */
    default void clearData(int page, int column, int length) {
        var data = new byte[length];
        setData(page, column, data, 0, length);
    }

    /**
     * Set the current display position.
     * @param page The page to set the position on.
     * @param column The column to set the position on.
     */
    void setPosition(int page, int column);

    /**
     * Write text to the display buffer.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param text The text to write.
     */
    default void drawText(int page, int column, String text) {
        setPosition(page, column);
        drawText(text);
    }

    /**
     * Write text to the display buffer at the current position.
     * @param text The text to write.
     */
    void drawText(String text);

    /**
     * Clear text from the display buffer.
     * @param page The page to clear.
     * @param column The column to start clearing from.
     * @param length The number of characters to clear.
     */
    void clearText(int page, int column, int length);

    /**
     * Append a character to the display buffer at the current position.
     * @param c The character to append.
     */
    void appendChar(char c);

}
