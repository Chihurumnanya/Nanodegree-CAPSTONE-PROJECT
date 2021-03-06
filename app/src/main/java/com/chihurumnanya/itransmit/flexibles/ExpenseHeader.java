package com.chihurumnanya.itransmit.flexibles;

import android.view.View;
import android.widget.TextView;

import com.chihurumnanya.itransmit.R;
import com.chihurumnanya.itransmit.models.HeaderModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Header for Expense items
 */
public class ExpenseHeader extends AbstractHeaderItem<ExpenseHeader.HeaderViewHolder>
        implements IFilterable, IHolder<HeaderModel> {

    private HeaderModel mExpenseHeader;

    public ExpenseHeader(HeaderModel expenseHeader) {
        mExpenseHeader = expenseHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExpenseHeader) {
            ExpenseHeader inItem = (ExpenseHeader) o;
            return mExpenseHeader.equals(inItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mExpenseHeader.hashCode();
    }

    @Override
    public HeaderModel getModel() {
        return mExpenseHeader;
    }

    @Override
    public boolean filter(String constraint) {
        return mExpenseHeader.getTitle() != null && mExpenseHeader.getTitle().equals(constraint);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.expense_header_item;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeaderViewHolder holder,
                               int position, List payloads) {
        holder.mTitle.setText(mExpenseHeader.getTitle());
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        @BindView(R.id.expense_header_title)
        public TextView mTitle;

        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//true only for header items when will be sticky
            ButterKnife.bind(this, view);
        }
    }
}