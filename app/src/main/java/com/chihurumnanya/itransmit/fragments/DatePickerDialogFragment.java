package com.chihurumnanya.itransmit.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * DialogFragment implementation for DatePickerDialog
 */
public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String SAVE_TARGET_VIEW_ID = "save_target_view_id";
    private static final String SAVE_CURRENT_DATE = "save_current_date";
    private static final String SAVE_MINIMUM_DATE = "save_minimum_date";
    private static final String SAVE_MAXIMUM_DATE = "save_maximum_date";

    private DatePickerDialog mDatePickerDialog;
    private OnDateSetListener mOnDateSetListener;
    private int mTargetViewId;
    private long mCurrentDate;
    private long mMinimumDate;
    private long mMaximumDate;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_TARGET_VIEW_ID, mTargetViewId);
        outState.putLong(SAVE_CURRENT_DATE, mCurrentDate);
        outState.putLong(SAVE_MINIMUM_DATE, mMinimumDate);
        outState.putLong(SAVE_MAXIMUM_DATE, mMaximumDate);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mTargetViewId = savedInstanceState.getInt(SAVE_TARGET_VIEW_ID);
            mCurrentDate = savedInstanceState.getLong(SAVE_CURRENT_DATE);
            mMinimumDate = savedInstanceState.getLong(SAVE_MINIMUM_DATE);
            mMaximumDate = savedInstanceState.getLong(SAVE_MAXIMUM_DATE);
        }

        Calendar calendar = Calendar.getInstance();

        if (mCurrentDate > 0) {
            calendar.setTime(new Date(mCurrentDate));
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

        if (mMinimumDate > 0) {
            mDatePickerDialog.getDatePicker().setMinDate(mMinimumDate);
        }

        if (mMaximumDate > 0) {
            mDatePickerDialog.getDatePicker().setMaxDate(mMaximumDate);
        }

        return mDatePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (mOnDateSetListener != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            mOnDateSetListener.onDateSet(mTargetViewId, calendar.getTimeInMillis());
        }
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        mOnDateSetListener = listener;
    }

    public void setTargetViewId(int targetViewId) {
        mTargetViewId = targetViewId;
    }

    public void setCurrentDate(long currentDate) {
        mCurrentDate = currentDate;
    }

    public void setMinimumDate(long minDate) {
        mMinimumDate = minDate;
    }

    public void setMaximumDate(long maxDate) {
        mMaximumDate = maxDate;
    }

    public interface OnDateSetListener {
        void onDateSet(int targetViewId, long dateMillis);
    }
}
