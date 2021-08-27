import java.lang.Math;

// An enemy following another enemy.

public class EnemyFollower extends Enemy
{	
	Enemy enemy;
	int
		previousX,
		previousY,
		currentX,
		currentY;
	public EnemyFollower(int x, int y, Enemy enemy, int index)
	{
		this.x = x;
		this.y = y;
		this.enemy = enemy;
		this.index = index;
		previousX = enemy.getX();
		previousY = enemy.getY();
		remove = false;
	}
	
	public void move()
	{
		currentX = enemy.getX();
		currentY = enemy.getY();
		
		if(enemy.checkRemove() && currentX == previousX && currentY == previousY) // This way, it doesn't get moved multiple times.
		{
			enemy.move(); // Enemies can be moved from the dead!
			currentX = enemy.getX();
			currentY = enemy.getY();
		}
		if(x > -width)
		{
			if(currentX > previousX)
				x += currentX - previousX;
			else
				x -= previousX - currentX;
				
			if(currentY > previousY)
				y += currentY - previousY;
			else
				y -= previousY - currentY;
				
			previousX = currentX;
			previousY = currentY;
		}
		else
			remove = true; // This enemy will not be followed, so it can be safely removed.
	}
}