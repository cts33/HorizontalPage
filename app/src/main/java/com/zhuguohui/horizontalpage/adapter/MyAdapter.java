package com.zhuguohui.horizontalpage.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuguohui.horizontalpage.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhuguohui on 2016/11/8.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private static List<String> data = new ArrayList<>();
    private static int data_name = 0;
    private Random random=new Random();
    public MyAdapter(){
        setData();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_item, parent, false);
        return new MyViewHolder(view);
    }


    private void setData(){
//        int size=random.nextInt(70);
        for (int i = 1; i <= 15; i++) {
            data.add(data_name + "-" + i + "");
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final String title = data.get(position);
        holder.tv_title.setText(title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "item" + title + " 被点击了", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }

    public void updateData() {
        data_name++;
        data.clear();
        setData();

    }
}
