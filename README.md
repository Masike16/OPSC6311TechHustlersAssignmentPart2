# EasEBudgetV1
Youtube video presentation link:
https://youtu.be/-IVl0UKKC_k

EasEBudget is a comprehensive personal finance management application designed to help users track their spending, manage budgets, and visualize their financial health. Built specifically with the South African market in mind, it defaults to South African Rands (ZAR) and provides a user-friendly interface for daily financial tracking.

## Group: Tech Hustlers
**Members:**
*   **ST10451774** - Acazia Ammon
*   **ST10452404** - Masike Jr Rasenyalo
*   **ST10452409** - Liyema Masala

---

## Features

### Secure Authentication
*   User registration and login system.
*   Session management that keeps you logged in securely.
*   Account deletion and profile update options.

### Transaction Management
*   Track both **Income** and **Expenses**.
*   Categorize transactions for better organization.
*   **Time Tracking**: Record both **Start Time** and **End Time** for precise transaction logging.
*   Add descriptions and select dates for every entry.
*   **Quick Adjust**: Features a "Quick Adjust Amount" seekbar for faster value entry.
*   **Receipt Attachments**: Capture photos of receipts using the camera or select from the gallery to keep a digital record.

### Budgeting & Limits
*   Set overall monthly budget goals.
*   Define specific spending limits for individual categories (e.g., Food, Transport).
*   Visual progress bars to show how much of your budget remains.
*   "Ready to Assign" calculation to help you plan your spending.

### Reports & Analytics
*   Interactive **Pie Charts** showing spending distribution by category.
*   Filter reports by Today, This Week, or This Month.
*   Summary cards for Total Income, Total Expenses, and Current Balance.
*   Top spending categories highlight.

### Key Utilities
*   **ZAR Support**: Localized formatting for South African Rands.
*   **Help Tooltips**: Onboarding system with helpful tips for new users.
*   **Persistent Storage**: All data is saved locally using Room Database (Version 2).
*   **Portable Design**: Project-relative paths and Gradle dependency management ensure the app can be easily built and run on any workstation.

---

## Technologies Used

*   **Language**: Kotlin
*   **UI Architecture**: MVVM (Model-View-ViewModel)
*   **Database**: Room (SQLite) with schema migration support
*   **Navigation**: Android Jetpack Navigation Component
*   **Image Loading**: Glide
*   **Charts**: MPAndroidChart
*   **Dependency Injection**: ViewModelProvider Factory
*   **UI Components**: Material Design 3

---

## Getting Started

1.  Clone the repository.
2.  Open the project in **Android Studio (Hedgehog or newer)**.
3.  Wait for **Gradle Sync** to finish downloading dependencies.
4.  Run the application on an emulator or physical Android device (**API 26+**).

---
