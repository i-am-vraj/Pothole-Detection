package com.example.mp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.security.KeyStore;
import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity
{
    LineChart mChart;
    float[] array;
    int len;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mChart = findViewById(R.id.graph);
//        mChart.setOnChartGestureListener(this);
//        mChart.setOnChartValueSelectedListener(this);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        getdata();

        ArrayList<Entry> yValues = new ArrayList<>();

        for (int i=0;i<len;++i)
        {
            yValues.add(new Entry(i,array[i]));
        }

        LineDataSet set1 = new LineDataSet(yValues,"dataset 1");

        set1.setFillAlpha(110);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        mChart.setData(data);

    }

    private void getdata() {
        SQLHelper helper;
        SQLiteDatabase database;
        helper = new SQLHelper(getApplicationContext());
        database = helper.getReadableDatabase();
        Cursor cursor = database.query("readings",new String[]{"x","y","z","t"},null,null,null,null,null);

        array = new float[2005];
        String x="X\n",y="Y\n",z="Z\n",t="time\n";

        int i=0;
        if (cursor.moveToLast()) {
            ++i;
            array[i-1]=cursor.getFloat(2);
            x+=""+cursor.getFloat(0)+'\n';
            y+=""+cursor.getFloat(1)+'\n';
            z+=""+cursor.getFloat(2)+'\n';
            t+=""+cursor.getFloat(3)+'\n';
            //arrayList.add(""+cursor.getFloat(0)+' '+cursor.getFloat(1)+' '+cursor.getFloat(2)+' '+cursor.getString(3));
            while (cursor.moveToPrevious() && (i<2000)) {
                //arrayList.add(""+cursor.getFloat(0)+' '+cursor.getFloat(1)+' '+cursor.getFloat(2)+' '+cursor.getString(3));
                ++i;
                array[i-1]=cursor.getFloat(2);
                x+=""+cursor.getFloat(0)+'\n';
                y+=""+cursor.getFloat(1)+'\n';
                z+=""+cursor.getFloat(2)+'\n';
                t+=""+cursor.getFloat(3)+'\n';
            }
        }
        len=i;
    }

}
