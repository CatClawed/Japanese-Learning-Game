import java.lang.Math;

// An enemy following a sine wave.

public class EnemySine extends Enemy
{	
	int yOffset;
	public EnemySine(int x, int yOffset, int index)
	{
		this.x = x;
		this.yOffset = yOffset;
		this.index = index;
		remove = false;
		movementSpeed = 4;
	}
	
	public void move()
	{
		x -= movementSpeed;
		y = (int) (Math.sin(Math.toRadians(x-50)) * 70) + yOffset;
		
		if(y < 0) // Stay in bounds.
			y = -y;
		if(y > GAME_HEIGHT - height)
			y = (GAME_HEIGHT - height) * 2 - y;

		if(x <= -width)
			remove = true; // Enemy is not followed, but could be.
	}
}