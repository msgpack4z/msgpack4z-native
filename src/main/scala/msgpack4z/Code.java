package msgpack4z;

final class Code {
    private Code() {
        throw new RuntimeException("should not create instance");
    }

    public static boolean isFixInt(byte b) {
        final int v = b & 0xFF;
        return v <= 0x7f || v >= 0xe0;
    }

    public static boolean isPosFixInt(byte b) {
        return (b & POSFIXINT_MASK) == 0;
    }

    public static boolean isNegFixInt(byte b) {
        return (b & NEGFIXINT_PREFIX) == NEGFIXINT_PREFIX;
    }

    public static boolean isFixStr(byte b) {
        return (b & (byte) 0xe0) == Code.FIXSTR_PREFIX;
    }

    public static boolean isFixedArray(byte b) {
        return (b & (byte) 0xf0) == Code.FIXARRAY_PREFIX;
    }

    public static boolean isFixedMap(byte b) {
        return (b & (byte) 0xe0) == Code.FIXMAP_PREFIX;
    }

    public static boolean isFixedRaw(byte b) {
        return (b & (byte) 0xe0) == Code.FIXSTR_PREFIX;
    }

    public static final byte POSFIXINT_MASK = (byte) 0x80;

    public static final byte FIXMAP_PREFIX = (byte) 0x80;
    public static final byte FIXARRAY_PREFIX = (byte) 0x90;
    public static final byte FIXSTR_PREFIX = (byte) 0xa0;

    public static final byte NIL = (byte) 0xc0;
    public static final byte FALSE = (byte) 0xc2;
    public static final byte TRUE = (byte) 0xc3;
    public static final byte BIN8 = (byte) 0xc4;
    public static final byte BIN16 = (byte) 0xc5;
    public static final byte BIN32 = (byte) 0xc6;
    public static final byte EXT8 = (byte) 0xc7;
    public static final byte EXT16 = (byte) 0xc8;
    public static final byte EXT32 = (byte) 0xc9;
    public static final byte FLOAT32 = (byte) 0xca;
    public static final byte FLOAT64 = (byte) 0xcb;
    public static final byte UINT8 = (byte) 0xcc;
    public static final byte UINT16 = (byte) 0xcd;
    public static final byte UINT32 = (byte) 0xce;
    public static final byte UINT64 = (byte) 0xcf;

    public static final byte INT8 = (byte) 0xd0;
    public static final byte INT16 = (byte) 0xd1;
    public static final byte INT32 = (byte) 0xd2;
    public static final byte INT64 = (byte) 0xd3;

    public static final byte FIXEXT1 = (byte) 0xd4;
    public static final byte FIXEXT2 = (byte) 0xd5;
    public static final byte FIXEXT4 = (byte) 0xd6;
    public static final byte FIXEXT8 = (byte) 0xd7;
    public static final byte FIXEXT16 = (byte) 0xd8;

    public static final byte STR8 = (byte) 0xd9;
    public static final byte STR16 = (byte) 0xda;
    public static final byte STR32 = (byte) 0xdb;

    public static final byte ARRAY16 = (byte) 0xdc;
    public static final byte ARRAY32 = (byte) 0xdd;

    public static final byte MAP16 = (byte) 0xde;
    public static final byte MAP32 = (byte) 0xdf;

    public static final byte NEGFIXINT_PREFIX = (byte) 0xe0;

    public static MsgType getType(final byte b) {
        final MsgType t = formatTable[b & 0xff];
        if (t == null) {
            throw new RuntimeException("invalid byte " + b);
        } else {
            return t;
        }
    }

    private final static MsgType[] formatTable = new MsgType[0x100];

    static {
        for (int b = 0; b <= 0xFF; ++b) {
            formatTable[b] = getType0((byte) b);
        }
    }

    private static MsgType getType0(final byte b) {
        if (isPosFixInt(b) || isNegFixInt(b)) {
            return MsgType.INTEGER;
        }
        if (Code.isFixStr(b)) {
            return MsgType.STRING;
        }
        if (Code.isFixedArray(b)) {
            return MsgType.ARRAY;
        }
        if (Code.isFixedMap(b)) {
            return MsgType.MAP;
        }
        switch (b) {
            case Code.NIL:
                return MsgType.NIL;
            case Code.FALSE:
            case Code.TRUE:
                return MsgType.BOOLEAN;
            case Code.BIN8:
            case Code.BIN16:
            case Code.BIN32:
                return MsgType.BINARY;
            case Code.FLOAT32:
            case Code.FLOAT64:
                return MsgType.FLOAT;
            case Code.UINT8:
            case Code.UINT16:
            case Code.UINT32:
            case Code.UINT64:
            case Code.INT8:
            case Code.INT16:
            case Code.INT32:
            case Code.INT64:
                return MsgType.INTEGER;
            case Code.STR8:
            case Code.STR16:
            case Code.STR32:
                return MsgType.STRING;
            case Code.ARRAY16:
            case Code.ARRAY32:
                return MsgType.ARRAY;
            case Code.MAP16:
            case Code.MAP32:
                return MsgType.MAP;
            case Code.FIXEXT1:
            case Code.FIXEXT2:
            case Code.FIXEXT4:
            case Code.FIXEXT8:
            case Code.FIXEXT16:
            case Code.EXT8:
            case Code.EXT16:
            case Code.EXT32:
                return MsgType.EXTENDED;

            default:
                return null;
        }
    }
}
