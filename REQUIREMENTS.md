# Recipe Sharing and Management App - Requirements

## Overview
A group of users is sharing and managing their recipes using a mobile application. Each user can upload, view, and edit their recipes while exploring recipes shared by others.

## Server Data Model

Each recipe on the server maintains the following details:

| Field | Type | Description |
|-------|------|-------------|
| **Id** | Integer | The unique identifier for the recipe. Integer value greater than zero. |
| **Date** | String | The date when the recipe was created. Format: "YYYY-MM-DD" |
| **Title** | String | The title of the recipe |
| **Ingredients** | String | A list of ingredients used in the recipe |
| **Category** | String | The category of the recipe (e.g., dessert, appetizer, main course) |
| **Rating** | Decimal | The average rating of the recipe |

## API Endpoints

- `GET /recipes` - Retrieve list of all recipes
- `GET /recipe/:id` - Retrieve details of a specific recipe
- `POST /recipe` - Create a new recipe
- `DELETE /recipe/:id` - Delete a recipe
- `GET /allRecipes` - Retrieve complete list of all recipes

## Application Features & Requirements

### Main Section (separate screen/activity) - 5 points total

#### A. View the list of all recipes (1 point)
- **Endpoint:** `GET /recipes`
- **Offline behavior:**
  - Display offline message if no connection
  - Allow retry mechanism
  - Once retrieved, data should be cached and available offline
- **Implementation:** ✅ COMPLETED
  - MainActivity displays list of recipes
  - SwipeRefreshLayout allows retry
  - Need to add: Local database caching for offline support

#### B. View detailed information about a recipe (2 points)
- **Endpoint:** `GET /recipe/:id`
- **Offline behavior:**
  - Once retrieved, data should be cached and available offline
- **Implementation:** ❌ NOT IMPLEMENTED
  - Need to create: Detail screen/activity
  - Need to implement: Click listener on recipe items
  - Need to add: Local database caching

#### C. Add a new recipe (1 point)
- **Endpoint:** `POST /recipe`
- **Availability:** Online only
- **Requirements:** Specify all recipe details (date, title, ingredients, category, rating)
- **Implementation:** ❌ NOT IMPLEMENTED
  - Need to create: Add recipe screen/activity
  - Need to implement: Form with all fields
  - Need to add: Validation and POST call

#### D. Edit or delete a recipe (1 point)
- **Endpoint:** `DELETE /recipe/:id`
- **Availability:** Online only
- **Implementation:** ✅ PARTIALLY COMPLETED
  - Delete functionality: ✅ IMPLEMENTED
  - Edit functionality: ❌ NOT IMPLEMENTED
  - Need to add: Edit screen/activity

### Explore Section (separate screen/activity) - 1 point

#### View monthly rating analysis (1 point)
- **Endpoint:** `GET /allRecipes`
- **Requirements:**
  - Compute list of monthly average ratings
  - Display in descending order
- **Implementation:** ❌ NOT IMPLEMENTED
  - Need to create: Explore screen/activity
  - Need to implement: Monthly grouping logic
  - Need to display: Chart or list view

### Insights Section (separate screen/activity) - 1 point

#### View the top 3 categories by rating (1 point)
- **Endpoint:** `GET /allRecipes`
- **Requirements:**
  - Compute top 3 categories by total rating
  - Display in descending order
- **Implementation:** ❌ NOT IMPLEMENTED
  - Need to create: Insights screen/activity
  - Need to implement: Category aggregation logic
  - Need to display: Top 3 with ratings

### WebSocket Notifications - 1 point

#### Real-time recipe notifications (1 point)
- **Technology:** WebSocket channel
- **Trigger:** When a new recipe is added
- **Requirements:**
  - Notify all connected clients
  - Display recipe title and category
  - Show in human-readable form (toast or dialog)
- **Implementation:** ❌ NOT IMPLEMENTED
  - Need to implement: WebSocket client connection
  - Need to add: Notification handler
  - Server already has WebSocket support on port 2528

### Additional Requirements

#### Progress Indicator (0.5 points)
- **Requirements:** Display during all server operations
- **Implementation:** ✅ COMPLETED
  - ProgressBar shows during loading
  - SwipeRefreshLayout shows during refresh

#### Error Handling (0.5 points)
- **Requirements:**
  - Display server interaction errors via toast or snackbar
  - Log all interactions (server and database)
- **Implementation:** ✅ PARTIALLY COMPLETED
  - Error toasts: ✅ IMPLEMENTED
  - Logging: ⚠️ PARTIALLY IMPLEMENTED (OkHttp logging only)
  - Need to add: Comprehensive logging for all operations

## Implementation Checklist

### ✅ Completed (4.5 / 10 points)
- [x] Main Section A: List all recipes with retry
- [x] Main Section D: Delete recipe (50% - missing edit)
- [x] Progress indicator during operations
- [x] Error messages via toast
- [x] Server request logging (OkHttp interceptor)

### ❌ To Implement (5.5 / 10 points)
- [ ] Main Section B: Recipe detail screen (2 points)
- [ ] Main Section C: Add new recipe (1 point)
- [ ] Main Section D: Edit recipe (0.5 points)
- [ ] Explore Section: Monthly rating analysis (1 point)
- [ ] Insights Section: Top 3 categories (1 point)

### ⚠️ Missing Critical Features (1 point)
- [ ] WebSocket notifications (1 point)

### 🔧 Technical Debt
- [ ] Local database implementation (Room) for offline caching
- [ ] Offline mode support for viewing cached data
- [ ] Comprehensive logging system
- [ ] Navigation between screens (Bottom Navigation or Drawer)

## Score Breakdown

| Feature | Points | Status |
|---------|--------|--------|
| **Main Section** | 5 | 2.5/5 ✅ |
| A. List recipes | 1 | 0.5/1 ⚠️ (no offline cache) |
| B. Recipe details | 2 | 0/2 ❌ |
| C. Add recipe | 1 | 0/1 ❌ |
| D. Edit/Delete | 1 | 0.5/1 ⚠️ (delete only) |
| **Explore Section** | 1 | 0/1 ❌ |
| Monthly analysis | 1 | 0/1 ❌ |
| **Insights Section** | 1 | 0/1 ❌ |
| Top 3 categories | 1 | 0/1 ❌ |
| **WebSocket** | 1 | 0/1 ❌ |
| Notifications | 1 | 0/1 ❌ |
| **Progress/Errors** | 1 | 1/1 ✅ |
| Progress indicator | 0.5 | 0.5/0.5 ✅ |
| Error handling | 0.5 | 0.5/0.5 ✅ |
| **TOTAL** | **10** | **3.5/10** |

## Priority Implementation Order

1. **High Priority (Required for basic functionality)**
   - Local database (Room) for offline support
   - Recipe detail screen
   - Add recipe screen
   - Edit recipe functionality

2. **Medium Priority (Analysis features)**
   - Explore Section (monthly analysis)
   - Insights Section (top categories)
   - Navigation between sections

3. **Low Priority (Nice to have)**
   - WebSocket real-time notifications
   - Advanced error handling
   - Comprehensive logging

## Technical Stack

### Current Implementation
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit + OkHttp
- **UI:** Material Design, RecyclerView, SwipeRefreshLayout
- **Async:** Coroutines + LiveData

### Still Needed
- **Database:** Room (for offline caching)
- **WebSocket:** OkHttp WebSocket or Java-WebSocket
- **Navigation:** Navigation Component or manual Activity navigation
- **Charts (optional):** MPAndroidChart for monthly analysis visualization
