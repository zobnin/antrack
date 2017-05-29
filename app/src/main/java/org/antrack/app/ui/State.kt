package org.antrack.app.ui

import org.antrack.app.Init
import org.antrack.app.ui.fragments.BaseFragment
import org.antrack.app.ui.fragments.InfoFragment

object State {
    var deviceMenuActive = false
    var device = Device(Init.DEVICE_NAME_IMEI)
    var fragment: BaseFragment = InfoFragment()
    var menuItemTitle: String = ""
}
