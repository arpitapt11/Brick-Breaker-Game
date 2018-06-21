package com.BrickBreaker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

/* Class for 'Ball' object */

class Ball {
    public double x, y;    // 2d position coordinates of the ball
    public double dx, dy;  /* x and y components of velocity
                              both the components are kept equal 
                              through out the game (to achieve a 45 degree 
                              angle of impact) */
    public double radius;  /* radius of the Ball (kept constant 
			      through out the game) */
    public Ball(double x, double y, double dx, double dy, double radius) {
	this.x = x;
	this.y = y;
	this.dx = dx;
	this.dy = dy;
	this.radius = radius;
    }
}

/* Class to denote a 'Player' block */

class Player {
    public double x, y;  // 2d coordinates of the player for position
    public double width, height; // width and height of the player
    public Player(double x, double y, double width, double height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }
}

/* Class to denote a brick */

class Brick {
    public double x, y; // 2d coordinates denoting postion of the brick
    public static final double width = 100, height = 30; //width and heights
    public boolean isRendered; // tells whether to render the brick on the screen or not
    public Rectangle2D br;  // The rectangle object that will be drawn on the JComponent
    
    public Brick(double x, double y) {
	this.x = x;
	this.y = y;
	isRendered = true;
	br = new Rectangle2D.Double(x, y, width, height);
    }
}

/* Class to denote the space in which the game occurs */

