Android Project AI Context
This document provides architecture rules and project context for AI assistants such as:
Cursor
ChatGPT
Gemini
Claude
AI must follow all rules in this document when generating, modifying, or refactoring code in this project.
Project Overview
This project is an Android application written in Kotlin.
Architecture:
Clean Architecture + MVVM + StateFlow
Goals:
Maintainable code
Clear separation of concerns
Scalable architecture
Testable business logic
Thin UI layer
Architecture Layers
Project uses Clean Architecture with 4 main layers.
presentation
domain
data
core
Additional layer:
di
Explanation:
Layer	Responsibility
presentation	UI and ViewModels
domain	business logic and use cases
data	repository implementations and database
core	utilities, helpers, constants
di	dependency injection modules
Dependency Rules
Dependencies must follow this direction:
presentation → domain → data
Rules:
presentation can depend on domain
domain must not depend on presentation
data must not depend on presentation
domain must not depend on Android framework
Never allow reverse dependencies.
Example of forbidden dependency:
domain → presentation ❌
data → presentation ❌
Technology Stack
Language
Kotlin
Architecture
MVVM + Clean Architecture
State management
StateFlow
Async
Kotlin Coroutines + Flow
Dependency Injection
Hilt
UI
Fragments + RecyclerView
Persistence
Room Database
Presentation Layer
The presentation layer contains:
Activities
Fragments
ViewModels
RecyclerView Adapters
UiState classes
Main rule:
Fragments and Activities must remain thin UI layers.
Allowed in UI Layer
Fragments and Activities may contain:
ViewBinding
RecyclerView adapter setup
collecting StateFlow
rendering UI
click listeners
navigation
simple UI state handling
Forbidden in UI Layer
The following must NOT appear inside Fragments or Activities:
Business logic:
transaction filtering
grouping transactions
sorting
calculations
statistics
currency calculations
Data manipulation:
Flow transformation
Flow combination
aggregation
repository calls
Data parsing:
date parsing
number formatting logic
data mapping
All of these must be moved to ViewModel or domain layer.
ViewModel Responsibilities
ViewModels are responsible for:
business logic
Flow combination
Flow transformations
filtering
grouping
sorting
building UiState
exposing screen state via StateFlow
Each screen must have its own ViewModel.
Example mapping:
DailyFragment → DailyViewModel
MonthlyFragment → MonthlyViewModel
StatisticFragment → StatisticViewModel
Shared cross-screen logic should use:
SharedTransactionViewModel
UiState Pattern
Each screen must expose a UiState class.
Example:
data class DailyUiState(
val transactions: List<TransactionGroup> = emptyList(),
val selectedDate: LocalDate,
val isEmpty: Boolean
)
ViewModel exposes state via:
private val _uiState = MutableStateFlow(DailyUiState())

val uiState: StateFlow<DailyUiState> = _uiState
Fragments only:
collect uiState
render UI
Fragments must not modify business data.
Flow Usage Rules
Avoid nested Flow collection.
Bad:
filterFlow.collect {
transactionFlow.collect { }
}
Correct:
combine(filterFlow, transactionFlow)
All Flow operations must occur inside ViewModel.
Fragments should collect only one main uiState Flow.
RecyclerView Adapter Rules
Adapters must be state driven.
Use:
submitList()
with DiffUtil.
Avoid:
notifyDataSetChanged()
notifyItemChanged()
unless absolutely necessary.
Data Transformation Rules
All data transformations must occur in ViewModel or domain layer.
Examples:
filtering transactions
grouping by date
sorting
currency totals
statistics
aggregation
These must not appear in Fragment.
Date Handling Rules
Date parsing must not happen in UI layer.
Bad:
LocalDate.parse()
SimpleDateFormat()
inside Fragment.
Correct locations:
ViewModel
domain mappers
helper classes in core
Use Case Layer (Domain)
Business logic should ideally be encapsulated in UseCases.
Example:
GetTransactionsUseCase
CalculateMonthlyStatsUseCase
GroupTransactionsByDateUseCase
UseCases should contain:
reusable business logic
data processing
aggregation
ViewModels call UseCases.
Navigation Rules
Navigation can remain inside Fragment.
However preferred architecture:
ViewModel emits navigation events.
Example:
sealed class DailyEvent {
object OpenAddTransaction : DailyEvent()
}
Fragment observes events and triggers navigation.
Dependency Injection Rules
Dependency injection uses Hilt.
Rules:
ViewModels must use @HiltViewModel
Dependencies must be injected via constructor
Avoid manual dependency creation
Example:
@HiltViewModel
class DailyViewModel @Inject constructor(
private val repository: TransactionRepository
)
Refactoring Rules for AI
When modifying code:
Analyze the entire file first
Detect business logic inside Fragments
Move logic to ViewModel
Create missing UiState if necessary
Prefer StateFlow over LiveData
Do not break repository API
Avoid rewriting large files unnecessarily
Prefer small safe refactors.
Screen Architecture Pattern
Final screen architecture should look like:
Fragment
↓
ViewModel
↓
UseCase (optional)
↓
Repository
↓
Data Source
Example Flow Architecture
ViewModel:
combine(
filterOption,
transactionFlow
) { filter, transactions ->

    process transactions

}
.collect {
update UiState
}
Fragment:
viewLifecycleOwner.lifecycleScope.launch {
viewModel.uiState.collect {
render(it)
}
}
Target Code Quality
Fragments must remain small and simple.
Typical Fragment responsibilities:
bind views
setup adapters
observe ViewModel state
render UI
handle click events
navigation
Most logic must exist inside ViewModel or domain layer.
Architecture Goal
The architecture must follow:
UI → ViewModel → UseCase → Repository → Data
UI must remain a thin rendering layer.
Business logic must live in ViewModel or domain layer.