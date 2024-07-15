package navigation

sealed class Route(val route: String) {
    data object Welcome : Route(route = "/welcome")
    data object Setup : Route(route = "/setup")
}