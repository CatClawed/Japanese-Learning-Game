// The data representing the player's location. The player can only move
// when up or down is pressed.

public class Player extends ImageData
{
	public Player()
    {
        x = 3;
        y = 250;
		width = 40;
		height = 40;
		movementSpeed = 5;
    }
	
	public void moveUp()
	{
		if(y > 0)
			y -= movementSpeed;
	}
	
	public void moveDown()
	{
		if(y < GAME_HEIGHT - height)
			y += movementSpeed;
	}
}