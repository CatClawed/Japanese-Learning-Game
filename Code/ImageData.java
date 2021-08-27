// Custom object to make life a little easier.
// This doesn't hold images, but rather, everything else about the any given image.
// It's super generic, mainly to prevent creation of the same methods over and over.

public abstract class ImageData
{
    int
        x, y,			// Coordinates of an image
        index,			// Points to correct image and reading.
		width, height,	// Size of image
		movementSpeed,	// Pixels moved per frame.
		GAME_WIDTH = 1020,
		GAME_HEIGHT = 574;
	
	boolean remove = false;
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
	
	public int getHeight()
	{
		return height;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public boolean checkRemove()
	{
		return remove;
	}
}