package powered;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.hk.engine.G2D;
import com.hk.engine.gui.GuiScreen;
import com.hk.engine.gui.Main;
import com.hk.game.pieces.Circuit;
import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;
import com.hk.json.Json;
import com.hk.json.JsonObject;
import com.hk.math.MathUtil;
import com.hk.math.vector.Point;

import java.util.*;

public class Powered extends GuiScreen
{
	private final LinkedList<World> worlds;
	private final LinkedList<View> views;
	public World rootWorld, world;
	public View view;
	public final int scl = 10;
	private boolean paused, step, dragging, isCopying, isCutting, isPasting;
	private boolean selectedCircuit, selectingCircuitIO;
	private Point copyStart, circuitStart, ioStart;
	private int selected, copySelected = -1;
	private long msgTime = -1;
	private String msg;
	private Blueprint held;
	private Circuit circuitToIO;
	private final List<Blueprint> copied;
	
	public Powered(Main main)
	{
		super(main);
		worlds = new LinkedList<>();
		views = new LinkedList<>();
		rootWorld = world = new World(this, 256, 256, null);
		view = new View();
		reset();

		copied = new ArrayList<>();
	}

	private void reset()
	{
		view.moveX = Main.WIDTH / 2F - (world.width * scl) / 2F;
		view.moveY = Main.HEIGHT / 2F - (world.height * scl) / 2F;

		selectedCircuit = false;
		selected = 1;
		setMessage("Selected Wire", 2000);
	}

	@Override
	public void update(double delta)
	{
		Vector2 m = main.unproject(Gdx.input.getX(), Gdx.input.getY());
		float mx = m.x, my = m.y;

		boolean isRightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
		boolean isLeftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		if(isRightPressed || isLeftPressed)
		{
			double dx = mx - view.lastX;
			double dy = my - view.lastY;
			if(isRightPressed)
			{
				view.moveX += dx / view.zoom;
				view.moveY += dy / view.zoom;
			}

			dragging = dragging || MathUtil.hypot(dx, dy) > 2;
		}
		else dragging = false;
		view.lastX = mx;
		view.lastY = my;
		
		if(!paused || step)
		{
			rootWorld.updateWorld();
			if(step)
				System.out.println("world.updates() = " + world.updates());
			step = false;
		}
		
		if(msgTime < System.currentTimeMillis())
		{
			msgTime = -1;
			msg = null;
		}
	}

