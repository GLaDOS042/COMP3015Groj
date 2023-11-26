import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;


public class Server {
	private class Data
	{
		int[][] data;
		Long[][] time;
		int sizeX;
		int sizeY;
		Data(int sizeX, int sizeY)
		{
			data = new int[sizeX][sizeY];
			time = new Long[sizeX][sizeY];
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}
		private Long edit(int x, int y, int color)
		{
			set(x,y,color,new Date().getTime());
			time[x][y] = time[x][y];
			return time[x][y];
		}
		private void set(int x, int y, int color, Long time)
		{
			data[x][y] = color;
			this.time[x][y] = time;
		}

		private int getColor(int x, int y)
		{
			return data[x][y];
		}
		private Long getTime(int x, int y)
		{
			return time[x][y];
		}

	}

	ServerSocket serverSocket;
	ArrayList<Socket> list = new ArrayList();
	ArrayList<ArrayList<Socket>> userGroup = new ArrayList();
	ArrayList<Data> data = new ArrayList();
	ArrayList<String> studioName = new ArrayList();
	public Server() throws IOException {
		serverSocket = new ServerSocket(12345);
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
							int port =  serverSocket.getLocalPort();
							packet = new DatagramPacket((reply+port).getBytes(), (reply+port).length(), receivedPacket.getAddress(), receivedPacket.getPort());
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

	private void handleChatMessage(DataInputStream in, int Group) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);
		System.out.println(new String(buffer, 0, len));

