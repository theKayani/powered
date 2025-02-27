package com.hk.game.pieces;

import com.hk.math.MathUtil;
import powered.Side;
import powered.TileData;
import powered.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Circuit extends TileData
{
	public final int width, height;
	private World circuitWorld;
	private boolean deleting = false;
	private final Map<World.WorldPoint, World.WorldPoint> edgeToWorld, worldToEdge;

	public Circuit(World world, int xCoord, int yCoord, int width, int height)
	{
		super(world, xCoord, yCoord);
		this.width = width;
		this.height = height;

		edgeToWorld = new HashMap<>();
		worldToEdge = new HashMap<>();
	}

	public void initializeWorld(int worldWidth, int worldHeight)
	{
		if(circuitWorld != null)
			throw new IllegalStateException("world has already been initialized");

		circuitWorld = new World(world.game, worldWidth, worldHeight, this);
	}

	public World getCircuitWorld()
	{
		return circuitWorld;
	}

	public boolean contains(int x, int y)
	{
		return x >= xCoord && y >= yCoord && x < xCoord + width && y < yCoord + height;
	}

	public boolean isOutBounds(int minX, int minY, int maxX, int maxY)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);
		return minx > xCoord || miny > yCoord || maxx < xCoord + width - 1 || maxy < yCoord + height - 1;
	}

	public boolean isOnEdge(int worldX, int worldY)
	{
		int maxX = xCoord + width - 1;
		int maxY = yCoord + height - 1;

		if(worldX == xCoord - 1 || worldX == maxX + 1)
			return worldY >= yCoord && worldY <= maxY;
		if(worldY == yCoord - 1 || worldY == maxY + 1)
			return worldX >= xCoord && worldX <= maxX;
		return false;
	}

	public void onWorldChanged(int x, int y, Side side)
	{
		if(isWorldConnected(x + side.xOff, y + side.yOff))
		{
			World.WorldPoint worldPoint = new World.WorldPoint(world, x + side.xOff, y + side.yOff);
			World.WorldPoint edgePoint = worldToEdge.get(worldPoint);
			Side edgeSide = getEdgeSide(edgePoint.x, edgePoint.y);
			circuitWorld.notifyNeighbor(edgePoint.x, edgePoint.y, edgeSide.getOpposite());

			Side worldSide = getWorldSide(worldPoint.x, worldPoint.y);
			int pwrIn = world.powerProvided(worldPoint.x, worldPoint.y, worldSide.getOpposite()) & 0xF;
			int pwrOut = circuitWorld.powerProvided(edgePoint.x - edgeSide.xOff, edgePoint.y - edgeSide.yOff, edgeSide) & 0xF;
			int pwr = Math.max(pwrIn, pwrOut);

			int meta = world.getMeta(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff);
			int oldMeta = meta;
			meta &= ~(0b1111 << 4 * worldSide.ordinal() + 8);
			meta |= pwr << 4 * worldSide.ordinal() + 8;

			if(meta != oldMeta)
				world.setMeta(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, meta);
		}
	}

	public void onEdgeChanged(int x, int y, Side side)
	{
		if(isEdgeConnected(x + side.xOff, y + side.yOff))
		{
			World.WorldPoint edgePoint = new World.WorldPoint(circuitWorld, x + side.xOff, y + side.yOff);
			World.WorldPoint worldPoint = edgeToWorld.get(edgePoint);
			Side worldSide = getWorldSide(worldPoint.x, worldPoint.y);
			world.notifyNeighbor(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, worldSide);

			Side edgeSide = getEdgeSide(edgePoint.x, edgePoint.y);
			int pwrIn = world.powerProvided(worldPoint.x, worldPoint.y, worldSide.getOpposite()) & 0xF;
			int pwrOut = circuitWorld.powerProvided(edgePoint.x - edgeSide.xOff, edgePoint.y - edgeSide.yOff, edgeSide) & 0xF;
			int pwr = Math.max(pwrIn, pwrOut);

			int meta = world.getMeta(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff);
			int oldMeta = meta;
			meta &= ~(0b1111 << 4 * worldSide.ordinal() + 8);
			meta |= pwr << 4 * worldSide.ordinal() + 8;

			if(meta != oldMeta)
				world.setMeta(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, meta);
		}
	}

	public int getEdgePower(int edgeX, int edgeY)
	{
		if(isEdgeConnected(edgeX, edgeY))
		{
			World.WorldPoint worldPoint = getEdgeConnect(edgeX, edgeY);
			Side worldSide = getWorldSide(worldPoint.x, worldPoint.y);
			int pwr = world.powerProvided(worldPoint.x, worldPoint.y, worldSide.getOpposite());
			return Math.max(pwr - 1, 0);
//			return pwr;
		}
		else
			return 0;
	}

	public int getWorldPower(int worldX, int worldY)
	{
		if(isWorldConnected(worldX, worldY))
		{
			World.WorldPoint edgePoint = getWorldConnect(worldX, worldY);
			Side edgeSide = getEdgeSide(edgePoint.x, edgePoint.y);
			int pwr = circuitWorld.powerProvided(edgePoint.x - edgeSide.xOff, edgePoint.y - edgeSide.yOff, edgeSide);
			return Math.max(pwr - 1, 0);
//			return pwr;
		}
		else
			return 0;
	}

	public void connect(int edgeX, int edgeY, int worldX, int worldY)
	{
		connect(edgeX, edgeY, worldX, worldY, true);
	}

	public void connect(int edgeX, int edgeY, int worldX, int worldY, boolean doIO)
	{
		checkEdge(edgeX, edgeY);
		checkWorld(worldX, worldY);

		if(isEdgeConnected(edgeX, edgeY))
			throw new UnsupportedOperationException("edge already has a connection");
		if(isWorldConnected(worldX, worldY))
			throw new UnsupportedOperationException("world already has a connection");

		World.WorldPoint edgePoint = new World.WorldPoint(circuitWorld, edgeX, edgeY);
		World.WorldPoint worldPoint = new World.WorldPoint(world, worldX, worldY);
		edgeToWorld.put(edgePoint, worldPoint);
		worldToEdge.put(worldPoint, edgePoint);

		Side edgeSide = getEdgeSide(edgeX, edgeY);
		Side worldSide = getWorldSide(worldX, worldY);
		circuitWorld.notifyNeighbor(edgePoint.x, edgePoint.y, edgeSide.getOpposite());
		world.notifyNeighbor(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, worldSide);

		if(doIO)
			setCircuitIO(worldX, worldY, true);
	}

	public void disconnectEdge(int edgeX, int edgeY)
	{
		disconnectEdge(edgeX, edgeY, true);
	}

	public void disconnectEdge(int edgeX, int edgeY, boolean doIO)
	{
		checkEdge(edgeX, edgeY);
		World.WorldPoint edgePoint = new World.WorldPoint(circuitWorld, edgeX, edgeY);
		World.WorldPoint worldPoint = edgeToWorld.get(edgePoint);
		if(worldPoint != null)
		{
			edgeToWorld.remove(edgePoint);
			worldToEdge.remove(worldPoint);
			if(doIO)
				setCircuitIO(worldPoint.x, worldPoint.y, false);

			Side edgeSide = getEdgeSide(edgeX, edgeY);
			Side worldSide = getWorldSide(worldPoint.x, worldPoint.y);
			circuitWorld.notifyNeighbor(edgePoint.x, edgePoint.y, edgeSide.getOpposite());
			world.notifyNeighbor(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, worldSide);
		}
	}

	public void disconnectWorld(int worldX, int worldY)
	{
		disconnectWorld(worldX, worldY, true);
	}

	public void disconnectWorld(int worldX, int worldY, boolean doIO)
	{
		checkWorld(worldX, worldY);
		World.WorldPoint worldPoint = new World.WorldPoint(world, worldX, worldY);
		World.WorldPoint edgePoint = worldToEdge.get(worldPoint);
		if(edgePoint != null)
		{
			worldToEdge.remove(worldPoint);
			edgeToWorld.remove(edgePoint);
			if(doIO)
				setCircuitIO(worldX, worldY, false);

			Side worldSide = getWorldSide(worldX, worldY);
			Side edgeSide = getEdgeSide(edgePoint.x, edgePoint.y);
			world.notifyNeighbor(worldPoint.x - worldSide.xOff, worldPoint.y - worldSide.yOff, worldSide);
			circuitWorld.notifyNeighbor(edgePoint.x, edgePoint.y, edgeSide.getOpposite());
		}
	}

	public World.WorldPoint getEdgeConnect(int edgeX, int edgeY)
	{
		checkEdge(edgeX, edgeY);
		return edgeToWorld.get(new World.WorldPoint(circuitWorld, edgeX, edgeY));
	}

	public Set<World.WorldPoint> getEdgeConnects()
	{
		return edgeToWorld.keySet();
	}

	public World.WorldPoint getWorldConnect(int worldX, int worldY)
	{
		checkWorld(worldX, worldY);
		return worldToEdge.get(new World.WorldPoint(world, worldX, worldY));
	}

	public Set<World.WorldPoint> getWorldConnects()
	{
		return worldToEdge.keySet();
	}

	public boolean isEdgeConnected(int edgeX, int edgeY)
	{
		return getEdgeConnect(edgeX, edgeY) != null;
	}

	public boolean isWorldConnected(int worldX, int worldY)
	{
		return getWorldConnect(worldX, worldY) != null;
	}

	public Side getEdgeSide(int edgeX, int edgeY)
	{
		edgeX = MathUtil.between(-1, edgeX, circuitWorld.width);
		edgeY = MathUtil.between(-1, edgeY, circuitWorld.height);
		checkEdge(edgeX, edgeY);

		if(edgeX == -1)
			return Side.WEST;
		if(edgeX == circuitWorld.width)
			return Side.EAST;
		if(edgeY == -1)
			return Side.NORTH;
		if(edgeY == circuitWorld.height)
			return Side.SOUTH;

		throw new AssertionError("unexpected");
	}

	public Side getWorldSide(int worldX, int worldY)
	{
		worldX = MathUtil.between(xCoord - 1, worldX, xCoord + width);
		worldY = MathUtil.between(yCoord - 1, worldY, yCoord + height);
		checkWorld(worldX, worldY);

		if(worldX == xCoord - 1)
			return Side.WEST;
		if(worldX == xCoord + width)
			return Side.EAST;
		if(worldY == yCoord - 1)
			return Side.NORTH;
		if(worldY == yCoord + height)
			return Side.SOUTH;

		throw new AssertionError("unexpected");
	}

	private void checkEdge(int edgeX, int edgeY)
	{
		if (!circuitWorld.isOnEdge(edgeX, edgeY))
			throw new IllegalArgumentException("edgeX and edgeY should be on the edge of the circuit");
	}

	private void checkWorld(int worldX, int worldY)
	{
		if (!isOnEdge(worldX, worldY))
			throw new IllegalArgumentException("worldX and worldY should be on the edge of the circuit");
	}

	private void setCircuitIO(int worldX, int worldY, boolean enabled)
	{
		checkWorld(worldX, worldY);
		Side side = getWorldSide(worldX, worldY);

		int cx = worldX - side.xOff;
		int cy = worldY - side.yOff;

		int pieceMeta = world.getMeta(cx, cy);
		pieceMeta &= ~(0x3 << (side.ordinal() * 2));

		if (enabled)
			pieceMeta |= (world.canTransfer(worldX, worldY, side.getOpposite()) ? 3 : 2) << (side.ordinal() * 2);
		else
			pieceMeta |= 1 << (side.ordinal() * 2);

		world.setMeta(cx, cy, pieceMeta);
		world.notifyNeighbor(cx, cy, side);
	}

	public void delete()
	{
		if(deleting)
			return;
		deleting = true;

		for (int cx = xCoord; cx < xCoord + width; cx++)
		{
			for (int cy = yCoord; cy < yCoord + height; cy++)
				world.setToAir(cx, cy, false);
		}

		worldToEdge.clear();
		edgeToWorld.clear();
		world.notifyNeighbors(xCoord, yCoord, xCoord + width - 1, yCoord + height - 1);
		circuitWorld.setEnabled(false);
		circuitWorld = null;
	}
}
