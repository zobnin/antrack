package org.antrack.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.antrack.app.C;
import org.antrack.app.CloudWatcher;
import org.antrack.app.FileWatcher;
import org.antrack.app.Init;
import org.antrack.app.Pw;
import org.antrack.app.Trial;
import org.antrack.app.libs.Checks;
import org.antrack.app.libs.Keyboard;
import org.antrack.app.libs.LoadingDialog;
import org.antrack.app.libs.Utils;
import org.antrack.app.service.MainService;
import org.antrack.app.Settings;
import org.antrack.app.ui.fragments.AppsFragment;
import org.antrack.app.ui.fragments.AudioFragment;
import org.antrack.app.ui.fragments.BaseFragment;
import org.antrack.app.ui.fragments.CallsFragment;
import org.antrack.app.ui.fragments.ContactsFragment;
import org.antrack.app.ui.fragments.ControlFragment;
import org.antrack.app.ui.fragments.HowtoFragment;
import org.antrack.app.ui.fragments.InfoFragment;
import org.antrack.app.ui.fragments.LogsFragment;
import org.antrack.app.ui.fragments.MapFragment;
import org.antrack.app.ui.fragments.ModulesFragment;
import org.antrack.app.ui.fragments.PhotosFragment;
import org.antrack.app.ui.fragments.ScreensFragment;
import org.antrack.app.ui.fragments.SettingsFragment;
import org.antrack.app.ui.fragments.ShellFragment;
import org.antrack.app.ui.fragments.SmsFragment;
import org.antrack.app.ui.callbacks.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import app.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final String TAG = "MainActivity";
    // For SaveState
    final String CURRENT_DEVICE = "currentDevice";

    FrameLayout container;

    NavigationView   navigationView;
    FragmentManager  fragmentManager;

    InfoFragment     infoFragment;
    MapFragment      mapFragment;
    AppsFragment     appsFragment;
    ContactsFragment contactsFragment;
    CallsFragment    callsFragment;
    SmsFragment      smsFragment;
    ScreensFragment  screensFragment;
    PhotosFragment   photosFragment;
    AudioFragment    audioFragment;
    ControlFragment  controlFragment;
    ShellFragment    shellFragment;
    LogsFragment     logsFragment;
    ModulesFragment  modulesFragment;
    SettingsFragment settingsFragment;
    HowtoFragment    howtoFragment;

    FileWatcher  fileWatcher;
    CloudWatcher cloudWatcher;

    Intent serviceIntent;

    TextView deviceTextView;
    TextView deviceTextView2;

    View fragmentContainer;

    ArrayList<Device> devices;

    // For Drawer
    BaseFragment selectedFragment;
    int selectedDevice = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*** Init ***/

        Init.all(this);

        if (savedInstanceState != null) {
            State.firstRun = false;
            State.device = new Device(savedInstanceState.getString(CURRENT_DEVICE));
        } else {
            State.device = new Device(Init.DEVICE_NAME_IMEI);
        }

        Log.d(TAG, "Running on: " + android.os.Build.BRAND + " " + android.os.Build.MODEL);

        /*** Start wizard ***/

        String wizard = Settings.get(C.S_LAUNCH_WIZARD);
        if (wizard == null || wizard.equals(C.TRUE)) {
            Intent intent = new Intent(this, WizardActivity.class);
            startActivityForResult(intent, 1);
        } else {
            main();
        }
    }

    private void main() {
        /*** Start service ***/

        final String serviceEnabled = Settings.get(C.S_ENABLE_SERVICE);

        if (serviceEnabled == null || serviceEnabled.equals(C.TRUE)) {
            startService();
        }

        /*** Load fragments ***/

        setContentView(R.layout.activity_main);

        container = (FrameLayout) findViewById(R.id.container);
        fragmentManager = getFragmentManager();

        infoFragment     = new InfoFragment();
        mapFragment      = new MapFragment();
        appsFragment     = new AppsFragment();
        contactsFragment = new ContactsFragment();
        callsFragment    = new CallsFragment();
        smsFragment      = new SmsFragment();
        screensFragment  = new ScreensFragment();
        photosFragment   = new PhotosFragment();
        audioFragment    = new AudioFragment();
        controlFragment  = new ControlFragment();
        shellFragment    = new ShellFragment();
        logsFragment     = new LogsFragment();
        modulesFragment  = new ModulesFragment();
        settingsFragment = new SettingsFragment();
        howtoFragment    = new HowtoFragment();

        /*** Toolbar ***/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*** Drawer ***/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            // Hide keyboard on drawer open
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Keyboard.hide(MainActivity.this);
            }
            // Change fragment on drawer close
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (selectedFragment != null) {
                    fragmentContainer.animate().alpha(1);
                    loadFragment(selectedFragment);
                } else if (selectedDevice != -1) {
                    fragmentContainer.animate().alpha(1);
                    State.device = devices.get(selectedDevice);
                    waitModulesAndSwitchDevice();
                }
                selectedFragment = null;
                selectedDevice = -1;
            }
        };

        drawer.setDrawerListener(drawerToggle);
        // Calling sync state is necessary or hamburger icon wont show up
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*** Drawer header ***/

        deviceTextView = (TextView) findViewById(R.id.nav_header_main_text1);
        deviceTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createDevicesMenu();
            }
        });

        deviceTextView2 = (TextView) findViewById(R.id.nav_header_main_text2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Pw pw = Pw.getInstance();
                final String mail = pw.getEmail();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceTextView2.setText(mail);
                    }
                });
            }
        }).start();

        /*** Load default fragment ***/

        if (State.firstRun) {
            waitModulesAndSwitchDevice();
        } else {
            switchDevice(false);
            State.initDone = true;
        }

        /*** Check trial and integrity ***/

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Trial.checkTrial()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(MainActivity.this, getResources().getString(R.string.trial_is_expired));
                            //System.exit(-1);
                        }
                    });
                    Log.e(TAG, "Trial is expired");
                }
                // Crash app
                if (!Checks.all(MainActivity.this)) {
                    //Pw zz = null;
                    //zz.isConnected();
                    Log.e(TAG, "Checks failed");
                }

            }
        }).start();
    }

    // Switch device to currentDevice
    public void switchDevice(boolean bootstrap) {
        /*
        if (!U.readModules()) {
            Utils.showToast(this, getResources().getString(R.string.cant_load_device));
            return;
        }
        */

        U.readModules();
        U.readFeatures();

        // Reload menu
        deviceTextView.setText(State.device.getName());
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        navigationView.getMenu().getItem(0).setChecked(true);

        State.deviceMenuActive = false;

        // If this is just screen orientation change fragment reloaded in onResume()
        if (bootstrap) {
            State.menuItemTitle = getResources().getString(R.string.menu_device_info);
            loadFragment(infoFragment);
            State.initDone = true;
        }

    }

    private void loadFragment(BaseFragment fragment) {
        if (fragment.equals(State.fragment)) {
            reloadCurrentFragment();
            return;
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (State.fragment == null) {
            ft.add(R.id.container, fragment, "fragment");
        } else {
            ft.replace(R.id.container, fragment, "fragment");
        }

        ft.commitAllowingStateLoss();

        // We must attach fragment immediately, otherwise getFiles() may return null
        fragmentManager.executePendingTransactions();

        State.fragment = fragment;
        setToolbarTitle();
        addCallbacks();

        Log.d(TAG, "Fragment loaded");
    }

    private void reloadCurrentFragment() {
        BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag("fragment");
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.detach(fragment).attach(fragment).commit();

        setToolbarTitle();

        Log.d(TAG, "Fragment reloaded");
    }

    private void setToolbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(State.menuItemTitle);

            String lastUpdate = U.getLastUpdate();
            if (lastUpdate == null) {
                getSupportActionBar().setSubtitle(State.device.getName());
            } else {
                getSupportActionBar().setSubtitle(
                        State.device.getName() + " (" + lastUpdate + ")");
            }

        }
    }

    // Create device selection menu
    // For better user experience method redraws menu two times:
    // 1. with items from app folder (fast, not synced)
    // 2. with items from cloud (slow, synced)
    private void createDevicesMenu() {
        /*** Show devices from app folder ***/

        ArrayList<String> deviceDirs = new ArrayList<>(Arrays.asList(new File(Init.DEVICES_DIR).list()));
        Collections.sort(deviceDirs);

        navigationView.getMenu().clear();

        devices = new ArrayList<>();

        int i = 0;
        for (String deviceDir : deviceDirs) {
            if (!deviceDir.matches("[A-Za-z].*_[0-9]{4}"))
                continue;

            Device device = new Device(deviceDir);
            devices.add(device);

            navigationView.getMenu().add(0, Menu.FIRST + i, Menu.NONE, device.getName())
                    .setIcon(R.drawable.ic_menu_device);
            i = i + 1;
        }

        State.deviceMenuActive = true;

        /*** Show devices from cloud ***/

        // If not connected - don't redraw menu and exit
        Pw pw = Pw.getInstance();
        if (!pw.isConnected()) {
            showToast(MainActivity.this, getResources().getString(R.string.not_connected_to_cloud));
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<String> deviceDirs = U.listDir("");

                    if (deviceDirs == null) {
                        showToast(MainActivity.this, getResources().getString(R.string.cant_get_devices_list));
                        return;
                    }

                    Collections.sort(deviceDirs);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            navigationView.getMenu().clear();

                            devices = new ArrayList<>();

                            int i = 0;
                            for (String deviceDir : deviceDirs) {
                                if (!new File(deviceDir).getName().matches("[A-Za-z].*_[0-9]{4}"))
                                    continue;

                                Device device = new Device(new File(deviceDir).getName());
                                devices.add(device);

                                //noinspection ResultOfMethodCallIgnored
                                new File(Init.DEVICES_DIR + deviceDir).mkdir();

                                // Workaround: if device menu is not shown don't redraw it
                                if (State.deviceMenuActive) {
                                    navigationView.getMenu().add(0, Menu.FIRST + i, Menu.NONE, device.getName())
                                            .setIcon(R.drawable.ic_menu_device);
                                }
                                i = i + 1;
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "Device selector error: " + e);
                }

            }
        }).start();
    }

    private void startService() {
        serviceIntent = new Intent(this, MainService.class);
        startService(serviceIntent);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        main();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!State.initDone) {
            return;
        }

        if (!State.firstRun) {
            reloadCurrentFragment();
        }

        addCallbacks();

        State.firstRun = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!State.initDone)
            return;

        removeCallbacks();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_DEVICE, State.device.getDir());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_launch_wizard:
                Intent intent = new Intent(this, WizardActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addCallbacks() {
        fileWatcher = FileWatcher.getInstance();
        cloudWatcher = CloudWatcher.getInstance();

        cloudWatcher.removeCallback("ui");
        fileWatcher.removeCallback("ui");

        fileWatcher.addCallback("ui", new FragmentCallback());

        if (!State.device.isMain())
            cloudWatcher.addCallback("ui", new CloudCallback());

    }

    private void removeCallbacks() {
        fileWatcher = FileWatcher.getInstance();
        cloudWatcher = CloudWatcher.getInstance();

        cloudWatcher.removeCallback("ui");
        fileWatcher.removeCallback("ui");
    }

    private void showToast(final Context context, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void waitModulesAndSwitchDevice() {
        // Get modules list and features if not exist
        String modulesFile = U.getLocalPath(C.MODULES_FILE);
        String featuresFile = U.getLocalPath(C.FEATURES_FILE);

        if (!new File(modulesFile).exists() || !new File(featuresFile).exists()) {
            fileWatcher = FileWatcher.getInstance();
            fileWatcher.addCallback("modules", new ModulesCallback(this));
            fileWatcher.addCallback("features", new FeaturesCallback(this));

            U.getFileAsync(C.MODULES_FILE);
            U.getFileAsync(C.FEATURES_FILE);

            LoadingDialog.show(MainActivity.this, getResources().getString(R.string.loading_dialog));

            // Workaround to stop loading dialog if don't get modules
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utils.sleep(15);
                    if (LoadingDialog.isShown()) {
                        LoadingDialog.hide(MainActivity.this);
                        showToast(MainActivity.this, getResources().getString(R.string.cant_connect));
                    }
                }
            }).start();
        } else {
            switchDevice(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_info:
                selectedFragment = infoFragment;
                break;
            case R.id.nav_map:
                selectedFragment = mapFragment;
                break;
            case R.id.nav_apps:
                selectedFragment = appsFragment;
                break;
            case R.id.nav_contacts:
                selectedFragment = contactsFragment;
                break;
            case R.id.nav_calls:
                selectedFragment = callsFragment;
                break;
            case R.id.nav_sms:
                selectedFragment = smsFragment;
                break;
            case R.id.nav_screenshots:
                selectedFragment = screensFragment;
                break;
            case R.id.nav_photos:
                selectedFragment = photosFragment;
                break;
            case R.id.nav_audio:
                selectedFragment = audioFragment;
                break;
            case R.id.nav_shell:
                selectedFragment = shellFragment;
                break;
            case R.id.nav_control:
                selectedFragment = controlFragment;
                break;
            case R.id.nav_logs:
                selectedFragment = logsFragment;
                break;
            case R.id.nav_modules:
                selectedFragment = modulesFragment;
                break;
            case R.id.nav_settings:
                selectedFragment = settingsFragment;
                break;
            case R.id.nav_howto:
                selectedFragment = howtoFragment;
                break;
            case R.id.nav_site:
                break;
            default:
                selectedDevice = item.getItemId() - Menu.FIRST;
        }

        // Select item
        //if (item.isChecked()) item.setChecked(false);
        //else item.setChecked(true);
        item.setChecked(true);

        // Save menu title in State
        State.menuItemTitle = (String) item.getTitle();

        // Hide "No data.", "No module." and so on
        //selectedFragment.hideAll();
        findViewById(R.id.nodata).setVisibility(View.GONE);
        findViewById(R.id.nomodule).setVisibility(View.GONE);
        findViewById(R.id.noroot).setVisibility(View.GONE);
        findViewById(R.id.nophone).setVisibility(View.GONE);

        // Fade out container
        fragmentContainer = findViewById(R.id.container);
        fragmentContainer.animate().alpha(0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}