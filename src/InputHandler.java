import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.io.File;

public class InputHandler {
    DataInputStream in;
    UI ui;

    InputHandler(DataInputStream in, UI ui) throws IOException {
        this.in = in;
        this.ui = ui;
    }

    void receive() {
        try {
            while (true) {
                int type = in.readInt();
                switch(type) {
                    case 0:
                        receiveChatMessage();
                        break;
                    case 1:
                        receivePixelMessage();
                        break;
                    case 2:
                        receiveGroupPixelMessage();
                        break;
                    case 42:
                        receiveCopy();
                        break;
                    default:
                        //TODO other things else?!
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    private void receiveChatMessage()throws IOException {
        // this is a chat message
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);

        String msg = new String(buffer, 0, len);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        msg = time + "\n" + msg;
        System.out.println(msg);

        ui.receiveChatMessage(msg);
    }

    private void receivePixelMessage() throws IOException{
        //TODO: we do it later!!
        int color = in.readInt();
        int x = in.readInt();
        int y = in.readInt();

        ui.receivePixelMessage(new Pixel(color,x,y));

    }

    private void receiveGroupPixelMessage() throws IOException{
        //TODO: we do it later!!
        int color = in.readInt();
        int size = in.readInt();
        for (int i = 0; i< size; i++)
        {
            int x = in.readInt();
            int y = in.readInt();

            ui.receivePixelMessage(new Pixel(color,x,y));
        }
    }

    private void receiveCopy() throws IOException{
        int sizeX = in.readInt();
        int sizeY = in.readInt();
        int[][] copy = new int[sizeX][sizeY];
        for (int y = 0; y< sizeY; y++)
        {
            for (int x = 0; x< sizeX; x++)
            {
                copy[x][y] = in.readInt();
            }
        }
        ui.data = copy;
        ui.refresh();
    }
    void load()
    {
        int[][] data = null;;
        int rows = 0;
        int cols = 0;
        try {
            // Create a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open File");

            // Show the dialog and get the user's selection
            int userSelection = fileChooser.showOpenDialog(new JFrame());

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File fileToOpen = fileChooser.getSelectedFile();

                // Create a data input stream to read from the file
                DataInputStream fi = new DataInputStream(new FileInputStream(fileToOpen));

                // Read the dimensions of the data
                rows = fi.readInt();
                cols = fi.readInt();

                // Create the data array
                data = new int[rows][cols];

                // Read the data from the file
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        data[i][j] = fi.readInt();
                    }
                }

                // Close the data input stream
                fi.close();

                System.out.println("File read successfully.");
            } else {
                System.out.println("Open operation cancelled by the user.");
            }
        } catch (IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
        if(rows != 0 && cols != 0)
        {
            ui.outputHandler.sendCopy(rows,cols,data);
        }

    }

}
