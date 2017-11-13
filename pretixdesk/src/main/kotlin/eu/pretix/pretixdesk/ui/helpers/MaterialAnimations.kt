package eu.pretix.pretixdesk.ui.helpers

import javafx.animation.Interpolator
import javafx.util.Duration

class MaterialDuration {
    // see https://material.io/guidelines/motion/duration-easing.html#duration-easing-common-durations
    companion object {
        val TYPICAL = Duration.millis(300.0)!!
        val ENTER = Duration.millis(225.0)!!
        val EXIT = Duration.millis(195.0)!!
    }
}

class MaterialInterpolator {
    // see https://material.io/guidelines/motion/duration-easing.html#duration-easing-natural-easing-curves
    companion object {
        val STANDARD = Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0)!!
        val ENTER = Interpolator.SPLINE(0.0, 0.0, 0.2, 1.0)!!
        val EXIT = Interpolator.SPLINE(0.4, 0.0, 1.0, 1.0)!!
        val SHARP = Interpolator.SPLINE(0.4, 0.0, 0.6, 1.0)!!
    }
}