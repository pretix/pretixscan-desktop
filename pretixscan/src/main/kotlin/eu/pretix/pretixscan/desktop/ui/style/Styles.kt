package eu.pretix.pretixscan.desktop.ui.style

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

val STYLE_BACKGROUND_COLOR = "#f6f1f9"
val STYLE_INPUT_BACKGROUND_COLOR = "#ffffff"
val STYLE_SHADOW_COLOR_COLOR = "#4E315D"
val STYLE_INPUT_PROMPT_COLOR = "#9D91A3"
val STYLE_INPUT_PROMPT_FOCUSED_COLOR = "#C5BDC9"
val STYLE_CARD_BACKGROUND_COLOR = "#ffffff"
val STYLE_STATE_VALID_COLOR = "#2E7D32"
val STYLE_STATE_REPEAT_COLOR = "#f0ad4e"
val STYLE_STATE_ERROR_COLOR = "#d9534f"
val STYLE_STATE_TEXT_COLOR = "#FFFFFF"
val STYLE_PRIMARY_DARK_COLOR = "#3b1c4a"
val STYLE_TOOLBAR_TEXT_COLOR = "#ffffff"
val STYLE_ATTENTION_COLOR = "#3B1C4A"
val STYLE_ATTENTION_ALTERNATE_COLOR = "#ffee58"
val STYLE_TEXT_COLOR_MUTED = "#909090"

class MainStyleSheet : Stylesheet() {

    companion object {
        val mainSearchField by cssclass()
        val card by cssclass()
        val cardBody by cssclass()
        val resultHolder by cssclass()
        val cardHeaderValid by cssclass()
        val cardHeaderRepeat by cssclass()
        val cardHeaderError by cssclass()
        val cardHeaderErrorNoMessage by cssclass()
        val cardHeaderLabel by cssclass()
        val cardFooterAttention by cssclass()
        val cardFooterAttentionBlink by cssclass()
        val searchItemProduct by cssclass()
        val searchItemOrderCode by cssclass()
        val searchItemAttendeeName by cssclass()
        val searchItemStatusRedeemed by cssclass()
        val searchItemStatusUnpaid by cssclass()
        val searchItemStatusValid by cssclass()
        val toolBar by cssclass()
        val eventInfoList by cssclass()
        val eventInfoItem by cssclass()
        val eventInfoItemHeader by cssclass()
        val eventInfoItemBody by cssclass()
        val eventInfoHeader by cssclass()
        val eventInfoItemNumber by cssclass()
        val eventSettingsCard by cssclass()
        val setupScreen by cssclass()
        val questionsForm by cssclass()
    }

    init {
        label {
            fontFamily = "Roboto"
            fontSize = 13.px
            graphicTextGap = 16.px
        }

        textInput {
            and(focused) {
                promptTextFill = c(STYLE_INPUT_PROMPT_FOCUSED_COLOR)
                effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.2))
            }

