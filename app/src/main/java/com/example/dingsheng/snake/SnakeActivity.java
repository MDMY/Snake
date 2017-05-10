package com.example.dingsheng.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dingsheng on 2017/5/9.
 */
public class SnakeActivity extends Activity{
    private Vibrator vibrator;
    private SnakeView snakeView;
    private TextView numText;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);
        snakeView= (SnakeView) findViewById(R.id.snakeView1);
        numText= (TextView) findViewById(R.id.num);
        vibrator= (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        mAlertDialog=new AlertDialog.Builder(SnakeActivity.this).setTitle("本局结束")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件

                    }
                }).create();
        snakeView.setOnEatSome(new SnakeView.EatSome() {
            @Override
            public void onEat(int num) {
                numText.setText("score: "+num);
                Log.e("当前得分","score: "+num);

            }
        });
        snakeView.setOnStatusListener(new SnakeView.OnStatusListener() {
            @Override
            public void onStop(int num) {
                numText.setText("score: "+0);
                vibrator.vibrate(500);
                mAlertDialog.setMessage("本局得分"+num);
                mAlertDialog.show();
            }
        });
    }

    public void speed(View view){
        snakeView.speed();
    }
    public void up(View view) {
        snakeView.up();
    }

    public void down(View view) {
        snakeView.down();
    }

    public void left(View view) {
        snakeView.left();
    }

    public void right(View view) {
        snakeView.right();
    }

    public void start(View view) {
        snakeView.start();
    }

    public void pause(View view) {
        snakeView.pause();
    }

    @Override
    protected void onDestroy(){

        if (mAlertDialog!=null){
            mAlertDialog.dismiss();
        }
        snakeView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
