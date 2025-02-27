package powered;

import com.hk.json.JsonObject;

import java.util.Objects;

public class TileData
{
	public final World world;
	public final int xCoord, yCoord;

	public TileData(World world, int xCoord, int yCoord)
	{
		this.world = Objects.requireNonNull(world);
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	public void loadFromJson(JsonObject obj)
	{}

	public JsonObject saveToJson()
	{
		return null;
	}
}
