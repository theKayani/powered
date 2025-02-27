package com.hk.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.hk.engine.gui.Main;

public class GDXMain extends ApplicationAdapter
{
	private OrthographicCamera camera;
	private G2D g2d;
	private ShapeRenderer renderer;
	private Main main;
	private Handler input;

	public GDXMain() {}

	@Override
	public void create()
	{
		camera = new OrthographicCamera();
		camera.setToOrtho(true, Main.WIDTH, Main.HEIGHT);
		renderer = new ShapeRenderer();
		g2d = new G2D(renderer, camera);
		main = new Main(this);
		input = new Handler(main, camera);
		Gdx.input.setInputProcessor(input);
	}

	@Override
	public void render()
	{
		camera.update();
		main.update();
		input.update();
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.identity();
		renderer.setProjectionMatrix(camera.combined);
		g2d.reset();
		main.render(g2d);
	}
	
	@Override
	public void dispose()
	{
		main.getCurrentScreen().onGuiClose();
		g2d.dispose();
	}
	
	@Override
	public void pause ()
	{
	}

	@Override
	public void resume ()
	{
	}

	public OrthographicCamera getCamera()
	{
		return camera;
	}

	public G2D getG2D()
	{
		return g2d;
	}

	public Main getMain()
	{
		return main;
	}

	public Vector2 unproject(float screenX, float screenY)
	{
		return input.unproject(screenX, screenY);
	}
}