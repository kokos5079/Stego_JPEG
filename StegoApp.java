import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class StegoApp extends JFrame {

    private final ImageProcess imageProcessor = new ImageProcess();
    private File selectedEncodeFile;
    private File selectedDecodeFile;

    private JLabel encodeFileLabel;
    private JTextArea inputMessageArea;
    private JComboBox<String> formatComboBox;

    private JLabel decodeFileLabel;
    private JTextArea outputMessageArea;

    public StegoApp() {
        setTitle("LSB Стеганография");
        setSize(520, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Зашифровать текст", createEncodePanel());
        tabbedPane.addTab("Расшифровать текст", createDecodePanel());

        add(tabbedPane);
    }

    private JPanel createEncodePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель: выбор файла
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        JButton selectFileBtn = new JButton("Выбрать изображение...");
        encodeFileLabel = new JLabel("Файл не выбран");
        topPanel.add(selectFileBtn, BorderLayout.WEST);
        topPanel.add(encodeFileLabel, BorderLayout.CENTER);

        // Центр: ввод текста
        inputMessageArea = new JTextArea();
        inputMessageArea.setLineWrap(true);
        inputMessageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputMessageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Текст для встраивания"));

        // Нижняя панель: формат и кнопка сохранения
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        formatComboBox = new JComboBox<>(new String[]{"png", "bmp"});
        JButton encodeAndSaveBtn = new JButton("Встроить и сохранить");

        bottomPanel.add(new JLabel("Формат:"));
        bottomPanel.add(formatComboBox);
        bottomPanel.add(encodeAndSaveBtn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики событий
        selectFileBtn.addActionListener(e -> {
            File file = chooseImageFile();
            if (file != null) {
                selectedEncodeFile = file;
                encodeFileLabel.setText(file.getName());
            }
        });

        encodeAndSaveBtn.addActionListener(e -> performEncode());

        return panel;
    }

    private JPanel createDecodePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель: выбор файла и кнопка расшифровки
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        JButton selectFileBtn = new JButton("Выбрать изображение...");
        decodeFileLabel = new JLabel("Файл не выбран");
        JButton decodeBtn = new JButton("Извлечь текст");

        topPanel.add(selectFileBtn, BorderLayout.WEST);
        topPanel.add(decodeFileLabel, BorderLayout.CENTER);
        topPanel.add(decodeBtn, BorderLayout.EAST);

        // Центр: вывод результата
        outputMessageArea = new JTextArea();
        outputMessageArea.setEditable(false);
        outputMessageArea.setLineWrap(true);
        outputMessageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputMessageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Извлечённое сообщение"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Обработчики событий
        selectFileBtn.addActionListener(e -> {
            File file = chooseImageFile();
            if (file != null) {
                selectedDecodeFile = file;
                decodeFileLabel.setText(file.getName());
            }
        });

        decodeBtn.addActionListener(e -> performDecode());

        return panel;
    }

    private File chooseImageFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Изображения (PNG, BMP, JPG)", "png", "bmp", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private void performEncode() {
        if (selectedEncodeFile == null) {
            showError("Сначала выберите исходное изображение.");
            return;
        }
        String text = inputMessageArea.getText();
        if (text.isEmpty()) {
            showError("Введите текст для шифрования.");
            return;
        }

        String format = (String) formatComboBox.getSelectedItem();
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Сохранить изображение с текстом");
        saveChooser.setSelectedFile(new File("encoded_image." + format));

        if (saveChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outFile = saveChooser.getSelectedFile();
            // Добавляем расширение, если пользователь его стёр
            if (!outFile.getName().toLowerCase().endsWith("." + format)) {
                outFile = new File(outFile.getAbsolutePath() + "." + format);
            }

            try {
                BufferedImage img = imageProcessor.extractImage(selectedEncodeFile);
                byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
                
                imageProcessor.encodeLSB(img, textBytes);
                imageProcessor.saveImage(img, outFile, format);

                JOptionPane.showMessageDialog(this, "Изображение успешно сохранено!", "Готово", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void performDecode() {
        if (selectedDecodeFile == null) {
            showError("Выберите изображение для расшифровки.");
            return;
        }

        try {
            BufferedImage img = imageProcessor.extractImage(selectedDecodeFile);
            byte[] extractedBytes = imageProcessor.decodeLSB(img);
            String decodedText = new String(extractedBytes, StandardCharsets.UTF_8);

            outputMessageArea.setText(decodedText);
        } catch (Exception ex) {
            showError("Ошибка при извлечении: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new StegoApp().setVisible(true);
        });
    }
}