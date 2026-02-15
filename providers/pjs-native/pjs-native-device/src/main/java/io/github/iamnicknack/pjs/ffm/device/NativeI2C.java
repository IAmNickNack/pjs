package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CMessage;
import io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CRdwrData;

import static io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CConstants.I2C_M_RD;
import static io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CConstants.I2C_RDWR;

class NativeI2C implements I2C {

    private final I2CConfig config;
    private final IoctlOperations ioctlOperations;
    private final FileDescriptor fileDescriptor;

    public NativeI2C(I2CConfig config, NativeContext context, FileDescriptor fileDescriptor) {
        this.config = config;
        this.ioctlOperations = new IoctlOperationsImpl(context);
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public I2CConfig getConfig() {
        return config;
    }

    @Override
    public void transfer(Message[] messages) {
        var nativeMessages = new I2CMessage[messages.length];
        for (int i = 0; i < messages.length; i++) {
            nativeMessages[i] = new I2CMessage(
                    messages[i].address(),
                    messages[i].type() == Message.Type.READ ? I2C_M_RD : 0,
                    messages[i].length(),
                    messages[i].data()
            );
        }
        var response = execute(nativeMessages);

        assert response.messageCount() == messages.length;

        for (var i = 0; i < response.messageCount(); i++) {
            if (messages[i].type() == Message.Type.READ) {
                System.arraycopy(response.messages()[i].buffer(), 0, messages[i].data(), 0, messages[i].length());
            }
        }
    }

    private I2CRdwrData execute(I2CMessage... messages) {
        var payload = new I2CRdwrData(messages, messages.length);
        return ioctlOperations.ioctl(fileDescriptor.fd(), I2C_RDWR, payload);
    }

    @Override
    public void close() throws Exception {
        fileDescriptor.close();
    }
}
