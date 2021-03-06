package com.chihurumnanya.itransmit.activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.chihurumnanya.itransmit.R;
import com.chihurumnanya.itransmit.data.FirebaseDbContract;
import com.chihurumnanya.itransmit.fragments.PlaceFactoryFragment;
import com.chihurumnanya.itransmit.models.PlaceModel;
import com.chihurumnanya.itransmit.models.TripModel;
import com.chihurumnanya.itransmit.utils.Constants;

/**
 * Activity that holds the place form fragment
 */
public class PlaceFactoryActivity extends FirebaseBaseActivity
        implements PlaceFactoryFragment.OnPlaceFactoryListener {

    public static final int RESULT_PLACE_UPDATED = 1;
    public static final int RESULT_PLACE_DELETED = 2;

    private static final String TAG_PLACE_FACTORY_FRAGMENT = "tag_place_factory_fragment";
    private PlaceFactoryFragment mPlaceFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_factory);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Toast.makeText(this, R.string.place_loading_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // PlaceModel already exists
                PlaceModel place = intent.getParcelableExtra(Constants.Extra.EXTRA_PLACE);
                mPlaceFactoryFragment = PlaceFactoryFragment.newInstance(trip, place);

            } else {
                // New place
                mPlaceFactoryFragment = PlaceFactoryFragment.newInstance(trip, null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.place_factory_fragment_container, mPlaceFactoryFragment,
                            TAG_PLACE_FACTORY_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mPlaceFactoryFragment = (PlaceFactoryFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_PLACE_FACTORY_FRAGMENT);
        }
    }

    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void savePlace(TripModel trip, PlaceModel place, boolean isEditMode) {
        if (place != null) {
            final DatabaseReference placeReference
                    = mRootReference.child(FirebaseDbContract.Places.PATH_PLACES).child
                    (mCurrentUser.getUid());

            if (isEditMode && !TextUtils.isEmpty(place.getId())) {
                DatabaseReference databaseReference = placeReference.child(trip.getId())
                        .child(place.getId());
                databaseReference.setValue(place);
                setResult(RESULT_PLACE_UPDATED);

            } else if (!isEditMode) {
                DatabaseReference databaseReference = placeReference.child(trip.getId()).push();
                place.setId(databaseReference.getKey());
                databaseReference.setValue(place);

            } else {
                Toast.makeText(this, getString(R.string.place_updated_error), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this, getString(R.string.place_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }

        finish();
    }

    @Override
    public void deletePlace(TripModel trip, PlaceModel place) {
        if (place != null) {
            final DatabaseReference placeReference
                    = mRootReference.child(FirebaseDbContract.Places.PATH_PLACES)
                    .child(mCurrentUser.getUid());

            placeReference.child(trip.getId()).child(place.getId()).removeValue();
            setResult(RESULT_PLACE_DELETED);
        } else {
            Toast.makeText(this, getString(R.string.place_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
