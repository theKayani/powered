package powered;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.hk.engine.G2D;
import com.hk.engine.gui.GuiScreen;
import com.hk.engine.gui.Main;
import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;
import com.hk.json.JsonFormatException;
import com.hk.math.MathUtil;
import com.hk.math.vector.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Powered extends GuiScreen
{	
	public final World world;
	public final int scl = 10;
	private double zoom, moveX, moveY, lastX, lastY;
	private boolean paused, step, dragging, isCopying, isCutting, isPasting;
	private Point copyStart;
	private int selected, copySelected = -1;
	private long msgTime = -1;
	private String msg;
	private boolean grid;
	private Blueprint held;
	private final List<Blueprint> copied;
	
	public Powered(Main main)
	{
		super(main);
		copied = new ArrayList<>();
		world = new World(this, 256, 256);
		reset();
		grid = true;
	}

	private void reset()
	{
		zoom = 2;
		moveX = Main.WIDTH / 2F - (world.width * scl) / 2F;
		moveY = Main.HEIGHT / 2F - (world.height * scl) / 2F;

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
			double dx = mx - lastX;
			double dy = my - lastY;
			if(isRightPressed)
			{
				moveX += dx / zoom;
				moveY += dy / zoom;
			}

			dragging = dragging || MathUtil.hypot(dx, dy) > 2;
		}
		else dragging = false;
		lastX = mx;
		lastY = my;
		
		if(!paused || step)
		{
			world.updateWorld();
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
		Gdx.graphics.setForegroundFPS(60);

		float mouseX = Gdx.input.getX();
		float mouseY = Gdx.input.getY();
		g2d.pushMatrix();

		float zoomWidth = (float) (Main.WIDTH * zoom);
		float zoomHeight = (float) (Main.HEIGHT * zoom);

		float anchorx = (Main.WIDTH - zoomWidth) / 2;
		float anchory = (Main.HEIGHT - zoomHeight) / 2;

		g2d.translate(anchorx, anchory);
		g2d.scale((float) zoom, (float) zoom);
		g2d.translate((float) moveX, (float) moveY);

		Affine2 aft = applyCameraTransform().inv();
		Vector2 mi = new Vector2(mouseX, mouseY);
		aft.applyTo(mi);

		Vector2 min = new Vector2(0, 0);
		aft.applyTo(min);
		Vector2 max = new Vector2(Main.WIDTH, Main.HEIGHT);
		aft.applyTo(max);
		int startX = (int) Math.floor(min.x / scl);
		int startY = (int) Math.floor(min.y / scl);
		int endX = (int) Math.ceil(max.x / scl);
		int endY = (int) Math.ceil(max.y / scl);

		startX = MathUtil.between(0, startX, world.width);
		startY = MathUtil.between(0, startY, world.height);
		endX = MathUtil.between(0, endX, world.width);
		endY = MathUtil.between(0, endY, world.height);

		g2d.setColor(0, 0, 0, 60);

		g2d.begin(false);
		if(grid)
		{
			for(int i = startX; i <= endX; i++)
				g2d.drawLine(i * scl, startY * scl, i * scl, endY * scl);
			for(int i = startY; i <= endY; i++)
				g2d.drawLine(startX * scl, i * scl, endX * scl, i * scl);
		}
		else
		{
			g2d.drawLine(0, 0, world.width * scl, 0);
			g2d.drawLine(world.width * scl, 0, world.width * scl, world.width * scl);

			g2d.drawLine(0, 0, 0, world.height * scl);
			g2d.drawLine(0, world.height * scl, world.height * scl, world.height * scl);
		}
		g2d.end();

		g2d.setColor(Color.RED);
		for(int x = startX; x < endX; x++)
		{
			for(int y = startY; y < endY; y++)
			{
				Tile tile = world.grid(x, y);
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
			g2d.begin(false);
			g2d.setColor(Color.GREEN);
			if (copyStart != null)
			{
				int minX = Math.min(selX, copyStart.x);
				int minY = Math.min(selY, copyStart.y);
				int maxX = Math.max(selX, copyStart.x);
				int maxY = Math.max(selY, copyStart.y);
				g2d.drawRect(minX * scl, minY * scl, Math.abs(maxX - minX + 1) * scl, Math.abs(maxY - minY + 1) * scl);
			}
			else
				g2d.drawRect(selX * scl, selY * scl, scl, scl);
			g2d.end();
		}
		else if(isPasting)
		{
			g2d.setColor(0.9F, 0.9F, 1F, 0.8F);
			g2d.begin(true);
			g2d.drawRect(selX * scl, selY * scl, held.width * scl, held.height * scl);
			g2d.end();

			g2d.setColor(Color.BLUE);
			g2d.begin(false);
			g2d.drawRect(selX * scl, selY * scl, held.width * scl, held.height * scl);
			g2d.end();
			held.paintBlueprint(g2d, selX * scl, selY * scl);
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

        float zoomWidth = (float) (Main.WIDTH * zoom);
		float zoomHeight = (float) (Main.HEIGHT * zoom);

		float anchorx = (Main.WIDTH - zoomWidth) / 2;
		float anchory = (Main.HEIGHT - zoomHeight) / 2;

        aft.translate(anchorx, anchory);
		aft.scale((float) zoom, (float) zoom);
		aft.translate((float) moveX, (float) moveY);
		
		return aft;
	}

	private void resetCursor()
	{
		copyStart = null;
		isCopying = isCutting = isPasting = false;
		copySelected = -1;
		held = null;
	}

	@Override
	public void mouse(float x, float y, int button, boolean pressed)
	{
		if(isCopying)
		{
			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);
			if(copyStart == null)
				copyStart = new Point(px, py);

			if(!pressed)
			{
				int minX = Math.min(px, copyStart.x);
				int minY = Math.min(py, copyStart.y);
				int maxX = Math.max(px, copyStart.x);
				int maxY = Math.max(py, copyStart.y);

				Blueprint blueprint = new Blueprint(world, minX, minY, maxX, maxY);
				copyBlueprint(blueprint);

				if(isCutting)
				{
					for (int cx = minX; cx <= maxX; cx++)
					{
						for (int cy = minY; cy <= maxY; cy++)
							world.setToAir(cx, cy, false);
					}

					for (int cx = minX; cx <= maxX; cx++)
					{
						world.notifyNeighbor(cx, minY, Side.NORTH);
						world.notifyNeighbor(cx, maxY, Side.SOUTH);
					}
					for (int cy = minY; cy <= maxY; cy++)
					{
						world.notifyNeighbor(minX, cy, Side.WEST);
						world.notifyNeighbor(maxX, cy, Side.EAST);
					}
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

			for (int i = 0; i < held.width; i++)
			{
				for (int j = 0; j < held.height; j++)
				{
					Blueprint.TileSnapshot snapshot = held.grid(i, j);

					if (snapshot != null && !snapshot.getPiece().isAir())
					{
						Piece piece = snapshot.getPiece();
						int meta = snapshot.getMeta();
						world.setPiece(px + i, py + j, piece, meta, 2);
						if(snapshot.hasData() && snapshot.getData() != null)
							world.getData(px + i, py + j).loadFromJson(snapshot.getData());
					}
					else
						world.setPiece(px + i, py + j, Pieces.AIR, 0, 2);
				}
			}

			for (int i = 0; i < held.width; i++) {
				for (int j = 0; j < held.height; j++) {
					Blueprint.TileSnapshot snapshot = held.grid(i, j);

					if (snapshot != null && !snapshot.getPiece().isAir())
						snapshot.getPiece().onPaste(world, held, px + i, py + j);
				}
			}

			int minX = MathUtil.between(0, px, world.width);
			int minY = MathUtil.between(0, py, world.height);
			int maxX = Math.min(px + held.width, world.width);
			int maxY = Math.min(py + held.height, world.height);

			for (int cx = minX; cx < maxX; cx++)
			{
				world.notifyNeighbor(cx, minY, Side.NORTH);
				world.notifyNeighbor(cx, maxY - 1, Side.SOUTH);
			}
			for (int cy = minY; cy < maxY; cy++)
			{
				world.notifyNeighbor(minX, cy, Side.WEST);
				world.notifyNeighbor(maxX - 1, cy, Side.EAST);
			}
			for (int cx = minX; cx < maxX; cx++)
			{
				world.notifyNeighbor(cx, minY - 1, Side.SOUTH, false);
				world.notifyNeighbor(cx, maxY, Side.NORTH, false);
			}
			for (int cy = minY; cy < maxY; cy++)
			{
				world.notifyNeighbor(minX - 1, cy, Side.EAST, false);
				world.notifyNeighbor(maxX, cy, Side.WEST, false);
			}

			resetCursor();
		}
		else
		{
			if(pressed || dragging) return;

			Vector2 mi = new Vector2(x, y);
			Affine2 aft = applyCameraTransform().inv();
			aft.applyTo(mi);
			int px = (int) Math.floor(mi.x / scl);
			int py = (int) Math.floor(mi.y / scl);
			Piece p = world.getPiece(px, py);
			if(button == Input.Buttons.LEFT)
			{
				if(p.isAir())
					world.setPiece(px, py, Pieces.all[selected], true);
				else
					p.onInteract(world, px, py);
			}
			else if(button == Input.Buttons.RIGHT)
				world.setToAir(px, py, true);
		}
	}

	private void copyBlueprint(Blueprint blueprint)
	{
		if(copied.size() == 20)
			copied.remove(19);
		copied.add(0, blueprint);
		System.out.println("blueprint = " + Blueprint.exportString(blueprint.toJson()));
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
			zoom *= 1 - amt / 10.0;
			zoom = MathUtil.between(1, zoom, 10);
		}
	}

	@Override
	public void key(int key, boolean pressed)
	{
		if(pressed) return;

		if(key == Input.Keys.X)
		{
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				resetCursor();
				isCopying = isCutting = true;
			}
			else
				paused = !paused;
		}
		else if(key == Input.Keys.C)
		{
			if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
			{
				resetCursor();
				isCopying = true;
			}
			else
				reset();
		}
		else if(key == Input.Keys.V)
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
		else if(key == Input.Keys.I)
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
							blueprint = Blueprint.fromJson(Blueprint.importString(text));
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
			grid = !grid;
		}
		else if(key == Input.Keys.S)
		{
			step = true;
		}
		else if(key == Input.Keys.Q)
		{
			resetCursor();
		}
		else if(intCodes.containsKey(key))
		{
			int n = intCodes.get(key);
			selected = MathUtil.between(1, n, Pieces.all.length - 1);
			setMessage("Selected " + Pieces.all[selected].name, 2000);
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
		intCodes.put(Input.Keys.NUM_0, 10);
	}
}

// some shit
// 2b0s2ihd0mv16dokce422ue7bswqdv9jillv0y5x9eslt9br5mkogrsqmob7jby6acn820ppevsgzc94p4buo97tvutj4dfna9l1oio98fo8rwf7mus3m2oa7rbmz2ojw98y32n3w603dulvtdxywdfbqov5iq8zxmmeeo76bn6f0asfm15bw657nxc1kjoe4xbb5chqx91lagyuy0v2gb5pj6wpdcxde30ivivntozpng3uj1godby9owsyoz1vjiywq27v38brfgli5ecw7bd85qlni2jkd4haulw4oaf2exm0o73s4fxxuyftr1dfw7lo8rw4vwc1ql964yyxzx47udncq9fip7hl53obbgbuhrjenz2unqf3vgcv4gwmqv84cby53z85llz7267hru7dvgfqug9m19gldayyzyf8iy7ifn9xstjxez5b8tubkfukgirf8z9maaes5cposyyo4m9kwihuase6n11ra8z2xeenkyjbwyn9pphphzjx4o4zw3rvehn0f43yz994gpw7e446b22dt88is7u9ez2uklhu6ipgpolptxav1231xzump0vm8bfscxqo4l1d4zdyqz5ktrex7mlhvmv39bk3b9pzzx4r7hfhf0fx4ors6qhyvpu7ye1t16cga2snyx0vwj26dcurdrmypm44wiphksjpjnpu5h5j4a3wkizluz47rp92tk9g1n05pwueomlzxq9zel24o3c2ch7lryaahxrd3xiyyo473rh0o3z5t6mzf8s5uf3c5akk5b7o62za0c7hglppuyr54kbbi02ivbrb9iyf3o3c2vcq57slsyhuc65rcm3f4ly5eijhc3elw2itvyf8fu2adrfozfb1d7obetdrisv1zcxlkpr9n0apo2mzuu2gyb9yb8p5il763o9ehr1d68u3e91amgkjflmc5sldr1naw560bxgikyf1ugfmcyak2rc57y23j0j6qw80wcedjrzpkvqdje3semlzdy7k6ysrqbf0sp28ij1pshn17td824lwlulm1focn7bvckopgs7nlfvxp8m410k9nd5qsbbyxg6ynhi3iklhiflgb81t2e7knmoj9tf8hr1lltcehwmyi2od5t7rg8ovuj8m3bdyx2y9dj9zjqub4tsea9yyzzgjpvjaqx20gs4mie3r2aue6xon30ycruztjjnbibxg033kow5rj7m8eqkxv424e7nxtu5ajlj99sv9wf417vktna3u2dqxktcznggh0um88erzk0vu271e0ku2hn4h2awtluievrv4992inwg89zhnc3bi78jy9qhzrz74ht1oyguzyp2xv3i87b6nx

// half-adder
// 3wow6uxdxkpbz3xjql2sfbcsuqw1m8qnsves126m11djfc4e2yjm9x1qeuujgqucjr9bjlz5jwun2et03fdxp9c5ubbz1adtqlw8qwvozmfceelvc5jjbcemfjpyrdxu1htj691agfsa0aev4y735es80m365ycra308mpdefl9p92u31q85nw1y0hiktc1w6jic6donrpdlg1jcgoflmiobqumjrgijl7bvfv352gkbmbfe178nasujxr71hywo8b26v4erawie5wpj2pzy29avfx99zefnzdr1brea0whe6m3vs5i82qzwtnqtwtjewnm05qi8qimfxcbt0vfe3svhcsa06jke3hdimjzu66x0oslx56bsoufibhfio2n3tji4ja2fvjhwvybut7mcta9xdirf173qng2i8d217zmb0yfs25mf8i6ss78urpgod2ygw8864n4mw87ccpw83pog4pl9in7kqrz57k6opd168dv0evn58pfmic2g8pelbl1srqn0ynpfsw0pzm15rfgbcbgj5lg8qggah6a0xo4pozufuiyyevq31j2ntt6g40nzdma62qvf5fr5qfafpifuovntxorrzytf3t1swv5m4qixj4l2y3nodv8nykuokh8exr302n6m1rorryqt1vlbhgwxxrnjbg0eths596o9pfklr9ehde3hfulf9cwo57bu5hxirevxvxzkn5f92fmtskters1zqk2uy5us8j1bna7q0key062ugz0ui6xbz30uucrhnqt957q84g1o1op85hl4hkhkwc9k8yhu418ytyn66xzstzsjg3wsnvshuoiq1m365d4ox47h6sbpaswo3kdv2857rnexg5hqbhy6etg2n6fjxhb6wsg6x46oklwyp0qod52pkfbe9sudyh90ax99k3w813l92sok0p3r8uc5mr6r0h1vqvs3az93nklxml