package powered;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import com.hk.game.pieces.Circuit;
import com.hk.game.pieces.Piece;
import com.hk.game.pieces.Pieces;
import com.hk.json.Json;
import com.hk.json.JsonArray;
import com.hk.json.JsonObject;
import com.hk.math.MathUtil;
import com.hk.math.vector.Point;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Blueprint
{
	public final int width, height;
	private final Map<Point, TileSnapshot> data;
	private final Map<Point, CircuitSnapshot> circuits;
	private final TileSnapshot[][] grid;

	public Blueprint(World world, int minX, int minY, int maxX, int maxY)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);
		width = maxx - minx + 1;
		height = maxy - miny + 1;
		grid = new TileSnapshot[width][height];
		data = new HashMap<>();
		circuits = new HashMap<>();
		copyRegion(world, minx, miny, maxx, maxy);
	}

	private Blueprint(int width, int height)
	{
		this.width = width;
		this.height = height;
		data = new HashMap<>();
		circuits = new HashMap<>();
		grid = new TileSnapshot[width][height];
	}

	public void paintBlueprint(G2D g2d, int x, int y)
	{
		g2d.pushMatrix();
		g2d.setColor(Color.RED);
		g2d.translate(x, y);
		for(int xc = 0; xc < width; xc++)
		{
			for(int yc = 0; yc < height; yc++)
			{
				TileSnapshot snapshot = grid[xc][yc];
				if(snapshot == null)
					continue;
				Piece p = snapshot.piece;
				if(p.isAir())
					continue;
				int meta = snapshot.meta;
				p.paintPiece(g2d, null, xc * 10, yc * 10, xc, yc, meta, false);
			}
		}
		g2d.popMatrix();
	}

	public JsonObject toJson()
	{
		JsonObject obj = new JsonObject();
		obj.put("width", width);
		obj.put("height", height);
		JsonArray tiles = new JsonArray();

		Set<Map.Entry<Point, TileSnapshot>> entries = data.entrySet();
		for (Map.Entry<Point, TileSnapshot> entry : entries)
		{
			Point point = entry.getKey();
			TileSnapshot datum = entry.getValue();
			JsonObject tile = new JsonObject();
			tile.put("x", point.x);
			tile.put("y", point.y);
			tile.put("piece", datum.piece.getShortName());
			if(datum.meta != datum.piece.getDefaultMeta())
				tile.put("meta", datum.meta);
			if(datum.hasData)
			{
				if(datum.data == null)
					tile.put("data", true);
				else
					tile.put("data", datum.data);
			}
			tiles.add(tile);
		}

		obj.put("tiles", tiles);

		JsonArray circuits = new JsonArray();

		for (CircuitSnapshot circuitSnapshot : this.circuits.values())
		{
			JsonObject circuit = new JsonObject();

			circuit.put("x", circuitSnapshot.xCoord);
			circuit.put("y", circuitSnapshot.yCoord);
			circuit.put("width", circuitSnapshot.width);
			circuit.put("height", circuitSnapshot.height);
			JsonArray connects = new JsonArray();
			for (Map.Entry<Point, Point> entry : circuitSnapshot.worldConnects.entrySet())
			{
				Point w = entry.getKey();
				Point e = entry.getValue();

				JsonObject connect = new JsonObject();
				connect.put("wx", w.x);
				connect.put("wy", w.y);
				connect.put("ex", e.x);
				connect.put("ey", e.y);
				connects.add(connect);
			}
			circuit.put("connects", connects);
			circuit.put("circuit", circuitSnapshot.circuitWorld.toJson());
			circuits.add(circuit);
		}

		if(!circuits.isEmpty())
			obj.put("circuits", circuits);
		return obj;
	}

	// b2rg2oym9i2xp8f4uug2k2lft6asjgaydzmmksqc4esnqq45xubfd7yx2a0dw4ctsxc7wp3t7kv3720ya1heyiv86xfau26nh8bqsnggwn9hmwbp1j92xkz5sjcbezqsblig2qnc1non8l80ew3tlef5cyq55euny5iljo80s9hcxl26ypwvl51csxte7ny5abvovqbqh3y5uzzfssc463nuot6ekdmr4d9q1g07lthlz29bgut09k76raykxor8g20bvdozyj2iinfulixx2ycgo4n1j9ezxansrnh4u1f4e6g5yab3s1sydrza9aix7k8zrqiokx3rrr9obgccterq9mhvsbqti4ycjoi188mjgcrz5euv66vqwdipbus5sj4maogcyrlvj7uzoni2nso53xk9kus2zx1vbttkm33z171ioc4907dwy2uis8he66um9red1jid4gi68h35urnx1z5w8c5e6smh7cpuqgk6tgede8y03bomw6y2oj1kgs34cxw2eqrl3pw2d5pwadyk9p0q8t5e0ozcd0n1giiffooguqyzl9jjfzdyep8z3fc6entch8mftgc1yq706yqd5f3kb9rmwxhafvxjnvg7o7zi8uqr211w1501vwn8yv9v1oep2blace8nzyujjthktfyu27x5hcsxokaiarzydmbb6tjrdydm3fo4ehp2foygz7x6f1dm12wwt8l8xpp0o3t4pcnef39a0q2n8sehiv39qxoizz7c3up9znqgt9vv4rsrbeh9vivczziczcre917i9br60jfbe25kiit3bnr0f2stnfjwqs6abpv3i90m9hs6awk1w6guhkcockb99athrjxtazylqh57w0qthmqdrycq06f2u1gzgqc1kn39gr5pq55x1se351u3t8hdtrsdt7x6k1tpbbs36a5uzdzxohvbdt6tsfm7trxvp40tx4lhxm62yq39jiilckotmzl3c54lnukvbm34rt0h9xh76524usw22yzxsthm4da1rn8wde2jttymfhr4zjfj2pwn45b9ubby8nvnhmv4p2k4hgz73n4hhdx1jyj305twulxohf5x5szuk6t3napimwnbt3iv7jes66ruuhp73ju1udprx0qmrxcazo4wy3k3tl5p6g8z41dg3v8m73gj7gn4j2c6y5vp8dqxzukgh8vuxxu12qjio5u1yv4slrnthp341z0ukdiot308hww8opzpow91ipztd3uezgad8cpx6utaeptavjj0tme4v3r516ez4sbixt7ezdek2faegibxjr5892kjtmt3ls0z87ya1w2ts40k34mdlklg1s7zzewaulw4vbb4p3wj6mtvpy4dwoqg2b6yozspmgn80g9aa4lew57739gpgo9hvy47508b1mtxf2ki78iy4l9dx3j52kv9jb4ck9ikpl3hk6kl6g1kp5p3jukkk2y7mxuz0hojmpinro1dvglic5vnysz9hgn811bqjtulhgzch1gmqddhcf52fqpkufkq87ujvc89bm1tbk1m630uettzqeotlh7hbxpgyiq0xz47ga1jawzviygy81nl8h8bces71km3p30s64t5o7sq0y1cb6xii7olukpjl7e7hln2izektbl0g1g5166f7grd1mb2y2f28ky4u2cqv6fos0zeni2a9gwslhvl4llpdnnkaweulb6ab9tvpz0l6cgnz5ksb8ulq6c9ww81duypasqzak9nbt89x9w2eh9z8wqqegdtk61eurekyniloxzflhlo6s5zbuy9l5c2y5prz8pnpjovflct0b363uf2kh224cjfxeh3s5btx2e6ftcj2i53u6ld2l7kqlyouwe9eaqr1lblrj2znm72239vl5sjcsrzks500t5gvmsuopw5drqt4v2as3z996vct4krlpx39ft83ve7sj4m8sbuv22uecqn2p42v8idclk3rnjztlhud5h5dv9hv58ezqhq8sot256ms204ym7vpu7z9j56r0olvtz77c8kbxt26wqd1scjkdaptry7d5cqmes6ygq2nzzhuqgupu78nfm5gdmchdsw1h1y2jlzc8yqy2ybwf8hx6ksxy9zbe48n8hd3ncwirviwvrnh081gwsbfoii1lmggwf604bzjnz9owqet9nnssx3iz9oivyuomgtoxdy4dqwu2bjrk2cxcn9bwtjnfh3rpfvwzti9sqdbpzpf06l3mp1ruougxifi3zm9j8cjjwh5j02n7e32ldohnioczod5asd4hdl5pj7qhxah9qpr3bj5xd6myj5x460gaxu26tdredhvv9wqo2sgw3oj0dhgn2ukkptgzcetbz1ni40vwod82b6lq8o29tmjs3979exf50lioe6sb05ygfsfyqzy8jjts2t752gk52iut63igqqc4dcr5j7u0gaqlpb0234ctz1ieobn6eviu58fo3q6hky165k9d9wxv0sfeyn1x4xgnbup02r2vd6ps82u5jh3dwx0o129r66q1dj02sgnhbmip4zcfxgitjkxjtn4xvw64mzrxw86paxs5omc9whl5yf9bf70p737lort7jdx0gw2amrvkqw1x6b6ye62153l3vlbmw83qfv46dv3rvikk6gw6ktgkyylb2sn0r91rzznfs2280a6xapp77uys7p9kmq85afij8iojq9eaiaago9v00js2jkkmjycj4bipan6yoz512mlmd3azu5kpv26wszxu8uitp08r6b7sdcy8n8wymkh0s2lxjkmfgvntq4r1gcwyo5wy0gdh93igwfgyy5ueib2usemq81c19l4ejyfcin68gdhaa59a30gbkojuvahbpkgoklme1lgue4o0uu7r9tvozpvbp3ik4edkwe09b8apaw7ca0ru6dxil9dsshnxba4ew9c7nsld0whtpvyv3kw47j0d37t9cs0u66tnh0ky4r9zzx2wwuu3xekoxcb2a9kxv6msu4oqfukp9x33gycvcr5m7g1qskx7yznq70658ob3wg8z119lrf87r6yh0odngt5iyewmh35ays37axgyp0izicj4zujbf6mj0wmj0rcnop77ed2blnbwi5setby5o1fkpiewvww5tfsjcnewmjuog0cuswrnknjrt6dgspawq18rttitpzpkss43mhrnzipte0im17ldjawr48ql5jwzp91ady9xdjov2bo17eh6vcyplgi3d5pj9fbnqoolpst6cdim98xs8yvnijnl4pvkamymtkm4nvdf6t4a15ja8ks9fqqduf3b5jmhqczeoezpyq0yv9sd5pegsdtt1pom0drcnqnctsenacc29y14csa4e57alh3vsz2bbyr2ozkw446pggz144armf6jmpnf1hs6emwazcw16woscmxoy8mktb0j0fhuyz6b3q87m0om08c9fdq139c04lsfolosq2q6rbn23y32zjcbu8q9dvs67rnh3b1nthhurh725l32bice3ihwinnjw3rc7ie7m0ddckf6ddy6sv372eioevx5gcahb3gcvr08k1rmyhwk9aqkmi4pnnya4gh7rxm7ko6v70p1irua4qhqd3f8y9kjdfocbainj8frn4tj0i8f0aaf3349ngu7l8zyd59vjqzjvkpdrfoklwxq98ndnovvxaen7zg8fjy38r9oqr1i54yd4radkpk283zwvkyl7h9l4onyrye7oxt2d9ku5p1t5znii3t6m039ftrzn8y0kznypn5l1snx36zroyacisyq2c95jy9gdfz3cv8pv1r39f8dg1nzy9wyad2d4125yqhdian5f8euyv76tk84dsl7d7398nrxxx8mko4ald6rpf6d51gu4dib0sv8d39pcshxqrs2v7khbvqrhi6zlyxzevtsnyb1i4mjxv4di38ad8j6u32tzvpz0b19fa9rznz8cnp86m9grt79xae88j23i0sfvu5tqiq1ks5evvrs4845rgm6942q2bcks0qy7idz1wiob0n003nsvb0g9xrlvmxy3ghnuvgu92lzz1ja5ffjn2ennaweoeaoqxr85xbp9r0x038sylvsdr0lnhdyzwt6hm6fhl7ff5150muaxre3p5s7wki6y64r53n1spnwmus6xde3dxsljbhho8fep4hka7u846gg8nxq0u0vh8abvpzdmf1s2boccg9fxsxtoggz5xdub8m3q8via8c442vkl2mdba9e1dlaf3z1df31pluytxiqk32tx4gan6r2cbdzjrp32v8kfv9ejp5ydu7ipqpbnad6byvnmjd17y4ah0lvama7anke1j00aa0pq84036zxlbsgehycxy4sci7f28am89evwzsnwozygfzux6c0cky4d731e1k0hcqm74gwya99ivdn0hh64ygmyhahpsstw9k1fwo8upkfa26pfau3mbkirea20xl4xt1vn2ww1t0ndhjmfv7tpmb1hc7050diphyteohiphv13l3bo2e9zd8khgin5mueqyxink4h2dxr2srrry1bu6mnpsa011tdhrovnizhpxr70vok1ozqazp65ccpqjbzuodh4mz33m3ndq5ju8xyhpkk352k87ja6i8gg23e3lxf4ixxcoe3aigmqv75aggtinsbkuep7yolvpm9gp68guq790liabmgvvz1931ejqnnj0r2973c14ifd38elu8o9pp8q2rxt1csbkj2qn8v5x1w5sjgeau0d1eveual9yzht37dyyis3d51modcu0qzib2xlox9lxc2qvcgdtl3p6hjr17zcrheo5h4i5m4czvdyyqxpyasnzdmbts6z69ipg1h3tsukwu1p1seir54zm6dykbed0qoyt8tl2f55jsl1noo57yrz2zr8sjne40hzhjgp0ubk2yzw5j8cck45mdku14d1768hs8uk24ulj469gajbo2mrle3hbp8twvjqctr3fzdml4t

	public static Blueprint fromJson(JsonObject json)
	{
		int width = json.getInt("width");
		int height = json.getInt("height");
		JsonArray tiles = json.getArray("tiles");
		Blueprint blueprint = new Blueprint(width, height);

		for (int i = 0; i < tiles.size(); i++)
		{
			JsonObject tile = tiles.get(i).getObject();
			Point point = new Point(tile.getInt("x"), tile.getInt("y"));
			Piece piece = Pieces.fromShortName(tile.getString("piece"));
			int meta;
			if (tile.isNumber("meta"))
				meta = tile.getInt("meta");
			else
				meta = piece.getDefaultMeta();
			boolean hasData = false;
			JsonObject data = null;
			if(tile.isBoolean("data"))
			{
				hasData = tile.getBoolean("data");
			}
			else if(tile.isObject("data"))
			{
				hasData = true;
				data = tile.getObject("data");
			}
			TileSnapshot snapshot = new TileSnapshot(piece, meta, hasData, data);
			blueprint.grid[point.x][point.y] = snapshot;
			blueprint.data.put(point, snapshot);
		}

		if(json.contains("circuits"))
		{
			JsonArray circuits = json.getArray("circuits");
			for (int i = 0; i < circuits.size(); i++)
			{
				JsonObject circuit = circuits.get(i).getObject();
				int cx = circuit.getInt("x");
				int cy = circuit.getInt("y");
				int cw = circuit.getInt("width");
				int ch = circuit.getInt("height");
				Blueprint circuitWorld = Blueprint.fromJson(circuit.getObject("circuit"));

				CircuitSnapshot circuitSnapshot = new CircuitSnapshot(cx, cy, cw, ch, circuitWorld);
				JsonArray connects = circuit.getArray("connects");
				for (int j = 0; j < connects.size(); j++)
				{
					JsonObject connect = connects.get(j).getObject();
					Point wp = new Point(connect.getInt("wx"), connect.getInt("wy"));
					Point ep = new Point(connect.getInt("ex"), connect.getInt("ey"));
					circuitSnapshot.worldConnects.put(wp, ep);
				}

				blueprint.circuits.put(new Point(cx, cy), circuitSnapshot);
			}
		}

		return blueprint;
	}

	public static boolean canCopy(World world, int minX, int minY, int maxX, int maxY)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);

		for (int cy = miny; cy <= maxy; cy++)
		{
			if(world.getPiece(minX, cy).isCircuit() && world.getData(minX, cy, Circuit.class).isOutBounds(minX, minY, maxX, maxY))
				return false;
			if(world.getPiece(maxX, cy).isCircuit() && world.getData(maxX, cy, Circuit.class).isOutBounds(minX, minY, maxX, maxY))
				return false;
		}
		for (int cx = minx; cx <= maxx; cx++)
		{
			if(world.getPiece(cx, minY).isCircuit() && world.getData(cx, minY, Circuit.class).isOutBounds(minX, minY, maxX, maxY))
				return false;
			if(world.getPiece(cx, maxY).isCircuit() && world.getData(cx, maxY, Circuit.class).isOutBounds(minX, minY, maxX, maxY))
				return false;
		}

		return true;
	}

	private void copyRegion(World world, int minX, int minY, int maxX, int maxY)
	{
		int minx = Math.min(minX, maxX);
		int miny = Math.min(minY, maxY);
		int maxx = Math.max(minX, maxX);
		int maxy = Math.max(minY, maxY);

		for (int cy = miny; cy <= maxy; cy++)
		{
			for (int cx = minx; cx <= maxx; cx++)
			{
				Point point = new Point(cx - minx, cy - miny);

				Piece piece = world.getPiece(cx, cy);
				if(piece.isAir())
					continue;

				int meta = piece.cleanMeta(world.getMeta(cx, cy));
				TileSnapshot snapshot;
				if(piece.isCircuit())
				{
					Circuit circuit = world.getData(cx, cy, Circuit.class);
					Point cp = new Point(circuit.xCoord - minx, circuit.yCoord - miny);
					if(!circuits.containsKey(cp))
						circuits.put(cp, new CircuitSnapshot(circuit, minx, miny));

					snapshot = new TileSnapshot(piece, meta, false, null);
				}
				else
				{
					TileData data = world.getData(cx, cy);
					JsonObject obj = data == null ? null : data.saveToJson();
					snapshot = new TileSnapshot(piece, meta, data != null, obj);
				}

				this.data.put(point, snapshot);
				this.grid[point.x][point.y] = snapshot;
			}
		}

//		System.out.println("data = " + data);
//		System.out.println("grid = " + Arrays.deepToString(grid));
//		System.out.println("circuits = " + circuits);
	}

	public void pasteBlueprint(World world, int px, int py, boolean doNotify)
	{
		boolean isPasting = world.isPasting();
		world.setPasting(true);
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				TileSnapshot snapshot = grid(i, j);

				if (snapshot != null && !snapshot.piece.isAir())
				{
					Piece piece = snapshot.piece;
					int meta = snapshot.meta;
					world.setPiece(px + i, py + j, piece, meta, 2);
					if(snapshot.hasData && snapshot.data != null)
						world.getData(px + i, py + j).loadFromJson(snapshot.data);
				}
				else
					world.setPiece(px + i, py + j, Pieces.AIR, 0, 2);
			}
		}

		for (CircuitSnapshot circuitSnapshot : circuits.values())
		{
			Circuit circuit = new Circuit(world, px + circuitSnapshot.xCoord, py + circuitSnapshot.yCoord, circuitSnapshot.width, circuitSnapshot.height);
			circuit.initializeWorld(circuitSnapshot.circuitWorld.width, circuitSnapshot.circuitWorld.height);

			for (int x = 0; x < circuit.width; x++)
			{
				for (int y = 0; y < circuit.height; y++)
				{
					world.setData(circuit.xCoord + x, circuit.yCoord + y, circuit);
				}
			}

			for (Map.Entry<Point, Point> entry : circuitSnapshot.worldConnects.entrySet())
			{
				Point wp = entry.getKey().clone().add(circuit.xCoord, circuit.yCoord);
				Point ep = entry.getValue();

				circuit.connect(ep.x, ep.y, wp.x, wp.y, false);
			}

			circuitSnapshot.circuitWorld.pasteBlueprint(circuit.getCircuitWorld(), 0, 0, true);
		}

		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				TileSnapshot snapshot = grid(i, j);

				if (snapshot != null && !snapshot.piece.isAir())
					snapshot.piece.onPaste(world, this, px + i, py + j);
			}
		}
		world.setPasting(isPasting);

		int minX = MathUtil.between(0, px, world.width);
		int minY = MathUtil.between(0, py, world.height);
		int maxX = Math.min(px + width - 1, world.width);
		int maxY = Math.min(py + height - 1, world.height);

		if(doNotify)
		{
			world.notifyNeighbors(minX, minY, maxX, maxY);
			for (int cx = minX; cx <= maxX; cx++)
			{
				world.notifyNeighbor(cx, minY - 1, Side.SOUTH, false);
				world.notifyNeighbor(cx, maxY + 1, Side.NORTH, false);
			}
			for (int cy = minY; cy <= maxY; cy++)
			{
				world.notifyNeighbor(minX - 1, cy, Side.EAST, false);
				world.notifyNeighbor(maxX + 1, cy, Side.WEST, false);
			}
		}
	}

	public static JsonObject importString(String encoded)
	{
		try
		{
			byte[] bytes = Base64.getDecoder().decode(encoded);
			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			return Json.read(new GZIPInputStream(bin), StandardCharsets.UTF_8).getObject();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public static String exportString(JsonObject obj)
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			Json.write(new GZIPOutputStream(bout), StandardCharsets.UTF_8, obj);
			return Base64.getEncoder().withoutPadding().encodeToString(bout.toByteArray());
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public TileSnapshot grid(int x, int y)
	{
		return grid[x][y];
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Blueprint blueprint = (Blueprint) o;
		return width == blueprint.width && height == blueprint.height && data.equals(blueprint.data) && circuits.equals(blueprint.circuits);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(width, height, data, circuits);
	}

	@Override
	public String toString() {
		return "Blueprint{" +
				"width=" + width +
				", height=" + height +
				", data=" + data +
				", circuits=" + circuits +
				", grid=" + Arrays.deepToString(grid) +
				'}';
	}
}
