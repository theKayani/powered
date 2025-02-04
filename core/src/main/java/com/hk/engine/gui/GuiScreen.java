package com.hk.engine.gui;

import com.hk.engine.G2D;
import com.hk.engine.util.ImageRegister;

import java.util.LinkedList;
import java.util.List;

public abstract class GuiScreen
{
	public final Main main;
	List<GuiOverlay> overlays = new LinkedList<GuiOverlay>();
	
	public GuiScreen(Main main)
	{
		this.main = main;
	}
	
	public void initialize(ImageRegister register)
	{}
	
	public abstract void update(double delta);
	
	public abstract void render(G2D g2d);

	public void mouse(float x, float y, int button, boolean pressed)
	{}
	
	public void mouseMoved(float x, float y)
	{}

	public void mouseWheel(int amt)
	{}

	public void key(int key, boolean pressed)
	{}

	public void onBack()
	{}

	public void onGuiClose()
	{}

	public void addOverlay(GuiOverlay overlay)
	{
		if(!overlays.contains(overlay))
		{
			overlay.initialize(main);
			overlays.add(overlay);
		}
	}
}
