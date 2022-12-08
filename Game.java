package com.tic.tac.toe;

import android.view.View;
import android.content.Context;
import android.widget.Toast;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.CountDownTimer;
import java.util.Random;
import android.app.Activity;
import android.widget.ImageView;
import java.util.Arrays;

public class Game
{
	static{
		System.loadLibrary("game_engine");
	}
	
	private  native int getPlayer(int rowIdx,int colIdx);

	private  native boolean canTurn(int rowIdx,int colIdx);

	private  native void turn(int rowIdx,int colIdx,int player);

	private native int getGameState();

	private native int resetGameEngine();
	
	//#End
	
	private TextView tvPlayer1;

	private TextView tvPlayer2;

	private boolean isComputerMode;

	private TouchFocusView touchFocusView;

	private TextView tvScorePlayer1;

	private TextView tvScorePlayer2;
	
	
	private GameView gameView;
	
	private Activity activity;

	public Game(GameActivity activity)
	{
		this.activity = activity;
		
		activity.setContentView(R.layout.game_activity);
		activity.getActionBar().hide();
		gameView=new GameView(activity);
		touchFocusView=new TouchFocusView(activity);
		((LinearLayout)activity.findViewById(R.id.game_activityLinearLayout)).addView(gameView);
		((LinearLayout)activity.findViewById(R.id.playeractivityLinearLayoutFocusView)).addView(touchFocusView);
		tvPlayer1 = (TextView)activity.findViewById(R.id.gameactivityTextViewPlayer1);
		tvPlayer2 = (TextView)activity.findViewById(R.id.gameactivityTextViewPlayer2);
		
	}
	public Game(PlayerActivity activity)
	{
		this.activity = activity;

		activity.setContentView(R.layout.player_activity);
		activity.getActionBar().hide();
		gameView=new GameView(activity);
		touchFocusView=new TouchFocusView(activity);
		((LinearLayout)activity.findViewById(R.id.game_activityLinearLayout)).addView(gameView);
		((LinearLayout)activity.findViewById(R.id.playeractivityLinearLayoutFocusView)).addView(touchFocusView);
		tvPlayer1 = (TextView)activity.findViewById(R.id.gameactivityTextViewPlayer1);
		tvPlayer2 = (TextView)activity.findViewById(R.id.gameactivityTextViewPlayer2);
	}
	public Game(ComputerActivity activity)
	{
		this.activity = activity;

		
		activity.setContentView(R.layout.computer_activity);
		activity.getActionBar().hide();
		gameView=new GameView(activity);
		touchFocusView=new TouchFocusView(activity);
		((LinearLayout)activity.findViewById(R.id.game_activityLinearLayout)).addView(gameView);
		((LinearLayout)activity.findViewById(R.id.computeractivityLinearLayoutFocusView)).addView(touchFocusView);
		tvScorePlayer1 = (TextView)activity.findViewById(R.id.computeractivityTextViewYou);
		tvScorePlayer2= (TextView)activity.findViewById(R.id.computeractivityTextViewBot);
		
		isComputerMode =true;
	}
	public void reset(){
		gameView.resetGame();
	}
	
	public class GameView extends View{
		
		private int height;
		private int width;

		private Bitmap bmpBoard;

		private int boardPosX;

		private int boardPosY;

		private int boardBoxWidth;

		private int boardBoxHeight;

		private int bw;

		private int bh;

		private Canvas boardCanvas;

		private Paint paintText;

		private int tx;

		private int ty;

		private boolean isTurn;

		private int ptx;

		private int pty;

		private int turnIdx;

		private int turnIdy;

		private boolean gameCanPlay=true;
		

		private Paint paintLine;

		private int score1;

		private int score2;

		private Dialog gameOverDialog;

		private ImageView gameImage;

		private TextView tvInfo;
		
		
		
		private GameView(Activity c){
			super(c);
			gameOver(c);
		}

