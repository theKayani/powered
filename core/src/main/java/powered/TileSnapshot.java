package powered;

import com.hk.game.pieces.Piece;
import com.hk.json.JsonObject;

import java.util.Objects;

public class TileSnapshot
{
	public final Piece piece;
	public final int meta;
	public final boolean hasData;
	public final JsonObject data;

	TileSnapshot(Piece piece, int meta, boolean hasData, JsonObject data)
	{
		this.piece = piece;
		this.meta = meta;
		this.hasData = hasData;
		this.data = data;
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
	public int hashCode() {
		return Objects.hash(piece, meta, hasData, data);
	}

	@Override
	public String toString() {
		return "TileSnapshot{" +
				"piece=" + piece +
				", meta=" + meta +
				", hasData=" + hasData +
				", data=" + data +
				'}';
	}
}
