import javax.swing.*;
public class NameAsker {
    NameAsker()
    {

    }
    String Ask()
    {
        JFrame frame = new JFrame("What's your name?");
        String name = JOptionPane.showInputDialog(frame, "What's your name?");
        return name;
    }

}
