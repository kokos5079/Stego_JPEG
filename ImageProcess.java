import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcess {
    void hideText(BufferedImage img, byte[] data){
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
}