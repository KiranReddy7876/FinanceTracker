package com.financetracker.ui.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.financetracker.R;
import com.financetracker.data.db.entity.Transaction;
import java.text.NumberFormat;
import java.util.*;

public class ReportsFragment extends Fragment {

    private ReportsViewModel viewModel;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvIncome, tvExpense, tvNet, tvMonthLabel;
    private int currentYear, currentMonth;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        tvIncome = view.findViewById(R.id.tv_report_income);
        tvExpense = view.findViewById(R.id.tv_report_expense);
        tvNet = view.findViewById(R.id.tv_report_net);
        tvMonthLabel = view.findViewById(R.id.tv_month_label);
        Button btnPrev = view.findViewById(R.id.btn_prev_month);
        Button btnNext = view.findViewById(R.id.btn_next_month);

        currentYear = viewModel.getSelectedYear();
        currentMonth = viewModel.getSelectedMonth();
        updateMonthLabel();

        btnPrev.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) { currentMonth = 11; currentYear--; }
            viewModel.setMonth(currentYear, currentMonth);
            updateMonthLabel();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) { currentMonth = 0; currentYear++; }
            viewModel.setMonth(currentYear, currentMonth);
            updateMonthLabel();
        });

        setupCharts();

        viewModel.getTransactionsForSelectedMonth().observe(getViewLifecycleOwner(), transactions -> {
            double income = viewModel.sumIncome(transactions);
            double expense = viewModel.sumExpenses(transactions);
            tvIncome.setText(fmt.format(income));
            tvExpense.setText(fmt.format(expense));
            tvNet.setText(fmt.format(income - expense));
            updatePieChart(transactions);
            updateBarChart(transactions);
        });
    }

    private void setupCharts() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setEntryLabelTextSize(11f);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
    }

    private void updatePieChart(List<Transaction> transactions) {
        Map<String, Float> categoryTotals = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.type)) {
                String categoryName = viewModel.getCategoryName(t.categoryId);
                categoryTotals.merge(categoryName, (float) t.amount, Float::sum);
            }
        }

        if (categoryTotals.isEmpty()) { pieChart.clear(); return; }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : categoryTotals.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        int[] colors = {
            Color.parseColor("#4f46e5"), Color.parseColor("#0ea5e9"),
            Color.parseColor("#10b981"), Color.parseColor("#f59e0b"),
            Color.parseColor("#ef4444"), Color.parseColor("#8b5cf6"),
            Color.parseColor("#ec4899"), Color.parseColor("#14b8a6")
        };

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private void updateBarChart(List<Transaction> transactions) {
        // Group by day of month
        float[] incomeByDay = new float[31];
        float[] expenseByDay = new float[31];

        Calendar cal = Calendar.getInstance();
        for (Transaction t : transactions) {
            cal.setTimeInMillis(t.date);
            int day = cal.get(Calendar.DAY_OF_MONTH) - 1;
            if (day >= 0 && day < 31) {
                if ("INCOME".equals(t.type)) incomeByDay[day] += (float) t.amount;
                else if ("EXPENSE".equals(t.type)) expenseByDay[day] += (float) t.amount;
            }
        }

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            if (incomeByDay[i] > 0 || expenseByDay[i] > 0) {
                incomeEntries.add(new BarEntry(i, incomeByDay[i]));
                expenseEntries.add(new BarEntry(i, expenseByDay[i]));
                labels.add(String.valueOf(i + 1));
            }
        }

        if (incomeEntries.isEmpty() && expenseEntries.isEmpty()) { barChart.clear(); return; }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Income");
        incomeSet.setColor(Color.parseColor("#2E7D32"));
        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Expense");
        expenseSet.setColor(Color.parseColor("#C62828"));

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(0.4f);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setData(data);
        barChart.groupBars(0f, 0.1f, 0.05f);
        barChart.invalidate();
    }

    private void updateMonthLabel() {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        tvMonthLabel.setText(months[currentMonth] + " " + currentYear);
    }
}
