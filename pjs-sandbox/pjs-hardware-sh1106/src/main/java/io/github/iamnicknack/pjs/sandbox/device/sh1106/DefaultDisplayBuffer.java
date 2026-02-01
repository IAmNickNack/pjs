package io.github.iamnicknack.pjs.sandbox.device.sh1106;

public class DefaultDisplayBuffer implements DisplayOperations {

    private byte[] buffer = new byte[DisplayOperations.BUFFER_SIZE];

    @Override
    public void setData(int position, byte[] data, int offset, int length) {
        System.arraycopy(data, offset, buffer, position, length);
    }

    @Override
    public void getData(int position, byte[] buffer, int offset, int length) {
        System.arraycopy(this.buffer, position, buffer, offset, length);
    }

    @Override
    public int getPointValue(int position) {
        return this.buffer[position];
    }
}
