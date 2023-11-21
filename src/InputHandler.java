import java.io.DataInputStream;
import java.io.IOException;

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
                    default:
                        //TODO other things else?!
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            receive();
        }
    }



    private void receiveChatMessage()throws IOException {
        // this is a chat message
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);

        String msg = new String(buffer, 0, len);
        System.out.println(msg);

        ui.receiveChatMessage(msg);
    }

    private void receivePixelMessage() throws IOException{
        //TODO: we do it later!!
        int color = in.readInt();
        int x = in.readInt();
        int y = in.readInt();

        System.out.printf("%d-(%d,%d)\n",color,x,y);

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

            System.out.printf("%d-(%d,%d)\n",color,x,y);
            ui.receivePixelMessage(new Pixel(color,x,y));
        }
    }
}