            promptTextFill = c(STYLE_INPUT_PROMPT_COLOR)
            backgroundColor += c(STYLE_INPUT_BACKGROUND_COLOR)
            borderWidth += box(0.px)
            effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.1))
            borderRadius += box(4.px)
        }

        mainSearchField {
            fontSize = 24.px
            minWidth = 480.px
            maxWidth = 480.px
            fontFamily = "Roboto"
        }

        resultHolder {
            minWidth = 480.px
            maxWidth = 480.px
            minHeight = 200.px
        }

        eventInfoList {
            minWidth = 500.px
            maxWidth = 500.px
            minHeight = 250.px
            backgroundColor += c(STYLE_BACKGROUND_COLOR)

            cell {
                backgroundColor += c(STYLE_BACKGROUND_COLOR)
                padding = box(10.px, 10.px, 10.px, 10.px)
                alignment = Pos.CENTER
            }
        }

        card {
            backgroundColor += c(STYLE_CARD_BACKGROUND_COLOR)
            effect = DropShadow(5.0, 0.0, 2.0, c(STYLE_SHADOW_COLOR_COLOR, 0.1))
            backgroundRadius += box(4.px)
        }

        cardHeaderLabel {
            textFill = c(STYLE_STATE_TEXT_COLOR)
            fontSize = 24.px
        }

        select("JFXListView") {
            borderWidth += box(0.px)
            borderRadius += box(4.px)
            backgroundColor += c(STYLE_CARD_BACKGROUND_COLOR)
        }

        cardHeaderValid {
            backgroundColor += c(STYLE_STATE_VALID_COLOR)
            backgroundRadius += box(4.px, 4.px, 0.px, 0.px)
            alignment = Pos.CENTER
        }
        cardHeaderError {
            backgroundColor += c(STYLE_STATE_ERROR_COLOR)
            backgroundRadius += box(4.px, 4.px, 0.px, 0.px)
            alignment = Pos.CENTER
        }
        cardHeaderErrorNoMessage {
            backgroundColor += c(STYLE_STATE_ERROR_COLOR)
            backgroundRadius += box(4.px, 4.px, 4.px, 4.px)
            alignment = Pos.CENTER
        }
        cardHeaderRepeat {
            backgroundColor += c(STYLE_STATE_REPEAT_COLOR)
            backgroundRadius += box(4.px, 4.px, 0.px, 0.px)
            alignment = Pos.CENTER
        }
        cardFooterAttention {
            backgroundColor += c(STYLE_ATTENTION_COLOR)
            backgroundRadius += box(0.px, 0.px, 4.px, 4.px)
            alignment = Pos.CENTER
            label {
                textFill = c("#ffffff")
            }
        }
        cardFooterAttentionBlink {
            backgroundColor += c(STYLE_ATTENTION_ALTERNATE_COLOR)
            backgroundRadius += box(0.px, 0.px, 4.px, 4.px)
            alignment = Pos.CENTER
            label {
                textFill = c(STYLE_ATTENTION_COLOR)
            }
        }

        cardBody {
            padding = box(15.px)
            label {
                fontSize = 18.px
            }
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
                    fontFamily = "Roboto"
                }
            }
            select("JFXButton") {
                select("LabeledText") {
                    fill = c(STYLE_TOOLBAR_TEXT_COLOR)
                }
            }
            label {
                textFill = c(STYLE_TOOLBAR_TEXT_COLOR)
            }
        }

        searchItemAttendeeName {
            fontSize = 18.px
        }
        searchItemOrderCode {
            fontSize = 18.px
            fontWeight = FontWeight.BOLD
        }
        searchItemProduct {
            fontSize = 18.px
        }
        searchItemStatusUnpaid {
            fontSize = 18.px
            textFill = c(STYLE_STATE_ERROR_COLOR)
            fontWeight = FontWeight.BOLD
        }
        searchItemStatusValid{
            fontSize = 18.px
            textFill = c(STYLE_STATE_VALID_COLOR)
            fontWeight = FontWeight.BOLD
        }
        searchItemStatusRedeemed {
            fontSize = 18.px
            textFill = c(STYLE_STATE_REPEAT_COLOR)
            fontWeight = FontWeight.BOLD
        }

        eventInfoItemHeader {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }
        eventInfoItemBody {
            fontSize = 14.px
            textFill = c(STYLE_TEXT_COLOR_MUTED)
        }
        eventInfoItem {
            minWidth = 460.px
            maxWidth = 460.px
        }
        eventInfoHeader {
            minWidth = 460.px
            maxWidth = 460.px
            backgroundColor += c(STYLE_PRIMARY_DARK_COLOR)
            label {
                textFill = c(STYLE_TOOLBAR_TEXT_COLOR)
            }
        }
        eventInfoItemNumber {
            textAlignment = TextAlignment.RIGHT
            alignment = Pos.CENTER_RIGHT
            minWidth = 60.px
        }
        eventSettingsCard {
            minWidth = 460.px
            maxWidth = 460.px
        }

        setupScreen {
            minWidth = 460.px
            maxWidth = 460.px

            label {
                textFill = c(STYLE_TOOLBAR_TEXT_COLOR)
            }
        }

        questionsForm {
            textField {
                backgroundColor += c("#000000", 0.0)
            }
            textArea {
                backgroundColor += c("#000000", 0.0)
            }
            label {
                endMargin = 5.px
            }
        }
    }
}