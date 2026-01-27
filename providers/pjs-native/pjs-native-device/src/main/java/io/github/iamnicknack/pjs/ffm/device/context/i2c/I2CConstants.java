package io.github.iamnicknack.pjs.ffm.device.context.i2c;

@SuppressWarnings("unused")
public class I2CConstants {

    /**
     * ioctl: fetch available i2c features
     */
    public static final int I2C_CHECK_FUNCTIONALITY         = 0x0705;
    /**
     * ioctl: i2c read/write
     */
    public static final int I2C_RDWR                        = 0x0707; // 0x0707

    /**
     * ioctl: configure peripheral address
     */
    public static final int I2C_PERIPHERAL                  = 0x0703;
    public static final int I2C_PERIPHERAL_FORCE            = 0x0706;

    public static final int I2C_M_RD                        = 0b00000000_00000000_00000001; // 0x1
    public static final int I2C_M_TEN                       = 0b00000000_00000000_00010000; // 0x10
    public static final int I2C_M_DMA_SAFE                  = 0b00000000_00000010_00000000; // 0x200
    public static final int I2C_M_RECV_LEN                  = 0b00000000_00000100_00000000; // 0x400
    public static final int I2C_M_NO_RD_ACK                 = 0b00000000_00001000_00000000; // 0x800
    public static final int I2C_M_IGNORE_NAK                = 0b00000000_00010000_00000000; // 0x1000
    public static final int I2C_M_REV_DIR_ADDR              = 0b00000000_00100000_00000000; // 0x2000
    public static final int I2C_M_NOSTART                   = 0b00000000_01000000_00000000; // 0x4000
    public static final int I2C_M_STOP                      = 0b00000000_10000000_00000000; // 0x8000

    public static final int I2C_FUNC_I2C                    = 0b00000000_00000000_00000001; // 0x1
    public static final int I2C_FUNC_10BIT_ADDR             = 0b00000000_00000000_00000010; // 0x2
    public static final int I2C_FUNC_PROTOCOL_MANGLING      = 0b00000000_00000000_00000100; // 0x4
    public static final int I2C_FUNC_SMBUS_PEC              = 0b00000000_00000000_00001000; // 0x8
    public static final int I2C_FUNC_NOSTART                = 0b00000000_00000000_00010000; // 0x10
    public static final int I2C_FUNC_SLAVE                  = 0b00000000_00000000_00100000; // 0x20
    public static final int I2C_FUNC_SMBUS_BLOCK_PROC_CALL  = 0b00000000_10000000_00000000; // 0x8000
    public static final int I2C_FUNC_SMBUS_QUICK            = 0b00000000_00000001_00000000; // 0x100
    public static final int I2C_FUNC_SMBUS_READ_BYTE        = 0b00000000_00000010_00000000; // 0x200
    public static final int I2C_FUNC_SMBUS_WRITE_BYTE       = 0b00000000_00000100_00000000; // 0x400
    public static final int I2C_FUNC_SMBUS_READ_BYTE_DATA   = 0b00000000_00001000_00000000; // 0x800
    public static final int I2C_FUNC_SMBUS_WRITE_BYTE_DATA  = 0b00000000_00010000_00000000; // 0x1000
    public static final int I2C_FUNC_SMBUS_READ_WORD_DATA   = 0b00000000_00100000_00000000; // 0x2000
    public static final int I2C_FUNC_SMBUS_WRITE_WORD_DATA  = 0b00000000_01000000_00000000; // 0x4000
    public static final int I2C_FUNC_SMBUS_PROC_CALL        = 0b00000000_10000000_00000000; // 0x8000
    public static final int I2C_FUNC_SMBUS_READ_BLOCK_DATA  = 0b00000001_00000000_00000000; // 0x10000
    public static final int I2C_FUNC_SMBUS_WRITE_BLOCK_DATA = 0b00000010_00000000_00000000; // 0x20000
    public static final int I2C_FUNC_SMBUS_READ_I2C_BLOCK   = 0b00000100_00000000_00000000; // 0x40000
    public static final int I2C_FUNC_SMBUS_WRITE_I2C_BLOCK  = 0b00001000_00000000_00000000; // 0x80000
    public static final int I2C_FUNC_SMBUS_HOST_NOTIFY      = 0b00010000_00000000_00000000; // 0x100000

    public static final int I2C_SMBUS_BLOCK_MAX             = 0b00000000_00000000_00100000; // 0x20
    public static final int I2C_SMBUS_READ                  = 0b00000000_00000000_00000001; // 0x1
    public static final int I2C_SMBUS_WRITE                 = 0b00000000_00000000_00000000; // 0x0
    public static final int I2C_SMBUS_QUICK                 = 0b00000000_00000000_00000000; // 0x0
    public static final int I2C_SMBUS_BYTE                  = 0b00000000_00000000_00000001; // 0x1
    public static final int I2C_SMBUS_BYTE_DATA             = 0b00000000_00000000_00000010; // 0x2
    public static final int I2C_SMBUS_WORD_DATA             = 0b00000000_00000000_00000011; // 0x3
    public static final int I2C_SMBUS_PROC_CALL             = 0b00000000_00000000_00000100; // 0x4
    public static final int I2C_SMBUS_BLOCK_DATA            = 0b00000000_00000000_00000101; // 0x5
    public static final int I2C_SMBUS_I2C_BLOCK_BROKEN      = 0b00000000_00000000_00000110; // 0x6
    public static final int I2C_SMBUS_BLOCK_PROC_CALL       = 0b00000000_00000000_00000111; // 0x7
    public static final int I2C_SMBUS_I2C_BLOCK_DATA        = 0b00000000_00000000_00001000; // 0x8

    public static final int I2C_FUNC_SMBUS_BYTE             = 0b00000000_00000110_00000000; // 0x600
    public static final int I2C_FUNC_SMBUS_BYTE_DATA        = 0b00000000_00011000_00000000; // 0x1800
    public static final int I2C_FUNC_SMBUS_WORD_DATA        = 0b00000000_01100000_00000000; // 0x6000
    public static final int I2C_FUNC_SMBUS_BLOCK_DATA       = 0b00110000_00000000_00000000; // 0x300000
    public static final int I2C_FUNC_SMBUS_I2C_BLOCK        = 0b00001100_00000000_00000000; // 0xc0000
    public static final int I2C_FUNC_SMBUS_EMUL             = 0b00001111_00000001_00011000; // 0xf018
    public static final int I2C_FUNC_SMBUS_EMUL_ALL         = 0b00001111_11111101_00011000; // 0xffd18
}
