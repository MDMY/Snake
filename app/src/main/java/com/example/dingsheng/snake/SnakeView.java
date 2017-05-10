package com.example.dingsheng.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Created by dingsheng on 2017/5/8.
 */

public class SnakeView extends View {
    private static final int UP = 0x001;
    private static final int DOWN = 0x002;
    private static final int LEFT = 0x003;
    private static final int RIGHT = 0x004;
    private int direction = RIGHT;
    private int directionBuf = direction;

    private Paint mPaint;
    private Paint linePaint;
    private Paint snakePaint;
    private Context context;
    private int pixl = 0;
    private int width = 0;
    private int height = 0;
    private List<Snake> snakeDatas = new ArrayList<SnakeView.Snake>();
    private boolean release = false;
    private Handler handler = new Handler() {
    };
    private Thread snakeThread = null;
    private Snake snakeFood = null;
    private Snake snakeEat = null;
    private int bengin = 10;

    private EatSome eatSome;
    private OnStatusListener onStatusListener;

    private static class Status {
        public static int START = 0x001;
        public static int PAUSE = 0x002;
        public static int STOP = 0x003;
    }

    private int status = Status.STOP;
    private int speed = 200;
    private boolean isCanEat = false;

    public SnakeView(Context context) {
        super(context);
        this.context = context;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        snakePaint = new Paint();
        snakePaint.setColor(Color.BLUE);
    }

