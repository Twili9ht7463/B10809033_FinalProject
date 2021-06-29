package com.example.accountingapp;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountingapp.database.AccountEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This AccountAdapter creates and binds ViewHolders, that hold the amount and level of a task,
 * to a RecyclerView to efficiently display data.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    // Constant for date format
    private static final String DATE_FORMAT = "dd/MM/yyy";

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Class variables for the List that holds account data and the Context
    private List<AccountEntry> mAccountEntries;
    private Context mContext;
    // Date formatter
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    /**
     * Constructor for the AccountAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public AccountAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new AccountViewHolder that holds the view for each task
     */
    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.account_layout, parent, false);

        return new AccountViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        // Determine the values of the wanted data
        AccountEntry accountEntry = mAccountEntries.get(position);
        String amount = accountEntry.getAmount();
        String description = accountEntry.getDescription();
        int level = accountEntry.getLevel();
        String updatedAt = dateFormat.format(accountEntry.getUpdatedAt());
        String currencySymbol = mContext.getString(R.string.currency_symbol);

        //Set values
        holder.accountAmountView.setText(currencySymbol + amount);
        holder.accountDescriptionView.setText(description);
        holder.updatedAtView.setText(updatedAt);

        // Programmatically set the text and color for the level TextView
        String levelString = "" + level; // converts int to String
        holder.levelView.setText(levelString);

        GradientDrawable levelCircle = (GradientDrawable) holder.levelView.getBackground();
        // Get the appropriate background color based on the level
        int levelColor = getLevelColor(level);
        levelCircle.setColor(levelColor);
    }

    /*
    Helper method for selecting the correct level circle color.
    P1 = red, P2 = orange, P3 = yellow
    */
    private int getLevelColor(int level) {
        int levelColor = 0;

        switch (level) {
            case 1:
                levelColor = ContextCompat.getColor(mContext, R.color.materialRed);
                break;
            case 2:
                levelColor = ContextCompat.getColor(mContext, R.color.materialOrange);
                break;
            case 3:
                levelColor = ContextCompat.getColor(mContext, R.color.materialYellow);
                break;
            default:
                break;
        }
        return levelColor;
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mAccountEntries == null) {
            return 0;
        }
        return mAccountEntries.size();
    }

    public List<AccountEntry> getAccounts() {
        return mAccountEntries;
    }

    /**
     * When data changes, this method updates the list of accountEntries
     * and notifies the adapter to use the new values on it
     */
    public void setAccounts(List<AccountEntry> accountEntries) {
        mAccountEntries = accountEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class AccountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView accountAmountView;
        TextView accountDescriptionView;
        TextView updatedAtView;
        TextView levelView;

        /**
         * Constructor for the AccountViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public AccountViewHolder(View itemView) {
            super(itemView);
            accountAmountView = itemView.findViewById(R.id.accountAmount);
            accountDescriptionView = itemView.findViewById(R.id.description);
            updatedAtView = itemView.findViewById(R.id.accountUpdatedAt);
            levelView = itemView.findViewById(R.id.levelTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mAccountEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
