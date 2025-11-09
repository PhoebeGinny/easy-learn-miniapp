package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.astrology.AstrologyInterpreter;
import com.example.demo.astrology.ChartView;
import com.example.demo.astrology.PlanetCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 主 Activity：负责接收用户输入（出生年月日、时间、经纬度），调用计算器计算行星黄经与宫位，
 * 并把结果传给 ChartView 渲染，同时用 AstrologyInterpreter 生成简单的文字解析。
 *
 * 注：本项目演示计算流程，若要精确到秒级/高精度，请集成 Swiss Ephemeris（见代码注释）。
 */
public class MainActivity extends AppCompatActivity {

    EditText etName, etDate, etTime;

    private Spinner etLocation;
    Button btnGenerate;
    ChartView chartView;
    TextView tvAnalysis;

    // 记录用户选择的日期与时间
    private int year, month, day, hour, minute;

    private double selectedLongitude = 0.0;

    // 定义出生地与经度的映射
    private final Map<String, Double> locationLongitudeMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        etName = findViewById(R.id.et_name);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etLocation = findViewById(R.id.et_location);
        btnGenerate = findViewById(R.id.btn_generate);
        chartView = findViewById(R.id.chartView);
        tvAnalysis = findViewById(R.id.tv_analysis);


        // 禁止用户直接输入，改为点击选择
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etTime.setFocusable(false);
        etTime.setClickable(true);

        // 点击生日框 → 弹出日期选择器
        etDate.setOnClickListener(vv -> showDatePickerDialog());

        // 点击出生时间框 → 弹出时间选择器
        etTime.setOnClickListener(vv -> showTimePickerDialog());

        // 初始化城市和经度（你可以添加更多）
        locationLongitudeMap.put("北京 (116.40°E)", 116.40);
        locationLongitudeMap.put("上海 (121.47°E)", 121.47);
        locationLongitudeMap.put("广州 (113.27°E)", 113.27);
        locationLongitudeMap.put("香港 (114.17°E)", 114.17);
        locationLongitudeMap.put("东京 (139.69°E)", 139.69);
        locationLongitudeMap.put("伦敦 (0.12°W)", -0.12);
        locationLongitudeMap.put("纽约 (74.00°W)", -74.00);

        // 将城市填充进 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(locationLongitudeMap.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etLocation.setAdapter(adapter);

        // 监听用户选择的城市
        etLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCity = (String) parent.getItemAtPosition(position);
                selectedLongitude = locationLongitudeMap.get(selectedCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String date = etDate.getText().toString().trim(); // "YYYY-MM-DD"
                String time = etTime.getText().toString().trim(); // "HH:MM"
                String loc = selectedLongitude+""; // "lon,lat"
                if (date.isEmpty() || time.isEmpty() || loc.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请完整填写生日、时间与经纬度", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // 解析经纬度
//                    String[] parts = loc.split(",");
//                    double lon = Double.parseDouble(parts[0].trim());
//                    double lat = Double.parseDouble(parts[1].trim());

                    // 调用计算器
                    PlanetCalculator.CalcResult result = PlanetCalculator.calculateFromLocal(date, time, selectedLongitude, selectedLongitude);

                    // 传给 ChartView 渲染
                    chartView.setResult(result);

                    // 分析文本
                    String analysis = AstrologyInterpreter.interpret(result, etName.getText().toString());
                    tvAnalysis.setText(analysis);

                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "解析输入时出错: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        });
    }

    // 日期选择对话框
    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int initYear = c.get(Calendar.YEAR);
        int initMonth = c.get(Calendar.MONTH);
        int initDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    this.year = year;
                    this.month = monthOfYear + 1; // 月份从0开始计数
                    this.day = dayOfMonth;
                    etDate.setText(year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
                }, initYear, initMonth, initDay);
        datePickerDialog.show();
    }

    // 时间选择对话框
    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int initHour = c.get(Calendar.HOUR_OF_DAY);
        int initMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    this.hour = hourOfDay;
                    this.minute = minute;
                    etTime.setText(String.format("%02d:%02d", hour, minute));
                }, initHour, initMinute, true); // true 表示24小时制
        timePickerDialog.show();
    }
}
