import java.awt.Rectangle;

// The enemy superclass. Since ImageData handles the X/Y data, this
// class is used to define that enemies move, and they refer to a
// particular character/image at an array's index, as defined within
// the Game class.

public abstract class Enemy extends ImageData
{
	int
		index,
		height = 50, // If I implement scaling, I'll have to change this. Works as prototype.
		width = 133,
		yOffset;
	boolean remove;
		
	abstract void move();
	
	public void setRemove()
	{
		remove = true;
	}
	
	public int getIndex()
	{
		return index;
	}
	public boolean checkRemove()
	{
		return remove;
	}
}