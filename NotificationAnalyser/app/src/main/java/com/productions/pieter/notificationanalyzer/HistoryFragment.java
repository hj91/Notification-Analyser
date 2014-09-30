package com.productions.pieter.notificationanalyzer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.productions.pieter.notificationanalyzer.Models.DatabaseHelper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;


public class HistoryFragment extends Fragment {
    private DatabaseHelper databaseHelper = null;
    private Date currentSelectedDate = null;
    private int currentSelectedBarPosition = -1;
    private BarChart barChart = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM");

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this.getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        View viewListHeader = inflater.inflate(R.layout.list_history_header, null);
        barChart = (BarChart) viewListHeader.findViewById(R.id.bar_chart);
        barChart.setBarChartListener(new BarChartListener() {
            @Override
            public void onBarClick(Date date, int position) {
                currentSelectedDate = date;
                currentSelectedBarPosition = position;
                TextView chartDateCurrent = (TextView) getActivity().findViewById(R.id.chart_date_current);
                chartDateCurrent.setText(dateFormat.format(date));
                chartDateCurrent.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) chartDateCurrent.getLayoutParams();
                int marginLeft = getResources().getDimensionPixelOffset(R.dimen.bar_chart_width_bar) * position
                        + getResources().getDimensionPixelOffset(R.dimen.bar_chart_width_bar) / 2 - chartDateCurrent.getWidth() / 2
                        + getResources().getDimensionPixelOffset(R.dimen.barchart_marginSides);
                layoutParams.setMargins(marginLeft, 0, 0, 0);
                chartDateCurrent.setLayoutParams(layoutParams);


                ListView listView = (ListView) getActivity().findViewById(R.id.list_view_history);
                try {
                    List<NotificationAppView> objects = getDatabaseHelper().getNotificationDao().getOverviewDay(date);
                    listView.setAdapter(new NotificationAdapter(getActivity(), objects));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        ListView listHistory = (ListView) view.findViewById(R.id.list_view_history);
        listHistory.addHeaderView(viewListHeader, null, false);
        listHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), AppDetail.class);
                NotificationAppView clickedApp = (NotificationAppView) adapterView.getAdapter().getItem(i);
                intent.putExtra(Intent.EXTRA_SUBJECT, clickedApp.AppName);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ListView listHistory = (ListView) this.getActivity().findViewById(R.id.list_view_history);
        if (currentSelectedDate != null) {
            Calendar calSelected = new GregorianCalendar();
            calSelected.setTime(currentSelectedDate);
            Calendar calToday = Calendar.getInstance();
            if (calSelected.get(Calendar.DATE) == calToday.get(Calendar.DATE)) {
                try {
                    List<NotificationAppView> objects = getDatabaseHelper().getNotificationDao().getOverviewDay(currentSelectedDate);
                    listHistory.setAdapter(new NotificationAdapter(this.getActivity(), objects));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            TextView chartDateCurrent = (TextView) getActivity().findViewById(R.id.chart_date_current);
            chartDateCurrent.setText(dateFormat.format(currentSelectedDate));
            chartDateCurrent.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) chartDateCurrent.getLayoutParams();
            int marginLeft = getResources().getDimensionPixelOffset(R.dimen.bar_chart_width_bar) * currentSelectedBarPosition;
            layoutParams.setMargins(marginLeft, 0, 0, 0);
            chartDateCurrent.setLayoutParams(layoutParams);
        } else {
            listHistory.setAdapter(new NotificationAdapter(this.getActivity(), new LinkedList<NotificationAppView>()));
            TextView chartDateCurrent = (TextView) getActivity().findViewById(R.id.chart_date_current);
            chartDateCurrent.setVisibility(View.INVISIBLE);

        }
        barChart.update();
        TextView chartDateStart = (TextView) this.getActivity().findViewById(R.id.chart_date_start);
        TextView chartDateEnd = (TextView) this.getActivity().findViewById(R.id.chart_date_end);
        chartDateStart.setText(barChart.getFirstDate() != null ? dateFormat.format(barChart.getFirstDate()) : "");
        chartDateEnd.setText(barChart.getLastDate() != null ? dateFormat.format(barChart.getLastDate()) : "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}
