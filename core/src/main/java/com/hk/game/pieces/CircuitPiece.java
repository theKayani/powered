package com.hk.game.pieces;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import powered.Side;
import powered.World;

public class CircuitPiece extends Piece
{
	public CircuitPiece()
	{
		super("Circuit");
	}

	@Override
	public void paintPiece(G2D g2d, World world, int x, int y, int xc, int yc, int meta, boolean exists)
	{
		g2d.begin(true);

		if(meta == 0)
		{
			g2d.setColor(0, 0.75F, 0);
			g2d.drawRect(x, y, 10, 10);
			g2d.end();
			return;
		}

		int metaNorth = meta & 0x3;
		boolean edgeNorth = metaNorth > 0;
		int metaSouth = (meta >> 2) & 0x3;
		boolean edgeSouth = metaSouth > 0;
		int metaWest = (meta >> 4) & 0x3;
		boolean edgeWest = metaWest > 0;
		int metaEast = (meta >> 6) & 0x3;
		boolean edgeEast = metaEast > 0;
		int gx, gy, gw, gh;
		int pwr;
		float f;

		if(edgeNorth)
		{
			g2d.setColor(0, 0.5F, 0);
			pwr = meta >> 8 & 0xF;
			gx = 0;
			gy = 2;
			gw = 10;
			gh = 1;

			if(metaNorth != 1)
			{
				g2d.drawRect(x + gx + (edgeWest ? 2 : 0), y + gy, 4.5F - (edgeWest ? 2 : 0), gh);
				g2d.drawRect(x + gx + 5.5F, y + gy, 4.5F - (edgeEast ? 2 : 0), gh);
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x + gx + 4, y, 2, 2);
				g2d.setColor(WirePiece.clrs[pwr & 0xF]);
				f = metaNorth == 2 ? 0.5F : 0;
				g2d.drawRect(x + gx + 4.5F, y + f, 1, 3 - f);
			}
			else
			{
				if(edgeWest)
				{
					gx += 2;
					gw -= 2;
				}
				if(edgeEast)
					gw -= 2;
				g2d.drawRect(x + gx, y + gy, gw, gh);
			}
		}

		if(edgeSouth)
		{
			g2d.setColor(0, 0.5F, 0);
			pwr = meta >> 12 & 0xF;
			gx = 0;
			gy = 7;
			gw = 10;
			gh = 1;

			if(metaSouth != 1)
			{
				g2d.drawRect(x + gx + (edgeWest ? 2 : 0), y + gy, 4.5F - (edgeWest ? 2 : 0), gh);
				g2d.drawRect(x + gx + 5.5F, y + gy, 4.5F - (edgeEast ? 2 : 0), gh);
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x + gx + 4, y + gy + 1, 2, 2);
				g2d.setColor(WirePiece.clrs[pwr & 0xF]);
				f = metaSouth == 2 ? 0.5F : 0;
				g2d.drawRect(x + gx + 4.5F, y + gy, 1, 3 - f);
			}
			else
			{
				if(edgeWest)
				{
					gx += 2;
					gw -= 2;
				}
				if(edgeEast)
					gw -= 2;
				g2d.drawRect(x + gx, y + gy, gw, gh);
			}
		}

		if(edgeWest)
		{
			g2d.setColor(0, 0.5F, 0);
			pwr = meta >> 16 & 0xF;
			gx = 2;
			gy = 0;
			gw = 1;
			gh = 10;

			if(metaWest != 1)
			{
				g2d.drawRect(x + gx, y + gy + (edgeNorth ? 2 : 0), gw, 4.5F - (edgeNorth ? 2 : 0));
				g2d.drawRect(x + gx, y + gy + 5.5F, gw, 4.5F - (edgeSouth ? 2 : 0));
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x, y + gy + 4, 2, 2);
				g2d.setColor(WirePiece.clrs[pwr & 0xF]);
				f = metaWest == 2 ? 0.5F : 0;
				g2d.drawRect(x + f, y + gy + 4.5F, 3 - f, 1);
			}
			else
			{
				if(edgeNorth)
				{
					gy += 2;
					gh -= 2;
				}
				if(edgeSouth)
					gh -= 2;
				g2d.drawRect(x + gx, y + gy, gw, gh);
			}
		}

		if(edgeEast)
		{
			g2d.setColor(0, 0.5F, 0);
			pwr = meta >> 20 & 0xF;
			gx = 7;
			gy = 0;
			gw = 1;
			gh = 10;

			if(metaEast != 1)
			{
				g2d.drawRect(x + gx, y + gy + (edgeNorth ? 2 : 0), gw, 4.5F - (edgeNorth ? 2 : 0));
				g2d.drawRect(x + gx, y + gy + 5.5F, gw, 4.5F - (edgeSouth ? 2 : 0));
				g2d.setColor(Color.BLACK);
				g2d.drawRect(x + gx + 1, y + gy + 4, 2, 2);
				g2d.setColor(WirePiece.clrs[pwr & 0xF]);
				f = metaEast == 2 ? 0.5F : 0;
				g2d.drawRect(x + gx, y + gy + 4.5F, 3 - f, 1);
			}
			else
			{
				if(edgeNorth)
				{
					gy += 2;
					gh -= 2;
				}
				if(edgeSouth)
					gh -= 2;
				g2d.drawRect(x + gx, y + gy, gw, gh);
			}
		}

		gx = 0;
		gy = 0;
		gw = 10;
		gh = 10;
		if(edgeNorth)
		{
			gy += 3;
			gh -= 3;
		}
		if(edgeWest)
		{
			gx += 3;
			gw -= 3;
		}
		if(edgeSouth)
			gh -= 3;
		if(edgeEast)
			gw -= 3;
		g2d.setColor(0, 0.75F, 0);
		g2d.drawRect(x + gx, y + gy, gw, gh);

		g2d.end();
	}

	@Override
	public void onNeighborChanged(World world, int x, int y, Side side)
	{
		int meta = world.getMeta(x, y);
		int oldMeta = meta;
		int sideMeta = meta >> (side.ordinal() * 2) & 0x3;
		if(sideMeta == 0)
			throw new UnsupportedOperationException("cannot update inside of circuit");
		if(sideMeta == 1)
			return;

		meta &= ~(0x3 << (side.ordinal() * 2));
		boolean canTransfer = world.canTransfer(x + side.xOff, y + side.yOff, side.getOpposite());
		meta |= (canTransfer ? 3 : 2) << (side.ordinal() * 2);
		if(meta != oldMeta)
		{
			world.setMeta(x, y, meta);
			world.notifyNeighbor(x, y, side);
		}

		if(canTransfer)
			world.getData(x, y, Circuit.class).onWorldChanged(x, y, side);
	}

	@Override
	public void onInteract(World world, int x, int y)
	{
		world.game.pushWorld(world.getData(x, y, Circuit.class).getCircuitWorld());
	}

	@Override
	public void onRemove(World world, int x, int y)
	{
		world.getData(x, y, Circuit.class).delete();
	}

	@Override
	public int powerProvided(World world, int x, int y, Side to)
	{
		return world.getData(x, y, Circuit.class).getWorldPower(x + to.xOff, y + to.yOff);
	}

	@Override
	public boolean canTransfer(World world, int x, int y, Side to)
	{
		return (world.getMeta(x, y) >> to.ordinal() * 2 & 0x3) > 1;
	}

	@Override
	public int cleanMeta(int oldMeta)
	{
		return oldMeta & 0xFF;
	}

	// 00 not edge
	// 01 edge
	// 10 disconnected I/O
	// 11 connected I/O
}
