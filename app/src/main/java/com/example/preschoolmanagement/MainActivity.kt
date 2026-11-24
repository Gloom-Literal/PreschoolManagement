package com.example.preschoolmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.preschoolmanagement.ui.screens.AttendanceScreen
import com.example.preschoolmanagement.ui.screens.ContractsScreen
import com.example.preschoolmanagement.ui.screens.GroupsScreen
import com.example.preschoolmanagement.ui.screens.MainMenuScreen
import com.example.preschoolmanagement.ui.screens.ParentsScreen
import com.example.preschoolmanagement.ui.screens.PaymentsScreen
import com.example.preschoolmanagement.ui.screens.ScheduleScreen
import com.example.preschoolmanagement.ui.screens.StudentsScreen
import com.example.preschoolmanagement.ui.screens.SubjectsScreen
import com.example.preschoolmanagement.ui.screens.TeachersScreen
import com.example.preschoolmanagement.ui.theme.PreschoolManagementTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreschoolManagementTheme {
                AppNavigation()
            }
        }
    }
}

/** Навигация по разделам приложения. */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    // Защита от белого экрана: на главном меню системный "назад" ничего не делает
    BackHandler(enabled = currentRoute?.destination?.route == "menu") { }

    // Возврат только при наличии предыдущего экрана
    fun goBack() {
        if (navController.previousBackStackEntry != null) navController.popBackStack()
    }

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MainMenuScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("students") { StudentsScreen(onBack = { goBack() }) }
        composable("parents") { ParentsScreen(onBack = { goBack() }) }
        composable("groups") { GroupsScreen(onBack = { goBack() }) }
        composable("teachers") { TeachersScreen(onBack = { goBack() }) }
        composable("subjects") { SubjectsScreen(onBack = { goBack() }) }
        composable("schedule") { ScheduleScreen(onBack = { goBack() }) }
        composable("contracts") { ContractsScreen(onBack = { goBack() }) }
        composable("payments") { PaymentsScreen(onBack = { goBack() }) }
        composable("attendance") { AttendanceScreen(onBack = { goBack() }) }
    }
}
