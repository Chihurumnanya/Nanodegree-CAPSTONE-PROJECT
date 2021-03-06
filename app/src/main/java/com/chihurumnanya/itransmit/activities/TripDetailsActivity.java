package com.chihurumnanya.itransmit.activities;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.chihurumnanya.itransmit.GlideApp;
import com.chihurumnanya.itransmit.R;
import com.chihurumnanya.itransmit.adapters.ViewPagerAdapter;
import com.chihurumnanya.itransmit.data.FirebaseDbContract;
import com.chihurumnanya.itransmit.fragments.BudgetListFragment;
import com.chihurumnanya.itransmit.fragments.ExpenseListFragment;
import com.chihurumnanya.itransmit.fragments.PlaceListFragment;
import com.chihurumnanya.itransmit.listeners.OnBudgetInteractionListener;
import com.chihurumnanya.itransmit.listeners.OnExpenseInteractionListener;
import com.chihurumnanya.itransmit.listeners.OnPlaceInteractionListener;
import com.chihurumnanya.itransmit.models.AttributionModel;
import com.chihurumnanya.itransmit.models.BudgetModel;
import com.chihurumnanya.itransmit.models.ExpenseModel;
import com.chihurumnanya.itransmit.models.PlaceModel;
import com.chihurumnanya.itransmit.models.TripModel;
import com.chihurumnanya.itransmit.utils.Constants;
import com.chihurumnanya.itransmit.utils.Features;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * Activity which holds the fragment that displays Trips' details
 */
