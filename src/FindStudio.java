import javax.swing.*;

public class FindStudio extends JFrame {
    private String[] StudioName = {"a","b","c", "d"};
    UI ui;

    public FindStudio(UI ui) {
        this.ui = ui;
        ui.outputHandler.getStudioList();
    }



    void getStudioNum() {
        StudioName = ui.StudioName;
        String[] options = new String[StudioName.length + 2];
        options[0] = "Create";
        for (int i = 0; i < StudioName.length; i++) {
            options[i + 1] = StudioName[i];
        }
        options[options.length - 1] = "Cancel";

        int selectedOption = JOptionPane.showOptionDialog(null, "Select a studio", "Studio Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selectedOption == 0) {
            int canvasSizeX = Integer.parseInt(JOptionPane.showInputDialog("Enter the canvas size (x):"));
            int canvasSizeY = Integer.parseInt(JOptionPane.showInputDialog("Enter the canvas size (y):"));
            String name = (JOptionPane.showInputDialog("Enter the studio name:"));
            ui.outputHandler.createStudio(canvasSizeX, canvasSizeY, name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (selectedOption > 0 && selectedOption <= StudioName.length) {
            ui.outputHandler.joinStudio(selectedOption-1);
        } else {
            System.exit(0);
        }
    }

}
