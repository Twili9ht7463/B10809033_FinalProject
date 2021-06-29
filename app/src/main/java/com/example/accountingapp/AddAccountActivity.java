package com.example.accountingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accountingapp.database.AccountEntry;
import com.example.accountingapp.database.AppDatabase;

import java.util.Date;

public class AddAccountActivity extends AppCompatActivity {

    // Extra for the account ID to be received in the intent
    public static final String EXTRA_ACCOUNT_ID = "extraAccountId";
    // Extra for the account ID to be received after rotation
    public static final String INSTANCE_ACCOUNT_ID = "instanceAccountId";
    // Constants for level
    public static final int LEVEL_HIGH = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_LOW = 3;
    // Constant for default account id to be used when not in update mode
    private static final int DEFAULT_ACCOUNT_ID = -1;
    // Constant for logging
    private static final String TAG = AddAccountActivity.class.getSimpleName();
    // Fields for views
    EditText amountEditText, descriptionEditText;
    RadioGroup mRadioGroup;
    Button mButton;

    private int mAccountId = DEFAULT_ACCOUNT_ID;

    // Member variable for the Database
    private AppDatabase mDb;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ACCOUNT_ID)) {
            mAccountId = savedInstanceState.getInt(INSTANCE_ACCOUNT_ID, DEFAULT_ACCOUNT_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ACCOUNT_ID)) {
            mButton.setText("Update");
            if (mAccountId == DEFAULT_ACCOUNT_ID) {
                mAccountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, DEFAULT_ACCOUNT_ID);

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        final AccountEntry account = mDb.accountDao().loadAccountById(mAccountId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateUI(account);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_ACCOUNT_ID, mAccountId);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        amountEditText = findViewById(R.id.editTextAccountAmount);
        descriptionEditText = findViewById(R.id.editTextAccountDescription);
        mRadioGroup = findViewById(R.id.radioGroup);

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    private void populateUI(AccountEntry account) {
        if (account == null) {
            return;
        }

        amountEditText.setText(account.getAmount());
        descriptionEditText.setText(account.getDescription());
        setLevelInViews(account.getLevel());
    }

    public void onSaveButtonClicked() {
        String amount = amountEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        int level = getLevelFromViews();
        Date date = new Date();

        final AccountEntry account = new AccountEntry(amount, level, description, date);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mAccountId == DEFAULT_ACCOUNT_ID) {
                    mDb.accountDao().insertAccount(account);
                } else {
                    account.setId(mAccountId);
                    mDb.accountDao().updateAccount(account);
                }
                finish();
            }
        });
    }

    public int getLevelFromViews() {
        int level = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButton1:
                level = LEVEL_HIGH;
                break;
            case R.id.radButton2:
                level = LEVEL_MEDIUM;
                break;
            case R.id.radButton3:
                level = LEVEL_LOW;
        }
        return level;
    }

    public void setLevelInViews(int level) {
        switch (level) {
            case LEVEL_HIGH:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton1);
                break;
            case LEVEL_MEDIUM:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton2);
                break;
            case LEVEL_LOW:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton3);
        }
    }
}
