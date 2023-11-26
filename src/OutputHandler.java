import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class OutputHandler {
    DataOutputStream out;
    int groupNo = Integer.MIN_VALUE;
    OutputHandler(DataOutputStream out){
        this.out = out;
    }
    void sendPixel(Pixel pixel)
    {
        try {
            out.writeInt(1); //that is pixel message
            out.writeInt(groupNo);
            out.writeInt(pixel.color);
            out.writeInt(pixel.x);
            out.writeInt(pixel.y);
            out.flush();

        }catch(IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }

    void sendMessage(String text)
    {
        try {
            out.writeInt(0); //0 means that is chat message
            out.writeInt(groupNo);
            out.writeInt(text.length());
            out.write(text.getBytes());
            out.flush();

        }catch(IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }

    void sendGroupPixel(int color, List<Point> pixels)
    {
        try {
            out.writeInt(2); //that is pixel message
            out.writeInt(groupNo);
            out.writeInt(color);
            out.writeInt(pixels.size());
            for (Point pixel : pixels)
            {
                out.writeInt(pixel.x);
                out.writeInt(pixel.y);
            }
            out.flush();

        }catch(IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }

    void requestCopy()
    {
        try {
            out.writeInt(42); //0 means that is chat message
            out.writeInt(groupNo);
            out.flush();

        }catch(IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }


    void save(int[][] data) {
        try {
            // Create a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save File");

            // Show the dialog and get the user's selection
            int userSelection = fileChooser.showSaveDialog(new JFrame());

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File fileToSave = fileChooser.getSelectedFile();

                // Create a data output stream to write to the file
                DataOutputStream fo = new DataOutputStream(new FileOutputStream(fileToSave));

                // Write the data to the file
                fo.writeInt(data.length);
                fo.writeInt(data[0].length);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[0].length; j++) {
                        fo.writeInt(data[i][j]);
                    }
                }

                // Close the data output stream
                fo.close();

                System.out.println("File saved successfully.");
            } else {
                System.out.println("Save operation cancelled by the user.");
            }
        } catch (IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }

    void sendCopy(int row, int col,int[][] data)
    {
        try {
            out.writeInt(99); // send the message type
            out.writeInt(groupNo);
            out.writeInt(row);
            out.writeInt(col);
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    out.writeInt(data[i][j]);
                }
            }
            out.flush();
        } catch (IOException ex) {
            System.out.println("The client is disconnected already");
        }
    }
    void getStudioList()
    {
        try {
            out.writeInt(88); // send the message type
            out.writeInt(Integer.MIN_VALUE);
            out.flush();
        } catch (IOException ex) {
            System.out.println("The client is disconnected already");
        }
    }

    void createStudio(int sizeX, int sizeY, String name)
    {
        try {
            out.writeInt(152); // send the message type
            out.writeInt(Integer.MIN_VALUE);
            out.writeInt(sizeX);
            out.writeInt(sizeY);
            out.writeInt(name.length());
            out.write(name.getBytes());
            out.flush();
        } catch (IOException ex) {
            System.out.println("The client is disconnected already");
        }
    }
    void joinStudio(int StudioNum)
    {   groupNo = StudioNum;
        try {
            out.writeInt(3); // send the message type
            out.writeInt(StudioNum);
        } catch (IOException ex) {
            System.out.println("The client is disconnected already");
        }
    }

    void Undo(int x, int y, PixelRecord pixelRecord)
    {
        try {
            out.writeInt(4); // send the message type
            out.writeInt(groupNo);
            out.writeInt(x);
            out.writeInt(y);
            out.writeLong(pixelRecord.userEditTime);
            out.writeInt(pixelRecord.color);
            out.writeLong(pixelRecord.lastEditTime);
            out.flush();
        } catch (IOException ex) {
            System.out.println("The client is disconnected already");
        }
    }


}
