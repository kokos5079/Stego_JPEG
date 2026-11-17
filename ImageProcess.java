import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcess {

    public BufferedImage extractImage(File f) throws Exception {
        return ImageIO.read(f);
    }

    public void encodeLSB(BufferedImage img, byte[] data) {
        int width = img.getWidth();
        int height = img.getHeight();

        final int metaDataLength = 4;
        int dataIndex = 0;
        byte[] dataLength = java.nio.ByteBuffer.allocate(4).putInt(data.length).array();   
             
        int metaDataIndex = 0; 
        int metaBitIndex = 7; 
        int bitIndex = 7; 
        
        // Исправлено: внешний цикл по y, внутренний по x (по строкам)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Color clr = new Color(img.getRGB(x, y));
                int red = clr.getRed();
                int green = clr.getGreen();
                int blue = clr.getBlue();

                if (metaDataIndex < metaDataLength) {
                    byte lengthByte = dataLength[metaDataIndex];
                    int lengthBit = (lengthByte >> metaBitIndex) & 1;
                    
                    blue = (blue & 0xFE) | lengthBit;

                    metaBitIndex--;
                    if (metaBitIndex < 0) {
                        metaBitIndex = 7;       
                        metaDataIndex++;        
                    }

                } else {
                    if (dataIndex >= data.length) {
                        return; 
                    }

                    byte msgByte = data[dataIndex];
                    int msgBit = (msgByte >> bitIndex) & 1;
                    
                    blue = (blue & 0xFE) | msgBit;

                    bitIndex--;
                    if (bitIndex < 0) {
                        bitIndex = 7;
                        dataIndex++;
                    }
                }

                Color newColor = new Color(red, green, blue);
                img.setRGB(x, y, newColor.getRGB());
            }
        }   
    }

    public byte[] decodeLSB(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        byte[] lengthBytes = extractBytes(img, 0, 4, width, height);
        int dataLength = java.nio.ByteBuffer.wrap(lengthBytes).getInt();

        if (dataLength <= 0 || dataLength > (width * height / 8)) {
            throw new IllegalArgumentException("Неверная длина сообщения или поврежденные данные.");
        }

        return extractBytes(img, 32, dataLength, width, height);
    }

    private byte[] extractBytes(BufferedImage img, int bitOffset, int lengthToRead, int width, int height) {
        byte[] data = new byte[lengthToRead];
        int bitTotalIndex = bitOffset;

        for (int i = 0; i < lengthToRead; i++) {
            byte currentByte = 0;
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                int x = bitTotalIndex % width;
                int y = bitTotalIndex / width;
                bitTotalIndex++;

                Color clr = new Color(img.getRGB(x, y));
                int bit = clr.getBlue() & 1;

                currentByte = (byte) (currentByte | (bit << bitIndex));
            }
            data[i] = currentByte;
        }
        return data;
    }
}