		synchronized (userGroup.get(Group)) {
			for (int i = 0; i < userGroup.get(Group).size(); i++) {

				try {
					Socket s = userGroup.get(Group).get(i);
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

	private void handlePixelMessage(DataInputStream in , int Group, Socket clientSocket) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		if (x < 0 || x >= data.get(Group).sizeX || y < 0 || y >= data.get(Group).sizeY) {
			return;
		}
		Long lastEditTime = data.get(Group).getTime(x,y);
		int oldColor = data.get(Group).getColor(x,y);
		data.get(Group).edit(x,y,color);
		System.out.println("Pixel: "+x+" "+y+" "+lastEditTime+" "+ data.get(Group).getTime(x,y));
		sendPixelHistory(Group,x,y,oldColor,lastEditTime,clientSocket);
		synchronized (userGroup.get(Group)) {
			for (int i = 0; i < userGroup.get(Group).size(); i++) {
				try {
					Socket s = userGroup.get(Group).get(i);
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
	private void handleGroupPixelMessage(DataInputStream in, int Group, Socket clientSocket) throws IOException {
		int color = in.readInt();
		int size = in.readInt();
		ArrayList<Integer> Xs = new ArrayList();
		ArrayList<Integer> Ys = new ArrayList();
		for (int i = 0; i < size; i++){
			int x = in.readInt();
			int y = in.readInt();
			Xs.add(x);
			Ys.add(y);
			Long lastEditTime = data.get(Group).getTime(x,y);
			int oldColor = data.get(Group).getColor(x,y);
			data.get(Group).edit(x,y,color);
			sendPixelHistory(Group,x,y,oldColor,lastEditTime,clientSocket);
		}

		synchronized (userGroup.get(Group)) {
			for (int i = 0; i < userGroup.get(Group).size(); i++) {
				try {
					Socket s = userGroup.get(Group).get(i);
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

	private void handleCopyRequest(Socket clientSocket, int Group) throws IOException {

		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			out.writeInt(42); // send the message type
			out.writeInt(data.get(Group).sizeX);
			out.writeInt(data.get(Group).sizeY);
			for (int i = 0; i < data.get(Group).sizeX; i++) {
				for (int j = 0; j < data.get(Group).sizeY; j++) {
					out.writeInt(data.get(Group).data[i][j]);
				}
			}

			out.flush();
		} catch (IOException ex) {
			System.out.println("The client is disconnected already");
		}
	}

	private void handleLoadRequest(DataInputStream in, int Group, Socket clientSocket) throws IOException
	{
		int sizeX = in.readInt();
		int sizeY = in.readInt();
		Long current= new Date().getTime();
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				Long lastEditTime = data.get(Group).getTime(i,j);
				int oldColor = data.get(Group).getColor(i,j);
				data.get(Group).set(i, j, in.readInt(), current);
				sendPixelHistory(Group,i,j,oldColor,lastEditTime,clientSocket);
			}
		}
		synchronized (userGroup.get(Group)) {
			for (int i = 0; i < userGroup.get(Group).size(); i++) {
				try {
					Socket s = userGroup.get(Group).get(i);
					handleCopyRequest(s, Group);
				} catch (IOException ex) {

				}
			}
		}

	}

	private void listStudio(Socket clientSocket)
	{
		int StudioNum = data.size();
		try {
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			out.writeInt(88); // send the message type
			out.writeInt(StudioNum);
			System.out.println("StudioNum: "+StudioNum);
			for (int i = 0; i < StudioNum; i++) {
				String text = studioName.get(i);
				out.writeInt(text.length());
				out.write(text.getBytes());
				//img of studio
			}
			out.flush();
		} catch (IOException ex) {
			System.out.println("The client is disconnected already");
		}
	}
	private void createStudio(DataInputStream in, Socket clientSocket)
	{
		byte[] buffer = new byte[1024];
		try {
			int row = in.readInt();
			int col = in.readInt();
			int len = in.readInt();
			in.read(buffer, 0, len);
			String name = new String(buffer, 0, len);
			System.out.println("Studio name: "+name+" is created");
			data.add(new Data(row,col));
			studioName.add(name);
			userGroup.add(new ArrayList());
			userGroup.get(data.size()-1).add(clientSocket);
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			out.writeInt(152); // send the message type
			out.writeInt(data.size()-1);
			out.flush();
		} catch (IOException ex) {
			System.out.println("The client is disconnected already");
		}

	}
	private void joinStudio(Socket clientSocket, int StudioNum) throws IOException
	{
		userGroup.get(StudioNum).add(clientSocket);
	}
	private void sendPixelHistory(int Group,int x, int y, int color, Long lastEditTime,Socket clientSocket) throws IOException
	{
		Long time = data.get(Group).getTime(x,y);
		if (lastEditTime == null)
		{
			lastEditTime = (long)0;
		}

		{
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			out.writeInt(4);
			out.writeInt(x);
			out.writeInt(y);
			out.writeLong(time);
			out.writeInt(color);
			out.writeLong(lastEditTime);
			out.flush();
		}
	}
	private void handleUndoPixelMessage(DataInputStream in, int Group) throws IOException
	{
		int x = in.readInt();
		int y = in.readInt();
		Long userEditTime = in.readLong();
		int color = in.readInt();
		Long lastEditTime =in.readLong();
		System.out.println("Undo: "+x+" "+y+" "+userEditTime+" "+lastEditTime+" "+color);
		System.out.println(data.get(Group).getTime(x,y));
		if (lastEditTime == 0)
		{
			lastEditTime = null;
		}
		System.out.println(data.get(Group).getTime(x,y)+" "+userEditTime);
		System.out.println((data.get(Group).getTime(x,y).compareTo(userEditTime) == 0));
		if ((data.get(Group).getTime(x,y).compareTo(userEditTime) == 0))
		{
			data.get(Group).set(x,y,color,lastEditTime);
			synchronized (userGroup.get(Group)) {
				for (int i = 0; i < userGroup.get(Group).size(); i++) {
					try {
						Socket s = userGroup.get(Group).get(i);
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
	}

	public void serve(Socket clientSocket) throws IOException {
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());

		while (true) {
			int type = in.readInt(); // get the message type
			int group = in.readInt();
			switch (type) {
			case 0: // chat message
				handleChatMessage(in, group);
				break;
			case 1: // pixel message
				handlePixelMessage(in, group,clientSocket);
				break;
			case 2:
				handleGroupPixelMessage(in, group,clientSocket);
				break;
			case 3:
				joinStudio(clientSocket,group);
				break;
			case 4:
				System.out.println("Undo request received");
				handleUndoPixelMessage(in, group);
				break;
			case 42:
				System.out.println("Copy request received");
				handleCopyRequest(clientSocket, group);
				break;
			case 88:
				listStudio(clientSocket);
				break;
			case 99:
				handleLoadRequest(in, group, clientSocket);
				break;
			case 152:
				createStudio(in, clientSocket);
			default:
				// TODO: something else???

			}

		}

	}

}
