package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CrisisPlan
import com.example.data.DeeskalationRepository
import com.example.data.IncidentReview
import com.example.data.CmsSection
import com.example.data.TeamLearning
import com.example.data.UserDiagnosis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BreathingPhase {
    IDLE, INHALE, EXHALE
}

class DeeskalationViewModel(private val repository: DeeskalationRepository) : ViewModel() {

    // Database flows
    val allCrisisPlans: StateFlow<List<CrisisPlan>> = repository.allCrisisPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allIncidentReviews: StateFlow<List<IncidentReview>> = repository.allIncidentReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCmsSections: StateFlow<List<CmsSection>> = repository.allCmsSections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTeamLearnings: StateFlow<List<TeamLearning>> = repository.allTeamLearnings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allUserDiagnoses: StateFlow<List<UserDiagnosis>> = repository.allUserDiagnoses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Interactive UI search & filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryTab = MutableStateFlow("HANDBUCH") // HANDBUCH, TOOLS, ADMIN
    val selectedCategoryTab: StateFlow<String> = _selectedCategoryTab.asStateFlow()

    private val _toolsMainTab = MutableStateFlow("COREG_SKILLS")
    val toolsMainTab: StateFlow<String> = _toolsMainTab.asStateFlow()

    private val _toolsSubTab = MutableStateFlow("BREATHING")
    val toolsSubTab: StateFlow<String> = _toolsSubTab.asStateFlow()

    private val _selectedPhaseId = MutableStateFlow("GELB")
    val selectedPhaseId: StateFlow<String> = _selectedPhaseId.asStateFlow()

    private val _selectedDiagnosisId = MutableStateFlow("EIPS")
    val selectedDiagnosisId: StateFlow<String> = _selectedDiagnosisId.asStateFlow()

    // Breathing trainer state flow
    private val _breathingPhase = MutableStateFlow(BreathingPhase.IDLE)
    val breathingPhase: StateFlow<BreathingPhase> = _breathingPhase.asStateFlow()

    private val _breathingSecondsLeft = MutableStateFlow(0)
    val breathingSecondsLeft: StateFlow<Int> = _breathingSecondsLeft.asStateFlow()

    private val _breathingCycleCount = MutableStateFlow(0)
    val breathingCycleCount: StateFlow<Int> = _breathingCycleCount.asStateFlow()

    private var breathingJob: Job? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ══════════════════════════════════════════════════════
    // WHO ICD-11 API INTEGRATION STATES
    // ══════════════════════════════════════════════════════
    private val _icdSearchQuery = MutableStateFlow("")
    val icdSearchQuery = _icdSearchQuery.asStateFlow()

    private val _icdSelectedTab = MutableStateFlow("OFFLINE") // "OFFLINE", "LIVE", "DOCS"
    val icdSelectedTab = _icdSelectedTab.asStateFlow()

    private val _icdIsLoading = MutableStateFlow(false)
    val icdIsLoading = _icdIsLoading.asStateFlow()

    private val _icdSearchResults = MutableStateFlow<List<com.example.data.IcdEntity>>(com.example.data.IcdApiManager.searchOffline(""))
    val icdSearchResults = _icdSearchResults.asStateFlow()

    private val _icdErrorMessage = MutableStateFlow<String?>(null)
    val icdErrorMessage = _icdErrorMessage.asStateFlow()

    private val _icdClientId = MutableStateFlow("")
    val icdClientId = _icdClientId.asStateFlow()

    private val _icdClientSecret = MutableStateFlow("")
    val icdClientSecret = _icdClientSecret.asStateFlow()

    private val _icdAccessTokenUser = MutableStateFlow("")
    val icdAccessTokenUser = _icdAccessTokenUser.asStateFlow()

    private val _icdConnectionStatus = MutableStateFlow<String?>(null) // "SUCCESS", "FAILED:<reason>", etc.
    val icdConnectionStatus = _icdConnectionStatus.asStateFlow()

    fun setIcdSearchQuery(query: String) {
        _icdSearchQuery.value = query
        executeIcdSearch()
    }

    fun setIcdSelectedTab(tab: String) {
        _icdSelectedTab.value = tab
        executeIcdSearch()
    }

    fun setIcdCredentials(clientId: String, clientSecret: String) {
        _icdClientId.value = clientId
        _icdClientSecret.value = clientSecret
    }

    fun setIcdAccessToken(token: String) {
        _icdAccessTokenUser.value = token
    }

