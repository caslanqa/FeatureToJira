import Utilities.FileOperations;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class App extends JFrame {
    private JButton btnSubmit;
    private JTextField inptFilePath;
    private JLabel lblImageContainer;
    private JLabel lblCompany;
    private JLabel lblApp;
    private JButton btnClose;
    private JPanel wrapper;
    public List<String> files;

    public App(){
        Arrays.asList(UIManager.getInstalledLookAndFeels())
                .stream()
                .filter(info -> info.getName().equals("Mac OS X"))
                .forEach(info -> {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });

        ImageIcon imageIcon = new ImageIcon(getClass().getResource("pf.png"));
        Image image = imageIcon.getImage();
        Image newImage = image.getScaledInstance(100, 60, Image.SCALE_DEFAULT);
        imageIcon = new ImageIcon(newImage);
        lblImageContainer.setIcon(imageIcon);

        setContentPane(wrapper);

        setSize(600, 600);
        setTitle("Feature To XRay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int x = (Toolkit.getDefaultToolkit().getScreenSize().width - getSize().width) / 2;
        int y = (Toolkit.getDefaultToolkit().getScreenSize().height - getSize().height) / 2;

        setLocation(x, y);
        setVisible(true);

        btnClose.addActionListener(e -> {
            System.exit(0);
        });

        inptFilePath.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                inptFilePath.setText("");
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);

                // Show open dialog
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    StringBuilder filePaths = new StringBuilder();
                    for (File selectedFile : selectedFiles) {
                        filePaths.append(selectedFile.getAbsolutePath()).append("; "); // Add file paths
                    }
                    String filePathsString = filePaths.toString();
                    if (filePathsString.length() > 2) {
                        filePathsString = filePathsString.substring(0, filePathsString.length() - 2); // Remove the last "; "
                    }
                    inptFilePath.setText(filePathsString);
                }
            }
        });
        btnSubmit.addActionListener(e -> {
            boolean hasError = false;
            String filePaths = inptFilePath.getText();

            files = Arrays.asList(filePaths.split("; "));

            for (String file : files) {
                if (!file.endsWith(".feature")) {
                    JOptionPane.showConfirmDialog(null, "Please Select feature file", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                    hasError = true;
                }
            }

            if (!hasError) {

                /*FileOperations.multiplyFeatureFiles(files);

                for (int i = 1; i <= files.size() ; i++) {
                    FileOperations.compressFeatures(i);
                }

                JOptionPane.showConfirmDialog(null, "Feature zip files created successfully", "Success", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);*/

                FileOperations.authToken = FileOperations.getAuthToken();

                for (int i = 1; i <= files.size() ; i++) {
                    try {
                        Thread.sleep(2000);
                        if (FileOperations.uploadZipFile(i)) {
                            JOptionPane.showConfirmDialog(null, "Feature zip files uploaded", "Success", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ignored) {
                    }
                }

                System.exit(0);
            }
        });
    }
}
