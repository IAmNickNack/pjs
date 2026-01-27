package io.github.iamnicknack.pjs.ffm.device.context;

public class IoctlConstants {

    public final static long _IOC_NRBITS = 8;
    public final static long _IOC_TYPEBITS = 8;
    public final static long _IOC_SIZEBITS = 14;
    public final static long _IOC_DIRBITS = 2;
    public final static long _IOC_NRSHIFT = 0;
    public final static long _IOC_NONE = 0;
    public final static long _IOC_READ = 2;
    public final static long _IOC_WRITE = 1;

    public static long _IOC_TYPESHIFT = _IOC_NRSHIFT + _IOC_NRBITS;
    public static long _IOC_SIZESHIFT = _IOC_TYPESHIFT + _IOC_TYPEBITS;
    public static long _IOC_DIRSHIFT = _IOC_SIZESHIFT + _IOC_SIZEBITS;


    public static long _IOR(long type, long nr, long size) {
        return _IOC(_IOC_READ, type, nr, size);
    }

    public static long _IOW(long type, long nr, long size) {
        return _IOC(_IOC_WRITE, type, nr, size);
    }

    public static long _IOWR(long type, long nr, long size) {
        return _IOC(_IOC_READ | _IOC_WRITE, type, nr, size);
    }

    static long _IOC(long dir, long type, long nr, long size) {
        return (
                (dir << _IOC_DIRSHIFT) |
                (type << _IOC_TYPESHIFT) |
                (nr << _IOC_NRSHIFT) |
                (size << _IOC_SIZESHIFT)
        );
    }
}