		private void gameOver(Activity a){
			gameOverDialog=new Dialog(a);
			gameOverDialog.setContentView(R.layout.game_over);
			gameOverDialog.setTitle("Game-Info");
			tvInfo=(TextView)gameOverDialog.findViewById(R.id.gameoverTextViewInfo);
			gameImage=(ImageView)gameOverDialog.findViewById(R.id.game_overImageView);
			gameOverDialog.findViewById(R.id.gameoverButton).setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						resetGame();
						gameOverDialog.hide();
					}

				
			});
		}
		
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh)
		{
			// TODO: Implement this method
			super.onSizeChanged(w, h, oldw, oldh);
			//show(""+w+":"+h);
			width=w;
			height=h;
			drawBoard();
			paintText=new Paint();
			paintText.setColor(Color.parseColor("#010314"));
			paintText.setTextSize(100);
			
			paintLine=new Paint();
			paintLine.setColor(Color.parseColor("#A60E0E"));
			paintLine.setStrokeWidth(6);
		}

		private void show(String p0)
		{
			Toast.makeText(activity,p0,Toast.LENGTH_SHORT).show();
		}
		private boolean isNowClick;

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			
			 tx =(int) event.getX();
			 ty=(int) event.getY();
			 
			
			 if(!gameCanPlay){
				 resetGame();
			 }
			
			
			boolean canClickX=tx > boardPosX & tx <bw+boardPosX;
			boolean canClickY=ty > boardPosY & ty <bh+boardPosY;
			
			if(!(canClickX&canClickY)){
				return false;
			}
			if(isComputerMode){
				touchFocusView.focusModeDraw(tx,ty+boardBoxHeight/3);
			}else{
				touchFocusView.focusModeDraw(tx,ty+boardBoxHeight/2);
			}
			tx = tx - boardPosX;
			ty = ty - boardPosY;
			int idx = tx/boardBoxWidth;
			int idy = ty/boardBoxHeight;
			
			int pcx =0;
			int pcy =0;
			
			pcx = boardBoxWidth/3;
			pcy = boardBoxHeight-(boardBoxHeight/3);
			
			ptx = idx*boardBoxWidth+pcx;
			pty = idy*boardBoxHeight+pcy;
			
			turnIdx=idx;
			turnIdy=idy;
			//show(""+idx+","+idy);
			isNowClick=true;
			if(canTurn(turnIdy,turnIdx)){
				nextTurn();
				invalidate();
				
			}
			
			
			return false;
		}
		private void nextTurn()
		{
			String turnStr = "X";
			boolean gameIsDraw=false;
			boolean gameIsWon=false;
			if(!isTurn & isNowClick & gameCanPlay){

				turnStr="O";
			}
			//canvas.drawText("Next Turn :"+turnStr,100,200,paintText);
			
			if(!gameCanPlay){
				return;
			}
			if(isNowClick){
				/*if(!canTurn(turnIdy,turnIdx)){
				 show("Turn off: false");
				 return;
				 }*/

				if(!isTurn){
					boardCanvas.drawText("X",ptx,pty,paintText);
					isTurn=true;
					turn(turnIdy,turnIdx,0);
					if(isComputerMode)prepareAIMove();
				}else{
					if(isComputerMode)prepareAIMove();
					///Human move
					else{
						boardCanvas.drawText("O",ptx,pty,paintText);
						isTurn=false;
						turn(turnIdy,turnIdx,1);
					}

				}

				/*if(turnStr.equals("X")){

				 //show("0->"+getPlayer(turnIdy,turnIdx));
				 }else{

				 //show("1->"+getPlayer(turnIdy,turnIdx));
				 }*/
				 
				 

				switch(getGameState()){
					case 0:
						//show("'X' own the game");
						if(!isComputerMode)
							tvInfo.setText("'X' own the game");
						else{
							tvInfo.setText("You Win!");
						}
						gameCanPlay=false;
						wonGame(0);
						score1++;
						if(isComputerMode){
							gameImage.setImageBitmap(bmpBoard);
							gameOverDialog.show();
						}else{
							tvPlayer2.setText("You Win!" +" NextTurn:"+turnStr);
							tvPlayer1.setText("You Lose!"+" NextTurn:"+turnStr);
							
							
						}
						
						break;
					case 1:
						//show("'O' own the game");
						if(!isComputerMode)
						tvInfo.setText("'O' own the game");
						else{
							tvInfo.setText("You Lose!");
						}
						gameCanPlay=false;
						wonGame(1);
						score2++;
						if(isComputerMode){
							gameImage.setImageBitmap(bmpBoard);
							gameOverDialog.show();
						}else{
							tvPlayer1.setText("You Win!" +" NextTurn:"+turnStr);
							tvPlayer2.setText("You Lose!"+" NextTurn:"+turnStr);
							
						}
						
						break;
					case 2:
						//show("game is draw!");
						tvInfo.setText("Game is draw!");
						gameCanPlay = false;
						gameIsDraw=true;
						if(isComputerMode){
							gameImage.setImageBitmap(bmpBoard);
							gameOverDialog.show();
						}else{
							tvPlayer1.setText("Tie! Touch To Start"+" NTurn:"+turnStr);
							tvPlayer2.setText("Tie! Touch To Start"+" NTurn:"+turnStr);
							
						}
						
						break;
						default:
						if(!isComputerMode){
							tvPlayer1.setText("You(O):"+score1+" NTurn:"+turnStr +" Opponent(X):"+score1);
							tvPlayer2.setText("You(X):"+score2+" NTurn:"+turnStr+" Opponent(O):"+score2);
							
						}
						break;
				}
				isNowClick=false;
			}


			if(isComputerMode){
				tvScorePlayer1.setText("You(X) : "+score1);
				tvScorePlayer2.setText("Computer(O) : "+score2);

			}
		}
		private void resetGame()
		{
		
			resetGameEngine();
			drawBoard();
			invalidate();
			//show("Game Reset");
			gameCanPlay=true;
			if(isComputerMode)isTurn=false;
		}
		

		 
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			canvas.drawBitmap(bmpBoard,boardPosX,boardPosY,null);
			/*
			
			*/
			
			//loadDraw(canvas);
		}

		private void loadDraw(Canvas canvas)
		{
			String turnStr = "X";

			if(!isTurn & isNowClick & gameCanPlay){

				turnStr="O";
			}
			//canvas.drawText("Next Turn :"+turnStr,100,200,paintText);
			tvPlayer1.setText("Next Turn :"+turnStr);
			if(tvPlayer2!=null)tvPlayer2.setText("Next Turn :"+turnStr);
			canvas.drawBitmap(bmpBoard,boardPosX,boardPosY,null);
			if(!gameCanPlay){
				return;
			}
			if(isNowClick){
				/*if(!canTurn(turnIdy,turnIdx)){
				 show("Turn off: false");
				 return;
				 }*/

				if(!isTurn){
					boardCanvas.drawText("X",ptx,pty,paintText);
					isTurn=true;
					turn(turnIdy,turnIdx,0);
					if(isComputerMode)prepareAIMove();
				}else{
					if(isComputerMode)prepareAIMove();
					///Human move
					else{
						boardCanvas.drawText("O",ptx,pty,paintText);
						isTurn=false;
						turn(turnIdy,turnIdx,1);
					}

				}

				/*if(turnStr.equals("X")){

				 //show("0->"+getPlayer(turnIdy,turnIdx));
				 }else{

				 //show("1->"+getPlayer(turnIdy,turnIdx));
				 }*/

				switch(getGameState()){
					case 0:
						show("'X' own the game");
						gameCanPlay=false;
						wonGame(0);
						if(isComputerMode)score1++;
						break;
					case 1:
						show("'O' own the game");
						gameCanPlay=false;
						wonGame(1);
						if(isComputerMode)score2++;
						break;
					case 2:
						show("game is draw!");
						gameCanPlay = false;
						break;
				}
				isNowClick=false;
			}


			if(isComputerMode){
				tvScorePlayer1.setText("You : "+score1);
				tvScorePlayer2.setText("Bot : "+score2);

			}
		}

		private void prepareAIMove()
		{
			
			if(getGameState()!=-1)return;
			int[] nextMoveIdx =nextAIMove();

			int pcx = boardBoxWidth / 3;
			int pcy = boardBoxHeight - (boardBoxHeight / 3);

			int idx=nextMoveIdx[1];
			int idy=nextMoveIdx[0];
		//	show(""+idx+","+idy);
			ptx = idx * boardBoxWidth + pcx;
			pty = idy*boardBoxHeight+pcy;

			boardCanvas.drawText("O",ptx,pty,paintText);
			isTurn=false;
			turnIdx=idx;
			turnIdy=idy;
			turn(turnIdy,turnIdx,1);
			
		}

		private class Board{
			int player;

			public Board(int player)
			{
				this.player = player;
			}
		}
		
		/*
		private int[] __nextAIMinimaxMove()
		{
			Board[][] board=new Board[3][3];
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					board[i][j]=new Board(getPlayer(i,j));
				}
			}
			
			int[] bestMove = null;
			return bestMove;
		}
		private int _minimax(Board board[][],int depth, boolean max)
		{
			int score = getGameState();
			
			return 0;
			
		}*/
		
		

		
		private int[] nextAIMove()
		{
			int[] nextMove =new int[2];
			
			/*nextMove[0]=2;
			nextMove[1]=2;*/
			
			
			
			/// horizontal move

			int turnState = 0;
			//row1
			if (getPlayer(0, 0) == turnState && getPlayer(0, 1)==turnState)
			{
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
				
				
			}else if (getPlayer(0, 1) == turnState && getPlayer(0, 2)==turnState)
			{
				
				if(getPlayer(0,0)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			//row2
			else if (getPlayer(1, 0) == turnState && getPlayer(1, 1)==turnState)
			{
				if(getPlayer(1,2)==5){
					nextMove[0]=1;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}else if (getPlayer(1, 1) == turnState && getPlayer(1, 2)==turnState)
			{
				if(getPlayer(1,0)==5){
					nextMove[0]=1;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			//row3
			else if (getPlayer(2, 0) == turnState && getPlayer(2, 1)==turnState)
			{
				if(getPlayer(1,2)==5){
					nextMove[0]=1;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}else if (getPlayer(2, 1) == turnState && getPlayer(2, 2)==turnState)
			{
				if(getPlayer(2,0)==5){
					nextMove[0]=2;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			
			//End Horizontal / Start Vertical
			//col1
			else if(getPlayer(0,0)==turnState&&getPlayer(1,0)==turnState){
				if(getPlayer(2,0)==5){
					nextMove[0]=2;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			else if(getPlayer(1,0)==turnState&&getPlayer(2,0)==turnState){
				if(getPlayer(0,0)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			//col2
			else if(getPlayer(0,1)==turnState&&getPlayer(1,1)==turnState){
				if(getPlayer(2,1)==5){
					nextMove[0]=2;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			else if(getPlayer(1,1)==turnState&&getPlayer(2,1)==turnState){
				if(getPlayer(0,1)==5){
					nextMove[0]=0;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			//col3
			else if(getPlayer(1,2)==turnState&&getPlayer(2,2)==turnState){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}
			//End Vertical / Start Corne
			//left
			else if(getPlayer(0,0)==turnState&&getPlayer(1,1)==turnState){
				if(getPlayer(2,2)==5){
					nextMove[0]=2;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}
			else if(getPlayer(2,2)==turnState&&getPlayer(1,1)==turnState){
				if(getPlayer(0,0)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			//right
			else if(getPlayer(0,2)==turnState&&getPlayer(1,1)==turnState){
				if(getPlayer(2,0)==5){
					nextMove[0]=2;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			else if(getPlayer(2,0)==turnState&&getPlayer(1,1)==turnState){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}
			//a[center] b of logic horizontal
			//#row 1
			else if (getPlayer(0, 0) == turnState && getPlayer(0, 2)==turnState)
			{

				if(getPlayer(0,1)==5){
					nextMove[0]=0;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			//#row 2
			else if (getPlayer(1, 0) == turnState && getPlayer(1, 2)==turnState)
			{

				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			//#row 3
			else if (getPlayer(2, 0) == turnState && getPlayer(2, 2)==turnState)
			{

				if(getPlayer(2,1)==5){
					nextMove[0]=2;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			////a[center] b of logic vertical
			//#col 1
			else if (getPlayer(0, 0) == turnState && getPlayer(2, 0)==turnState)
			{
				if(getPlayer(1,0)==5){
					nextMove[0]=1;
					nextMove[1]=0;
				}else{
					bestMove(nextMove);
				}
			}
			//#col 2
			else if (getPlayer(0, 1) == turnState && getPlayer(2, 1)==turnState)
			{
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}
			//#col 2
			else if (getPlayer(0, 2) == turnState && getPlayer(2, 2)==turnState)
			{
				if(getPlayer(1,2)==5){
					nextMove[0]=1;
					nextMove[1]=2;
				}else{
					bestMove(nextMove);
				}
			}
			////a[center] b of logic corner
			//# 1
			else if (getPlayer(0, 0) == turnState && getPlayer(2, 2)==turnState)
			{
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}//# 2
			else if (getPlayer(0, 2) == turnState && getPlayer(2, 0)==turnState)
			{
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}else{
					bestMove(nextMove);
				}
			}

			
			
			
			else{
				bestMove(nextMove);
			}
			
			return nextMove;
		}

		private Random r=new Random();
		private void bestMove(int[]nextMove)
		{
			
			String a="";
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					int player = getPlayer(i,j);
					if(player==5){
						a+=i+","+j+"\n";
					}
				}

			}
			
			String[] ds=a.split("\\n+");
			String d=ds[r.nextInt(ds.length)];
			ds=d.replace(",","\n").split("\\n+");
			
			nextMove[0]=Integer.parseInt(ds[0]);
			nextMove[1]=Integer.parseInt(ds[1]);
			
			int state = 1;
			
			//x=[0,0] 
			if(getPlayer(0,0)==0){
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}
			}else if(getPlayer(0,2)==0){
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}
			}else if(getPlayer(2,0)==0){
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}
			}else if(getPlayer(2,2)==0){
				if(getPlayer(1,1)==5){
					nextMove[0]=1;
					nextMove[1]=1;
				}
			}
			//horizontal
			//1
			else if(getPlayer(0,0)==state&getPlayer(0,1)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=2;
				}
			}
			//2
			else if(getPlayer(0,1)==state&getPlayer(0,2)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}
			}
			//3
			else if(getPlayer(1,0)==state&getPlayer(1,1)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=1;
					nextMove[1]=2;
				}
			}
			//4
			else if(getPlayer(1,1)==state&getPlayer(1,2)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=1;
					nextMove[1]=0;
				}
			}
			//5
			else if(getPlayer(2,0)==state&getPlayer(2,1)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=2;
					nextMove[1]=2;
				}
			}
			//6
			else if(getPlayer(2,1)==state&getPlayer(2,2)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}
			}
			//vertical
			//7
			else if(getPlayer(0,0)==state&getPlayer(1,0)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=2;
					nextMove[1]=0;
				}
			}
			
			//8
			else if(getPlayer(1,0)==state&getPlayer(2,0)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=0;
				}
			}
			//9
			else if(getPlayer(0,1)==state&getPlayer(1,1)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=2;
					nextMove[1]=1;
				}
			}
			//10
			else if(getPlayer(1,1)==state&getPlayer(2,1)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=1;
				}
			}
			//11
			else if(getPlayer(0,2)==state&getPlayer(1,2)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=2;
					nextMove[1]=2;
				}
			}
			//12
			else if(getPlayer(1,2)==state&getPlayer(2,2)==state){
				if(getPlayer(0,2)==5){
					nextMove[0]=0;
					nextMove[1]=2;
				}
			}
		}

		

		
		
		private void wonGame(int turnState)
		{
			
					if(getPlayer(0,0)==turnState&&getPlayer(0,1)==turnState&&getPlayer(0,2)==turnState){
						boardCanvas.drawLine(0,boardBoxHeight/2,bw,boardBoxHeight/2,paintLine);
					}else if(getPlayer(1,0)==turnState&&getPlayer(1,1)==turnState&&getPlayer(1,2)==turnState){
						boardCanvas.drawLine(0,boardBoxHeight/2*3,bw,boardBoxHeight/2*3,paintLine);
					}else if(getPlayer(2,0)==turnState&&getPlayer(2,1)==turnState&&getPlayer(2,2)==turnState){
						boardCanvas.drawLine(0,boardBoxHeight/2*5,bw,boardBoxHeight/2*5,paintLine);
					}
					//End Horizontal / Start Vertical
					else if(getPlayer(0,0)==turnState&&getPlayer(1,0)==turnState&&getPlayer(2,0)==turnState){
						boardCanvas.drawLine(boardBoxWidth/2,0,boardBoxWidth/2,bh,paintLine);
					}else if(getPlayer(0,1)==turnState&&getPlayer(1,1)==turnState&&getPlayer(2,1)==turnState){
						boardCanvas.drawLine(boardBoxWidth/2*3,0,boardBoxWidth/2*3,bh,paintLine);
					}else if(getPlayer(0,2)==turnState&&getPlayer(1,2)==turnState&&getPlayer(2,2)==turnState){
						boardCanvas.drawLine(boardBoxWidth/2*5,0,boardBoxWidth/2*5,bh,paintLine);
					}
					//End Vertical / Start Corner
					else if(getPlayer(0,0)==turnState&&getPlayer(1,1)==turnState&&getPlayer(2,2)==turnState){
						boardCanvas.drawLine(0,0,bw,bh,paintLine);
					}else if(getPlayer(0,2)==turnState&&getPlayer(1,1)==turnState&&getPlayer(2,0)==turnState){
						boardCanvas.drawLine(bw,0,0,bh,paintLine);
					}
			
			
					
		}
		/*
		 //Full Grid With Border
		 for(int y=0;y<row+1;y++){
		 for(int x=0;x<col+1;x++){

		 canvas.drawLine(x*boardBoxWidth,
		 y*boardBoxHeight
		 ,x*boardBoxWidth+boardBoxWidth,
		 y*boardBoxHeight,paint);

		 canvas.drawLine(x*boardBoxWidth,
		 y*boardBoxHeight
		 ,x*boardBoxWidth,
		 y*boardBoxHeight+boardBoxHeight,paint);
		 }
		 }
		 */
		private void drawBoard()
		{
			
			int row = 3;
			int col = 3;
			
			
			 boardBoxWidth = 200;
			 boardBoxHeight = 200;
			 bw=col*boardBoxWidth;
			 bh=row*boardBoxHeight;
			bmpBoard=Bitmap.createBitmap(bw,bh,Bitmap.Config.ARGB_8888);
			Canvas canvas=new Canvas(bmpBoard);
			boardCanvas = canvas;
			Paint paint=new Paint();
			paint.setColor(Color.parseColor("#010314"));
			paint.setStrokeWidth(6);
			int cx=bw;
			if(bw>width){
				
			}else{
				int scx = width - bw;
				cx =scx/2;
			}
			boardPosX =cx;
			
			int cy=bh;
			if(bh>height){
				
			}else{
				int scy = height - bh;
				cy =scy/2;
			}
			boardPosY =cy;
			
			for(int y=0;y<row;y++){
				for(int x=0;x<col;x++){
					if(y!=0){
						canvas.drawLine(x*boardBoxWidth,
										y*boardBoxHeight
										,x*boardBoxWidth+boardBoxWidth,
										y*boardBoxHeight,paint);
					}
					if(x!=0)
					canvas.drawLine(x*boardBoxWidth,
									y*boardBoxHeight
									,x*boardBoxWidth,
									y*boardBoxHeight+boardBoxHeight,paint);
				}
			}
		}
		
		
		
		
	}
	
}
