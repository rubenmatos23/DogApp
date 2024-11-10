package com.example.mydogproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.littlelemon.Destination
import com.example.littlelemon.Home
import com.example.littlelemon.MapView
import com.example.littlelemon.ProfileView
import com.example.mydogproject.ui.theme.MyDogProjectTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.*
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

data class userCoordinates(
    val latitude: Double,
    val longitude: Double
)

var listCoordinates = listOf(userCoordinates(6.5,6.5), userCoordinates(6.8,6.8))

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        //Initialize it where you need it
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var coordinates by mutableStateOf(Pair(0.00, 0.00))
        var except: Exception = Exception("dont know")
        super.onCreate(savedInstanceState)
        setContent {
            RequestLocationPermission({
            }, {

            }, {

            })
            MyDogProjectTheme {
                getCurrentLocation({ currentloc ->
                    coordinates = currentloc
                }, { excetion ->
                    except = excetion
                })
                val singapore: LatLng by mutableStateOf(
                    LatLng(
                        coordinates.first,
                        coordinates.second
                    )
                )
                println(singapore)
                val singaporeMarkerState =
                    rememberMarkerState(position = singapore).apply { position = singapore }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(singapore, 10f)
                }.apply { position = CameraPosition.fromLatLngZoom(singapore, 10f) }
                MenuNavigation(singaporeMarkerState, cameraPositionState)
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val navHostController = rememberNavController()
                Scaffold(
//                        topBar = {
//                            TopAppBar(drawerstate = drawerState, scope = scope)
//                        },
                    bottomBar = { MyBottomNavigation(navController = navHostController) }
                ) {
                    Box(modifier = Modifier.padding(it)) {
                        NavHost(navController = navHostController, startDestination = Home.route) {
                            composable(Home.route) {
                                HomeScreen(
                                    navHostController,
                                    singaporeMarkerState,
                                    cameraPositionState
                                )
                            }
                            composable(MapView.route) {
                                MapScreen(singaporeMarkerState, cameraPositionState)
                            }
                            composable(ProfileView.route) {
                                ProfileScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted; false otherwise.
     */
    private fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Retrieves the last known user location asynchronously.
     *
     * @param onGetLastLocationSuccess Callback function invoked when the location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetLastLocationFailed Callback function invoked when an error occurs while retrieving the location.
     *        It provides the Exception that occurred.
     */
    @SuppressLint("MissingPermission")
    private fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit
    ) {
        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the last known location
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // If location is not null, invoke the success callback with latitude and longitude
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }
                }
                .addOnFailureListener { exception ->
                    // If an error occurs, invoke the failure callback with the exception
                    onGetLastLocationFailed(exception)
                }
        }
    }

    /**
     * Retrieves the current user location asynchronously.
     *
     * @param onGetCurrentLocationSuccess Callback function invoked when the current location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetCurrentLocationFailed Callback function invoked when an error occurs while retrieving the current location.
     *        It provides the Exception that occurred.
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }
}

@Composable
fun MyBottomNavigation(navController: NavController){
    val destinationList = listOf<Destination>(
        Home,
        ProfileView
    )
    val selectedIndex = rememberSaveable {
        mutableStateOf(0)
    }
    BottomAppBar() {
        destinationList.forEachIndexed{ index, destination ->
            NavigationBarItem(
                label = { Text(text = destination.title)},
                icon = { Icon(imageVector = destination.icon, contentDescription = destination.title) },
                selected = index == selectedIndex.value,
                onClick = {
                    selectedIndex.value = index
                    navController.navigate(destinationList[index].route){
                        popUpTo(Home.route)
                        launchSingleTop = true
                    }
                })

        }
    }
}

@Composable
fun ProfileScreen(){
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "The title is here!!!!!!!!!!!"
        )
    }
}

@Composable
fun HomeScreen(navController: NavController, singaporeMarkerState: MarkerState, cameraPositionState:CameraPositionState){
    Column {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "The title is here"
        )
        Box(Modifier.fillMaxWidth()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
        Button(onClick = {navController.navigate(MapView.route)}, modifier = Modifier.fillMaxWidth()) {
            Text("Button here")
        }
    }
}

@Composable
fun MapScreen(singaporeMarkerState: MarkerState, cameraPositionState:CameraPositionState){
    Box(Modifier.fillMaxWidth()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = singaporeMarkerState,
                title = "Coordinate 1",
                snippet = "Market for coordinate 1"
            )
            displayMarker(listCoordinates)
        }
    }
}

@Composable
fun MenuNavigation(singaporeMarkerState: MarkerState, cameraPositionState:CameraPositionState){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home.route){
        composable(Home.route){
            HomeScreen(navController, singaporeMarkerState, cameraPositionState)
        }
        composable(MapView.route){
            MapScreen(singaporeMarkerState, cameraPositionState)
        }
    }
}

/**
 * Composable function to request location permissions and handle different scenarios.
 *
 * @param onPermissionGranted Callback to be executed when all requested permissions are granted.
 * @param onPermissionDenied Callback to be executed when any requested permission is denied.
 * @param onPermissionsRevoked Callback to be executed when previously granted permissions are revoked.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionsRevoked: () -> Unit
) {
    // Initialize the state for managing multiple location permissions.
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    // Use LaunchedEffect to handle permissions logic when the composition is launched.
    LaunchedEffect(key1 = permissionState) {
        // Check if all previously granted permissions are revoked.
        val allPermissionsRevoked =
            permissionState.permissions.size == permissionState.revokedPermissions.size

        // Filter permissions that need to be requested.
        val permissionsToRequest = permissionState.permissions.filter {
            !it.status.isGranted
        }

        // If there are permissions to request, launch the permission request.
        if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

        // Execute callbacks based on permission status.
        if (allPermissionsRevoked) {
            onPermissionsRevoked()
        } else {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}

@Composable
fun displayMarker(coordinates: List<userCoordinates>){
    if (coordinates.isEmpty()) {
        println("Noo coordinates")
    }else{
        for (data in coordinates){
            Marker(
                state = MarkerState(position = LatLng(data.latitude, data.longitude)),
                title = "Coordinate 1",
                snippet = "Market for coordinate 1"
            )
        }
    }
}
