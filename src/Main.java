import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Main  extends JFrame {
	
	boolean cheat = false;
	
	static Main win = null;
	static String name = "";
	static String hostname = "Unknown";
	
	int boomcounter = 0;
	private int moveSpeed = 2;
	private int[] screenSizeXY = {400,715};
	private int[] carSizeXY = {20,46};
	private int[] bullestSizeXY = {5,5};
	private int[] gunSizeXY = {87, 27};
	private int trafficWeight = 500;
	private int raceSizePixels = 50000;
	private static int score = 0 ;
	static int sleep = 7;
	private static boolean debugEnabled = false;
	static boolean gameWon = false;
	static boolean gameLost = false;
	static boolean infoSent = false;
	private int numberOfImages = 5;
	private int numberOfGuns = 5;
	private int numberOfBullets = 10;
	
	public static int randInt(int min, int max) 
	{
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public Main()
	{
		setTitle("AcceleratorPlus 2");
		setSize(screenSizeXY[0], screenSizeXY[1]);
		setResizable(false);
		add(new sprit());
		
		if(debugEnabled){
			setResizable(true);
			trafficWeight = 10;
			raceSizePixels = 20000;
		}
	}
	
	class sprit extends JPanel
	{
		int bulletCount = 0;
		boolean haveGun = false;
		boolean isBulletActive = false;
	
		boolean[] keys = new boolean[KeyEvent.KEY_LAST];

		int carX=0, carY=screenSizeXY[1]-100, y=0, y2=-287;
		int killedcars = 0;
		//character middle point or radius
		int midpoint = 12;
		
		Image[] trafficPics = new Image[numberOfImages];
		Image[] boom = new Image[5];
		
		ImageIcon roadpic = new ImageIcon(Main.class.getResource("/resources/road.png"));
		Image road = roadpic.getImage();

		ImageIcon carpic = new ImageIcon(Main.class.getResource("/resources/car.png"));
		Image car = carpic.getImage();
		
		ImageIcon carFlashpic = new ImageIcon(Main.class.getResource("/resources/carFlash.gif"));
		Image carFlash = carFlashpic.getImage();
		
		ImageIcon gunPic = new ImageIcon(Main.class.getResource("/resources/gun.png"));
		Image gun = gunPic.getImage();
		
		ImageIcon youWinpic = new ImageIcon( Main.class.getResource("/resources/congrats.gif"));
		Image youWin = youWinpic.getImage();
		
		ImageIcon youLosepic = new ImageIcon( Main.class.getResource("/resources/youlose.gif"));
		Image youLose = youLosepic.getImage();
		
		Rectangle carRect = new Rectangle(carSizeXY[0], carSizeXY[1]);
		Rectangle bullectRect = new Rectangle(-10,-10,bullestSizeXY[0], bullestSizeXY[1]);
		
		Rectangle[] traffic = new Rectangle[trafficWeight];
		Rectangle[] guns = new Rectangle[numberOfGuns];
		
		
		public boolean isColliding(Rectangle[] vehicles, Rectangle currentVehicle)
		{
			for(int i=0;i<vehicles.length;i++)
			{
				if(vehicles[i]!=null && currentVehicle.intersects(vehicles[i]))
				{
					return true;
				}
			}
			return false;
		}
		
		public sprit()
		{
			URL urlp0 = Main.class.getResource("/resources/car0.png");
			ImageIcon pic0 = new ImageIcon(urlp0);
			trafficPics[0] = pic0.getImage();
			
			URL urlp1 = Main.class.getResource("/resources/car1.png");
			ImageIcon pic1 = new ImageIcon(urlp1);
			trafficPics[1] = pic1.getImage();
			
			URL urlp2 = Main.class.getResource("/resources/car2.png");
			ImageIcon pic2 = new ImageIcon(urlp2);
			trafficPics[2] = pic2.getImage();
			
			URL urlp3 = Main.class.getResource("/resources/car3.png");
			ImageIcon pic3 = new ImageIcon(urlp3);
			trafficPics[3] = pic3.getImage();
			
			URL urlp4 = Main.class.getResource("/resources/car4.png");
			ImageIcon pic4 = new ImageIcon(urlp4);
			trafficPics[4] = pic4.getImage();
				
			ImageIcon e1 = new ImageIcon(Main.class.getResource("/resources/e1.png"));
			boom[0] = e1.getImage();
			ImageIcon e2 = new ImageIcon(Main.class.getResource("/resources/e2.png"));
			boom[1] = e2.getImage();
			ImageIcon e3 = new ImageIcon(Main.class.getResource("/resources/e3.png"));
			boom[2] = e3.getImage();
			ImageIcon e4 = new ImageIcon(Main.class.getResource("/resources/e4.png"));
			boom[3] = e4.getImage();
			ImageIcon e5 = new ImageIcon(Main.class.getResource("/resources/e5.png"));
			boom[4] = e5.getImage();
			
			//initialize traffic
			for(int i=0;i<traffic.length;i++)
			{
				traffic[i] = new Rectangle(0, 0, carSizeXY[0], carSizeXY[1]);
			}
			
			//setting initial traffic position
			Rectangle[] temp = new Rectangle[traffic.length];
			Rectangle collChecker = new Rectangle(carSizeXY[0], carSizeXY[1]);
			for(int i=0;i<traffic.length;i++)
			{
				while(isColliding(temp,collChecker))
				{
					collChecker = new Rectangle(randInt(0, screenSizeXY[0]), randInt(0, raceSizePixels)*-1, carSizeXY[0], carSizeXY[1]);
				}
				temp[i] = collChecker;
			}
			traffic = temp;
			
			//setting initial gun position
			temp = new Rectangle[guns.length];
			collChecker = new Rectangle(gunSizeXY[0], gunSizeXY[1]);
			for(int i=0;i<guns.length;i++)
			{
				while(isColliding(temp,collChecker))
				{
					collChecker = new Rectangle(randInt(0, screenSizeXY[0]), randInt(0, raceSizePixels)*-1, carSizeXY[0], carSizeXY[1]);
				}
				temp[i] = collChecker;
			}
			guns = temp;
			
			//listening to keys
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0){
					keys[arg0.getKeyCode()]=true;
				}
				@Override
				public void keyReleased(KeyEvent arg0){
					keys[arg0.getKeyCode()]=false;
				}
			});
			setFocusable(true);
			setBackground(Color.black.brighter());
		}
		
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.setColor(Color.white);
			
			//draw road and score
			g.drawImage(road, 0, y, null);
			g.drawImage(road, 0, y2, null);
			
			//main object
			g.setColor(Color.red);
			
			if(!gameLost)
			{
				if(haveGun)
				{
					g.drawImage(carFlash, carX-4, carY-4, null);
				}
				else
				{
					g.drawImage(car, carX-4, carY-4, null);
				}
				carRect.setLocation(carX, carY);
			}
			
			if(debugEnabled)
			{
				g.fillRect((int)carRect.getX(), (int)carRect.getY(),(int)carRect.getWidth(), (int)carRect.getHeight());
				
				for(int i=0;i<traffic.length;i++)
				{
					if(traffic[i]!=null)
					{
						int x = (int) traffic[i].getX();
						int y = (int) traffic[i].getY();
						int w = (int) traffic[i].getWidth();
						int h = (int) traffic[i].getHeight();
						
						g.fillRect(x, y, w, h);
					}
				}
			}
			
			for(int i=0;i<traffic.length;i++)
			{
				if(traffic[i]!=null)
				{
					if(!gameLost && !gameWon)
					{
						traffic[i].setLocation((int)traffic[i].getX(), (int)(traffic[i].getY()+moveSpeed+1));
					}
					int x = (int) traffic[i].getX();
					int y = (int) traffic[i].getY();
					g.drawImage(trafficPics[i%numberOfImages], x-4, y-4, null);
					
					if(carRect.intersects(traffic[i]))
					{
						if(!cheat){gameLost = true;}
					}
					
					if(bullectRect!=null && isBulletActive && bullectRect.intersects(traffic[i]))
					{
						bullectRect = null;
						bulletCount--;
						isBulletActive = false;
						traffic[i]=null;
						killedcars++;
						score+=500;
					}
					
					if(traffic[i]!=null && traffic[i].getY()>screenSizeXY[1]+50)
					{
						traffic[i]=null;					
						killedcars++;
					}
				}
			}
			
			for(int i=0;i<guns.length;i++)
			{
				if(!gameLost && !gameWon)
				{
					guns[i].setLocation((int)guns[i].getX(), (int)(guns[i].getY()+moveSpeed));
				}
				int x = (int) guns[i].getX();
				int y = (int) guns[i].getY();
				
				if(debugEnabled){g.fillRect(x, y, gunSizeXY[0], gunSizeXY[1]);}
				
				if(carRect.intersects(guns[i]))
				{
					guns[i].setLocation(-500,0);
					haveGun = true;
					bulletCount += numberOfBullets;
				}
				else
				{
					g.drawImage(gun, x, y, null);
				}
			}
			
			if(bullectRect!=null)
			{
				bullectRect.setLocation((int)bullectRect.getX(), (int)bullectRect.getY()-4);
			}
			
			if(bullectRect!=null && bullectRect.getY()<0)
			{
				bullectRect = null;
				bulletCount--;
				isBulletActive = false;
			}
			
			if(bulletCount<=0)
			{
				haveGun=false;
			}
			
