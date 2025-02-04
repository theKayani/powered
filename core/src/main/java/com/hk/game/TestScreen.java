package com.hk.game;

import com.badlogic.gdx.graphics.Color;
import com.hk.engine.G2D;
import com.hk.engine.gui.GuiScreen;
import com.hk.engine.gui.Main;

public class TestScreen extends GuiScreen
{
	public TestScreen(Main main)
	{
		super(main);
	}

	@Override
	public void update(double delta)
	{
	}

	@Override
	public void render(G2D g2d)
	{
		g2d.setColor(Color.BLUE);
		g2d.begin(true);
		g2d.drawRect(10, 10, 20, 20);
		g2d.end();

		g2d.setColor(Color.BLACK);
		g2d.enableCentered();
		g2d.beginString();
		g2d.drawString(g2d.width / 2, g2d.height / 2, "Hello!\nThis is my string!");
		g2d.endString();
		g2d.disableCentered();
	}

	public void mouse(float x, float y, boolean pressed)
	{
	}

	@Override
	public void mouseMoved(float x, float y)
	{
	}
}
