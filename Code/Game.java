///////////////////////////////////////////////////////////////////////////////////
//
// Game
//
// The class that handles the overall rules of the game. In summary, it does all
// of the following:
//
//	1.) Creates images of enemies, from the information passed from the main menu.
//	2.) Handles keyboard inputs
//	3.) Moves all objects on screen
//	4.) Repaints at ~60 FPS
//  5.) Toggles between difficulties, enemy formations, and the probability that
//		a particular enemy will appear.
//
///////////////////////////////////////////////////////////////////////////////////

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;
import javax.swing.Timer;

public class Game extends JComponent
{
	private Screen screen;
	private Random random = new Random();
	private Timer time, repaintTimer;
	
    private int
	// Time
		minutes = 0,
		seconds = 0,
	// Mode
        mode,
		KANJI_MODE = 3,
		KATAKANA_MODE = 2,
		HIRAGANA_MODE = 1,
	// Difficulty
		EASY_DIFFICULTY = 1,
		NORMAL_DIFFICULTY = 2,
		HARD_DIFFICULTY = 3,
		difficulty = NORMAL_DIFFICULTY,
	// Score, difficulty scaling
        score = 0,
		incorrectCollections = 0,
		correctCollections = 0,
		correctCount = 0, // Used to change characters.
		incorrectShootings = 0,
		totalShootings = 0,
		timeCount = 0,
		charactersShown = 0,
	// Dimensions
		bulletHeight = 2,
		enemyHeight,
		enemyWidth,
		HEIGHT = 574, // Size of window (minus insets)
		WIDTH = 1020;
		
	private String fileString;
	private Map.Entry currentPair;
		
	private Font
		JapaneseMissileFont = new Font("Meiryo", Font.BOLD, 20),				// Missile font for kanji mode.
		EnglishMissileFont = new Font("Berlin Sans FB Demi", Font.PLAIN, 26),	// Missile font for kana mode.
		JapaneseCharacterFont = new Font("Meiryo", Font.BOLD, 35),				// Displays kanji/kana in sidebar.
		EnglishTimeFont = new Font("Lucida Console", Font.PLAIN, 30),			// For displaying time.
		EnglishSmallerFont = new Font("Berlin Sans FB Demi", Font.PLAIN, 20);	// For misc. information
		
	private Color yellowGreen = new Color(0x8fd900);
		
	private boolean
		repainting = false, // Prevents multiple calls to myRepaint()
		showHint = false,
		upPressed = false,  // Allows me to use two keys at once.
		downPressed = false,
		gameOver = false;
		
	// Keyboard stuff
	public InputMap inputmap;
	public ActionMap actionmap;
	
	BufferStrategy strategy; // Active rendering
	Dimension d; // For the JFrame.
	Insets insets; // From the JFrame, to ensure the image displays at the correct place.
    
	// Objects on board
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private Player player;
	private BufferedImage
		playerImg,	// This is what the game paints. It's switched between normal and collection mode.
		playerNormal,
		playerCollect;
	
	// Collision detection. Instead of giving each ImageData a rectangle, I handle that stuff here.
	private Rectangle
		playerRect = new Rectangle(40,40),
		bulletRect = new Rectangle(10,7),
		enemyRect = new Rectangle(133,50); 
	
	private ArrayList<Double> probability;
	private ArrayList<BufferedImage> enemyImages;
	private ArrayList<String> kanjiKana;
	private HashMap<String, String> hint; // A character/hint pair. May reorder characters, but that's quite alright.
	private Iterator it; // I use the iterator in a sort of nonstandard way; it determines when the game is done, and what
						 // character I should display.
	
	/************************************************************************************************************************
	 *
	 * Game()
	 * 	Constructor. Takes the insets, the screen (to notify it of game over), a string containing the file to be read from,
	 *  and the game's mode.
	 *
	 ***********************************************************************************************************************/
	
