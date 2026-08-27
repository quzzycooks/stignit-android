package com.stignit.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stignit.app.data.sessionStore
import com.stignit.app.ui.auth.AuthScreen
import com.stignit.app.ui.auth.OtpScreen
import com.stignit.app.ui.auth.RegisterScreen
import com.stignit.app.ui.components.BottomNavTab
import com.stignit.app.ui.contacts.ContactsScreen
import com.stignit.app.ui.home.HomeScreen
import com.stignit.app.ui.onboarding.OnboardingScreen
import com.stignit.app.ui.safety.SafetyScreen
import com.stignit.app.ui.situationroom.SituationRoomScreen
import com.stignit.app.ui.welfare.WelfareCheckScreen
import com.stignit.app.ui.welfarehistory.WelfareHistoryScreen

/** Mirrors the route tree Lovable generated (src/routes/, one file per screen) 1:1. */
private object Routes {
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val Otp = "otp"
    const val Register = "register"
    const val Home = "home"
    const val WelfareCheck = "welfare_check"
    const val SituationRoom = "situation_room"
    const val Contacts = "contacts"
    const val Safety = "safety"
    const val WelfareHistory = "welfare_history"
}

/** Carries the in-progress phone/OTP between the auth steps without stuffing it in routes. */
private class PendingAuth {
    var phone: String = ""
    var devCode: String? = null
    var resendInSec: Int = 30
}

@Composable
fun StignItNavHost() {
    val navController = rememberNavController()
    val session = LocalContext.current.sessionStore()
    val pending = remember { PendingAuth() }

    // Skip straight past auth if there's already a valid session.
    val start = if (session.isSignedIn && session.registrationComplete) Routes.Home else Routes.Onboarding

    fun goHome() = navController.navigate(Routes.Home) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }

    // Shared handler so tapping a BottomNav tab from any screen behaves the same way.
    fun onTabSelect(tab: BottomNavTab) {
        val route = when (tab) {
            BottomNavTab.Home -> Routes.Home
            BottomNavTab.Situation -> Routes.SituationRoom
            BottomNavTab.Contacts -> Routes.Contacts
            BottomNavTab.Safety -> Routes.Safety
        }
        navController.navigate(route)
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onSkip = { navController.navigate(Routes.Auth) },
                onDone = { navController.navigate(Routes.Auth) },
            )
        }
        composable(Routes.Auth) {
            AuthScreen(
                onCodeSent = { phone, devCode, resendInSec ->
                    pending.phone = phone
                    pending.devCode = devCode
                    pending.resendInSec = resendInSec
                    navController.navigate(Routes.Otp)
                },
            )
        }
        composable(Routes.Otp) {
            OtpScreen(
                phone = pending.phone,
                initialDevCode = pending.devCode,
                initialResendInSec = pending.resendInSec,
                onVerified = { registrationComplete ->
                    if (registrationComplete) {
                        goHome()
                    } else {
                        // Code is spent — drop Otp so Back from Register doesn't re-expose it.
                        navController.navigate(Routes.Register) {
                            popUpTo(Routes.Otp) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onRegistered = { goHome() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenSituationRoom = { navController.navigate(Routes.SituationRoom) },
                onOpenContacts = { navController.navigate(Routes.Contacts) },
                onOpenWelfareHistory = { navController.navigate(Routes.WelfareHistory) },
                onOpenSafety = { navController.navigate(Routes.Safety) },
                onSimulateImpact = { navController.navigate(Routes.WelfareCheck) },
                onSelectTab = ::onTabSelect,
            )
        }
        composable(Routes.WelfareCheck) {
            WelfareCheckScreen(
                onImOk = { goHome() },
                onGetHelp = { navController.navigate(Routes.SituationRoom) },
            )
        }
        composable(Routes.SituationRoom) {
            SituationRoomScreen(
                onBack = { navController.popBackStack() },
                onMarkSafe = { goHome() },
            )
        }
        composable(Routes.Contacts) {
            ContactsScreen(
                onBack = { navController.popBackStack() },
                currentTab = BottomNavTab.Contacts,
                onSelectTab = ::onTabSelect,
            )
        }
        composable(Routes.Safety) {
            SafetyScreen(
                onBack = { navController.popBackStack() },
                onStartDrill = { navController.navigate(Routes.WelfareCheck) },
                currentTab = BottomNavTab.Safety,
                onSelectTab = ::onTabSelect,
            )
        }
        composable(Routes.WelfareHistory) {
            WelfareHistoryScreen(
                onBack = { navController.popBackStack() },
                currentTab = BottomNavTab.Home,
                onSelectTab = ::onTabSelect,
            )
        }
    }
}