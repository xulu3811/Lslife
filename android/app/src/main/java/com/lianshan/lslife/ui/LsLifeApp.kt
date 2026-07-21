package com.lianshan.lslife.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lianshan.lslife.R
import com.lianshan.lslife.feature.auth.ForgotPasswordScreen
import com.lianshan.lslife.feature.auth.LoginScreen
import com.lianshan.lslife.feature.cart.CartScreen
import com.lianshan.lslife.feature.home.HomeScreen
import com.lianshan.lslife.feature.search.SearchScreen
import com.lianshan.lslife.feature.merchant.MerchantDetailScreen
import com.lianshan.lslife.feature.orders.OrderListScreen
import com.lianshan.lslife.feature.orders.OrderTrackScreen
import com.lianshan.lslife.feature.profile.AddressScreen
import com.lianshan.lslife.feature.profile.CropScreen
import com.lianshan.lslife.feature.profile.EditProfileScreen
import com.lianshan.lslife.feature.profile.MembershipScreen
import com.lianshan.lslife.feature.chat.ChatSessionListScreen
import com.lianshan.lslife.feature.chat.ChatScreen
import com.lianshan.lslife.feature.profile.PersonalInfoScreen
import com.lianshan.lslife.feature.profile.ProfileScreen
import com.lianshan.lslife.feature.profile.RealNameScreen
import com.lianshan.lslife.feature.publish.PublishScreen
import com.lianshan.lslife.feature.settings.AboutScreen
import com.lianshan.lslife.feature.settings.PrivacyScreen
import com.lianshan.lslife.feature.settings.SettingsScreen
import com.lianshan.lslife.ui.navigation.Routes

private data class Tab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val tabs = listOf(
    Tab(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    Tab(Routes.MESSAGE_LIST, R.string.nav_messages, Icons.Filled.Receipt, Icons.Outlined.Receipt), // TODO: update icon and string
    Tab(Routes.PUBLISH, R.string.nav_publish, Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
    Tab(Routes.CART, R.string.nav_cart, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    Tab(Routes.PROFILE, R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun LsLifeApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val mainRoutes = tabs.map { it.route } + Routes.PUBLISH
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Box {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                    ) {
                        tabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes),
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) Routes.HOME else Routes.LOGIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenMerchant = { navController.navigate(Routes.merchant(it)) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ORDERS) {
                OrderListScreen(onTrack = { navController.navigate(Routes.orderTrack(it)) })
            }
            composable(Routes.PUBLISH) { PublishScreen() }
            composable(Routes.CART) {
                CartScreen(onOpenMerchant = { navController.navigate(Routes.merchant(it)) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenPersonalInfo = { navController.navigate(Routes.PERSONAL_INFO) },
                    onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                    onOpenAddress = { navController.navigate(Routes.ADDRESS_LIST) },
                    onOpenMessage = { navController.navigate(Routes.MESSAGE_LIST) },
                    onOpenRealName = { navController.navigate(Routes.REAL_NAME_AUTH) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) }
                    },
                )
            }
            composable(Routes.PERSONAL_INFO) {
                PersonalInfoScreen(
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                )
            }
            composable(Routes.EDIT_PROFILE) { entry ->
                val croppedAvatar = entry.savedStateHandle.get<String>("cropped_avatar")
                EditProfileScreen(
                    croppedAvatar = croppedAvatar,
                    onBack = { navController.popBackStack() },
                    onNavigateToCrop = { navController.navigate(Routes.cropAvatar()) }
                )
            }
            composable(Routes.CROP_AVATAR) { entry ->
                CropScreen(
                    uriString = "avatar_temp.jpg",
                    onBack = { navController.popBackStack() },
                    onCropped = { uploadedUrl ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("cropped_avatar", uploadedUrl)
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.MEMBERSHIP) {
                MembershipScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADDRESS_LIST) { AddressScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MESSAGE_LIST) { 
                ChatSessionListScreen(
                    onNavigateToChat = { sessionId, targetId, targetName ->
                        navController.navigate(Routes.chat(sessionId, targetId, targetName))
                    }
                )
            }
            composable(Routes.CHAT) { entry ->
                ChatScreen(
                    sessionId = entry.arguments?.getString("sessionId").orEmpty(),
                    targetUserId = entry.arguments?.getString("targetUserId").orEmpty(),
                    targetName = entry.arguments?.getString("targetName").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REAL_NAME_AUTH) { RealNameScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                )
            }
            composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.MERCHANT) { entry ->
                MerchantDetailScreen(
                    merchantId = entry.arguments?.getString("merchantId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onCheckedOut = { orderId -> navController.navigate(Routes.orderTrack(orderId)) },
                )
            }
            composable(Routes.ORDER_TRACK) { entry ->
                OrderTrackScreen(
                    orderId = entry.arguments?.getString("orderId").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
