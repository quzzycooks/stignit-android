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
import androidx.compose.runtime.LaunchedEffect
import com.stignit.app.data.AccountRole
import com.stignit.app.data.ApiResult
import com.stignit.app.data.rememberIncidentRepository
import com.stignit.app.data.sessionStore
import com.stignit.app.detection.CrashSignal
import com.stignit.app.detection.DetectionConfidence
import com.stignit.app.notifications.ProximityAlertSignal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.stignit.app.ui.auth.AuthScreen
import com.stignit.app.ui.auth.MedicalInfoStepScreen
import com.stignit.app.ui.auth.OtpScreen
import com.stignit.app.ui.auth.RegisterScreen
import com.stignit.app.ui.auth.RoleSelectScreen
import com.stignit.app.ui.components.BottomNavTab
import com.stignit.app.ui.contacts.ContactsScreen
import com.stignit.app.ui.home.HomeScreen
import com.stignit.app.ui.incident.DeclareRoleScreen
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
    const val RoleSelect = "role_select"
    const val Register = "register"
    const val MedicalInfoStep = "medical_info_step"
    const val Settings = "settings"
    const val Home = "home"
    private const val WelfareCheckBase = "welfare_check"
    const val WelfareCheck = "$WelfareCheckBase?real={real}"
    fun welfareCheckDrill() = WelfareCheckBase
    fun welfareCheckReal() = "$WelfareCheckBase?real=true"
    const val SituationRoom = "situation_room/{incidentId}"
    fun situationRoom(incidentId: String) = "situation_room/$incidentId"
    const val DeclareRole = "declare_role/{incidentId}"
    fun declareRole(incidentId: String) = "declare_role/$incidentId"
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
    var role: AccountRole = AccountRole.CIVILIAN
}

@Composable
fun StignItNavHost(pendingIncidentId: String? = null) {
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

    // CrashDetectionService has no NavController of its own; it signals here via
    // CrashSignal, and this collects only while the NavHost is actually composed
    // (i.e. the app is in the foreground).
    LaunchedEffect(Unit) {
        CrashSignal.events.collectLatest { confidence ->
            if (confidence != DetectionConfidence.LOW) {
                navController.navigate(Routes.welfareCheckReal())
            }
        }
    }

    // StignItMessagingService has no NavController of its own either; a warm
    // (foregrounded) app picks up a proximity alert here instead of via the
    // notification's PendingIntent — see MainActivity for the cold-start path.
    LaunchedEffect(Unit) {
        ProximityAlertSignal.events.collectLatest { incidentId ->
            navController.navigate(Routes.declareRole(incidentId))
        }
    }

    // Cold start / warm-but-backgrounded relaunch from a notification tap — see
    // MainActivity.incidentIdFrom. Keyed on the value itself so a genuinely new
    // tap (a different or repeated incidentId push from MainActivity) re-fires,
    // but recomposition alone does not.
    LaunchedEffect(pendingIncidentId) {
        pendingIncidentId?.let { navController.navigate(Routes.declareRole(it)) }
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
                        // Code is spent — drop Otp so Back from RoleSelect doesn't re-expose it.
                        navController.navigate(Routes.RoleSelect) {
                            popUpTo(Routes.Otp) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.RoleSelect) {
            RoleSelectScreen(
                onRoleSelected = { role ->
                    pending.role = role
                    navController.navigate(Routes.Register)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                role = pending.role,
                onRegistered = {
                    // Code is spent for phone/email flows too — drop Register so Back
                    // from the medical step (or Home) can't re-expose the form.
                    if (pending.role == AccountRole.CIVILIAN) {
                        navController.navigate(Routes.MedicalInfoStep) {
                            popUpTo(Routes.Register) { inclusive = true }
                        }
                    } else {
                        // Medical Personnel / Driver-Responder submitted their profile
                        // fields as part of Register itself — no civilian-only medical
                        // info step to show.
                        goHome()
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
            // Placeholder routing: every role lands on the same HomeScreen for now.
            // The branch exists so Medical Personnel / Driver-Responder can get their
            // own Home destination later without touching the civilian path.
            when (session.role) {
                AccountRole.CIVILIAN, AccountRole.MEDICAL_PERSONNEL, AccountRole.DRIVER_RESPONDER ->
                    HomeScreen(
                        userName = session.fullName?.trim()?.substringBefore(' ') ?: "there",
                        onOpenSituationRoom = { openActiveSituationRoom() },
                        onOpenContacts = { navController.navigate(Routes.Contacts) },
                        onOpenWelfareHistory = { navController.navigate(Routes.WelfareHistory) },
                        onOpenSafety = { navController.navigate(Routes.Safety) },
                        onSimulateImpact = { navController.navigate(Routes.welfareCheckDrill()) },
                        onOpenSettings = { navController.navigate(Routes.Settings) },
                        onSelectTab = ::onTabSelect,
                    )
            }
        }
        composable(
            Routes.WelfareCheck,
            arguments = listOf(navArgument("real") { type = NavType.BoolType; defaultValue = false }),
        ) { backStackEntry ->
            val isReal = backStackEntry.arguments?.getBoolean("real") ?: false
            WelfareCheckScreen(
                onImOk = { goHome() },
                onGetHelp = { incidentId -> navController.navigate(Routes.situationRoom(incidentId)) },
                isDrill = !isReal,
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
        composable(
            Routes.DeclareRole,
            arguments = listOf(navArgument("incidentId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId").orEmpty()
            DeclareRoleScreen(
                onRoleDeclared = { role ->
                    scope.launch {
                        incidents.declareRole(incidentId, role)
                        navController.navigate(Routes.situationRoom(incidentId)) {
                            popUpTo(Routes.declareRole(incidentId)) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
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
                onStartDrill = { navController.navigate(Routes.welfareCheckDrill()) },
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