package eu.pretix.pretixscan.desktop.ui.helpers

import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.util.Duration
import tornadofx.ViewTransition
import tornadofx.and
import tornadofx.move
import tornadofx.point

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

class MaterialSlide(val direction: Direction = Direction.LEFT) : ViewTransition.ReversibleViewTransition<MaterialSlide>() {
    override fun create(current: Node, replacement: Node, stack: StackPane): Animation {
        val bounds = current.boundsInLocal
        val destination = when (direction) {
            Direction.UP -> point(0, -bounds.height)
            Direction.RIGHT -> point(bounds.width, 0)
            Direction.DOWN -> point(0, bounds.height)
            Direction.LEFT -> point(-bounds.width, 0)
        }
        return current.move(MaterialDuration.ENTER, destination, play = false, easing = MaterialInterpolator.ENTER)
                .and(replacement.move(MaterialDuration.ENTER, destination.multiply(-1.0), easing=MaterialInterpolator.ENTER, reversed = true, play = false))
    }

    override fun stack(current: Node, replacement: Node) = super.stack(replacement, current)

    override fun onComplete(removed: Node, replacement: Node) {
        removed.translateX = 0.0
        removed.translateY = 0.0
    }

    override fun reversed() = MaterialSlide(direction.reversed()).apply { setup = this@MaterialSlide.setup }
}