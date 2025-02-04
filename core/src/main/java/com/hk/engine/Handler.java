package com.hk.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.hk.engine.gui.Main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public final class Handler implements InputProcessor
{
	private final Camera camera;
	private final Main main;
	private final Vector3 touch = new Vector3();
	private final Set<Integer> mousePressed, keyPressed;
	
	public Handler(Main main, Camera camera)
	{
		this.main = main;
		this.camera = camera;
		mousePressed = new HashSet<>();
		keyPressed = new HashSet<>();
	}

	public void update()
	{
		for (Integer button : mousePressed)
		{
			getScaledTouch(Gdx.input.getX(), Gdx.input.getY());
			main.mouse((int) touch.x, (int) touch.y, button, true);
		}
		for (Integer key : keyPressed)
			main.key(key, true);
	}

	@Override
	public boolean keyDown(int key)
	{
		keyPressed.add(key);
		return false;
	}

	@Override
	public boolean keyUp(int key)
	{
		if(key == Input.Keys.BACK || key == Input.Keys.BACKSPACE)
		{
			main.onBack();
			return true;
		}
		else
		{
			keyPressed.remove(key);
			main.key(key, false);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		mousePressed.add(button);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		mousePressed.remove(button);
		getScaledTouch(screenX, screenY);
		main.mouse((int) touch.x, (int) touch.y, button, false);

		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button)
	{
		return touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
//		touchDown(screenX, screenY, 0, pointer);
//		mouseMoved(screenX, screenY);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		getScaledTouch(screenX, screenY);
		main.mouseMoved((int) touch.x, (int) touch.y);
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY)
	{
		main.mouseWheel((int) amountY);
		return false;
	}

	private void getScaledTouch(float screenX, float screenY)
	{
		camera.unproject(touch.set(screenX, screenY, 0));
	}

	Vector2 unproject(float screenX, float screenY)
	{
		getScaledTouch(screenX, screenY);
		return new Vector2(touch.x, touch.y);
	}
}