class World extends JComponent {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4488936313240904289L;
	private Ball ball;  // A Object of type ball
    private Player player; // Object denoting player
    private Brick[][] bricks; // A 2d array of bricks
    private Ellipse2D e; // graphics object for Ball
    private Rectangle2D c; // Graphics object for Player
    private int score = 0; // Game score
    public boolean isGameOver = false; // Denotes whether the game is over or not
    private int lives = 3; // Number of lives
    private double impact_distance; /* distance from point of impact 
				       to the center of the Player block */
    private Font f1, f2;  /* Font objects for changing font styles */
    private TextLayout t1, t2, t3; /* Text layouts */
    private FontRenderContext frc;
    private Preferences prefs; // Top score preferences
    public World() {
	e = new Ellipse2D.Double();
	c = new Rectangle2D.Double();
	prefs = Preferences.userNodeForPackage(this.getClass());
    }
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g;
	frc = g2.getFontRenderContext();
	if(isGameOver) {	    
	    t1 = new TextLayout("Well Played", f1, frc);
	    t2 = new TextLayout("Awesome!", f2, frc);
	   
	    t1.draw(g2, 300, 270);
	    if(score == 15) {
		t2.draw(g2, 350, 290);  // Means the player has cleared all the bricks
	    }else{
		t3.draw(g2, 350, 290); // Game ended but some bricks left
	    }
	    g2.drawString("Top Scores", 370, 350); // Display top scores of all time
	    g2.drawString("First:  "+Integer.toString(prefs.getInt("#1", 0)), 360, 370);
	    g2.drawString("Second: "+Integer.toString(prefs.getInt("#2", 0)), 360, 390);
	    g2.drawString("Third:  "+Integer.toString(prefs.getInt("#3", 0)), 360, 410);
	}
	e.setFrame(ball.x, ball.y, ball.radius, ball.radius); // Set ball position
	c.setFrame(player.x, player.y, player.width, player.height); // Set player position
	// Fill and draw the above objects
	g2.fill(e);
	g2.draw(e);
	g2.fill(c);
	g2.draw(c);
	g2.drawString("Score: "+Integer.toString(score), 20, 560);
	g2.drawString("Lives: "+Integer.toString(lives)+" Remaining", 650, 560);
	// Draw the Bricks
	for(Brick[] Row : bricks) {
	    for(Brick brick : Row) {
		if(brick.isRendered) {		    
		    g2.setColor(Color.red);		   
		    g2.fill(brick.br);
		    g2.setColor(Color.black);
		    g2.draw(brick.br);		    
		}
	    }
	}
    }
    
    public void resetPlayer() {
	// Resets the player position
	Dimension d = getSize();
	ball.x = d.width/2;
	ball.y = d.height - 65;
	ball.dx = 4.0;
	ball.dy = -4.0;
	player.x = d.width/2 - 50;
    }

    public void resetBricks() {
	// Resets the Bricks (Makes all them visible)
	for(Brick[] Row : bricks) {
	    for(Brick brick : Row) {
		brick.isRendered = true;
	    }
	}
    }
    
    public void resetStats() {
	// Resets the stats
	lives = 3;
	score = 0;
	isGameOver = false;
    }
    
    public void handleEvent(KeyEvent e) {
	// Handles the keyboard events
	double shift = 15;
	Dimension d = getSize();
	int k = e.getKeyCode();
	if((k == KeyEvent.VK_LEFT) || (k== KeyEvent.VK_A) ||(k == KeyEvent.VK_KP_LEFT)) {
	    // If a left arrow key
	    if(player.x > 0) {
		player.x -= shift; // Move left
	    }
	}
	if((k == KeyEvent.VK_RIGHT) || (k==KeyEvent.VK_D) || (k == KeyEvent.VK_KP_RIGHT)) {
	    // If a right arrow key
	    if(player.x < (d.width - player.width - shift)) {
		player.x += shift;  // Move right
	    }
	}
    }

    public void initscene() {
	// Initialize the scene
	double x0 = 50.0, y0 = 100.0;
	Dimension d = getSize();
	ball  = new Ball(d.width/2, d.height - 60, 4.0, -4.0, 10.0);
	player = new Player(d.width/2 - 50, d.height - 50, 100, 10);	
	bricks = new Brick[3][];
	bricks[0] = new Brick[7]; // First row
	bricks[1] = new Brick[5]; // Second row
	bricks[2] = new Brick[3]; // Third row
	double xs = x0, ys = y0;
	for(int i=0; i<7; ++i) {  // Layout first row
	    bricks[0][i] = new Brick(xs, ys);
	    xs += Brick.width;
	}
	xs = x0 + Brick.width;
	ys = y0 + Brick.height;
	for(int i=0; i<5; ++i) { // Layout second row
	    bricks[1][i] = new Brick(xs, ys);
	    xs += Brick.width;
	}
	xs = x0 + (2.0 * Brick.width);
	ys = y0 + (2.0 * Brick.height);
	for(int i=0; i<3; ++i) { // Layout third row
	    bricks[2][i] = new Brick(xs, ys);
	    xs += Brick.width;
	}
	
	f1 = new Font("Consolas", Font.BOLD, 32);
	f2 = new Font("Consolas", Font.BOLD, 20);
	
    }

    private void manageScores() { // Manages the top scores
	int[] a  = new int[3];
	a[0] = prefs.getInt("#1", 0);
	a[1] = prefs.getInt("#2", 0);
	a[2] = prefs.getInt("#3", 0);
	if(score > a[0]) { // Sort them
	    prefs.putInt("#1", score);
	    prefs.putInt("#2", a[0]);
	    prefs.putInt("#3", a[1]);
	}else if(score > a[1]) {
	    prefs.putInt("#2", score);
	    prefs.putInt("#3", a[1]);
	}else if(score > a[2]) {
	    prefs.putInt("#3", score);
	}
    }
    
    public void resetScores() { // Resets the top scores
	prefs.putInt("#1", 0);
	prefs.putInt("#2", 0);
	prefs.putInt("#3", 0);
    }

    public void play() throws Exception{
	// The game play
	Dimension d = getSize();
	if(ball.x < 0) {
	    // Ball hits the left wall
	    ball.x = 0;
	    ball.dx = -ball.dx;
	}
	if(ball.y < 0) {
	    // Ball hits the upper wall
	    ball.y = 0;
	    ball.dy = -ball.dy;
	}
	if((ball.x + ball.radius) >= d.width) {
	    // Ball hits the right wall
	    ball.x = d.width - ball.radius;
	    ball.dx = -ball.dx;
	}
	if((ball.y + ball.radius) >= d.height) {
	    // Ball hits the bottom wall
	    lives -= 1; // decrement the number of lives
	    if(lives > 0) {
		resetPlayer(); // Reset and resume if the player still has lives
	    }else {
		// Declare the game to be over
		isGameOver = true;
		BrickBreaker.tmr.stop();
		manageScores();
	    }	    
	}
	checkCollisionWithPlayer(); // Check collision with the player
	if(ball.y < 200) { // If the ball is near the bricks
	    for(Brick[] Row : bricks) {
		for(Brick brick : Row) {
		    checkCollisionWithBrick(brick); // Check for collison with each of the bricks
		}
	    }
	}
	// Move the ball
	ball.x += ball.dx;
	ball.y += ball.dy;
    }
    private boolean checkCollisionWithPlayer() { // checking and handling collision of ball with Player
	if(e.intersects(c)) { // If the ball collides
	    ball.dy = - Math.abs(ball.dy); // Bounce it
	    // Calculate impact distance (The distance from the point of impact to the center of the player block )
	    impact_distance = Point2D.distance(e.getCenterX(), e.getCenterY(), c.getCenterX(), c.getCenterY());
	    if(impact_distance >= 30.0) { // If Impact distance is > 30 that is
		ball.dx = -ball.dx;       // if the ball has hit the player at the edges reverse the direction  
	    }
	    ball.x += ball.dx;
	    ball.y += ball.dy;
	    return true;
	}
	return false;
    }
    private void checkCollisionWithBrick(Brick b) throws Exception { // Checks collisons with brick
	if(b.isRendered) { // If the brick was rendered on the screen
	    if(e.intersects(b.br)) { // And if collided
		try {
		    if(BrickBreaker.isSoundOn) {
			BrickBreaker.playClip();
		    }
		} catch(Exception ex) {
		    BrickBreaker.isSoundOn = false;
		}
		++score; // Increment score
		if(score == 15) { // If score is 50 end the game
		    isGameOver = true;
		    BrickBreaker.tmr.stop();
		    manageScores();
		}
		b.isRendered = false; // The brick should disappear
		// Determine on which of the brick has the impact occured
		int k = b.br.outcode(e.getCenterX(), e.getCenterY());
		if((k == Rectangle2D.OUT_LEFT) || (k == Rectangle2D.OUT_RIGHT)) { // Left or right side
		    ball.dx = -ball.dx;
		}else { // Top of bottom side of the brick
		    ball.dy = -ball.dy;
		}
	    }
	}
    }
}

