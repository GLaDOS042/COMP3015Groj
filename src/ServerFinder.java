import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;

public class ServerFinder {

    public ServerFinder(){

    }

    public String findAddress(){
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

                if (content.equals(reply))
                    return receivedPacket.getAddress().toString().replace("/","");
            }
        } catch (IOException e) {
            System.err.println("System error: " + e.getMessage());
        }
        return "";
    }



}
