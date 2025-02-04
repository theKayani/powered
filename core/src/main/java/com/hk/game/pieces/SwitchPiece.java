package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.World;

public class SwitchPiece extends Piece
{
	public SwitchPiece()
	{
		super("Switch");
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);
		g2d.setColor(Color.GREEN);
		g2d.drawRect(x, y, 10, 10);
		
		g2d.setColor(meta == 0 ? Color.BLUE : Color.RED);
		g2d.drawRect(x + 3, y + 3, 4, 4);
		g2d.end();
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		world.setMeta(x, y, world.getMeta(x, y) == 0 ? 1 : 0);
		world.notifyNeighbors(x, y);
	}

	@Override
	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{
		world.notifyNeighbors(x, y);
	}

	public int powerProvided(World world, int x, int y, Side to)
	{
		return world.getMeta(x, y) == 0 ? 0 : 15;
	}

	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return true;
	}
}
