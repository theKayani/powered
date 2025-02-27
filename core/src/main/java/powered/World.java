package powered;

import com.hk.game.pieces.Circuit;
import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class World
{
	public final Powered game;
	public final int width, height;
	public final World rootParent, parent;
	public final Circuit circuit;
	private int ticks;
	private final Tile[][] grid;
	private final Collection<WorldPoint> updates;
	private final Map<WorldPoint, TileData> tickers;
	private boolean updating = false;
	private boolean enabled;
	private boolean pasting = false;

	public World(Powered game, int width, int height, Circuit circuit)
	{
		this.game = game;
		this.width = width;
		this.height = height;
		grid = new Tile[width][height];
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				grid[x][y] = new Tile(this, x, y);
			}
		}
		enabled = true;

		if (circuit == null)
		{
			updates = new LinkedHashSet<>();
			tickers = new LinkedHashMap<>();
			this.rootParent = this.parent = null;
			this.circuit = null;
		}
		else
		{
			this.circuit = Objects.requireNonNull(circuit);
			this.parent = this.circuit.world;
			this.rootParent = parent.parent == null ? parent : parent.rootParent;
			updates = parent.updates;
			tickers = parent.tickers;
		}
	}
	
	@SuppressWarnings("ForLoopReplaceableByForEach")
	public void updateWorld()
	{
		if(hasParent())
			throw new IllegalStateException("unexpected update to child world");
		if(isPasting())
			throw new IllegalStateException("unexpecting pasting state?");

		updating = true;
		ticks++;
		List<WorldPoint> currentUpdates = new ArrayList<>(updates);
		updates.clear();

		for (int i = 0; i < currentUpdates.size(); i++)
		{
			WorldPoint next = currentUpdates.get(i);
			if (next.world.enabled && next.world.inBounds(next.x, next.y))
				next.world.grid[next.x][next.y].getPiece().onNeighborChanged(next.world, next.x, next.y, next.side);
		}
		updating = false;

		Set<Map.Entry<WorldPoint, TileData>> entrySet = tickers.entrySet();

		Iterator<Map.Entry<WorldPoint, TileData>> itr = entrySet.iterator();
		while (itr.hasNext())
		{
			Map.Entry<WorldPoint, TileData> entry = itr.next();
			WorldPoint p = entry.getKey();
			TileData tileData = entry.getValue();
			if (tileData != null && Objects.equals(tileData, p.world.grid[p.x][p.y].getData()))
				((Ticker) tileData).onUpdate();
			else
				itr.remove();
		}
	}

	public boolean hasParent()
	{
		return parent != null;
	}

	public World getParent()
	{
		return parent;
	}

	public World getRootParent()
	{
		return rootParent;
	}

	public boolean isUpdating()
	{
		return rootParent != null ? rootParent.updating : updating;
	}

	public boolean isPasting()
	{
		return pasting;
	}

	public void setPasting(boolean pasting)
	{
		this.pasting = pasting;
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
			if(p != old || meta != grid[x][y].getMeta())
			{
				old.onRemove(this, x, y);
				grid[x][y].initialize(p, meta);
				if(!replace)
				{
					if (p.onAdded(this, x, y) && notify)
						notifyNeighbors(x, y);
				}
			}
		}
		return this;
	}

	public World setPiece(int x, int y, Piece p, int meta, boolean notify)
	{
		return setPiece(x, y, p, meta, notify ? 1 : 0);
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

	public World createCircuit(int minX, int minY, int maxX, int maxY, int worldWidth, int worldHeight)
	{
		List<Side> notifies = new ArrayList<>(4);
		for (int cx = minX; cx <= maxX; cx++)
		{
			for (int cy = minY; cy <= maxY; cy++)
			{
				int meta = 0;

				if(cy == minY)
				{
					meta |= 0x1;
					notifies.add(Side.NORTH);
				}
				if(cy == maxY)
				{
					meta |= 0x4;
					notifies.add(Side.SOUTH);
				}
				if(cx == minX)
				{
					meta |= 0x10;
					notifies.add(Side.WEST);
				}
				if(cx == maxX)
				{
					meta |= 0x40;
					notifies.add(Side.EAST);
				}

				setPiece(cx, cy, Pieces.CIRCUIT, meta, false);

				for (Side side : notifies)
					notifyNeighbor(cx, cy, side);

				notifies.clear();
			}
		}


		int width = Math.abs(maxX - minX + 1);
		int height = Math.abs(maxY - minY + 1);
		Circuit circuit = new Circuit(this, minX, minY, width, height);
		for (int cx = minX; cx <= maxX; cx++)
		{
			for (int cy = minY; cy <= maxY; cy++)
			{
				setData(cx, cy, circuit);
			}
		}
		circuit.initializeWorld(worldWidth, worldHeight);

		return this;
	}

	public Tile grid(int x, int y)
	{
		return grid[x][y];
	}

	public int updates()
	{
		return updates.size();
	}

	public int getTicks()
	{
		return ticks;
	}

	public void addTicker(int x, int y, TileData ticker)
	{
		tickers.put(new WorldPoint(this, x, y, null), ticker);
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

	@SuppressWarnings("ConstantConditions")
	public int powerProvided(int x, int y, Side to)
	{
		if(hasParent() && isOnEdge(x, y))
			return circuit.getEdgePower(x, y);
		else
			return getPiece(x, y).powerProvided(this, x, y, to);
	}

	@SuppressWarnings("ConstantConditions")
	public boolean canTransfer(int x, int y, Side to)
	{
		if(hasParent() && isOnEdge(x, y))
			return circuit.isEdgeConnected(x, y);
		else
			return getPiece(x, y).canTransfer(this, x, y, to);
	}
	
	public int getPowerTo(int x, int y)
	{
		int pwr = 0;
		for(int i = 0; i < Side.size(); i++)
		{
			Side side = Side.get(i);
			if(canTransfer(x + side.xOff, y + side.yOff, side.getOpposite()))
				pwr = Math.max(pwr, powerProvided(x + side.xOff, y + side.yOff, side.getOpposite()));
		}
		return pwr & 0xF;
	}

	public boolean isOnEdge(int selX, int selY)
	{
		if(selX == -1 || selX == width)
			return selY >= 0 && selY < height;
		if(selY == -1 || selY == height)
			return selX >= 0 && selX < width;
		return false;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	private void addUpdate(WorldPoint update)
	{
		updates.add(update);
	}

	@SuppressWarnings("ConstantConditions")
	public void notifyNeighbor(int x, int y, Side side, boolean checkAir, boolean checkCircuit)
	{
		Piece piece = getPiece(x + side.xOff, y + side.yOff);
		if(hasParent() && isOnEdge(x + side.xOff, y + side.yOff))
			circuit.onEdgeChanged(x, y, side);
		else if(checkCircuit && piece.isCircuit())
			piece.onNeighborChanged(this, x + side.xOff, y + side.yOff, side.getOpposite());
		else if(!checkAir || !piece.isAir())
			addUpdate(new WorldPoint(this, x + side.xOff, y + side.yOff, side.getOpposite()));
	}

	public void notifyNeighbor(int x, int y, Side side, boolean checkAir)
	{
		notifyNeighbor(x, y, side, checkAir, !pasting);
	}

	public void notifyNeighbors(int x, int y)
	{
		for(Side side : Side.values())
			notifyNeighbor(x, y, side);
	}

	public void notifyNeighbor(int x, int y, Side side)
	{
		notifyNeighbor(x, y, side, true);
	}

	public void notifyNeighbors(int minX, int minY, int maxX, int maxY)
	{
		for (int cx = minX; cx <= maxX; cx++)
		{
			notifyNeighbor(cx, minY, Side.NORTH);
			notifyNeighbor(cx, maxY, Side.SOUTH);
		}
		for (int cy = minY; cy <= maxY; cy++)
		{
			notifyNeighbor(minX, cy, Side.WEST);
			notifyNeighbor(maxX, cy, Side.EAST);
		}
	}

	public static class WorldPoint
	{
		public final World world;
		public final int x, y;
		public final Side side;

		public WorldPoint(World world, int x, int y)
		{
			this(world, x, y, null);
		}

		public WorldPoint(World world, int x, int y, Side side)
		{
			this.world = world;
			this.x = x;
			this.y = y;
			this.side = side;
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof WorldPoint &&
					world == ((WorldPoint) o).world &&
					x == ((WorldPoint) o).x &&
					y == ((WorldPoint) o).y &&
					side == ((WorldPoint) o).side;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(world, x, y, side);
		}

		@Override
		public String toString()
		{
			return "WorldPoint{world=" + world + ", x=" + x + ", y=" + y + ", side=" + side + '}';
		}
	}

	public interface Ticker
	{
		void onUpdate();
	}
}
