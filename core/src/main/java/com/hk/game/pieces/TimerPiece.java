package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import com.hk.math.MathUtil;
import powered.Blueprint;
import powered.Side;
import powered.TileData;
import powered.World;

public class TimerPiece extends Piece
{
	public TimerPiece()
	{
		super("Timer");
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);
		g2d.setColor(Color.BLUE);
		g2d.drawCircle(x, y, 5);

		float percent;
		if(exists)
		{
			TimerTick ticker = world.getData(xc, yc, TimerTick.class);
			percent = ticker.tick * 1F / times[meta >> 1];
			percent = MathUtil.between(0, percent, 1);
		}
		else percent = 0;

		if((meta & 1) == 0)
			g2d.setColor(percent, 1F, 0F);
		else
			g2d.setColor(Color.RED);
		g2d.drawRect(x + 3, y + 3, 4, 4);
		g2d.end();
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		int level = meta >> 1;
		level = (level + 1) % times.length;
		world.setMeta(x, y, (meta & 1) | (level << 1));
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
		world.notifyNeighbors(x, y);
	}

	public int powerProvided(World world, int x, int y, Side to)
	{
		return (world.getMeta(x, y) & 1) == 0 ? 0 : 15;
	}

	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return true;
	}

	public TimerTick createData(World world, int x, int y, int meta)
	{
		return new TimerTick(world, x, y);
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & ~1;
	}

	static final int[] times = { 3, 7, 15, 31, 63, 127, 255, 511 };

	static class TimerTick extends TileData implements World.Ticker
	{
		int tick = Integer.MAX_VALUE - 1;

		TimerTick(World world, int xCoord, int yCoord)
		{
			super(world, xCoord, yCoord);
		}

		@Override
		public void onUpdate()
		{
			int level = world.getMeta(xCoord, yCoord) >> 1;
			tick++;
			if(tick == 0)
			{
				world.setMeta(xCoord, yCoord, level << 1);
				world.notifyNeighbors(xCoord, yCoord);
			}
			else if(tick >= times[level])
			{
				tick = -1;
				world.setMeta(xCoord, yCoord, (level << 1) + 1);
				world.notifyNeighbors(xCoord, yCoord);
			}
		}
	}
}