import java.lang.Math;

// Starts out like EnemyLinear. Then makes a circle. This enemy does have a mild
// problem staying in bounds, but I used the final stretch to mitigate it somewhat.

public class EnemyCircle extends Enemy
{	
	int
		delayCount = 40,
		yIntercept,
		theta = 0,
		initialTheta,
		a, b,
		radius = 80;
	double slope;
	
	public EnemyCircle(int x, int y, int yIntercept, int index, boolean delay)
	{
		this.x = x;
		this.y = y;
		this.yIntercept = yIntercept;
		this.index = index;
		slope = (double) (y - yIntercept)/(double)x;
			
		if(delay)
			delayCount = 0;
			
		remove = false;
	}
	
	public void move()
	{
		if(delayCount < 35)
			delayCount++;
		else
		{
			if(x > 2 * width && theta == 0)
			{
				x -= 5;
				y = (int) (x * slope) + yIntercept;
				
				if(slope > 0)
					b = y - 70;
				else
					b = y + 70;
					
				a = x;
			}
			else if(theta == 0)
			{
				if(slope > 0)
					theta = 90;
				else
					theta = 180;
				initialTheta = theta;
			}
			else // Circle
			{
				if(theta <= initialTheta + 360)
				{
					x = a + (int) (radius * Math.cos(Math.toRadians(theta)));
					y = b + (int) (radius * Math.sin(Math.toRadians(theta)));
				}
				else
				{
					x -= 4;
					if(slope < 0)
						y -= 3;
					else
						y += 3;
				}
					
				theta += 5;
			}
		}
		if(x <= -width)
			remove = true; // May still be referred to by a follower, so I let it move when called.
	}
}