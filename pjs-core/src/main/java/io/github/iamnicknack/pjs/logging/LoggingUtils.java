package io.github.iamnicknack.pjs.logging;

public class LoggingUtils {

    private LoggingUtils() {}

    public static String byteArrayAsHexString(byte[] bytes) {
        return byteArrayAsHexString(bytes, 0, bytes.length);
    }

    public static String byteArrayAsHexString(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i;
        for (i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim() + ']';
    }
}
