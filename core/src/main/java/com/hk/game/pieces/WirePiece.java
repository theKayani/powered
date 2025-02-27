package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.World;

import java.util.AbstractMap;
import java.util.Map;

public class WirePiece extends Piece
{
	public WirePiece(String name)
	{
		super(name);
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.setColor(Color.BLACK);
		g2d.begin(true);
		
		int n1 = 4;

		g2d.drawRect(x + n1, y + n1, 2, 2);
		if((meta & 1 << Side.EAST.ordinal() + 4) != 0)
			g2d.drawRect(x + 5, y + n1, 5, 2);
		if((meta & 1 << Side.WEST.ordinal() + 4) != 0)
			g2d.drawRect(x, y + n1, 5, 2);
		if((meta & 1 << Side.NORTH.ordinal() + 4) != 0)
			g2d.drawRect(x + n1, y, 2, 5);
		if((meta & 1 << Side.SOUTH.ordinal() + 4) != 0)
			g2d.drawRect(x + n1, y + 5, 2, 5);

		g2d.setColor(clrs[meta & 0xF]);
		g2d.drawRect(x + n1 + 0.5F, y + n1 + 0.5F, 1, 1);
		if((meta & 1 << Side.EAST.ordinal() + 4) != 0)
			g2d.drawRect(x + 5, y + n1 + 0.5F, 5, 1);
		if((meta & 1 << Side.WEST.ordinal() + 4) != 0)
			g2d.drawRect(x, y + n1 + 0.5F, 5, 1);
		if((meta & 1 << Side.NORTH.ordinal() + 4) != 0)
			g2d.drawRect(x + n1 + 0.5F, y, 1, 5);
		if((meta & 1 << Side.SOUTH.ordinal() + 4) != 0)
			g2d.drawRect(x + n1 + 0.5F, y + 5, 1, 5);

//		g2d.setColor(Color.GREEN);
//		if((meta & 1 << Side.EAST.ordinal() + 8) != 0)
//			g2d.drawRect(x + 8, y + n1, 2, 2);
//		if((meta & 1 << Side.WEST.ordinal() + 8) != 0)
//			g2d.drawRect(x, y + n1, 2, 2);
//		if((meta & 1 << Side.NORTH.ordinal() + 8) != 0)
//			g2d.drawRect(x + n1, y, 2, 2);
//		if((meta & 1 << Side.SOUTH.ordinal() + 8) != 0)
//			g2d.drawRect(x + n1, y + 8, 2, 2);

		g2d.end();
	}
	
	public boolean onAdded(World world, int x, int y)
	{
		int meta = 0;
		for(int i = 0; i < Side.size(); i++)
		{
			Side side = Side.get(i);
			if(world.canTransfer(x + side.xOff, y + side.yOff, side.getOpposite()))
				meta |= 1 << side.ordinal() + 4;
		}
		meta &= ~0xF;
		Map.Entry<Integer, Integer> entry = getPower(world, x, y);
		meta |= entry.getValue();
		meta |= entry.getKey() << 8;
		world.setMeta(x, y, meta);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;
		if(world.canTransfer(x + side.xOff, y + side.yOff, side.getOpposite()))
			meta |= 1 << side.ordinal() + 4;
		else
			meta &= ~(1 << side.ordinal() + 4);

		meta &= ~0xF0F;
		Map.Entry<Integer, Integer> entry = getPower(world, x, y);
		meta |= entry.getValue();
		meta |= entry.getKey() << 8;
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			world.notifyNeighbors(x, y);
		}
	}
	
	private Map.Entry<Integer, Integer> getPower(World world, int x, int y)
	{
		int input = 0;
		int pwr = 0;
		for(int i = 0; i < Side.size(); i++)
		{
			Side side = Side.get(i);
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
		if((meta & 1 << to.ordinal() + 8) == 0)
			return meta & 0xF;
		else
			return 0;
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return true;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & 0xF0;
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
//		onAdded(world, x, y);
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);

		if((meta & 240) == 240)
			world.setPiece(x, y, Pieces.CROSS_WIRE, true);
		else if((meta & 48) == 48)
			world.setPiece(x, y, Pieces.STRAIGHT_WIRE, 16, true);
		else if((meta & 192) == 192)
			world.setPiece(x, y, Pieces.STRAIGHT_WIRE, 0, true);
		else
		{
			for(int i = 0; i < Side.size(); i++)
			{
				Side side = Side.get(i);
				int flags = 1 << side.ordinal() | 1 << side.rotate90().ordinal();
				flags = flags << 4;

				if((meta & flags) == flags)
				{
					world.setPiece(x, y, Pieces.CORNER_WIRE, side.ordinal() << 4, true);
					return;
				}
			}
		}
	}

	static final Color[] clrs = new Color[16];
	static
	{
		clrs[0] = Color.GRAY;
		for(int i = 1; i < 16; i++)
		{
			int c = 255 - (15 - i) * 10;
			clrs[i] = new Color(c / 255F, 0, 0, 1F);
		}
	}
}
