package eu.bsinfo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomMac

actual val Dispatchers.IO: CoroutineDispatcher get() = IO // This returns the kmp extension Dispatchers.IO

actual val isMobile: Boolean =
    UIDevice.currentDevice().userInterfaceIdiom() != UIUserInterfaceIdiomMac
