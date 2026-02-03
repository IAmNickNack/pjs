package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import java.nio.ByteBuffer;

public interface TextOperations {
    /**
     * Write text to the display buffer.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param text The text to write.
     */
    void drawText(int page, int column, String text);

    /**
     * Clear text from the display buffer.
     * @param page The page to clear.
     * @param column The column to start clearing from.
     * @param length The number of characters to clear.
     */
    void clearText(int page, int column, int length);

    static TextOperations create(DisplayOperations displayOperations) {
        return new Default(displayOperations);
    }

    class Default implements TextOperations {
        private final DisplayOperations displayOperations;

        public Default(DisplayOperations displayOperations) {
            this.displayOperations = displayOperations;
        }

        @Override
        public void drawText(int page, int column, String text) {
            var buffer = ByteBuffer.allocate(text.length() * 6);
            text.chars().forEach(c -> {
                buffer.put(FontData.getCharacterData(c));
            });
            displayOperations.setData(page, column, buffer.array(), 0, buffer.limit());
        }

        @Override
        public void clearText(int page, int column, int length) {
            displayOperations.setData(page, column, new byte[length * 6], 0, length * 6);
        }
    }
}
