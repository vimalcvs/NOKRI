package com.scriptsbundle.nokri.candidate.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.scriptsbundle.nokri.activities.Nokri_MainActivity;
import com.scriptsbundle.nokri.candidate.dashboard.fragments.Nokri_CandidateDashboardFragment;
import com.scriptsbundle.nokri.candidate.dashboard.models.Nokri_CandidateDashboardModel;
import com.scriptsbundle.nokri.candidate.jobs.fragments.Nokri_JobsForYouFragment;
import com.scriptsbundle.nokri.employeer.jobs.fragments.Nokri_JobDetailFragment;
import com.scriptsbundle.nokri.guest.blog.fragments.Nokri_BlogGridFragment;
import com.scriptsbundle.nokri.guest.dashboard.Nokri_GuestDashboardActivity;
import com.scriptsbundle.nokri.guest.faq.fragments.Nokri_FaqFragment;


import com.scriptsbundle.nokri.R;
import com.scriptsbundle.nokri.candidate.edit.fragments.Nokri_CandidateEditProfileFragment;
import com.scriptsbundle.nokri.candidate.jobs.fragments.Nokri_AllJobsFragment;
import com.scriptsbundle.nokri.candidate.profile.fragments.Nokri_MyProfileFragment;
import com.scriptsbundle.nokri.candidate.resume.fragments.Nokri_YourResumeFragment;
import com.scriptsbundle.nokri.guest.home.fragments.Nokri_Home2ScreenFragment;
import com.scriptsbundle.nokri.guest.home.fragments.Nokri_HomeScreenFragment;
import com.scriptsbundle.nokri.guest.search.fragments.Nokri_CandidateSearchFragment;
import com.scriptsbundle.nokri.guest.search.fragments.Nokri_JobSearchFragment;
import com.scriptsbundle.nokri.candidate.follow.fragments.Nokri_FollowingFragment;
import com.scriptsbundle.nokri.candidate.jobs.fragments.Nokri_JobAppliedFragment;
import com.scriptsbundle.nokri.candidate.jobs.fragments.Nokri_SavedJobsFragment;
import com.scriptsbundle.nokri.guest.settings.fragments.Nokri_SettingsFragment;
import com.scriptsbundle.nokri.manager.Nokri_AdManager;
import com.scriptsbundle.nokri.manager.Nokri_DialogManager;
import com.scriptsbundle.nokri.manager.Nokri_FontManager;
import com.scriptsbundle.nokri.manager.Nokri_GoogleAnalyticsManager;
import com.scriptsbundle.nokri.manager.Nokri_PopupManager;
import com.scriptsbundle.nokri.manager.Nokri_SharedPrefManager;
import com.scriptsbundle.nokri.manager.Nokri_ToastManager;
import com.scriptsbundle.nokri.manager.notification.FireBaseNotificationModel;
import com.scriptsbundle.nokri.manager.notification.Nokri_NotificationPopup;
import com.scriptsbundle.nokri.utils.AnimationUtils;
import com.scriptsbundle.nokri.utils.Nokri_Config;
import com.scriptsbundle.nokri.utils.Nokri_Globals;
import com.scriptsbundle.nokri.utils.Nokri_LanguageSupport;
import com.scriptsbundle.nokri.utils.Nokri_Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by Glixen Technologies on 27/01/2018.
 */

public class Nokri_CandidateDashboardActivity extends AppCompatActivity implements Nokri_PopupManager.ConfirmInterface{
    private Toolbar toolbar;
    private TextView toolbarTitleTextView;
    private Nokri_FontManager fontManager;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;
    private LinearLayout toolbarTitleContainer;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    SpaceNavigationView spaceNavigationView;
    public CircularImageView profileImage;
    private Nokri_CandidateDashboardModel candidateDashboardModel;
    private LinearLayout bottomAdContainer,topAdContainer;

    boolean doubleBackToExitPressedOnce = false;
    private Nokri_PopupManager popupManager;
    private Fragment fragment;
    private Class fragmentClass;

