package org.antrack.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import org.antrack.app.libs.Files;
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

import java.io.File;
import java.io.IOException;
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

    View fragmentContainer;

    ArrayList<Device> devices;

    // For Drawer
    BaseFragment selectedFragment;
    int selectedDevice = -1;

    // For CreateDeviceMenu
    boolean deviceMenuActive = false;

    boolean firstRun = true;
    boolean initDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*** Init ***/

        Init.all(this);

        if (savedInstanceState != null) {
            firstRun = false;
            V.currentDevice = new Device(savedInstanceState.getString(CURRENT_DEVICE));
        } else {
            V.currentDevice = new Device(Init.DEVICE_NAME_IMEI);
        }

        Log.d(TAG, "Running on: " + android.os.Build.BRAND + " " + android.os.Build.MODEL);

        /*** Start wizard ***/

        String wizard = Settings.get(C.S_LAUNCH_WIZARD);
        if (wizard == null || wizard.equals("true")) {
            Intent intent = new Intent(this, WizardActivity.class);
            startActivityForResult(intent, 1);
        } else {
            main();
        }
    }

    private void main() {
        /*** Check trial and integrity ***/

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Trial.checkTrial()) {
                    // FIXME translate
                    Utils.showToast(MainActivity.this, "Trial is expired");
                    //System.exit(-1);
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

        /*** Start service ***/

        final String serviceEnabled = Settings.get(C.S_ENABLE_SERVICE);

        if (serviceEnabled == null || serviceEnabled.equals("true")) {
            startService();
        }

        /*** Read modules ***/

        readModules();

        /*** Load fragments ***/

        setContentView(R.layout.activity_main);

        container = (FrameLayout) findViewById(R.id.container);
        fragmentManager = getSupportFragmentManager();

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
                    selectedFragment = null;
                    return;
                }
                if (selectedDevice != -1) {
                    fragmentContainer.animate().alpha(1);
                    selectDevice(selectedDevice);
                    selectedDevice = -1;
                }
            }
        };

        drawer.setDrawerListener(drawerToggle);
        // Calling sync state is necessary or hamburger icon wont show up
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        /*** Device selector ***/

        deviceTextView = (TextView) findViewById(R.id.nav_header_main_text1);
        deviceTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createDevicesMenu();
            }
        });

        /*** Load default fragment ***/

        if (firstRun) {
            deviceTextView.setText(V.currentDevice.getName());
            loadFragment(infoFragment);
            navigationView.getMenu().getItem(0).setChecked(true);
        } else {
            if (V.currentDevice.isMain()) {
                deviceTextView.setText(V.currentDevice.getName());
            } else {
                switchDevice();
            }
        }

        Log.d(TAG, "Initialization done");
        initDone = true;
    }

    private void readModules() {
        if (!new File(Init.MAIN_DIR + C.MODULES_FILE).exists()) {
            // Wait for modules init
            LoadingDialog.show(MainActivity.this, getResources().getString(R.string.loading_dialog));

            while (!new File(Init.MAIN_DIR + C.MODULES_FILE).exists()) {
                Utils.sleep(1);
            }

            U.initModules();
            LoadingDialog.hide(MainActivity.this);
        } else {
            U.initModules();
        }
    }

    private void loadFragment(BaseFragment fragment) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, fragment, "fragment");
        ft.commitAllowingStateLoss();
        V.currentFragment = fragment;

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(V.currentDevice.getName() + " / " + fragment.getName());

        Log.d(TAG, "Fragment loaded");
    }

    private void reloadCurrentFragment() {
        BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag("fragment");
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.detach(fragment).attach(fragment).commit();

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(V.currentDevice.getName() + " / " + fragment.getName());

        Log.d(TAG, "Fragment reloaded");
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

        deviceMenuActive = true;

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
                                if (deviceMenuActive) {
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

    // Callback for update fragments on file changes
    public class FileUpdatedFragmentCallback implements FileWatcher.Callback {
        String watchFile = null;

        public void onFileUpdate(String path) {
            if (V.currentFragment != null) {
                V.currentFragment.onFileUpdate();
                Log.d(TAG, "Fragment updated");
            }
        }

        public String getWatchFile() {
            if (V.currentFragment != null) {
                watchFile = "/" + V.currentDevice.getDir() + V.currentFragment.getWatchFile();
            }
            return watchFile;
        }
    }

    // Callback watching for result file
    public class FileUpdatedResultCallback implements FileWatcher.Callback {
        public void onFileUpdate(String path) {
            Log.d(TAG, "RESULT UPDATED!!!");
            String result = "";
            try {
                result = Files.readTextFile(path);
            } catch (IOException e) {
                Log.e(TAG, "Can't read result file: " + e);
            }

            final String result2 = result;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showToast(getApplicationContext(), result2);
                }
            });
        }

        public String getWatchFile() {
            return "/" + V.currentDevice.getDir() + C.RESULT_FILE;
        }
    }

    // Callback for update modules
    public class FileUpdatedModulesCallback implements FileWatcher.Callback {
        public void onFileUpdate(String path) {
            fileWatcher.removeCallback("modules");

            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    switchDevice();
                    LoadingDialog.hide(MainActivity.this);
                }
            });
        }

        public String getWatchFile() {
            return "/" + V.currentDevice.getDir() + C.MODULES_FILE;
        }
    }

    // Callback for update files from cloud
    public class CloudUpdatedCallback implements CloudWatcher.Callback {
        public void onFileUpdate(final String path) {
            if (V.currentFragment != null) {
                String watchFile = V.currentFragment.getWatchFile();
                if (watchFile != null) {
                    if (path.contains(watchFile)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Pw pw = Pw.getInstance();
                                    if (pw.isConnected())
                                        pw.getFile(Init.DEVICES_DIR + path, path);
                                } catch (Exception e) {
                                    Log.d(TAG, "CloudUpdatedCallback: error downloading file: " + e);
                                }
                            }
                        }).start();
                    }
                }
            }
        }

        public String getWatchFile() {
            return "/" + V.currentDevice.getDir() + "/";
        }
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

        if (!initDone) {
            return;
        }
        if (!firstRun) {
            reloadCurrentFragment();
        }

        addCallbacks();

        firstRun = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!initDone)
            return;

        removeCallbacks();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CURRENT_DEVICE, V.currentDevice.getDir());
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

    // Switch device to currentDevice
    private void switchDevice() {
        // Init modules
        U.initModules();

        // Reload menu
        deviceTextView.setText(V.currentDevice.getName());
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        navigationView.getMenu().getItem(0).setChecked(true);

        deviceMenuActive = false;

        loadFragment(infoFragment);
    }

    private void addCallbacks() {
        fileWatcher = FileWatcher.getInstance();
        cloudWatcher = CloudWatcher.getInstance();

        cloudWatcher.removeCallback("ui");
        fileWatcher.removeCallback("ui");

        fileWatcher.addCallback("ui", new FileUpdatedFragmentCallback());

        if (!V.currentDevice.isMain())
            cloudWatcher.addCallback("ui", new CloudUpdatedCallback());

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

    // Called when devices selected from menu
    private void selectDevice(int deviceId) {
        V.currentDevice = devices.get(deviceId);

        // Get modules list if not exist
        if (!V.currentDevice.isMain()) {
            String modulesFile = U.getFullPath(C.MODULES_FILE);
            if (!new File(modulesFile).exists()) {
                fileWatcher = FileWatcher.getInstance();
                fileWatcher.addCallback("modules", new FileUpdatedModulesCallback());
                U.getFileAsync(C.MODULES_FILE);
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
                switchDevice();
                addCallbacks();
            }
        } else {
            switchDevice();
            addCallbacks();
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
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);

        // Hide "No data." and "No module."
        findViewById(R.id.nodata).setVisibility(View.GONE);
        findViewById(R.id.nomodule).setVisibility(View.GONE);

        // Fade out container
        fragmentContainer = findViewById(R.id.container);
        fragmentContainer.animate().alpha(0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}