package org.antrack.app.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import app.R
import org.antrack.app.*
import org.antrack.app.libs.L
import org.antrack.app.service.MainService
import org.antrack.app.ui.callbacks.CloudCallback
import org.antrack.app.ui.callbacks.FragmentCallback
import org.antrack.app.ui.callbacks.ResultCallback
import org.antrack.app.ui.fragments.*
import org.jetbrains.anko.find

class MainActivity : BillingActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        var act: MainActivity? = null
            private set
    }

    internal val TAG = "MainActivity"
    // For SaveState
    internal val CURRENT_DEVICE = "currentDevice"

    lateinit var serviceIntent: Intent
    lateinit var toolbar: Toolbar

    // For Drawer
    internal var selectedFragment: BaseFragment? = null
    internal var selectedDevice = -1

    internal var savedInstanceState: Bundle? = null

    var firstRun = true
    var initDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        act = this

        this.savedInstanceState = savedInstanceState

        if (WizardActivity.needLaunchWizard()) {
            val intent = Intent(this, WizardActivity::class.java)
            startActivityForResult(intent, 1)
        } else {
            main()
        }
    }

    private fun main() {
        L.d(TAG, "Running on: " + android.os.Build.BRAND + " " + android.os.Build.MODEL)

        initState()
        initService()
        initView()
        initToolbar()
        Drawer.init()
        initDefaultFragment()
    }

    private fun initState() {
        savedInstanceState?.let {
            State.device = Device(it.getString(CURRENT_DEVICE))
            State.fragment = fragmentManager.findFragmentByTag("fragment") as BaseFragment
            firstRun = false
        }
    }

    /*
    private void checkTrialAndIntegrity() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Crash app
                if (!Checks.all(MainActivity.this)) {
                    //Pw zz = null;
                    //zz.isConnected();
                    L.e(TAG, "Checks failed");
                }
            }
        }).start();
    }
*/
    private fun initDefaultFragment() {
        if (firstRun) {
            WaitFilesAndSwitchDevice()
        } else {
            switchDevice(false)
        }
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun initView() {
        setContentView(R.layout.activity_main)
    }

    private fun initService() {
        val serviceEnabled = Settings[C.S_ENABLE_SERVICE]

        if (serviceEnabled != C.FALSE) {
            serviceIntent = Intent(this, MainService::class.java)
            startService(serviceIntent)
        }
    }

    // Switch device to currentDevice
    fun switchDevice(bootstrap: Boolean) {
        Drawer.showDefaultMenu()

        // If this is just screen orientation change fragment reloaded in onResume()
        if (bootstrap) {
            State.menuItemTitle = resources.getString(R.string.menu_device_info)
            loadFragment(State.fragment)
        }

        initDone = true
    }

    fun loadFragment(fragment: BaseFragment) {
        //if (fragment == State.fragment) {
        //    reloadCurrentFragment()
        //    return
        //}

        State.fragment.hideAllMessages()

        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment, "fragment")
        ft.commitAllowingStateLoss()

        // We must attach fragment immediately, otherwise getWatchFile() may return null
        fragmentManager.executePendingTransactions()

        State.fragment = fragment
        addCallbacks()
        setToolbarTitle()

        L.d(TAG, "Fragment loaded")
    }

    private fun reloadCurrentFragment() {
        State.fragment.hideAllMessages()

        val fragment = fragmentManager.findFragmentByTag("fragment") as BaseFragment
        val ft = fragmentManager.beginTransaction()
        ft.detach(fragment).attach(fragment).commit()

        addCallbacks()
        setToolbarTitle()

        L.d(TAG, "Fragment reloaded")
    }

    fun setToolbarTitle() {
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.title = State.menuItemTitle

            if (!State.fragment.needSubtitle) {
                actionBar.subtitle = null
                return
            }

            val lastUpdate = State.device.lastUpdate
            if (lastUpdate == null) {
                actionBar.subtitle = State.device.name
            } else {
                actionBar.subtitle = "${State.device.name} ($lastUpdate)"
            }
        }
    }

    fun createDevicesMenu() {
        Drawer.showDevices()
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            //moveTaskToBack(true);
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        main()
    }

    override fun onResume() {
        super.onResume()

        if (!initDone) return
        if (!firstRun) reloadCurrentFragment()

        addCallbacks()

        firstRun = false
    }

    override fun onPause() {
        super.onPause()

        hideKeyboard()

        if (!initDone) return

        removeCallbacks()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (initDone) {
            savedInstanceState.putString(CURRENT_DEVICE, State.device.dir)
            super.onSaveInstanceState(savedInstanceState)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_launch_wizard -> {
                val intent = Intent(this, WizardActivity::class.java)
                startActivityForResult(intent, 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addCallbacks() {
        CloudWatcher.removeCallback("ui")
        FileWatcher.removeCallback("ui")

        FileWatcher.addCallback("ui", FragmentCallback())
        FileWatcher.addCallback("result", ResultCallback(this))

        if (!State.device.isMain) {
            CloudWatcher.addCallback("ui", CloudCallback())
        }
    }

    private fun removeCallbacks() {
        CloudWatcher.removeCallback("ui")
        FileWatcher.removeCallback("result")
        FileWatcher.removeCallback("ui")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_info -> selectedFragment = InfoFragment()
            R.id.nav_map -> selectedFragment = MapFragment()
            R.id.nav_apps -> selectedFragment = AppsFragment()
            R.id.nav_contacts -> selectedFragment = ContactsFragment()
            R.id.nav_calls -> selectedFragment = CallsFragment()
            R.id.nav_sms -> selectedFragment = SmsFragment()
            R.id.nav_screenshots -> selectedFragment = ScreensFragment()
            R.id.nav_photos -> selectedFragment = PhotosFragment()
            R.id.nav_audio -> selectedFragment = AudioFragment()
            R.id.nav_shell -> selectedFragment = ShellFragment()
            //R.id.nav_control -> selectedFragment = ControlFragment()
            R.id.nav_logs -> selectedFragment = LogsFragment()
            R.id.nav_modules -> selectedFragment = ModulesFragment()
            R.id.nav_settings -> selectedFragment = SettingsFragment()
            R.id.nav_howto -> selectedFragment = HowtoFragment()
            else -> selectedDevice = item.itemId - Menu.FIRST
        }

        // Save menu title in State
        State.menuItemTitle = item.title as String

        // Hide "No data.", "No module." and so on
        //State.fragment.hideAllMessages();

        // Fade out container
        val fragmentContainer = findViewById(R.id.container)
        fragmentContainer.animate().alpha(0f)

        val drawer = find<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}