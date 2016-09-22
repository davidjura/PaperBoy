package app.paperboy.paperboy;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by David Jura
 *
 * This class handles the Home Activity
 */
public class HomeActivity extends AppCompatActivity {
    private FeedLoader feedLoader;
    private SwipeRefreshLayout swipeView;
    private ListView homeList;
    private TextView txtNoFavorites;
    private TimelineAdapter adapter;
    private UserData userData;
    private ProgressBar progressLoading;

    private boolean isHomeClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Typeface mainFont = Typeface.createFromAsset(getAssets(), "fonts/appfont.ttf");

        //Instantiate FeedLoader instance and set the response callback interface
        feedLoader = new FeedLoader(this);
        feedLoader.setFeedLoadListener(feedListener);

        userData = new UserData(this);

        isHomeClicked = true;

        //Setup layout views
        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(mainFont);

        ImageView btnHome = (ImageView) findViewById(R.id.btnHome);
        btnHome.setOnTouchListener(homeClick);

        ImageView btnFavorites = (ImageView) findViewById(R.id.btnFavorites);
        btnFavorites.setOnTouchListener(favoriteClick);

        ImageView btnLogo = (ImageView) findViewById(R.id.btnLogo);
        btnLogo.setOnClickListener(logoClick);

        ImageView btnSettings = (ImageView) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(settingsClick);

        //Setup loading circle and set the color to the app's primary color
        progressLoading = (ProgressBar) findViewById(R.id.progressBar);
        progressLoading.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(
                        this,
                        R.color.colorPrimary),
                        PorterDuff.Mode.SRC_IN
                );

        //Setup the SwipeRefreshLayout element
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipeView);
        swipeView.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );
        swipeView.setOnRefreshListener(refreshListener);
        swipeView.setVisibility(View.INVISIBLE);

        //Setup the main ListView element
        homeList = (ListView) findViewById(R.id.homeList);
        homeList.setVisibility(View.INVISIBLE);

        //Setup the empty favorites message
        txtNoFavorites = (TextView) findViewById(R.id.txtNoFavorites);
        txtNoFavorites.setTypeface(mainFont);

        //fetch the home feed
        feedLoader.fetchFeed();

    }

    /**
     * This method ensures that when a user returns from an article from their favorites, they go
     * back to their favorites
     */
    @Override
    protected void onResume() {
        if (!isHomeClicked)
            loadFavorites();
        super.onResume();
    }

    /**
     * Touch listener for the home menu button
     */
    private View.OnTouchListener homeClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setY(view.getY() + 20);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setY(view.getY() - 20);
                    if (!isHomeClicked) {
                        feedLoader.fetchFeed();
                        isHomeClicked = true;
                        txtNoFavorites.setVisibility(View.INVISIBLE);
                        progressLoading.setVisibility(View.VISIBLE);
                    } else
                        homeList.smoothScrollToPosition(0);
                    break;
            }
            return true;
        }
    };

    /**
     * Touch listener for the favorite menu button
     */
    private View.OnTouchListener favoriteClick = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setY(view.getY() + 20);
                    break;
                case MotionEvent.ACTION_UP:
                    progressLoading.setVisibility(View.VISIBLE);
                    view.setY(view.getY() - 20);
                    //load the user's favorite list
                    loadFavorites();
                    break;
            }
            return true;
        }
    };

    /**
     * Touch listener for the refreshListener
     */
    private SwipeRefreshLayout.OnRefreshListener refreshListener =
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //Clear the adapter and re-fetch timeline feed or load user favorites
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    if (isHomeClicked)
                        feedLoader.fetchFeed();
                    else
                        loadFavorites();
                }
            };

    /**
     * FeedLoadListener instantiation to handle FeedLoader response callback
     */
    private FeedLoader.FeedLoadListener feedListener = new FeedLoader.FeedLoadListener() {
        @Override
        public void onResult(ArrayList<TimeLineDocument> data) {
            //Set visibilities, re-initialize the adapter, and update the homeList
            swipeView.setVisibility(View.VISIBLE);
            homeList.setVisibility(View.VISIBLE);
            adapter = new TimelineAdapter(getApplicationContext(), data);
            homeList.setAdapter(adapter);
            progressLoading.setVisibility(View.INVISIBLE);
            swipeView.setRefreshing(false);
        }
    };

    /**
     * Click listener for the settings button
     */
    private View.OnClickListener settingsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(HomeActivity.this, IntroActivity.class);
            i.putExtra("settings_click", "yes");
            startActivity(i);
        }
    };

    /**
     * Click listener for the logo
     */
    private View.OnClickListener logoClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Navigate to the top of the homeList
            homeList.smoothScrollToPosition(0);
        }
    };

    /**
     * This method load's a user's favorites
     */
    private void loadFavorites() {
        ArrayList<TimeLineDocument> newlist = userData.getFavoritesList();
        //If no articles have been added to the favorites, show the txtNoFavorites message
        if (newlist.size() == 0) {
            txtNoFavorites.setVisibility(View.VISIBLE);
        }
        isHomeClicked = false;
        //Re-initialize the adapter and update homeList with it
        adapter.clear();
        adapter = new TimelineAdapter(getApplicationContext(), newlist);
        adapter.notifyDataSetChanged();
        homeList.setAdapter(adapter);
        progressLoading.setVisibility(View.INVISIBLE);
        swipeView.setRefreshing(false);
    }
}
