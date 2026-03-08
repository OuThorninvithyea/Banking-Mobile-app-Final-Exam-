package com.hustle.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hustle.bankapp.data.RemoteBankRepositoryImpl
import com.hustle.bankapp.data.TokenManager
import com.hustle.bankapp.data.TransactionType
import com.hustle.bankapp.data.api.AuthInterceptor
import com.hustle.bankapp.data.api.BankApiService
import com.hustle.bankapp.theme.HustleBankTheme
import com.hustle.bankapp.ui.auth.AuthViewModel
import com.hustle.bankapp.ui.auth.LoginScreen
import com.hustle.bankapp.ui.auth.RegisterScreen
import com.hustle.bankapp.ui.auth.RegisterViewModel
import com.hustle.bankapp.ui.dashboard.DashboardScreen
import com.hustle.bankapp.ui.dashboard.DashboardViewModel
import com.hustle.bankapp.ui.history.HistoryScreen
import com.hustle.bankapp.ui.history.HistoryViewModel
import com.hustle.bankapp.ui.profile.ProfileScreen
import com.hustle.bankapp.ui.profile.ProfileViewModel
import com.hustle.bankapp.ui.transaction.DepositWithdrawScreen
import com.hustle.bankapp.ui.transaction.DepositWithdrawViewModel
import com.hustle.bankapp.ui.cards.CardsScreen
import com.hustle.bankapp.ui.transfer.QRScannerScreen
import com.hustle.bankapp.ui.transfer.TransferScreen
import com.hustle.bankapp.ui.transfer.TransferViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { false }

        // ── Token & Networking Setup ──────────────────────────────────────
        val tokenManager = TokenManager(applicationContext)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(BankApiService::class.java)

        val repository = RemoteBankRepositoryImpl(apiService, tokenManager)
        val hasToken = !tokenManager.getToken().isNullOrEmpty()

        setContent {
            HustleBankTheme {
                val navController = rememberNavController()
                
                androidx.compose.material3.Scaffold(
                    bottomBar = { com.hustle.bankapp.ui.navigation.MainBottomNavBar(navController) },
                    containerColor = com.hustle.bankapp.theme.BackgroundBlack
                ) { innerPadding ->
                    // Floating nav bar: Box ignores innerPadding so the content scrolls underneath
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (hasToken) "dashboard" else "login"
                        ) {
                        // ── Login ──────────────────────────────────────────────────
                        composable("login") {
                            val vm: AuthViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        AuthViewModel(repository) as T
                                }
                            )
                            LoginScreen(
                                viewModel = vm,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // ── Register ──────────────────────────────────────────────
                        composable("register") {
                            val vm: RegisterViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        RegisterViewModel(repository) as T
                                }
                            )
                            RegisterScreen(
                                viewModel = vm,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }

                        // ── Dashboard ──────────────────────────────────────────────
                        composable("dashboard") {
                            val vm: DashboardViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        DashboardViewModel(repository) as T
                                }
                            )
                            DashboardScreen(
                                viewModel = vm,
                                onNavigateToTransfer = { navController.navigate("transfer") },
                                onNavigateToDeposit = { navController.navigate("deposit") },
                                onNavigateToWithdraw = { navController.navigate("withdraw") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToQrScanner = { navController.navigate("qr_scanner") }
                            )
                        }

                        // ── Cards ──────────────────────────────────────────────────
                        composable("cards") {
                            CardsScreen(
                                repository = repository,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Transfer ───────────────────────────────────────────────
                        composable("transfer") {
                            val vm: TransferViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        TransferViewModel(repository) as T
                                }
                            )
                            TransferScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── QR Scanner ────────────────────────────────────────────
                        composable("qr_scanner") {
                            QRScannerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Deposit ────────────────────────────────────────────────
                        composable("deposit") {
                            val vm: DepositWithdrawViewModel = viewModel(
                                key = "deposit",
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        DepositWithdrawViewModel(repository, TransactionType.DEPOSIT) as T
                                }
                            )
                            DepositWithdrawScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Withdraw ───────────────────────────────────────────────
                        composable("withdraw") {
                            val vm: DepositWithdrawViewModel = viewModel(
                                key = "withdraw",
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        DepositWithdrawViewModel(repository, TransactionType.WITHDRAW) as T
                                }
                            )
                            DepositWithdrawScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── History ────────────────────────────────────────────────
                        composable("history") {
                            val vm: HistoryViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        HistoryViewModel(repository) as T
                                }
                            )
                            HistoryScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Profile ────────────────────────────────────────────────
                        composable("profile") {
                            val vm: ProfileViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        ProfileViewModel(repository) as T
                                }
                            )
                            ProfileScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() },
                                onLogoutSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        } // composable closing
                    } // NavHost closing
                } // Box closing
            } // Scaffold closing
        } // HustleBankTheme closing
    } // setContent closing
} // MainActivity closing