	@Override
	public void render(G2D g2d)
	{
		Vector2 m = main.unproject(Gdx.input.getX(), Gdx.input.getY());
		float mouseX = m.x;
		float mouseY = m.y;
		g2d.pushMatrix();

		float zoomWidth = (float) (Main.WIDTH * view.zoom);
		float zoomHeight = (float) (Main.HEIGHT * view.zoom);

		float anchorx = (Main.WIDTH - zoomWidth) / 2;
		float anchory = (Main.HEIGHT - zoomHeight) / 2;

		g2d.translate(anchorx, anchory);
		g2d.scale((float) view.zoom, (float) view.zoom);
		g2d.translate((float) view.moveX, (float) view.moveY);

		Affine2 aft = applyCameraTransform().inv();
		Vector2 mi = new Vector2(mouseX, mouseY);
		aft.applyTo(mi);

		Vector2 min = new Vector2(0, 0);
		aft.applyTo(min);
		Vector2 max = new Vector2(Main.WIDTH, Main.HEIGHT);
		aft.applyTo(max);
		int sx = (int) Math.floor(min.x / scl);
		int sy = (int) Math.floor(min.y / scl);
		int ex = (int) Math.ceil(max.x / scl);
		int ey = (int) Math.ceil(max.y / scl);

		sx = MathUtil.between(-1, sx, world.width);
		sy = MathUtil.between(-1, sy, world.height);
		ex = MathUtil.between(-1, ex, world.width);
		ey = MathUtil.between(-1, ey, world.height);

		int startX = MathUtil.between(0, sx, world.width - 1);
		int startY = MathUtil.between(0, sy, world.height - 1);
		int endX = MathUtil.between(0, ex, world.width - 1);
		int endY = MathUtil.between(0, ey, world.height - 1);

		boolean outWest = sx != startX;
		boolean outEast = ex != endX;
		boolean outNorth = sy != startY;
		boolean outSouth = ey != endY;
		g2d.enableBlend();
		if(view.grid)
		{
			g2d.begin(false);
			if(world.hasParent() && (outWest || outNorth || outEast || outSouth))
			{
				g2d.setColor(192, 0, 0, 60);
				if(outWest)
				{
					for (int i = startY; i <= endY + 1; i++)
						g2d.drawLine(sx * scl, i * scl, sx * scl + scl, i * scl);
				}
				if(outEast)
				{
					for (int i = startY; i <= endY + 1; i++)
						g2d.drawLine((ex + 1) * scl, i * scl, (ex + 1) * scl - scl, i * scl);
				}
				if(outNorth)
				{
					for (int i = startX; i <= endX + 1; i++)
						g2d.drawLine(i * scl, sy * scl, i * scl, sy * scl + scl);
				}
				if(outSouth)
				{
					for (int i = startX; i <= endX + 1; i++)
						g2d.drawLine(i * scl, (ey + 1) * scl, i * scl, (ey + 1) * scl - scl);
				}
			}
			g2d.setColor(0, 0, 0, 60);
			for(int i = startX; i <= endX + 1; i++)
				g2d.drawLine(i * scl, startY * scl, i * scl, (endY + 1) * scl);
			for(int i = startY; i <= endY + 1; i++)
				g2d.drawLine(startX * scl, i * scl, (endX + 1) * scl, i * scl);
			g2d.end();
		}
		else
		{
			g2d.begin(false);
			if(world.hasParent() && (outWest || outNorth || outEast || outSouth))
			{
				g2d.setColor(192, 0, 0, 60);
				if(outWest)
					g2d.drawLine(sx * scl, sy * scl, sx * scl, (ey + 1) * scl);
				if(outEast)
					g2d.drawLine((ex + 1) * scl, sy * scl, (ex + 1) * scl, (ey + 1) * scl);
				if(outNorth)
					g2d.drawLine(sx * scl, sy * scl, (ex + 1) * scl, sy * scl);
				if(outSouth)
					g2d.drawLine(sx * scl, (ey + 1) * scl, (ex + 1) * scl, (ey + 1) * scl);
			}
			g2d.setColor(0, 0, 0, 60);
			g2d.drawLine(0, 0, world.width * scl, 0);
			g2d.drawLine(world.width * scl, 0, world.width * scl, world.width * scl);

			g2d.drawLine(0, 0, 0, world.height * scl);
			g2d.drawLine(0, world.height * scl, world.height * scl, world.height * scl);
			g2d.end();
		}

		if(world.hasParent() && (outWest || outNorth || outEast || outSouth))
		{
			Set<World.WorldPoint> edgeConnects = world.circuit.getEdgeConnects();
			World.WorldPoint p;
			g2d.setColor(192, 0, 0, 60);
			g2d.begin(true);
			for (int i = startX; i <= endX; i++)
			{
				p = new World.WorldPoint(world, i, sy);
				if(outNorth && edgeConnects.contains(p))
					g2d.drawRect(i * scl, sy * scl, scl, scl);
				p = new World.WorldPoint(world, i, ey);
				if(outSouth && edgeConnects.contains(p))
					g2d.drawRect(i * scl, ey * scl, scl, scl);
			}
			for (int i = startY; i <= endY; i++)
			{
				if(outWest && edgeConnects.contains(new World.WorldPoint(world, sx, i)))
					g2d.drawRect(sx * scl, i * scl, scl, scl);
				if(outEast && edgeConnects.contains(new World.WorldPoint(world, ex, i)))
					g2d.drawRect(ex * scl, i * scl, scl, scl);
			}
			g2d.end();
		}
		g2d.disableBlend();

		g2d.setColor(Color.RED);
		for(int x = startX; x <= endX; x++)
		{
			for(int y = startY; y <= endY; y++)
			{
				Tile tile = world.grid(x, y);
				if(selectingCircuitIO && circuitToIO.contains(x, y))
					continue;
				Piece p = tile.getPiece();
				if(p.isAir())
					continue;
				int meta = tile.getMeta();
				p.paintPiece(g2d, world, x * 10, y * 10, x, y, meta, true);
			}
		}

		int selX = (int) Math.floor(mi.x / scl);
		int selY = (int) Math.floor(mi.y / scl);
		if(isCopying)
		{
			selX = MathUtil.between(0, selX, world.width - 1);
			selY = MathUtil.between(0, selY, world.height - 1);
			g2d.begin(false);
			int minX, minY, maxX, maxY;

			if (copyStart != null)
			{
				minX = Math.min(selX, copyStart.x);
				minY = Math.min(selY, copyStart.y);
				maxX = Math.max(selX, copyStart.x);
				maxY = Math.max(selY, copyStart.y);
			}
			else
			{
				minX = maxX = selX;
				minY = maxY = selY;
			}

			g2d.setColor(Blueprint.canCopy(world, minX, minY, maxX, maxY) ? Color.GREEN : Color.RED);
			if (copyStart != null)
				g2d.drawRect(minX * scl, minY * scl, Math.abs(maxX - minX + 1) * scl, Math.abs(maxY - minY + 1) * scl);
			else
				g2d.drawRect(selX * scl, selY * scl, scl, scl);
			g2d.end();
		}
		else if(isPasting)
		{
			g2d.enableBlend();
			g2d.setColor(0.9F, 0.9F, 1F, 0.8F);
			g2d.begin(true);
			g2d.drawRect(selX * scl, selY * scl, held.width * scl, held.height * scl);
			g2d.end();
			g2d.disableBlend();

			g2d.setColor(Color.BLUE);
			g2d.begin(false);
			g2d.drawRect(selX * scl, selY * scl, held.width * scl, held.height * scl);
			g2d.end();
			held.paintBlueprint(g2d, selX * scl, selY * scl);
		}
		else if(selectedCircuit)
		{
			selX = MathUtil.between(0, selX, world.width - 1);
			selY = MathUtil.between(0, selY, world.height - 1);
			int minX, minY;
			int width, height;
			if (circuitStart != null)
			{
				minX = Math.min(selX, circuitStart.x);
				minY = Math.min(selY, circuitStart.y);
				width = Math.abs(Math.max(selX, circuitStart.x) - minX + 1);
				height = Math.abs(Math.max(selY, circuitStart.y) - minY + 1);
			}
			else
			{
				minX = selX;
				minY = selY;
				width = height = 1;
			}

			g2d.enableBlend();
			g2d.setColor(0.9F, 1F, 0.9F, 0.8F);
			g2d.begin(true);
			g2d.drawRect(minX * scl, minY * scl, width * scl, height * scl);
			g2d.end();
			g2d.disableBlend();

			g2d.setColor(Color.GREEN);
			g2d.begin(false);
			g2d.drawRect(minX * scl, minY * scl, width * scl, height * scl);
			g2d.end();

			g2d.end();
		}
		else if(selectingCircuitIO)
		{
			g2d.enableBlend();
			g2d.setColor(1F, 1F, 1F, 0.8F);
			g2d.begin(true);
			g2d.drawRect(startX * scl, startY * scl, (endX + 1) * scl, (endY + 1) * scl);
			g2d.end();
			g2d.disableBlend();

			for(int x = circuitToIO.xCoord; x < circuitToIO.xCoord + circuitToIO.width; x++)
			{
				for(int y = circuitToIO.yCoord; y < circuitToIO.yCoord + circuitToIO.height; y++)
				{
					Tile tile = world.grid(x, y);
					Piece p = tile.getPiece();
					if(p.isAir())
						continue;
					int meta = tile.getMeta();
					p.paintPiece(g2d, world, x * 10, y * 10, x, y, meta, true);
				}
			}

			g2d.enableBlend();
			g2d.setColor(0.5F, 0.5F, 0.5F, 0.2F);
			g2d.begin(true);

			for (int x = 0; x < circuitToIO.width; x++)
			{
				g2d.drawRect(circuitToIO.xCoord + x * scl, (circuitToIO.yCoord - 1) * scl, scl, scl);
				g2d.drawRect(circuitToIO.xCoord + x * scl, (circuitToIO.yCoord + circuitToIO.height) * scl, scl, scl);
			}
			g2d.drawRect(circuitToIO.xCoord * scl, (circuitToIO.yCoord - 1) * scl, circuitToIO.width * scl, scl);
			g2d.drawRect(circuitToIO.xCoord * scl, (circuitToIO.yCoord + circuitToIO.height) * scl, circuitToIO.width * scl, scl);
			g2d.drawRect((circuitToIO.xCoord - 1) * scl, circuitToIO.yCoord * scl, scl, circuitToIO.height * scl);
			g2d.drawRect((circuitToIO.xCoord + circuitToIO.width) * scl, circuitToIO.yCoord * scl, scl, circuitToIO.height * scl);

			if(circuitToIO.isOnEdge(selX, selY))
			{
				g2d.setColor(0.7F, 0.7F, 0.1F, 0.2F);
				g2d.drawRect(selX * scl, selY * scl, scl, scl);
			}

			g2d.end();
			g2d.disableBlend();
		}
		else
		{
			if(!world.getPiece(selX, selY).isAir())
			{
				g2d.setColor(0.7F, 0.7F, 0.1F);
				g2d.begin(false);
				g2d.drawRect(selX * scl, selY * scl, scl, scl);
				g2d.end();
			}
			else if(world.hasParent() && world.isOnEdge(selX, selY))
			{
				g2d.setColor(0.9F, 0.4F, 0.4F);
				g2d.begin(true);
				g2d.drawRect(selX * scl, selY * scl, scl, scl);
				g2d.end();
			}
		}

		g2d.popMatrix();
		
		g2d.setColor(Color.BLACK);
		g2d.setFontSize(8);
		g2d.beginString();
		g2d.drawString(5, 15, selX + ", " + selY);
		int meta = world.getMeta(selX, selY);
		g2d.drawString(5, 30, world.getPiece(selX, selY).name + " (" + meta + ")");
		g2d.drawString(5, 45, MathUtil.intBin(meta));
		g2d.drawString(5, 60, "FPS: " + Gdx.graphics.getFramesPerSecond());
		g2d.drawString(5, 75, "View: " + view);
		g2d.endString();

		if(msg != null)
		{
			g2d.setFontSize(18);
			float width = g2d.getStringWidth(msg);
			float height = g2d.getStringHeight(msg);

			g2d.begin(true);
			g2d.setColor(Color.WHITE);
			g2d.drawRect(0, g2d.height - height - 5, width + 10, height + 5);
			g2d.end();

			g2d.begin(false);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(0, g2d.height - height - 5, width + 10, height + 5);
			g2d.end();

			g2d.beginString();
			g2d.drawString(5, g2d.height - height - 3, msg);
			g2d.endString();
		}
		g2d.setFontSize(12);
		g2d.beginString();
		g2d.drawString(g2d.width - 510, g2d.height - 40, "Use the number keys to change pieces");
		g2d.drawString(g2d.width - 470, g2d.height - 20, "Mouse buttons to place and remove");
		g2d.endString();
	}

