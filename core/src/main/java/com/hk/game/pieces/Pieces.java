package com.hk.game.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Pieces
{
	public static final Piece AIR = new Piece("Air").setShortName("air");
	public static final Piece WIRE = new WirePiece("Wire").setShortName("wire");
	public static final Piece SWITCH = new SwitchPiece().setShortName("switch");
	public static final Piece AND = new LogicalPiece(LogicalPiece.Type.AND).setShortName("and_gate");
	public static final Piece OR = new LogicalPiece(LogicalPiece.Type.OR).setShortName("or_gate");
	public static final Piece XOR = new LogicalPiece(LogicalPiece.Type.XOR).setShortName("xor_gate");
	public static final Piece NOT = new NotPiece().setShortName("not_gate");
	public static final Piece LIGHT = new LightPiece().setShortName("light");
	public static final Piece TIMER = new TimerPiece().setShortName("timer");
	public static final Piece DIODE = new DiodePiece().setShortName("diode");


	public static final Piece CORNER_WIRE = new CornerWirePiece("Corner Wire").setShortName("cnwire");
	public static final Piece CROSS_WIRE = new CrossWirePiece("Cross Wire").setShortName("cwire");
	public static final Piece STRAIGHT_WIRE = new StraightWirePiece("Straight Wire").setShortName("swire");
	public static final Piece CIRCUIT = new CircuitPiece().setShortName("circuit");

	public static final Piece[] all;
	public static final Map<String, Piece> pieceShortNames;

	public static Piece fromShortName(String shortName)
	{
		return pieceShortNames.get(shortName);
	}

	static
	{
		List<Piece> pcs = new ArrayList<>();

		pcs.add(AIR);
		pcs.add(WIRE);
		pcs.add(SWITCH);
		pcs.add(AND);
		pcs.add(OR);
		pcs.add(XOR);
		pcs.add(NOT);
		pcs.add(LIGHT);
		pcs.add(TIMER);
		pcs.add(DIODE);

		all = new Piece[pcs.size()];
		pcs.toArray(all);

		pcs.add(CORNER_WIRE);
		pcs.add(CROSS_WIRE);
		pcs.add(STRAIGHT_WIRE);
		pcs.add(CIRCUIT);

		pieceShortNames = new HashMap<>();

		for(Piece piece : pcs)
		{
			String shortName = piece.getShortName();
			if(shortName == null)
				throw new RuntimeException("piece has null shortname: " + piece + ", (class=" + piece.getClass() + ")");
			pieceShortNames.put(shortName, piece);
		}
	}

	private Pieces()
	{}
}
