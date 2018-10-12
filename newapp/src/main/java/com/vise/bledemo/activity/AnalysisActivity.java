package com.vise.bledemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

import com.vise.bledemo.R;
import com.vise.bledemo.adapter.AnalysisAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 分析数据的界面
 *
 * @author Darcy
 */
public class AnalysisActivity extends AppCompatActivity {

    //用于存放源数据的数组
    public List<String> groupStrings = new ArrayList<>();

    private List<Integer> groupInt = new ArrayList<>();

    private AnalysisAdapter analysisAdapter;

    private ArrayList<String> stepList;
    private String command;
    //= "ATHST01=0550911";

    private String TAG = "hello";

    String firstTime;
    String lastTime;
    int firstTimeInt;
    int lastTimeInt;

    //用于装截取时间的有效字符串数组
    private List<String> endStringList = new ArrayList<>();

    //用于存放有效的每个子数组的十六进制数据
    private List<String> StringList = new ArrayList<>();

    //用于存放4位有效的时间值数据
    private List<String> fourStringList;

    //用于存放子数据的集合
    private List<List<String>> childStrings = new ArrayList<>();

    //用于装截取时间的有效字符串
    private String endString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Intent intent = getIntent();
        stepList = intent.getStringArrayListExtra("step");
        command = intent.getStringExtra("command");
        String substring = command.substring(command.length() - 4);
        firstTime = substring.substring(0, 2);
        firstTimeInt = Integer.parseInt(firstTime);
        lastTime = substring.substring(2, 4);
        lastTimeInt = Integer.parseInt(lastTime);

        checkData();

        //确定源数组的大小
        getGroupString();

        ExpandableListView expandableView = (ExpandableListView) findViewById(R.id.expandableView);
        analysisAdapter = new AnalysisAdapter(groupStrings, childStrings, this);
        expandableView.setAdapter(analysisAdapter);
    }

    //检查数据
    private void checkData() {
        for (int i = 0; i < stepList.size(); i++) {
            //Log.i("hello", "checkData: " + stepList.get(i));
            if (i == 0) {
                String substring = stepList.get(0).substring(12, stepList.get(0).length());
                endStringList.add(substring);

                endString += substring;
            } else {
                String substring = stepList.get(i).substring(6, stepList.get(i).length());
                endStringList.add(substring);
                endString += substring;
            }
        }
        //Log.i("hello", "checkData: " + endString);
        //将获取到数据的字符串分割成12个一组并存在FourStringList数组中,每个对应的是子数据值
        for (int i = 0; i < ((endString.length()) / 48); i++) {
            String twoSubstring = endString.substring(48 * i, 48 * (i + 1));
            //Log.i(TAG, "checkData: " +twoSubstring);
            StringList.add(twoSubstring);
        }

        // Log.i("hello", "checkData++++++: " + StringList.size());
        //通过二维数组将每个子数据源的数据分割为4位字符串的数组
        for (int i = 0; i < StringList.size(); i++) {
            fourStringList = new ArrayList<>();
            int step = 0;
            Log.i(TAG, "checkData&&&&&&&&&&&&&: " + StringList.get(i).length());
            for (int j = 0; j < ((StringList.get(i).length()) / 4); j++) {
                String twoSubstring = StringList.get(i).substring(4 * j, 4 * (j + 1));
                Log.i(TAG, "checkData: " + twoSubstring);
                //将4位字符的步数转化为有效的步数数据
                String stepString = "" + calculate(twoSubstring);
                //计算源数组中的数据
                step += calculate(twoSubstring);

                fourStringList.add(stepString);
            }
            childStrings.add(fourStringList);
            groupInt.add(step);
        }
        //Log.i(TAG, "checkData:********************** " +groupInt.size());
    }

    //确定组的大小
    private void getGroupString() {
        int length = lastTimeInt - firstTimeInt;
        //填入相关的源数据
        for (int i = 0; i < length + 1; i++) {
            String s = (firstTimeInt + i) + "--" + (firstTimeInt + 1 + i) + "小时的步数 :" + groupInt.get(i);
            groupStrings.add(s);
        }
    }

    /**
     * 计算每5分钟对应的10进制数据
     *
     * @param s 有效的4位数数组
     **/
    private int calculate(String s) {
        int step = 0;

        String firstData = s.substring(0, 2);
        //将第一个2位字符的16进制转化为10进制
        int x = Integer.parseInt(firstData, 16);
        step += x;

        String lastData = s.substring(2, 4);
        //将第二个2位字符的16进制转化为10进制
        int y = Integer.parseInt(lastData, 16);

        step += y;

        // Log.i(TAG, "calculate: "+firstData+"---"+lastData);

        return step;
    }

    /**
     * 计算每5分钟对应的10进制数据
     *
     * @param s 有效的4位数数组
     **/
    private int calculate1(String s) {

        //将第二个2位字符的16进制转化为10进制
        int step = Integer.parseInt(s, 16);

        return step;
    }

    /**
     * 计算每5分钟对应的10进制数据
     *
     * @param s 有效的4位数数组
     **/
    private int calculate2(String s) {
        int step = 0;
        String firstData = s.substring(0, 2);
        if (!firstData.equals("00"))
            step = Integer.parseInt(firstData, 16);

        String lastData = s.substring(2, 4);
        if (!lastData.equals(lastData))
            step = Integer.parseInt(lastData, 16);
        return step;
    }

}
