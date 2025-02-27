package com.hk.game.pieces;

import com.hk.engine.G2D;
import powered.Blueprint;
import powered.Side;
import powered.TileData;
import powered.World;

public class Piece
{
	public final String name;
	private String shortName;
	
	protected Piece(String name)
	{
		this.name = name;
	}

	protected Piece setShortName(String shortName)
	{
		this.shortName = shortName;
		return this;
	}

	public String getShortName()
	{
		return shortName;
	}
	
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{}
	
	public int powerProvided(World world, int x, int y, Side to)
	{
		return 0;
	}
	
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return false;
	}
	
	public void onNeighborChanged(World world, int x, int y, Side side)
	{}

	/**
	 * @return true to notify neighbors
	 */
	public boolean onAdded(World world, int x, int y)
	{
		return true;
	}

	public void onPlaced(World world, int x, int y)
	{}

	public void onMetaChanged(World world, int x, int y, int om, int nm)
	{}
	
	public void onDataChanged(World world, int x, int y, TileData od, TileData nd)
	{}

	public void onRemove(World world, int x, int y)
	{}

	public void onInteract(World world, int x, int y)
	{}

	public void onPaste(World world, Blueprint blueprint, int x, int y)
	{}

	public TileData createData(World world, int x, int y, int meta)
	{
		return null;
	}

	public boolean isAir()
	{
		return this == Pieces.AIR;
	}

	public boolean isCircuit()
	{
		return this == Pieces.CIRCUIT;
	}

	public int getDefaultMeta()
	{
		return 0;
	}

	public int cleanMeta(int oldMeta)
	{
		return oldMeta;
	}

	@Override
	public String toString()
	{
		return "'" + name + "' {" + shortName + "}";
	}
}
