package com.hk.engine.gui;

import com.hk.engine.G2D;
import com.hk.engine.util.ImageRegister;

public abstract class GuiOverlay
{
	boolean remove = false, visible = true;

	public void initialize(ImageRegister register)
	{}

	public abstract void update(double delta);

	public abstract void render(G2D g2d);

	public abstract boolean mouse(float x, float y, int button, boolean pressed);
	
	public abstract boolean mouseMoved(float x, float y);

	public abstract boolean mouseWheel(int amt);

	public abstract boolean key(int key, boolean pressed);

	public boolean onBack()
	{
		return false;
	}

	public GuiOverlay setVisible(boolean visible)
	{
		this.visible = visible;
		return this;
	}

	public void removeSelf()
	{
		remove = true;
	}
}
