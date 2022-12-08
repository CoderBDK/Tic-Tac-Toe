package com.tic.tac.toe;
import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.content.Intent;

public class LaunchActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch);
		findViewById(R.id.launchButtonPlayer).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					startActivity(new Intent(getApplicationContext(),PlayerActivity.class));
					
				}

			
		});
		findViewById(R.id.launchButtonComputer).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					startActivity(new Intent(getApplicationContext(),ComputerActivity.class));

				}


			});
	}
	
}
