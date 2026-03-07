# Phase 10: Auth & Token Management

The Android client is currently suffering from a **401 Unauthorized** error because there is no persistent caching strategy for the `jwt` token received during `Login`/`Register`, nor an `OkHttp Interceptor` attaching it to outbound Retrofit requests. Furthermore, `AuthViewModel` is still utilizing mocked credentials.

Please execute the following implementation steps systematically to resolve the 401 Unauthorized error.

## 1. Token Persistence

*   **Create `app/src/main/java/com/hustle/bankapp/data/TokenManager.kt`**
    *   Create a class `TokenManager(context: Context)` that wraps Android `SharedPreferences`.
    *   Implement methods: `saveToken(token: String)`, `getToken(): String?`, and `clearToken()`.
    *   Use the EncryptedSharedPreferences if possible, or standard SharedPreferences for simplicity.

## 2. API Authorization Interceptor

*   **Create `app/src/main/java/com/hustle/bankapp/data/api/AuthInterceptor.kt`**
    *   Implement `okhttp3.Interceptor`.
    *   Initialize it with an instance of `TokenManager`.
    *   In the `intercept` method, retrieve the token from `TokenManager`.
    *   If the token is not null, append the `Authorization: Bearer <token>` header to the original request.
    *   Proceed with the request.

*   **Modify `app/src/main/java/com/hustle/bankapp/MainActivity.kt`**
    *   Instantiate `TokenManager` using `applicationContext` in `onCreate`.
    *   Instantiate `AuthInterceptor(tokenManager)`.
    *   Add the `AuthInterceptor` to the `OkHttpClient.Builder()` alongside the existing logging interceptor.

## 3. Link Repository and ViewModels

*   **Modify `app/src/main/java/com/hustle/bankapp/data/BankRepository.kt` & `RemoteBankRepositoryImpl.kt`**
    *   Update the `BankRepository` interface to add `suspend fun login(email: String, password: String): Result<User>`.
    *   Inject `TokenManager` into `RemoteBankRepositoryImpl`.
    *   Implement the `login` function. Call `api.login()`. On success, call `tokenManager.saveToken(response.body()!!.token)` and return `Result.success(user)`.
    *   Update the `registerUser` function. On success, call `tokenManager.saveToken(response.body()!!.token)`.
    *   Update the `logout` function to clear the local token using `tokenManager.clearToken()`.

*   **Modify `app/src/main/java/com/hustle/bankapp/ui/auth/AuthViewModel.kt`**
    *   Change the constructor to accept `private val repository: BankRepository`.
    *   Remove the hardcoded mock credentials (`validAccount`, `validPassword`).
    *   Refactor the `login()` function to launch a coroutine and call `repository.login(accountNumber, password)`.
    *   Handle `onSuccess` by setting `isAuthenticated = true` and `isLoading = false`.
    *   Handle `onFailure` by showing the error message.

*   **Modify `app/src/main/java/com/hustle/bankapp/MainActivity.kt` (Dependency Injection Update)**
    *   Update the `RemoteBankRepositoryImpl` instantiation to pass the new `tokenManager` parameter.
    *   Update the `composable("login")` block to provide the `AuthViewModel` using a `ViewModelProvider.Factory`, passing the `repository` to its constructor (just like the other ViewModels).
