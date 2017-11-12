package eu.pretix.pretixdesk.ui.style;

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import tornadofx.*

val STYLE_BACKGROUND_COLOR = "#f6f1f9"
val STYLE_INPUT_BACKGROUND_COLOR = "#ffffff"
val STYLE_SHADOW_COLOR_COLOR = "#4E315D"
val STYLE_INPUT_PROMPT_COLOR = "#9D91A3"
val STYLE_INPUT_PROMPT_FOCUSED_COLOR = "#C5BDC9"
val STYLE_CARD_BACKGROUND_COLOR = "#ffffff"
val STYLE_STATE_VALID_COLOR = "#2E7D32"
val STYLE_STATE_TEXT_COLOR = "#FFFFFF"
val STYLE_PRIMARY_DARK_COLOR = "#3b1c4a"
val STYLE_TOOLBAR_TEXT_COLOR = "#ffffff"

class MainStyleSheet : Stylesheet() {

    companion object {
        val mainSearchField by cssclass()
        val card by cssclass()
        val cardBody by cssclass()
        val resultCard by cssclass()
        val resultHolder by cssclass()
        val cardHeaderValid by cssclass()
        val cardHeaderLabel by cssclass()
        val toolBar by cssclass()
    }

    init {
        textInput {
            and(focused) {
                promptTextFill = c(STYLE_INPUT_PROMPT_FOCUSED_COLOR)
                effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.2))
            }

            promptTextFill = c(STYLE_INPUT_PROMPT_COLOR)
            backgroundColor += c(STYLE_INPUT_BACKGROUND_COLOR)
            borderWidth += tornadofx.box(0.px)
            effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.1))
            borderRadius += tornadofx.box(4.px)
        }

        mainSearchField {
            fontSize = 24.px
            minWidth = 480.px
            maxWidth = 480.px
        }

        resultHolder {
            minWidth = 480.px
            maxWidth = 480.px
            minHeight = 200.px
        }

        card {
            backgroundColor += c(STYLE_CARD_BACKGROUND_COLOR)
            effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.1))
            backgroundRadius += tornadofx.box(4.px)
        }

        cardHeaderLabel {
            textFill = c(STYLE_STATE_TEXT_COLOR)
            fontSize = 24.px
        }

        cardHeaderValid {
            backgroundColor += c(STYLE_STATE_VALID_COLOR)
            backgroundRadius += tornadofx.box(4.px, 4.px, 0.px, 0.px)
            alignment = Pos.CENTER
        }

        cardBody {
            padding = box(15.px)
        }

        select("JFXSpinner .arc") {
            stroke = c(STYLE_PRIMARY_DARK_COLOR)
            strokeWidth = 4.px
        }

        toolBar {
            alignment = Pos.CENTER
            backgroundColor += c(STYLE_PRIMARY_DARK_COLOR)
            minHeight = 48.px
            maxHeight = 48.px

            select("JFXToggleButton") {
                select("LabeledText") {
                    fill = c(STYLE_TOOLBAR_TEXT_COLOR)
                }
            }
            select("JFXButton") {
                select("LabeledText") {
                    fill = c(STYLE_TOOLBAR_TEXT_COLOR)
                }
            }
        }
    }
}