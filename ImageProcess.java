import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcess {

    public BufferedImage extractImage(File f) throws Exception { // Функция извлечения картинки в матричном виде
        return ImageIO.read(f);
    }

    public void encodeLSB(BufferedImage img, byte[] data) {
    int width = img.getWidth();
    int height = img.getHeight();

    final int metaDataLength = 4;
    int dataIndex = 0;
    byte[] dataLength = Convert.intToBytes(data.length);
    
    // Индексы для метаданных
    int metaDataIndex = 0; 
    int metaBitIndex = 7; // С какого бита начинаем читать байт длины

    int bitIndex = 7; // Бит для самого сообщения
    
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {

            Color clr = new Color(img.getRGB(x, y));
            int red = clr.getRed();
            int green = clr.getGreen();
            int blue = clr.getBlue();

            if (metaDataIndex < metaDataLength) {
                // --- ЭТАП 1: Встраиваем 4 байта длины ---
                
                // Берем текущий байт из массива длины
                byte lengthByte = dataLength[metaDataIndex];
                // Извлекаем нужный бит (от старшего к младшему)
                int lengthBit = (lengthByte >> metaBitIndex) & 1;
                
                // Встраиваем бит в синий канал
                blue = (blue & 0xFE) | lengthBit;

                // Двигаем указатель битов
                metaBitIndex--;
                if (metaBitIndex < 0) {
                    metaBitIndex = 7;       // Сброс на старший бит
                    metaDataIndex++;        // Переход к следующему байту длины
                }

            } else {
                // --- ЭТАП 2: Встраиваем само сообщение ---
                
                if (dataIndex >= data.length) {
                    return; // Сообщение закончилось
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

            // Сохраняем измененный пиксель (общий код для обоих этапов)
            Color newColor = new Color(red, green, blue);
            img.setRGB(x, y, newColor.getRGB());
        }
    }   
}

    byte[] decodeLSB(BufferedImage img, int dataLength) { // Функция расшифровки сообщения с передачей размера
        int width = img.getWidth();
        int height = img.getHeight();

        byte[] data = new byte[dataLength];
        int dataIndex = 0;
        int bitIndex = 7;
        byte currentByte = 0;

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (dataIndex >= data.length){
                    return data;
                }

                Color clr = new Color(img.getRGB(x, y));

                int blue = clr.getBlue();
                int bit = blue & 1;

                currentByte = (byte) (currentByte | (bit << bitIndex));

                bitIndex--;
                if (bitIndex < 0){
                    data[dataIndex] = currentByte;
                    dataIndex++;
                    currentByte = 0;
                    bitIndex = 7;
                }
            } 
        }
        return data;
    }

    byte[] decodeLSB(BufferedImage img) {


        return data;
    }

}

