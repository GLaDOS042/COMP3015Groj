import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;

public class ServerFinder {

    public ServerFinder(){

    }

    public String[] findAddress(){
        String[] result = new String[2];
        try{

            String msg = "Finding Server";
            String reply = "Server Found";

            DatagramSocket socket = new DatagramSocket(5555);
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), InetAddress.getByName("255.255.255.255"), 5555);
            socket.send(packet);
            DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
            while(true) {
                socket.receive(receivedPacket);
                String content = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                if (content.contains(reply)) {
                    result[0] = receivedPacket.getAddress().toString().replace("/", "");
                    result[1] = content.replace(reply, "");
                }
            }
        } catch (IOException e) {
            if (e.getMessage().equals("Address already in use (Bind failed)")){
                result[0] = "127.0.0.1";
                result[1] = "12345";
            }
            System.err.println("System error: " + e.getMessage());
        }
        return result;
    }



}
