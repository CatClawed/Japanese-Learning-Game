// An enemy going in a straight line.

public class EnemyLinear extends Enemy
{	
	int yIntercept;
	double slope;
	public EnemyLinear(int x, int y, int yIntercept, int index)
	{
		this.x = x;
		this.y = y;
		this.yIntercept = yIntercept;
		this.index = index;
		slope = (double) (y - yIntercept)/(double)x;
		remove = false;
	}
	
	public void move()
	{
		if(x <= -width)
			remove = true; // May still be referred to by a follower, so I let it move when called.
		
		x -= 5;
		y = (int) (x * slope) + yIntercept;
	}
}