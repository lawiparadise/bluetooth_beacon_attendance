package com.law.blueinnofora;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MonthItemView extends RelativeLayout {
    private Context mContext;

    private MonthItem item;

    private RelativeLayout itemContainer;
    private TextView dayText;
    private ImageView weatherImage;

    public MonthItemView(Context context) {
        super(context);

        mContext = context;

        init();
    }

    public MonthItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.month_item, this, true);

        itemContainer = (RelativeLayout) findViewById(R.id.itemContainer);
        dayText = (TextView) findViewById(R.id.dayText);
        weatherImage = (ImageView) findViewById(R.id.weatherImage);

        itemContainer.setBackgroundColor(Color.WHITE);
    }


    public MonthItem getItem() {
        return item;
    }

    public void setItem(MonthItem item) {
        this.item = item;

        int day = item.getDay();
        if (day != 0) {
            dayText.setText(String.valueOf(day));
        } else {
            dayText.setText("");
        }

    }

    public void setWeatherImage(int resId) {
        weatherImage.setImageResource(resId);
    }

    public void setTextColor(int color) {
        dayText.setTextColor(color);
    }

    public void setBackgroundColor(int color) {
        itemContainer.setBackgroundColor(color);
    }

}