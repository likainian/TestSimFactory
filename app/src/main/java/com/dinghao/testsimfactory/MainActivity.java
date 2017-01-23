package com.dinghao.testsimfactory;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    public int failed;
    private CheckBox mEasy;
    private Button mBegin;
    private ListView mListView;
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    if (failed == 0) {
                        mBegin.setBackgroundColor(Color.GREEN);
                    } else {
                        mBegin.setBackgroundColor(Color.RED);
                    }
                    mProgressBar.setVisibility(View.GONE);
                    FileUtil.saveFile(list.toString(), "/sdcard/testsimfactory", "sim.txt", false);
                    FileUtil.readFile("/sdcard/testsimfactory/sim.txt");
                    mStatus.setText(getString(R.string.finish) + failed + getString(R.string.failed));
                    break;
            }
        }
    };
    private TextView mStatus;
    private ProgressBar mProgressBar;
    private RadioButton mRadioButton1;
    private RadioButton mRadioButton2;
    private RadioButton mRadioButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAdapter();
    }

    private void initAdapter() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        mListView.setAdapter(adapter);
    }

    private void initView() {
        mEasy = (CheckBox) findViewById(R.id.easy);
        mBegin = (Button) findViewById(R.id.begin);
        mListView = (ListView) findViewById(R.id.list_view);

        mBegin.setOnClickListener(this);
        mStatus = (TextView) findViewById(R.id.status);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRadioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        mRadioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        mRadioButton3 = (RadioButton) findViewById(R.id.radioButton3);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.begin:
                mBegin.setBackgroundColor(Color.WHITE);
                mStatus.setText(R.string.testing);
                mProgressBar.setVisibility(View.VISIBLE);
                list.clear();
                adapter.notifyDataSetChanged();
                SimUtil.setFailed(0);
                SimUtil.clearList();
                if(mRadioButton1.isChecked()){
                    SimUtil.setSwitchMode(1);
                }else if(mRadioButton2.isChecked()){
                    SimUtil.setSwitchMode(2);
                }else if(mRadioButton3.isChecked()){
                    SimUtil.setSwitchMode(3);
                }
                Thread switchThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SimUtil.switchSimCard(MainActivity.this, true, 0, new SimUtil.OnSimReadyListener() {

                            @Override
                            public void onSimReady(List<String> result) {
                                list.clear();
                                list.addAll(result);
                                handler.sendEmptyMessage(0);
                            }

                            @Override
                            public void onFinished(int failed) {
                                MainActivity.this.failed = failed;
                                handler.sendEmptyMessage(1);
                            }
                        });
                    }
                });
                switchThread.start();

                break;
        }
    }
}
