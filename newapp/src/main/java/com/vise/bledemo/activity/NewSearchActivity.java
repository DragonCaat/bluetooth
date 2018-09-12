package com.vise.bledemo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.vise.bledemo.R;
import com.vise.bledemo.adapter.ViewPagerAdapter;
import com.vise.bledemo.fragment.FragmentBoneBlueTooth;
import com.vise.bledemo.fragment.FragmentUnBoneBlueTooth;
import com.vise.bledemo.view.NavitationLayout;

import java.util.ArrayList;
import java.util.List;

public class NewSearchActivity extends AppCompatActivity {
    private NavitationLayout navitationLayout;
    private ViewPager viewPager;
    private String[] titles = { "已配对蓝牙", "未配对蓝牙" };

    private ViewPagerAdapter viewPagerAdapter;
    private List<Fragment> fragments;

    private FragmentBoneBlueTooth fragmentBoneBlueTooth;
    private FragmentUnBoneBlueTooth fragmentUnBoneBlueTooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_search);

        init();
    }

    private void init() {
        fragmentBoneBlueTooth = new FragmentBoneBlueTooth();
        fragmentUnBoneBlueTooth = new FragmentUnBoneBlueTooth();

        initShowNaviationBar();

    }
    private void initShowNaviationBar() {
        navitationLayout = (NavitationLayout) findViewById(R.id.bar1);
        viewPager = (ViewPager) findViewById(R.id.viewpager1);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);

        fragments = new ArrayList<>();
        fragments.add(fragmentBoneBlueTooth);
        fragments.add(fragmentUnBoneBlueTooth);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);

        viewPager.setAdapter(viewPagerAdapter);

        navitationLayout.setViewPager(this, titles, viewPager, R.color.white, R.color.beauty_blue, 14, 16, 0, 0, true);
        navitationLayout.setBgLine(this, 1, R.color.white);
        navitationLayout.setNavLine(this, 3, R.color.beauty_blue, 0);

    }

}
