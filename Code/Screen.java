import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

// The screen class exists to separate game logic from main menu logic.
// It's also the location of the main method, despite being a relatively
// unimportant class.

public class Screen extends JFrame
{
    private static Game game;
	private static MainMenu menu;
    public static BufferedImage img = new BufferedImage(1020,574,2);
    
    public Screen()
    {
        super("Game");
		setVisible(true);
		this.createBufferStrategy(2);
		menu = new MainMenu(getInsets(), this);
		menu.myRepaint();
		add(menu);
		pack();
		
		addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
		
    }
	
	// Main menu calls this to start up a new gaem.
	
	public void startGame(int mode, String file)
	{
		remove(menu);
		game = new Game(getInsets(), this, file, mode);
		add(game);
	}
	
	// Game calls this to switch to the main menu.
	
	public void gameOver()
	{
		menu = new MainMenu(getInsets(), this);
		remove(game);
		add(menu);
	}
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Screen();
			}
		});
    }
}
