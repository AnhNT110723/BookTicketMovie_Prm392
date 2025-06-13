# BookTicketMovie MVC Refactoring Guide

## New MVC Architecture Structure

This project has been refactored to follow the **Model-View-Controller (MVC)** architectural pattern for better code organization, maintainability, and testability.

## Directory Structure

```
com.example.bookingticketmove_prm392/
│
├── controllers/              # Business Logic Layer (C)
│   ├── BaseController.java       # Base controller with common functionality
│   ├── UserController.java       # User-related business logic
│   ├── MovieController.java      # Movie-related business logic
│   ├── CinemaController.java     # Cinema-related business logic
│   └── BookingController.java    # Booking-related business logic
│
├── models/                   # Data Models (M)
│   ├── User.java                 # User entity
│   ├── Movie.java                # Movie entity
│   ├── Cinema.java               # Cinema entity
│   ├── Booking.java              # Booking entity
│   └── Show.java                 # Show entity
│
├── views/                    # User Interface Layer (V)
│   ├── activities/               # Android Activities
│   │   ├── LoginActivity.java
│   │   ├── HomeActivity.java
│   │   ├── MovieDetailActivity.java
│   │   └── ...
│   └── fragments/                # Android Fragments
│
├── repositories/             # Data Access Abstraction
│   ├── BaseRepository.java       # Base repository pattern
│   ├── UserRepository.java       # User data operations
│   ├── MovieRepository.java      # Movie data operations
│   └── CinemaRepository.java     # Cinema data operations
│
├── database/                 # Database Layer
│   ├── dao/                      # Data Access Objects
│   │   ├── BaseDAO.java          # Enhanced base DAO
│   │   ├── UserDAO.java          # User database operations
│   │   ├── MovieDAO.java         # Movie database operations
│   │   └── CinemaDAO.java        # Cinema database operations
│   ├── DatabaseConnection.java   # Database connection management
│   └── DatabaseConfig.java       # Database configuration
│
├── adapters/                 # RecyclerView Adapters
│   ├── MovieAdapter.java         # Movie list adapter
│   └── AdminMovieAdapter.java    # Admin movie adapter
│
└── utils/                    # Utility Classes
    ├── ValidationUtils.java      # Input validation
    └── ConfigHelper.java         # Configuration helpers
```

## MVC Pattern Implementation

### 1. **Model Layer**

- **Purpose**: Represents data and business entities
- **Location**: `models/` package
- **Examples**: `User.java`, `Movie.java`, `Cinema.java`
- **Responsibilities**:
  - Data structure definition
  - Data validation (basic)
  - Entity relationships

### 2. **View Layer**

- **Purpose**: Handles user interface and user interactions
- **Location**: `views/activities/` and `views/fragments/`
- **Examples**: `LoginActivity.java`, `HomeActivity.java`
- **Responsibilities**:
  - UI rendering
  - User input handling
  - Calling controller methods
  - Displaying data from controllers

### 3. **Controller Layer**

- **Purpose**: Contains business logic and coordinates between Model and View
- **Location**: `controllers/` package
- **Examples**: `UserController.java`, `MovieController.java`
- **Responsibilities**:
  - Business logic implementation
  - Input validation
  - Data processing
  - Coordinating with repositories
  - Error handling

### 4. **Repository Layer**

- **Purpose**: Abstracts data access and provides clean API for controllers
- **Location**: `repositories/` package
- **Examples**: `UserRepository.java`, `MovieRepository.java`
- **Responsibilities**:
  - Data access abstraction
  - Async operation handling
  - Caching (if needed)
  - Error handling for data operations

## How to Use the New Architecture

### 1. **In Activities (View Layer)**

```java
public class LoginActivity extends AppCompatActivity {
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize controller
        userController = new UserController();

        // Setup UI components
        setupLoginButton();
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Call controller method
            userController.login(email, password, new UserController.ControllerCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    // Handle successful login
                    navigateToHome(user);
                }

                @Override
                public void onError(String error) {
                    // Show error message
                    showErrorMessage(error);
                }

                @Override
                public void onLoading(boolean isLoading) {
                    // Show/hide loading indicator
                    showLoadingIndicator(isLoading);
                }
            });
        });
    }
}
```

### 2. **Controller Usage**

Controllers handle all business logic and validation:

```java
// User authentication
userController.login(email, password, callback);

// Movie operations
movieController.loadAllMovies(callback);
movieController.searchMovies(query, callback);

// Profile management
userController.updateProfile(user, callback);
```

### 3. **Repository Pattern**

Repositories provide clean data access:

```java
// In Controller
userRepository.loginUser(email, password, new UserRepository.RepositoryCallback<User>() {
    @Override
    public void onSuccess(User user) {
        // Handle success
    }

    @Override
    public void onError(Exception error) {
        // Handle error
    }
});
```

## Benefits of This Architecture

1. **Separation of Concerns**: Each layer has a specific responsibility
2. **Testability**: Business logic in controllers can be easily unit tested
3. **Maintainability**: Code is organized and easy to modify
4. **Reusability**: Controllers can be used by multiple views
5. **Error Handling**: Centralized error handling in controllers
6. **Async Operations**: Proper handling of database operations
7. **Validation**: Input validation centralized in controllers

## Migration Guide

### For Existing Activities:

1. **Move business logic** from Activities to Controllers
2. **Replace direct DAO calls** with Controller calls
3. **Implement callback interfaces** for async operations
4. **Move Activities** to `views/activities/` package

### Example Migration:

**Before (Old Structure):**

```java
// In Activity - NOT RECOMMENDED
new UserDAO().authenticateUser(email, password, new DatabaseCallback() {
    // Handle database response directly in UI
});
```

**After (MVC Structure):**

```java
// In Activity - RECOMMENDED
userController.login(email, password, new UserController.ControllerCallback<User>() {
    @Override
    public void onSuccess(User user) {
        // Handle successful login
    }

    @Override
    public void onError(String error) {
        // Handle error with user-friendly message
    }

    @Override
    public void onLoading(boolean isLoading) {
        // Show loading state
    }
});
```

## Next Steps

1. **Migrate existing Activities** to use Controllers
2. **Create additional Controllers** for Cinema, Booking, etc.
3. **Add more Repositories** as needed
4. **Implement proper error handling** throughout the app
5. **Add unit tests** for Controllers and Repositories
6. **Consider adding ViewModels** for complex UI state management

## Best Practices

1. **Never call DAOs directly** from Activities
2. **Always use Controllers** for business logic
3. **Handle loading states** in UI
4. **Provide user-friendly error messages**
5. **Validate inputs** in Controllers
6. **Keep Activities lightweight** - only UI logic
7. **Use Repository pattern** for data access abstraction

This refactored architecture will make your BookTicketMovie app more maintainable, testable, and scalable.
