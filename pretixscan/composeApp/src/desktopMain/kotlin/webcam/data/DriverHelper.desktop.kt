package webcam.data

import com.github.sarxos.webcam.Webcam
import com.github.eduramiba.webcamcapture.drivers.NativeDriver

actual fun configureWebCam() {
    Webcam.setDriver(NativeDriver())
}