    /**
     * 背景墙画笔
     */
    private static Paint paintWall = null;
    private boolean isInit = false;
    private boolean isStop = false;
    private int count = 0;

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        snakePaint = new Paint();
        snakePaint.setColor(Color.BLUE);
        if (paintWall == null) {
            paintWall = new Paint();
            paintWall.setColor(Color.LTGRAY);
            paintWall.setStyle(Paint.Style.STROKE);
            paintWall.setStrokeWidth(2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        pixl = width / SnakeConfig.snakeNum;
        if (!isInit) {
            initPoint();//初始化食物一个点
            isInit = true;
        }
        mPaint.setStrokeWidth(pixl * 2);
        snakePaint.setStrokeWidth(pixl - 2);
        initWall(canvas);    //初始化背景
        initSnake(canvas);   //初始化小蛇
        drawSnakeBuf(canvas);//初始化小蛇食物
    }

    /**
     * 初始化背景线
     */
    private void initSpaceLine(Canvas canvas) {
        for (int i = 1; i < SnakeConfig.snakeNum; i++) {
            canvas.drawLine(pixl * i, 0, pixl * i + 1, width, linePaint);
        }
        for (int i = 1; i < SnakeConfig.snakeNum; i++) {
            canvas.drawLine(0, pixl * i, width, pixl * i + 1, linePaint);
        }
    }

    /**
     * 初始化墙壁
     */
    private void initWall(Canvas canvas) {
        // canvas.drawLine(0, 0, 0, width, mPaint);
        // canvas.drawLine(width, 0, width, width, mPaint);
        // canvas.drawLine(0, 0, width, 0, mPaint);
        // canvas.drawLine(0, width, width, width, mPaint);
        RectF rel;
        paintWall.setStyle(Paint.Style.FILL_AND_STROKE);
        paintWall.setColor(Color.RED);
        for (int i = bengin; i < width - pixl; i += pixl) {
            for (int j = bengin; j < width - pixl; j += pixl) {
                if (i == bengin || j == bengin || i > width - 2 * pixl || j > width - 2 * pixl) {
                    rel = new RectF(i, j, i + pixl, j + pixl);
                    canvas.drawRoundRect(rel, 10, 10, paintWall);
                }
            }
        }
        paintWall.setStyle(Paint.Style.STROKE);
        paintWall.setColor(Color.LTGRAY);
        for (int i = bengin; i < width - pixl; i += pixl) {
            for (int j = bengin; j < width - pixl; j += pixl) {
                if (i == bengin || j == bengin || i > width - 2 * pixl || j > width - 2 * pixl) {

                } else {
                    rel = new RectF(i, j, i + pixl, j + pixl);
                    canvas.drawRoundRect(rel, 4, 4, paintWall);
                }
            }
        }
    }

    /**
     * 初始化第一个点
     */
    private void initSnake(Canvas canvas) {
        if (!release) {
            initSnakeData();
            release = true;
        }
        int len = snakeDatas.size();
        RectF rel;
        for (int i = 0; i < len; i++) {
            rel = new RectF(snakeDatas.get(i).x + 2, snakeDatas.get(i).y + 2, snakeDatas.get(i).x - 2 + pixl,
                    snakeDatas.get(i).y + pixl - 2);
            canvas.drawRoundRect(rel, 5, 5, snakePaint);
        }
    }

    /**
     * 在画布上显示出下一个食物
     */
    private void drawSnakeBuf(Canvas canvas) {
        if (snakeFood != null) {
            RectF rel;
            rel = new RectF(snakeFood.x + 2, snakeFood.y + 2, snakeFood.x - 2 + pixl, snakeFood.y + pixl - 2);
            canvas.drawRoundRect(rel, 5, 5, snakePaint);
        }
    }

    /**
     *
     */
    /**
     * 贪吃蛇吃到食物回调，用于刷新分数
     */
    public interface EatSome {
        public void onEat(int num);
    }

    /**
     * 游戏状态回调
     */
    public interface OnStatusListener {
        public void onStop(int num);
    }

    /**
     * 设置贪吃蛇吃到食物回调接口
     */
    public void setOnEatSome(EatSome eatSome) {
        this.eatSome = eatSome;
    }

    /**
     * 设置游戏状态回调接口
     */
    public void setOnStatusListener(OnStatusListener onStatusListener) {
        this.onStatusListener = onStatusListener;
    }

    /**
     * 控制贪吃蛇移动
     */
    private class SnakeThread implements Runnable {
        @Override
        public void run() {
            while (!isStop) {
                while (status == Status.START) {
                    if (directionBuf != direction && isCanMove(directionBuf)) {
                        direction = directionBuf;
                    }
                    switch (direction) {
                        case UP:
                            moveToUp();
                            break;
                        case DOWN:
                            moveToDown();
                            break;
                        case LEFT:
                            moveToLeft();
                            break;
                        case RIGHT:
                            moveToRight();
                            break;
                        default:
                            break;
                    }
                    eatFood();
                    getFood();
                    //判断是否吃掉食物
                    if (snakeEat != null && isSamePoint(snakeDatas.get(snakeDatas.size() - 1), snakeEat)) {
                        isCanEat = true;
                    }
                    if (isDone()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                clearSnake();
                            }
                        });
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                    try {
                        Thread.sleep(speed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 改变速度：如果当前速度已经为加速状态，则恢复默认，否则加速，默认速度：200
     */
    public void speed() {
        if (speed == 200) {
            speed = 100;
        } else {
            speed = 200;
        }
    }

    /**
     * 开始
     */
    public void start() {
        if (snakeThread == null || !snakeThread.isAlive()) {
            snakeThread = new Thread(new SnakeThread());
            snakeThread.start();
        }
        status = Status.START;
        if (snakeFood == null) {
            getRandPoint();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        status = Status.PAUSE;
    }

    /**
     * 判断是否可以向此方向移动
     */
    public boolean isCanMove(int direction) {
        Snake buf = new Snake();
        buf.x = snakeDatas.get(0).x;
        buf.y = snakeDatas.get(0).y;
        switch (direction) {
            case UP:
                buf.y = snakeDatas.get(0).y - pixl;
                break;
            case DOWN:
                buf.y = snakeDatas.get(0).y + pixl;
                break;
            case LEFT:
                buf.x = snakeDatas.get(0).x - pixl;
                break;
            case RIGHT:
                buf.x = snakeDatas.get(0).x + pixl;
                break;
            default:
                break;
        }
        if (isSamePoint(buf, snakeDatas.get(1))) {
            Log.e("move", "不可以移动");
            return false;
        }
        return true;
    }

    /**
     * 向左移动
     */
    private void moveToLeft() {
        int len = snakeDatas.size();
        for (int i = len - 1; i > 0; i--) {
            snakeDatas.get(i).x = snakeDatas.get(i - 1).x;
            snakeDatas.get(i).y = snakeDatas.get(i - 1).y;
        }
        snakeDatas.get(0).x = snakeDatas.get(0).x - pixl;
    }

    /**
     * 向右移动
     */
    private void moveToRight() {
        int len = snakeDatas.size();
        for (int i = len - 1; i > 0; i--) {
            snakeDatas.get(i).x = snakeDatas.get(i - 1).x;
            snakeDatas.get(i).y = snakeDatas.get(i - 1).y;
        }
        snakeDatas.get(0).x = snakeDatas.get(0).x + pixl;
    }

    /**
     * 向下移动
     */
    private void moveToDown() {
        int len = snakeDatas.size();
        for (int i = len - 1; i > 0; i--) {
            snakeDatas.get(i).x = snakeDatas.get(i - 1).x;
            snakeDatas.get(i).y = snakeDatas.get(i - 1).y;
        }
        snakeDatas.get(0).y = snakeDatas.get(0).y + pixl;
    }

    /**
     * 向上移动
     */
    private void moveToUp() {
        int len = snakeDatas.size();
        for (int i = len - 1; i > 0; i--) {
            snakeDatas.get(i).x = snakeDatas.get(i - 1).x;
            snakeDatas.get(i).y = snakeDatas.get(i - 1).y;
        }
        snakeDatas.get(0).y = snakeDatas.get(0).y - pixl;
    }

    /**
     * 判断本局是否结束
     */
    public boolean isDone() {
        int x = snakeDatas.get(0).x;
        int y = snakeDatas.get(0).y;
        //撞墙了
        if (x <= bengin || x > width - 2 * pixl || y < pixl || y > width - 2 * pixl) {
            return true;
        }
        int len = snakeDatas.size();
        for (int i = 2; i < len; i++) {
            if (isSamePoint(snakeDatas.get(0), snakeDatas.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断两个点是否在同一个位置
     */
    public boolean isSamePoint(Snake snake, Snake snakeBuf2) {
        if ((snake.x - snakeBuf2.x) < -2 || (snake.x - snakeBuf2.x) > 2) {
            return false;
        }
        if ((snake.y - snakeBuf2.y) < -2 || (snake.y - snakeBuf2.y) > 2) {
            return false;
        }
        return true;
    }

    /**
     * 随机产生一个食物
     */
    private void getRandPoint() {
        Random rand = new Random();
        int randNum = rand.nextInt(point.length);
        int len = snakeDatas.size();
        Snake buf = point[randNum];
        for (int i = 0; i < len; i++) {
            //判断食物是否在小蛇身上，如果在则重新产生一个食物
            if (buf != null && isSamePoint(snakeDatas.get(i), buf)) {
                randNum = rand.nextInt(point.length);
                buf = point[randNum];
            }
        }
        snakeFood = point[randNum];

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }

    /**
     * 每次开始的时候，初始化三个点
     */
    private void initSnakeData() {
        snakeDatas.add(new Snake(pixl * SnakeConfig.snakeNum / 2 + bengin, pixl * SnakeConfig.snakeNum / 2 + bengin));
        snakeDatas.add(new Snake(pixl * SnakeConfig.snakeNum / 2 + bengin, pixl * SnakeConfig.snakeNum / 2 + pixl + bengin));
        snakeDatas.add(new Snake(pixl * SnakeConfig.snakeNum / 2 + bengin, pixl * SnakeConfig.snakeNum / 2 + pixl * 2 + bengin));
    }

    public void up() {
        directionBuf = UP;
    }

    public void down() {
        directionBuf = DOWN;
    }

    public void left() {
        directionBuf = LEFT;
    }

    public void right() {
        directionBuf = RIGHT;
    }

    private Snake[] point = new Snake[(SnakeConfig.snakeNum - 2) * (SnakeConfig.snakeNum - 2)];

    /**
     * 初始化一个点
     */
    public void initPoint() {
        int count = 0;
        for (int i = 2; i < SnakeConfig.snakeNum - 2; i++) {
            for (int j = 2; j < SnakeConfig.snakeNum - 2; j++) {
                point[count] = new Snake(bengin + pixl * i, bengin + pixl * j);
                count++;
            }
        }
     //   count = 0;
    }

    /**
     * 吃掉食物
     */
    private void eatFood() {
        if (isCanEat) {
            snakeDatas.add(snakeEat);
            snakeEat = null;
            isCanEat = false;
            if (snakeFood == null) {
                getRandPoint();
            }
        }
    }

    /**
     * 获取到了食物，但是还没吃掉食物
     */
    private void getFood() {
        if (snakeFood != null && isSamePoint(snakeDatas.get(0), snakeFood)) {
            count++;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    eatSome.onEat(count);
                }
            });
            snakeEat = snakeFood;
            snakeFood = null;
            getRandPoint();
        }

    }

    /**
     * 本局结束，清空之前的数据
     */
    private void clearSnake() {
        snakeDatas.clear();
        initSnakeData();
        status = Status.STOP;
        snakeFood = null;
        onStatusListener.onStop(count);
        count=0;
    }

    public void onDestroy() {
        status = Status.STOP;
        isStop = true;
        if (snakeThread != null) {
            snakeThread.interrupt();
        }
    }

    /**
     * 小蛇实体
     */
    public class Snake {
        int x;
        int y;

        public Snake() {
        }

        public Snake(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
