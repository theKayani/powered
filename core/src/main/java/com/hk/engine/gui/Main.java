package com.hk.engine.gui;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.hk.engine.G2D;
import com.hk.engine.GDXMain;
import com.hk.engine.Handler;
import com.hk.engine.util.ImageRegister;
import com.hk.engine.util.Version;
import com.hk.game.TestScreen;
import powered.Powered;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Main implements ImageRegister
{
	public static boolean DEBUG = true;
	public static final int WIDTH = 1280, HEIGHT = 720;
	public static final Version VERSION = new Version("0.0.1");
	public static final int VERSION_NUM = VERSION.getCode();
	public final GDXMain gdxParent;
	private GuiScreen currScreen;

	public Main(GDXMain justIncase)
	{
		this.gdxParent = justIncase;
		setCurrentScreen(new Powered(this));
	}

	public void setCurrentScreen(GuiScreen currScreen)
	{
		if (this.currScreen != null)
		{
			this.currScreen.onGuiClose();
		}
		this.currScreen = currScreen;
		if (currScreen != null)
		{
			currScreen.initialize(this);
		}
	}

	public GuiScreen getCurrentScreen()
	{
		return currScreen;
	}

	public void update()
	{
		float dt = Gdx.graphics.getDeltaTime();
		currScreen.update(dt);
		Iterator<GuiOverlay> iterator = currScreen.overlays.iterator();
		while(iterator.hasNext())
		{
			GuiOverlay overlay = iterator.next();
			if(overlay.visible)
				overlay.update(dt);
			if(overlay.remove)
				iterator.remove();
		}
	}

	public void render(G2D g2d)
	{
		g2d.setColor(Color.WHITE);
		g2d.pushMatrix();
		currScreen.render(g2d);
		g2d.popMatrix();
		List<GuiOverlay> overlays = currScreen.overlays;
		for(GuiOverlay overlay : overlays)
		{
			if(overlay.visible)
				overlay.render(g2d);
		}
	}

	public void mouse(float x, float y, int button, boolean pressed)
	{
		boolean used = false;
		ListIterator<GuiOverlay> iterator = currScreen.overlays.listIterator(currScreen.overlays.size());
		while(iterator.hasPrevious())
		{
			GuiOverlay overlay = iterator.previous();
			if(overlay.visible && overlay.mouse(x, y, button, pressed))
			{
				used = true;
				break;
			}
		}
		if(!used)
			currScreen.mouse(x, y, button, pressed);
	}

	public void mouseMoved(float x, float y)
	{
		boolean used = false;
		ListIterator<GuiOverlay> iterator = currScreen.overlays.listIterator(currScreen.overlays.size());
		while(iterator.hasPrevious())
		{
			GuiOverlay overlay = iterator.previous();
			if(overlay.visible && overlay.mouseMoved(x, y))
			{
				used = true;
				break;
			}
		}
		if(!used)
			currScreen.mouseMoved(x, y);
	}

	public void mouseWheel(int amt)
	{
		boolean used = false;
		ListIterator<GuiOverlay> iterator = currScreen.overlays.listIterator(currScreen.overlays.size());
		while(iterator.hasPrevious())
		{
			GuiOverlay overlay = iterator.previous();
			if(overlay.visible && overlay.mouseWheel(amt))
			{
				used = true;
				break;
			}
		}
		if(!used)
			currScreen.mouseWheel(amt);
	}

	public void key(int key, boolean pressed)
	{
		boolean used = false;
		ListIterator<GuiOverlay> iterator = currScreen.overlays.listIterator(currScreen.overlays.size());
		while(iterator.hasPrevious())
		{
			GuiOverlay overlay = iterator.previous();
			if(overlay.visible && overlay.key(key, pressed))
			{
				used = true;
				break;
			}
		}
		if(!used)
			currScreen.key(key, pressed);
	}

	public void onBack()
	{
		boolean used = false;
		ListIterator<GuiOverlay> iterator = currScreen.overlays.listIterator(currScreen.overlays.size());
		while(iterator.hasPrevious())
		{
			GuiOverlay overlay = iterator.previous();
			if(overlay.visible && overlay.onBack())
			{
				used = true;
				break;
			}
		}
		if(!used)
			currScreen.onBack();
	}

	public Vector2 unproject(float screenX, float screenY)
	{
		return gdxParent.unproject(screenX, screenY);
	}

	public static Preferences getPrefs()
	{
		return Gdx.app.getPreferences("com.hk.powered" + (DEBUG ? ".debug" : ""));
	}

	public static boolean isAndroid()
	{
		return Gdx.app.getType() == ApplicationType.Android;
	}

	public static boolean isDesktop()
	{
		return Gdx.app.getType() == ApplicationType.Desktop;
	}

	public static boolean isApple()
	{
		return Gdx.app.getType() == ApplicationType.iOS;
	}

	@Override
	public int registerImage(String path)
	{
		return gdxParent.getG2D().registerImage(path);
	}

	@Override
	public G2D getG2D()
	{
		return gdxParent.getG2D();
	}

	public static final int YES_NO_OPTIONS = 1;
	public static final int YES_NO_CANCEL_OPTIONS = 2;
	public static final int OK_CANCEL_OPTIONS = 3;
	public static final int CONFIRM_CANCEL_OPTIONS = 4;
}
