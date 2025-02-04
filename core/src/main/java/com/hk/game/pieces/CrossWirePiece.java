package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Side;
import powered.World;

public class CrossWirePiece extends WirePiece
{
	public CrossWirePiece(String name)
	{
		super(name);
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);

		int n1 = 4;

		g2d.setColor(Color.BLACK);
		g2d.drawRect(x, y + n1, 10, 2);

		g2d.setColor(clrs[(meta >> 4) & 0xF]);
		g2d.drawRect(x, y + n1 + 0.5F, 10, 1);

		g2d.setColor(Color.BLACK);
		g2d.drawRect(x + n1, y, 2, 10);

		g2d.setColor(clrs[meta & 0xF]);
		g2d.drawRect(x + n1 + 0.5F, y, 1, 10);

		g2d.end();
	}
	
	public boolean onAdded(World world, int x, int y)
	{
		int meta = getPower(world, x, y, true);
		meta |= getPower(world, x, y, false) << 4;
		world.setMeta(x, y, meta);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;

		if(side.isVertical())
		{
			meta &= ~0xF;
			meta |= getPower(world, x, y, true);
		}
		else
		{
			meta &= ~0xF0;
			meta |= getPower(world, x, y, false) << 4;
		}
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			Side[] sides = side.isVertical() ? Side.VERTICAL : Side.HORIZONTAL;
			for(Side s : sides)
				world.notifyNeighbor(x, y, s);
		}
	}
	
	protected int getPower(World world, int x, int y, boolean vertical)
	{
		Side[] sides = vertical ? Side.VERTICAL : Side.HORIZONTAL;
		int pwr = 0;
		for(Side side : sides)
		{
			Piece p = world.getPiece(x + side.xOff, y + side.yOff);
			if(p.canTransfer(world, x + side.xOff, y + side.yOff, side.getOpposite()))
			{
				int pv = world.powerProvided(x + side.xOff, y + side.yOff, side.getOpposite());
				if(p instanceof WirePiece)
					pv--;
				pwr = Math.max(pwr, pv);
			}
		}
		return pwr & 0xF;
	}
	
	public int powerProvided(World world, int x, int y, Side to)
	{
		switch(to)
		{
			case NORTH:
			case SOUTH:
				return world.getMeta(x, y) & 0xF;
			case WEST:
			case EAST:
				return (world.getMeta(x, y) >> 4) & 0xF;
			default:
				throw new AssertionError(to.name());
		}
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		world.setPiece(x, y, Pieces.STRAIGHT_WIRE, true);
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return 0;
	}
}