//			if(debugEnabled && bullectRect!=null){g.fillRect((int)bullectRect.getX(), (int)bullectRect.getY(), (int)bullectRect.getWidth(), (int)bullectRect.getHeight());}
			if(bullectRect!=null)
			{
				g.setColor(Color.yellow.darker());
				g.fillRect((int)bullectRect.getX(), (int)bullectRect.getY(), (int)bullectRect.getWidth(), (int)bullectRect.getHeight());
				g.setColor(Color.BLACK.darker());
				g.fillOval((int)bullectRect.getX(), (int)bullectRect.getY(), (int)bullectRect.getWidth(), (int)bullectRect.getHeight());
			}
			
			if(killedcars>=trafficWeight)
			{
				gameWon=true;
			}

			//Responding to key events
			if(keys[KeyEvent.VK_LEFT] && (carX>0) && !gameLost){
				carX-=moveSpeed;
			}
			
			if(keys[KeyEvent.VK_UP] && (carY>0) && !gameLost){
				carY-=moveSpeed;
			}
			
			if(keys[KeyEvent.VK_DOWN] && (carY<screenSizeXY[1]-carSizeXY[1]) && !gameLost){
				carY+=moveSpeed;
			}
			
			if(keys[KeyEvent.VK_RIGHT] && (carX<screenSizeXY[0]-carSizeXY[0]) && !gameLost){
				carX+=moveSpeed;
			}
			
			if(keys[KeyEvent.VK_SPACE] && !gameLost && haveGun && !isBulletActive)
			{
				bullectRect = new Rectangle((int)(carRect.getX()+4), carY, bullestSizeXY[0], bullestSizeXY[1]);
				isBulletActive = true;
			}
			
			if(keys[KeyEvent.VK_SHIFT] && keys[KeyEvent.VK_Y])
			{
				if(cheat)
				{
					cheat=false;
				}
				else
				{
					cheat=true;
				}
			}
			
			if(keys[KeyEvent.VK_W])
			{
				if(sleep>4)
				{
					sleep--;
				}
			}
			
			if(keys[KeyEvent.VK_D])
			{
				if(sleep<7)
				{
					sleep++;
				}
			}
			//handling road background
			if(!gameLost)
			{
				y+=2;
				y2+=2;
				if(y>287)
				{
					y=0;
					y2=-287;
				}
			}
			
			if(gameWon)
			{
				g.drawImage(youWin, 5, 20, null);
			}
			
			if(gameLost)
			{
				boomcounter++;
				g.drawImage(boom[boomcounter%5], carX-50, carY-75, null);
				g.drawImage(youLose, 50, 50, null);
			}
			
			g.setColor(Color.black);
			g.fillRect(0, 0, screenSizeXY[0], 14);
			String info = "Ammo: "+ bulletCount + "     Score: "+score;
			if(gameLost)
			{
				info = name + " You Lost :-(  >>>> " + info;
			}
			else if(gameWon)
			{
				info = name +" You Won :-)   >>>>> " + info;
			}		
			if(cheat)
			{
				info+= " GOD MODE";
			}
			g.setColor(Color.red);
			g.drawString(info, 10, 10);
		}
	}
	
	public static void main(String[] args){
		

		while((name.length()<3) || (name.length()>10))
		{
			name = JOptionPane.showInputDialog("UP,DOWN,LEFT,RIGHT - Arrow Keys \nSHOOT - SpaceBar \nACCELERATE - W, D \n\nPlease Enter Your Name:");
		}
		
		if(true)
		{
			win = new Main();
			win.setLocationRelativeTo(null);
			win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			win.setVisible(true);
			
			Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						while(true){
							try{
								win.repaint();
								Thread.sleep(sleep);
							}
							catch(InterruptedException e){if(debugEnabled){e.printStackTrace();}}
						}
					}
				});
			t.start();
			
			Thread z = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true){
						try{
							
							Thread.sleep(2000);
							if(!gameWon && !gameLost)
								score+=100;
								
//							if(sleep>5)
//								sleep--;
							
							if(gameLost)
							{
								sleep = 100;
							}
							
							if(gameLost || gameWon)
							{
								if(!infoSent)
								{
									if (JOptionPane.showConfirmDialog(null, "Your Score is "+score+"\n"+"Would you like to upload your score?", "Send Score",
									        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
									    SendScore();
									} 
									infoSent=true;
								}
							}
						}
						catch(InterruptedException e){if(debugEnabled){e.printStackTrace();}}
					}
				}
			});
			z.start();
		}
		
	}
	
	private static void SendScore()
	{
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		}
		catch (UnknownHostException e)	{if(debugEnabled){e.printStackTrace();}}
		
		try {
			String makeUrl = "http://ata.mygamesonline.org/send.php?name="+name+"&pcname="+hostname+"&score="+score+"&password=accele787&gamewon="+gameWon;
			System.out.println(makeUrl);
			URL url = new URL(makeUrl);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
		        InputStream is = conn.getInputStream();
		        if(debugEnabled){System.out.println(is.toString());}
		    }
		} 
		catch (MalformedURLException e) {if(debugEnabled){e.printStackTrace();}} 
		catch (IOException e) {if(debugEnabled){e.printStackTrace();}}
	}

}
