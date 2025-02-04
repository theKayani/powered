package powered;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;
import com.hk.json.Json;
import com.hk.json.JsonArray;
import com.hk.json.JsonObject;
import com.hk.math.vector.Point;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Blueprint
{
	public final int width, height;
	private final Map<Point, TileSnapshot> data;
	private final TileSnapshot[][] grid;

	public Blueprint(World world, int minX, int minY, int maxX, int maxY)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);
		width = maxx - minx + 1;
		height = maxy - miny + 1;
		grid = new TileSnapshot[width][height];
		data = copyRegion(world, minx, miny, maxx, maxy, grid);
	}

	private Blueprint(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.data = new HashMap<>();
		grid = new TileSnapshot[width][height];
	}

	public void paintBlueprint(G2D g2d, int x, int y)
	{
		g2d.pushMatrix();
		g2d.setColor(Color.RED);
		g2d.translate(x, y);
		for(int xc = 0; xc < width; xc++)
		{
			for(int yc = 0; yc < height; yc++)
			{
				TileSnapshot snapshot = grid[xc][yc];
				if(snapshot == null)
					continue;
				Piece p = snapshot.piece;
				if(p.isAir())
					continue;
				int meta = snapshot.meta;
				p.paintPiece(g2d, null, xc * 10, yc * 10, xc, yc, meta, false);
			}
		}
		g2d.popMatrix();
	}

	public JsonObject toJson()
	{
		JsonObject obj = new JsonObject();
		obj.put("width", width);
		obj.put("height", height);
		JsonArray tiles = new JsonArray();

		Set<Map.Entry<Point, TileSnapshot>> entries = data.entrySet();
		for (Map.Entry<Point, TileSnapshot> entry : entries)
		{
			Point point = entry.getKey();
			TileSnapshot datum = entry.getValue();
			JsonObject tile = new JsonObject();
			tile.put("x", point.x);
			tile.put("y", point.y);
			tile.put("piece", datum.piece.getShortName());
			if(datum.meta != datum.piece.getDefaultMeta())
				tile.put("meta", datum.meta);
			if(datum.hasData)
			{
				if(datum.data == null)
					tile.put("data", true);
				else
					tile.put("data", datum.data);
			}
			tiles.add(tile);
		}

		obj.put("tiles", tiles);
		return obj;
	}

	public static Blueprint fromJson(JsonObject json)
	{
		System.out.println(json);
		int width = json.getInt("width");
		int height = json.getInt("height");
		JsonArray tiles = json.getArray("tiles");
		Blueprint blueprint = new Blueprint(width, height);

		for (int i = 0; i < tiles.size(); i++)
		{
			JsonObject tile = tiles.get(i).getObject();
			Point point = new Point(tile.getInt("x"), tile.getInt("y"));
			Piece piece = Pieces.fromShortName(tile.getString("piece"));
			int meta;
			if (tile.isNumber("meta"))
				meta = tile.getInt("meta");
			else
				meta = piece.getDefaultMeta();
			boolean hasData = false;
			JsonObject data = null;
			if(tile.isBoolean("data"))
			{
				hasData = tile.getBoolean("data");
			}
			else if(tile.isObject("data"))
			{
				hasData = true;
				data = tile.getObject("data");
			}
			TileSnapshot snapshot = new TileSnapshot(piece, meta, hasData, data);
			blueprint.grid[point.x][point.y] = snapshot;
			blueprint.data.put(point, snapshot);
		}

		return blueprint;
	}

	private static Map<Point, TileSnapshot> copyRegion(World world, int minX, int minY, int maxX, int maxY, TileSnapshot[][] grid)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);

		Map<Point, TileSnapshot> map = new HashMap<>();
		for (int cy = miny; cy <= maxy; cy++)
		{
			for (int cx = minx; cx <= maxx; cx++)
			{
				Piece piece = world.getPiece(cx, cy);
				if(piece.isAir())
					continue;

				int meta = piece.cleanMeta(world.getMeta(cx, cy));
				TileData data = world.getData(cx, cy, TileData.class);
				JsonObject obj = data == null ? null : data.saveToJson();
				Point point = new Point(cx - minx, cy - miny);
				TileSnapshot snapshot = new TileSnapshot(piece, meta, data != null, obj);
				map.put(point, snapshot);
				if(grid != null)
					grid[point.x][point.y] = snapshot;
			}
		}
		return map;
	}

	public static JsonObject importString(String encoded)
	{
		byte[] bytes = new BigInteger(encoded, 36).toByteArray();
		return Json.read(new String(bytes, StandardCharsets.UTF_8)).getObject();
	}

	public static String exportString(JsonObject obj)
	{
		byte[] bytes = Json.write(obj).getBytes(StandardCharsets.UTF_8);
		return new BigInteger(1, bytes).toString(36);
	}

	public TileSnapshot grid(int x, int y)
	{
		return grid[x][y];
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Blueprint blueprint = (Blueprint) o;
		return width == blueprint.width && height == blueprint.height && data.equals(blueprint.data);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(width, height, data);
	}

	public static class TileSnapshot
	{
		private final Piece piece;
		private final int meta;
		private final boolean hasData;
		private final JsonObject data;

		private TileSnapshot(Piece piece, int meta, boolean hasData, JsonObject data)
		{
			this.piece = piece;
			this.meta = meta;
			this.hasData = hasData;
			this.data = data;
		}

		public Piece getPiece()
		{
			return piece;
		}

		public int getMeta()
		{
			return meta;
		}

		public boolean hasData()
		{
			return hasData;
		}

		public JsonObject getData()
		{
			return data;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TileSnapshot that = (TileSnapshot) o;
			return meta == that.meta && hasData == that.hasData && piece.equals(that.piece) && Objects.equals(data, that.data);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(piece, meta, hasData, data);
		}
	}
}
