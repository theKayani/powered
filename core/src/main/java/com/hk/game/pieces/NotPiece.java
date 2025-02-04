package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.World;

public class NotPiece extends Piece
{
	protected NotPiece()
	{
		super("NOT Gate");
	}

	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);
		g2d.setColor(meta != 0 ? Color.RED : Color.BLACK);
		g2d.drawRect(x, y, 1, 10);
		g2d.setColor(meta == 0 ? Color.RED : Color.BLACK);
		g2d.drawRect(x + 9, y, 1, 10);

		g2d.setColor(Color.BLACK);
		g2d.drawRect(x, y, 10, 1);
		g2d.drawRect(x, y + 9, 10, 1);
		g2d.end();

		float scale = 1F / 3F;
		g2d.pushMatrix();
		g2d.setFontSize(18);
		g2d.scale(scale, scale);
		g2d.enableCentered();
		g2d.beginString();
		g2d.drawString((x + 5) * (1 / scale), (y + 4) * (1 / scale), "!");
		g2d.endString();
		g2d.disableCentered();
		g2d.popMatrix();
	}
	
	public boolean onAdded(World world, int x, int y)
	{
		onNeighborChanged(world, x, y, null);
		return super.onAdded(world, x, y);
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int old = meta;
		meta = world.powerProvided(x + Side.WEST.xOff, y, Side.EAST) > 0 ? 1 : 0;
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			world.notifyNeighbors(x, y);
		}
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
		world.notifyNeighbor(x, y, Side.WEST);
		world.notifyNeighbor(x, y, Side.EAST);
	}

	public int powerProvided(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		return to == Side.EAST && meta == 0 ? 15 : 0;
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return to != Side.NORTH && to != Side.SOUTH;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return 0;
	}

	enum Type
	{
		AND('x', false, 1),
		OR('+', false, 2),
		XOR('^', false, 4),
		NAND('x', true, 1),
		NOR('+', true, 2),
		XNOR('^', true, 4);


		public final char letter;
		public boolean negative;
		public int letterOffset = 0;

		Type(char letter, boolean negative)
		{
			this.letter = letter;
			this.negative = negative;
		}

		Type(char letter, boolean negative, int letterOffset)
		{
			this.letter = letter;
			this.negative = negative;
			this.letterOffset = letterOffset;
		}
	}
}
