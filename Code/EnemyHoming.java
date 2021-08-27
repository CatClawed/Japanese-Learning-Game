import java.lang.Math;

// An enemy following the player. Designed as decoy, of sorts, but is fairly easy to shoot.

public class EnemyHoming extends Enemy
{	
	Player player;
	
	public EnemyHoming(int x, int y, Player player, int index)
	{
		this.x = x;
		this.y = y;
		this.player = player;
		this.index = index;
		remove = false;
	}
	
	public void move()
	{
		if(x <= -width)
			remove = true;
		
		x -= 5;
		if(player.getY() > y+3)
			y += 3; // Vertically slower than the player.
		else if(player.getY() < y-3)
			y -= 3;
	}
}