package powered;

import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;
import com.hk.math.vector.Point;

import java.util.*;

public class World
{
	public final Powered game;
	public final int width, height;
	private final Tile[][] grid;
	private final Set<ToUpdate> updates;
	private List<ToUpdate> currentUpdates;
	private final Map<Point, Object> tickers;

	public World(Powered game, int width, int height)
	{
		this.game = game;
		this.width = width;
		this.height = height;
		updates = new LinkedHashSet<>();
		tickers = new HashMap<>();
		grid = new Tile[width][height];
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				grid[x][y] = new Tile(this, x, y);
			}
		}

		for(int x = 5; x < 25; x++)
		{
			for(int y = 5; y < 25; y++)
			{
				if(y == 15 && x <= 12)
					continue;
				setPiece(x, y, Pieces.WIRE, true);
			}
		}

		for(int x = 5; x < 13; x++)
			setPiece(x, 15, Pieces.STRAIGHT_WIRE, 0, true);

		do {
			updateWorld();
		} while(!updates.isEmpty());

		setPiece(4, 15, Pieces.SWITCH, 1, true);
	}
	
	public void updateWorld()
	{
		currentUpdates = new ArrayList<>(updates);
		updates.clear();

		for (int i = 0; i < currentUpdates.size(); i++)
		{
			ToUpdate next = currentUpdates.get(i);
			if(inBounds(next.x, next.y))
				grid[next.x][next.y].getPiece().onNeighborChanged(this, next.x, next.y, next.side);
		}
		currentUpdates = null;

		Set<Map.Entry<Point, Object>> entrySet = tickers.entrySet();

		Iterator<Map.Entry<Point, Object>> itr = entrySet.iterator();
		while (itr.hasNext())
		{
			Map.Entry<Point, Object> entry = itr.next();
			Point p = entry.getKey();

			Object obj1 = entry.getValue();
			Object obj2 = grid[p.x][p.y].getData();
			if (Objects.equals(obj1, obj2))
				((Ticker) obj1).onUpdate(this, p.x, p.y);
			else
				itr.remove();
		}
	}

	public Piece getPiece(int x, int y)
	{
		if(!inBounds(x, y))
			return Pieces.AIR;
		return grid[x][y].getPiece();
	}
	
	public int getMeta(int x, int y)
	{
		if(!inBounds(x, y))
			return 0;
		return grid[x][y].getMeta();
	}

	public <T extends TileData> T getData(int x, int y, Class<T> cls)
	{
		if(!inBounds(x, y))
			return null;
		TileData o = grid[x][y].getData();
		return o == null ? null : cls.cast(o);
	}

	public TileData getData(int x, int y)
	{
		if(!inBounds(x, y))
			return null;
		return grid[x][y].getData();
	}
	
	public World setPiece(int x, int y, Piece p, int meta, int flags)
	{
		if(inBounds(x, y))
		{
			boolean notify = (flags & 1) != 0;
			boolean replace = (flags & 2) != 0;
			Piece old = grid[x][y].getPiece();
			if(p != old)
			{
				grid[x][y].initialize(p, meta);
				if(!replace)
				{
					old.onRemove(this, x, y);
					if (p.onAdded(this, x, y) && notify)
						notifyNeighbors(x, y);
				}
			}
		}
		return this;
	}

	public World setPiece(int x, int y, Piece p, int meta, boolean notify)
	{
		return setPiece(x, y, p, meta, 1);
	}

	public World setPiece(int x, int y, Piece p, boolean notify)
	{
		return setPiece(x, y, p, p.getDefaultMeta(), notify);
	}
	
	public World setMeta(int x, int y, int meta)
	{
		if(inBounds(x, y))
		{
			int old = grid[x][y].getMeta();
			grid[x][y].setMeta(meta);
			grid[x][y].getPiece().onMetaChanged(this, x, y, old, meta);
		}
		return this;
	}
	
	public World setData(int x, int y, TileData data)
	{
		if(inBounds(x, y))
		{
			TileData old = grid[x][y].getData();
			grid[x][y].setData(data);
			grid[x][y].getPiece().onDataChanged(this, x, y, old, data);
		}
		return this;
	}

	public Tile grid(int x, int y)
	{
		return grid[x][y];
	}

	public void addTicker(int x, int y, Ticker ticker)
	{
		tickers.put(new Point(x, y), ticker);
	}
	
	public boolean inBounds(int x, int y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}
	
	public boolean isAir(int x, int y)
	{
		return getPiece(x, y) == Pieces.AIR;
	}
	
	public World setToAir(int x, int y, boolean notify)
	{
		return setPiece(x, y, Pieces.AIR, notify);
	}
	
	public boolean isPowered(int x, int y)
	{
		return getPowerTo(x, y) > 0;
	}

	public int powerProvided(int x, int y, Side to)
	{
		return getPiece(x, y).powerProvided(this, x, y, to);
	}
	
	public int getPowerTo(int x, int y)
	{
		int pwr = 0;
		for(int i = 0; i < Side.size(); i++)
		{
			Side side = Side.get(i);
			Piece p = getPiece(x + side.xOff, y + side.yOff);
			if(p.canTransfer(this, x + side.xOff, y + side.yOff, side.getOpposite()))
				pwr = Math.max(pwr, powerProvided(x + side.xOff, y + side.yOff, side.getOpposite()));
		}
		return pwr & 0xF;
	}

	public void notifyNeighbors(int x, int y)
	{
		for(Side side : Side.values())
			notifyNeighbor(x, y, side);
	}

	public void notifyNeighbor(int x, int y, Side side, boolean check)
	{
		if(!check || !isAir(x + side.xOff, y + side.yOff))
			updates.add(new ToUpdate(x + side.xOff, y + side.yOff, side.getOpposite()));
	}

	public void notifyNeighbor(int x, int y, Side side)
	{
		notifyNeighbor(x, y, side, true);
	}

	public void directNotify(int x, int y, Side side)
	{
		if(currentUpdates == null)
			notifyNeighbor(x, y, side);
		else if(!isAir(x + side.xOff, y + side.yOff))
		{
			ToUpdate update = new ToUpdate(x + side.xOff, y + side.yOff, side.getOpposite());
			if(!currentUpdates.contains(update))
				currentUpdates.add(update);
		}
	}

	private static class ToUpdate
	{
		private final int x, y;
		private final Side side;

		private ToUpdate(int x, int y, Side side)
		{
			this.x = x;
			this.y = y;
			this.side = side;
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof ToUpdate &&
					x == ((ToUpdate) o).x &&
					y == ((ToUpdate) o).y &&
					side == ((ToUpdate) o).side;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(x, y, side);
		}

		@Override
		public String toString()
		{
			return "ToUpdate{x=" + x + ", y=" + y + ", side=" + side + '}';
		}
	}

	public interface Ticker
	{
		void onUpdate(World world, int x, int y);
	}
}
