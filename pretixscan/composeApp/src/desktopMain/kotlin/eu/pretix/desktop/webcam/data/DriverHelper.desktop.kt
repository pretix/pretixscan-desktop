package eu.pretix.desktop.webcam.data

import com.github.eduramiba.webcamcapture.drivers.NativeDriver
import com.github.sarxos.webcam.Webcam

actual fun configureWebCam() {
    Webcam.setDriver(NativeDriver())
}