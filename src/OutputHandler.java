import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class OutputHandler {
    DataOutputStream out;
    OutputHandler(DataOutputStream out){
        this.out = out;
    }
    void sendPixel(Pixel pixel)
    {
        try {
            out.writeInt(1); //that is pixel message
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
            out.writeInt(0);
            out.flush();

        }catch(IOException ex) {
            ex.printStackTrace(); //for debugging only. remove it after development
        }
    }

}
