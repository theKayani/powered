package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Side;
import powered.World;

import java.util.Map;

public class StraightWirePiece extends CrossWirePiece
{
	public StraightWirePiece(String name)
	{
		super(name);
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);

		boolean vertical = (meta & 16) != 0;
		int n1 = 4;

		g2d.setColor(Color.BLACK);
		if (vertical)
		{
			g2d.drawRect(x + n1, y, 2, 10);
			g2d.drawRect(x + n1 - 0.5F, y + 1, 3, 8);

			g2d.setColor(clrs[meta & 0xF]);
			g2d.drawRect(x + n1 + 0.5F, y, 1, 10);
		}
		else
		{
			g2d.drawRect(x, y + n1, 10, 2);
			g2d.drawRect(x + 1, y + n1 - 0.5F, 8, 3);

			g2d.setColor(clrs[meta & 0xF]);
			g2d.drawRect(x, y + n1 + 0.5F, 10, 1);
		}

		g2d.end();
	}
	
	public boolean onAdded(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		boolean vertical = (meta & 16) != 0;
		Map.Entry<Integer, Integer> entry = getPower(world, x, y, vertical);
		meta = (vertical ? 16 : 0);
		meta |= entry.getValue();
		meta |= entry.getKey() << 5;
		world.setMeta(x, y, meta);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;
		boolean vertical = (meta & 16) != 0;

		if(vertical == side.isVertical())
		{
			if(vertical)
				meta &= ~0b001101111;
			else
				meta &= ~0b110001111;
			Map.Entry<Integer, Integer> entry = getPower(world, x, y, vertical);
			meta |= entry.getValue();
			meta |= entry.getKey() << 5;
		}
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			Side[] sides = vertical ? Side.VERTICAL : Side.HORIZONTAL;
			for(Side s : sides)
				world.notifyNeighbor(x, y, s);
		}
	}
	
	public int powerProvided(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		boolean vertical = (meta & 16) != 0;
		if((meta & 1 << to.ordinal() + 5) != 0)
			return 0;

		if(to.isVertical() == vertical)
			return meta & 0xF;
		return 0;
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		boolean vertical = (meta & 16) != 0;
		if(!vertical)
		{
			boolean canNorth = world.canTransfer(x, y - 1, Side.SOUTH);
			boolean canSouth = world.canTransfer(x, y + 1, Side.NORTH);
			if(canNorth && canSouth)
			{
				world.setMeta(x, y, 16);
				world.notifyNeighbors(x, y);
				return;
			}
		}

		world.setPiece(x, y, Pieces.WIRE, true);
	}

	public boolean canTransfer(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		boolean vertical = (meta & 16) != 0;
		return to.isVertical() == vertical;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & 16;
	}
}
