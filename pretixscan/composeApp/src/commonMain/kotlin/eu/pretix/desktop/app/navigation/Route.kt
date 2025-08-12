package eu.pretix.desktop.app.navigation

sealed class Route(val route: String) {
    data object Welcome : Route(route = "/eu/pretix/scan/welcome")
    data object Setup : Route(route = "/eu/pretix/scan/setup")
    data object Main : Route(route = "/eu/pretix/scan/main")
    data object Settings : Route(route = "/eu/pretix/scan/settings")

    data object EventStats : Route(route = "/eu/pretix/scan/status")
}