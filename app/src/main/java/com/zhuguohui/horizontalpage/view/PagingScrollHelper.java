package com.zhuguohui.horizontalpage.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 实现RecycleView分页滚动的工具类
 * Created by zhuguohui on 2016/11/10.
 */

public class PagingScrollHelper {
    private Context context;

    public PagingScrollHelper(Context context) {
        this.context = context;
    }

    private RecyclerView mRecyclerView = null;

    private MyOnScrollListener mOnScrollListener = new MyOnScrollListener();
    private MyOnFlingListener mOnFlingListener = new MyOnFlingListener();

    private int offsetY = 0;
    private int offsetX = 0;


    int startY = 0;
    int startX = 0;

    enum ORIENTATION {
        HORIZONTAL, VERTICAL, NULL
    }

    private ORIENTATION mOrientation = ORIENTATION.HORIZONTAL;

    public void setUpRecycleView(RecyclerView recycleView) {
        if (recycleView == null) {
            throw new IllegalArgumentException("recycleView must be not null");
        }
        mRecyclerView = recycleView;
        //处理滑动
        recycleView.setOnFlingListener(mOnFlingListener);
        //设置滚动监听，记录滚动的状态，和总的偏移量
        recycleView.addOnScrollListener(mOnScrollListener);
        //记录滚动开始的位置
        recycleView.setOnTouchListener(mOnTouchListener);
        //获取滚动的方向
        updateLayoutManger();

    }

