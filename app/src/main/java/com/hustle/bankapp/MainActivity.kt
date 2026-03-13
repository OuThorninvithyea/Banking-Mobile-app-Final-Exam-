package com.hustle.bankapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.hustle.bankapp.ui.transaction.ReceiveScreen
import com.hustle.bankapp.ui.cards.CardsScreen
import com.hustle.bankapp.ui.cards.CardsViewModel
import com.hustle.bankapp.ui.transfer.QRScannerScreen
import com.hustle.bankapp.ui.transfer.TransferAmountScreen
import com.hustle.bankapp.ui.transfer.TransferSelectionScreen
import com.hustle.bankapp.ui.transfer.TransferSelectionViewModel
import com.hustle.bankapp.ui.transfer.TransferViewModel
import com.hustle.bankapp.ui.accounts.AccountsScreen
import com.hustle.bankapp.ui.accounts.AccountsViewModel
import com.hustle.bankapp.ui.favorites.FavoritesScreen
import com.hustle.bankapp.ui.favorites.FavoritesViewModel
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

                LaunchedEffect(Unit) {
                    tokenManager.sessionExpired.collect {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                androidx.compose.material3.Scaffold(
                    bottomBar = { com.hustle.bankapp.ui.navigation.MainBottomNavBar(navController) },
                    containerColor = com.hustle.bankapp.theme.BackgroundBlack
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = if (hasToken) "dashboard" else "login",
                            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
                            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 } },
                            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
                            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 } }
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
                                onNavigateToDeposit = { navController.navigate("deposit") },
                                onNavigateToWithdraw = { navController.navigate("withdraw") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToQrScanner = { navController.navigate("qr_scanner") },
                                onNavigateToCards = { navController.navigate("cards") },
                                onNavigateToAccounts = { navController.navigate("accounts") },
                                onNavigateToFavorites = { navController.navigate("favorites") },
                                onNavigateToTransfer = { navController.navigate("transfer_selection") },
                                onNavigateToReceive = { navController.navigate("receive") }
                            )
                        }

                        // ── Cards ──────────────────────────────────────────────────
                        composable("cards") {
                            val vm: CardsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        CardsViewModel(repository) as T
                                }
                            )
                            CardsScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Transfer Selection ──────────────────────────────────────
                        composable("transfer_selection") {
                            val vm: TransferSelectionViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        TransferSelectionViewModel(repository) as T
                                }
                            )
                            TransferSelectionScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToTransfer = { recipient ->
                                    navController.navigate("transfer_amount?recipient=$recipient")
                                }
                            )
                        }

                        // ── Transfer Amount ──────────────────────────────────────────
                        composable(
                            route = "transfer_amount?recipient={recipient}",
                            arguments = listOf(navArgument("recipient") {
                                type = NavType.StringType
                                defaultValue = ""
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val initialRecipient = backStackEntry.arguments?.getString("recipient") ?: ""
                            val vm: TransferViewModel = viewModel(
                                key = "transfer_$initialRecipient",
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        TransferViewModel(repository, initialRecipient) as T
                                }
                            )
                            TransferAmountScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── QR Scanner ────────────────────────────────────────────
                        composable("qr_scanner") {
                            QRScannerScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onQrCodeScanned = { scannedValue ->
                                    val encoded = java.net.URLEncoder.encode(scannedValue, "UTF-8")
                                    navController.navigate("transfer_amount?recipient=$encoded") {
                                        popUpTo("qr_scanner") { inclusive = true }
                                    }
                                }
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

                        // ── Receive (Deposit + QR Code) ──────────────────────────
                        composable("receive") {
                            ReceiveScreen(
                                repository = repository,
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

                        // ── Accounts ──────────────────────────────────────────────
                        composable("accounts") {
                            val vm: AccountsViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        AccountsViewModel(repository) as T
                                }
                            )
                            AccountsScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ── Favorites ──────────────────────────────────────────────
                        composable("favorites") {
                            val vm: FavoritesViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                        FavoritesViewModel(repository) as T
                                }
                            )
                            FavoritesScreen(
                                viewModel = vm,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToTransfer = { recipient ->
                                    navController.navigate("transfer_amount?recipient=$recipient")
                                }
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
                        } // profile closing
                        } // NavHost closing
                    } // Box closing
                } // Scaffold closing
            } // HustleBankTheme closing
        } // setContent closing
    } // MainActivity closing
}