    fun executeIcdSearch() {
        val q = _icdSearchQuery.value
        val tab = _icdSelectedTab.value
        
        if (tab == "OFFLINE") {
            _icdIsLoading.value = false
            _icdErrorMessage.value = null
            _icdSearchResults.value = com.example.data.IcdApiManager.searchOffline(q)
            return
        }

        // Live API search
        viewModelScope.launch {
            _icdIsLoading.value = true
            _icdErrorMessage.value = null
            try {
                // Determine token
                var token = _icdAccessTokenUser.value.trim()
                if (token.isEmpty() && _icdClientId.value.trim().isNotEmpty() && _icdClientSecret.value.trim().isNotEmpty()) {
                    // Try to retrieve token automatically
                    try {
                        val authRes = com.example.data.IcdApiManager.authApi.getAccessToken(
                            clientId = _icdClientId.value.trim(),
                            clientSecret = _icdClientSecret.value.trim()
                        )
                        token = authRes.accessToken
                        _icdAccessTokenUser.value = token
                    } catch (e: Exception) {
                        throw Exception("Token-Handshake fehlgeschlagen: ${e.localizedMessage}. Bitte überprüfen Sie Client-ID & Client-Secret.")
                    }
                }

                if (token.isEmpty()) {
                    // If no token or keys, we do online search error with helper
                    throw Exception("Kein gültiger Access-Token vorhanden. Bitte tragen Sie entweder einen Access-Token oder Client-ID & Client-Secret oben ein.")
                }

                val searchResult = com.example.data.IcdApiManager.searchApi.searchMms(
                    authHeader = "Bearer $token",
                    query = if (q.isBlank()) "mental" else q
                )

                if (searchResult.errorMessage != null) {
                    throw Exception(searchResult.errorMessage)
                }

                _icdSearchResults.value = searchResult.destinationEntities ?: emptyList()
            } catch (e: Exception) {
                // Graceful fallback to offline and set error warning
                _icdErrorMessage.value = e.localizedMessage ?: "Verbindungsfehler zur WHO API."
                // Even if Live failed, we populate with the offline database match so the user sees results!
                _icdSearchResults.value = com.example.data.IcdApiManager.searchOffline(q)
            } finally {
                _icdIsLoading.value = false
            }
        }
    }

    fun testIcdConnection() {
        viewModelScope.launch {
            _icdIsLoading.value = true
            _icdConnectionStatus.value = "Verbindung wird hergestellt..."
            try {
                val clientId = _icdClientId.value.trim()
                val clientSecret = _icdClientSecret.value.trim()
                val manualToken = _icdAccessTokenUser.value.trim()

                var actualToken = manualToken
                if (actualToken.isEmpty() && clientId.isNotEmpty() && clientSecret.isNotEmpty()) {
                    val authRes = com.example.data.IcdApiManager.authApi.getAccessToken(
                        clientId = clientId,
                        clientSecret = clientSecret
                    )
                    actualToken = authRes.accessToken
                    _icdAccessTokenUser.value = actualToken
                }

                if (actualToken.isEmpty()) {
                    throw Exception("Sowohl Token als auch Client-Credentials sind leer.")
                }

                // Check with simple search query "stress"
                val check = com.example.data.IcdApiManager.searchApi.searchMms(
                    authHeader = "Bearer $actualToken",
                    query = "stress"
                )
                
                _icdConnectionStatus.value = "ERFOLGREICH VERBUNDEN! ICD-11 WHO Schnittstelle ist bereit."
            } catch (e: Exception) {
                _icdConnectionStatus.value = "FEHLER: ${e.localizedMessage}"
            } finally {
                _icdIsLoading.value = false
            }
        }
    }

    fun setSelectedCategoryTab(tab: String) {
        _selectedCategoryTab.value = tab
    }

    fun setToolsMainTab(tab: String) {
        _toolsMainTab.value = tab
        _toolsSubTab.value = when (tab) {
            "COREG_SKILLS" -> "BREATHING"
            "TEAM_STATION" -> "ASSIST_ALERT"
            "LEARN_REVIEWS" -> "CASE_SIMS"
            else -> "BREATHING"
        }
    }

    fun setToolsSubTab(tab: String) {
        _toolsSubTab.value = tab
    }

    fun navigateToTools(mainTab: String, subTab: String) {
        _selectedCategoryTab.value = "TOOLS"
        _toolsMainTab.value = mainTab
        _toolsSubTab.value = subTab
    }

