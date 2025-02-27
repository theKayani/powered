package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.World;

public class DiodePiece extends Piece
{
	protected DiodePiece()
	{
		super("Diode");
	}

	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		Side orientation = Side.get((meta >> 4) & 0x3);
		int pwr = meta & 0xF;
		g2d.begin(true);
		g2d.setColor(Color.BLACK);
		int[] tri = tris[orientation.ordinal()];
		g2d.drawTriangle(x + tri[0], y + tri[1], x + tri[2], y + tri[3], x + tri[4], y + tri[5]);
		g2d.setColor(WirePiece.clrs[pwr]);
		switch (orientation)
		{
			case NORTH:
				g2d.drawRect(x + 2.5F, y, 5, 1);
				break;
			case SOUTH:
				g2d.drawRect(x + 2.5F, y + 9, 5, 1);
				break;
			case WEST:
				g2d.drawRect(x, y + 2.5F, 1, 5);
				break;
			case EAST:
				g2d.drawRect(x + 9, y + 2.5F, 1, 5);
				break;
		}
		g2d.setColor(Color.BLACK);
		switch (orientation)
		{
			case NORTH:
				g2d.drawRect(x + 1, y + 9, 8, 1);
				break;
			case SOUTH:
				g2d.drawRect(x + 1, y, 8, 1);
				break;
			case WEST:
				g2d.drawRect(x + 9, y + 1, 1, 8);
				break;
			case EAST:
				g2d.drawRect(x, y + 1, 1, 8);
				break;
		}

		g2d.end();
	}
	
	public boolean onAdded(World world, int x, int y)
	{
		Side orientation = null;
		int maxPwr = 0;
		for (int i = 0; i < Side.size(); i++)
		{
			Side side = Side.get(i);
			if(world.canTransfer(x + side.xOff, y + side.yOff, side.getOpposite()))
			{
				int pwr = world.powerProvided(x + side.xOff, y + side.yOff, side.getOpposite());

				if(pwr > maxPwr)
				{
					orientation = side;
					maxPwr = pwr;
				}
				else if(orientation == null)
					orientation = side;
			}
		}
		if(orientation == null)
			orientation = Side.NORTH;
		world.setMeta(x, y, orientation.ordinal() << 4 | maxPwr & 0xF);
		world.notifyNeighbor(x, y, orientation);
		world.notifyNeighbor(x, y, orientation.getOpposite());

		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		Side orientation = Side.get((meta >> 4) & 0x3);
		if(side == orientation)
		{
			int old = meta;
			int pwr = world.powerProvided(x + side.xOff, y + side.yOff, side.getOpposite());
			meta = (meta & ~0xF) | (pwr & 0xF);
			if(meta != old)
			{
				world.setMeta(x, y, meta);
				world.notifyNeighbor(x, y, side.getOpposite());
			}
		}
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
		int meta = world.getMeta(x, y);
		Side orientation = Side.get((meta >> 4) & 0x3);
		world.notifyNeighbor(x, y, orientation.getOpposite());
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		Side orientation = Side.get((meta >> 4) & 0x3);
		orientation = orientation.rotate90();
		world.setMeta(x, y, orientation.ordinal() << 4);
		onNeighborChanged(world, x, y, orientation);
		world.notifyNeighbors(x, y);
	}

	public int powerProvided(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		Side orientation = Side.get((meta >> 4) & 0x3);
		return to == orientation.getOpposite() && (meta & 0xF) != 0 ? 15 : 0;
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		Side orientation = Side.get((meta >> 4) & 0x3);
		return to == orientation || to == orientation.getOpposite();
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & 0x30;
	}

	static final int[][] tris = {
			{ 1, 0, 9, 0, 5, 10 },
			{ 1, 10, 9, 10, 5, 0 },
			{ 0, 1, 0, 9, 10, 5 },
			{ 10, 1, 10, 9, 0, 5 },
	};
}
