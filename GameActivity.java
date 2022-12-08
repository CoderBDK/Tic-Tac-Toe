package com.tic.tac.toe;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		new Game(this);
	}
	
	
}
