package com.stignit.app.ui.nav

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberIncidentRepository
import com.stignit.app.data.sessionStore
import kotlinx.coroutines.launch
import com.stignit.app.ui.auth.AuthScreen
import com.stignit.app.ui.auth.MedicalInfoStepScreen
import com.stignit.app.ui.auth.OtpScreen
import com.stignit.app.ui.auth.RegisterScreen
import com.stignit.app.ui.components.BottomNavTab
import com.stignit.app.ui.contacts.ContactsScreen
import com.stignit.app.ui.home.HomeScreen
import com.stignit.app.ui.onboarding.OnboardingScreen
import com.stignit.app.ui.safety.DrillGuideDetailScreen
import com.stignit.app.ui.safety.SafetyScreen
import com.stignit.app.ui.settings.SettingsScreen
import com.stignit.app.ui.situationroom.SituationRoomScreen
import com.stignit.app.ui.welfare.WelfareCheckScreen
import com.stignit.app.ui.welfarehistory.WelfareHistoryScreen

/** Mirrors the route tree Lovable generated (src/routes/, one file per screen) 1:1. */
private object Routes {
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val Otp = "otp"
    const val Register = "register"
    const val MedicalInfoStep = "medical_info_step"
    const val Settings = "settings"
    const val Home = "home"
    const val WelfareCheck = "welfare_check"
    const val SituationRoom = "situation_room/{incidentId}"
    fun situationRoom(incidentId: String) = "situation_room/$incidentId"
    const val Contacts = "contacts"
    const val Safety = "safety"
    const val SafetyGuide = "safety_guide/{guideId}"
    fun safetyGuide(guideId: String) = "safety_guide/$guideId"
    const val WelfareHistory = "welfare_history"
}

/** Carries the in-progress phone-or-email/OTP between the auth steps without stuffing it in routes. */
private class PendingAuth {
    var identifier: String = ""
    var isEmail: Boolean = false
    var devCode: String? = null
    var resendInSec: Int = 30
}

@Composable
fun StignItNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = context.sessionStore()
    val pending = remember { PendingAuth() }
    val incidents = rememberIncidentRepository()
    val scope = rememberCoroutineScope()

    // Skip straight past auth if there's already a valid session.
    val start = if (session.isSignedIn && session.registrationComplete) Routes.Home else Routes.Onboarding

    fun goHome() = navController.navigate(Routes.Home) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }

    // There's no active incident to show unless one is actually open — check before navigating.
    fun openActiveSituationRoom() {
        scope.launch {
            when (val r = incidents.getActiveIncident()) {
                is ApiResult.Ok -> {
                    val active = r.value
                    if (active != null) {
                        navController.navigate(Routes.situationRoom(active.incidentId))
                    } else {
                        Toast.makeText(context, "No active incident right now", Toast.LENGTH_SHORT).show()
                    }
                }
                is ApiResult.Err -> Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Switches to a tab's root without piling copies onto the back stack — same
    // single-top/save-and-restore pattern as any standard bottom-nav setup.
    fun navigateToTabRoot(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Shared handler so tapping a BottomNav tab from any screen behaves the same way.
    fun onTabSelect(tab: BottomNavTab) {
        when (tab) {
            BottomNavTab.Home -> navigateToTabRoot(Routes.Home)
            BottomNavTab.Situation -> openActiveSituationRoom()
            BottomNavTab.Contacts -> navigateToTabRoot(Routes.Contacts)
            BottomNavTab.Safety -> navigateToTabRoot(Routes.Safety)
        }
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
                onCodeSent = { identifier, isEmail, devCode, resendInSec ->
                    pending.identifier = identifier
                    pending.isEmail = isEmail
                    pending.devCode = devCode
                    pending.resendInSec = resendInSec
                    navController.navigate(Routes.Otp)
                },
            )
        }
        composable(Routes.Otp) {
            OtpScreen(
                identifier = pending.identifier,
                isEmail = pending.isEmail,
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
                onRegistered = {
                    // Code is spent for phone/email flows too — drop Register so Back
                    // from the medical step (or Home) can't re-expose the form.
                    navController.navigate(Routes.MedicalInfoStep) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.MedicalInfoStep) {
            MedicalInfoStepScreen(onDone = { goHome() })
        }
        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Home) {
            HomeScreen(
                userName = session.fullName?.trim()?.substringBefore(' ') ?: "there",
                onOpenSituationRoom = { openActiveSituationRoom() },
                onOpenContacts = { navController.navigate(Routes.Contacts) },
                onOpenWelfareHistory = { navController.navigate(Routes.WelfareHistory) },
                onOpenSafety = { navController.navigate(Routes.Safety) },
                onSimulateImpact = { navController.navigate(Routes.WelfareCheck) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onSelectTab = ::onTabSelect,
            )
        }
        composable(Routes.WelfareCheck) {
            WelfareCheckScreen(
                onImOk = { goHome() },
                onGetHelp = { incidentId -> navController.navigate(Routes.situationRoom(incidentId)) },
            )
        }
        composable(
            Routes.SituationRoom,
            arguments = listOf(navArgument("incidentId") { type = NavType.StringType }),
        ) { backStackEntry ->
            SituationRoomScreen(
                incidentId = backStackEntry.arguments?.getString("incidentId").orEmpty(),
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
                onOpenGuide = { guideId -> navController.navigate(Routes.safetyGuide(guideId)) },
                currentTab = BottomNavTab.Safety,
                onSelectTab = ::onTabSelect,
            )
        }
        composable(
            Routes.SafetyGuide,
            arguments = listOf(navArgument("guideId") { type = NavType.StringType }),
        ) { backStackEntry ->
            DrillGuideDetailScreen(
                guideId = backStackEntry.arguments?.getString("guideId").orEmpty(),
                onBack = { navController.popBackStack() },
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