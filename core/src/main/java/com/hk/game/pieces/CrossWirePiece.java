package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Side;
import powered.World;

import java.util.AbstractMap;
import java.util.Map;

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
		int meta = 0;
		Map.Entry<Integer, Integer> entry1 = getPower(world, x, y, true);
		Map.Entry<Integer, Integer> entry2 = getPower(world, x, y, false);

		meta |= entry1.getKey();
		meta |= entry2.getKey() << 4;
		meta |= (entry1.getValue() | entry2.getValue()) << 8;

		world.setMeta(x, y, meta);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;

		if(side.isVertical())
		{
			meta &= ~0x30F;
			Map.Entry<Integer, Integer> entry = getPower(world, x, y, true);
			meta |= entry.getValue();
			meta |= entry.getKey() << 8;
		}
		else
		{
			meta &= ~0xCF0;
			Map.Entry<Integer, Integer> entry = getPower(world, x, y, false);
			meta |= entry.getValue() << 4;
			meta |= entry.getKey() << 8;
		}
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			Side[] sides = side.isVertical() ? Side.VERTICAL : Side.HORIZONTAL;
			for(Side s : sides)
				world.notifyNeighbor(x, y, s);
		}
	}
	
	protected Map.Entry<Integer, Integer> getPower(World world, int x, int y, boolean vertical)
	{
		Side[] sides = vertical ? Side.VERTICAL : Side.HORIZONTAL;
		int input = 0;
		int pwr = 0;
		for(Side side : sides)
		{
			Piece p = world.getPiece(x + side.xOff, y + side.yOff);
			if(world.canTransfer(x + side.xOff, y + side.yOff, side.getOpposite()))
			{
				int pv = world.powerProvided(x + side.xOff, y + side.yOff, side.getOpposite());
				if(p instanceof WirePiece)
					pv--;

				if(pv > pwr)
				{
					pwr = pv;
					input = 0;
				}
				if (pwr > 0 && pv == pwr)
					input |= 1 << side.ordinal();
			}
		}
		return new AbstractMap.SimpleEntry<>(input, pwr & 0xF);
	}
	
	public int powerProvided(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		if((meta & 1 << to.ordinal() + 8) != 0)
			return 0;

		switch(to)
		{
			case NORTH:
			case SOUTH:
				return meta & 0xF;
			case WEST:
			case EAST:
				return (meta >> 4) & 0xF;
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
