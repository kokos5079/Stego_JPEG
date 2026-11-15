import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageProcess {

    public BufferedImage extractImage(File f) throws Exception { // Функция извлечения картинки в матричном виде
        return ImageIO.read(f);
    }

    public void EncodeLSB(BufferedImage img, byte[] data){ // Функция встраивания информации в синий канал
        int width = img.getWidth();
        int height = img.getHeight();

        int dataIndex = 0;
        int bitIndex = 7;
        
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){

                if (dataIndex >= data.length){
                    return;
                }

                Color clr = new Color(img.getRGB(x, y));
                int red = clr.getRed();
                int green = clr.getGreen();
                int blue = clr.getBlue();

                byte msgByte = data[dataIndex];
                int msgBit = (msgByte >> bitIndex) & 1;
                
                blue = (blue & 0xFE) | msgBit;

                Color newColor = new Color(red, green, blue);
                img.setRGB(x, y, newColor.getRGB());

                bitIndex--;
                if (bitIndex < 0) {
                    bitIndex = 7;
                    dataIndex++;
                }
            }
        }   

    }

    byte[] decodeLSB(BufferedImage img, int dataLength) {
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
}

