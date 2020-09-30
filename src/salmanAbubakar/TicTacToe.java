package salmanAbubakar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TicTacToe implements Runnable {

	public String ip = "localhost";  /////ip localhost  by default///
	public int port = 33333;			//setted port to 33333
	public Scanner scanner = new Scanner(System.in); /// for handling the input we have imported the scanner
	public JFrame frame;
	public final int WIDTH = 506;
	public final int HEIGHT = 527;
	public Thread thread; 		///threads are used to perform the multiple task at the same time
								/// here we we will have communication among the different clients so we are using threads//

	public Painter painter;
	public Socket socket; //// A socket is an endpoint for communication between two machines/// 
	public DataOutputStream d; /// data sent from and to is read by this ///
								
	public DataInputStream t;		 /// data sent from and to is read by this ///

	public ServerSocket serverSocket; //This class implements server sockets. A server socket waits for requests to come in over the network. It performs some operation 
									//based on that request, and then possibly returns a result to the requester. 
									//perform operations on the basis of request

	public BufferedImage board; 		////load images in to buffer // buffer is used for loading images//
	public BufferedImage redX;
	public  BufferedImage blueX;
	public  BufferedImage redCircle;
	public  BufferedImage blueCircle;

	public  String[] spaces = new String[9];	///there are 9 spaces on the board//which means 9 squares////

	public  boolean Turn = false;
	public  boolean circle = true;
	public  boolean accepted = false;
	public  boolean unable = false;
	public  boolean won = false;
	public  boolean enemyWon = false;
	public  boolean tie = false;

	public  int lengthOfSpace = 160;  /// all the squares on the board are approx.. equal to 160//
	public  int errors = 0;
	public  int oneSpot = -1;
	public  int twoSpot = -1;
	// the lines which are shown on the winning of players cross line//
	public  Font font = new Font("Verdana", Font.BOLD, 32); 	//cross line///
	public  Font smallerFont = new Font("Verdana", Font.BOLD, 20);  //cross lne
	public  Font largerFont = new Font("Verdana", Font.BOLD, 50);	//cross line//

	public  String waitString = "Waiting for another player";
	public  String unableString = "We can't Communicate.";
	public  String wonString = "You won!";
	public  String opWonString = "Opponent won!";
	public  String tieString = "Game ended in a tie.";
	//// back-end board setup/// these are the all mentioned wining condition for both players//
	public  int[][] wins = new int[][] {
		/** if i say this is my board
		 * 0 1 2
		 * 3 4 5
		 * 6 7 8
		 * 
		 */
		
	//all of these are winning combinations in 3 by 3array that can be possible
		{ 0, 1, 2 }, /// first line wining condition for both 
		{ 3, 4, 5 },	/// second winning condition for both 
		{ 6, 7, 8 },	// 3rd .......
		{ 0, 3, 6 },	// 4th.....
		{ 1, 4, 7 }, 	///5th .....
		{ 2, 5, 8 },	//6th...
		{ 0, 4, 8 }, 	///7th,,,
		{ 2, 4, 6 } 	///8th...
		};

	
	///constructor of class/// Having all the ports and ip ///by default setting ip which is local host///
	public TicTacToe() {
		System.out.println("Please Enter the IP: ");
		ip = scanner.nextLine();
		System.out.println("Please Enter the port: ");
		port = scanner.nextInt(); // take input and mov to next line
		while (port < 1 || port > 65535) {	///The maximum value is 65535. The TCP/IP network port number, it means i can set any port which 
					//less than 65535
			System.out.println("The port you entered was invalid, please input another port: ");
			port = scanner.nextInt(); //if wrong port is entered//
		}
////for loading images which are stored in buffer
	loadImages();

		painter = new Painter();///down there we have a painter class which is mentioned below which
	///which resides over the mouser functionality /// this painter class will load our board
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT)); // this will be settled on the basis of height and width
		/// calculations show us that the size settled by painter class will be(506 ,527)= 1033

		if (!connect()) initializeServer();