    fun setSelectedPhaseId(phaseId: String) {
        _selectedPhaseId.value = phaseId
    }

    fun setSelectedDiagnosisId(diagId: String) {
        _selectedDiagnosisId.value = diagId
    }

    // Breathing co-regulation logic
    fun startBreathing() {
        stopBreathing()
        _breathingCycleCount.value = 0
        breathingJob = viewModelScope.launch {
            while (true) {
                // Inhale for 4 seconds
                _breathingPhase.value = BreathingPhase.INHALE
                for (s in 4 downTo 1) {
                    _breathingSecondsLeft.value = s
                    delay(1000)
                }
                
                // Exhale for 8 seconds (activation of parasympathetic system)
                _breathingPhase.value = BreathingPhase.EXHALE
                for (s in 8 downTo 1) {
                    _breathingSecondsLeft.value = s
                    delay(1000)
                }
                _breathingCycleCount.value += 1
            }
        }
    }

    fun stopBreathing() {
        breathingJob?.cancel()
        breathingJob = null
        _breathingPhase.value = BreathingPhase.IDLE
        _breathingSecondsLeft.value = 0
    }

    // Database writes
    fun saveCrisisPlan(
        initials: String,
        diagnosisId: String,
        trigger: String,
        warningSigns: String,
        calming: String,
        worsening: String
    ) {
        viewModelScope.launch {
            repository.insertCrisisPlan(
                CrisisPlan(
                    patientInitials = initials,
                    mainDiagnosis = diagnosisId,
                    individualTrigger = trigger,
                    earlyWarningSigns = warningSigns,
                    preferredCalming = calming,
                    whatVerschlimmert = worsening
                )
            )
        }
    }

    fun deleteCrisisPlan(id: Int) {
        viewModelScope.launch {
            repository.deleteCrisisPlanById(id)
        }
    }

    fun saveIncidentReview(
        initials: String,
        dateString: String,
        descr: String,
        trig: String,
        strengths: String,
        lessons: String,
        wellbeing: String
    ) {
        viewModelScope.launch {
            repository.insertIncidentReview(
                IncidentReview(
                    patientInitials = initials,
                    incidentDate = dateString,
                    description = descr,
                    triggerSource = trig,
                    teamStrengths = strengths,
                    lessonsLearned = lessons,
                    teamWellbeing = wellbeing
                )
            )
        }
    }

    fun deleteIncidentReview(id: Int) {
        viewModelScope.launch {
            repository.deleteIncidentReviewById(id)
        }
    }

    // CMS Actions
    fun saveCmsSection(
        id: Int = 0,
        title: String,
        description: String,
        contentText: String,
        imageUrl: String,
        accentColorHex: String,
        phaseId: String
    ) {
        viewModelScope.launch {
            repository.insertCmsSection(
                CmsSection(
                    id = id,
                    title = title,
                    description = description,
                    contentText = contentText,
                    imageUrl = imageUrl,
                    accentColorHex = accentColorHex,
                    phaseId = phaseId
                )
            )
        }
    }

    fun deleteCmsSection(id: Int) {
        viewModelScope.launch {
            repository.deleteCmsSectionById(id)
        }
    }

    // Team Learning Actions ("Was hat funktioniert?")
    fun saveTeamLearning(situation: String, whatWorked: String, submittedByRole: String) {
        viewModelScope.launch {
            repository.insertTeamLearning(
                TeamLearning(
                    situation = situation,
                    whatWorked = whatWorked,
                    submittedByRole = submittedByRole
                )
            )
        }
    }

    fun deleteTeamLearning(id: Int) {
        viewModelScope.launch {
            repository.deleteTeamLearningById(id)
        }
    }

    fun saveUserDiagnosis(
        icdCode: String,
        name: String,
        dynamik: String,
        absicherung: String,
        klaerung: String,
        aufloesung: String
    ) {
        viewModelScope.launch {
            repository.insertUserDiagnosis(
                UserDiagnosis(
                    icdCode = icdCode,
                    name = name,
                    dynamik = dynamik,
                    absicherung = absicherung,
                    klaerung = klaerung,
                    aufloesung = aufloesung
                )
            )
        }
    }

    fun deleteUserDiagnosis(id: Int) {
        viewModelScope.launch {
            repository.deleteUserDiagnosisById(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBreathing()
    }
}

class DeeskalationViewModelFactory(private val repository: DeeskalationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeeskalationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeeskalationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