    public Game(Insets insets, Screen screen, String fileString, int mode)
    {
		this.insets = insets;
		this.screen = screen;
		this.fileString = fileString;
		this.mode = mode;
		setIgnoreRepaint(true); // Dunno if these two are necessary, but it doesn't seem to hurt.
		setDoubleBuffered(true);
		d = new Dimension(WIDTH,HEIGHT);
		strategy = screen.getBufferStrategy();
        player = new Player();
		enemyImages = new ArrayList<>();
		probability = new ArrayList<>();
		kanjiKana = new ArrayList<>();
		hint = new HashMap();
		
		generateEnemyImages();
		insertKeyControls();
		
		try
		{
			playerNormal = ImageIO.read(new File("Image\\Player.png"));
			playerCollect = ImageIO.read(new File("Image\\Player2.png"));
			playerImg = playerNormal;
		} catch (IOException e) {}
		
		// Repaints at, calculation delay aside, 60 FPS.
		repaintTimer = new Timer(16, new ActionListener() {
				public void actionPerformed(ActionEvent evt)
				{
					myRepaint();
				}
			});
		
		
		// A timer for the game's time.
		time = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt)
				{
					if(seconds == 58)
					{
						minutes++;
						seconds = 0;
					}
					else
						seconds++;
				}
			});
		repaintTimer.start();
		time.start();
    }
	
	/************************************************************************************************************************
	 *
	 * insertKeyControls()
	 *
	 * 	Instead of having a bloated constructor, all keyboard inputs have been thrown here. These are the controls:
	 *		F    - Shoot Bullet
	 *		D    - Collect missile
	 *		A    - Display hint
	 *		Up   - Move Up
	 *		Down - Move Down (lower priority than moving up, if both are pressed, as specified in moveEverything())
	 *		Esc  - Exit
	 *		Q    - To Main Menu
	 *
	 ***********************************************************************************************************************/
	
	public void insertKeyControls()
	{
		inputmap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		actionmap = getActionMap();
		
		// Escape for exiting
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "exit" );
      
        actionmap.put("exit", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
              System.exit(0);
			}
		});
		
		// Up and down arrows for player movement.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), "moveDown");
          
        actionmap.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
				downPressed = true;
            }
        });
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), "moveUp");
          
        actionmap.put("moveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				upPressed = true;
            }
        });
		
		// Key release behavior for up/down keys.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0,true), "stopMoveDown");
          
        actionmap.put("stopMoveDown", new AbstractAction(){
            public void actionPerformed(ActionEvent e)
			{
				downPressed = false;
            }
        });
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0,true), "stopMoveUp");
          
        actionmap.put("stopMoveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
				upPressed = false;
            }
        });
		
		// F key for bullets. Shoots upon release of key, for simplicity. Would be neat
		// to add some sort of charging feature.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F,0,true), "createBullet");
          
        actionmap.put("createBullet", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				bullets.add(new Bullet(player.getX()+player.getWidth(), player.getY()+player.getHeight()/2 - bulletHeight/2));
            }
        });
		
		// D key for collection. Turns player icon red.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D,0), "collect");
          
        actionmap.put("collect", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				playerImg = playerCollect;
            }
        });
		
		// D release. Player icon is blue again.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D,0,true), "stopCollect");
          
        actionmap.put("stopCollect", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				playerImg = playerNormal;
            }
        });
		
		// A release. Deterines whether to show hint.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,0,true), "toggleHint");
          
        actionmap.put("toggleHint", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				showHint = !showHint;
            }
        });
		
		// Q release. Returns to main menu.
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q,0,true), "quit");
          
        actionmap.put("quit", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				stopGame();
            }
        });
	}
	
	/************************************************************************************************************************
	 *
	 * generateEnemyImages()
	 *
	 * 	Where enemy images, and their related information, are created. I do not claim my method of stuffing text file info
	 *  into arraylists and hashmaps is the best. It was simply a quick, understandable approach.
	 *
	 *  This part operates under the assumption that text files are formatted correctly.
	 *
	 ***********************************************************************************************************************/
    
    public void generateEnemyImages()
    {
		try
		{
		
			File file = new File(fileString);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String str;
			String str2;
			String primaryCharacter;
			Font fontUsed;
			
			while((str = in.readLine()) != null)
			{
				// The initial data about the kanji/kana.
				if(mode == KATAKANA_MODE)
				{
					fontUsed = EnglishMissileFont;
					primaryCharacter = str.substring(0,1);
					hint.put(primaryCharacter, str.substring(2));
				}
				else if(mode == KANJI_MODE)
				{
					fontUsed = JapaneseMissileFont;
					primaryCharacter = str.substring(0,1);
					hint.put(primaryCharacter, str.substring(2));
				}
				else
				{
					fontUsed = EnglishMissileFont;
					primaryCharacter = str.substring(2);
					hint.put(primaryCharacter, str.substring(0,1));
				}
				
				// Reading info. The kanji or kana gets stuffed into an ArrayList, whose index
				// matches the BufferedImage with a corresponding reading.
				while(!(str2 = in.readLine()).contains("~"))
				{
					kanjiKana.add(primaryCharacter);
					BufferedImage tempImg = ImageIO.read(new File("Image\\Missile4.png"));
					
					Graphics2D temp = (Graphics2D) tempImg.getGraphics();
					temp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					temp.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					temp.setFont(fontUsed);
					
					// Text outline. Used to transition text to the background color in effort to slightly smooth animation.
					FontRenderContext frc = temp.getFontRenderContext();
					TextLayout textTl = new TextLayout(str2, fontUsed, frc);
					AffineTransform transform = new AffineTransform();
					Shape outline = textTl.getOutline(null);
					Rectangle outlineBounds = outline.getBounds();
					transform = temp.getTransform();
					transform.translate((133 / 2 - (outlineBounds.width / 2)), 50 / 2 + (outlineBounds.height / 2)); // Center
					temp.transform(transform);
					
					// Draw text
					temp.setColor(Color.yellow);
					temp.drawString(str2, 0,0);
					
					//Draw outline. Shows up better if drawn second.
					temp.setColor(yellowGreen);
					temp.draw(outline);
					temp.setClip(outline);
					temp.dispose();
					enemyImages.add(tempImg);
				}
			}
			it = hint.entrySet().iterator();
			currentPair = (Map.Entry) it.next();
			setProbability();
		} catch (IOException e) {}
    }
	
	/************************************************************************************************************************
	 *
	 * moveEverything()
	 *
	 * 	A fairly self explanatory method. Keys may influence the movement, but the behavior is ultimately defined within
	 * 	the ImageData subclasses.
	 *
	 ***********************************************************************************************************************/
	
	public void moveEverything()
	{
		if(upPressed)
		{
			player.moveUp();
		}
		else if(downPressed) // If both are pressed, I suppose it just goes up.
		{
			player.moveDown();
		}
		for(Bullet temp : bullets)
		{
			temp.move();
		}
		for(Enemy temp : enemies)
		{
			temp.move();
		}
	}
	
	/************************************************************************************************************************
	 *
	 * collisionDetect()
	 *
	 * 	This method uses three rectangles, assigns them to the coordinates of the relevant objects, and checks for
	 *  collision. There are only three rectangles because it seems unnecessary to have one per enemy/bullet. This does
	 *  give the player a funky hitbox, though.
	 *
	 ***********************************************************************************************************************/
	 
	public void collisionDetect()
	{
		playerRect.setLocation(player.getX(), player.getY());
	
		for(int i = 0; i < enemies.size(); i++)
		{
			Enemy e = enemies.get(i);
			enemyRect.setLocation(e.getX(), e.getY());
			
			for(int j = 0; j < bullets.size(); j++)
			{
				Bullet b = bullets.get(j);
				bulletRect.setLocation(b.getX(), b.getY());
				
				if(bulletRect.intersects(enemyRect))
				{
					if((String) currentPair.getKey() == kanjiKana.get(e.getIndex()))
					{
						score -= 15; // Hit a missile that should have been collected.
						incorrectShootings++;
						totalShootings++;
					}
					else
					{
						score += 20;
						totalShootings++;
					}
					enemies.get(i).setRemove();
					//i--;
					bullets.remove(j);
					j = bullets.size();
				}
			}
			if(playerRect.intersects(enemyRect) && playerImg == playerCollect) // Checks for the correct image + intersection.
			{
				if((String) currentPair.getKey() == kanjiKana.get(e.getIndex()))
				{
					score += 100;
					correctCollections++;
				}
				else
				{
					score -= 150;
					incorrectCollections++;
				}
				enemies.get(i).setRemove(); // Because of the EnemyFollower class, I remove these when I repaint instead.
				//i--;
			}
		}
	}
	
	/************************************************************************************************************************
	 *
	 * createEnemies()
	 *
	 *  Generates enemies and their formations. There are four patterns/formations:
	 *
	 *   Name    | Enemies | Notes
	 *   --------+----------------------------------------------------------------------------------
	 *   Circle  |  2      | Easy and above. Initially similar to the linear pattern.
	 *   Sine    |  3      | Easy and above, probably the easiest of 'em all.
	 *   Linear  |  3/4    | Easy and above, but has 3 enemies on easy, 4 on normal/hard.
	 *   Homing  |  3      | Hard only. Like the circle pattern, but has a leading homing enemy.
	 *
	 ***********************************************************************************************************************/
	 
	 public void createEnemies()
	 {
		if(correctCollections > correctCount + 2) // Only three correct collections needed to proceed. Can be up to six, with luck.
		{
			if(!it.hasNext())
				gameOver = true;
			else
			{
				correctCount = correctCollections; // Ensures this only happens when changing characters.
				currentPair = (Map.Entry) it.next(); // Move on to next character.
				setProbability();
			}
		}
		
		int formation;
		
		// More formations for harder difficulties.
		
		if(difficulty == EASY_DIFFICULTY)
			formation = random.nextInt(3);
		else if(difficulty == NORMAL_DIFFICULTY)
			formation = random.nextInt(3);
		else
			formation = random.nextInt(4);
		
		if(formation == 1) 	// CIRCLE FORMATION, two enemies, easy and above
		{
			int yIntercept = random.nextInt(HEIGHT - 200) + 100;
			int y = random.nextInt(HEIGHT);
			int y2;
			if(y > HEIGHT/2)
				y2 = y-130;
			else
				y2 = y+130;
			
			EnemyCircle followed = new EnemyCircle(1020, y, yIntercept, fetchIndex(), false);
			enemies.add(followed);
			enemies.add(new EnemyFollower(1020 + 100, y2, followed, fetchIndex()));
			
		}
		else if(formation == 2) // SINE FORMATION, three enemies, easy and above
		{
			int yOffset = random.nextInt(HEIGHT - 400) + 200;
			enemies.add(new EnemySine(1020,       yOffset,       fetchIndex()));
			enemies.add(new EnemySine(1020 + 150, yOffset - 100, fetchIndex()));
			enemies.add(new EnemySine(1020 + 300, yOffset + 100, fetchIndex()));
		}
		else if(formation== 3) // LINEAR FORMATION, three enemies for easy, four for hard/normal
		{
			int yIntercept = random.nextInt(HEIGHT - 200) + 100;
			int y = random.nextInt(HEIGHT);
			EnemyLinear followed = new EnemyLinear(1020, y, yIntercept, fetchIndex());
			enemies.add(followed);
			enemies.add(new EnemyFollower(1020 + 100, y+55, followed, fetchIndex()));
			enemies.add(new EnemyFollower(1020 + 100, y-55, followed, fetchIndex()));
			
			if(difficulty > EASY_DIFFICULTY) // One more enemy for normal/hard
				enemies.add(new EnemyFollower(1020 + 200, y, followed, fetchIndex()));
		}
		else  // HOMING FORMATION, one homing enemy, two circle enemies, hard exclusive
		{
			int yIntercept = random.nextInt(HEIGHT - 200) + 100;
			int y = random.nextInt(HEIGHT);
			int y2;
			if(y > HEIGHT/2)
				y2 = y-130;
			else
				y2 = y+130;
			
			enemies.add(new EnemyHoming(1020, random.nextInt(HEIGHT), player, fetchIndex()));
			
			EnemyCircle followed = new EnemyCircle(1020, y, yIntercept, fetchIndex(), true);
			enemies.add(followed);
			enemies.add(new EnemyFollower(1020 + 100, y2, followed, fetchIndex()));
		}
	 }
	 
	 /************************************************************************************************************************
	 *
	 * int fetchIndex()
	 *
	 *  When creating enemies, an index is assigned to them. This index is determined by the probability array and a
	 *  random number. The index points to the correct enemy image to display and the character it represents.
	 *
	 ***********************************************************************************************************************/
	 
	 public int fetchIndex()
	 {
		int index;
		double r = random.nextDouble();
		for(int i = 0; i < probability.size(); i++)
		{
			if(r < probability.get(i))
			{
				return i;
			}
		}
		return -2; // Should not reach here.
	 }
	
	 /************************************************************************************************************************
	 *
	 * setProbability()
	 *
	 *  Sets probability that any character will appear. 4/9ths of the time, on easy mode, the correct answer(s) will
	 *  appear. It's otherwise set to 1/3. The rest of the probability pie is divided between incorrect answers.
	 *
	 ***********************************************************************************************************************/
	 
	 public void setProbability()
	 {
		probability.clear();
		
		int firstIndex = kanjiKana.indexOf((String) currentPair.getKey());
		int lastIndex = kanjiKana.lastIndexOf((String) currentPair.getKey());
		double difference = (double) (lastIndex - firstIndex + 1);
		
		double probCorrect;
		double probIncorrect;
		double previous = 0;
		
		if(difficulty == EASY_DIFFICULTY) // More correct missiles. 4/9 correct.
		{
			probCorrect = (4.0 / 9.0) / (difference);
			probIncorrect = (5.0 / 9.0) / ((double) kanjiKana.size() - difference);
		}
		else // Same correct for Normal/Hard. 1/3 correct.
		{
			probCorrect = (1.0 / 3.0) / (difference);
			probIncorrect = (2.0 / 3.0) / ((double)kanjiKana.size() - difference);
		}
		for(int i = 0; i < kanjiKana.size() -1; i++)
		{
			if(kanjiKana.get(i) == (String) currentPair.getKey()) // If the character at i is the one being displayed...
			{
				previous += probCorrect;
				probability.add(previous);
			}
			else
			{
				previous += probIncorrect;
				probability.add(previous);
			}
		}
		probability.add(1.0); // No fuzzy final numbers.
	 }
	 
	 /************************************************************************************************************************
	 *
	 * scaleDifficulty()
	 *
	 *  Changes difficulty based on player performance. Easy mode just requires that half of all collections OR half
	 *  of all shot missiles were incorrect. Hard requires good performance with both shootings AND collections. Technically
	 *  starts in hard mode.
	 *
	 ***********************************************************************************************************************/
	 
	 public void scaleDifficulty()
	 {
		double shootingRatio = (double) incorrectShootings/(double) totalShootings;
		double collectedRatio = (double)correctCollections/(double)(correctCollections+incorrectCollections);
		int oldDifficulty = difficulty;
		
		if(collectedRatio > .85 && shootingRatio < .20)
			difficulty = HARD_DIFFICULTY;
		else if(collectedRatio < .5 || shootingRatio > .5)
			difficulty = EASY_DIFFICULTY;
		else
			difficulty = NORMAL_DIFFICULTY;
			
		if(oldDifficulty != difficulty)
			setProbability(); // Changes probabilities for difficulty changes.
	 }
	 
	 public void stopGame()
	 {
		inputmap.clear();
		actionmap.clear();
		time.stop();
		repaintTimer.stop();
		screen.gameOver(); // Tells the screen the game is done.
	 }
	
	/************************************************************************************************************************
	 *
	 * myRepaint()
	 *
	 * 	Instead of relying on Java to handle repainting for me, I decided to take a faster active rendering approach.
	 *	This method is called every 16 milliseconds and handles the overall rules of the game. In reality, I may not be
	 *  hitting 60 FPS after calculations; that may require threads.
	 *
	 ***********************************************************************************************************************/
	
	public void myRepaint()
	{
		if(gameOver && enemies.isEmpty())
		{
			stopGame();
		}
		else if(repainting)
			return;
		else
		{
			repainting = true;
			timeCount++;
			
			if(timeCount % (60 * 4) == 0) // Basicallly, check every 4-ish seconds
			{
				scaleDifficulty();
			}
			
			if(enemies.isEmpty())
			{
				createEnemies();
			}
			
			moveEverything();
			
			Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics(); // 2D for antialiasing.
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Draw main area background
			graphics.setColor(Color.GRAY);
			graphics.fillRect(insets.left, insets.top, WIDTH, HEIGHT);
				
			// Draw Bullets
			for(int i = 0; i < bullets.size(); i++)
			{
				graphics.setColor(Color.WHITE);
				Bullet temp = bullets.get(i);
				graphics.fillOval(temp.getX() + insets.left, temp.getY() + insets.top,
					temp.getWidth(), temp.getHeight());
				graphics.setColor(Color.black);
				graphics.drawOval(temp.getX() + insets.left, temp.getY() + insets.top,
					temp.getWidth(), temp.getHeight());
				
				if(temp.checkRemove())
				{
					bullets.remove(i);
					i--;
				}
			}
			
			// Draw Missiles
			for(int i = 0; i < enemies.size(); i++)
			{
				Enemy temp = enemies.get(i);
				if(temp.checkRemove())
				{
					enemies.remove(i);
					i--;
				}
				graphics.drawImage(enemyImages.get(temp.getIndex()), temp.getX() + insets.left, temp.getY() + insets.top, null);
			}
			
			// Draw Player (last, so the player is always on top)
			graphics.drawImage(playerImg, player.getX() + insets.left, player.getY() + insets.top, null);
			
			// Draw side bar background
			graphics.setColor(Color.BLACK);
			graphics.fillRect(insets.left + WIDTH - 200, insets.top, 200, HEIGHT);
			
			// Draw text
			graphics.setColor(Color.WHITE);
			graphics.setFont(EnglishSmallerFont);
			graphics.drawString("Time:", 900+insets.left, 20+insets.top);
			
			graphics.drawString("Score:", 900+insets.left, 500+insets.top);
			graphics.drawString(score + "", 900+insets.left, 520+insets.top);
			
			graphics.setFont(EnglishTimeFont);
			if(seconds < 10)
				graphics.drawString(minutes + ":0" + seconds, 900+insets.left, 50+insets.top);
			else
				graphics.drawString(minutes + ":" + seconds, 900+insets.left, 50+insets.top);
			
			graphics.setFont(JapaneseCharacterFont);
			
			graphics.drawString((String)currentPair.getKey(), 900+insets.left, 200+insets.top);
			if(showHint && mode != KANJI_MODE)
			{
				graphics.setFont(JapaneseMissileFont);
				graphics.drawString("( " + (String)currentPair.getValue() + " )", 900+insets.left, 235+insets.top);
			}
			else if(showHint)
			{
				graphics.setFont(EnglishSmallerFont);
				graphics.drawString("( " + (String)currentPair.getValue() + " )", 900+insets.left, 235+insets.top);
			}
				
			
			if(graphics != null)
				graphics.dispose();
			
			collisionDetect(); // I let the last frame draw before removing any objects.
			
			strategy.show();
			Toolkit.getDefaultToolkit().sync();
			repainting = false;
		}
	}
	
	// This is for the JFrame, so the window is automatically the correct size.
	public Dimension getPreferredSize()
	{
		return d;
	}
}