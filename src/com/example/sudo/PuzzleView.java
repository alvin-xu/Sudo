package com.example.sudo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class PuzzleView extends View{
	private static final String TAG="Sudoku";
	private final Game game;

	private float width;
	private float height;
	private int selX;
	private int selY;
	private final Rect selRect=new Rect();
	
	public PuzzleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.game=(Game)context;
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	//该方法在视图被创建，并且Android确定了视图大小以后被调用
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		width=w/9f;
		height=h/9f;
		getRect(selX,selY,selRect);
		Log.d(TAG,"onSizeChanged:width"+width+","+height);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	//根据x,y计算矩形覆盖区域
	private void getRect(int x,int y,Rect rect){
		rect.set((int)(x*width), (int)(y*height) , (int)(x*width+width), (int)(y*height+height));
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//定义棋盘用到的画笔，颜色
		Paint background=new Paint();
		background.setColor(getResources().getColor(R.color.puzzle_background));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		
		Paint dark=new Paint();
		dark.setColor(getResources().getColor(R.color.puzzle_dark));
		Paint hilite=new Paint();
		hilite.setColor(getResources().getColor(R.color.puzzle_hilite));
		Paint light=new Paint();
		light.setColor(getResources().getColor(R.color.puzzle_light));
		//绘制棋盘
		for(int i=0;i<9;i++){
			canvas.drawLine(0,i*height,getWidth(),i*height,light);
			canvas.drawLine(0,i*height+1,getWidth(),i*height+1,hilite);
			canvas.drawLine(i*width, 0, i*width, getHeight(), light);
			canvas.drawLine(i*width+1, 0, i*width+1, getHeight(), hilite);
		}
		for(int i=0;i<9;i++){
			if(i%3!=0) continue;
			canvas.drawLine(0,i*height,getWidth(),i*height,dark);
			canvas.drawLine(0,i*height+1,getWidth(),i*height+1,hilite);
			canvas.drawLine(i*width, 0, i*width, getHeight(), dark);
			canvas.drawLine(i*width+1, 0, i*width+1, getHeight(), hilite);
		}
		
		//绘制数字
		Paint foreground=new Paint(Paint.ANTI_ALIAS_FLAG);
		foreground.setColor(getResources().getColor(R.color.puzzle_foreground));
		foreground.setStyle(Style.FILL);
		foreground.setTextScaleX(width/height);
		foreground.setTextAlign(Paint.Align.CENTER);
		FontMetrics fm=foreground.getFontMetrics();
		float x=width/2;
		float y=height/2-(fm.ascent+fm.descent);
		for(int i=0;i<9;i++){
			for(int j=0;j<9;j++){
				canvas.drawText(this.game.getTileString(i,j),i*width+x,j*height+y, foreground);
			}
		}
		
		//绘制选定区域
		Log.d(TAG,"selRect="+selRect);
		Paint selected=new Paint();
		selected.setColor(getResources().getColor(R.color.puzzle_selected));
		canvas.drawRect(selRect, selected);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			select(selX,selY-1);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			select(selX,selY+1);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			select(selX-1,selY);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			select(selX+1,selY);
			break;
		case KeyEvent.KEYCODE_0:	
		case KeyEvent.KEYCODE_SPACE:setSelectedTile(0);break;
		case KeyEvent.KEYCODE_1:	setSelectedTile(1);break;
		case KeyEvent.KEYCODE_2:	setSelectedTile(2);break;
		case KeyEvent.KEYCODE_3:	setSelectedTile(3);break;
		case KeyEvent.KEYCODE_4:	setSelectedTile(4);break;
		case KeyEvent.KEYCODE_5:	setSelectedTile(5);break;
		case KeyEvent.KEYCODE_6:	setSelectedTile(6);break;
		case KeyEvent.KEYCODE_7:	setSelectedTile(7);break;
		case KeyEvent.KEYCODE_8:	setSelectedTile(8);break;
		case KeyEvent.KEYCODE_9:	setSelectedTile(9);break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			game.showKeypadOrError(selX,selY);
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()!=MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		select((int)(event.getX()/width), (int)(event.getY()/height));
		game.showKeypadOrError(selX,selY);
		return true;
	}

	private void select(int x, int y){
		invalidate(selRect);	//原选择矩形覆盖的区域需要重绘
		selX=Math.min(Math.max(x, 0), 8);
		selY=Math.min(Math.max(y, 0), 8);
		getRect(selX,selY,selRect);	//计算新矩形覆盖区域
		invalidate(selRect);	//新选择矩形覆盖的区域需要重绘
	}
	//输入数字为tile后，相关操作
	public void setSelectedTile(int tile){
		if(game.setTileIfValid(selX,selY,tile)){
			invalidate();
		}else{
			//输入无效数字
			startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
		}
	}
}
