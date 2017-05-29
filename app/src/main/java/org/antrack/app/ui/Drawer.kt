package org.antrack.app.ui

import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import app.R
import org.antrack.app.*
import org.antrack.app.libs.L
import org.antrack.app.ui.fragments.BaseFragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.io.File
import java.util.*

object Drawer {

    private lateinit var navigationView: NavigationView
    private lateinit var deviceTextView: TextView
    private lateinit var drawerHeader: View
    private lateinit var devices: ArrayList<Device>

    // We must use function to recreate drawer on rotate
    // Android saves objects on rotate
    fun init() {
        with (MainActivity.act!!) {
            val drawer = findViewById(R.id.drawer_layout) as DrawerLayout

            navigationView = findViewById(R.id.nav_view) as NavigationView
            navigationView.setNavigationItemSelectedListener(this)

            drawerHeader = navigationView.getHeaderView(0)

            deviceTextView = drawerHeader.findViewById(R.id.nav_header_main_text1) as TextView
            deviceTextView.setOnClickListener {
                rotateArrowUp()
                createDevicesMenu()
            }

            val deviceTextView2 = drawerHeader.findViewById(R.id.nav_header_main_text2) as TextView

            val drawerToggle = object : ActionBarDrawerToggle(this, drawer, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close) {

                // Hide keyboard on drawer open
                override fun onDrawerOpened(drawerView: View?) {
                    super.onDrawerOpened(drawerView)
                    hideKeyboard()

                    if (Settings[C.S_SHOW_HELP].isNullOrEmpty()) {
                        MaterialShowcaseView.Builder(MainActivity.act)
                                .setTarget(findViewById(R.id.nav_header_main_text1))
                                .setContentText(R.string.overlay_help_message)
                                .setDismissText(R.string.ok)
                                .show()
                        Settings.put(C.S_SHOW_HELP, C.FALSE)
                    }

                    if (deviceTextView2.text == resources.getString(R.string.drawer_mail)) {
                        doAsync {
                            val mail = Pw.email
                            uiThread { deviceTextView2.text = mail }
                        }
                    }
                }

                // Change fragment on drawer close
                override fun onDrawerClosed(drawerView: View?) {
                    super.onDrawerClosed(drawerView)

                    val fragmentContainer = findViewById(R.id.container)

                    if (selectedFragment != null) {

                        fragmentContainer.animate().alpha(1f)
                        loadFragment(selectedFragment as BaseFragment)

                    } else if (selectedDevice != -1) {

                        fragmentContainer.animate().alpha(1f)
                        State.device = devices[selectedDevice]
                        WaitFilesAndSwitchDevice()
                    }
                    selectedFragment = null
                    selectedDevice = -1
                }
            }

            drawer.addDrawerListener(drawerToggle)
            // Calling sync state is necessary or hamburger icon wont show up
            drawerToggle.syncState()
        }
    }

    fun showDevices() {
        // Create device selection menu
        // For better user experience method redraws menu two times:
        // 1. with items from app folder (fast, not synced)
        // 2. with items from cloud (slow, synced)

        with (MainActivity.act!!) {
            /* Show devices from app folder */

            val deviceDirs = ArrayList(Arrays.asList(*File(Init.DEVICES_DIR).list()))
            Collections.sort(deviceDirs)

            navigationView.menu.clear()

            devices = ArrayList<Device>()

            var i = 0
            for (deviceDir in deviceDirs) {
                if (!deviceDir.matches("[A-Za-z].*_[0-9]{4}".toRegex()))
                    continue

                val device = Device(deviceDir)
                devices.add(device)

                navigationView.menu.add(0, Menu.FIRST + i, Menu.NONE, device.name)
                        .setIcon(R.drawable.ic_menu_device)
                i += 1
            }

            State.deviceMenuActive = true

            /* Show devices from cloud */

            // If not connected - don't redraw menu and exit
            if (!Pw.isConnected) {
                toast(R.string.not_connected_to_cloud)
                return@with
            }

            Thread(Runnable {
                try {
                    val deviceDirs = U.listDir("")

                    if (deviceDirs == null) {
                        toast(R.string.cant_get_devices_list)
                        return@Runnable
                    }

                    Collections.sort(deviceDirs)

                    runOnUiThread(Runnable {
                        // Workaround: if device menu is not shown don't redraw it
                        if (!State.deviceMenuActive) {
                            return@Runnable
                        }

                        navigationView.menu.clear()

                        devices = ArrayList<Device>()

                        var i = 0
                        for (deviceDir in deviceDirs) {
                            if (!File(deviceDir).name.matches("[A-Za-z].*_[0-9]{4}".toRegex()))
                                continue

                            val device = Device(File(deviceDir).name)
                            devices.add(device)

                            File(Init.DEVICES_DIR + deviceDir).mkdir()

                            navigationView.menu.add(0, Menu.FIRST + i, Menu.NONE, device.name)
                                    .setIcon(R.drawable.ic_menu_device)
                            i += 1
                        }
                    })
                } catch (e: Exception) {
                    L.d(TAG, "Device selector error: " + e)
                }
            }).start()
        }
    }

    fun showDefaultMenu() {
        with (MainActivity.act!!) {
            deviceTextView.text = State.device.name

            with(navigationView) {
                menu.clear()
                inflateMenu(R.menu.activity_main_drawer)
                menu.getItem(0).isChecked = true
            }

            rotateArrowDown()
            State.deviceMenuActive = false
        }
    }

    private fun rotateArrowUp() {
        val arrow = drawerHeader.findViewById(R.id.arrow) as Button
        arrow.animate().rotation(180f)
    }

    private fun rotateArrowDown() {
        val arrow = drawerHeader.findViewById(R.id.arrow) as Button
        arrow.animate().rotation(0f)
    }
}

