import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcess {

    public BufferedImage extractImage(File f) throws Exception {
        validateFile(f);
        BufferedImage img = ImageIO.read(f);
        validateImage(img);
        return img;
    }

    public void encodeLSB(BufferedImage img, byte[] data) {
        validateEncode(img, data);

        int width = img.getWidth();
        int height = img.getHeight();

        final int metaDataLength = 4;
        int dataIndex = 0;
        byte[] dataLength = java.nio.ByteBuffer.allocate(4).putInt(data.length).array();   
             
        int metaDataIndex = 0; 
        int metaBitIndex = 7; 
        int bitIndex = 7; 
        
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
        validateDecode(img);

        int width = img.getWidth();
        int height = img.getHeight();

        byte[] lengthBytes = extractBytes(img, 0, 4, width, height);
        int dataLength = java.nio.ByteBuffer.wrap(lengthBytes).getInt();

        validateDataLength(dataLength, width, height);

        return extractBytes(img, 32, dataLength, width, height);
    }

    private byte[] extractBytes(BufferedImage img, int bitOffset, int lengthToRead, int width, int height) {
        validateExtract(img, bitOffset, lengthToRead, width, height);

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


// ========= МЕТОДЫ ДЛЯ ВАЛИДАЦИИ =========

    private void validateFile(File f){
        if (f == null || !f.exists() || !f.isFile()) {
            throw new IllegalArgumentException("Файл не найден или не доступен.");
        }
    }

    private void validateImage(BufferedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("Не удалось прочитать изображение.");
        }
    }

    private void validateEncode(BufferedImage img, byte[] data) {
        validateImage(img);
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Отсутствуют данные для встраивания.");
        }
        int maxBytes = ((int) img.getWidth() * img.getHeight() - 32) / 8;
        if (data.length > maxBytes) {
            throw new IllegalArgumentException("Недостаточно места для встраивания данных.");
        }
    }

    private void validateDecode(BufferedImage img) {
        validateImage(img);
        int imgSize = img.getHeight() * img.getWidth();
        if (imgSize < 32) {
            throw new IllegalArgumentException("Изображение слишком мало.");
        }
    }

    private void validateDataLength(int dataLength, int width, int height) {
        int maxBytes = ((int) width * height - 32) / 8;
        if (dataLength <= 0 || dataLength > maxBytes) {
            throw new IllegalArgumentException("Неверная длина сообщения.");
        }
    }

    private void validateExtract(BufferedImage img, int offset, int length, int width, int height) {
        validateImage(img);
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("Некорректное смещение или длина чтения.");
        }
        
        int imgSize = width * height;
        int lastByte = offset + length * 8;
        if (lastByte > imgSize) {
            throw new IllegalArgumentException("Выход за пределы изображения при чтении");
        }
    }


}