package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.CommandSequence;

import java.nio.ByteBuffer;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.DEFAULT_STARTUP_SEQUENCE;

public class Sh1106Operations implements DisplayOperations, ControlOperations {

    private final Sh1106Driver display;

    public Sh1106Operations(Sh1106Driver display) {
        this.display = display;
    }

    @Override
    public void init() {
        display.command(DEFAULT_STARTUP_SEQUENCE);
    }

    @Override
    public void displayOn() {
        new CommandSequence()
                .append(Sh1106Driver.Command.DISPLAY_ON_OFF, 1)
                .writeTo(display.getCommandWriteOperation());
    }

    @Override
    public void displayOff() {
        new CommandSequence()
                .append(Sh1106Driver.Command.DISPLAY_ON_OFF, 0)
                .writeTo(display.getCommandWriteOperation());
    }

    @Override
    public void clear() {
        for (int i = 0; i < 8; i++) {
            clearPage(i);
        }
    }

    @Override
    public void clearPage(int page) {
        clearData(page, 0, 128);
    }

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        display.display(page, column, data, offset, length);
    }

    @Override
    public void setPosition(int page, int column) {
        new CommandSequence()
                .append(Sh1106Driver.Command.PAGE_ADDRESS, page)
                .append(Sh1106Driver.Command.DISPLAY_COLUMN_HIGH, (column + 2) >> 4)
                .append(Sh1106Driver.Command.DISPLAY_COLUMN_LOW, (column + 2) & 0x0f)
                .writeTo(display.getCommandWriteOperation());
    }

    @Override
    public void drawText(String text) {
        var buffer = ByteBuffer.allocate(text.length() * 6);
        text.chars().forEach(c -> {
            buffer.put(FontData.getCharacterData(c));
        });
        display.getDisplayWriteOperation().writeBytes(buffer.array(), 0, buffer.limit());
    }

    @Override
    public void clearText(int page, int column, int length) {
        setPosition(page, column);
        var nullData = new byte[length * 6];
        display.display(page, column, nullData, 0, nullData.length);
    }

    @Override
    public void appendChar(char c) {
        display.getDisplayWriteOperation().writeBytes(FontData.getCharacterData(c));
    }
}
