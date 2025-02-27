package powered;

import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;

import java.util.Objects;

public class Tile
{
	public final World world;
	public final int x, y;
	private Piece piece;
	private int meta;
	private TileData data;

	public Tile(World world, int x, int y)
	{
		this.world = world;
		this.x = x;
		this.y = y;

		piece = Pieces.AIR;
		meta = 0;
		data = null;
	}

	public void initialize(Piece piece, int meta)
	{
		this.piece = piece;
		this.meta = meta;
		this.data = piece.createData(world, x, y, meta);
		if(data instanceof World.Ticker)
			world.addTicker(x, y, data);
	}

	public void setPiece(Piece piece)
	{
		this.piece = piece;
	}

	public Piece getPiece()
	{
		return piece;
	}

	public void setMeta(int meta)
	{
		this.meta = meta;
	}

	public int getMeta()
	{
		return meta;
	}

	public void setData(TileData data)
	{
		if(!Objects.equals(this.data, data))
		{
			this.data = data;
			if(data instanceof World.Ticker)
				world.addTicker(x, y, data);
		}
	}

	public TileData getData()
	{
		return data;
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof Tile &&
				x == ((Tile) o).x &&
				y == ((Tile) o).y;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(x, y);
	}
}
