package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Side;
import powered.World;

import java.util.AbstractMap;
import java.util.Map;

public class CornerWirePiece extends WirePiece
{
	public CornerWirePiece(String name)
	{
		super(name);
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.setColor(Color.BLACK);
		g2d.begin(true);
		Side origin = Side.get((meta >> 4) & 3);

		int n1 = 4;

		g2d.drawRect(x + n1 - 0.5F, y + n1 - 0.5F, 3, 3);
		if(origin == Side.EAST)
		{
			g2d.drawRect(x + 5, y + n1, 5, 2);
			g2d.drawRect(x + n1, y + 5, 2, 5);
		}
		if(origin == Side.WEST)
		{
			g2d.drawRect(x, y + n1, 5, 2);
			g2d.drawRect(x + n1, y, 2, 5);
		}
		if(origin == Side.NORTH)
		{
			g2d.drawRect(x + n1, y, 2, 5);
			g2d.drawRect(x + 5, y + n1, 5, 2);
		}
		if(origin == Side.SOUTH)
		{
			g2d.drawRect(x + n1, y + 5, 2, 5);
			g2d.drawRect(x, y + n1, 5, 2);
		}

		g2d.setColor(clrs[meta & 0xF]);
		if(origin == Side.EAST)
		{
			g2d.drawRect(x + 5, y + n1 + 0.5F, 5, 1);
			g2d.drawRect(x + n1 + 0.5F, y + 5, 1, 5);
		}
		if(origin == Side.WEST)
		{
			g2d.drawRect(x, y + n1 + 0.5F, 5, 1);
			g2d.drawRect(x + n1 + 0.5F, y, 1, 5);
		}
		if(origin == Side.NORTH)
		{
			g2d.drawRect(x + n1 + 0.5F, y, 1, 5);
			g2d.drawRect(x + 5, y + n1 + 0.5F, 5, 1);
		}
		if(origin == Side.SOUTH)
		{
			g2d.drawRect(x + n1 + 0.5F, y + 5, 1, 5);
			g2d.drawRect(x, y + n1 + 0.5F, 5, 1);
		}

		g2d.end();
	}

	public boolean onAdded(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		Side origin = Side.get((meta >> 4) & 3);
		meta &= ~0x3CF;
		Map.Entry<Integer, Integer> entry = getPower(world, x, y, origin);
		meta |= entry.getValue();
		meta |= entry.getKey() << 6;
		world.setMeta(x, y, meta);
		Side[] sides = Side.CORNERS[origin.ordinal()];
		for(Side s : sides)
			world.notifyNeighbor(x, y, s);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		Side origin = Side.get((meta >> 4) & 3);
		int old = meta;

		if(side == origin || side == origin.rotate90())
		{
			meta &= ~0x3CF;
			Map.Entry<Integer, Integer> entry = getPower(world, x, y, origin);
			meta |= entry.getValue();
			meta |= entry.getKey() << 6;
		}

		if(meta != old)
		{
			world.setMeta(x, y, meta);
			Side[] sides = Side.CORNERS[origin.ordinal()];
			for(Side s : sides)
				world.notifyNeighbor(x, y, s);
		}
	}

	private Map.Entry<Integer, Integer> getPower(World world, int x, int y, Side origin)
	{
		Side[] sides = Side.CORNERS[origin.ordinal()];
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
		if((meta & 1 << to.ordinal() + 6) != 0)
			return 0;

		return meta & 0xF;
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		world.setPiece(x, y, Pieces.WIRE, true);
	}

	public boolean canTransfer(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		Side side = Side.get((meta >> 4) & 3);
		return side == to || side.rotate90() == to;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & 0x30;
	}
}
