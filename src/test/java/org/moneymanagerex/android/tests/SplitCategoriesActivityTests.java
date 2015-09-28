package org.moneymanagerex.android.tests;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MmexContentProvider;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.view.RobotoTextView;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Unit tests for Split Categories activity.
 *
 * Created by Alen Siljak on 28/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SplitCategoriesActivityTests {

    private ActivityController<SplitTransactionsActivity> controller;
    private SplitTransactionsActivity activity;

    @BeforeClass
    public static void suiteSetup() {
        // can't initialize content provider here as the static context does not have an application.
    }

    @Before
    public void setUp() {
        // set up the content provider
        UnitTestHelper.initializeContentProvider();
        // todo: insert any data here, if needed.

        this.controller = UnitTestHelper.getController(SplitTransactionsActivity.class);
//        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @After
    public void tearDown() {
        this.controller.destroy();
    }

    @Test
    public void activityRunsStandalone() {
        assertThat(this.activity).isNotNull();
    }

    /**
     * Confirm that the displayed amount after entry contains the correctly formatted currency,
     * and the correct currency.
     */
    @Test
    public void displayCurrencyMatchesTheAccount() {
        // set up

        Intent intent = createIntent();
        Money enteredAmount = MoneyFactory.fromString("5.38");

        // run

        this.activity = this.controller
                .withIntent(intent)
                .create().visible().start().get();

        assertThat(activity).isNotNull();
        assertThat(activity.getIntent().getStringExtra(SplitTransactionsActivity.KEY_DATASET_TYPE))
                .isEqualTo(TableSplitTransactions.class.getSimpleName());

        // enter number
        // get the id of the first visible split fragment.
        Fragment fragment = activity.getSupportFragmentManager().getFragments().get(0);
        assertThat(fragment).isNotNull();
        // get amount text box.
        View view = fragment.getView().findViewById(R.id.editTextTotAmount);
        assertThat(view).isNotNull();

        // click to open input dialog here

        // receive amount back
        ((IInputAmountDialogListener) fragment).onFinishedInputAmountDialog(
                view.getId(), enteredAmount);

        // view must be text view.
        assertThat(view.getClass()).isEqualTo(RobotoTextView.class);
        assertThat((String) view.getTag()).isEqualTo(enteredAmount.toString());
        String actualAmountText = ((TextView) view).getText().toString();
        assertThat(actualAmountText).isNotEqualTo(enteredAmount.toString());
        assertThat(actualAmountText).isEqualTo("€ 5.38");
    }

    private Intent createIntent() {
        // Recurring transactions
        // TableBudgetSplitTransactions.class.getSimpleName()
        // Account Transactions
        String datasetName = TableSplitTransactions.class.getSimpleName();
        TransactionTypes transactionType = TransactionTypes.Withdrawal;
        ArrayList<ISplitTransactionsDataset> mSplitTransactions = null;
        ArrayList<ISplitTransactionsDataset> mSplitTransactionsDeleted = null;
        int currencyId = 2;

        Context context = UnitTestHelper.getApplication();

        // this is a copy of production intent code

        Intent intent = new Intent(context, SplitTransactionsActivity.class);
        intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, transactionType.getCode());
        intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION, mSplitTransactions);
        intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionsDeleted);
        intent.putExtra(SplitTransactionsActivity.KEY_CURRENCY_ID, currencyId);

//        mParent.startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);

        return intent;
    }
}
