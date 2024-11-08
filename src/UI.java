import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import javax.swing.border.LineBorder;


enum PaintMode {Pixel, Area};

public class UI extends JFrame {
	String UserName;
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	InputHandler inputHandler;
	OutputHandler outputHandler;

	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;

	private JToggleButton save;
	private JToggleButton load;

	private static UI instance;
	private int selectedColor = -543230; // golden

	int[][] data = new int[50][50]; // pixel color data array
	int blockSize = 16;
	PaintMode paintMode = PaintMode.Pixel;

	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static UI getInstance() throws IOException {

		if (instance == null)
			instance = new UI();

		return instance;
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 */


	void receiveChatMessage(String msg){
		SwingUtilities.invokeLater(() -> {
			chatArea.append(msg + "\n");
		});
	}

	void receivePixelMessage(Pixel p){
		paintPixel(p.color,p.x,p.y);
	}

	private UI() throws IOException {
		UserName = (new NameAsker().Ask());
		ServerFinder finder = new ServerFinder();
		String ip = finder.findAddress();
		if (ip.equals(""))
		{
			System.out.println("No server found");
			System.exit(0);
		}
		socket = new Socket(ip, 12345);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		inputHandler = new InputHandler(in,this);
		outputHandler = new OutputHandler(out);
		Thread t = new Thread(() -> {
			inputHandler.receive();
		});
		Thread i = new Thread(() -> {
			outputHandler.requestCopy();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});

		t.start();
		i.start();
		setTitle("KidPaint");

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));

		paintPanel = new JPanel() {

			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

				// enable anti-aliasing
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHints(rh);

				// clear the paint panel using black
				g2.setColor(Color.black);
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());

				// draw and fill circles with the specific colors stored in the data array
				for (int x = 0; x < data.length; x++) {
					for (int y = 0; y < data[0].length; y++) {
						g2.setColor(new Color(data[x][y]));
						g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						g2.setColor(Color.darkGray);
						g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					}
				}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0)
					outputHandler.sendGroupPixel(
							selectedColor,paintArea(e.getX() / blockSize, e.getY() / blockSize));
				else if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
				{
					Pixel p = new Pixel(selectedColor,e.getX()/blockSize,e.getY()/blockSize);
					outputHandler.sendPixel(p);
				}

			}
		});

		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
				{
					Pixel p = new Pixel(selectedColor,e.getX()/blockSize,e.getY()/blockSize);
					outputHandler.sendPixel(p);
				}

			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		toolPanel.add(pnlColorPicker);

		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);

		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		save = new JToggleButton("Save");
		toolPanel.add(save);
		load = new JToggleButton("load");
		toolPanel.add(load);
		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				paintMode = PaintMode.Area;
			}
		});

		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				outputHandler.save(data);
			}
		});
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				inputHandler.load();
			}
		});

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}

		});

		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

		this.setSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(() -> {
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text - user inputted text
	 */
	private void onTextInputted(String text) {
//		chatArea.setText(chatArea.getText() + text + "\n");
		outputHandler.sendMessage(UserName+": "+text);
	}

	/**
	 * change the color of a specific pixel
	 * 
	 * @param col, row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		paintPixel(selectedColor,col,row);
	}
	
	public void paintPixel(int color,int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = color;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}
	/**
	 * change the color of a specific area
	 * 
	 * @param col, row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int[][] temp = new int[this.data.length][];
		for (int i = 0; i < this.data.length; i++) {
			temp[i] = this.data[i].clone();
		}
		int oriColor = temp[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (temp[x][y] != oriColor)
					continue;

				temp[x][y] = selectedColor;
				filledPixels.add(p);

				if (x > 0 && temp[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < temp.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && temp[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < temp[0].length - 1 && temp[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	public void refresh() {
		paintPanel.repaint();
	}

	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		outputHandler.requestCopy();
		paintPanel.repaint();
	}
}
