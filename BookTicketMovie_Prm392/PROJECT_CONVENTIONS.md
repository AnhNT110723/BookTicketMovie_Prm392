# BookTicket Movie Android App - Project Conventions & Architecture

> **⚠️ IMPORTANT FOR AI ASSISTANT:** Always reference this file when implementing new features, fixing bugs, or making code changes. Follow these patterns exactly to maintain consistency.

## 📋 Overview

This document defines the coding conventions, architecture patterns, and database structure for the BookTicket Movie Android application. Follow these guidelines to ensure consistency across the codebase.

---

## 🗄️ Database Structure & Naming Conventions

### Database Schema

- **Database Name**: `MovieTicketBookingSystem`
- **Tables**: Use PascalCase (e.g., `Movie`, `User`, `Cinema`, `BookedSeat`)
- **Columns**: Use PascalCase (e.g., `MovieID`, `UserName`, `IsActive`)
- **Primary Keys**: Always `{TableName}ID` (e.g., `MovieID`, `UserID`, `CinemaID`)
- **Foreign Keys**: Reference the primary key name (e.g., `UserID`, `MovieID`)
- **Boolean Fields**: Use `Is{Property}` format (e.g., `IsActive`, `IsTrending`)

### Key Tables Structure

```sql
Movie: MovieID, Title, Description, Genre, Duration, Director, ReleaseDate, Rating, PosterURL, TrailerURL, Price, IsActive, IsTrending, CreatedDate
User: UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, RegistrationDate, IsActive, RoleID
Cinema: CinemaID, Name, Address, CityID, ContactInfo
Booking: BookingID, UserID, ShowID, BookingTime, NumberOfSeats, TotalPrice, Status, QRCodeData
```

---

## 🏗️ DAO (Data Access Object) Pattern

### BaseDAO Structure

All DAOs must extend `BaseDAO` and use its methods:

```java
public class ExampleDAO extends BaseDAO {
    // Use executeQuery() for SELECT operations
    // Use executeUpdate() for INSERT/UPDATE/DELETE operations
    // Use closeResources(rs, statement) in finally blocks
    // Always throw SQLException - don't catch and handle in DAO
}
```

### Required Methods Pattern

```java
// Get all items
public List<Entity> getAllEntities() throws SQLException {
    String query = "SELECT * FROM TableName WHERE IsActive = 1";
    ResultSet rs = null;
    PreparedStatement statement = null;

    try {
        rs = executeQuery(query);
        while (rs.next()) {
            entities.add(mapResultSetToEntity(rs));
        }
    } finally {
        closeResources(rs, statement);
    }
    return entities;
}

// Get by ID
public Entity getEntityById(int id) throws SQLException {
    String query = "SELECT * FROM TableName WHERE EntityID = ?";
    ResultSet rs = null;
    PreparedStatement statement = null;

    try {
        rs = executeQuery(query, id);
        if (rs.next()) {
            return mapResultSetToEntity(rs);
        }
    } finally {
        closeResources(rs, statement);
    }
    return null;
}

// Helper mapping method
private Entity mapResultSetToEntity(ResultSet rs) throws SQLException {
    // Map all fields from ResultSet to Entity object
}
```

### DAO Constructor

- DAOs extend BaseDAO - **NO** constructor parameters needed
- BaseDAO handles database connection management
- Example: `MovieDAO movieDAO = new MovieDAO();`

---

## 📱 Android Activity Structure

### Activity Lifecycle Pattern

```java
public class ExampleActivity extends AppCompatActivity {
    private static final String TAG = "ExampleActivity";

    // UI Components (declare at top)
    private Toolbar toolbar;
    private RecyclerView recyclerView;

    // Data variables
    private List<DataType> dataList;
    private DataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
        setupClickListeners();
    }

    private void initViews() { /* findViewById calls */ }
    private void setupToolbar() { /* Toolbar configuration */ }
    private void setupRecyclerView() { /* RecyclerView setup */ }
    private void loadData() { /* AsyncTask or data loading */ }
    private void setupClickListeners() { /* Click listeners */ }
}
```

### AsyncTask Pattern for Database Operations

```java
private class LoadDataTask extends AsyncTask<Void, Void, List<DataType>> {
    @Override
    protected List<DataType> doInBackground(Void... voids) {
        try {
            DataDAO dao = new DataDAO();
            return dao.getAllData();
        } catch (SQLException e) {
            Log.e(TAG, "Error loading data", e);
            return new ArrayList<>();
        }
    }

    @Override
    protected void onPostExecute(List<DataType> data) {
        // Update UI with data
        if (data != null && !data.isEmpty()) {
            dataList.clear();
            dataList.addAll(data);
            adapter.notifyDataSetChanged();
        }
    }
}
```

---

## 🎨 UI & Layout Conventions

### Layout File Naming

- Activities: `activity_{name}.xml` (e.g., `activity_movie_detail.xml`)
- Fragments: `fragment_{name}.xml`
- RecyclerView Items: `item_{type}.xml` (e.g., `item_movie.xml`, `item_cinema.xml`)
- Dialogs: `dialog_{name}.xml`

