package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.World;

public class LogicalPiece extends Piece
{
	public final Type type;

	protected LogicalPiece(Type type)
	{
		super(type.name() + " Gate");
		this.type = type;
	}
	
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);
		g2d.setColor((meta & 1) != 0 ? Color.RED : Color.BLACK);
		g2d.drawRect(x, y, 10, 1);
		g2d.setColor((meta & 2) != 0 ? Color.RED : Color.BLACK);
		g2d.drawRect(x, y + 9, 10, 1);

		g2d.setColor(isActive(meta) ? Color.RED : Color.BLACK);
		g2d.drawRect(x + 9, y, 1, 10);

		g2d.setColor(Color.BLACK);
		g2d.drawRect(x, y, 1, 10);
		g2d.end();

		float scale = 1F / 3F;
		g2d.pushMatrix();
		g2d.setFontSize(18);
		g2d.scale(scale, scale);
		g2d.enableCentered();
		g2d.beginString();
		g2d.drawString((x + 5) * (1 / scale), (y + 2 + type.letterOffset) * (1 / scale), String.valueOf(type.letter));
		g2d.endString();
		g2d.disableCentered();
		g2d.popMatrix();
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
		if(side == null)
		{
			meta = world.powerProvided(x, y + Side.NORTH.yOff, Side.SOUTH) > 0 ? meta | 1 : meta & ~1;
			meta = world.powerProvided(x, y + Side.SOUTH.yOff, Side.NORTH) > 0 ? meta | 2 : meta & ~2;
		}
		else if(side.isVertical())
		{
			if(world.powerProvided(x, y + side.yOff, side.getOpposite()) > 0)
				meta |= 1 << side.ordinal();
			else
				meta &= ~(1 << side.ordinal());
		}
		if(meta != old)
		{
			world.setMeta(x, y, meta);
			world.notifyNeighbor(x, y, Side.EAST);
//			world.notifyNeighbors(x, y);
		}
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
//		world.notifyNeighbor(x, y, Side.NORTH);
//		world.notifyNeighbor(x, y, Side.SOUTH);
		world.notifyNeighbor(x, y, Side.EAST);
	}

	public int powerProvided(World world, int x, int y, Side to)
	{
		int meta = world.getMeta(x, y);
		return to == Side.EAST && isActive(meta) ? 15 : 0;
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return to != Side.WEST;
	}

	private boolean isActive(int meta)
	{
		boolean flag;
		switch(type)
		{
			case AND:
			case NAND:
				flag = meta == 3;
				break;
			case OR:
			case NOR:
				flag = (meta & 3) != 0;
				break;
			case XOR:
			case XNOR:
				flag = meta == 1 || meta == 2;
				break;
			default:
				throw new AssertionError();
		}
		return type.negative != flag;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return 0;
	}

	enum Type
	{
		AND('x', false, 2),
		OR('+', false, 2),
		XOR('^', false, 4),
		NAND('x', true, 2),
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
