package com.tic.tac.toe;
import android.app.Activity;
import android.os.Bundle;

public class PlayerActivity extends Activity
{

	private Game game;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		game=new Game(this);
	}
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		game.reset();
		finish();
	}
}
