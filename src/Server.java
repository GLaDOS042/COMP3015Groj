import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
	private class Data
	{
		int[][] data;
		int sizeX;
		int sizeY;
		Data(int sizeX, int sizeY)
		{
			data = new int[sizeX][sizeY];
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}

	}

	ServerSocket serverSocket;
	ArrayList<Socket> list = new ArrayList();
	ArrayList<Data> data = new ArrayList();
	public Server() throws IOException {
		serverSocket = new ServerSocket(12345);
		data.add(new Data(50,50));
		Thread i = new Thread(() -> {
			try {
					System.out.println("start");
					String msg = "Finding Server";
					String reply = "Server Found";

					DatagramSocket socket = new DatagramSocket(5555);
					DatagramPacket packet;

					DatagramPacket receivedPacket = new DatagramPacket(new byte[1024], 1024);
					while(true) {

						socket.receive(receivedPacket);
						String content = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

						if (content.equals(msg)) {
							System.out.println("Server found");
							packet = new DatagramPacket(reply.getBytes(), reply.length(), receivedPacket.getAddress(), receivedPacket.getPort());
							socket.send(packet);
						}
					}
			} catch (IOException e) {
				System.err.println("System error: " + e.getMessage());
			}
		});
		i.start();
		while (true) {
			Socket clientSocket = serverSocket.accept();

			Thread t = new Thread(() -> {
				synchronized (list) {
					list.add(clientSocket);
				}
				try {
					serve(clientSocket);

				} catch (IOException ex) {
				}

				synchronized (list) {
					list.remove(clientSocket);
				}
			});
			t.start();
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new Server();
	}

	private void handleChatMessage(DataInputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);
		System.out.println(new String(buffer, 0, len));

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {

				try {
					Socket s = list.get(i);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(0); // send the message type
					out.writeInt(len);
					out.write(buffer, 0, len);
					out.flush();
				} catch (IOException ex) {
					System.out.println("The client is disconnected already");
				}
			}
		}
	}

	private void handlePixelMessage(DataInputStream in) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		data.get(0).data[x][y] = color;

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket s = list.get(i);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(1);
					out.writeInt(color);
					out.writeInt(x);
					out.writeInt(y);
					out.flush();
				} catch (IOException ex) {

				}
			}
		}
	}
	private void handleGroupPixelMessage(DataInputStream in) throws IOException {
		int color = in.readInt();
		int size = in.readInt();
		ArrayList<Integer> Xs = new ArrayList();
		ArrayList<Integer> Ys = new ArrayList();
		for (int i = 0; i < size; i++){
			int x = in.readInt();
			int y = in.readInt();
			Xs.add(x);
			Ys.add(y);
			data.get(0).data[x][y] = color;
		}

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket s = list.get(i);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(2);
					out.writeInt(color);
					out.writeInt(size);
					for (int j = 0; j < size; j++){
						out.writeInt(Xs.get(j));
						out.writeInt(Ys.get(j));
					}
					out.flush();
				} catch (IOException ex) {

				}
			}
		}
	}

	private void handleCopyRequest(Socket clientSocket) throws IOException {

		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			out.writeInt(42); // send the message type
			out.writeInt(data.get(0).sizeX);
			out.writeInt(data.get(0).sizeY);
			for (int i = 0; i < data.get(0).sizeX; i++) {
				for (int j = 0; j < data.get(0).sizeY; j++) {
					out.writeInt(data.get(0).data[i][j]);
				}
			}
			out.flush();
		} catch (IOException ex) {
			System.out.println("The client is disconnected already");
		}
	}

	private void handleLoadRequest(DataInputStream in) throws IOException
	{
		int sizeX = in.readInt();
		int sizeY = in.readInt();

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				data.get(0).data[i][j] = in.readInt();
			}
		}
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket s = list.get(i);
					handleCopyRequest(s);
				} catch (IOException ex) {

				}
			}
		}

	}


	public void serve(Socket clientSocket) throws IOException {
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());

		while (true) {
			int type = in.readInt(); // get the message type

			switch (type) {
			case 0: // chat message
				handleChatMessage(in);
				break;
			case 1: // pixel message
				handlePixelMessage(in);
				break;
			case 2:
				handleGroupPixelMessage(in);
				break;
			case 42:
				System.out.println("Copy request received");
				handleCopyRequest(clientSocket);
				break;
			case 99:
				handleLoadRequest(in);
			default:
				// TODO: something else???

			}

		}

	}

}