	public void setMessage(String message, long millis)
	{
		msgTime = System.currentTimeMillis() + millis;
		msg = message;
	}

	private Affine2 applyCameraTransform()
	{
		Affine2 aft = new Affine2();

        float zoomWidth = (float) (Main.WIDTH * view.zoom);
		float zoomHeight = (float) (Main.HEIGHT * view.zoom);

		float anchorx = (Main.WIDTH - zoomWidth) / 2;
		float anchory = (Main.HEIGHT - zoomHeight) / 2;

        aft.translate(anchorx, anchory);
		aft.scale((float) view.zoom, (float) view.zoom);
		aft.translate((float) view.moveX, (float) view.moveY);
		
		return aft;
	}

	private void resetCursor()
	{
		circuitStart = null;
		copyStart = null;
		isCopying = isCutting = isPasting = false;
		copySelected = -1;
		held = null;
		setSelectedPiece(1);
	}


	private void copyBlueprint(Blueprint blueprint)
	{
		if(copied.size() == 20)
			copied.remove(19);
		copied.add(0, blueprint);
		System.out.println("blueprint = " + Blueprint.exportString(blueprint.toJson()));
//		System.out.println("blueprint = " + Json.write(blueprint.toJson()));
	}

	private void setSelectedPiece(int n)
	{
		if(selectingCircuitIO)
			return;

		selectedCircuit = false;
		selected = MathUtil.between(1, n, Pieces.all.length - 1);
		setMessage("Selected " + Pieces.all[selected].name, 2000);
	}