    @Override
    protected void onResume() {
        super.onResume();
        Nokri_GoogleAnalyticsManager.getInstance().trackScreenView(getClass().getSimpleName());


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Nokri_Config.APP_COLOR = Nokri_SharedPrefManager.getAppColor(this);
        setContentView(R.layout.activity_nokri_user_dashboard2);

        Nokri_GoogleAnalyticsManager.initialize(this);

        Nokri_GoogleAnalyticsManager.getInstance().get(Nokri_GoogleAnalyticsManager.Target.APP, Nokri_Config.GOOGLE_ANALYTICS_TRACKING_ID);
        Nokri_LanguageSupport.setLocale(this, Nokri_SharedPrefManager.getLocal(this));

        FireBaseNotificationModel fireBaseNotificationModel = Nokri_SharedPrefManager.getFirebaseNotification(this);
        if(fireBaseNotificationModel!=null)
        {
            if(!fireBaseNotificationModel.getTitle().trim().isEmpty()&& Nokri_Globals.SHOULD_HOW_FIREBASE_NOTIFICATION){

                Nokri_NotificationPopup.showNotificationDialog(this,fireBaseNotificationModel.getTitle(),fireBaseNotificationModel.getMessage(),fireBaseNotificationModel.getImage());
                Nokri_Globals.SHOULD_HOW_FIREBASE_NOTIFICATION = false;
            }
        }
        candidateDashboardModel = Nokri_SharedPrefManager.getCandidateSettings(this);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitleTextView = findViewById(R.id.toolbar_title);
        toolbarTitleContainer = findViewById(R.id.toolbar_title_container);
        toolbar.findViewById(R.id.collapse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) findViewById(R.id.collapse);
                imageView.setVisibility(View.VISIBLE);
                ImageView imageViewRefresh = (ImageView) findViewById(R.id.refresh);
                imageViewRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Nokri_JobSearchFragment fragment = new Nokri_JobSearchFragment();

                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_placeholder, fragment).addToBackStack(null).commit();
                    }
                });

//
                Nokri_JobSearchFragment fragment = new Nokri_JobSearchFragment();

                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_placeholder, fragment).addToBackStack(null).commit();