    public void updateLayoutManger() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            if (layoutManager.canScrollVertically()) {
                mOrientation = ORIENTATION.VERTICAL;
            } else if (layoutManager.canScrollHorizontally()) {
                mOrientation = ORIENTATION.HORIZONTAL;
            } else {
                mOrientation = ORIENTATION.NULL;
            }
            if (mAnimator != null) {
                mAnimator.cancel();
            }
            startX = 0;
            startY = 0;
            offsetX = 0;
            offsetY = 0;

        }

    }

    /**
     * 获取总共的页数
     */
    public int getPageCount() {
        if (mRecyclerView != null) {
            if (mOrientation == ORIENTATION.NULL) {
                return 0;
            }
            if (mOrientation == ORIENTATION.VERTICAL && mRecyclerView.computeVerticalScrollExtent() != 0) {
                return mRecyclerView.computeVerticalScrollRange() / mRecyclerView.computeVerticalScrollExtent();
            } else if (mRecyclerView.computeHorizontalScrollExtent() != 0) {
                Log.i("zzz", "rang=" + mRecyclerView.computeHorizontalScrollRange() + " extent=" + mRecyclerView.computeHorizontalScrollExtent());
                return mRecyclerView.computeHorizontalScrollRange() / mRecyclerView.computeHorizontalScrollExtent();
            }
        }
        return 0;
    }


    ValueAnimator mAnimator = null;

    public void scrollToPosition(int position) {
        if (mAnimator == null) {
            mOnFlingListener.onFling(0, 0);
        }
        if (mAnimator != null) {
            int startPoint = mOrientation == ORIENTATION.VERTICAL ? offsetY : offsetX, endPoint = 0;
            if (mOrientation == ORIENTATION.VERTICAL) {
                endPoint = mRecyclerView.getHeight() * position;
            } else {
                endPoint = mRecyclerView.getWidth() * position;
            }
            if (startPoint != endPoint) {
                mAnimator.setIntValues(startPoint, endPoint);
                mAnimator.start();
            }
        }
    }

    private static final String TAG = "PagingScrollHelper";

    public class MyOnFlingListener extends RecyclerView.OnFlingListener {

        @Override
        public boolean onFling(int velocityX, int velocityY) {
            if (mOrientation == ORIENTATION.NULL) {
                return false;
            }
            //获取开始滚动时所在页面的index
            int p = getStartPageIndex();

            Log.d(TAG, "onFling() 1 velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]");
            //记录滚动开始和结束的位置
            int endPoint = 0;
            int startPoint = 0;

            //如果是垂直方向
            if (mOrientation == ORIENTATION.VERTICAL) {
                startPoint = offsetY;

                if (velocityY < 0) {
                    p--;
                } else if (velocityY > 0) {
                    p++;
                }
                //更具不同的速度判断需要滚动的方向
                //注意，此处有一个技巧，就是当速度为0的时候就滚动会开始的页面，即实现页面复位
                endPoint = p * mRecyclerView.getHeight();

                Log.d(TAG, "onFling: 2 VERTICAL startPoint=" + startPoint + "  endPoint=" + endPoint);
            } else {
                startPoint = offsetX;
                if (velocityX < 0) {
                    p--;
                } else if (velocityX > 0) {
                    p++;
                }
                endPoint = p * mRecyclerView.getWidth();

                Log.d(TAG, "onFling: 3 !!!VERTICAL startPoint=" + startPoint + "  endPoint=" + endPoint);
            }
            if (endPoint < 0) {
                endPoint = 0;
            }
            Log.d(TAG, "onFling:  4 startPoint=" + startPoint + "  endPoint=" + endPoint);
            // TODO 获取recyclerview的位置，如果不是屏幕的宽度
            int right = mRecyclerView.getRight();
            int width = mRecyclerView.getWidth();

            Log.d(TAG, "onFling: width="+width  +"  right="+right);

            startAnimation(startPoint, endPoint);

            return true;
        }
    }

    public void startAnimation(int startPoint, int endPoint) {
        Log.d(TAG, "startAnimation() called with: startPoint = [" + startPoint + "], endPoint = [" + endPoint + "]");
        if (Math.abs(endPoint - startPoint) < FLIP_DISTANCE) {
            return;
        }
        //使用动画处理滚动
        if (mAnimator == null) {
            mAnimator = new ValueAnimator().ofInt(startPoint, endPoint);

            mAnimator.setDuration(300);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int nowPoint = (int) animation.getAnimatedValue();
                    if (mOrientation == ORIENTATION.VERTICAL) {
                        int dy = nowPoint - offsetY;
                        //这里通过RecyclerView的scrollBy方法实现滚动。
                        Log.d(TAG, "onAnimationUpdate: y " + dy);
                        mRecyclerView.scrollBy(0, dy);
                    } else {
                        int dx = nowPoint - offsetX;
                        Log.d(TAG, "onAnimationUpdate: x " + dx);
                        mRecyclerView.scrollBy(dx, 0);
                    }
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d(TAG, "onAnimationEnd: " + animation);
                    //回调监听
                    if (null != mOnPageChangeListener) {
                        mOnPageChangeListener.onPageChange(getPageIndex());
                    }
                    //修复双击item bug
                    mRecyclerView.stopScroll();
                    startY = offsetY;
                    startX = offsetX;
                }
            });
        } else {
            mAnimator.cancel();
            Log.d(TAG, "onFling:  5 startPoint=" + startPoint + "  endPoint=" + endPoint);
            mAnimator.setIntValues(startPoint, endPoint);
        }

        mAnimator.start();
    }

    public class MyOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            //newState==0表示滚动停止，此时需要处理回滚
            Log.d(TAG, "onScrollStateChanged()    [" + recyclerView + "], newState = [" + newState + "]");

            if (newState == 0 && mOrientation != ORIENTATION.NULL) {
                boolean move;
                int vX = 0, vY = 0;
                if (mOrientation == ORIENTATION.VERTICAL) {
                    int absY = Math.abs(offsetY - startY);
                    //如果滑动的距离超过屏幕的一半表示需要滑动到下一页
                    move = absY > recyclerView.getHeight() / 2;
                    vY = 0;

                    if (move) {
                        vY = offsetY - startY < 0 ? -1000 : 1000;
                    }

                } else {
                    int absX = Math.abs(offsetX - startX);
                    move = absX > recyclerView.getWidth() / 2;
                    Log.d(TAG, "onScrollStateChanged: move=" + move);
                    if (move) {
                        vX = offsetX - startX < 0 ? -1000 : 1000;
                    }
                }

                Log.d(TAG, "onScrollStateChanged: move=" + vX);
                mOnFlingListener.onFling(vX, vY);

            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //滚动结束记录滚动的偏移量
            Log.d(TAG, "onScrolled()   dx = [" + dx + "], dy = [" + dy + "]");
//            if (Math.abs(dx) < FLIP_DISTANCE) {
//                offsetY = 0;
//                offsetX = 0;
//
//                return;
//            }
            offsetY += dy;
            offsetX += dx;
        }
    }

    protected static final float FLIP_DISTANCE = 50;

    private boolean firstTouch = true;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        float x = 0;
        float y = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            Log.d(TAG, "onTouch() called with: x = [" + x + "], y = [" + y + "]");
            //手指按下的时候记录开始滚动的坐标
            Log.d(TAG, "onTouch() firstTouch = [" + firstTouch + "], startX = " + startX + "  startY = [" + startY + "]");
            if (firstTouch) {
                //第一次touch可能是ACTION_MOVE或ACTION_DOWN,所以使用这种方式判断
                firstTouch = false;
                startY = offsetY;
                startX = offsetX;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    if (y == 0) {
                        y = event.getY();
                        Log.d(TAG, "onTouch: y ACTION_MOVE=" + y);
                    }
                    if (x == 0) {
                        x = event.getX();
                        Log.d(TAG, "onTouch: x ACTION_MOVE=" + x);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    firstTouch = true;
//                    float yy = Math.abs(event.getY() - y);
//                    Log.d(TAG, "onTouch: ACTION_UP  yy=" + yy);
//                    y = 0;
//                    if (yy > FLIP_DISTANCE) {
//                        Log.d(TAG, "onTouch: 上下滑动禁止");
//                        return true;
//                    }
//                    float xx = Math.abs(event.getX() - x);
//                    Log.d(TAG, "onTouch: ACTION_UP  xx=" + xx);
//                    x = 0;
//                    if (xx > FLIP_DISTANCE) {
//                        Log.d(TAG, "onTouch: 左右滑动 false");
//                        return false;
//                    } else {
//                        return true;
//                    }
                }
            }


            return false;
        }

    };

    private int getPageIndex() {
        int p = 0;
        if (mRecyclerView.getHeight() == 0 || mRecyclerView.getWidth() == 0) {
            return p;
        }
        if (mOrientation == ORIENTATION.VERTICAL) {
            p = offsetY / mRecyclerView.getHeight();
        } else {
            p = offsetX / mRecyclerView.getWidth();
        }
        return p;
    }

    private int getStartPageIndex() {
        int p = 0;
        if (mRecyclerView.getHeight() == 0 || mRecyclerView.getWidth() == 0) {
            //没有宽高无法处理
            return p;
        }
        if (mOrientation == ORIENTATION.VERTICAL) {
            p = startY / mRecyclerView.getHeight();
        } else {
            p = startX / mRecyclerView.getWidth();
        }
        return p;
    }

    onPageChangeListener mOnPageChangeListener;

    public void setOnPageChangeListener(onPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public interface onPageChangeListener {
        void onPageChange(int index);
    }

}
