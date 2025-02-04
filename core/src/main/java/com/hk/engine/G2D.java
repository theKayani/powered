package com.hk.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntMap;
import com.hk.engine.gui.Main;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class G2D
{
	private final IntMap<BitmapFont> fonts;
	private int fontSize;
	private BitmapFont font;
	private final SpriteBatch batch;
	private final ShapeRenderer renderer;
	private final Camera camera;
	private final List<Matrix4> matrices;
	private final List<Color> colors;
	private final List<Integer> fontSizes;
	private final List<String> texturePaths;
	private final List<Texture> textures;
	private boolean centered;
	public final float width, height;
	
	public G2D(ShapeRenderer renderer, Camera camera)
	{
		this.renderer = renderer;
		this.camera = camera;
		matrices = new ArrayList<>();
		colors = new ArrayList<>();
		fontSizes = new ArrayList<>();
		textures = new ArrayList<>();
		texturePaths = new ArrayList<>();
		batch = new SpriteBatch();
		fonts = new IntMap<>();
		this.width = Main.WIDTH;
		this.height = Main.HEIGHT;

		loadFont(8);
		loadFont(12);
		loadFont(18);
		loadFont(32);
		
		setFontSize(18);
	}
	
	private void loadFont(int fontSize)
	{
		FileHandle f = Gdx.files.internal("font.ttf");
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(f);
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.size = fontSize * 2;
		param.flip = true;
		fonts.put(fontSize, gen.generateFont(param));
	}
	
	public int registerImage(String path)
	{
		int indx = texturePaths.indexOf(path);
		if(indx == -1)
		{
			texturePaths.add(path);
			textures.add(new Texture(Gdx.files.internal((Main.isDesktop() ? "android/assets/" : "") + "images/" + path)));
			indx = textures.size() - 1;
		}
		return indx;
	}
	
	public Texture getImage(int id)
	{
		return textures.get(id);
	}

	public void reset()
	{
		matrices.clear();
		colors.clear();
		fontSizes.clear();
	}
	
	public G2D begin(boolean filled)
	{
		renderer.begin(filled ? ShapeType.Filled : ShapeType.Line);
		return this;
	}
	
	public G2D beginPoint()
	{
		renderer.begin(ShapeType.Point);
		return this;
	}
	
	public G2D drawLine(float x1, float y1, float x2, float y2)
	{
		renderer.line(x1, y1, x2, y2);
		return this;
	}
	
	public G2D drawPoint(float x, float y)
	{
		renderer.point(x, y, 0);
		return this;
	}

	public G2D drawRect(float x, float y, float w, float h)
	{
		renderer.rect(getCenteredX(x, w), getCenteredY(y, h), w, h);
		return this;
	}

	public G2D drawRoundRect(float x, float y, float w, float h, float wa, float ha)
	{
		drawLine(x + wa / 2, y, x + w - wa / 2, y);
		drawLine(x + wa / 2, y + h, x + w - wa / 2, y + h);
		drawLine(x, y + ha / 2, x, y + h - ha / 2);
		drawLine(x + w, y + ha / 2, x + w, y + h - ha / 2);
		renderer.arc(x + wa / 2, y + ha / 2, wa / 2, 180, 90);
		renderer.arc(x + w - wa / 2, y + ha / 2, wa / 2, 270, 90);
		renderer.arc(x + wa / 2, y + h - ha / 2, wa / 2, 90, 90);
		renderer.arc(x + w - wa / 2, y + h - ha / 2, wa / 2, 0, 90);
		if(renderer.getCurrentType() == ShapeType.Filled)
		{
			drawRect(x + wa / 2, y, w - wa, ha / 2);
			drawRect(x + wa / 2, y + h - ha / 2, w - wa, ha / 2);
			drawRect(x, y + ha / 2, w, h - ha);
		}
		return this;
	}

	public G2D drawArc(float x, float y, float radius, float sa, float ea)
	{
		x = getCenteredX(x + radius, radius * 2);
		y = getCenteredY(y + radius, radius * 2);
		renderer.arc(x, y, radius, sa, ea - sa);
		return this;
	}
	
	public G2D drawCircle(float x, float y, float radius)
	{
		renderer.circle(getCenteredX(x + radius, radius * 2), getCenteredY(y + radius, radius * 2), radius);
		return this;
	}
	
	public G2D drawOval(float x, float y, float w, float h)
	{
		renderer.ellipse(getCenteredX(x, w), getCenteredY(y, h), w, h);
		return this;
	}
	
	public G2D drawImage(int id, float x, float y)
	{
		if (id != -1)
		{
			if (id < 0 || id >= textures.size())
			{
				new ArrayIndexOutOfBoundsException("Texture Does Not Exist: " + id).printStackTrace(System.err);
				Gdx.app.exit();
				return null;
			}
			Texture texture = textures.get(id);
			batch.setProjectionMatrix(camera.combined);
			batch.setTransformMatrix(getMatrix());
			batch.begin();
			if (centered)
			{
				x -= texture.getWidth() / 2F;
				y -= texture.getHeight() / 2F;
			}
			batch.draw(texture, x, y);
			batch.end();
		}
		return this;
	}
	
	public G2D drawImage(int id, float x, float y, float xScale, float yScale)
	{
		if (id != -1)
		{
			if (id < 0 || id >= textures.size())
			{
				new ArrayIndexOutOfBoundsException("Texture Does Not Exist: " + id).printStackTrace(System.err);
				Gdx.app.exit();
				return null;
			}
			Texture texture = textures.get(id);
			if (centered)
			{
				x -= (texture.getWidth() * xScale) / 2F;
				y -= (texture.getHeight() * yScale) / 2F;
			}
			pushMatrix();
			translate(x, y);
			scale(xScale, yScale);
			batch.setProjectionMatrix(camera.combined);
			batch.setTransformMatrix(getMatrix());
			batch.begin();
			batch.draw(texture, 0, 0);
			batch.end();
			popMatrix();
		}
		return this;
	}
	
	public G2D drawImage(TextureRegion region, float x, float y, float xScale, float yScale)
	{
		if (centered)
		{
			x -= (region.getRegionWidth() * xScale) / 2F;
			y -= (region.getRegionHeight() * yScale) / 2F;
		}
		pushMatrix();
		translate(x, y);
		scale(xScale, yScale);
		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(getMatrix());
		batch.begin();
		batch.draw(region, 0, 0);
		batch.end();
		popMatrix();
		return this;
	}
	
	public G2D drawImage(TextureRegion region, float x, float y)
	{
		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(getMatrix());
		batch.begin();
		if (centered)
		{
			x -= region.getRegionWidth() / 2F;
			y -= region.getRegionHeight() / 2F;
		}
		batch.draw(region, x, y);
		batch.end();
		return this;
	}
	
	public G2D end()
	{
		renderer.end();
		return this;
	}

	public G2D beginString()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.setTransformMatrix(getMatrix());
		batch.begin();
		enableBlend();
		return this;
	}

	public G2D drawString(float x, float y, String str)
	{
		GlyphLayout layout = new GlyphLayout(font, str, getColor(), 0, centered ? Align.center : Align.left, false);
		if(centered)
			y -= layout.height / 2;

		font.setColor(getColor());
		font.draw(batch, layout, x, y);
		return this;
	}

	public G2D endString()
	{
		batch.end();
		disableBlend();
		return this;
	}

	public int getStringWidth(String s)
	{
		return (int) new GlyphLayout(font, s).width;
	}

	public int getStringHeight(String s)
	{
		int total = 0;
		String[] ss = s.split("\n");
		for(int i = 0; i < ss.length; i++)
		{
			total += (int) new GlyphLayout(font, ss[i]).height;
			if(i < ss.length - 1)
				total += 10;
		}
		return total;
	}
	
	public G2D enableBlend()
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		return this;
	}
	
	public G2D disableBlend()
	{
		Gdx.gl.glDisable(GL20.GL_BLEND);
		return this;
	}
	
	public G2D enableCentered()
	{
		centered = true;
		return this;
	}
	
	public G2D disableCentered()
	{
		centered = false;
		return this;
	}
	
	public G2D pushFontSize()
	{
		fontSizes.add(getFontSize());
		return this;
	}
	
	public int getFontSize()
	{
		return fontSize;
	}
	
	public G2D setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
		font = fonts.get(fontSize);
		return this;
	}
	
	public G2D popFontSize()
	{
		setFontSize(fontSizes.remove(fontSizes.size() - 1));
		return this;
	}
	
	public G2D pushColor()
	{
		colors.add(getColor());
		return this;
	}
	
	public Color getColor()
	{
		return renderer.getColor();
	}
	
	public G2D setColor(Color color)
	{
		renderer.setColor(color);
		return this;
	}

	public G2D setColor(int rgb)
	{
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		renderer.setColor(r / 255F, g / 255F, b / 255F, 1F);
		return this;
	}

	public G2D setColor(int argb, boolean alpha)
	{
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		int a = alpha ? (argb >> 24) & 0xFF : 255;
		renderer.setColor(r / 255F, g / 255F, b / 255F, a / 255F);
		return this;
	}

	public G2D setColor(int r, int g, int b)
	{
		renderer.setColor(r / 255F, g / 255F, b / 255F, 1F);
		return this;
	}

	public G2D setColor(int r, int g, int b, int a)
	{
		renderer.setColor(r / 255F, g / 255F, b / 255F, a / 255F);
		return this;
	}

	public G2D setColor(float r, float g, float b)
	{
		renderer.setColor(r, g, b, 1F);
		return this;
	}
	
	public G2D setColor(float r, float g, float b, float a)
	{
		renderer.setColor(r, g, b, a);
		return this;
	}
	
	public G2D popColor()
	{
		setColor(colors.remove(colors.size() - 1));
		return this;
	}
	
	public G2D pushMatrix()
	{
		matrices.add(getMatrix().cpy());
		return this;
	}
	
	public Matrix4 getMatrix()
	{
		return renderer.getTransformMatrix();
	}
	
	public G2D setMatrix(Matrix4 matrix)
	{
		batch.setTransformMatrix(matrix);
		renderer.setTransformMatrix(matrix);
		return this;
	}
	
	public G2D popMatrix()
	{
		setMatrix(matrices.remove(matrices.size() - 1));
		return this;
	}

	public void setLineWidth(float lineWidth)
	{
		Gdx.gl.glLineWidth(lineWidth);
	}
	
	public float getCenteredX(float x, float width)
	{
		return centered ? x - width / 2F : x;
	}
	
	public float getCenteredY(float y, float height)
	{
		return centered ? y - height / 2F : y;
	}
	
	public G2D translate(float x, float y)
	{
		renderer.translate(x, y, 0F);
		return this;
	}
	
	public G2D rotate(float degrees)
	{
		renderer.rotate(0F, 0F, 1F, degrees);
		return this;
	}
	
	public G2D rotate(float degrees, float x, float y)
	{
		translate(x, y);
		renderer.rotate(0F, 0F, 1F, degrees);
		translate(-x, -y);
		return this;
	}

	public G2D scale(float x, float y)
	{
		renderer.scale(x, y, 1F);
		return this;
	}
	
	public ShapeRenderer getRenderer()
	{
		return renderer;
	}
	
	public SpriteBatch getSpriteBatch()
	{
		return batch;
	}

	public void dispose()
	{
		batch.dispose();
		for(BitmapFont font : fonts.values())
		{
			font.dispose();
		}
		renderer.dispose();
	}
}