//
            }
        });
        drawer =  findViewById(R.id.drawer_layout);
        navigationView =  findViewById(R.id.nav_view);

        bottomAdContainer = findViewById(R.id.bottom_ad_container);
        topAdContainer = findViewById(R.id.top_ad_container);


        popupManager = new Nokri_PopupManager(this,this);
        if(Nokri_Globals.SHOW_AD) {


            if(Nokri_Globals.IS_BANNER_EBABLED){
                if (Nokri_Globals.SHOW_AD_TOP) {

                    Nokri_AdManager.nokri_displaybanners(this, topAdContainer);
                }

                if (!Nokri_Globals.SHOW_AD_TOP) {

                    Nokri_AdManager.nokri_displaybanners(this, bottomAdContainer);
                }

            }


            if(Nokri_Globals.IS_INTERTIAL_ENABLED)
                Nokri_AdManager.loadInterstitial(this);

        }
        View headerView = navigationView.getHeaderView(0); // 0-index header
        profileImage = headerView.findViewById(R.id.img_profile);
        TextView navHeaderTextView = headerView.findViewById(R.id.txt_nav_header);
        TextView navEmailTextView = headerView.findViewById(R.id.txt_nav_email);
        if(!TextUtils.isEmpty(Nokri_SharedPrefManager.getProfileImage(this)))
        Picasso.with(this).load(Nokri_SharedPrefManager.getProfileImage(this)).fit().centerCrop().into(profileImage);
        if(Nokri_SharedPrefManager.getName(this)!=null)
            navHeaderTextView.setText(Nokri_SharedPrefManager.getName(this));
        if(Nokri_SharedPrefManager.getEmail(this)!=null)
            navEmailTextView.setText(Nokri_SharedPrefManager.getEmail(this));
        fontManager = new Nokri_FontManager();

        fontManager.nokri_setMonesrratSemiBioldFont(toolbarTitleTextView,getAssets());
        fontManager.nokri_setMonesrratSemiBioldFont(navHeaderTextView,getAssets());
        fontManager.nokri_setMonesrratSemiBioldFont(navEmailTextView,getAssets());
        nokri_setNavigationFont(navigationView.getMenu());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Intent intent = getIntent();

        boolean linkedin  = intent.getBooleanExtra("linkedin",false);
        if(linkedin) {
            Nokri_JobDetailFragment jobDetailFragment = new Nokri_JobDetailFragment();

            fragmentTransaction.add(R.id.fragment_placeholder,jobDetailFragment).commit();
                intent.putExtra("linkedin",false);
        }

        else {
            if(Nokri_SharedPrefManager.getHomeType(getApplicationContext()).equals("1")){
            Fragment homeScreenFragment = new Nokri_HomeScreenFragment();
            //  Fragment homeFragmeent = new Nokri_HomeScreenFragment();
            fragmentTransaction.add(R.id.fragment_placeholder, homeScreenFragment).commit();
            toolbarTitleTextView.setText(candidateDashboardModel.getDashboard());
        }
        else
            {
            Fragment homeScreen2Fragment=new Nokri_Home2ScreenFragment();
            fragmentTransaction.add(R.id.fragment_placeholder,homeScreen2Fragment).commit();
                toolbarTitleTextView.setText(candidateDashboardModel.getDashboard());

            }
        }
        setUpNavigationView();
        nokri_setDrawerMenuText(navigationView.getMenu());



        Drawable mDrawable = getResources().getDrawable(R.drawable.drawer_highlight);
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(Color.parseColor(Nokri_Config.APP_COLOR), PorterDuff.Mode.SRC_IN));
        navigationView.setItemBackground(mDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(Nokri_Config.APP_COLOR));
        }
        headerView.setBackgroundColor(Color.parseColor(Nokri_Config.APP_COLOR));
        toolbar.setBackgroundColor(Color.parseColor(Nokri_Config.APP_COLOR));

        profileImage.setBorderColor(Color.parseColor(Nokri_Config.APP_COLOR));
    }

    private void nokri_setDrawerMenuText(Menu menu){

         MenuItem dashboard = menu.findItem(R.id.nav_dashboard);
        MenuItem edit = menu.findItem(R.id.nav_editprofile);
        MenuItem profile = menu.findItem(R.id.nav_fullprofile);
        MenuItem resume = menu.findItem(R.id.nav_resume);
        MenuItem jobs = menu.findItem(R.id.nav_all_jobs);
        MenuItem applied = menu.findItem(R.id.nav_job_applied);
        MenuItem company = menu.findItem(R.id.nav_job_Following);
        MenuItem logout = menu.findItem(R.id.nav_logout);
        MenuItem blog = menu.findItem(R.id.nav_blog);
        MenuItem advancedSearch = menu.findItem(R.id.nav_explore);
        MenuItem exit = menu.findItem(R.id.nav_exit);
        MenuItem save = menu.findItem(R.id.nav_saved_jobs);
        MenuItem home = menu.findItem(R.id.nav_home);
        MenuItem settings = menu.findItem(R.id.nav_settings);
        MenuItem candidateSearch = menu.findItem(R.id.nav_candidate_search);
        MenuItem jobsForYou = menu.findItem(R.id.nav_jobs_for_you);
        dashboard.setTitle(candidateDashboardModel.getDashboard());
        edit.setTitle(candidateDashboardModel.getEdit());
        profile.setTitle(candidateDashboardModel.getProfile());
        resume.setTitle(candidateDashboardModel.getResume());
        jobs.setTitle(candidateDashboardModel.getJobs());
        applied.setTitle(candidateDashboardModel.getApplied());
        company.setTitle(candidateDashboardModel.getCompany());
        logout.setTitle(candidateDashboardModel.getLogout());
        blog.setTitle(candidateDashboardModel.getBlog());
        advancedSearch.setTitle(candidateDashboardModel.getExplore());
        exit.setTitle(candidateDashboardModel.getExit());
        save.setTitle(candidateDashboardModel.getSaved());
        home.setTitle(candidateDashboardModel.getHome());
        settings.setTitle(candidateDashboardModel.getSettings());
        jobsForYou.setTitle(candidateDashboardModel.getJobsForYou());

    }

    private void nokri_setNavigationFont(Menu m){

        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);

            //the method we have create in activity
            fontManager.nokri_applyFontToMenuItem(mi,getAssets());
    }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.action_search:
                handleMenuSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void handleMenuSearch(){
//
    }
    private void doSearch() {
        Nokri_AllJobsFragment fragment = new Nokri_AllJobsFragment();
        fragment.setFilterText(edtSeach.getText().toString().trim());
        Nokri_AllJobsFragment.ALL_JOBS_SOURCE = "";
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_placeholder, fragment).addToBackStack(null).commit();

    }


    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Check to see which item was being clicked and perform appropriate action
                int i = menuItem.getItemId();//Replacing the main content with ContentFragment Which is our Inbox View;
                if (i == R.id.nav_home) {
                    if (Nokri_SharedPrefManager.getHomeType(getApplicationContext()).equals("1")) {

                        fragmentClass = Nokri_HomeScreenFragment.class;

                    } else if (Nokri_SharedPrefManager.getHomeType(getApplicationContext()).equals("2")) {
                        fragmentClass = Nokri_Home2ScreenFragment.class;

                    }
                }


                else if (i == R.id.nav_dashboard) {
                    fragmentClass = Nokri_CandidateDashboardFragment.class;
                } else if (i == R.id.nav_editprofile) {
                    fragmentClass =  Nokri_CandidateEditProfileFragment.class;
                } else if (i == R.id.nav_fullprofile) {
                    fragmentClass = Nokri_MyProfileFragment.class;
                } else if (i == R.id.nav_resume) {
                    fragmentClass = Nokri_YourResumeFragment.class;
                } else if (i == R.id.nav_all_jobs) {
                    fragmentClass = Nokri_AllJobsFragment.class;
                    Nokri_AllJobsFragment.ALL_JOBS_SOURCE = "";
                } else if (i == R.id.nav_job_applied) {
                    fragmentClass = Nokri_JobAppliedFragment.class;
                } else if (i == R.id.nav_jobs_for_you) {
                    fragmentClass = Nokri_JobsForYouFragment.class;
                } else if (i == R.id.nav_saved_jobs) {
                    fragmentClass = Nokri_SavedJobsFragment.class;
                } else if (i == R.id.nav_explore) {
                    fragmentClass = Nokri_JobSearchFragment.class;
                } else if (i == R.id.nav_job_Following) {
                    fragmentClass = Nokri_FollowingFragment.class;
                } else if (i == R.id.nav_blog) {
                    fragmentClass = Nokri_BlogGridFragment.class;
                } else if (i == R.id.nav_logout) {
                    fragmentClass = null;
                    Nokri_DialogManager dialogManager = new Nokri_DialogManager();
                    dialogManager.showAlertDialog(Nokri_CandidateDashboardActivity.this);
                    Nokri_SharedPrefManager.invalidate(Nokri_CandidateDashboardActivity.this);
                    Intent intent = new Intent(Nokri_CandidateDashboardActivity.this, Nokri_MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    dialogManager.hideAlertDialog();
                    finish();
                } else if (i == R.id.nav_settings) {
                    fragmentClass = Nokri_SettingsFragment.class;
                } else if (i == R.id.nav_exit) {
                    popupManager.nokri_showPopupWithCustomMessage(Nokri_Globals.EXIT_TEXT);
                }


                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);


                drawer.closeDrawers();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
                if(fragmentClass!=null){
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(fragment!=null){
                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_placeholder, fragment).commit();}
               fragmentClass = null; }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };
        navigationView.getMenu().getItem(0).setChecked(true);
        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        else{
            if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if(findViewById(R.id.filter_reset_container).getVisibility() == View.VISIBLE)
                {findViewById(R.id.filter_reset_container).setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                return;}
                    /* {
                    drawer.openDrawer(GravityCompat.START);
                    return;
                }*/
             //   drawer.openDrawer(GravityCompat.START);
                if (doubleBackToExitPressedOnce) {
                    if(popupManager!=null)
                        popupManager.nokri_showPopupWithCustomMessage(Nokri_Globals.EXIT_TEXT);
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Nokri_ToastManager.showShortToast(this, Nokri_Globals.ON_BACK_EXIT_TEXT);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);

            }
            else
                getSupportFragmentManager().popBackStackImmediate();
        }
        if(isSearchOpened) {
            handleMenuSearch();
            return;
        }
    }



    @Override
    public void onConfirmClick(Dialog dialog) {

        dialog.dismiss();
        finish();
    }
}
