import java.nio.ByteBuffer;

public class Convert {
    public static byte[] intToBytes(int value) {
    return new byte[] {
        (byte) (value >> 24),  // старший байт
        (byte) (value >> 16),
        (byte) (value >> 8),
        (byte) value           // младший байт
    };
}
}

