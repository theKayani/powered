package com.hk.game.pieces;

import com.hk.engine.G2D;
import powered.Side;
import powered.World;

public class LightPiece extends Piece
{
	private final float[][] clrs = new float[6][3];
	
	public LightPiece()
	{
		super("Light");
		clrs[0] = new float[] { 1, 0, 0 };
		clrs[1] = new float[] { 0, 1, 0 };
		clrs[2] = new float[] { 0, 0, 1 };
		clrs[3] = new float[] { 1, 0, 1 };
		clrs[4] = new float[] { 0, 1, 1 };
		clrs[5] = new float[] { 1, 1, 0 };
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		float[] clr = clrs[meta / 2];
		float r = clr[0];
		float g = clr[1];
		float b = clr[2];
		if(meta % 2 == 0)
		{
			r *= 0.666F;
			g *= 0.666F;
			b *= 0.666F;
		}
		else
		{
			r = Math.min(r + 0.5F, 1);
			g = Math.min(g + 0.5F, 1);
			b = Math.min(b + 0.5F, 1);
		}
		g2d.setColor(r, g, b);
		g2d.begin(true);
		g2d.drawRect(x, y, 10, 10);
		g2d.end();
		g2d.begin(false);
		g2d.setColor(clr[0] / 2, clr[1] / 2, clr[2] / 2);
		g2d.drawRect(x + 0.5F, y + 0.5F, 9, 9);
		g2d.end();
	}

	public boolean onAdded(World world, int x, int y)
	{
		onNeighborChanged(world, x, y, null);
		return true;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;
		meta = meta / 2 * 2 + (world.isPowered(x, y) ? 1 : 0);

		if(meta != old)
			world.setMeta(x, y, meta);
	}
	
	@Override
	public void onInteract(World world, int x, int y)
	{
		int meta = world.getMeta(x, y);
		world.setMeta(x, y, (meta + 2) % (clrs.length * 2));
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return true;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta / 2 * 2;
	}
}
