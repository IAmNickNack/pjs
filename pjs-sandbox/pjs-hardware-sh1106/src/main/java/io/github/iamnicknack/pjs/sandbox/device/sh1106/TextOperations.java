package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import java.nio.ByteBuffer;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations.PAGE_SIZE;

public interface TextOperations {
    /**
     * Write text to the display buffer.
     * @param page The page to write to.
     * @param column The column to write to.
     * @param text The text to write.
     */
    default void drawText(int page, int column, String text) {
        drawText((page * PAGE_SIZE) + column, text);
    }

    /**
     * Write text to the display buffer at the specified position.
     * @param position The position to write to.
     * @param text The text to write.
     */
    void drawText(int position, String text);;

    /**
     * Clear text from the display buffer.
     * @param page The page to clear.
     * @param column The column to start clearing from.
     * @param length The number of characters to clear.
     */
    default void clearText(int page, int column, int length) {
        clearText((page * PAGE_SIZE) + column, length);
    }

    /**
     * Clear text from the display buffer at the specified position.
     * @param position The position to start clearing from.
     * @param length The number of characters to clear.
     */
    void clearText(int position, int length);

    static TextOperations create(DisplayOperations displayOperations) {
        return new Default(displayOperations);
    }

    class Default implements TextOperations {
        private final DisplayOperations displayOperations;

        public Default(DisplayOperations displayOperations) {
            this.displayOperations = displayOperations;
        }

        @Override
        public void drawText(int position, String text) {
            var buffer = ByteBuffer.allocate(text.length() * 6);
            text.chars().forEach(c -> {
                buffer.put(FontData.getCharacterData(c));
            });
            displayOperations.setData(position, buffer.array(), 0, buffer.limit());
        }

        @Override
        public void clearText(int position, int length) {
            displayOperations.setData(position, new byte[length * 6], 0, length * 6);
        }
    }
}
