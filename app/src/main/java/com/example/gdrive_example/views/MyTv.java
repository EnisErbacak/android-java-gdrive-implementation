package com.example.gdrive_example.views;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MyTv extends androidx.appcompat.widget.AppCompatTextView{

    public MyTv(Context context, String str) {
        super(context);
        setText(str);
        setBackgroundColor(Color.BLACK);
        setTextColor(Color.GREEN);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setPadding(0,10,0,10);
    }
}