package powered;

public enum Side
{
	NORTH(0, -1),
	SOUTH(0, 1),
	WEST(-1, 0),
	EAST(1, 0);
	
	public final int xOff, yOff;
	
	private Side(int xOff, int yOff)
	{
		this.xOff = xOff;
		this.yOff = yOff;
	}

	public boolean isVertical()
	{
		switch (this)
		{
			case NORTH:
			case SOUTH:
				return true;
			case WEST:
			case EAST:
				return false;
		}
		throw new AssertionError(this);
	}
	
	public Side getOpposite()
	{
		switch(this)
		{
			case NORTH: return SOUTH;
			case SOUTH: return NORTH;
			case WEST: return EAST;
			case EAST: return WEST;
		}
		throw new AssertionError(this);
	}

	public Side rotate90()
	{
		switch(this)
		{
			case NORTH: return EAST;
			case SOUTH: return WEST;
			case WEST: return NORTH;
			case EAST: return SOUTH;
		}
		throw new AssertionError(this);
	}

	public Side rotateCounter90()
	{
		switch(this)
		{
			case NORTH: return WEST;
			case SOUTH: return EAST;
			case WEST: return SOUTH;
			case EAST: return NORTH;
		}
		throw new AssertionError(this);
	}
	
	public static Side get(int index)
	{
		return values()[index];
	}
	
	public static int size()
	{
		return values().length;
	}

	public static final Side[] HORIZONTAL = { WEST, EAST };
	public static final Side[] VERTICAL = { NORTH, SOUTH };
	public static final Side[][] CORNERS = { { NORTH, EAST }, { SOUTH, WEST }, { WEST, NORTH }, { EAST, SOUTH } };
}