	public void pushWorld(World world)
	{
		worlds.push(this.world);
		views.push(this.view);

		this.world = world;
		this.view = new View();
		reset();
		resetCursor();
	}

	public void popWorld()
	{
		if(worlds.isEmpty())
			return;

		world = worlds.pop();
		view = views.pop();
		resetCursor();
	}

	@Override
	public void onBack()
	{
		if(selectingCircuitIO)
		{
			selectingCircuitIO = false;
			circuitToIO = null;
		}
		popWorld();
	}

	@Override
	public void mouse(float x, float y, int button, boolean pressed)
	{
		if(isCopying)
		{
			if(button != Input.Buttons.LEFT) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);
			px = MathUtil.between(0, px, world.width - 1);
			py = MathUtil.between(0, py, world.height - 1);
			if(copyStart == null)
				copyStart = new Point(px, py);

			if(!pressed)
			{
				int minX = Math.min(px, copyStart.x);
				int minY = Math.min(py, copyStart.y);
				int maxX = Math.max(px, copyStart.x);
				int maxY = Math.max(py, copyStart.y);

				if(!Blueprint.canCopy(world, minX, minY, maxX, maxY))
				{
					resetCursor();
					setMessage("Cannot copy, clipping circuit", 1500);
					return;
				}

				Blueprint blueprint = new Blueprint(world, minX, minY, maxX, maxY);
				copyBlueprint(blueprint);

				if(isCutting)
				{
					for (int cx = minX; cx <= maxX; cx++)
					{
						for (int cy = minY; cy <= maxY; cy++)
							world.setToAir(cx, cy, false);
					}

					world.notifyNeighbors(minX, minY, maxX, maxY);
				}

				held = blueprint;
				copySelected = 0;
				isPasting = true;
				copyStart = null;
				isCopying = isCutting = false;
			}
		}
		else if(isPasting)
		{
			if(pressed || dragging || button != Input.Buttons.LEFT) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);
			held.pasteBlueprint(world, px, py, true);
			copied.remove(held);
			copied.add(0, held);
			resetCursor();
		}
		else if(selectedCircuit)
		{
			if(button != Input.Buttons.LEFT) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = MathUtil.between(0, (int) Math.floor(mi.x / scl), world.width - 1);
			int py = MathUtil.between(0, (int) Math.floor(mi.y / scl), world.height - 1);
			if(circuitStart == null)
				circuitStart = new Point(px, py);

			if(!pressed)
			{
				int minX = Math.min(px, circuitStart.x);
				int minY = Math.min(py, circuitStart.y);
				int maxX = Math.max(px, circuitStart.x);
				int maxY = Math.max(py, circuitStart.y);

				Gdx.input.getTextInput(new Input.TextInputListener() {
					@Override
					public void input(String text)
					{
						text = text.toLowerCase().trim();
//						text = text.replaceAll("\\s", "");
						while(text.startsWith("\"") || text.startsWith("'"))
							text = text.substring(1).trim();
						while(text.endsWith("\"") || text.endsWith("'"))
							text = text.substring(0, text.length() - 1).trim();

						String[] sp = text.split("x");
						if(sp.length == 0)
						{
							setMessage("invalid format, use [width]x[height]!", 2000);
							return;
						}
						if(sp.length == 1)
						{
							setMessage("invalid format, missing 'x'!", 2000);
							return;
						}
						if(sp.length > 2)
						{
							setMessage("invalid format, too many 'x's!", 2000);
							return;
						}
						int worldWidth, worldHeight;
						String s;

						try
						{
							s = sp[0].trim();
							while(s.endsWith("\"") || s.endsWith("'"))
								s = s.substring(0, s.length() - 1).trim();
							worldWidth = Integer.parseInt(s);
						}
						catch (NumberFormatException e)
						{
							setMessage("invalid format, width isn't a number!", 2000);
							return;
						}
						if(worldWidth <= 0)
						{
							setMessage("width must be greater than 0!", 2000);
							return;
						}
						if(worldWidth > 1024)
						{
							setMessage("width must be less than 1025!", 2000);
							return;
						}

						try
						{
							s = sp[1].trim();
							while(s.startsWith("\"") || s.startsWith("'"))
								s = s.substring(1).trim();
							worldHeight = Integer.parseInt(s);
						}
						catch (NumberFormatException e)
						{
							setMessage("invalid format, height isn't a number!", 2000);
							return;
						}
						if(worldHeight <= 0)
						{
							setMessage("height must be greater than 0!", 2000);
							return;
						}
						if(worldHeight > 1024)
						{
							setMessage("height must be less than 1025!", 2000);
							return;
						}

						world.createCircuit(minX, minY, maxX, maxY, worldWidth, worldHeight);
					}

					@Override
					public void canceled() {}
				}, "Create Circuit\n(enter circuit width and height like so: \"[width]x[height]\", ex \"9x20\", \"128x128\")", "", "[width]x[height]");

				resetCursor();
				setSelectedPiece(1);
			}
		}
		else if(selectingCircuitIO)
		{
			if(pressed || dragging) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);