class KeyMon implements KeyListener { // Monitors keyboard events
    public KeyMon(World w) {
	this.w = w;
    }
    public void keyPressed(KeyEvent e) {
	if(!w.isGameOver) { // If the game is running
	    if(e.getKeyCode() == KeyEvent.VK_SPACE) { // If SPACE is pressed
		if(BrickBreaker.tmr.isRunning()) { // If the game is running pause it
		    BrickBreaker.tmr.stop();
		} else {
		    BrickBreaker.tmr.start(); // If not resume it
		}
		return;
	    }
	}else{ // If no game is running
	    if(e.getKeyCode() == KeyEvent.VK_Y) { // If 'Y' is pressed
		w.resetPlayer(); // Reset Players
		w.resetBricks(); // Reset Bricks
		w.resetStats();  // Reset Stats
		BrickBreaker.tmr.start(); // Start the game
	    }
	    if(e.getKeyCode() == KeyEvent.VK_R) {
		w.resetScores();
		w.repaint();
	    }
	}
	if(BrickBreaker.tmr.isRunning()) { // If the game is running
	    w.handleEvent(e); // Handle the "Left" and "Right" arrow keys
	}
    }
    public void keyTyped(KeyEvent e) {
	// Implemented
    }
    public void keyReleased(KeyEvent e) {
	// Implemented
    }
    public World w;
}

public class BrickBreaker {
    private static File soundFile = new File("impact.wav");
    public static boolean isSoundOn = true;
    public static void main(String[] args) {
	JFrame F = new JFrame("B Breaker");
	final World w = new World();
	F.add(w);
	F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	F.setSize(800,600);
	F.setResizable(false); // Frame not resizable
	F.setLocationByPlatform(true); // Allow the platform to position the frame
	KeyMon k = new KeyMon(w);
	F.addKeyListener(k);
	F.setVisible(true);
	w.initscene(); // Initialize the scene
	Action playAndUpdateAction = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7073057547577855258L;

		public void actionPerformed(ActionEvent e) {
		    try {
			w.play();
		    }catch(Exception ex) {
			ex.printStackTrace();
		    }
		    w.repaint();		
		}
	    };
	tmr = new Timer(20, playAndUpdateAction); // Fire the above action event every 20 ms
	tmr.start(); // Let's get it started!
    }
    public static Timer tmr; // Timer object

    public static void playClip() //Plays the sound clip
	throws IOException, UnsupportedAudioFileException,
	       LineUnavailableException {
	AudioInputStream auIn = null;
	Clip clip = null;
	try {
	    auIn = AudioSystem.getAudioInputStream(soundFile); // May fail in some cases
	    clip = AudioSystem.getClip();                      //  like when audio is in use by other programs
	    clip.open(auIn);
	    clip.start();	
	} finally {
	    if(auIn != null) {
		auIn.close();
	    }
	}
    }
}