### ID Naming Convention

- Use snake_case with descriptive names
- Format: `{component_type}_{purpose}`
- Examples: `movie_title_text`, `book_ticket_button`, `cinemas_recycler_view`

### Color Scheme

```xml
<color name="primary">#1976D2</color>
<color name="primary_dark">#1565C0</color>
<color name="accent">#FF5722</color>
<color name="background_light">#FAFAFA</color>
<color name="text_primary">#212121</color>
<color name="text_secondary">#757575</color>
```

---

## 🔄 RecyclerView Adapter Pattern

### Standard Adapter Structure

```java
public class EntityAdapter extends RecyclerView.Adapter<EntityAdapter.EntityViewHolder> {
    private Context context;
    private List<Entity> entityList;
    private OnEntityClickListener onEntityClickListener;

    public interface OnEntityClickListener {
        void onEntityClick(Entity entity);
    }

    public EntityAdapter(Context context, List<Entity> entityList) {
        this.context = context;
        this.entityList = entityList;
    }

    public void setOnEntityClickListener(OnEntityClickListener listener) {
        this.onEntityClickListener = listener;
    }

    class EntityViewHolder extends RecyclerView.ViewHolder {
        // UI components

        public void bind(Entity entity) {
            // Bind data to views
            // Set click listener
        }
    }
}
```

---

## 🚀 Navigation & Intent Patterns

### Activity Navigation

```java
// Passing data between activities
Intent intent = new Intent(this, TargetActivity.class);
intent.putExtra("key_name", value);
intent.putExtra(TargetActivity.EXTRA_CONSTANT, value);
startActivity(intent);

// Activity constants for intent extras
public static final String EXTRA_MOVIE_ID = "movie_id";
public static final String EXTRA_MOVIE_TITLE = "movie_title";
```

### Navigation Flow

```
HomeActivity → MovieDetailActivity → CinemaSelectionActivity → ShowtimeSelectionActivity → SeatSelectionActivity → BookingConfirmationActivity
```

---

## 🔐 Authentication & Session Management

### User Session Pattern

```java
// Store user session
SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
SharedPreferences.Editor editor = prefs.edit();
editor.putBoolean("isLoggedIn", true);
editor.putInt("userId", user.getUserId());
editor.putString("userName", user.getName());
editor.apply();

// Check authentication
boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
```

### Role-Based Navigation

- Admin (RoleID = 1) → AdminActivity
- Customer (RoleID = 2) → HomeActivity
- FrontDeskOfficer (RoleID = 3) → OfficerActivity

---

## 📋 Error Handling & Logging

### Logging Convention

```java
private static final String TAG = "ActivityName";
Log.d(TAG, "Debug message");
Log.e(TAG, "Error message", exception);
```

### Error Handling in Activities

- Show Toast messages for user-facing errors
- Log technical errors with full stack traces
- Use try-catch in AsyncTasks, not in DAOs

---

## 🔧 Model Object Conventions

### Constructor Pattern

```java
public class Entity {
    // Default constructor
    public Entity() {
        // Set default values
    }

    // Constructor with essential fields
    public Entity(essential, fields) {
        this(); // Call default constructor
        // Set essential fields
    }

    // Full constructor
    public Entity(all, fields, including, id) {
        // Set all fields
    }
}
```

### Field Naming

- Use camelCase for Java fields
- Match database column names where possible
- Boolean fields: `isActive`, `isTrending` (not `active`, `trending`)

---

## 📚 File Organization

```
app/src/main/java/com/example/bookingticketmove_prm392/
├── adapters/           # RecyclerView adapters
├── database/dao/       # Data Access Objects
├── models/            # Entity/Model classes
├── utils/             # Utility classes (validation, password, etc.)
├── *.java            # Activity classes (root level)

app/src/main/res/
├── layout/           # XML layouts
├── drawable/         # Icons, images, selectors
├── values/          # Colors, strings, dimensions
├── menu/            # Menu XML files
```

---

## ✅ Code Quality Checklist

### Before Committing Code:

- [ ] Follows DAO pattern with BaseDAO inheritance
- [ ] Uses proper AsyncTask for database operations
- [ ] Includes proper error handling and logging
- [ ] Follows naming conventions for files and IDs
- [ ] Uses consistent UI patterns and colors
- [ ] Includes proper navigation flow
- [ ] Tests basic functionality
- [ ] No hardcoded strings (use strings.xml)
- [ ] Proper resource management (close connections)

---

## 🎯 Common Pitfalls to Avoid

1. **Don't** create DAO constructors with Context parameters
2. **Don't** catch SQLException in DAO methods - let them bubble up
3. **Don't** use direct database connections in Activities
4. **Don't** forget to call `closeResources()` in finally blocks
5. **Don't** hardcode database connection strings
6. **Don't** perform database operations on the main thread
7. **Don't** forget to register new Activities in AndroidManifest.xml

---

_This document should be referenced for all development work on the BookTicket Movie application. Update this file when new patterns or conventions are established._