////things which will be shown on the board///
		frame = new JFrame();
		frame.setTitle("Tic-Tac-Toe");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);

		thread = new Thread(this, "TicTacToe");		////Allocates a new Thread object. This constructor has the same effect
													// the object whose run method is invoked when this threadis started. If null, this thread's run method is invoked
		thread.start();
	}
	///this method is required when you made a class implements runnable 
	public void run() {
		while (true) {
			tick();
			painter.repaint();
			//if there is no server listen to check whether somebody is connecting
			if (!circle && !accepted) {
				ServerRequest();
			}

		}
	}

	public  void render(Graphics g) {
		g.drawImage(board, 0, 0, null);  // board drawing will start here all set to zero because connection has not created yet//
		/*
		 * X,Y coordinate board drawing								/// no image is loaded//
		 */
								
										
		if (unable) {
			g.setColor(Color.BLACK);
			g.setFont(smallerFont);
			Graphics2D g2 = (Graphics2D) g;
			/// this is pixelation in the text///show on the board///
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(unableString); /// check length of string
			// then draw in the centre
			g.drawString(unableString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			return;
		}

		if (accepted) {
			for (int i = 0; i < spaces.length; i++) {
				if (spaces[i] != null) {
					if (spaces[i].equals("X")) {
						if (circle) {
							///Compares this string to the specified object. The result is true if and only if the argument is not null and is a String object 
							///that represents the same sequence of characters as thisobject. 
										//	(5%3)=2 *160 +10 =330 *2 =660
							g.drawImage(redX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(blueX, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					} else if (spaces[i].equals("O")) {
						if (circle) {
							g.drawImage(blueCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						} else {
							g.drawImage(redCircle, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
						}
					}
				}
			}
			if (won || enemyWon) {
				///draw winning line on the screen when any of one them won///
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(10));
				g.setColor(Color.BLACK);
				g.drawLine(oneSpot % 3 * lengthOfSpace + 10 *oneSpot % 3 + lengthOfSpace / 2, (int) (oneSpot / 3) * lengthOfSpace + 10 * (int) (oneSpot / 3) + lengthOfSpace / 2, twoSpot % 3 * lengthOfSpace + 10 * twoSpot % 3 + lengthOfSpace / 2, (int) (twoSpot / 3) * lengthOfSpace + 10 * (int) (twoSpot / 3) + lengthOfSpace / 2);

				g.setColor(Color.RED);
				g.setFont(largerFont);
				if (won) {
					int stringWidth = g2.getFontMetrics().stringWidth(wonString);
					g.drawString(wonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				} else if (enemyWon) {
					int stringWidth = g2.getFontMetrics().stringWidth(opWonString);
					g.drawString(opWonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
				}
			}
			if (tie) {
				Graphics2D g2 = (Graphics2D) g;
				g.setColor(Color.BLACK);
				g.setFont(largerFont);
				int stringWidth = g2.getFontMetrics().stringWidth(tieString);
				g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			}
		} else {
			g.setColor(Color.RED);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(waitString);
			g.drawString(waitString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}

	}

	public  void tick() {
		if (errors >= 10) 
			unable = true;
			//execptional conditions are settled here///
		if (!Turn && !unable) {
			try {// if noting above is full filled then it will try to get some data in tcp manner
				// because data is send and received
				int space = t.readInt();
				
				// check for winning condition if all the spaces are filled with the X then X player will win 
				// if spaces are filled with O the opponent will be winners
				if (circle) spaces[space] = "X";
				else spaces[space] = "O";
				EnemyWin(); 	/// check for tie and check for winner//
				
				ForTie();
				Turn = true;	// if all of these conditions are not filled then turns will be chaning continously untill the 
									// any player from both sides get wins
			} catch (IOException e) {
				e.printStackTrace();
				errors++;
			}
		}
	}

	public void checkForWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					oneSpot = wins[i][0];
					twoSpot = wins[i][2];
					won = true;
				}
			} else {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					oneSpot = wins[i][0];
					twoSpot = wins[i][2];
					won = true;
				}
			}
		}
	}

	public void EnemyWin() {
		for (int i = 0; i < wins.length; i++) {
			if (circle) {
				if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X") {
					oneSpot = wins[i][0];
					twoSpot = wins[i][2];
					enemyWon = true;
				}
			} else {
				if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O") {
					oneSpot = wins[i][0];
					twoSpot = wins[i][2];
					enemyWon = true;
				}
			}
		}
	}

	public void ForTie() {
		for (int i = 0; i < spaces.length; i++) {
			if (spaces[i] == null) {
				return;
			}
		}
		tie = true;
	}

	public void ServerRequest() {
		Socket socket = null;
		try {
			/*Listens for a connection to be made to this socket and acceptsit. The method blocks until a connection is made. 
			 * Creates a new data output stream to write data to the specifiedunderlying output stream. 
			 * The counter written isset to zero
			 * Returns an input stream for this socket
			 *Prints this throwable and its backtrace to thestandard error stream. 
			 *This method prints a stack trace for this Throwable object on the error output stream 
			 *that isthe value of the field System.err 
			 */
			socket = serverSocket.accept();
			d = new DataOutputStream(socket.getOutputStream());
			t = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("CLIENT HAS REQUESTED TO JOIN, AND WE HAVE ACCEPTED");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean connect() {
		try {
			/*after connection bw two servers the socket connection will be made through these two d 
			 * and dis which is used for the communication of two end points data sent from one point will be recived from
			 * the other point and so on
			 */
			socket = new Socket(ip, port);
			d = new DataOutputStream(socket.getOutputStream());
			t = new DataInputStream(socket.getInputStream());
			accepted = true;
			
			/*any exception occured so it will try to connect again
			 */
		} catch (IOException e) {
			System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
			return false;
		}
		System.out.println("Successfully connected to the server.");
		return true;
	}

		public void initializeServer() {
		try {
			serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Turn = true;
		circle = false;
	}
		/// load all the images on the pointer board this is the 
		// built in functionalities of Jframe//
	public void loadImages() {
		try {
			board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
			redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
			redCircle = ImageIO.read(getClass().getResourceAsStream("/redCircle.png"));
			blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
			blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueCircle.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		TicTacToe ticTacToe = new TicTacToe();
	}

	public class Painter extends JPanel implements MouseListener {
		public static final long serialVersionUID = 1L;

		public Painter() {
			/*making mouse focused
			 * requesting for mouse focusing on board
			 * settled back ground color
			 * finding mouse
			 */ 
			setFocusable(true);
			requestFocus();
			setBackground(Color.YELLOW);
			addMouseListener(this);
		}

		@Override
		// this method is built in and it is inside over JPanel//
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (accepted) {
				if (Turn && !unable && !won && !enemyWon) {
					int x = e.getX() / lengthOfSpace;
					int y = e.getY() / lengthOfSpace;
					y *= 3;
					int position = x + y;

					if (spaces[position] == null) {
						if (!circle) spaces[position] = "X";
						else spaces[position] = "O";
						Turn = false;
						repaint();
						Toolkit.getDefaultToolkit().sync();

						try {
							d.writeInt(position);
							d.flush();
						} catch (IOException e1) {
							errors++;
							e1.printStackTrace();
						}

						System.out.println("DATA WAS SENT");
						checkForWin();
						ForTie();

					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

	}

}