//			px = MathUtil.between(0, px, world.width - 1);
//			py = MathUtil.between(0, py, world.height - 1);

			if(world.inBounds(px, py) && circuitToIO.isOnEdge(px, py))
			{
				circuitToIO.disconnectWorld(px, py);
				circuitToIO.connect(ioStart.x, ioStart.y, px, py);

				selectingCircuitIO = false;
				ioStart = null;
				circuitToIO = null;
				world.game.popWorld();
			}
		}
		else
		{
			if(pressed || dragging) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);

			if(world.inBounds(px, py))
			{
				Piece p = world.getPiece(px, py);
				if(button == Input.Buttons.LEFT)
				{
					if(p.isAir())
					{
						Piece placed = Pieces.all[selected];
						world.setPiece(px, py, placed, true);
						placed.onPlaced(world, px, py);
					}
					else
						p.onInteract(world, px, py);
				}
				else if(button == Input.Buttons.RIGHT)
					world.setToAir(px, py, true);
			}
			else if(world.hasParent() && world.isOnEdge(px, py))
			{
				Circuit circuit = world.circuit;
				ioStart = new Point(px, py);
				circuit.disconnectEdge(px, py);
				selectingCircuitIO = true;
				circuitToIO = circuit;

				pushWorld(world.parent);
				view.lookAt(this, circuit.xCoord + circuit.width / 2F, circuit.yCoord + circuit.height / 2F);
				view.zoom = 5;
			}
		}
	}

	@Override
	public void mouseWheel(int amt)
	{
		if(isPasting)
		{
			copySelected++;
			if(copySelected == copied.size())
				copySelected = 0;
			held = copied.get(copySelected);
		}
		else
		{
			view.zoom *= 1 - amt / 10.0;
			view.zoom = MathUtil.between(1, view.zoom, 10);
		}
	}

	@Override
	public void key(int key, boolean pressed)
	{
		if(pressed) return;

		if(key == Input.Keys.X && !selectingCircuitIO)
		{
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				resetCursor();
				isCopying = isCutting = true;
			}
			else
				paused = !paused;
		}
		else if(key == Input.Keys.C && !selectingCircuitIO)
		{
			if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				resetCursor();
				isCopying = true;
			}
		}
		else if(key == Input.Keys.V && !selectingCircuitIO)
		{
			if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				resetCursor();
				if(!copied.isEmpty())
				{
					isPasting = true;
					copySelected = 0;
					held = copied.get(0);
				}
			}
		}
		else if(key == Input.Keys.I && !selectingCircuitIO)
		{
			if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				Gdx.input.getTextInput(new Input.TextInputListener() {
					@Override
					public void input(String text)
					{
						Blueprint blueprint = null;
						try
						{
							JsonObject obj = Blueprint.importString(text);
							System.out.println("imported json:\n" + obj);
							blueprint = Blueprint.fromJson(obj);
							System.out.println("blueprint = " + blueprint);
						}
						catch (Exception ex)
						{
							new RuntimeException(ex).printStackTrace();
							setMessage("Invalid Blueprint String", 1000);
						}

						if(blueprint != null)
						{
							resetCursor();
							copyBlueprint(blueprint);
							isPasting = true;
							copySelected = 0;
							held = copied.get(0);
						}
					}

					@Override
					public void canceled() {}
				}, "Import Blueprint", "", "long blueprint string");
			}
		}
		else if(key == Input.Keys.G)
		{
			view.grid = !view.grid;
		}
		else if(key == Input.Keys.S && !selectingCircuitIO)
		{
			step = true;
		}
		else if(key == Input.Keys.Q)
		{
			if(selectingCircuitIO)
				onBack();
			else
				resetCursor();
		}
		else if(key == Input.Keys.T)
		{
			Vector2 mi = main.unproject(Gdx.input.getX(), Gdx.input.getY());
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			float px = (int) Math.floor(mi.x / scl);
			float py = (int) Math.floor(mi.y / scl);

			view.lookAt(this, px + 0.5F, py + 0.5F);
		}
		else if(intCodes.containsKey(key) && !selectingCircuitIO)
		{
			setSelectedPiece(intCodes.get(key));
		}
		else if(key == Input.Keys.TAB && !selectingCircuitIO)
		{
			resetCursor();
			selectedCircuit = true;
			setMessage("Selected Machine", 2000);
		}
	}

	private static final Map<Integer, Integer> intCodes = new HashMap<>();

	static
	{
		intCodes.put(Input.Keys.NUM_1, 1);
		intCodes.put(Input.Keys.NUM_2, 2);
		intCodes.put(Input.Keys.NUM_3, 3);
		intCodes.put(Input.Keys.NUM_4, 4);
		intCodes.put(Input.Keys.NUM_5, 5);
		intCodes.put(Input.Keys.NUM_6, 6);
		intCodes.put(Input.Keys.NUM_7, 7);
		intCodes.put(Input.Keys.NUM_8, 8);
		intCodes.put(Input.Keys.NUM_9, 9);
	}

	public static class View
	{
		public double zoom = 2, moveX, moveY, lastX, lastY;
		public boolean grid = true;

		@Override
		public String toString()
		{
			return moveX + ", " + moveY + " (" + zoom + ")";
		}

		public void lookAt(Powered powered, float xCoord, float yCoord)
		{
			moveX = Main.WIDTH / 2D - xCoord * powered.scl;
			moveY = Main.HEIGHT / 2D - yCoord * powered.scl;
		}
	}
}