public class TripDetailsActivity extends FirebaseBaseActivity implements
        OnExpenseInteractionListener,
        OnBudgetInteractionListener,
        OnPlaceInteractionListener {

    public static final int TAB_EXPENSES_POSITION = 0;
    public static final int TAB_BUDGETS_POSITION = 1;
    public static final int TAB_PLACES_POSITION = 2;
    private static final String SAVE_IS_SHARED_ELEMENT_TRANSITION =
            "save_is_shared_element_transition";
    @BindView(R.id.attribution_container)
    protected LinearLayout mAttributionContainer;
    @BindView(R.id.trip_photo)
    protected ImageView mTripPhoto;
    @BindView(R.id.trip_photo_protection)
    protected View mTripPhotoProtection;
    @BindView(R.id.attribution_prefix)
    protected TextView mAttributionPrefix;
    @BindView(R.id.trip_title)
    protected TextView mTripTitle;
    @BindView(R.id.attribution_content)
    protected TextView mAttributionContent;
    @BindView(R.id.fab)
    protected FloatingActionButton mFab;
    @BindView(R.id.tablayout)

    protected TabLayout mTabLayout;
    private TripModel mTrip;
    private boolean mIsSharedElementTransition;
    private int mDefaultTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            mTrip = getIntent().getParcelableExtra(Constants.Extra.EXTRA_TRIP);
            mDefaultTabIndex = getIntent().getIntExtra(Constants.Extra.EXTRA_TRIP_DETAILS_TAB_INDEX,
                    TAB_EXPENSES_POSITION);
        }

        if (mTrip == null) {
            Toast.makeText(this, R.string.could_not_load_trip_details, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_trip_details);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mIsSharedElementTransition = savedInstanceState.getBoolean
                    (SAVE_IS_SHARED_ELEMENT_TRANSITION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        } else {
            supportPostponeEnterTransition();
        }


        setTripBackdropPhoto(mTrip.getId());
        setupAppBarLayout();
        setupToolbar();
        setupFab();
        setupViewPager(mDefaultTabIndex);
        setTransitionNames();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = toolbar.findViewById(R.id.trip_title);
        title.setText(mTrip.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void setupFab() {
        mFab.setImageResource(getFabImageResource());
        mFab.setContentDescription(getFabContentDescription(this));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mTabLayout.getSelectedTabPosition()) {
                    case TAB_EXPENSES_POSITION:
                        startExpenseFactory(null);
                        break;
                    case TAB_BUDGETS_POSITION:
                        startBudgetFactory(null);
                        break;
                    case TAB_PLACES_POSITION:
                        startPlaceFactory(null);
                        break;
                }
            }
        });
    }

    private void setupAppBarLayout() {
        AppBarLayout appbarLayout = findViewById(R.id.appbarlayout);

        if (Features.TRIP_LIST_SHARED_ELEMENT_TRANSITION_ENABLED) {
            appbarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    switch (state) {
                        case COLLAPSED:
                        case IDLE:
                            mIsSharedElementTransition = false;
                            break;
                        case EXPANDED:
                            mIsSharedElementTransition = true;
                            break;
                    }

                }
            });
        }
    }

    private void setTransitionNames() {
        String tripId = mTrip.getId();
        ViewCompat.setTransitionName(mTripPhoto,
                Constants.Transition.PREFIX_TRIP_PHOTO + tripId);
        ViewCompat.setTransitionName(mTripPhotoProtection,
                Constants.Transition.PREFIX_TRIP_PHOTO_PROTECTION + tripId);
        ViewCompat.setTransitionName(mAttributionContainer,
                Constants.Transition.PREFIX_TRIP_ATTRIBUTION + tripId);
        ViewCompat.setTransitionName(mTripTitle,
                Constants.Transition.PREFIX_TRIP_TITLE + tripId);
    }

    private int getFabImageResource() {
        switch (mTabLayout.getSelectedTabPosition()) {
            case TAB_EXPENSES_POSITION:
                return R.drawable.ic_fab_expense;
            case TAB_BUDGETS_POSITION:
                return R.drawable.ic_fab_budget;
            case TAB_PLACES_POSITION:
                return R.drawable.ic_fab_place;
            default:
                return R.drawable.ic_fab_plus;
        }
    }

    private String getFabContentDescription(Context context) {
        switch (mTabLayout.getSelectedTabPosition()) {
            case TAB_EXPENSES_POSITION:
                return context.getString(R.string.add_expense);
            case TAB_BUDGETS_POSITION:
                return context.getString(R.string.add_budget);
            case TAB_PLACES_POSITION:
                return context.getString(R.string.add_place);
            default:
                return context.getString(R.string.add_expense);
        }
    }

    private void animateFab() {
        if (!mFab.isShown()) {
            mFab.setImageResource(getFabImageResource());
            mFab.show();
        } else {
            mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    mFab.setImageResource(getFabImageResource());
                    mFab.show();
                }
            });
        }
    }

    private void setupViewPager(int defaultTabIndex) {
        ViewPager viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(ExpenseListFragment.newInstance(mTrip), getString(R.string.expenses));
        adapter.addFragment(BudgetListFragment.newInstance(mTrip), getString(R.string.budgets));
        adapter.addFragment(PlaceListFragment.newInstance(mTrip), getString(R.string.places));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(defaultTabIndex);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_IS_SHARED_ELEMENT_TRANSITION, mIsSharedElementTransition);
        super.onSaveInstanceState(outState);
    }

    private void setTripBackdropPhoto(String tripId) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directoryFile = cw.getDir(Constants.General.DESTINATION_PHOTO_DIR,
                Context.MODE_PRIVATE);
        final File photoFile = new File(directoryFile, tripId);

        ImageView tripPhotoBackdrop = findViewById(R.id.trip_photo);

        GlideApp
                .with(this)
                .load(photoFile)
                .signature(new ObjectKey(photoFile.lastModified()))
                .error(R.drawable.trip_photo_default)
                .placeholder(R.color.tripCardPlaceholderBackground)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        displayPhotoAttribution(mTrip.getId(), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        displayPhotoAttribution(mTrip.getId(), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                        return false;
                    }
                })
                .into(tripPhotoBackdrop);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                if (mIsSharedElementTransition) {
                    supportFinishAfterTransition();
                } else {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsSharedElementTransition) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    private void displayPhotoAttribution(String tripId, boolean shouldDisplay) {
        if (mAttributionContainer != null) {
            if (shouldDisplay) {
                DatabaseReference attributionsReference = mRootReference
                        .child(FirebaseDbContract.Attributions.PATH_ATTRIBUTIONS)
                        .child(mCurrentUser.getUid())
                        .child(tripId);

                attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("onDataChange");
                        if (mAttributionContainer != null) {
                            AttributionModel attribution = dataSnapshot.getValue(AttributionModel
                                    .class);
                            if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                                Spanned result;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(attribution.getText(),
                                            Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(attribution.getText());
                                }

                                mAttributionContent.setText(result);
                                mAttributionContent.setMovementMethod(
                                        LinkMovementMethod.getInstance());
                                mAttributionPrefix.setVisibility(View.VISIBLE);
                                mAttributionContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d("onCancelled", databaseError.getMessage());
                    }
                });
            } else {
                mAttributionPrefix.setVisibility(View.GONE);
                mAttributionContent.setVisibility(View.GONE);
            }
        }
    }

    public void startExpenseFactory(ExpenseModel expense) {
        Intent intent = new Intent(TripDetailsActivity.this, ExpenseFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (expense != null) {
            intent.putExtra(Constants.Extra.EXTRA_EXPENSE, (Parcelable) expense);
        }
        startActivity(intent);
    }

    public void startBudgetFactory(BudgetModel budget) {
        Intent intent = new Intent(TripDetailsActivity.this, BudgetFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (budget != null) {
            intent.putExtra(Constants.Extra.EXTRA_BUDGET, (Parcelable) budget);
        }
        startActivity(intent);
    }

    public void startPlaceFactory(PlaceModel place) {
        Intent intent = new Intent(TripDetailsActivity.this, PlaceFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (place != null) {
            intent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
        }
        startActivity(intent);
    }

    public void startPlaceDetails(PlaceModel place) {
        Intent intent = new Intent(TripDetailsActivity.this,
                PlaceDetailsActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (place != null) {
            intent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
        }
        startActivity(intent);
    }

    @Override
    public void onExpenseClicked(ExpenseModel expense) {
        startExpenseFactory(expense);
    }

    @Override
    public void onBudgetClicked(BudgetModel budget) {
        startBudgetFactory(budget);
    }

    @Override
    public void onPlaceClicked(PlaceModel place) {
        startPlaceDetails(place);
    }

    private static abstract class AppBarStateChangeListener implements
            AppBarLayout.OnOffsetChangedListener {

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }
    }
}
