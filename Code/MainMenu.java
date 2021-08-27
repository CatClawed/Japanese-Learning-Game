import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import javax.swing.Timer;

public class MainMenu extends JComponent
{
	private Screen screen;
	private BufferStrategy strategy;
	private Dimension d;
	private Insets insets;
	public InputMap inputmap;
	public ActionMap actionmap;
	private Timer timer;
	
	private Font
		header = new Font("Berlin Sans FB Demi", Font.BOLD, 30),
		menuText  = new Font("Berlin Sans FB Demi", Font.PLAIN, 26);
	
	private boolean
		moveDown = false,
		moveUp = false,
		moveleft = false,
		moveRight = false,
		repainting = false;
		
	private String fileString;
	
	private ArrayList<String>
		modes = new ArrayList<>(),
		kanji = new ArrayList<>(),
		kana = new ArrayList<>(),
		controls = new ArrayList<>(),
		controls2 = new ArrayList<>();;
	
	private int
		HEIGHT = 574,
		WIDTH = 1020,
		modeSelect = 0,
		fileSelect = -1,
		xOffset1 = 50, // For displaying text.
		xOffset2 = 250,
		yOffset;
	
	public MainMenu(Insets insets, Screen screen)
	{
		this.insets = insets;
		this.screen = screen;
		setIgnoreRepaint(true); // Dunno if these two are necessary, but it doesn't seem to hurt.
		setDoubleBuffered(true);
		strategy = screen.getBufferStrategy();
		d = new Dimension(WIDTH,HEIGHT);
		insertKeyControls();
		
		modes.add("Hiragana");
		modes.add("Katakana");
		modes.add("Kanji");
		
		controls.add("Up/Down:");
		controls.add("F:");
		controls.add("D:");
		controls.add("A:");
		controls.add("Esc:");
		controls.add("Q:");
		
		controls2.add("Move Player");
		controls2.add("Shoot Bullet");
		controls2.add("Collect Enemy");
		controls2.add("Toggle Hint");
		controls2.add("Exit");
		controls2.add("Return to Main Menu");
		
		for(int i = 0; i < 4; i++)
		{
			kana.add("Group " + (i+1));
		}
		for(int i = 0; i < 3; i++)
		{
			kanji.add("Group " + (i+1));
		}
		timer = new Timer(33, new ActionListener() {
				public void actionPerformed(ActionEvent evt)
				{
					myRepaint();
				}
			});
		timer.start();
	}
	
	public void insertKeyControls()
	{
		inputmap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		actionmap = getActionMap();
	
		// Escape for exiting
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "quit" );
      
        actionmap.put("quit", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
              System.exit(0);
			}
		});
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "startGame" );
      
        actionmap.put("startGame", new AbstractAction() {
			public void actionPerformed(ActionEvent e)
			{
				if(fileSelect > -1)
				{
					startGame();
				}
			}
		});
		
		// Arrow keys for navigation
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), "moveDown");
          
        actionmap.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				if(fileSelect > -1)
				{
					if(modeSelect != 2 && fileSelect < kana.size() - 1)
					{
						fileSelect++;
					}
					else if(fileSelect < kanji.size() - 1)
					{
						fileSelect++;
					}
				}
				else if(modeSelect < 2)
				{
					modeSelect++;
				}
            }
        });
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), "moveUp");
          
        actionmap.put("moveUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				if(fileSelect > 0)
				{
					fileSelect--;
				}
				else if(modeSelect > 0)
				{
					modeSelect--;
				}
            }
        });
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), "moveLeft");
          
        actionmap.put("moveLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				fileSelect = -1;
            }
        });
		
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), "moveRight");
          
        actionmap.put("moveRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
			{
				if(fileSelect == -1)
					fileSelect = 0;
            }
        });
	
	}
	
	// Tells the screen to start up a new game.
	
	public void startGame()
	{
		String file;
		timer.stop();
		inputmap.clear();
		actionmap.clear();
		
		if(modeSelect != 2)
			file = "JapaneseData\\Kana\\Group" + (fileSelect+1) + ".txt";
		else
			file = "JapaneseData\\Kanji\\Group" + (fileSelect+1) + ".txt";
		
		screen.startGame(modeSelect+1, file);
	}
	
	public void myRepaint()
	{
		if(repainting)
			return;
		else
		{
			repainting = true;
			Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			graphics.setColor(Color.BLACK);
			graphics.fillRect(insets.left, insets.top, WIDTH, HEIGHT);
			
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.setFont(header);
			graphics.drawString("Mode Select", xOffset1+insets.left, 50+insets.top);
			graphics.drawString("Group Select", xOffset2+insets.left, 50+insets.top);
			graphics.drawString("Controls", xOffset1 + insets.left, 350+insets.top);
			graphics.setFont(menuText);
			
			yOffset = 100;
			for(int i = 0; i < modes.size(); i++)
			{
				if(modeSelect == i)
				{
					graphics.setColor(Color.WHITE);
					graphics.drawString(modes.get(i), xOffset1+insets.left, yOffset+insets.top);
					graphics.setColor(Color.LIGHT_GRAY);
				}
				else
				{
					graphics.drawString(modes.get(i), xOffset1+insets.left, yOffset+insets.top);
				}
				yOffset += 50;
			}
			yOffset = 100;
			if(modeSelect != 2)
			{
				for(int i = 0; i < kana.size(); i++)
				{
					if(fileSelect == i)
					{
						graphics.setColor(Color.WHITE);
						graphics.drawString(kana.get(i), xOffset2+insets.left, yOffset+insets.top);
						graphics.setColor(Color.LIGHT_GRAY);
					}
					else
					{
						graphics.drawString(kana.get(i), xOffset2+insets.left, yOffset+insets.top);
					}
					yOffset += 50;
				}
			}
			else
			{
				for(int i = 0; i < kanji.size(); i++)
				{
					if(fileSelect == i)
					{
						graphics.setColor(Color.WHITE);
						graphics.drawString(kanji.get(i), xOffset2+insets.left, yOffset+insets.top);
						graphics.setColor(Color.LIGHT_GRAY);
					}
					else
					{
						graphics.drawString(kanji.get(i), xOffset2+insets.left, yOffset+insets.top);
					}
					yOffset += 50;
				}
			}
			yOffset = 400;
			for(int i = 0; i < 3; i++)
			{
				graphics.drawString(controls.get(i), xOffset1+insets.left, yOffset+insets.top);
				graphics.drawString(controls2.get(i), xOffset1+150+insets.left, yOffset+insets.top);
				yOffset += 50;
			}
			yOffset = 400;
			for(int i = 3; i < 6; i++)
			{
				graphics.drawString(controls.get(i), xOffset1+400+insets.left, yOffset+insets.top);
				graphics.drawString(controls2.get(i), xOffset1+500+insets.left, yOffset+insets.top);
				yOffset += 50;
			}
			
			
			strategy.show();
			Toolkit.getDefaultToolkit().sync();
			repainting = false;
		}
	}
	
	// For the JFrame to know the size of the window.
	
	public Dimension getPreferredSize()
	{
		return d;
	}
}