package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.logging.LoggingI2C;
import io.github.iamnicknack.pjs.logging.LoggingUtils;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.Command;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver.CommandSequence;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Sh1106DriverTest {

    @Test
    void canTurnDisplayOn() {
        var commandSequence = new CommandSequence()
                .append(Command.DISPLAY_ON_OFF, 1);

        commandSequence.writeTo((data, offset, length) -> {
            System.out.println(LoggingUtils.byteArrayAsHexString(data));

            assertEquals(1, length);
            assertEquals((byte) Command.DISPLAY_ON_OFF.opcode | 1, data[0]);
        });
    }

    @Test
    void canSetContrast() {
        var commandSequence = new CommandSequence()
                .append(Command.CONTRAST_MODE, 0xff);

        commandSequence.writeTo((data, offset, length) -> {
            System.out.println(LoggingUtils.byteArrayAsHexString(data));

            assertEquals(2, length);
            assertEquals((byte) Command.CONTRAST_MODE.opcode, data[0]);
            assertEquals((byte)0xff, data[1]);
        });
    }

    @Test
    void canWriteMultipleCommands() {
        SerialWriteOperation assertion = (data, offset, len) -> {
            System.out.println(LoggingUtils.byteArrayAsHexString(data, offset, len));

            assertEquals(3, len);
            assertEquals((byte) Command.CONTRAST_MODE.opcode, data[0]);
            assertEquals((byte)0xff, data[1]);
            assertEquals((byte) Command.DISPLAY_ON_OFF.opcode | 1, data[2]);
        };

        var commandSequence = new CommandSequence()
                .append(Command.CONTRAST_MODE, 0xff)
                .append(Command.DISPLAY_ON_OFF, 1);

        commandSequence.writeTo(assertion);
    }

    @Disabled
    @Nested
    class IntegrationTest {

        static {
            System.setProperty("pjs.proxy.port", "9090");
            System.setProperty("pjs.proxy.host", "10.0.0.2");
            System.setProperty("pjs.mode", "grpc");
        }

        private final I2CConfig oledConfig = I2CConfig.builder()
                .bus(1)
                .build();

        @Test
        void test() {

            try (var registry = DeviceRegistryLoader.defaultRegistry()) {
                assertNotNull(registry);

                var device = new LoggingI2C(registry.create(oledConfig));
                assertNotNull(device);

                Sh1106Driver oled = new Sh1106Driver(device, 0x3c);
                oled.command(Sh1106Driver.DEFAULT_STARTUP_SEQUENCE);

                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 1));

                Thread.sleep(1000);

                var data = new byte[128];
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 0));
                for (int page = 0; page < 8; page++) {
                    oled.display(page, 0, data, 0, data.length);
                }
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 1));

                Thread.sleep(1000);

                for (int i = 0; i < data.length; i++) {
                    data[i] = (i % 2 == 0) ? 0x55 : (byte)0xaa;
                }
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 0));
                for (int page = 0; page < 8; page++) {
                    oled.display(page, 0, data, 0, data.length);
                }
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 1));
//                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 0));

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }


        @Test
        void writeText() {
            try (var registry = DeviceRegistryLoader.defaultRegistry()) {
                var device = new LoggingI2C(registry.create(oledConfig));
                var oled = new Sh1106Driver(device, 0x3c);

                oled.command(Sh1106Driver.DEFAULT_STARTUP_SEQUENCE);
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 1));

                var data = new byte[128];
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 0));
                for (int page = 0; page < 8; page++) {
                    oled.display(page, 0, data, 0, data.length);
                }
                oled.command(new CommandSequence().append(Command.DISPLAY_ON_OFF, 1));

                var textBuffer = ByteBuffer.allocate(128);
                "Hello World!".chars().forEach(c -> {
                    var charData = FontData.getCharacterData(c);
                    textBuffer.put(charData);
                    textBuffer.put((byte)0);
                });

                oled.display(0, 0, textBuffer.array(), 0, textBuffer.limit());
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        @Test
        void displayOperations() {
            try (var registry = DeviceRegistryLoader.defaultRegistry()) {
                var device = new LoggingI2C(registry.create(oledConfig));
                var oled = new Sh1106Driver(device, 0x3c);
                var operations = new Sh1106Operations(oled);

                operations.init();
                operations.clear();
                operations.displayOn();

                operations.drawText(0, 0, "I Am Nicknack!");

                operations.setPosition(1, 0);
                for(int i = 0; i < 3; i++) {
                    Thread.sleep(250);
                    operations.appendChar('.');
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}