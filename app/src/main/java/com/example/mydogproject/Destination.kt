package com.example.littlelemon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

interface Destination {
    val route: String
    val icon: ImageVector
    val title: String
}

object Home: Destination{
    override val route: String = "Home"
    override val icon: ImageVector = Icons.Filled.Home
    override val title: String = "Home"
}

object MapView: Destination{
    override val route: String = "MapView"
    override val icon: ImageVector = Icons.Filled.Home
    override val title: String = "MapView"
}

object ProfileView: Destination{
    override val route: String = "Profile"
    override val icon: ImageVector = Icons.Filled.Home
    override val title: String = "Profile View"
}
