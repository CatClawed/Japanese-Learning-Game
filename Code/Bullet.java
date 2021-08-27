// The data for a given bullet. It's a pretty simple object, as it only moves
// right.

public class Bullet extends ImageData
{	
	public Bullet(int x, int y)
    {
        this.x = x;
        this.y = y;
		movementSpeed = 8;
		width = 10;
		height = 7;
    }
	
	public void move()
	{
		if(x < 820)
			x += movementSpeed;
		else
			remove = true;
	}
}