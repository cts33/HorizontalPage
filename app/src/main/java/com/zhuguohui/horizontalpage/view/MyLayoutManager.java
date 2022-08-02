package com.zhuguohui.horizontalpage.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by zhuguohui on 2016/11/9.
 */

public class MyLayoutManager extends RecyclerView.LayoutManager {
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
    }


    private SparseArray<Rect> allItemFrames = new SparseArray<>();

    int totalHeight = 0;
    int offsetY = 0;
    private static final String TAG = "MyLayoutManager";

//    @Override
//    public boolean canScrollHorizontally() {
//        return true;
//    }
//
//    @Override
//    public boolean canScrollVertically() {
//        return false;
//    }

//    @Override
//    public boolean isAutoMeasureEnabled() {
//        return true;
//    }

//    @Override
//    public void scrollToPosition(int position) {
//        super.scrollToPosition(position);
//    }
//
//    @Override
//    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
//        super.smoothScrollToPosition(recyclerView, state, position);
//    }

//    @Override
//    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
//
//        if (state.getItemCount() == 0) {
//            super.onMeasure(recycler, state, widthSpec, heightSpec);
//            return;
//        }
//        if (state.isPreLayout()) return;
//        View itemView = recycler.getViewForPosition(0);
//        addView(itemView);
//        itemView.measure(widthSpec, heightSpec);
//        int mItemWidth = getDecoratedMeasuredWidth(itemView);
//        int mItemHeight = getDecoratedMeasuredHeight(itemView);
//        //回收这个View
//        detachAndScrapView(itemView, recycler);
//        setMeasuredDimension(mItemWidth * allItemFrames.size(), mItemHeight);
//    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        Log.d(TAG, "1 scrollVerticallyBy: "+dy);
        int newY = offsetY + dy;
        int result = dy;
        if (newY > totalHeight) {
            result = totalHeight - offsetY;
        } else if (newY < -getPaddingTop()) {
            result = -getPaddingTop() - offsetY;
        }
        offsetY += result;

        Log.d(TAG, "2 scrollVerticallyBy: "+result);
        offsetChildrenVertical(-result);
        recycleAndFillItems(recycler, state);

        return result;

        //    return super.scrollVerticallyBy(dy, recycler, state);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //    Log.i("zzz", "onLayoutChildren");
        if (getItemCount() == 0) {
            return;
        }
        if (state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);
        int offsetY = 0;
        int count = getItemCount();
        Log.d(TAG, "onLayoutChildren: " + count);
        for (int i = 0; i < count; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);
            Log.d(TAG, "onLayoutChildren:   " + width + "---" + height);
            //   layoutDecorated(view, 0, offsetY, width, offsetY + height);
            Rect rect = allItemFrames.get(i);
            Log.d(TAG, "onLayoutChildren:rect=   " + rect);
            if (rect == null) {
                rect = new Rect();
            }
            rect.set(0, offsetY, width, offsetY + height);
            allItemFrames.put(i, rect);

            offsetY += height;
        }
        totalHeight = Math.max(offsetY, getHeight());
        totalHeight = totalHeight - getHeight() + getPaddingBottom() + getPaddingTop();
        Log.d(TAG, "onLayoutChildren:totalHeight=   " + totalHeight);
        recycleAndFillItems(recycler, state);
        // Log.i("zzz", "item count=" + getChildCount());
    }


    private void recycleAndFillItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            return;
        }

        Rect displayRect = new Rect(getPaddingLeft(), getPaddingTop() + offsetY, getWidth() - getPaddingLeft() - getPaddingRight(), offsetY + getHeight() - getPaddingTop() - getPaddingBottom());
        Log.d(TAG, "recycleAndFillItems: 1 displayRect=" + displayRect);
        Rect childRect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            childRect.left = getDecoratedLeft(child);
            childRect.top = getDecoratedTop(child);
            childRect.right = getDecoratedRight(child);
            childRect.bottom = getDecoratedBottom(child);
            Log.d(TAG, "recycleAndFillItems: 2 displayRect=" + displayRect);
            if (!Rect.intersects(displayRect, childRect)) {
                removeAndRecycleView(child, recycler);
            }
        }

        for (int i = 0; i < getItemCount(); i++) {
            if (Rect.intersects(displayRect, allItemFrames.get(i))) {
                View view = recycler.getViewForPosition(i);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                Rect rect = allItemFrames.get(i);
                layoutDecorated(view, rect.left, rect.top - offsetY, rect.right, rect.bottom - offsetY);
            }
        }

    }


}
