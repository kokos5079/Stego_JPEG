import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Создаем тестовое изображение с поддержкой ARGB, чтобы избежать искажений LSB при сохранении
            // Используем TYPE_3BYTE_BGR для стабильного сохранения пикселей без альфа-канала
            BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);            
            // 2. Секретное сообщение
            String secretText = "hello";
            byte[] secretBytes = secretText.getBytes("UTF-8");
            
            ImageProcess processor = new ImageProcess();
            
            // 3. Встраиваем данные
            processor.encodeLSB(img, secretBytes);
            
            // Сохраняем картинку
            File outFile = new File("test_secret.png");
            ImageIO.write(img, "png", outFile);
            System.out.println("Сообщение успешно зашифровано и сохранено в test_secret.png");
            
            // 4. Загружаем картинку обратно для проверки
            BufferedImage loadedImg = processor.extractImage(outFile);
            
            // 5. Расшифровываем
            byte[] extractedBytes = processor.decodeLSB(loadedImg);
            
            // 6. Выводим результат
            String resultText = new String(extractedBytes, "UTF-8");
            System.out.println("Извлеченный текст: " + resultText);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}