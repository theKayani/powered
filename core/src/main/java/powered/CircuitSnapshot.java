package powered;

import com.hk.game.pieces.Circuit;
import com.hk.math.vector.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CircuitSnapshot
{
	public final int xCoord, yCoord, width, height;
	public final Map<Point, Point> worldConnects = new HashMap<>();
	public final Blueprint circuitWorld;

	public CircuitSnapshot(int xCoord, int yCoord, int width, int height, Blueprint circuitWorld)
	{
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.width = width;
		this.height = height;
		this.circuitWorld = circuitWorld;
	}

	public CircuitSnapshot(Circuit circuit, int xOffset, int yOffset)
	{
		xCoord = circuit.xCoord - xOffset;
		yCoord = circuit.yCoord - yOffset;
		width = circuit.width;
		height = circuit.height;
		Set<World.WorldPoint> connects = circuit.getWorldConnects();
		for (World.WorldPoint worldPoint : connects)
		{
			World.WorldPoint edgePoint = circuit.getWorldConnect(worldPoint.x, worldPoint.y);
			Point wp = new Point(worldPoint.x - circuit.xCoord, worldPoint.y - circuit.yCoord);
			Point ep = new Point(edgePoint.x, edgePoint.y);
			worldConnects.put(wp, ep);
		}
		World circuitWorld = circuit.getCircuitWorld();
		this.circuitWorld = new Blueprint(circuitWorld, 0, 0, circuitWorld.width - 1, circuitWorld.height - 1);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CircuitSnapshot that = (CircuitSnapshot) o;
		return xCoord == that.xCoord && yCoord == that.yCoord && width == that.width && height == that.height && worldConnects.equals(that.worldConnects) && circuitWorld.equals(that.circuitWorld);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(xCoord, yCoord, width, height, worldConnects, circuitWorld);
	}

	@Override
	public String toString() {
		return "CircuitSnapshot{" +
				"xCoord=" + xCoord +
				", yCoord=" + yCoord +
				", width=" + width +
				", height=" + height +
				", worldConnects=" + worldConnects +
				", circuitWorld=" + circuitWorld +
				'}';
	}
}
