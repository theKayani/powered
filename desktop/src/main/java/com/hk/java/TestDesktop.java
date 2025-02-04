package com.hk.java;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.hk.engine.GDXMain;

public class TestDesktop
{
	public static void main (String[] args)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "titties";
		config.width = 1280;
		config.height = 720;
		config.backgroundFPS = 0;
		config.foregroundFPS = 0;
		config.vSyncEnabled = false;
		new LwjglApplication(new GDXMain(), config);
	}
}
