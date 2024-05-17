import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    App app = new App();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
