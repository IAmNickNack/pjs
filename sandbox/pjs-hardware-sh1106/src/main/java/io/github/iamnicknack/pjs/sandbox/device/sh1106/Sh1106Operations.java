package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.CommandSequence;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultSh1106Driver;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.DEFAULT_STARTUP_SEQUENCE;

public class Sh1106Operations implements DisplayOperations, ControlOperations, TextOperations {

    private final Sh1106Driver display;
    private final TextOperations textOperations;

    public Sh1106Operations(Sh1106Driver display) {
        this.display = display;
        this.textOperations = TextOperations.create(this);
    }

    @Override
    public void init() {
        display.command(DEFAULT_STARTUP_SEQUENCE);
    }

    @Override
    public void displayOn() {
        display.command(DefaultSh1106Driver.Command.DISPLAY_ON_OFF, 1);
    }

    @Override
    public void displayOff() {
        display.command(DefaultSh1106Driver.Command.DISPLAY_ON_OFF, 0);
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
    public void drawText(int page, int column, String text) {
        textOperations.drawText(page, column, text);
    }

    @Override
    public void clearText(int page, int column, int length) {
        textOperations.clearText(page, column, length);
    }

    public void setPosition(int position) {
        setPosition(position / PAGE_SIZE, position % PAGE_SIZE);
    }

    public void setPosition(int page, int column) {
        display.command(new CommandSequence()
                .append(DefaultSh1106Driver.Command.PAGE_ADDRESS, page)
                .append(DefaultSh1106Driver.Command.DISPLAY_COLUMN_HIGH, (column + 2) >> 4)
                .append(DefaultSh1106Driver.Command.DISPLAY_COLUMN_LOW, (column + 2) & 0x0f)
        );
    }
}
