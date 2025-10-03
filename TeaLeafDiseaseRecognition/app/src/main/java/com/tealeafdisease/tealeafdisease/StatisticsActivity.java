package com.tealeafdisease.tealeafdisease;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {

    private static final boolean WEEK_START_SUNDAY = false;

    private BarChart barChart;
    private PieChart pieChart;
    private Spinner filterSpinner;
    private Button btnBack, btnPickDate, btnApplyRange, btnToggleChart;
    private EditText etCustomRange;
    private TextView tvSelectedDate, tvSummary;
    private AppDatabase db;

    private Calendar pickedDate = null;

    private final Map<String, Integer> diseaseColors = new HashMap<>() {{
        put("Healthy", Color.GREEN);
        put("Tea Leaf Blight", Color.RED);
        put("Tea Red Leaf Spot", Color.MAGENTA);
        put("Tea Red Scab", Color.CYAN);
    }};

    private final Map<String, String> shortNames = new HashMap<>() {{
        put("Healthy", "Healthy");
        put("Tea Leaf Blight", "Leaf Blight");
        put("Tea Red Leaf Spot", "Red Spot");
        put("Tea Red Scab", "Red Scab");
    }};

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dayMonthFmt = new SimpleDateFormat("MMM-dd", Locale.getDefault());
    private final SimpleDateFormat monthFmt = new SimpleDateFormat("MMM", Locale.getDefault());
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.getDefault());

    private boolean showPie = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        filterSpinner = findViewById(R.id.filterSpinner);
        btnBack = findViewById(R.id.btnBack);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnApplyRange = findViewById(R.id.btnApplyRange);
        btnToggleChart = findViewById(R.id.btnToggleChart);
        etCustomRange = findViewById(R.id.etCustomRange);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSummary = findViewById(R.id.tvSummary);

        db = androidx.room.Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "statistics-db"
        ).allowMainThreadQueries().build();

        Calendar today = Calendar.getInstance();
        tvSelectedDate.setText("Today: " + dayFmt.format(today.getTime()) + " " + dayMonthFmt.format(today.getTime()));

        String[] filters = {"Day", "Week", "Month"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, filters);
        filterSpinner.setAdapter(adapter);

        loadChartData("Day", today);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar ref = (pickedDate != null) ? (Calendar) pickedDate.clone() : Calendar.getInstance();
                loadChartData(filters[position], ref);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnApplyRange.setOnClickListener(v -> applyCustomRange());
        btnToggleChart.setOnClickListener(v -> toggleChart());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        final Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    pickedDate = Calendar.getInstance();
                    pickedDate.set(year, month, dayOfMonth, 0, 0, 0);

                    Calendar today = Calendar.getInstance();
                    String friendly = dayFmt.format(pickedDate.getTime()) + " " + dayMonthFmt.format(pickedDate.getTime());
                    if (isSameDay(pickedDate, today)) {
                        tvSelectedDate.setText("Today: " + friendly);
                    } else {
                        tvSelectedDate.setText("Selected: " + friendly);
                    }
                    loadChartData("Day", pickedDate);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void applyCustomRange() {
        String input = etCustomRange.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Enter number of days", Toast.LENGTH_SHORT).show();
            return;
        }

        int days;
        try {
            days = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (days <= 0) {
            Toast.makeText(this, "Enter a positive number", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar reference = (pickedDate != null) ? (Calendar) pickedDate.clone() : Calendar.getInstance();
        Calendar startCal = (Calendar) reference.clone();
        startCal.add(Calendar.DAY_OF_YEAR, -(days - 1));
        Calendar endCal = (Calendar) reference.clone();

        String rangeText = String.format(Locale.getDefault(),
                "From %s %s to %s %s",
                dayFmt.format(startCal.getTime()), dayMonthFmt.format(startCal.getTime()),
                dayFmt.format(endCal.getTime()), dayMonthFmt.format(endCal.getTime()));
        tvSelectedDate.setText(rangeText);

        loadChartDataForRange(startCal, endCal);
    }

    private void toggleChart() {
        showPie = !showPie;
        if (showPie) {
            barChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
            btnToggleChart.setText("\uD83D\uDCCA Show Bar Chart");
        } else {
            barChart.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            btnToggleChart.setText("\uD83D\uDEDE Show Pie Chart");
        }
    }

    private void loadChartData(String filter, Calendar referenceDate) {
        List<StatisticsEntity> stats = db.statisticsDao().getAll();
        List<StatisticsEntity> filteredStats = new ArrayList<>();

        try {
            Calendar startCal = (Calendar) referenceDate.clone();
            Calendar endCal = (Calendar) referenceDate.clone();
            String rangeText = "";

            if ("Week".equals(filter)) {
                if (WEEK_START_SUNDAY) {
                    while (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                        startCal.add(Calendar.DAY_OF_YEAR, -1);
                    }
                } else {
                    while (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        startCal.add(Calendar.DAY_OF_YEAR, -1);
                    }
                }
                endCal = (Calendar) startCal.clone();
                endCal.add(Calendar.DAY_OF_YEAR, 6);

                rangeText = String.format(Locale.getDefault(),
                        "From %s %s to %s %s",
                        dayFmt.format(startCal.getTime()), dayMonthFmt.format(startCal.getTime()),
                        dayFmt.format(endCal.getTime()), dayMonthFmt.format(endCal.getTime()));
                tvSelectedDate.setText(rangeText);

            } else if ("Month".equals(filter)) {
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                endCal = (Calendar) startCal.clone();
                int lastDay = startCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                endCal.set(Calendar.DAY_OF_MONTH, lastDay);

                rangeText = String.format(Locale.getDefault(),
                        "From %s-01 to %s-%02d",
                        monthFmt.format(startCal.getTime()),
                        monthFmt.format(startCal.getTime()),
                        lastDay);
                tvSelectedDate.setText(rangeText);

            } else { // Day
                Calendar today = Calendar.getInstance();
                String friendly = dayFmt.format(referenceDate.getTime()) + " " + dayMonthFmt.format(referenceDate.getTime());
                if (isSameDay(referenceDate, today)) {
                    tvSelectedDate.setText("Today: " + friendly);
                } else {
                    tvSelectedDate.setText("Selected: " + friendly);
                }
            }

            Date startDate = sdf.parse(sdf.format(startCal.getTime()));
            Date endDate = sdf.parse(sdf.format(endCal.getTime()));
            if (startDate == null || endDate == null) {
                renderCharts(filteredStats);
                return;
            }

            for (StatisticsEntity s : stats) {
                if (s.date == null) continue;
                Date recordDate = sdf.parse(s.date);
                if (recordDate == null) continue;

                if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                    filteredStats.add(s);
                }
            }

        } catch (ParseException e) {
            Log.e("StatisticsActivity", "Date parse error", e);
        }

        renderCharts(filteredStats);
    }

    private void loadChartDataForRange(Calendar start, Calendar end) {
        List<StatisticsEntity> stats = db.statisticsDao().getAll();
        List<StatisticsEntity> filteredStats = new ArrayList<>();

        try {
            Date startDate = sdf.parse(sdf.format(start.getTime()));
            Date endDate = sdf.parse(sdf.format(end.getTime()));
            if (startDate == null || endDate == null) {
                renderCharts(filteredStats);
                return;
            }

            for (StatisticsEntity s : stats) {
                if (s.date == null) continue;
                Date recordDate = sdf.parse(s.date);
                if (recordDate == null) continue;

                if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                    filteredStats.add(s);
                }
            }
        } catch (ParseException e) {
            Log.e("StatisticsActivity", "Date parse error", e);
        }
        renderCharts(filteredStats);
    }

    private void renderCharts(List<StatisticsEntity> stats) {
        Map<String, Integer> diseaseCountMap = new LinkedHashMap<>();
        int diseasedCount = 0;
        int totalCount = 0;

        for (StatisticsEntity s : stats) {
            int current = diseaseCountMap.getOrDefault(s.disease, 0);
            diseaseCountMap.put(s.disease, current + s.count);
            totalCount += s.count;
            if (!"Healthy".equals(s.disease)) {
                diseasedCount += s.count;
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : diseaseCountMap.entrySet()) {
            entries.add(new BarEntry(idx, e.getValue()));
            labels.add(shortNames.getOrDefault(e.getKey(), e.getKey()));
            colors.add(diseaseColors.getOrDefault(e.getKey(), Color.GRAY));
            idx++;
        }

        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(colors);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.BLACK);
        barChart.setData(new BarData(set));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int i = Math.round(value);
                return (i >= 0 && i < labels.size()) ? labels.get(i) : "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.BLACK);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false); // ⬅ no subtitle
        barChart.animateY(800);
        barChart.invalidate();

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> pieColors = new ArrayList<>();
        for (Map.Entry<String, Integer> e : diseaseCountMap.entrySet()) {
            pieEntries.add(new PieEntry(e.getValue(), shortNames.getOrDefault(e.getKey(), e.getKey())));
            pieColors.add(diseaseColors.getOrDefault(e.getKey(), Color.GRAY));
        }
        PieDataSet pieSet = new PieDataSet(pieEntries, "");
        pieSet.setColors(pieColors);
        pieSet.setValueTextSize(12f);
        pieSet.setValueTextColor(Color.BLACK);
        pieChart.setData(new PieData(pieSet));
        pieChart.getLegend().setEnabled(true);
        pieChart.getDescription().setEnabled(false); // ⬅ no subtitle
        pieChart.invalidate();

        if (totalCount > 0) {
            float percentage = (diseasedCount * 100f) / totalCount;
            tvSummary.setText(String.format(Locale.getDefault(), "%d diseased leaves among %d leaves (%.1f%% diseased)",
                    diseasedCount, totalCount, percentage));
        } else {
            tvSummary.setText("No data available for selected period.");
        }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
