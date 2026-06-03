package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

// ══════════════════════════════════════════════════════
// GLOBAL APP DESIGN CUSTOMIZER STATE
// ══════════════════════════════════════════════════════
object AppCustomizer {
    var primaryColorHex by mutableStateOf("#1D4ED8")
    var cardStyle by mutableStateOf("Glassmorphism") // Glassmorphism, Brutalist, Minimal Outline, Glowing Neon
    var fontFamilyName by mutableStateOf("Clean Modern") // Clean Modern, Sophisticated Serif, Tech Monospace, Friendly OpenDyslexic
    var paddingDp by mutableStateOf(14f)
    var spacingDp by mutableStateOf(12f)
    var textAlignmentName by mutableStateOf("Left") // Left, Center, Right, Justify
    var applyGlobally by mutableStateOf(true)
    var customHtmlContent by mutableStateOf("""
<div style="text-align: center;">
  <h1>✨ Klinisches Interaktions-Protokoll</h1>
  <p>Verwenden Sie dieses interaktive Widget, um App-Styles <b>live</b> auf HTML auszuwerten.</p>
  <hr/>
  <h3>📋 Wichtige Richtlinien:</h3>
  <ul>
    <li>Fokus auf Atemfrequenz im Deeskalations-Modus</li>
    <li>Echtzeit-Feedback bei kognitiver Überlastung</li>
    <li>Medizinisch auditierte Empfehlungen</li>
  </ul>
</div>
    """.trimIndent())
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DeeskalationApp(viewModel: DeeskalationViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedCategoryTab.collectAsStateWithLifecycle()
    val selectedPhaseId by viewModel.selectedPhaseId.collectAsStateWithLifecycle()
    val selectedDiagnosisId by viewModel.selectedDiagnosisId.collectAsStateWithLifecycle()
    val crisisPlans by viewModel.allCrisisPlans.collectAsStateWithLifecycle()
    val incidentReviews by viewModel.allIncidentReviews.collectAsStateWithLifecycle()
    val cmsSections by viewModel.allCmsSections.collectAsStateWithLifecycle()
    val teamLearnings by viewModel.allTeamLearnings.collectAsStateWithLifecycle()
    val userDiagnoses by viewModel.allUserDiagnoses.collectAsStateWithLifecycle()

    val breathingPhase by viewModel.breathingPhase.collectAsStateWithLifecycle()
    val breathingSeconds by viewModel.breathingSecondsLeft.collectAsStateWithLifecycle()
    val breathingCycles by viewModel.breathingCycleCount.collectAsStateWithLifecycle()
    val toolsMainTab by viewModel.toolsMainTab.collectAsStateWithLifecycle()
    val toolsSubTab by viewModel.toolsSubTab.collectAsStateWithLifecycle()

    var showAdminUnlockDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var adminDialogErrorMessage by remember { mutableStateOf("") }

    if (showAdminUnlockDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminUnlockDialog = false
                adminPinInput = ""
                adminDialogErrorMessage = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("CMS-Admin freischalten")
                }
            },
            text = {
                Column {
                    Text(
                        "Geben Sie den Administrator-Code ein, um neue Inhaltsbereiche zu erstellen, Pfaden zuzuweisen und Texte/Bilder per CMS zu ändern.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = { adminPinInput = it },
                        label = { Text("Code (Hinweis: admin)") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                    )
                    if (adminDialogErrorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adminDialogErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPinInput == "admin") {
                            isAdminUnlocked = true
                            showAdminUnlockDialog = false
                            adminPinInput = ""
                            adminDialogErrorMessage = ""
                            viewModel.setSelectedCategoryTab("ADMIN")
                        } else {
                            adminDialogErrorMessage = "Falscher Code! Bitte verwenden Sie 'admin'."
                        }
                    },
                    modifier = Modifier.testTag("submit_admin_confirm")
                ) {
                    Text("Freischalten")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminUnlockDialog = false
                        adminPinInput = ""
                        adminDialogErrorMessage = ""
                    }
                ) {
                    Text("Abheben")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable {
                                // Secret trigger: clicking on the title reveals the admin login dialog too!
                                showAdminUnlockDialog = true
                            }
                    ) {
                        Text(
                            text = "Deeskalation KJP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Safewards · Polyvagal · DBT · GFK · Mentalisierung",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (isAdminUnlocked) {
                                // Locked if already unlocked, toggles state
                                isAdminUnlocked = false
                                viewModel.setSelectedCategoryTab("PHASEN")
                            } else {
                                showAdminUnlockDialog = true
                            }
                        },
                        modifier = Modifier.testTag("admin_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "CMS Admin",
                            tint = if (isAdminUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setSearchQuery("") },
                        modifier = Modifier.testTag("reset_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Nachrichten zurücksetzen"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Mobile-first, Adaptive Custom Navigation Bar
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val menuTabs = mutableListOf(
                    Triple("HANDBUCH", "Handbuch", Icons.Default.Info),
                    Triple("DESIGN", "Design Remix", Icons.Default.List)
                )
                if (isAdminUnlocked) {
                    menuTabs.add(Triple("ADMIN", "CMS Admin", Icons.Default.Settings))
                }
                
                menuTabs.forEach { (tabId, tabName, icon) ->
                    NavigationBarItem(
                        selected = (selectedTab == tabId) || (tabId == "HANDBUCH" && (selectedTab != "DESIGN" && selectedTab != "ADMIN")),
                        onClick = { viewModel.setSelectedCategoryTab(tabId) },
                        icon = { Icon(icon, contentDescription = tabName) },
                        label = { Text(tabName, fontSize = 11.sp) },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_item_${tabId.lowercase()}")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .testTag("main_app_scaffold_content")
        ) {
            // Max Width constraint for Large screens (Tablets/Landscape)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                // Main Content Switching
                val activeTab = when (selectedTab) {
                    "DESIGN" -> "DESIGN"
                    "ADMIN" -> "ADMIN"
                    else -> "HANDBUCH"
                }
                when (activeTab) {
                    "HANDBUCH" -> HandbuchScreen(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        selectedPhaseId = selectedPhaseId,
                        onPhaseSelected = { viewModel.setSelectedPhaseId(it) },
                        selectedDiagnosisId = selectedDiagnosisId,
                        onDiagnosisSelected = { viewModel.setSelectedDiagnosisId(it) },
                        cmsSections = cmsSections,
                        crisisPlans = crisisPlans,
                        incidentReviews = incidentReviews,
                        teamLearnings = teamLearnings,
                        userDiagnoses = userDiagnoses,
                        breathingPhase = breathingPhase,
                        breathingSeconds = breathingSeconds,
                        breathingCycles = breathingCycles,
                        onStartBreathing = { viewModel.startBreathing() },
                        onStopBreathing = { viewModel.stopBreathing() },
                        onSaveCrisisPlan = { init, diag, trig, warn, calm, worsening ->
                            viewModel.saveCrisisPlan(init, diag, trig, warn, calm, worsening)
                        },
                        onDeleteCrisisPlan = { id -> viewModel.deleteCrisisPlan(id) },
                        onSaveIncidentReview = { init, date, desc, trig, stren, less, wellbeing ->
                            viewModel.saveIncidentReview(init, date, desc, trig, stren, less, wellbeing)
                        },
                        onDeleteIncidentReview = { id -> viewModel.deleteIncidentReview(id) },
                        onSaveTeamLearning = { situation, help, role ->
                            viewModel.saveTeamLearning(situation, help, role)
                        },
                        onDeleteTeamLearning = { id -> viewModel.deleteTeamLearning(id) },
                        onSaveUserDiagnosis = { code, name, dyn, abs, kla, auf ->
                            viewModel.saveUserDiagnosis(code, name, dyn, abs, kla, auf)
                        },
                        onDeleteUserDiagnosis = { id -> viewModel.deleteUserDiagnosis(id) }
                    )
                    "DESIGN" -> {
                        DesignRemixScreen()
                    }
                    "ADMIN" -> {
                        if (isAdminUnlocked) {
                            AdminCmsScreen(
                                cmsSections = cmsSections,
                                onSaveSection = { id, title, desc, text, url, color, phase ->
                                    viewModel.saveCmsSection(id, title, desc, text, url, color, phase)
                                },
                                onDeleteSection = { id -> viewModel.deleteCmsSection(id) }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Bitte entsperren Sie den Admin-Bereich.")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// 1. PHASEN SCREEN
// ══════════════════════════════════════════════════════
@Composable
fun PhasenScreen(
    selectedPhaseId: String,
    onPhaseSelected: (String) -> Unit,
    cmsSections: List<CmsSection>,
    crisisPlans: List<CrisisPlan>,
    onSaveCrisisPlan: (String, String, String, String, String, String) -> Unit,
    onDeleteCrisisPlan: (Int) -> Unit,
    breathingPhase: BreathingPhase,
    breathingSeconds: Int,
    breathingCycles: Int,
    onStartBreathing: () -> Unit,
    onStopBreathing: () -> Unit
) {
    val phaseList = ScientificContent.phases
    val currentPhase = phaseList.first { it.id == selectedPhaseId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Die 5 Deeskalations-Phasen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Strukturierter Ablauf nach dem wissenschaftlich fundierten Safewards-Modell. Deeskalation gelingt am effektivsten durch Co-Regulation in Phase GELB.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Illustrierte Erregungskurve Pipeline (Staircase Peak flow)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ILLUSTRIERTER PHASEN-VERLAUF (Erregungsniveau)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Connected stairs displaying arousal curve white -> green -> yellow -> red -> blue
                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val phasesData = listOf(
                            Triple("WEISS", "Baseline", Color(0xFF64748B)),
                            Triple("GRUEN", "Prävention", Color(0xFF22C55E)),
                            Triple("GELB", "Eskalation", Color(0xFFF59E0B)),
                            Triple("ROT", "Akutkrise", Color(0xFFEF4444)),
                            Triple("BLAU", "Latenz", Color(0xFF3B82F6))
                        )
                        
                        phasesData.forEachIndexed { idx, (id, label, color) ->
                            val isSelected = id == selectedPhaseId
                            val heightFract = (idx + 1) * 0.2f
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightFract)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) color else color.copy(alpha = 0.25f))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else color.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onPhaseSelected(id) }
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = id,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Selector Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                phaseList.forEach { phase ->
                    val isSelected = phase.id == selectedPhaseId
                    val bg = Color(android.graphics.Color.parseColor(phase.colorHex))
                    val tc = Color(android.graphics.Color.parseColor(phase.textColorHex))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) tc else bg.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                             )
                            .clickable { onPhaseSelected(phase.id) }
                            .padding(vertical = 8.dp, horizontal = 2.dp)
                            .testTag("phase_pill_${phase.id.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                    text = phase.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tc
                            )
                            Text(
                                text = when(phase.id) {
                                    "WEISS" -> "Grund"
                                    "GRUEN" -> "Präv."
                                    "GELB" -> "Früh"
                                    "ROT" -> "Krise"
                                    "BLAU" -> "Nachb"
                                    else -> ""
                                },
                                fontSize = 9.sp,
                                color = tc.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Active Phase Information Card
        item {
            val phaseBg = Color(android.graphics.Color.parseColor(currentPhase.colorHex))
            val phaseTc = Color(android.graphics.Color.parseColor(currentPhase.textColorHex))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = phaseBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = phaseTc,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "PHASE ${currentPhase.id}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = currentPhase.subtitle,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = phaseTc.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentPhase.deName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = phaseTc
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentPhase.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = phaseTc
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = phaseTc.copy(alpha = 0.2f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NEUROBIOLOGISCHE BASIS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = phaseTc.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentPhase.neuroBasics,
                        fontSize = 13.sp,
                        color = phaseTc.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // IN-CONTEXT CLINICAL DATABASES & TOOLS:
        // 1. Patient crisis plans database and form (Under GRÜN)
        if (selectedPhaseId == "GRUEN") {
            item {
                var isExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📋 PATIENTEN-KRISENPLÄNE (Datenbank)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Erstellen Sie deeskalative Krisenpläne für Patienten auf der Station.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (isExpanded) "Schließen" else "Öffnen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            var initials by remember { mutableStateOf("") }
                            var diagnosis by remember { mutableStateOf("") }
                            var trigger by remember { mutableStateOf("") }
                            var warningSigns by remember { mutableStateOf("") }
                            var calming by remember { mutableStateOf("") }
                            var worsening by remember { mutableStateOf("") }
                            
                            OutlinedTextField(
                                value = initials,
                                onValueChange = { initials = it },
                                label = { Text("Patienten-Initialen (z.B. M.K.)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("cp_initials_input")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = diagnosis,
                                onValueChange = { diagnosis = it },
                                label = { Text("Hintergrund-Diagnostik", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = trigger,
                                onValueChange = { trigger = it },
                                label = { Text("Individuelle Trigger / Reize", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = warningSigns,
                                onValueChange = { warningSigns = it },
                                label = { Text("Frühwarnzeichen (Phy./Verh.)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = calming,
                                onValueChange = { calming = it },
                                label = { Text("Beruhigungsmethoden & Co-Regulation", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = worsening,
                                onValueChange = { worsening = it },
                                label = { Text("Was verschlimmert die Spannung?", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Button(
                                onClick = {
                                    if (initials.isNotEmpty()) {
                                        onSaveCrisisPlan(initials, diagnosis, trigger, warningSigns, calming, worsening)
                                        initials = ""
                                        diagnosis = ""
                                        trigger = ""
                                        warningSigns = ""
                                        calming = ""
                                        worsening = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End).testTag("save_crisis_plan_btn")
                            ) {
                                Text("Krisenplan sichern", fontSize = 12.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Gespeicherte Pläne für diese Station:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            if (crisisPlans.isEmpty()) {
                                Text("Keine Pläne hinterlegt.", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                            } else {
                                crisisPlans.forEach { plan ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Patient: ${plan.patientInitials}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                IconButton(
                                                    onClick = { onDeleteCrisisPlan(plan.id) },
                                                    modifier = Modifier.size(24.dp).testTag("delete_crisis_plan_${plan.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text("• Diagnose: ${plan.mainDiagnosis}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("• Trigger: ${plan.individualTrigger}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("• Frühwarnzeichen: ${plan.earlyWarningSigns}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("• Bevorzugte Beruhigung: ${plan.preferredCalming}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("• Zu vermeiden: ${plan.whatVerschlimmert}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Paced breathing coach (Under ROT)
        if (selectedPhaseId == "ROT") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🧘 ATEMCOACHING (CO-REGULATION BED-SIDE)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.widthIn(max = 600.dp).align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Führen Sie den Jugendlichen durch verlangsamtes, synchrones Atmen (4s Einatmen / 8s Ausatmen). Spiegelneuronen ko-regulieren das System automatisch.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.widthIn(max = 600.dp).align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isRunning = breathingPhase != BreathingPhase.IDLE
                        
                        // Breathing visually animated pulse box
                        val animatedScale by animateFloatAsState(
                            targetValue = when (breathingPhase) {
                                BreathingPhase.INHALE -> 1.5f
                                BreathingPhase.EXHALE -> 0.9f
                                else -> 1.0f
                            },
                            animationSpec = tween(
                                durationMillis = when (breathingPhase) {
                                    BreathingPhase.INHALE -> 4000
                                    BreathingPhase.EXHALE -> 8000
                                    else -> 1000
                                },
                                easing = LinearEasing
                            )
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    when (breathingPhase) {
                                        BreathingPhase.INHALE -> Color(0xFF86EFAC).copy(alpha = animatedScale * 0.4f)
                                        BreathingPhase.EXHALE -> Color(0xFF93C5FD).copy(alpha = animatedScale * 0.4f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (breathingPhase) {
                                        BreathingPhase.INHALE -> "EINATMEN"
                                        BreathingPhase.EXHALE -> "AUSATMEN"
                                        BreathingPhase.IDLE -> "BEREIT"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                if (isRunning) {
                                    Text(
                                        text = "$breathingSeconds Sek.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Abgeschlossene Atemzyklen: $breathingCycles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Button(
                            onClick = {
                                if (isRunning) onStopBreathing() else onStartBreathing()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFDC2626) else Color(0xFF16A34A)
                            ),
                            modifier = Modifier.testTag("start_breathing_btn")
                        ) {
                            Text(if (isRunning) "Atemcoach stoppen" else "Atemcoach starten", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Section: Key Actions
        item {
            Text(
                text = "Schlüsselaspekte & Deeskalations-Handlungen",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(currentPhase.keyInteractions) { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Don'ts - Warnings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Fehler vermeiden",
                            tint = Color(0xFFB91C1C)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KRITISCHE DON'TS (ESKALATIONSTREIBER):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    currentPhase.donts.forEach { dont ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Text(text = "• ", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text(
                                text = dont,
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (currentPhase.additionalTips.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tipp",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPhase.additionalTips,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Real-time CMS Dynamic Custom Sections for this active phase
        val phaseCmsItems = cmsSections.filter { it.phaseId == selectedPhaseId }
        if (phaseCmsItems.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Zusätzliche Stations-Leitlinien (CMS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(phaseCmsItems) { section ->
                CmsSectionCard(section = section, isAdminView = false)
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// 2. DIAGNOSEN SCREEN (ICD-11 API & Custom Teams DB)
// ══════════════════════════════════════════════════════
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DiagnosenScreen(
    viewModel: DeeskalationViewModel,
    userDiagnoses: List<UserDiagnosis>,
    onSaveUserDiagnosis: (String, String, String, String, String, String) -> Unit,
    onDeleteUserDiagnosis: (Int) -> Unit
) {
    var screenTab by remember { mutableStateOf("WHO_API") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = screenTab == "WHO_API",
                onClick = { screenTab = "WHO_API" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WHO ICD-11 Live-Suche", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("diag_section_who_tab")
            )
            FilterChip(
                selected = screenTab == "COGNITIVE_USER",
                onClick = { screenTab = "COGNITIVE_USER" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eigene Diagnosen (Team DB)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("diag_section_user_tab")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        if (screenTab == "WHO_API") {
            IcdApiScreen(viewModel = viewModel)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Vom Team gepflegte Diagnosen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Passen Sie Deeskalationsleitfäden für Ihre Stationsrealität an. Diese fließen direkt in das agile Wissen Ihrer Kollegen ein.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Add Diagnosis Form Card
                item {
                    var showForm by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showForm = !showForm },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("➕ Neue Team-Diagnose hinzufügen", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }

                            if (showForm) {
                                Spacer(modifier = Modifier.height(12.dp))
                                var code by remember { mutableStateOf("") }
                                var name by remember { mutableStateOf("") }
                                var dynamics by remember { mutableStateOf("") }
                                var safe by remember { mutableStateOf("") }
                                var clear by remember { mutableStateOf("") }
                                var resolve by remember { mutableStateOf("") }

                                OutlinedTextField(
                                    value = code,
                                    onValueChange = { code = it },
                                    label = { Text("ICD-Code (z.B. F90.0)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("user_diag_code_input")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Diagnosenbezeichnung (z.B. ADHS)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("user_diag_name_input")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = dynamics,
                                    onValueChange = { dynamics = it },
                                    label = { Text("Eskalationsdynamik & Triggerfaktoren", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = safe,
                                    onValueChange = { safe = it },
                                    label = { Text("Absicherungs-Interventionen (BEI ERREGUNG)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = clear,
                                    onValueChange = { clear = it },
                                    label = { Text("Klärungs-Interventionen (GELB / INTERAKTION)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = resolve,
                                    onValueChange = { resolve = it },
                                    label = { Text("Auflösungs-Interventionen (BLAU / WIEDERAUFBAU)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (code.isNotEmpty() && name.isNotEmpty()) {
                                            onSaveUserDiagnosis(code, name, dynamics, safe, clear, resolve)
                                            code = ""
                                            name = ""
                                            dynamics = ""
                                            safe = ""
                                            clear = ""
                                            resolve = ""
                                            showForm = false
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End).testTag("save_user_diagnosis_btn")
                                ) {
                                    Text("Diagnose speichern", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Display list of Saved User-Diagnoses
                if (userDiagnoses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Noch keine eigenen Diagnosen hinterlegt.", fontSize = 12.sp, fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                } else {
                    items(userDiagnoses) { diag ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = diag.icdCode,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = diag.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeleteUserDiagnosis(diag.id) },
                                        modifier = Modifier.size(24.dp).testTag("delete_user_diagnosis_${diag.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Eskalationsdynamik:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(diag.dynamik, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Absicherung (WEISS/GRÜN):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB45309))
                                Text(diag.absicherung, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Klärungskonzepte (GELB):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text(diag.klaerung, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Auflösungsmethoden (BLAU):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF15803D))
                                Text(diag.aufloesung, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionPillarCard(
    title: String,
    content: String,
    bg: Color,
    tc: Color,
    border: Color,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, border),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tc,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = tc,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFF1E293B)
            )
        }
    }
}

// ══════════════════════════════════════════════════════
// 3. WISSEN / REFERENCE SCREEN
// ══════════════════════════════════════════════════════
// 3. HANDBUCH / COHESIVE 7-CHAPTER REFERENCE TEXTBOOK SCREEN
// ══════════════════════════════════════════════════════
@Composable
fun HandbuchScreen(
    viewModel: DeeskalationViewModel,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedPhaseId: String,
    onPhaseSelected: (String) -> Unit,
    selectedDiagnosisId: String,
    onDiagnosisSelected: (String) -> Unit,
    cmsSections: List<CmsSection>,
    crisisPlans: List<CrisisPlan>,
    incidentReviews: List<IncidentReview>,
    teamLearnings: List<TeamLearning>,
    userDiagnoses: List<UserDiagnosis>,
    breathingPhase: BreathingPhase,
    breathingSeconds: Int,
    breathingCycles: Int,
    onStartBreathing: () -> Unit,
    onStopBreathing: () -> Unit,
    onSaveCrisisPlan: (String, String, String, String, String, String) -> Unit,
    onDeleteCrisisPlan: (Int) -> Unit,
    onSaveIncidentReview: (String, String, String, String, String, String, String) -> Unit,
    onDeleteIncidentReview: (Int) -> Unit,
    onSaveTeamLearning: (String, String, String) -> Unit,
    onDeleteTeamLearning: (Int) -> Unit,
    onSaveUserDiagnosis: (String, String, String, String, String, String) -> Unit,
    onDeleteUserDiagnosis: (Int) -> Unit
) {
    var selectedChapterId by remember { mutableStateOf<Int?>(null) }

    if (searchQuery.isNotEmpty()) {
        HandbuchSearchResultsView(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            cmsSections = cmsSections,
            onNavigateToChapter = { chapterId ->
                // Account for the shifted indices in results
                selectedChapterId = when (chapterId) {
                    1 -> 1
                    2 -> 2
                    3 -> 3
                    4 -> 1 // Unified phasen
                    5 -> 4 // Shuffled Diagnosen
                    6 -> 5 // Shuffled Team
                    7 -> 6 // Shuffled Referenzen
                    8 -> 4 // Consolidated ICD Search
                    else -> 1
                }
                onSearchChange("")
            },
            onNavigateToPhase = { phaseId ->
                selectedChapterId = 1
                onPhaseSelected(phaseId)
                onSearchChange("")
            },
            onNavigateToDiagnosis = { diagId ->
                selectedChapterId = 4
                onDiagnosisSelected(diagId)
                onSearchChange("")
            }
        )
    } else if (selectedChapterId == null) {
        TableOfContentsView(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            onSelectChapter = { selectedChapterId = it }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedChapterId = null },
                    modifier = Modifier.testTag("back_to_toc_button_chapter")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück zum Inhaltsverzeichnis"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "KAPITEL 0$selectedChapterId",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { selectedChapterId = null }
                ) {
                    Text("Inhaltsverzeichnis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Box(modifier = Modifier.weight(1f)) {
                when (selectedChapterId) {
                    1 -> PhasenScreen(
                        selectedPhaseId = selectedPhaseId,
                        onPhaseSelected = onPhaseSelected,
                        cmsSections = cmsSections,
                        crisisPlans = crisisPlans,
                        onSaveCrisisPlan = onSaveCrisisPlan,
                        onDeleteCrisisPlan = onDeleteCrisisPlan,
                        breathingPhase = breathingPhase,
                        breathingSeconds = breathingSeconds,
                        breathingCycles = breathingCycles,
                        onStartBreathing = onStartBreathing,
                        onStopBreathing = onStopBreathing
                    )
                    2 -> Chapter2View(onNavigateToTools = { _, _ -> })
                    3 -> Chapter3View(onNavigateToTools = { _, _ -> })
                    4 -> DiagnosenScreen(
                        viewModel = viewModel,
                        userDiagnoses = userDiagnoses,
                        onSaveUserDiagnosis = onSaveUserDiagnosis,
                        onDeleteUserDiagnosis = onDeleteUserDiagnosis
                    )
                    5 -> Chapter6View(
                        incidentReviews = incidentReviews,
                        onSaveIncidentReview = onSaveIncidentReview,
                        onDeleteIncidentReview = onDeleteIncidentReview,
                        teamLearnings = teamLearnings,
                        onSaveTeamLearning = onSaveTeamLearning,
                        onDeleteTeamLearning = onDeleteTeamLearning
                    )
                    6 -> Chapter7View()
                }
            }
        }
    }
}

@Composable
fun BorderedBox(
    title: String,
    borderColor: Color,
    contentColor: Color = borderColor,
    backgroundColor: Color = borderColor.copy(alpha = 0.05f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = contentColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun SideBySideCards(
    title1: String,
    border1: Color,
    items1: List<String>,
    title2: String,
    border2: Color,
    items2: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BorderedBox(title = title1, borderColor = border1) {
            items1.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("• ", fontWeight = FontWeight.Bold, color = border1)
                    Text(text = item, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        BorderedBox(title = title2, borderColor = border2) {
            items2.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("• ", fontWeight = FontWeight.Bold, color = border2)
                    Text(text = item, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CompactTableCard(
    badgeText: String,
    badgeBg: Color,
    badgeTextCol: Color,
    title: String,
    fields: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeBg,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextCol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            fields.forEach { (label, value) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(text = value, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TableOfContentsView(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectChapter: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ASKLEPIOS KLINIKUM HAMBURG HARBURG · KINDER- UND JUGENDPSYCHIATRIE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KLINISCHES REFERENZWERK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Deeskalierende Gesprächsführung",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Multiprofessionelle Kompetenz für die psychiatrische Akutstation • 14 bis 17 Jahre",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val circles = listOf(
                            Triple("W", "WEISS", Color(0xFFF1F5F9) to Color(0xFF334155)),
                            Triple("G", "GRÜN", Color(0xFFDCFCE7) to Color(0xFF166534)),
                            Triple("G", "GELB", Color(0xFFFEF3C7) to Color(0xFF92400E)),
                            Triple("R", "ROT", Color(0xFFFEE2E2) to Color(0xFF991B1B)),
                            Triple("B", "BLAU", Color(0xFFDBEAFE) to Color(0xFF1E40AF))
                        )
                        circles.forEach { (let, full, colors) ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(colors.first, CircleShape)
                                    .border(1.dp, colors.second.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = let, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.second)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val tags = listOf("Safewards", "Polyvagal-Theorie", "DBT Skills", "GFK", "Mentalisierung")
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pflege · Medizin · Psychologie · Sozialarbeit · Ergotherapie · Schule",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Version 1.0 · 2025 · Nur für internen Dienstgebrauch",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("handbuch_search_bar"),
                placeholder = { Text("Suchbegriff oder Kapitel suchen...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "INHALTSVERZEICHNIS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        val chapters = listOf(
            Triple(1, "Das 5-Farben-Modell", "Einführung · Illustrierter Verlauf · Phasen im Detail (Atemcoaching & Krisenpläne)"),
            Triple(2, "Neurobiologie", "Polyvagal-Theorie · Amygdala Hijack · Kortisol-Latenz · Co-Regulation · Mentalisierung"),
            Triple(3, "Haltung & Kommunikation", "Professionelle Haltung · Körpersprache · Stimme · GFK · Validation · Spaltungsprävention"),
            Triple(4, "Diagnosen (ICD API & Team)", "WHO ICD-11 Live-Suche · Klinische Leitfäden des Teams · Custom Diagnosen"),
            Triple(5, "Team & Nachbereitung", "Post-Incident-Review · Team-Learnings · Rollenverteilung · Selbstfürsorge"),
            Triple(6, "Referenzen", "Wissenschaftliche Grundlagen und Literaturangaben")
        )

        items(chapters) { (id, title, contents) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectChapter(id) }
                    .testTag("toc_chapter_$id"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0$id",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = contents,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Öffnen",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "WARUM DIESES DOKUMENT?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deeskalation ist keine individuelle Technik – sie ist eine multiprofessionelle Teamkompetenz. Dieses Referenzwerk fasst fünf wissenschaftlich evaluierte Ansätze zu einem konsistenten Gesamtbild zusammen, das für alle Berufsgruppen auf der Station gleichzeitig gilt.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BorderedBox(title = "DAS KERNPRINZIP", borderColor = Color(0xFF1E40AF)) {
                Text(
                    text = "Deeskalation beginnt im eigenen Nervensystem – nicht beim Patienten. Co-Regulation ist neurobiologisch messbar: Das Nervensystem des Teams reguliert das der gesamten Station.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF1E3A8A)
                )
            }

            BorderedBox(title = "WISSENSCHAFTLICHE BASIS", borderColor = Color(0xFF15803D)) {
                val bases = listOf(
                    "Safewards" to "bis zu 20% Reduktion von Zwangsmaßnahmen (Bowers et al., 2014)",
                    "Polyvagal-Theorie" to "Neurobiologie von Sicherheit und Stress (Porges, 2011)",
                    "DBT-Skills" to "Interaktive Selbstregulation des Fachpersonals (Linehan, 2015)",
                    "GFK" to "Strukturierte, bedürfnisbezogene Gesprächsführung (Rosenberg, 2016)",
                    "Mentalisierung" to "Verstehen innerer Zustände und Absichten (Fonagy, 2004)"
                )
                bases.forEach { (title, desc) ->
                    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                        Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Text(
                            text = "$title: $desc",
                            fontSize = 11.sp,
                            color = Color(0xFF14532D),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            BorderedBox(title = "WIE DIESES DOKUMENT ZU LESEN IST", borderColor = Color(0xFF475569)) {
                Text(
                    text = "Die Kapitel bauen aufeinander auf – das Neurobiologie-Kapitel erklärt, warum die Interventionen in den Phasen so gestaltet sind, wie sie sind. Das Diagnose-Kapitel kombiniert alle vorherigen Konzepte für spezifische klinische Konstellationen. Kapitel 04 (Die 5 Phasen im Detail) is die primäre praktische Referenz für den Stationsalltag.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun Chapter1View(
    onSelectPhase: (String) -> Unit,
    onNavigateToTools: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "KAPITEL 01 · SCHNELLREFERENZ",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Das 5-Farben-Modell",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gemeinsame Sprache des Teams · Grundlage für schnelle, abgestimmte Krisenreaktion. Jede Phase signalisiert dem gesamten Team sofort die anwendbaren neurobiologischen Methoden.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // Selector Row of Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "WEISS" to Color(0xFFF1F5F9),
                    "GRUEN" to Color(0xFFDCFCE7),
                    "GELB" to Color(0xFFFEF3C7),
                    "ROT" to Color(0xFFFEE2E2),
                    "BLAU" to Color(0xFFDBEAFE)
                ).forEach { (ph, bg) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(bg, RoundedCornerShape(4.dp))
                            .clickable { onSelectPhase(ph) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ph, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "EVIDENZBASIERTE SCHNELLREFERENZ",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
        }

        val rows = listOf(
            CompactTableCardData(
                badgeText = "W", badgeBg = Color(0xFFF1F5F9), badgeTextCol = Color(0xFF334155), title = "WEISS (Grundlage)",
                fields = listOf(
                    "NERVENSYSTEM" to "Ventraler Vagus · Soziale Verbundenheit",
                    "ERKENNUNGSZEICHEN" to "Stabile Grundstimmung, Team-Kooperation, keine Frühwarnzeichen",
                    "KERNINTERVENTIONEN" to "Ausbildung · Krisenplanung · Selbstfürsorge · DBT PLEASE · Supervision",
                    "ABSOLUT VERMEIDEN" to "Krisenplanung aufschieben · Supervision als optional behandeln"
                )
            ),
            CompactTableCardData(
                badgeText = "G", badgeBg = Color(0xFFDCFCE7), badgeTextCol = Color(0xFF166534), title = "GRÜN (Prävention)",
                fields = listOf(
                    "NERVENSYSTEM" to "Ventraler Vagus · aktiv & zugänglich",
                    "ERKENNUNGSZEICHEN" to "Patient offen, kommunizierend, ruhige Mimik, Blickkontakt",
                    "KERNINTERVENTIONEN" to "Beziehungsaufbau · Krisenplan herstellen/aktualisieren · klare Strukturen",
                    "ABSOLUT VERMEIDEN" to "Unangekündigte Regeländerungen · personalabhängige Ausnahmen"
                )
            ),
            CompactTableCardData(
                badgeText = "G", badgeBg = Color(0xFFFEF3C7), badgeTextCol = Color(0xFF92400E), title = "GELB (Frühwarnung)",
                fields = listOf(
                    "NERVENSYSTEM" to "Sympathikus beginnt zu aktivieren · Kortex noch erreichbar",
                    "ERKENNUNGSZEICHEN" to "Rückzug · motorische Unruhe · angespannter Körper · einsilbig",
                    "KERNINTERVENTIONEN" to "DBT STOP · Absicherung · Grounding · GFK Schritte 1-3 · Reizreduktion",
                    "ABSOLUT VERMEIDEN" to "Klärungsgespräch beginnen · Erklärungen geben · zwei Personen sprechen zugleich"
                )
            ),
            CompactTableCardData(
                badgeText = "R", badgeBg = Color(0xFFFEE2E2), badgeTextCol = Color(0xFF991B1B), title = "ROT (Akutkrise)",
                fields = listOf(
                    "NERVENSYSTEM" to "Amygdala übernimmt; Kortex offline ODER dorsaler Vagus; Erstarrung",
                    "ERKENNUNGSZEICHEN" to "Aggression · Schreien · Grenzüberschreitung ODER Starre · leerer Blick",
                    "KERNINTERVENTIONEN" to "Sicherheit · Reizreduktion · verlängerte Ausatmung · Grounding (Erstarrung)",
                    "ABSOLUT VERMEIDEN" to "Klärung versuchen · Argumente · Berühren ohne Erlaubnis"
                )
            ),
            CompactTableCardData(
                badgeText = "B", badgeBg = Color(0xFFDBEAFE), badgeTextCol = Color(0xFF1E40AF), title = "BLAU (Nachbereitung)",
                fields = listOf(
                    "NERVENSYSTEM" to "Rückkehr zu ventral · aber Kortisol noch 20-60 Min. erhöht",
                    "ERKENNUNGSZEICHEN" to "Äußerlich ruhig – aber erst nach Kortisol-Latenz wirklich zugänglich",
                    "KERNINTERVENTIONEN" to "Warten (min. 20 Min.) · Klärung · GFK Schritt 4 · Würde herstellen · PIR",
                    "ABSOLUT VERMEIDEN" to "Sofortiges Klärungsgespräch · Vorwürfe · Entschuldigung erzwingen"
                )
            )
        )

        items(rows) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            BorderedBox(title = "NEUROBIOLOGISCHE GRUNDREGEL", borderColor = Color(0xFF1E40AF)) {
                Text(
                    text = "Was in einer phase sinnvoll ist, ist in einer anderen neurobiologisch nicht erreichbar. Ein Klärungsgespräch in Phase ROT scheitert nicht an mangelndem Geschick – es scheitert an der Physiologie des Gehirns. Die Phase bestimmt, was möglich ist.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF1E3A8A)
                )
            }
            BorderedBox(title = "WICHTIGSTE TEAMREGEL", borderColor = Color(0xFF15803D)) {
                Text(
                    text = "Phasenwechsel laut ansagen: „Ich sehe GELB“ ist keine Diagnose – es ist ein Koordinationssignal. Wer zuerst erkennt, gibt die Information weiter. Das Team reagiert auf den Zustand des Patienten, nicht auf die eigene Deutung des Verhaltens.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF14532D)
                )
            }
            BorderedBox(title = "KORTISOL-LATENZ - DIE UNSICHTBAE GEFAHR", borderColor = Color(0xFFD97706)) {
                Text(
                    text = "Nach dem sichtbaren Abklingen einer ROT-Krise bleibt der Kortisol-Spiegel noch 20 bis 60 Minuten erhöht. Der Patient wirkt ruhig – das Nervensystem bleibt in Alarmbereitschaft. Ein Klärungsgespräch in dieser Zeit kann einen zweiten Eskalationszyklus auslösen. Warten ist keine Gleichgültigkeit, sondern klinisch notwendige Rücksicht auf physiologische Realität.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF78350F)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DIREKTE VERLINKUNG ZU PRAXIS-TOOLS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wenden Sie die gelernten Prinzipien sofort mit unseren interaktiven Stations-Hilfsmitteln an:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToTools("COREG_SKILLS", "BREATHING") },
                            modifier = Modifier.testTag("ch1_link_breathing"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atemcoaching", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onNavigateToTools("COREG_SKILLS", "DBT_SENSORY") },
                            modifier = Modifier.testTag("ch1_link_dbt"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DBT & Sensorik (Eispack)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

data class CompactTableCardData(
    val badgeText: String,
    val badgeBg: Color,
    val badgeTextCol: Color,
    val title: String,
    val fields: List<Pair<String, String>>
)

@Composable
fun Chapter2View(onNavigateToTools: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "KAPITEL 02 · NEUROBIOLOGISCHE GRUNDLAGEN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Gehirn & Nervensystem im Stress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Warum verhalten sich Jugendliche in Krisen so wie sie es tun? Die moderne Gehirnforschung erklärt die Notwendigkeit von Co-Regulation und deeskalierendem Verhalten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "POLYVAGAL-THEORIE (PORGES, 2011)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val pvData = listOf(
            CompactTableCardData(
                "W/G", Color(0xFFDCFCE7), Color(0xFF166534), "Ventraler Vagus (Soziale Verbundenheit)",
                fields = listOf(
                    "ERKENNBAR AN" to "Gute Mimik, Blickkontakt, freie Sprache, Kooperation",
                    "WAS PHYSIOLOGISCH MÖGLICH IST" to "Klärung · Therapie · Krisenplan · Lernen · Nachgespräch (einziges therapeutisches Fenster)"
                )
            ),
            CompactTableCardData(
                "Y", Color(0xFFFEF3C7), Color(0xFF92400E), "Sympathikus (Kampf-Flucht / GELB)",
                fields = listOf(
                    "ERKENNBAR AN" to "Rückzug · Unruhe · angespannter Körper · einsilbig · Provokation",
                    "WAS PHYSIOLOGISCH MÖGLICH IST" to "Nur Validation und Absicherung – kein rationales Klärungsgespräch"
                )
            ),
            CompactTableCardData(
                "R", Color(0xFFFEE2E2), Color(0xFF991B1B), "Dorsaler Vagus (Erstarrung-Collapse / ROT)",
                fields = listOf(
                    "ERKENNBAR AN" to "Dissoziation, vollständige Starre, starrer Blick, Sprache blockiert",
                    "WAS PHYSIOLOGISCH MÖGLICH IST" to "Ausschließlich Grounding (Erdung) und Reizarmut, keine Berührung"
                )
            )
        )

        items(pvData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "AMYGDALA HIJACK & KORTISOL-LATENZ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "In 17 bis 100 Millisekunden kann die Amygdala bei stressreiz den Kortex offline schalten – stumm schalten. Das betrifft Patient und Fachperson gleichermaßen.",
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        val ahData = listOf(
            CompactTableCardData(
                "GELB", Color(0xFFFEF3C7), Color(0xFF92400E), "Vor dem Hijack",
                fields = listOf(
                    "NEUROBIOLOGIE" to "Amygdala zunehmend aktiv · Kortex noch erreichbar",
                    "KONSEQUENZ FÜR DAS TEAM" to "Letztes Zeitfenster für Kommunikation · DBT STOP jetzt · keine Konfrontation"
                )
            ),
            CompactTableCardData(
                "ROT", Color(0xFFFEE2E2), Color(0xFF991B1B), "Hijack active",
                fields = listOf(
                    "NEUROBIOLOGIE" to "Amygdala übernimmt vollständig · Kortex offline",
                    "KONSEQUENZ FÜR DAS TEAM" to "Nur Sicherheit herstellen · keine Klärung · keine Argumente"
                )
            ),
            CompactTableCardData(
                "POST", Color(0xFFECEFEE), Color(0xFF475569), "Post-Hijack (sichtbar ruhig)",
                fields = listOf(
                    "NEUROBIOLOGIE" to "Amygdala beruhigt sich · Kortex beginnt Rückkehr",
                    "KONSEQUENZ FÜR DAS TEAM" to "Kortisol noch erhöht · mind. 20 Minuten warten – noch KEINE Klärung"
                )
            ),
            CompactTableCardData(
                "BLAU", Color(0xFFDBEAFE), Color(0xFF1E40AF), "Latenz abgeklungen",
                fields = listOf(
                    "NEUROBIOLOGIE" to "Volle Rückkehr zum ventralen Vagus",
                    "KONSEQUENZ FÜR DAS TEAM" to "Jetzt ist deeskalierendes Gespräch physiologisch möglich und sinnvoll"
                )
            )
        )

        items(ahData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CO-REGULATION & SPIEGELNEURONEN",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nervensysteme beeinflussen sich gegenseitig direkt, automatisch und unwillkürlich schneller als Sprache. Spiegelneuronen übertragen Emotionen.",
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        val cgData = listOf(
            CompactTableCardData(
                "1", Color(0xFFECEFEE), Color(0xFF475569), "Atemrhythmus",
                fields = listOf(
                    "WIRKUNG" to "Verlangsamtes Atem signalisiert dem Gegenüber Sicherheit (Parasympathikus)",
                    "ANWENDUNG" to "Sichtbar 4 Sek. einatmen, 8 Sek. ausatmen für deeskalierendes Modell"
                )
            ),
            CompactTableCardData(
                "2", Color(0xFFECEFEE), Color(0xFF475569), "Bewegungstempo",
                fields = listOf(
                    "WIRKUNG" to "Motorische Entschleunigung bremst Amygdala-Spiegelung",
                    "ANWENDUNG" to "Sich langsamer bewegen als die eigene Aufregung es fordert"
                )
            ),
            CompactTableCardData(
                "3", Color(0xFFECEFEE), Color(0xFF475569), "Stimmfrequenz",
                fields = listOf(
                    "WIRKUNG" to "Eine tiefe Stimme triggert Vagus-Bremse",
                    "ANWENDUNG" to "Stimme in den Brustraum senken, gleichmäßig fließen lassen"
                )
            ),
            CompactTableCardData(
                "4", Color(0xFFECEFEE), Color(0xFF475569), "Muskeltonus",
                fields = listOf(
                    "WIRKUNG" to "Gespannte Hände signalisieren Kampf im Gegenüber",
                    "ANWENDUNG" to "Schultern senken, offene Hände, entspanntes Gesicht einnehmen"
                )
            )
        )

        items(cgData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            BorderedBox(title = "KERNPRINZIP CO-REGULATION", borderColor = Color(0xFFDC2626)) {
                Text(
                    text = "Dysreguliertes Personal kann kein Kind ko-regulieren. Ablösung in der Krise ist keine Niederlage, sondern professionelle Intervention. Wenn ein Teammitglied anhaltend dysreguliert ist, erhöht das die Grundspannung der gesamten Station – für alle Patienten gleichzeitig.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF991B1B)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MENTALISIERUNG & STRESSVERZERRUNG",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            SideBySideCards(
                title1 = "ZUSAMMENBRUCH DER MENTALISIERUNG – ZEICHEN",
                border1 = Color(0xFFEA580C),
                items1 = listOf(
                    "Extremes Schwarz-Weiß-Denken (Alles/Nichts)",
                    "Globale Abwertungen: „Du hast mich noch nie gemocht“",
                    "Unfähigkeit, Vergangenheit von Gegenwart teils zu trennen",
                    "Verhalten des Gegenübers wird sofort als persönlicher Angriff interpretiert"
                ),
                title2 = "MENTALISIERUNG REAKTIVIEREN",
                border2 = Color(0xFF16A34A),
                items2 = listOf(
                    "Neugier zeigen statt zu korrigieren oder zu belehren",
                    "Traumapädagogische Leitfrage stellen: „Was ist passiert?\" statt „Warum tut er das?\"",
                    "Im Team-Review: „Was könnte im Jugendlichen vorgegangen sein, als es begann?\""
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "WAHRNEHMUNGSFEHLER DES TEAMS UNTER STRESS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val wfData = listOf(
            CompactTableCardData(
                "ERR1", Color(0xFFECEFEE), Color(0xFF475569), "Fundamental Attribution Error",
                fields = listOf(
                    "BESCHREIBUNG" to "Das eskalative Verhalten wird fälschlich der stabilen Persönlichkeit zugeschrieben.",
                    "KLINISCHE KONSEQUENZ" to "„Er ist aggressiv“ statt „Er befindet sich im Amygdala Hijack und kann physiologisch gerade nicht anders“"
                )
            ),
            CompactTableCardData(
                "ERR2", Color(0xFFECEFEE), Color(0xFF475569), "Confirmation Bias (Bestätigungsfehler)",
                fields = listOf(
                    "BESCHREIBUNG" to "Stressverengung lässt uns Signale übersehen, die unsere Meinung widerlegen.",
                    "KLINISCHE KONSEQUENZ" to "Frühwarnsignale bei vertrauten oder „schwierigen“ Patienten werden übersehen oder abgetan."
                )
            ),
            CompactTableCardData(
                "ERR3", Color(0xFFECEFEE), Color(0xFF475569), "Perceptual Narrowing (Tunnelblick)",
                fields = listOf(
                    "BESCHREIBUNG" to "Fokus engt sich extrem auf die stärkste Bedrohungs- oder Reizquelle ein.",
                    "KLINISCHE KONSEQUENZ" to "Andere Patienten im Raum, sekundäre Gefahren und Fluchtwege werden visuell ausgeblendet."
                )
            )
        )

        items(wfData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "DBT STOP-SKILL FÜR DAS TEAM",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("S", "STOP", "Innehalten, einfrieren"),
                    Triple("T", "TAKE", "Schritt zurück"),
                    Triple("O", "OBSERVE", "Situation beobachten"),
                    Triple("P", "PROCEED", "Wise Mind nutzen")
                ).forEach { (l, sh, de) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(l, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(sh, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                            Text(de, fontSize = 8.sp, lineHeight = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            BorderedBox(title = "INNERER CHECK – 10 SEKUNDEN VOR INTERVENTION", borderColor = Color(0xFF9333EA)) {
                listOf(
                    "Wie ist mein Atemrhythmus? bin ich verlangsamt?",
                    "Sind meine Schultern angespannt?",
                    "Arbeite ich gerade im unkontrollierten Emotion Mind oder im kühlen Protokoll (Reasonable Mind)?",
                    "• Fallweise Gegenregulation: Wenn zwei von drei Fragen mit NEIN beantwortet werden: erst drei tiefe Atemzüge (Opposite Action), dann eintreten."
                ).forEach { q ->
                    Text("• $q", fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF581C87))
                }
            }
            Text(
                text = "„Wise Mind ist wie der Boden am Grund eines Sees – an der Oberfläche stürmische Wellen, unten immer ruhig.“ – Linehan (2015)",
                fontStyle = FontStyle.Italic,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PRAKTISCHE ANWENDUNG DER NEUROBIOLOGIE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nutzen Sie co-regulatorische Atemsynchronisation oder Team-Atemübungen direkt auf Station:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToTools("COREG_SKILLS", "BREATHING") },
                            modifier = Modifier.testTag("ch2_link_breathing"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atemcoaching starten", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: NeuroArticle) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (expanded) article.content else article.content.take(130) + "...",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expanded && article.bulletPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                article.bulletPoints.forEach { (boldText, plainText) ->
                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            text = "◆ $boldText",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = plainText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Chapter3View(onNavigateToTools: (String, String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "KAPITEL 03 · PROFESSIONELLE HALTUNG & KOMMUNIKATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Haltung & deeskalierende Gesprächsführung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kommunikation beginnt in der inneren Haltung des Klinikers. Deeskalationstechniken deeskalieren nachhaltig, wenn sie auf radikaler Akzeptanz und tieber Schamsensibilität beruhen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "KOMMUNIKATIVE GRUNDREGELN IN DER PRAXIS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val commData = listOf(
            CompactTableCardData(
                "ALT1", Color(0xFFDCFCE7), Color(0xFF166534), "Körperhaltung",
                fields = listOf(
                    "DEESKALIEREND ✓" to "Seitlich versetzt · entspannt · offene Hände sichtbar · Fluchtweg des Jugendlichen unversperrt belassen",
                    "ESKALIEREND ✗ (VERMEIDEN)" to "Frontal aufrecht · Arme verschränkt · Weg versperren · Raum dominieren"
                )
            ),
            CompactTableCardData(
                "ALT2", Color(0xFFDCFCE7), Color(0xFF166534), "Blickkontakt",
                fields = listOf(
                    "DEESKALIEREND ✓" to "Freundlich · weich · gelegentlich wegschauen bei hoher Erregung des Gegenübers",
                    "ESKALIEREND ✗ (VERMEIDEN)" to "Starren · fixierender / prüfender Blick · Blickkontakt erzwingen wollen"
                )
            ),
            CompactTableCardData(
                "ALT3", Color(0xFFDCFCE7), Color(0xFF166534), "Stimme",
                fields = listOf(
                    "DEESKALIEREND ✓" to "Tief · verlangsamt · ruhig · gleichmäßig fließend",
                    "ESKALIEREND ✗ (VERMEIDEN)" to "Laut · hoch · gepresst · scharf · sarkastisch · ironisch"
                )
            ),
            CompactTableCardData(
                "ALT4", Color(0xFFDCFCE7), Color(0xFF166534), "Körperliche Distanz",
                fields = listOf(
                    "DEESKALIEREND ✓" to "1,5 bis 2,0 Meter Abstand halten · Fluchtwege frei halten · kein Festhalten",
                    "ESKALIEREND ✗ (VERMEIDEN)" to "Nähe erzwingen · Fluchtwege blockieren · unaufgeforderte Berührungen"
                )
            )
        )

        items(commData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SCHAM ALS PRIMÄRER ESKALATIONSTREIBER",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scham ist die schmerzhafteste Emotion bei Jugendlichen. Scham-Wut-Spirale: Demütigung -> Scham unerträglich -> Abwehr durch explosive Wut. Der Trigger liegt oft 10-30 Min. zurück.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            SideBySideCards(
                title1 = "TYPISCHE SCHAM-AUSLÖSER ON STATION",
                border1 = Color(0xFFDC2626),
                items1 = listOf(
                    "Kritik, Zurechtweisung oder Grenzziehung im Beisein Dritter",
                    "Globale Anklagen like „Du hast mal wieder...“",
                    "Abgenötigte, erzwungene öffentliche Entschuldigungen",
                    "Negative Konsequenzen lautstark im Gruppenraum verkünden",
                    "Nichtbeachtung (Blickkontakt-Vermeidung, Gruß verweigern)"
                ),
                title2 = "SCHAM-SENSIBLE ALTERNATIVEN",
                border2 = Color(0xFF16A34A),
                items2 = listOf(
                    "Kritik und korrigierende Konsequenzen AUSSCHLIESSLICH im privaten Raum unter vier Augen",
                    "Faktisches Verhalten beschreiben statt die Persönlichkeit zu bewerten",
                    "Entschuldigungen freiwillig ermöglichen und reifen lassen, niemals erzwingen",
                    "Lob oder positives Feedback gern öffentlich spiegeln, Kritisches immer diskret"
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            BorderedBox(title = "VERACHTUNG ALS UNBEWUSSTER ESKALATIONSVERSTÄRKER (GOTTMAN, 1994)", borderColor = Color(0xFFF59E0B)) {
                Text(
                    text = "Verachtungssignale (Augenrollen, leises Seufzen beim Betreten des Zimmers, herablassender Tonfall) werden von Jugendlichen extrem sensibel registriert. Verachtung aktiviert dieselben Hirnareale wie körperlicher Schmerz und treibt Eskalation massiv an. Wenn im Team-Review Verachtung auffällt: Das ist ein Signal für tiefe Erschöpfung und Unterstützungsmangel, kein moralisches Urteil.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF78350F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "GEWALTFREIE KOMMUNIKATION IN DER KRISE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val gfkData = listOf(
            CompactTableCardData(
                "GFK1", Color(0xFFECEFEE), Color(0xFF475569), "1 · Beobachtung (Phase GELB)",
                fields = listOf(
                    "FORMULIERUNG" to "„Ich sehe, dass du seit einer halben Stunde am Fenster stehst und die Fäuste ballst.“",
                    "NICHT SO (VERMEIDEN)" to "„Du verhältst dich schon wieder aggressiv.“ (Globale Bewertung schürt Gegenwehr)"
                )
            ),
            CompactTableCardData(
                "GFK2", Color(0xFFECEFEE), Color(0xFF475569), "2 · Gefühl (Phase GELB)",
                fields = listOf(
                    "FORMULIERUNG" to "„Ich glaube, du bist gerade extrem aufgebracht und verzweifelt. Kann das sein?“",
                    "NICHT SO (VERMEIDEN)" to "„Du bist einfach wütend.“ (Nimmt Fremdbestimmung vor)"
                )
            ),
            CompactTableCardData(
                "GFK3", Color(0xFFECEFEE), Color(0xFF475569), "3 · Bedürfnis (Phase GELB)",
                fields = listOf(
                    "FORMULIERUNG" to "„Brauchst du im Moment einfach etwas Abstand und deine Ruhe?“",
                    "NICHT SO (VERMEIDEN)" to "Sofortige Lösungen oder Regeln aufzwingen, ohne den Kern zu kennen."
                )
            ),
            CompactTableCardData(
                "GFK4", Color(0xFFDBEAFE), Color(0xFF1E40AF), "4 · Bitte (Phase BLAU)",
                fields = listOf(
                    "FORMULIERUNG" to "„Möchtest du jetzt pfünf Minuten mit mir in den Hof gehen?“",
                    "NICHT SO (VERMEIDEN)" to "„Du musst dich jetzt beruhigen.“ ODER Scheinalternativen ohne echte Wahl."
                )
            )
        )

        items(gfkData) { data ->
            CompactTableCard(badgeText = data.badgeText, badgeBg = data.badgeBg, badgeTextCol = data.badgeTextCol, title = data.title, fields = data.fields)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "VALIDATION – DIE WIRKSAMSTE KURZINTERVENTION",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            SideBySideCards(
                title1 = "VALIDATION – SO KLINGT ES ✓",
                border1 = Color(0xFF16A34A),
                items1 = listOf(
                    "„Ich verstehe, dass du gerade extrem wütend bist.“",
                    "„Das macht total Sinn, dass dich das so aufwühlt.“",
                    "„Das klingt wirklich unerträglich belastend für dich.“",
                    "„Ich höre, wie viel dir das gerade wird.“",
                    "„Du musst das nicht alleine durchstehen.“"
                ),
                title2 = "NICHT-VALIDATION (AMYGDALA-AKTIVATOR) ✗",
                border2 = Color(0xFFDC2626),
                items2 = listOf(
                    "„Das ist doch alles nicht so schlimm.“",
                    "„Du überreagierst völlig.“",
                    "„Andere Jugendliche haben es hier auch schwer.“",
                    "„Jetzt reiß dich einfach mal zusammen.“",
                    "„Ich weiß genau, wie du dich fühlst.“"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DBT Wise Mind (Weiser Verstand)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("Emotion Mind", Color(0xFFFEF3C7), "Vollständig überflutet von Gefühlen; Gespräch verzerrt"),
                    Triple("Wise Mind ✓", Color(0xFFDCFCE7), "Integration von Gefühl & Logik; deeskalierende Präsenz"),
                    Triple("Reasonable Mind", Color(0xFFDBEAFE), "Rein rational, wirksamkeitsarm, klingt distanziert wie Protokoll")
                ).forEach { (t, c, d) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = c),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(t, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(d, fontSize = 9.sp, lineHeight = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            BorderedBox(title = "DBT OPPOSITE ACTION IN DER KRISE", borderColor = Color(0xFF7C3AED)) {
                Text(
                    text = "Der natürliche Handlungsimpuls bei aggressivem Erregungsaufbau ist: lauter werden, herantreten, frontal konfrontieren. Die DBT Gegenregulation (Opposite Action) erfordert: Stimme senken (tiefe Tonlage), Abstand vergrößern (einen Schritt zurücktreten), Stille und Schweigen aushalten. Diese Aktion muss vollständig ausgeführt werden, um wirksam zu sein.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF5B21B6)
                )
            }

            Text(
                text = "„Aggression ist oft ein Hilferuf in erlerter Sprache. Das Team, das das Verhalten vom wahren Kern trennt, deeskaliert nachhaltig.“",
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "READY-TO-USE VERBALE SCRIPTS & GFK",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Greifen Sie direkt auf anwendbare Deeskalations-Formulierungen und Gesprächs-Leitfäden für die Praxis zu:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToTools("COREG_SKILLS", "VERBAL_SCRIPTS") },
                            modifier = Modifier.testTag("ch3_link_scripts"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gesprächsscripts", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Chapter6View(
    incidentReviews: List<IncidentReview>,
    onSaveIncidentReview: (String, String, String, String, String, String, String) -> Unit,
    onDeleteIncidentReview: (Int) -> Unit,
    teamLearnings: List<TeamLearning>,
    onSaveTeamLearning: (String, String, String) -> Unit,
    onDeleteTeamLearning: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Teamkoordination & Nachbereitung",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Krisenintervention ist eine koordinierte Mannschaftsdisziplin. Verlässliche Nachbereitung im System schützt Fachkräfte vor Sekundärtraumatisierung.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(ScientificContent.teamArticles) { article ->
            ArticleCard(article = article)
        }

        // Live Workspace 1: Team-Vorkommnis Post-Incident-Reviews
        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📋 POST-INCIDENT REVIEWS (Vorkommnis-Datenbank)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Dokumentieren und reflektieren Sie kritische Situationen im Nachgang.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (isExpanded) "Schließen" else "Öffnen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        var pInitials by remember { mutableStateOf("") }
                        var incDate by remember { mutableStateOf("") }
                        var descText by remember { mutableStateOf("") }
                        var trigSource by remember { mutableStateOf("") }
                        var strengthsText by remember { mutableStateOf("") }
                        var lessonsText by remember { mutableStateOf("") }
                        var wellbeingScore by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = pInitials,
                            onValueChange = { pInitials = it },
                            label = { Text("Patienten-Initialen", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("ir_initials_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = incDate,
                            onValueChange = { incDate = it },
                            label = { Text("Datum des Vorfalls (z.B. 03.06.2026)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = descText,
                            onValueChange = { descText = it },
                            label = { Text("Genaue Situationsbeschreibung", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = trigSource,
                            onValueChange = { trigSource = it },
                            label = { Text("Auslösende Faktoren (Milieu / Intern)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = strengthsText,
                            onValueChange = { strengthsText = it },
                            label = { Text("Stärken bei Deeskalation (Was lief sofort gut?)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = lessonsText,
                            onValueChange = { lessonsText = it },
                            label = { Text("Lerneffekte (Unterschiede für das nächste Mal)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = wellbeingScore,
                            onValueChange = { wellbeingScore = it },
                            label = { Text("Befinden des Teams (Reflexion & Supervision)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (pInitials.isNotEmpty() && descText.isNotEmpty()) {
                                    onSaveIncidentReview(pInitials, incDate, descText, trigSource, strengthsText, lessonsText, wellbeingScore)
                                    pInitials = ""
                                    incDate = ""
                                    descText = ""
                                    trigSource = ""
                                    strengthsText = ""
                                    lessonsText = ""
                                    wellbeingScore = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End).testTag("save_incident_review_btn")
                        ) {
                            Text("Vorfallsnachbereitung speichern", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Bisherige Fallbesprechungen & Reviews:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (incidentReviews.isEmpty()) {
                            Text("Keine Vorfallsnachbereitungen dokumentiert.", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                        } else {
                            incidentReviews.forEach { review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Review: Patient ${review.patientInitials} (${review.incidentDate})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { onDeleteIncidentReview(review.id) },
                                                modifier = Modifier.size(24.dp).testTag("delete_incident_review_${review.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Text("• Ereignis: ${review.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("• Trigger: ${review.triggerSource}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("• Gelungene Deeskalation: ${review.teamStrengths}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("• Erkenntnisse: ${review.lessonsLearned}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("• Mitarbeiter-Fürsorge / Befinden: ${review.teamWellbeing}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Workspace 2: Team-Learnings pool (agile reflection board)
        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👥 STATIONS-REFLEXIONSBOARD (Agile Learnings)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Teilen Sie kurze, wertvolle und pragmatische Teamerfahrungen des Stationsalltags.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (isExpanded) "Schließen" else "Öffnen",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        var situation by remember { mutableStateOf("") }
                        var whatWorked by remember { mutableStateOf("") }
                        var submitterRole by remember { mutableStateOf("Pflege") }

                        OutlinedTextField(
                            value = situation,
                            onValueChange = { situation = it },
                            label = { Text("Situation / Anlassbeobachtung", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("tl_situation_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = whatWorked,
                            onValueChange = { whatWorked = it },
                            label = { Text("Was hat deeskalierend geholfen?", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Ihre Berufsgruppenrolle:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val roles = listOf("Pflege", "Arzt", "Therapeut", "Schuldienst")
                            roles.forEach { r ->
                                val isSelected = submitterRole == r
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { submitterRole = r },
                                    label = { Text(r, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("role_chip_$r")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (situation.isNotEmpty() && whatWorked.isNotEmpty()) {
                                    onSaveTeamLearning(situation, whatWorked, submitterRole)
                                    situation = ""
                                    whatWorked = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End).testTag("save_team_learning_btn")
                        ) {
                            Text("Learning hinzufügen", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Gesammelte Stationserfahrungen:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (teamLearnings.isEmpty()) {
                            Text("Es wurden noch keine Erfahrungen eingetragen.", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                        } else {
                            teamLearnings.forEach { tl ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = tl.submittedByRole,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "Erfahrungswert",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { onDeleteTeamLearning(tl.id) },
                                                modifier = Modifier.size(24.dp).testTag("delete_team_learning_${tl.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("• Situation: ${tl.situation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("• Lösungsfaktor: ${tl.whatWorked}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Chapter7View() {
    var quizStep by remember { mutableStateOf(0) } // 0 = Start, 1 = Q1, 2 = Q2, 3 = Q3, 4 = Q4, 5 = Finished
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var showCorrection by remember { mutableStateOf(false) }

    val questions = listOf(
        QuizQuestion(
            question = "1. Welche Verhaltensweise ist in der akuten Eskalationsphase (ROTE Phase) absolut kontraproduktiv?",
            options = listOf(
                "A) Ruhe ausstrahlen, Sprechtempo senken, ausreichend Körperabstand einhalten.",
                "B) Konfrontatives Diskutieren, laute Machtdemonstrationen oder physisches Bedrängen.",
                "C) Dem Jugendlichen Rückzugsorte anbieten und für Reizabschirmung sorgen."
            ),
            correctIndex = 1,
            explanation = "Konfrontation und Dominanzgebaren in der Akutphase bewirken physiologisch eine massive Bedrohungs- und Stressverstärkung und beschleunigen die Eskalation."
        ),
        QuizQuestion(
            question = "2. Welches Hauptziel verfolgt der deeskalative Krisenplan in der stabilen Phase (GRÜNE Phase)?",
            options = listOf(
                "A) Physische Sicherheitsmaßnahmen für den Notfall vorzubereiten.",
                "B) Individuelle Frühwarnzeichen und Trigger des Jugendlichen zu ermitteln sowie Beruhigungstechniken präventiv zu erproben.",
                "C) Dem Jugendlichen administrative Verhaltensregeln der Station aufzuzwingen."
            ),
            correctIndex = 1,
            explanation = "Die grüne Phase ist das Lernfenster. Hier können Trigger analysiert, Verhaltensabsprachen getroffen und Copingstrategien fehlerfrei eingeübt werden."
        ),
        QuizQuestion(
            question = "3. Was ist der Kern der professionellen 'Co-Regulation'?",
            options = listOf(
                "A) Dass sich das Personal durch die emotionale Erregung des Jugendlichen anstecken lässt, um authentischer zu wirken.",
                "B) Dass die Fachkraft durch bewusste Selbstbeherrschung (Stimme, Atem, Gestik) ein biologisches Sicherheits-Signal an das Nervensystem des Jugendlichen sendet.",
                "C) Den Jugendlichen medikamentös ruhigzustellen, bis er sich kooperativ verhält."
            ),
            correctIndex = 1,
            explanation = "Menschliche Gehirne spiegeln sich. Ein ruhiges, regulatorisch stabiles Gegenüber signalisiert dem dysregulierten Organismus Sicherheit und mindert die Angst."
        ),
        QuizQuestion(
            question = "4. Warum ist die strukturierte Post-Incident-Nachbereitung (Review) im Team unverzichtbar?",
            options = listOf(
                "A) Zur Zuweisung individueller Fehlentscheidungen im Team.",
                "B) Zur Prävention von Sekundärtraumatisierung, emotionaler Entlastung und agiler Reflexion des Interventionserfolgs.",
                "C) Ausschließlich zur rechtlichen Absicherung gegenüber Trägerverbänden."
            ),
            correctIndex = 1,
            explanation = "Nachbereitung schützt die mentale Gesundheit des Personals und überführt ungelöste Akutkrisen in geteiltes Station-Wissen (agile Fehlerkultur)."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Klinisches Standardwissen & Quick-Quiz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Diese Leitlinien basieren auf führenden Standardwerken und kognitiv-verhaltenstherapeutischen Therapiemanualen. Testen Sie spielerisch Ihr deeskalatives Wissen:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Display references dynamically
        items(ScientificContent.references) { ref ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Standardwerk",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ref,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (quizStep == 5) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.2.dp,
                    color = if (quizStep == 5) Color(0xFF86EFAC) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧠 INTERAKTIVES DEESKALATIONS-QUIZ",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (quizStep == 5) Color(0xFF15803D) else MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (quizStep == 0) {
                        Text(
                            text = "Auffrischung für Pflegekräfte, Ärzte und Therapeuten. 4 praxisrelevante Multiple-Choice-Fragen zu den Phasenmodellen.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                quizStep = 1
                                score = 0
                                selectedAnswer = null
                                showCorrection = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("start_quiz_btn")
                        ) {
                            Text("Quiz jetzt starten", fontSize = 12.sp)
                        }
                    } else if (quizStep in 1..4) {
                        val currentQ = questions[quizStep - 1]
                        Text(
                            text = "Frage $quizStep von 4:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        currentQ.options.forEachIndexed { idx, opt ->
                            val isChosen = selectedAnswer == idx
                            val chipBg = when {
                                showCorrection && idx == currentQ.correctIndex -> Color(0xFFDCFCE7)
                                showCorrection && isChosen && idx != currentQ.correctIndex -> Color(0xFFFEE2E2)
                                isChosen -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val chipBorder = when {
                                showCorrection && idx == currentQ.correctIndex -> Color(0xFF22C55E)
                                showCorrection && isChosen && idx != currentQ.correctIndex -> Color(0xFFEF4444)
                                isChosen -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !showCorrection) { selectedAnswer = idx },
                                colors = CardDefaults.cardColors(containerColor = chipBg),
                                border = BorderStroke(1.dp, chipBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isChosen,
                                        onClick = { if (!showCorrection) selectedAnswer = idx },
                                        enabled = !showCorrection
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (showCorrection) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedAnswer == currentQ.correctIndex) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (selectedAnswer == currentQ.correctIndex) "✓ Richtig gelöst!" else "✗ Falsche Antwort",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selectedAnswer == currentQ.correctIndex) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentQ.explanation,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (quizStep == 4) {
                                        quizStep = 5
                                    } else {
                                        quizStep++
                                        selectedAnswer = null
                                        showCorrection = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.End).testTag("quiz_next_btn")
                            ) {
                                Text(if (quizStep == 4) "Quiz abschließen" else "Nächste Frage", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (selectedAnswer != null) {
                                        showCorrection = true
                                        if (selectedAnswer == currentQ.correctIndex) {
                                            score++
                                        }
                                    }
                                },
                                enabled = selectedAnswer != null,
                                modifier = Modifier.align(Alignment.End).testTag("quiz_verify_btn")
                            ) {
                                Text("Bestätigen", fontSize = 12.sp)
                            }
                        }
                    } else if (quizStep == 5) {
                        Text(
                            text = "Glückwunsch! Sie haben das Quick-Quiz abgeschlossen.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "Testergebnis: $score von 4 richtigen Antworten (${(score * 100) / 4}%)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                quizStep = 0
                                score = 0
                                selectedAnswer = null
                                showCorrection = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier.fillMaxWidth().testTag("quiz_restart_btn")
                        ) {
                            Text("Quiz wiederholen", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun HandbuchSearchResultsView(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    cmsSections: List<CmsSection>,
    onNavigateToChapter: (Int) -> Unit,
    onNavigateToPhase: (String) -> Unit,
    onNavigateToDiagnosis: (String) -> Unit
) {
    val q = searchQuery.trim()
    
    val matchedPhases = ScientificContent.phases.filter {
        it.id.contains(q, ignoreCase = true) || 
        it.subtitle.contains(q, ignoreCase = true) || 
        it.summary.contains(q, ignoreCase = true) || 
        it.neuroBasics.contains(q, ignoreCase = true)
    }

    val matchedDiagnoses = ScientificContent.diagnoses.filter {
        it.id.contains(q, ignoreCase = true) || 
        it.name.contains(q, ignoreCase = true) || 
        it.dynamik.contains(q, ignoreCase = true) || 
        it.absicherung.contains(q, ignoreCase = true) || 
        it.klaerung.contains(q, ignoreCase = true) || 
        it.aufloesung.contains(q, ignoreCase = true)
    }

    val matchedNeuro = ScientificContent.neuroArticles.filter {
        it.title.contains(q, ignoreCase = true) || 
        it.content.contains(q, ignoreCase = true)
    }

    val matchedComm = ScientificContent.commArticles.filter {
        it.title.contains(q, ignoreCase = true) || 
        it.description.contains(q, ignoreCase = true)
    }

    val matchedTeam = ScientificContent.teamArticles.filter {
        it.title.contains(q, ignoreCase = true) || 
        it.content.contains(q, ignoreCase = true)
    }

    val totalMatches = matchedPhases.size + matchedDiagnoses.size + matchedNeuro.size + matchedComm.size + matchedTeam.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.testTag("clear_search_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Suche zurücksetzen")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Suchergebnisse ($totalMatches)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Echtzeit-Treffer für „$searchQuery“ in allen Standardkapiteln und Diagnosen:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (totalMatches == 0) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine passenden Übereinstimmungen gefunden.\nBitte korrigieren Sie Ihre Suche oder leeren Sie das Feld.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (matchedPhases.isNotEmpty()) {
            item {
                Text(
                    text = "PHASEN-ÜBEREINSTIMMUNGEN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(matchedPhases) { phase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Phase: ${phase.id} (${phase.subtitle})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = phase.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { onNavigateToPhase(phase.id) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Öffne Phase ${phase.id} in Kap. 4 →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (matchedDiagnoses.isNotEmpty()) {
            item {
                Text(
                    text = "DIAGNOSEN-ÜBEREINSTIMMUNGEN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(matchedDiagnoses) { diag ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = diag.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = diag.dynamik, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { onNavigateToDiagnosis(diag.id) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("In diagnostische Strategien (Kap. 5) öffnen →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (matchedNeuro.isNotEmpty()) {
            item {
                Text(
                    text = "KAPITEL 2: NEUROBIOLOGIE TREFFER",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(matchedNeuro) { art ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = art.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = art.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { onNavigateToChapter(2) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("In Kapitel 2 (Neurobiologie) öffnen →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (matchedComm.isNotEmpty()) {
            item {
                Text(
                    text = "KAPITEL 3: GESPRÄCHSFÜHRUNG TREFFER",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(matchedComm) { art ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = art.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = art.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { onNavigateToChapter(3) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("In Kapitel 3 (Kommunikation) öffnen →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (matchedTeam.isNotEmpty()) {
            item {
                Text(
                    text = "KAPITEL 6: TEAM & NACHBEREITUNG TREFFER",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(matchedTeam) { art ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = art.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = art.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { onNavigateToChapter(6) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("In Kapitel 6 (Nachbereitung) öffnen →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════
// 4. TOOLS & CLINICAL WORKSPACE SCREEN (Room DB writes)
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolsScreen(
    breathingPhase: BreathingPhase,
    breathingSeconds: Int,
    breathingCycles: Int,
    crisisPlans: List<CrisisPlan>,
    incidentReviews: List<IncidentReview>,
    teamLearnings: List<TeamLearning>,
    toolsMainTab: String,
    toolsSubTab: String,
    onToolsMainTabChange: (String) -> Unit,
    onToolsSubTabChange: (String) -> Unit,
    onStartBreathing: () -> Unit,
    onStopBreathing: () -> Unit,
    onSaveCrisisPlan: (String, String, String, String, String, String) -> Unit,
    onDeleteCrisisPlan: (Int) -> Unit,
    onSaveIncidentReview: (String, String, String, String, String, String, String) -> Unit,
    onDeleteIncidentReview: (Int) -> Unit,
    onSaveTeamLearning: (String, String, String) -> Unit,
    onDeleteTeamLearning: (Int) -> Unit
) {
    val context = LocalContext.current
    val mainTab = toolsMainTab
    val subTab = toolsSubTab

    // Assistance Alert Local State
    var assistantActive by remember { mutableStateOf(false) }
    var activeAlarmLevel by remember { mutableStateOf("GELB") }
    var alarmSecondsElapsed by remember { mutableStateOf(0) }
    var alarmRespondersCount by remember { mutableStateOf(0) }
    var collapseRequested by remember { mutableStateOf(false) }

    LaunchedEffect(assistantActive) {
        if (assistantActive) {
            alarmSecondsElapsed = 0
            alarmRespondersCount = 0
            while (true) {
                delay(1000)
                alarmSecondsElapsed++
                if (alarmSecondsElapsed == 4) alarmRespondersCount = 1
                if (alarmSecondsElapsed == 9) alarmRespondersCount = 2
                if (alarmSecondsElapsed == 15) alarmRespondersCount = 3
            }
        }
    }

    // 2-Tap Micro-Report State
    var ptInitials by remember { mutableStateOf("") }
    var selectedBehavior by remember { mutableStateOf("") }
    var selectedIntervention by remember { mutableStateOf("") }
    var selectedResult by remember { mutableStateOf("") }
    var generatedReportText by remember { mutableStateOf("") }

    // DBT Cold Shock / Temperature Timer State
    var tTimerActive by remember { mutableStateOf(false) }
    var tSecondsLeft by remember { mutableStateOf(30) }
    LaunchedEffect(tTimerActive) {
        if (tTimerActive) {
            tSecondsLeft = 30
            while (tSecondsLeft > 0) {
                delay(1000)
                tSecondsLeft--
            }
            tTimerActive = false
        }
    }

    // Sensory reduction checklist state
    var sensoryLightsDipped by remember { mutableStateOf(false) }
    var sensoryNoiseClosed by remember { mutableStateOf(false) }
    var sensoryAudienceRemoved by remember { mutableStateOf(false) }
    var sensoryDistanceMaintained by remember { mutableStateOf(false) }

    // Case Simulations State
    var currentSimIndex by remember { mutableStateOf(0) }
    var selectedSimAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var simIsSubmitted by remember { mutableStateOf(false) }

    // Mini-Quizzes State
    var quizScore by remember { mutableStateOf(0) }
    var currentQuizIndex by remember { mutableStateOf(0) }
    var selectedQuizAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var quizIsSubmitted by remember { mutableStateOf(false) }

    // Team learning addition State
    var newLearningSit by remember { mutableStateOf("") }
    var newLearningWorked by remember { mutableStateOf("") }
    var newLearningRole by remember { mutableStateOf("Pflege") }

    // SBAR Interactive Handover Protocol State
    var sbarSituation by remember { mutableStateOf("") }
    var sbarBackground by remember { mutableStateOf("") }
    var sbarAssessment by remember { mutableStateOf("GELB") }
    var sbarRecommendation by remember { mutableStateOf("") }
    var sbarGeneratedText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Klinische Praxis & Co-Regulation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Interaktives Station-Zentrum für Co-Regulation, Teamalarme, 2-Tap Patientenberichte und Didaktik.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // --- PRIMARY CATEGORY TABS ---
        item {
            ScrollableTabRow(
                selectedTabIndex = when (mainTab) {
                    "COREG_SKILLS" -> 0
                    "TEAM_STATION" -> 1
                    "LEARN_REVIEWS" -> 2
                    else -> 0
                },
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).fillMaxWidth()
            ) {
                Tab(
                    selected = mainTab == "COREG_SKILLS",
                    onClick = { onToolsMainTabChange("COREG_SKILLS") },
                    text = { Text("Co-Reg & Skills", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = mainTab == "TEAM_STATION",
                    onClick = { onToolsMainTabChange("TEAM_STATION") },
                    text = { Text("Team & Station", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = mainTab == "LEARN_REVIEWS",
                    onClick = { onToolsMainTabChange("LEARN_REVIEWS") },
                    text = { Text("Reviews & Lernen", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // --- SECONDARY Pills SubSelector Row ---
        item {
            SingleLineFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val subTabOptions = when (mainTab) {
                    "COREG_SKILLS" -> listOf(
                        Pair("BREATHING", "Atemcoaching"),
                        Pair("DBT_SENSORY", "DBT & Sensorik"),
                        Pair("VERBAL_SCRIPTS", "Gesprächsscripts")
                    )
                    "TEAM_STATION" -> listOf(
                        Pair("ASSIST_ALERT", "Team-Assist Alert"),
                        Pair("MICRO_REPORT", "2-Tap Bericht"),
                        Pair("UNIT_INFO", "Pflegeprozedere & Kontakte"),
                        Pair("CRISIS_PLANS", "Krisenpläne (DB)")
                    )
                    "LEARN_REVIEWS" -> listOf(
                        Pair("CASE_SIMS", "Fallsimulation"),
                        Pair("QUIZZES", "Wissensquiz"),
                        Pair("TEAM_LEARNING", "Team learning (DB)"),
                        Pair("INCIDENT_REVIEWS", "Review-Archiv")
                    )
                    else -> emptyList()
                }

                subTabOptions.forEach { (tabId, label) ->
                    val isSelected = subTab == tabId
                    Button(
                        onClick = { onToolsSubTabChange(tabId) },
                        modifier = Modifier.testTag("subtab_$tabId"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- CONTENT SCREEN DIRECT ROUTING ---
        when (subTab) {
            "BREATHING" -> {
                // PACED BREATHING COMPONENT WITH VERBAL CUES
                item {
                    BreathingGuideComponent(
                        breathingPhase = breathingPhase,
                        breathingSeconds = breathingSeconds,
                        breathingCycles = breathingCycles,
                        onStartBreathing = onStartBreathing,
                        onStopBreathing = onStopBreathing
                    )
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Co-Regulativer Audio-/Visueller Taktgeber", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nutzen Sie ein tiefes Sprechtempo und senken Sie Ihre Stimmlage synchron zur Ausatmungsphase ab (Pulsierender Ring).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            "DBT_SENSORY" -> {
                // DBT SKILLS (STOP, TIPP) & SENSORY TOOLKITS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "DBT TIPP-SKILL: Kälte / Temperatur", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ein plötzlicher Kältereiz im Gesicht (z.B. Eispack oder eiskaltes Wasser) löst sekundenschnell den Tauchreflex (Mammalian Dive Reflex) aus. Dies verlangsamt die Herzfrequenz sympathomimetisch, senkt extreme Anspannung schlagartig und stellt den Wise Mind her.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { tTimerActive = !tTimerActive },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (tTimerActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(if (tTimerActive) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (tTimerActive) "Skill abbrechen" else "Eispack-Timer starten (30s)")
                                }

                                if (tTimerActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$tSecondsLeft",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Sensory De-escalation & Reizreduktion", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Die Reduzierung externer Stressfaktoren schützt das Nervensystem in Phase GELB vor dem Kippen in Phase ROT (Amygdala-Hijack).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("STANZ-REIZREDUKTIONS-CHECKLISTE:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sensoryLightsDipped, onCheckedChange = { sensoryLightsDipped = it })
                                    Text("Beleuchtung dimmen / Deckenlichter ausschalten", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sensoryNoiseClosed, onCheckedChange = { sensoryNoiseClosed = it })
                                    Text("Fenster schließen, Lärmpegel dämpfen", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sensoryAudienceRemoved, onCheckedChange = { sensoryAudienceRemoved = it })
                                    Text("Zuschauer & andere Patienten aus dem Raum weisen", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = sensoryDistanceMaintained, onCheckedChange = { sensoryDistanceMaintained = it })
                                    Text("Seitlicher Abstand von 1,5m bis 2m dauerhaft gewahrt", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("SENSORY-KIT INVENTAR (Schrankpflege):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("• Gewichtsdecke (Schwere Decke für propriozeptiven Druck)\n• Rauschunterdrückende Kopfhörer (Noise-Cancelling)\n• Igelbälle, Stressbälle (Taktile Fokussierung)\n• Ätherische Öle (Olfaktorischer Reiz: Lavendel oder Ammoniak für Hochspannung)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            "VERBAL_SCRIPTS" -> {
                // VERBAL PHRASES & SCRIPTS FOR CRITICAL CLINICAL ENGAGEMENTS
                item {
                    Text("Deeskalation-Gesprächsscripts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                val verbalPhrases = listOf(
                    Triple(
                        "Extremer Erregungszustand",
                        "Für Jugendliche mit traumatischer Disregulation oder heftiger Panik.",
                        listOf(
                            "„Ich bin hier bei dir. Du bist auf Station und in Sicherheit.“",
                            "„Es ist gerade extrem viel für dich – wir atmen jetzt zusammen ein... und aus.“",
                            "„Ich tue dir nichts und ich weiche nicht zurück. Du schaffst das.“"
                        )
                    ),
                    Triple(
                        "Konfliktsituation & Abgrenzung",
                        "Zur Deeskalation von verbaler Feindseligkeit in Phase GELB.",
                        listOf(
                            "„Ich sehe, dass du kochend vor Wut bist. Lass uns in ein ruhiges Zimmer gehen und das klären.“",
                            "„Ich möchte dich verstehen, aber ich brauche, dass du in einem ruhigeren Ton mit mir sprichst.“",
                            "„Es geht mir nicht darum, dich zu bestrafen, sondern ich will, dass du sicher bist.“"
                        )
                    ),
                    Triple(
                        "Dissoziatives Abdriften",
                        "Bei einsetzender Starre oder Abwesenheit (Präventionsreiz).",
                        listOf(
                            "„Sprich mir nach: Ich stehe fest auf dem Boden...“",
                            "„Drücke deine Füße fest in den Boden. Spürst du die Fliesen?“",
                            "„Wenn du mich hören kannst, ball deine Hände ganz fest zusammen und lass wieder los.“"
                        )
                    )
                )

                items(verbalPhrases) { (title, subtitle, phrases) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            phrases.forEach { phrase ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Deeskalationsscript", phrase)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Satz in Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = phrase, fontSize = 11.sp, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }
            "ASSIST_ALERT" -> {
                // NEED ASSISTANCE EMERGENCY ALERT PANEL
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (assistantActive) {
                                when (activeAlarmLevel) {
                                    "ROT" -> Color(0xFFFEE2E2)
                                    "ORANGE" -> Color(0xFFFFEDD5)
                                    else -> Color(0xFFFEF3C7)
                                }
                            } else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (assistantActive) 2.dp else 1.dp,
                            color = if (assistantActive) {
                                when (activeAlarmLevel) {
                                    "ROT" -> Color(0xFFEF4444)
                                    "ORANGE" -> Color(0xFFF97316)
                                    else -> Color(0xFFF59E0B)
                                }
                            } else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (assistantActive) Icons.Default.Warning else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (assistantActive) Color(0xFFB91C1C) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (assistantActive) "TEAM-NOTRUF AKTIViert!" else "Team-Assistenz anfordern",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (assistantActive) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Der mobile Notruf simuliert eine verschlüsselte Peer-to-Peer Wlan-Notfallkette an das Stationspersonal. Sichert unaufgeregtes Nachrücken an den Krisenort.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!assistantActive) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("ALARMSTUFE AUSWÄHLEN:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            activeAlarmLevel = "GELB"
                                            assistantActive = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        modifier = Modifier.weight(1f).testTag("alert_yellow")
                                    ) {
                                        Text("Gelb (Support)", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            activeAlarmLevel = "ORANGE"
                                            assistantActive = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                        modifier = Modifier.weight(1f).testTag("alert_orange")
                                    ) {
                                        Text("Orange (Co-Reg)", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            activeAlarmLevel = "ROT"
                                            assistantActive = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        modifier = Modifier.weight(1f).testTag("alert_red")
                                    ) {
                                        Text("ROT (NOTRUF)", fontSize = 10.sp)
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Typ: Alarmstufe $activeAlarmLevel", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7F1D1D))
                                            Text("Aktiv seit: $alarmSecondsElapsed Sek.", fontSize = 12.sp, color = Color(0xFF991B1B))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (alarmRespondersCount == 0) "Suche freie Einheiten..." else "$alarmRespondersCount Kollegen haben bestätigt und eilen herbei!",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF14532D)
                                            )
                                        }

                                        if (alarmSecondsElapsed >= 15) {
                                            Text("• Dr. Becker (Oberarzt-Dienst) wurde per Krisentelefon hinzugerufen.", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { collapseRequested = !collapseRequested },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (collapseRequested) Color(0xFF1D4ED8) else Color(0xFF6B7280)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (collapseRequested) "Ablösung gerufen!" else "Sprecher ablösen", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            assistantActive = false
                                            collapseRequested = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                        modifier = Modifier.weight(1f).testTag("dismiss_alarm")
                                    ) {
                                        Text("Entwarnung / Reset", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "MICRO_REPORT" -> {
                // 2-TAP MICRO-REPORT TOOL
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "2-Tap Mikro-Bericht-Dokumentation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Akutkräfte haben wenig Zeit. Tippen Sie das Verhalten des Jugendlichen und Ihre angewandte Deeskalations-Intervention an. Das Tool kompiliert sofort einen professionellen, DSGVO-konformen Verlaufsbericht für die Pflegedokumentation.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = ptInitials,
                                onValueChange = { ptInitials = it },
                                label = { Text("Kürzel Jugendliche(r) (Optional, z.B. J.K.)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("1. VERHALTEN / AUSGANGSSITUATION (1-Tap):", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "Starke innere Anspannung & unruhige Gereiztheit (GELB)",
                                    "Verbale Drohung und Grenzüberschreitung",
                                    "Dissoziative Erstarrung & Rückzug (ROT)",
                                    "Körperliche Aggression gegen Inventar",
                                    "Scham-Abwehr & laute Provokation"
                                ).forEach { behavior ->
                                    val isSel = selectedBehavior == behavior
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedBehavior = behavior }
                                            .testTag("report_behavior_${behavior.take(4).lowercase()}"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    ) {
                                        Text(text = behavior, fontSize = 11.sp, modifier = Modifier.padding(8.dp), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("2. INTERVENTION DER AKUTKRAFT (2-Tap):", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "Validation und schamsensibles GFK-Gespräch",
                                    "Co-regulatives Atmen & Stimmabsenkung",
                                    "Reizreduktion und physische Distanzgewährung",
                                    "Anleitung DBT STOP & TIPP Kältereiz (Eispack)",
                                    "Regulationsangebot über Sensory-Kit (Gewichtsdecke)"
                                ).forEach { inter ->
                                    val isSel = selectedIntervention == inter
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedIntervention = inter }
                                            .testTag("report_inter_${inter.take(4).lowercase()}"),
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    ) {
                                        Text(text = inter, fontSize = 11.sp, modifier = Modifier.padding(8.dp), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("3. KLINISCHES ERGEBNIS:", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    "Erfolgreich co-reguliert, Jugendliche(r) in Phase GRÜN",
                                    "Situation beruhigt, engmaschige Begleitung fortgesetzt",
                                    "Arzt zur diagnostischen Klärung hinzugerufen"
                                ).forEach { res ->
                                    val isSel = selectedResult == res
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedResult = res },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    ) {
                                        Text(text = res, fontSize = 11.sp, modifier = Modifier.padding(8.dp), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                    val init = if (ptInitials.isNotBlank()) "bei Jugendliche(r) ($ptInitials)" else "beim Jugendlichen"
                                    generatedReportText = "Doku $timeStr Uhr: Akute Krise $init.\nReaktion: $selectedBehavior.\nKlinische Deeskalation: $selectedIntervention.\nVerlauf: $selectedResult. Keine freiheitsentziehenden Maßnahmen nötig."
                                },
                                enabled = selectedBehavior.isNotEmpty() && selectedIntervention.isNotEmpty() && selectedResult.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().testTag("compile_microreport")
                            ) {
                                Text("Berichtsbaustein generieren")
                            }

                            if (generatedReportText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("GENERIERTER VERLAUFSBERICHT:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = generatedReportText,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Patientenbericht", generatedReportText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Doku-Text in Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Eintrag kopieren")
                                }
                            }
                        }
                    }
                }
            }
            "UNIT_INFO" -> {
                // PROCEDURAL CLINICAL PROCEDURES, CONTACT DIRECTORY & SBAR HANDOVER GUIDE
                item {
                    Text("Interne Station-Info & Prozedere", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ZWANGSVERMEIDUNGS- & SCHUTZ-PROTOKOLL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Äußerste Ratio: Freiheitsentziehende Maßnahmen (Fixierung, Isolierung) sind die absolute Ausnahme bei unmittelbarer Eigen-/Fremdgefährdung.\n" +
                                        "2. Genehmigung: Ärztliche Anordnung unverzüglich einholen.\n" +
                                        "3. Dauer-Sitzwache: 1-to-1 Begleitung durch Pflegekraft unterbrochen herstellen.\n" +
                                        "4. Vitalwerte: Alle 15 Minuten Puls, Atmung, Bewusstseinslage messen und dokumentieren.\n" +
                                        "5. Debriefing: Nach jeder Zwangsmaßnahme Nachbesprechung mit Patient und Team innerhalb von 24 Std.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("sbar_interactive_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "SBAR-ÜBERGABEKARTE (Strukturierte Visite & Handover)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Füllen Sie die Abschnitte aus, um ein professionelles Übergabeprotokoll zu kompilieren.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // S - Situation
                            OutlinedTextField(
                                value = sbarSituation,
                                onValueChange = { sbarSituation = it },
                                label = { Text("S – Situation (Wer ist betroffen? Akuter Zustand?)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("sbar_input_situation"),
                                placeholder = { Text("z.B. Patient J.H. zeigt stark ansteigende Erregung") },
                                singleLine = false,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // B - Background
                            OutlinedTextField(
                                value = sbarBackground,
                                onValueChange = { sbarBackground = it },
                                label = { Text("B – Background (Hintergrund? Trauma? Trigger?)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("sbar_input_background"),
                                placeholder = { Text("z.B. Bekanntes Trauma & ADHS. Reagiert stark auf laute, direkte Ansprache") },
                                singleLine = false,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // A - Assessment (Phasen-Auswahl Buttons!)
                            Text("A – Assessment (Aktuelle Phase auswerten):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("GRÜN", "GELB", "ROT", "BLAU", "WEISS").forEach { phase ->
                                    val isSel = sbarAssessment == phase
                                    val color = when (phase) {
                                        "GRÜN" -> Color(0xFF16A34A)
                                        "GELB" -> Color(0xFFD97706)
                                        "ROT" -> Color(0xFFDC2626)
                                        "BLAU" -> Color(0xFF2563EB)
                                        "WEISS" -> Color(0xFF7C3AED)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(1.dp, if (isSel) color else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                            .clickable { sbarAssessment = phase }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = phase,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // R - Recommendation
                            OutlinedTextField(
                                value = sbarRecommendation,
                                onValueChange = { sbarRecommendation = it },
                                label = { Text("R – Recommendation (Welcher Plan gilt? Trigger vermeiden?)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("sbar_input_rec"),
                                placeholder = { Text("z.B. Co-regulatives Atmen anbieten, Reizreduktion über Sensory-Kit einleiten") },
                                singleLine = false,
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val sitVal = sbarSituation.ifBlank { "Keine akute Bedrohung eingetragen" }
                                    val bgVal = sbarBackground.ifBlank { "Keine Trauma-Anamnese spezifiziert" }
                                    val recVal = sbarRecommendation.ifBlank { "Engmaschige non-verbale Präsenz" }
                                    sbarGeneratedText = """
                                        [SBAR HANDOVER]
                                        S (Situation): $sitVal
                                        B (Hintergrund): $bgVal
                                        A (Einschätzung): Phase $sbarAssessment aktiv
                                        R (Empfehlung): $recVal
                                    """.trimIndent()
                                    Toast.makeText(context, "SBAR Übergabe wurde erfolgreich generiert!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().testTag("sbar_compile_button")
                            ) {
                                Text("Visitenbaustein generieren", fontSize = 12.sp)
                            }

                            if (sbarGeneratedText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "STRUKTURIERTER ÜBERGABE-TEXT:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sbarGeneratedText,
                                            fontSize = 11.sp,
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("SBAR Übergabe", sbarGeneratedText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "SBAR Übergabeprotokoll kopiert!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("sbar_copy_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Text("Protokoll in Zwischenablage kopieren", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ANSPRECHPARTNER & KLINISCHE NUMMERN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• KJP Arzt (Dienstzeit-Zentrale): Tel: 3012\n" +
                                    "• Oberarzt-Krisentelefon (Harburg): Tel: 3044\n" +
                                    "• Kriseninterventionsdienst (Mobil): Tel: 9110\n" +
                                    "• Pflegedirektion Akutstation: Tel: 4402\n" +
                                    "• Kälte-Eispack & Sensory-Zubehör: Dienstzimmer-Kühlschrank 1. Stock / Schrank 2A",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            "CRISIS_PLANS" -> {
                // CRISIS PLANS MANAGEMENT (Room DB integration)
                item {
                    CrisisPlanWorkspaceSection(
                        crisisPlans = crisisPlans,
                        onSaveCrisisPlan = onSaveCrisisPlan,
                        onDeleteCrisisPlan = onDeleteCrisisPlan
                    )
                }
            }
            "CASE_SIMS" -> {
                // STRATEGIC SCENARIO DECISION PATH TRAINING
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Fall-Szenario-Entscheidungs-Training", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lösen Sie reale Akutfälle spielerisch. Ihre Entscheidungspfade deeskalieren oder verschlimmern die Erregung aus neurobiologischer Sicht.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Case Scenario Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Szenario ${currentSimIndex + 1} von 2", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                TextButton(onClick = {
                                    currentSimIndex = (currentSimIndex + 1) % 2
                                    selectedSimAnswerIndex = null
                                    simIsSubmitted = false
                                }) {
                                    Text("Nächstes Szenario →", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val currentScenario = if (currentSimIndex == 0) {
                                // Scenario 1
                                Triple(
                                    "Frühwarnung bei Autismus (ASS) u18",
                                    "Ein 15-jähriger Junge mit ASS steht vor dem verschlossenen Stationskiosk, schreit hysterisch und tritt aggressiv gegen das Gitter, weil er seine vertraute Limonadenmarke heute nicht erhält:",
                                    listOf(
                                        "A) Ihm ruhig und bestimmt erklären, dass die Kioskzeit abgelaufen ist und die Hausordnung für alle gleich gilt.",
                                        "B) Die Limonade sofort herausgeben, um die Aggression abzufangen, selbst wenn es regelfreie Ausnahmen erzeugt.",
                                        "C) Seine sensorische Überreizung validieren, den Fluchtweg unversperrt seitlich absichern, Reizquellen dämpfen und ihm eine visuell strukturierte Alternative anbieten."
                                    )
                                )
                            } else {
                                // Scenario 2
                                Triple(
                                    "EIPS Krisenregulation am Gang",
                                    "Eine 16-jährige Patientin mit EIPS (GELB) läuft weinend über den Stationsgang und schlägt sich leicht den Kopf an die Wand. Ein Kollege herrscht sie im Gang lautstark an, sofort in ihr Zimmer zu verschwinden:",
                                    listOf(
                                        "A) Sich lautstark einmischen und den Kollegen vor der Gruppe kritisieren, um die Patientin zu schützen.",
                                        "B) Die Patientin ruhig ansprechen, sie diskret in ein ruhiges Therapiezimmer begleiten, Gefühle validieren und ihr ein DBT TIPP Eispack-Kohlereiz anbieten.",
                                        "C) Den Kollegen gewähren lassen, da beziehungsorientierte Zuwendung in diesem Moment das selbstverletzende Verhalten ungewollt verstärken würde."
                                    )
                                )
                            }

                            Text(text = currentScenario.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = currentScenario.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 17.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            currentScenario.third.forEachIndexed { idx, ans ->
                                val isSelected = selectedSimAnswerIndex == idx
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (!simIsSubmitted) {
                                                selectedSimAnswerIndex = idx
                                            }
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(text = ans, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                                }
                            }

                            if (selectedSimAnswerIndex != null && !simIsSubmitted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { simIsSubmitted = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Entscheidung pathologische Auswertung")
                                }
                            }

                            if (simIsSubmitted) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val explanation = if (currentSimIndex == 0) {
                                    when (selectedSimAnswerIndex) {
                                        0 -> "❌ Nicht optimal! Bei ASS blockiert extremer Stress die rationale Informationsverarbeitung. Das Beharren auf Paragraphen (Hausordnung) wirkt wie Verachtung und triggert massiven Amygdala Hijack."
                                        1 -> "⚠️ Nur bedinkter Teilerfolg! Das sofortige Nachgeben verhindert zwar die Eskalation, trainiert jedoch unbewusst instrumentelle Gewalt und begünstigt Splitting-Dynamiken auf Station."
                                        else -> "✓ Ausgezeichnete Deeskalation! Gefühlsvalidation nimmt die Not an. Durch die sensorische Abkühlung (Reizreduktion) und das visuelle Angebot wird der ventrale Vagus reaktiviert."
                                    }
                                } else {
                                    when (selectedSimAnswerIndex) {
                                        0 -> "❌ Fatal! Das Kritisieren des Kollegen spaltet das Therapeutenteam (Splitting-Falle). Dies verstärkt bei EIPS das Gefühl unzuverlässiger Bindungsgrenzen."
                                        1 -> "✓ Großartig reguliert! Die Verlegung in ein ruhiges Zimmer verhindert die schambeladene Flureskalation vor Dritten. Das Eispack-Kälte-TIPP-Angebot dämpft physiologische Übererregung augenblicklich."
                                        else -> "❌ Vorsicht! Die Annahme, dass emotionale Notfälle bei EIPS reine Manipulation zur Belohnung sind, ist neurobiologisch überholt. Unsichere Bindungsmuster brauchen feinfühlige Haltepunkte."
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("FEEDBACK & ERKLÄRUNG:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = explanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "QUIZZES" -> {
                // MINI-QUIZZES FOR RETENTION CHECK
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Deeskalations-Wissens-Check", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prüfen Sie Ihr Fachwissen über Safewards-Konzepte, Polyvagal-Theorie, GFK und DBT.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Frage ${currentQuizIndex + 1} von 3", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Punkte: $quizScore", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val quizQuestions = listOf(
                                Triple(
                                    "Wie lange dauert die biologische 'Kortisol-Latenz' nach heftigen emotionalen Krisen (KJP)?",
                                    listOf(
                                        "A) Ca. 2-5 Minuten, danach ist das System sofort wieder neutral",
                                        "B) Mindestens 20-60 Minuten, in denen Verhandlungen wegen Re-Eskalationsgefahr warten müssen",
                                        "C) Exakt 24 Stunden, in denen der Patient das Zimmer nicht verlassen sollte"
                                    ),
                                    1 // Correct Answer index
                                ),
                                Triple(
                                    "Welchen physiologischen Effekt bewirkt das DBT TIPP Temperatur-Element (Eispack im Gesicht)?",
                                    listOf(
                                        "A) Triggert den Tauchreflex, der den Puls augenblicklich drosselt",
                                        "B) Es erzeugt langanhaltende Schmerzen zur Reizüberlagerung",
                                        "C) Es blockiert die visuelle Perceptual Narrowing Seh-Einschränkung"
                                    ),
                                    0 // Correct index
                                ),
                                Triple(
                                    "Wie sollte Kritik an Jugendlichen geäußert werden, um extreme Scham-Wut-Spiralen abzufangen?",
                                    listOf(
                                        "A) Unmittelbar auf dem Gang, um ein klares Statement für alle Akteure zu setzen",
                                        "B) Unter vier Augen in einem separaten Rückzugsraum",
                                        "C) Durch konsequentes Ignorieren über den restlichen Tag"
                                    ),
                                    1 // Correct index
                                )
                            )

                            val qObj = quizQuestions[currentQuizIndex]
                            Text(text = qObj.first, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            qObj.second.forEachIndexed { index, ansOption ->
                                val isSelected = selectedQuizAnswerIndex == index
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (!quizIsSubmitted) {
                                                selectedQuizAnswerIndex = index
                                            }
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(text = ansOption, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                                }
                            }

                            if (selectedQuizAnswerIndex != null && !quizIsSubmitted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        quizIsSubmitted = true
                                        if (selectedQuizAnswerIndex == qObj.third) {
                                            quizScore += 10
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("submit_quiz_answer")
                                ) {
                                    Text("Antwort überprüfen")
                                }
                            }

                            if (quizIsSubmitted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val isCorrect = selectedQuizAnswerIndex == qObj.third
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isCorrect) "✓ Richtig gelöst!" else "✗ Leider falsch!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "Wissenschaftlicher Hintergrund: " + if (currentQuizIndex == 0) {
                                                "Kortisol flutet im Blut extrem träge ab. Wer zu früh klärt, re-eskaliert unfreiwillig, da die Amygdala hochsensibel geschaltet bleibt."
                                            } else if (currentQuizIndex == 1) {
                                                "Der parasympathische Vagus-Ast wird durch Kälte-Thermorezeptoren getriggert – senkt die vegetative Grundspannung."
                                            } else {
                                                "Scham ist die schmerzhafteste Emotion. Kritik vor Dritten vernichtet das Selbstwertgefühl und mündet fast immer in Gegenangriffe."
                                            },
                                            fontSize = 10.sp,
                                            color = if (isCorrect) Color(0xFF14532D) else Color(0xFF7F1D1D)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (currentQuizIndex < 2) {
                                            currentQuizIndex++
                                        } else {
                                            currentQuizIndex = 0
                                            quizScore = 0
                                        }
                                        selectedQuizAnswerIndex = null
                                        quizIsSubmitted = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (currentQuizIndex < 2) "Nächste Frage" else "Quiz neustarten")
                                }
                            }
                        }
                    }
                }
            }
            "TEAM_LEARNING" -> {
                // TEAM LEARNING SYSTEM ("WAS HAT FUNKTIONIERT?")
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Kollegiales Best-Practice Board", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Was hat in einer eskalativen Situation funktioniert? Teilen Sie Ihre Erfahrungen unaufgeregt und offline-verfügbar für das gesamte Team.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newLearningSit,
                                onValueChange = { newLearningSit = it },
                                label = { Text("Situation / Auslöser (z.B. Visite ADHS)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("learning_input_situation")
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newLearningWorked,
                                onValueChange = { newLearningWorked = it },
                                label = { Text("Was hat geholfen? (z.B. Timebox visuell gestellt)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("learning_input_worked")
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rolle:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                listOf("Pflege", "Arzt", "Therapeut").forEach { role ->
                                    val isSel = newLearningRole == role
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { newLearningRole = role },
                                        label = { Text(role, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onSaveTeamLearning(newLearningSit, newLearningWorked, newLearningRole)
                                    newLearningSit = ""
                                    newLearningWorked = ""
                                    Toast.makeText(context, "Erfahrung kollegial geteilt!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = newLearningSit.isNotBlank() && newLearningWorked.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("save_team_learning_button")
                            ) {
                                Text("Auf Board posten")
                            }
                        }
                    }
                }

                if (teamLearnings.isEmpty()) {
                    item {
                        Text("Moderierte Best-Practices der Station:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    val defaultPractices = listOf(
                        Triple("ASS Reizüberflutung", "Sinnvoll: Gewichtsdecke und Kopfhörer im Entspannungsraum unaufgefordert anbieten.", "Pflege"),
                        Triple("ADHS Entladung", "Sinnvoll: Klare, visuelle Sanduhr einsetzen und Bewegungsalternativen im KJP-Schulhof erlauben.", "Therapeut"),
                        Triple("EIPS Dissoziation", "Sinnvoll: Kältesensoren aktivieren (Eispack im Nacken) statt kognitiven Dialogen.", "Arzt")
                    )
                    items(defaultPractices) { (sit, helped, role) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Situation: $sit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text(text = role, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Hilfreich: $helped", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item {
                        Text("Kollegiale Beiträge (${teamLearnings.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    items(teamLearnings) { learning ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Klient/Sit: ${learning.situation}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text(text = learning.submittedByRole, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { onDeleteTeamLearning(learning.id) },
                                            modifier = Modifier.size(24.dp).testTag("delete_learning_${learning.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Hilfreich: ${learning.whatWorked}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            "INCIDENT_REVIEWS" -> {
                // ARCHIVED POST-INCIDENT REVIEWS (Room DB integration)
                item {
                    IncidentReviewWorkspaceSection(
                        reviews = incidentReviews,
                        onSaveIncidentReview = onSaveIncidentReview,
                        onDeleteIncidentReview = onDeleteIncidentReview
                    )
                }
            }
        }
    }
}

@Composable
fun SingleLineFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Elegant fall-back container to layout children horizontally scrollable on mobile
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
        Spacer(modifier = Modifier.width(20.dp)) // horizontal breathing space
    }
}


// ══════════════════════════════════════════════════════
// paced breathing co-regulation guide
// ══════════════════════════════════════════════════════
@Composable
fun BreathingGuideComponent(
    breathingPhase: BreathingPhase,
    breathingSeconds: Int,
    breathingCycles: Int,
    onStartBreathing: () -> Unit,
    onStopBreathing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("breathing_guide_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Paced Breathing Co-Regulation",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aktiviert den ventralen Vagus parasympathisch über ein verlangsamtes Atemverhältnis (4s Einatmen, 8s Ausatmen). Perfekt als Co-Regulations-Modell.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated breathing circle scaling
            val scale by animateFloatAsState(
                targetValue = when (breathingPhase) {
                    BreathingPhase.INHALE -> 1.5f
                    BreathingPhase.EXHALE -> 0.8f
                    BreathingPhase.IDLE -> 1.0f
                },
                animationSpec = tween(
                    durationMillis = when (breathingPhase) {
                        BreathingPhase.INHALE -> 4000
                        BreathingPhase.EXHALE -> 8000
                        BreathingPhase.IDLE -> 300
                    },
                    easing = LinearEasing
                ),
                label = "breathingScale"
            )

            val circleColor by animateColorAsState(
                targetValue = when (breathingPhase) {
                    BreathingPhase.INHALE -> Color(0xFF93C5FD) // soft blue
                    BreathingPhase.EXHALE -> Color(0xFF86EFAC) // calm green
                    BreathingPhase.IDLE -> MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(300),
                label = "circleColor"
            )

            // Infinite smooth pulsing concentric ring for immersive somatic guidance
            val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
            val pulseSizeOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseSizeOffset"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.08f,
                targetValue = 0.28f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // outer infinite pulsated halo background
                if (breathingPhase != BreathingPhase.IDLE) {
                    Box(
                        modifier = Modifier
                            .size((110 * scale + pulseSizeOffset).dp)
                            .clip(CircleShape)
                            .background(circleColor.copy(alpha = pulseAlpha))
                    )
                }

                // outer pulsed background
                Box(
                    modifier = Modifier
                        .size((110 * scale).dp)
                        .clip(CircleShape)
                        .background(circleColor.copy(alpha = 0.25f))
                )

                // inner solid target
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (breathingPhase != BreathingPhase.IDLE) {
                            Text(
                                text = "$breathingSeconds",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sekunden",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text Cues & Metronome helper text
            Text(
                text = when (breathingPhase) {
                    BreathingPhase.INHALE -> "EINATMEN... (Füllen)"
                    BreathingPhase.EXHALE -> "AUSATMEN... (Entspannen und senken)"
                    BreathingPhase.IDLE -> "Bereit zur Co-Regulation"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = when (breathingPhase) {
                    BreathingPhase.INHALE -> Color(0xFF1E40AF)
                    BreathingPhase.EXHALE -> Color(0xFF166534)
                    BreathingPhase.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (breathingPhase != BreathingPhase.IDLE) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zyklusanzahl: $breathingCycles geklappt",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (breathingPhase == BreathingPhase.IDLE) {
                    Button(
                        onClick = onStartBreathing,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("start_breathing_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Rhythmus starten", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStopBreathing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .testTag("stop_breathing_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Anhalten / Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// Patient Crisis Plan section
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisPlanWorkspaceSection(
    crisisPlans: List<CrisisPlan>,
    onSaveCrisisPlan: (String, String, String, String, String, String) -> Unit,
    onDeleteCrisisPlan: (Int) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }

    var initials by remember { mutableStateOf("") }
    var selectedDiagId by remember { mutableStateOf("EIPS") }
    var triggerText by remember { mutableStateOf("") }
    var warningText by remember { mutableStateOf("") }
    var calmingText by remember { mutableStateOf("") }
    var worseningText by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Individuelle Patienten-Krisenpläne",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showForm = !showForm },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("toggle_crisis_form_button")
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showForm) "Schließen" else "Neu erstellen", fontSize = 11.sp)
            }
        }

        if (showForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crisis_plan_form_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Krisenplan eintragen (WEISS/GRÜN Interaktion)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = initials,
                        onValueChange = { initials = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_initials"),
                        placeholder = { Text("Patienten-Kürzel (z.B. A.B.)") },
                        label = { Text("Initiale / Kürzel") },
                        singleLine = true
                    )

                    // Diagnostic selector input
                    Text(text = "Hauptdiagnose auswählen:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ADHS", "EIPS", "PTBS", "ASS", "Psychose").forEach { item ->
                            val active = selectedDiagId == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { selectedDiagId = item }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = triggerText,
                        onValueChange = { triggerText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_trigger"),
                        placeholder = { Text("z.B. Körperkontakt, grelles Licht, Ungewissheit...") },
                        label = { Text("Individuelle Trigger") }
                    )

                    OutlinedTextField(
                        value = warningText,
                        onValueChange = { warningText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_warning_signs"),
                        placeholder = { Text("z.B. ballt Fäuste, zieht Kapuze auf, atmet schnell...") },
                        label = { Text("Frühwarnzeichen (Phase GELB)") }
                    )

                    OutlinedTextField(
                        value = calmingText,
                        onValueChange = { calmingText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_calming"),
                        placeholder = { Text("z.B. Paced Breathing, Skill-Box, Rückzug...") },
                        label = { Text("Präferenz zur Co-Regulation") }
                    )

                    OutlinedTextField(
                        value = worseningText,
                        onValueChange = { worseningText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_worsening"),
                        placeholder = { Text("z.B. Festhalten, Diskutieren, Gruppe belehren...") },
                        label = { Text("Was die Krise verschlimmert (Absolute Don'ts)") }
                    )

                    Button(
                        onClick = {
                            if (initials.trim().isEmpty() || triggerText.trim().isEmpty()) {
                                // show simple toast if fields empty
                            } else {
                                onSaveCrisisPlan(
                                    initials,
                                    selectedDiagId,
                                    triggerText,
                                    warningText,
                                    calmingText,
                                    worseningText
                                )
                                // clear
                                initials = ""
                                triggerText = ""
                                warningText = ""
                                calmingText = ""
                                worseningText = ""
                                showForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_crisis_plan_button")
                    ) {
                        Text(text = "Speichern & im Team teilen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (crisisPlans.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keine individuellen Krisenpläne hinterlegt.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nutzen Sie den Button oben, um individuelle State-Pläne zu hinterlegen.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            crisisPlans.forEach { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = plan.patientInitials.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Krisenplan: ${plan.patientInitials}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Hauptdiagnose: ${plan.mainDiagnosis}",
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteCrisisPlan(plan.id) },
                                modifier = Modifier.testTag("delete_plan_${plan.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Plan löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRowDetail(label = "Individuelle Trigger:", value = plan.individualTrigger)
                        InfoRowDetail(label = "Frühwarnung (GELB):", value = plan.earlyWarningSigns)
                        InfoRowDetail(label = "Hilft zur Co-Regulation:", value = plan.preferredCalming)

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF5F5), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "VERSCHLIMMERT (DON'T): ${plan.whatVerschlimmert}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// Post-Incident Debriefing section
// ══════════════════════════════════════════════════════
@Composable
fun IncidentReviewWorkspaceSection(
    reviews: List<IncidentReview>,
    onSaveIncidentReview: (String, String, String, String, String, String, String) -> Unit,
    onDeleteIncidentReview: (Int) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }

    var initials by remember { mutableStateOf("") }
    var incidentDate by remember { mutableStateOf("") }
    var descr by remember { mutableStateOf("") }
    var triggerText by remember { mutableStateOf("") }
    var teamStrengths by remember { mutableStateOf("") }
    var lessonsLearned by remember { mutableStateOf("") }
    var teamWellbeing by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ereignis-Nachbereitung (Debriefing)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = {
                    showForm = !showForm
                    incidentDate = dateFormat.format(Date())
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("toggle_review_form_button")
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (showForm) "Schließen" else "Eintragen", fontSize = 11.sp)
            }
        }

        if (showForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incident_review_form_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Incident Review & Teambegleitung (Phase BLAU)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = initials,
                        onValueChange = { initials = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_initials"),
                        placeholder = { Text("Kürzel (z.B. L.M.)") },
                        label = { Text("Patienten-Initialen") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = incidentDate,
                        onValueChange = { incidentDate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_date"),
                        placeholder = { Text("z.B. 03.06.2026") },
                        label = { Text("Datum des Vorfalls") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descr,
                        onValueChange = { descr = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_description"),
                        placeholder = { Text("Was ist konkret vorgefallen? (Fakten)") },
                        label = { Text("Beschreibung des Vorfalls") }
                    )

                    OutlinedTextField(
                        value = triggerText,
                        onValueChange = { triggerText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_trigger"),
                        placeholder = { Text("Was war der eigentliche Auslöser? (Polyvagal)") },
                        label = { Text("Möglicher Trigger-Auslöser") }
                    )

                    OutlinedTextField(
                        value = teamStrengths,
                        onValueChange = { teamStrengths = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_strengths"),
                        placeholder = { Text("Was geland ausgezeichnet? (Sprecher-Rolle etc.)") },
                        label = { Text("Stärken des Teams") }
                    )

                    OutlinedTextField(
                        value = lessonsLearned,
                        onValueChange = { lessonsLearned = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_lessons"),
                        placeholder = { Text("Was machen wir nächstes Mal anders? (Plan-Update)") },
                        label = { Text("Lessons Learned (Kerneffekte)") }
                    )

                    OutlinedTextField(
                        value = teamWellbeing,
                        onValueChange = { teamWellbeing = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_wellbeing"),
                        placeholder = { Text("Wie geht es dem betroffenen Personal? Support benötigt?") },
                        label = { Text("Befinden & Selbstfürsorge des Teams") }
                    )

                    Button(
                        onClick = {
                            if (initials.trim().isEmpty() || descr.trim().isEmpty()) {
                                // invalid fields check
                            } else {
                                onSaveIncidentReview(
                                    initials,
                                    incidentDate,
                                    descr,
                                    triggerText,
                                    teamStrengths,
                                    lessonsLearned,
                                    teamWellbeing
                                )
                                // clear
                                initials = ""
                                incidentDate = ""
                                descr = ""
                                triggerText = ""
                                teamStrengths = ""
                                lessonsLearned = ""
                                teamWellbeing = ""
                                showForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_review_button")
                    ) {
                        Text(text = "Review speichern", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keine Debriefing-Einträge vorhanden.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nutzen Sie ein Post-Incident-Review, um das Lernen im Team zu verankern.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            reviews.forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = r.patientInitials.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Review: ${r.patientInitials}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Vorfallsdatum: ${r.incidentDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteIncidentReview(r.id) },
                                modifier = Modifier.testTag("delete_review_${r.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eintrag löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRowDetail(label = "Fakten & Ablauf:", value = r.description)
                        InfoRowDetail(label = "Auslöser (Trigger):", value = r.triggerSource)
                        InfoRowDetail(label = "Stärken im Team:", value = r.teamStrengths)
                        InfoRowDetail(label = "Lessons Learned:", value = r.lessonsLearned)
                        InfoRowDetail(label = "Befinden & Support:", value = r.teamWellbeing)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowDetail(label: String, value: String) {
    if (value.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 3.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════
// 5. CMS COMPONENT: CARD VIEW
// ══════════════════════════════════════════════════════
@Composable
fun CmsSectionCard(
    section: CmsSection,
    isAdminView: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = try {
        if (AppCustomizer.applyGlobally) {
            Color(android.graphics.Color.parseColor(AppCustomizer.primaryColorHex))
        } else {
            Color(android.graphics.Color.parseColor(section.accentColorHex))
        }
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val activeFontFamily = if (AppCustomizer.applyGlobally) {
        when (AppCustomizer.fontFamilyName) {
            "Sophisticated Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
            "Tech Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
            "Friendly OpenDyslexic" -> androidx.compose.ui.text.font.FontFamily.Cursive
            else -> androidx.compose.ui.text.font.FontFamily.SansSerif
        }
    } else {
        androidx.compose.ui.text.font.FontFamily.SansSerif
    }

    val cardShape = when {
        AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Brutalist" -> RoundedCornerShape(0.dp)
        AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Minimal Outline" -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(12.dp)
    }

    val cardBorderWidth = when {
        AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Brutalist" -> 3.dp
        AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Glowing Neon" -> 2.5.dp
        else -> 1.5.dp
    }

    val cardBorderColor = if (AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Glowing Neon") {
        accentColor
    } else {
        accentColor.copy(alpha = 0.8f)
    }

    val cardShadowElevation = if (AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Brutalist") 6.dp else 0.dp

    val cardBgColor = when {
        AppCustomizer.applyGlobally && AppCustomizer.cardStyle == "Glassmorphism" -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surface
    }

    val activePadding = if (AppCustomizer.applyGlobally) AppCustomizer.paddingDp else 14f
    val activeSpacing = if (AppCustomizer.applyGlobally) AppCustomizer.spacingDp else 10f

    val activeAlignment = if (AppCustomizer.applyGlobally) {
        when (AppCustomizer.textAlignmentName) {
            "Center" -> TextAlign.Center
            "Right" -> TextAlign.Right
            "Justify" -> TextAlign.Justify
            else -> TextAlign.Left
        }
    } else {
        TextAlign.Left
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { if (!isAdminView) expanded = !expanded }
            .testTag("cms_section_card_${section.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = cardShadowElevation),
        border = BorderStroke(cardBorderWidth, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(activePadding.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = section.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = activeFontFamily
                        )
                    }
                    Text(
                        text = section.description,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 16.dp),
                        fontFamily = activeFontFamily
                    )
                }

                if (isAdminView) {
                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_cms_btn_${section.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editieren",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_cms_btn_${section.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Löschen",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Wechseln",
                        tint = accentColor
                    )
                }
            }

            if (section.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(activeSpacing.dp))
                coil.compose.AsyncImage(
                    model = section.imageUrl,
                    contentDescription = section.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            if (expanded || isAdminView) {
                Spacer(modifier = Modifier.height(activeSpacing.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(activeSpacing.dp))
                Text(
                    text = section.contentText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = activeFontFamily,
                    textAlign = activeAlignment,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Kanal: " + if (section.phaseId == "ALL") "Allgemeines Wissen" else "Phase ${section.phaseId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    Text(
                        text = "Aktualisiert: " + SimpleDateFormat("dd.MM, HH:mm", Locale.GERMAN).format(Date(section.createdAt)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// 6. CMS COMPONENT: CMS ADMIN VIEW
// ══════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCmsScreen(
    cmsSections: List<CmsSection>,
    onSaveSection: (Int, String, String, String, String, String, String) -> Unit,
    onDeleteSection: (Int) -> Unit
) {
    var editingId by remember { mutableStateOf(0) }
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }
    var chosenColorHex by remember { mutableStateOf("#1D4ED8") }
    var chosenPhaseId by remember { mutableStateOf("ALL") }

    val presetColors = listOf(
        "#1D4ED8" to "Klassisch Blau",
        "#10B981" to "Präventiv Grün",
        "#F59E0B" to "Frühwarn Gelb",
        "#EF4444" to "Alarm Rot",
        "#8B5CF6" to "Post-Vagal Violett",
        "#06B6D4" to "Mental Cyan"
    )

    val listPhasesOptions = listOf("ALL", "WEISS", "GRUEN", "GELB", "ROT", "BLAU")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Content Management System (CMS)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hier können Sie Inhalte bearbeiten, neue Fachartikel schreiben, Bilder verknüpfen und Abschnitte den Phasen zuteilen. Änderungen sind sofort aktiv.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔑 ADMINISTRATOR-ZUGANG:\n" +
                                "• Standard-PIN: \"admin\"\n" +
                                "• Freischaltung über das Schloss-Symbol oben rechts in der App-Leiste.\n" +
                                "• Sobald entriegelt, erscheint dieser Einstellungsbereich in der Hauptnavigation.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer, contentColor = MaterialTheme.colorScheme.primaryContainer),
                        onClick = {
                            // Seed stunning high fidelity medical / clinician examples
                            onSaveSection(0, "Sensomotorische Deeskalation", "Körperorientierte Regulation in der Akutpsychiatrie", "Wenn verbale Interventionen nicht mehr greifen, hilft Co-Regulation durch Mimik, Proxemik, Atemfrequenz und Tiefensensibilität. Der Körper dient als resonanzfähiges Beruhigungs-Medium.", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=400", "#10B981", "GELB")
                            onSaveSection(0, "Therapeutischer Schutzraum", "Safewards-gestützte Architektur des Interaktionsfeldes", "Der visuelle Fluss auf einer Krisenstation entscheidet maßgeblich über das Erregungsniveau. Nutzen Sie reizarme Zonen (Snoezelen), reduzieren Sie metallische Klickgeräusche und arrangieren Sie Stühle im schrägen 30-Grad-Winkel statt in konfrontativer Face-to-Face Anordnung.", "https://images.unsplash.com/photo-1516549655169-df83a0774514?q=80&w=400", "#1D4ED8", "ALL")
                            onSaveSection(0, "Posttraumatisches Debriefing", "Schutz vor sekundärer Traumatisierung des Pflegeteams", "Ein Übergriff oder eine Zwangsmaßnahme hinterlassen auch beim Fachpersonal physiologische Stressspuren. Jedes Ereignis erfordert ein strukturiertes kollegiales Debriefing innerhalb von 24 Stunden, um emotionale Abwehrstrategien wie Zynismus oder Rückzug präventiv abzubauen.", "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?q=80&w=400", "#8B5CF6", "BLAU")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("seed_demo_cms")
                    ) {
                        Text("Demo-Inhalte laden (mit echten Bildern)")
                    }
                }
            }
        }

        // Editor Form Section Card
        item {
            Card(
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (editingId > 0) "Sektion editieren (ID: $editingId)" else "Neue Sektion anlegen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Titel*") },
                        modifier = Modifier.fillMaxWidth().testTag("cms_form_title"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Kurzbeschreibung / Subtitel*") },
                        modifier = Modifier.fillMaxWidth().testTag("cms_form_desc"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Ausführlicher Fließtext*") },
                        modifier = Modifier.fillMaxWidth().testTag("cms_form_content"),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = { Text("Bild URL (z.B. von Unsplash / Coil)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth().testTag("cms_form_image"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Phase association selector
                    Text(
                        text = "Kategorie / Deeskalation Phase:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listPhasesOptions.forEach { phase ->
                            val isSel = chosenPhaseId == phase
                            FilterChip(
                                selected = isSel,
                                onClick = { chosenPhaseId = phase },
                                label = { Text(if (phase == "ALL") "Allgemein" else phase) },
                                modifier = Modifier.testTag("phase_chip_$phase")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Accent Picker
                    Text(
                        text = "Akzentfarbe für das Leitlinien-Infoblatt:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetColors.forEach { (colorHex, text) ->
                            val colorVal = Color(android.graphics.Color.parseColor(colorHex))
                            val isSel = chosenColorHex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(width = 76.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorVal)
                                    .border(
                                        width = if (isSel) 3.dp else 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.outline else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { chosenColorHex = colorHex }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = text.take(6),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button if editing
                        if (editingId > 0) {
                            TextButton(
                                onClick = {
                                    editingId = 0
                                    titleInput = ""
                                    descInput = ""
                                    contentInput = ""
                                    imageUrlInput = ""
                                    chosenColorHex = "#1D4ED8"
                                    chosenPhaseId = "ALL"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Abbrechen")
                            }
                        }

                        Button(
                            enabled = titleInput.isNotEmpty() && descInput.isNotEmpty() && contentInput.isNotEmpty(),
                            onClick = {
                                onSaveSection(
                                    editingId,
                                    titleInput,
                                    descInput,
                                    contentInput,
                                    imageUrlInput,
                                    chosenColorHex,
                                    chosenPhaseId
                                )
                                // Reset form
                                editingId = 0
                                titleInput = ""
                                descInput = ""
                                contentInput = ""
                                imageUrlInput = ""
                                chosenColorHex = "#1D4ED8"
                                chosenPhaseId = "ALL"
                            },
                            modifier = Modifier.weight(2f).testTag("save_cms_section_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (editingId > 0) "Update speichern" else "CMS Beitrag erstellen")
                        }
                    }
                }
            }
        }

        // List of current CMS items
        item {
            Text(
                text = "Bestehende CMS Abschnitte (${cmsSections.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (cmsSections.isEmpty()) {
            item {
                Text(
                    text = "Noch keine eigenen Einträge in der SQLite Datenbank vorhanden. Nutzen Sie das Formular oben, um neue Leitlinien zu publizieren.",
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(cmsSections) { section ->
                CmsSectionCard(
                    section = section,
                    isAdminView = true,
                    onEdit = {
                        editingId = section.id
                        titleInput = section.title
                        descInput = section.description
                        contentInput = section.contentText
                        imageUrlInput = section.imageUrl
                        chosenColorHex = section.accentColorHex
                        chosenPhaseId = section.phaseId
                    },
                    onDelete = {
                        onDeleteSection(section.id)
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// 7. DESIGN CUSTOMIZER SCREEN & LIVE HTML BUILDER
// ══════════════════════════════════════════════════════
@Composable
fun DesignRemixScreen() {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var activePreviewTab by remember { mutableStateOf("LIVE_HTML") } // "LIVE_HTML" or "ELEMENT_PREVIEW"
    var showExportCode by remember { mutableStateOf(false) }
    
    // Smooth infinite breathing canvas for the background graphic (Google AI style)
    val infiniteTransition = rememberInfiniteTransition(label = "aiGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val currentThemeColor = try {
        Color(android.graphics.Color.parseColor(AppCustomizer.primaryColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val cssFontName = when (AppCustomizer.fontFamilyName) {
        "Sophisticated Serif" -> "Georgia, Times, 'Times New Roman', serif"
        "Tech Monospace" -> "'Courier New', Courier, monospace"
        "Friendly OpenDyslexic" -> "'Comic Sans MS', cursive, sans-serif"
        else -> "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"
    }

    val textAlignValue = AppCustomizer.textAlignmentName.lowercase()

    val cardStylesCss = when (AppCustomizer.cardStyle) {
        "Brutalist" -> """
            background: #ffffff;
            color: #1a1a1a;
            border: 3px solid #1a1a1a;
            box-shadow: 8px 8px 0px #1a1a1a;
            border-radius: 0px;
        """.trimIndent()
        "Minimal Outline" -> """
            background: #ffffff;
            border: 1.5px solid ${AppCustomizer.primaryColorHex};
            border-radius: 16px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
        """.trimIndent()
        "Glowing Neon" -> """
            background: #0d0d0c;
            color: #f3f4f6;
            border: 2.5px solid ${AppCustomizer.primaryColorHex};
            box-shadow: 0 0 20px ${AppCustomizer.primaryColorHex};
            border-radius: 12px;
        """.trimIndent()
        else -> """
            /* Glassmorphism Default */
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            border: 1.5px solid ${AppCustomizer.primaryColorHex};
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
        """.trimIndent()
    }

    val currentPreviewHtml = if (activePreviewTab == "LIVE_HTML") {
        AppCustomizer.customHtmlContent
    } else {
        """
<h1>🏆 Überschrift H1 (Dynamic)</h1>
<h2>🥈 Sektionstext H2</h2>
<h3>🥉 Unterthema H3</h3>
<hr/>
<p>Dies ist ein typischer Absatz mit <b>fetter Hervorbehebung</b> sowie <i>kursiven Elementen</i>. Es zeigt die harmonische Einbettung in das Layout.</p>
<h4>📋 Richtlinien-Protokoll:</h4>
<ul>
  <li>Schonung der kognitiven Ressourcen</li>
  <li>Deeskalativer Fokus über Atempulse</li>
  <li>Sofortiges Feedback des System-Status</li>
</ul>
        """.trimIndent()
    }

    val standaloneHtmlCode = """
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Applet Design Export</title>
    <style>
        :root {
            --primary-hex: ${AppCustomizer.primaryColorHex};
            --padding-dp: ${AppCustomizer.paddingDp}px;
            --spacing-dp: ${AppCustomizer.spacingDp}px;
        }
        body {
            background-color: ${if (AppCustomizer.cardStyle == "Glowing Neon") "#0d0d0c" else "#f3f4f6"};
            color: ${if (AppCustomizer.cardStyle == "Glowing Neon") "#f3f4f6" else "#1f2937"};
            font-family: $cssFontName;
            margin: 0;
            padding: 40px 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }
        .applet-card {
            width: 100%;
            max-width: 650px;
            padding: var(--padding-dp);
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
            gap: var(--spacing-dp);
            $cardStylesCss
        }
        .applet-card h1 {
            font-size: 24px;
            font-weight: 900;
            margin: 0;
            color: var(--primary-hex);
            text-align: $textAlignValue;
        }
        .applet-card h2 {
            font-size: 20px;
            font-weight: 700;
            margin: 0;
            color: var(--primary-hex);
            text-align: $textAlignValue;
        }
        .applet-card h3 {
            font-size: 16px;
            font-weight: 600;
            margin: 0;
            color: ${if (AppCustomizer.cardStyle == "Glowing Neon") "#9ca3af" else "#4b5563"};
            text-align: $textAlignValue;
        }
        .applet-card p {
            font-size: 14px;
            line-height: 1.6;
            margin: 0;
            text-align: $textAlignValue;
        }
        .applet-card hr {
            border: none;
            border-top: 1px solid ${if (AppCustomizer.cardStyle == "Glowing Neon") "rgba(255,255,255,0.15)" else "rgba(0,0,0,0.1)"};
            margin: 10px 0;
        }
        .applet-card ul {
            margin: 0;
            padding-left: 20px;
            text-align: $textAlignValue;
        }
        .applet-card li {
            font-size: 14px;
            line-height: 1.6;
            margin-bottom: 5px;
        }
        .preview-controls {
            margin-top: 15px;
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            justify-content: ${when (AppCustomizer.textAlignmentName) {
                "Center" -> "center"
                "Right" -> "flex-end"
                else -> "flex-start"
            }};
        }
        .badge {
            display: inline-block;
            padding: 4px 10px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-radius: 50px;
            background: var(--primary-hex);
            color: #ffffff;
        }
        .btn-primary {
            display: inline-block;
            padding: 10px 18px;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #ffffff;
            background: var(--primary-hex);
            border: none;
            border-radius: ${if (AppCustomizer.cardStyle == "Brutalist") "0px" else "8px"};
            cursor: pointer;
            box-shadow: ${if (AppCustomizer.cardStyle == "Brutalist") "3px 3px 0px #000" else "none"};
        }
        .btn-outline {
            display: inline-block;
            padding: 8px 16px;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: var(--primary-hex);
            background: transparent;
            border: 2px solid var(--primary-hex);
            border-radius: ${if (AppCustomizer.cardStyle == "Brutalist") "0px" else "8px"};
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="applet-card">
        $currentPreviewHtml
        <div class="preview-controls">
            <span class="badge">GESCHÜTZT</span>
            <button class="btn-primary">Aktion ausführen</button>
            <button class="btn-outline">Verwerfen</button>
        </div>
    </div>
</body>
</html>
    """.trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Title & Interactive Canvas Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                currentThemeColor.copy(alpha = 0.2f),
                                currentThemeColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(
                                    currentThemeColor.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background rotating orbits graphic using Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = 120f * glowPulse
                    drawCircle(
                        color = currentThemeColor.copy(alpha = 0.06f * glowPulse),
                        radius = baseRadius + 40f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = currentThemeColor.copy(alpha = 0.03f * glowPulse),
                        radius = baseRadius + 80f,
                        center = centerOffset
                    )
                    drawCircle(
                        color = currentThemeColor,
                        radius = 6f,
                        center = centerOffset,
                        style = Stroke(width = 2f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✨ AI DESIGN REMIXER ✨",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = currentThemeColor,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gestalten Sie das Layout, die Typografie und Abstände für Ihr Applet. Änderungen werden direkt auf Reales HTML applied!",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Global override switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auf gesamte App anwenden",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Überschreibt das Styling aller CMS-Karten im Handbuch",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = AppCustomizer.applyGlobally,
                        onCheckedChange = { AppCustomizer.applyGlobally = it },
                        modifier = Modifier.testTag("apply_globally_switch")
                    )
                }
            }
        }

        // 1. COLORS SCHEME SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. FARBSCHEMA WÄHLEN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = currentThemeColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val presetColors = listOf(
                        Triple("#1D4ED8", "Clinical Blue", "🔵"),
                        Triple("#10B981", "Bio Forest Green", "🟢"),
                        Triple("#D97706", "Sunset Calm", "🟠"),
                        Triple("#DC2626", "Safeguard Crimson", "🔴"),
                        Triple("#8B5CF6", "Polyvagal Purple", "🔮"),
                        Triple("#1F2937", "Minimal Slate", "⚫")
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetColors.forEach { (hex, name, emoji) ->
                            val isSelected = AppCustomizer.primaryColorHex.equals(hex, ignoreCase = true)
                            val itemColor = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) itemColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        AppCustomizer.primaryColorHex = hex
                                        Toast.makeText(context, "$name ausgewählt", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = if (isSelected) itemColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CARD STYLE & TYPOGRAPHY GRID
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("2. LAYOUT-ÄSTHETIK & SCHRIFTART", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = currentThemeColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Card style selector
                    Text("Rahmen-Stil der App-Karten:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf("Glassmorphism", "Brutalist", "Minimal Outline", "Glowing Neon").forEach { style ->
                        val isSelected = AppCustomizer.cardStyle == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentThemeColor.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable { AppCustomizer.cardStyle = style }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = style,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) currentThemeColor else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { AppCustomizer.cardStyle = style },
                                colors = RadioButtonDefaults.colors(selectedColor = currentThemeColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Font selection
                    Text("Schriftart-Familie (Predefined Typography):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    val fontPresets = listOf(
                        "Clean Modern" to "Schnelle Auffassung, standardisierter Look",
                        "Sophisticated Serif" to "Klassischer Buchstil, klinische Expertise",
                        "Tech Monospace" to "Gelistete Protokolle, strukturierte Daten",
                        "Friendly OpenDyslexic" to "Abgerundet, hoch-inklusiver Wohlfühllook"
                    )
                    fontPresets.forEach { (fontName, slogan) ->
                        val isSelected = AppCustomizer.fontFamilyName == fontName
                        val fontFamily = when (fontName) {
                            "Sophisticated Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                            "Tech Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                            "Friendly OpenDyslexic" -> androidx.compose.ui.text.font.FontFamily.Cursive
                            else -> androidx.compose.ui.text.font.FontFamily.SansSerif
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentThemeColor.copy(alpha = 0.08f) else Color.Transparent)
                                .border(1.dp, if (isSelected) currentThemeColor.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { AppCustomizer.fontFamilyName = fontName }
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fontName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                    color = if (isSelected) currentThemeColor else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = currentThemeColor, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = slogan,
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. SPACING & ALIGNMENT SLIDERS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("3. GRÖSSEN, ABSTÄNDE & AUSRICHTUNG", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = currentThemeColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Inner padding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Innenabstand (Padding):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${AppCustomizer.paddingDp.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currentThemeColor)
                    }
                    Slider(
                        value = AppCustomizer.paddingDp,
                        onValueChange = { AppCustomizer.paddingDp = it },
                        valueRange = 8f..32f,
                        colors = SliderDefaults.colors(thumbColor = currentThemeColor, activeTrackColor = currentThemeColor)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Elements gap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Zwischenabstände (Gaps):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${AppCustomizer.spacingDp.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currentThemeColor)
                    }
                    Slider(
                        value = AppCustomizer.spacingDp,
                        onValueChange = { AppCustomizer.spacingDp = it },
                        valueRange = 4f..24f,
                        colors = SliderDefaults.colors(thumbColor = currentThemeColor, activeTrackColor = currentThemeColor)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text align selector
                    Text("Text-Ausrichtung (Alignment):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Left", "Center", "Right", "Justify").forEach { align ->
                            val isSelected = AppCustomizer.textAlignmentName == align
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) currentThemeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { AppCustomizer.textAlignmentName = align }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (align) {
                                        "Left" -> "Links"
                                        "Center" -> "Mitte"
                                        "Right" -> "Rechts"
                                        else -> "Blocksatz"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. REAL RAW HTML INPUT & INTERACTIVE LIVE BUILDER PREVIEW
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, currentThemeColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("4. INTERAKTIVES REAL-HTML SANDBOXING", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = currentThemeColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Geben Sie hier HTML-Code ein. Unser nativer XML/HTML Parser interpretiert diesen live!",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = AppCustomizer.customHtmlContent,
                        onValueChange = { AppCustomizer.customHtmlContent = it },
                        label = { Text("Quelltext HTML-Editor", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth().height(160.dp).testTag("html_sandbox_editor"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        singleLine = false,
                        maxLines = 15
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            AppCustomizer.customHtmlContent = """
<div style="text-align: center;">
  <h1>📈 Klinische Live-Auswertung</h1>
  <p>Hier ist ein <b>dynamisches</b> Dokument mit den geänderten App-Styles!</p>
  <hr/>
  <h3>📌 Systemstatus: Normal</h3>
  <p>Die Schrifthöhe, Margins und text-aligns wurden hardware-beschleunigt gerendert.</p>
</div>
                            """.trimIndent()
                            Toast.makeText(context, "Vorlage geladen!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Andere Demo-Vorlage laden", fontSize = 11.sp)
                    }
                }
            }
        }

        // 5. PREVIEW TYPE WORKSPACE TABS (TAB SWITCHER)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePreviewTab == "LIVE_HTML") currentThemeColor else Color.Transparent)
                            .clickable { activePreviewTab = "LIVE_HTML" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = if (activePreviewTab == "LIVE_HTML") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Live-Sandbox",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activePreviewTab == "LIVE_HTML") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePreviewTab == "ELEMENT_PREVIEW") currentThemeColor else Color.Transparent)
                            .clickable { activePreviewTab = "ELEMENT_PREVIEW" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = if (activePreviewTab == "ELEMENT_PREVIEW") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Element-Vorschau",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activePreviewTab == "ELEMENT_PREVIEW") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. LIVE RENDER RESULT OUTPUT CARD (WIDGET DOCK)
        item {
            val viewLabel = if (activePreviewTab == "LIVE_HTML") "LIVE-VORSCHAU-DOCK (HTML SANDBOX)" else "UI-ELEMENT-BIBLIOTHEK (DESIGN PREVIEW)"
            Text(viewLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))

            val cardShape = when (AppCustomizer.cardStyle) {
                "Brutalist" -> RoundedCornerShape(0.dp)
                "Minimal Outline" -> RoundedCornerShape(16.dp)
                else -> RoundedCornerShape(12.dp)
            }

            val cardBorderWidth = when (AppCustomizer.cardStyle) {
                "Brutalist" -> 3.dp
                "Glowing Neon" -> 2.5.dp
                else -> 1.5.dp
            }

            val cardBorderColor = if (AppCustomizer.cardStyle == "Glowing Neon") {
                currentThemeColor
            } else {
                currentThemeColor.copy(alpha = 0.8f)
            }

            val cardShadowElevation = if (AppCustomizer.cardStyle == "Brutalist") 8.dp else 0.dp

            val cardBgColor = when (AppCustomizer.cardStyle) {
                "Glassmorphism" -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                else -> MaterialTheme.colorScheme.surface
            }

            val parsedFontFamily = when (AppCustomizer.fontFamilyName) {
                "Sophisticated Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                "Tech Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                "Friendly OpenDyslexic" -> androidx.compose.ui.text.font.FontFamily.Cursive
                else -> androidx.compose.ui.text.font.FontFamily.SansSerif
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .testTag("html_preview_card_tab"),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = cardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = cardShadowElevation),
                border = BorderStroke(cardBorderWidth, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Parse and render dynamic html blocks structured by customizer settings
                    HtmlPreviewRenderer(
                        html = currentPreviewHtml,
                        primaryColor = currentThemeColor,
                        fontFamily = parsedFontFamily,
                        innerPadding = AppCustomizer.paddingDp,
                        elementSpacing = AppCustomizer.spacingDp,
                        textAlignment = AppCustomizer.textAlignmentName
                    )

                    // 2. Extra Native Interactive Elements Preview (Only visible in Element-Vorschau mode)
                    if (activePreviewTab == "ELEMENT_PREVIEW") {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = AppCustomizer.paddingDp.dp)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppCustomizer.paddingDp.dp),
                            verticalArrangement = Arrangement.spacedBy(AppCustomizer.spacingDp.dp),
                            horizontalAlignment = when (AppCustomizer.textAlignmentName) {
                                "Center" -> Alignment.CenterHorizontally
                                "Right" -> Alignment.End
                                else -> Alignment.Start
                            }
                        ) {
                            Text(
                                "Native interaktive Web-Komponenten:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentThemeColor,
                                fontFamily = parsedFontFamily
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Badge component
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(currentThemeColor)
                                        .clickable {
                                            Toast.makeText(context, "Aktiv-Status ausgewählt", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "GESCHÜTZT",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = parsedFontFamily
                                    )
                                }

                                // Theme primary action button
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Primary Action: Deeskalations-Modus gestartet!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentThemeColor),
                                    shape = cardShape,
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Aktion",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = parsedFontFamily
                                    )
                                }

                                // Outlined action button
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Vorgang verworfen", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.5.dp, currentThemeColor),
                                    shape = cardShape,
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Verwerfen",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentThemeColor,
                                        fontFamily = parsedFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. SINGLE-FILE HTML/CSS CODESHEET EXPORTER & SHARE SHEET
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("html_exporter_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, currentThemeColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📤 CODESHEET-EXPORT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = currentThemeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(currentThemeColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("HTML5 + CSS3", color = currentThemeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        IconButton(
                            onClick = { showExportCode = !showExportCode },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showExportCode) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Code anzeigen/verbergen",
                                tint = currentThemeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Exportieren Sie das customized Applet-Dokument als eine einzige, standalone-fähige HTML-Datei mit eingebetten Stylesheets, Klassen und Komponenten.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(standaloneHtmlCode))
                                Toast.makeText(context, "✔ HTML & CSS Code in Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = currentThemeColor),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Code Kopieren", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, standaloneHtmlCode)
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "HTML-Design Exportieren")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HTML Teilen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showExportCode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            "STANDALONE SOURCE CODE PREVIEW:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .horizontalScroll(rememberScrollState())
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            ) {
                                item {
                                    Text(
                                        text = standaloneHtmlCode,
                                        color = Color(0xFFD4D4D4),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ══════════════════════════════════════════════════════
// NATIVE HTML-TO-APP PARSING AND RENDER COMPONENT
// ══════════════════════════════════════════════════════
@Composable
fun HtmlPreviewRenderer(
    html: String,
    primaryColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    innerPadding: Float,
    elementSpacing: Float,
    textAlignment: String
) {
    val blocks = remember(html) {
        parseHtmlToBlocks(html)
    }

    val composeAlignment = when (textAlignment) {
        "Center" -> TextAlign.Center
        "Right" -> TextAlign.Right
        "Justify" -> TextAlign.Justify
        else -> TextAlign.Left
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding.dp),
        verticalArrangement = Arrangement.spacedBy(elementSpacing.dp),
        horizontalAlignment = when (textAlignment) {
            "Center" -> Alignment.CenterHorizontally
            "Right" -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        blocks.forEach { block ->
            when (block.tag) {
                "h1" -> {
                    Text(
                        text = block.content,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        fontFamily = fontFamily,
                        color = primaryColor,
                        textAlign = composeAlignment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "h2" -> {
                    Text(
                        text = block.content,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = fontFamily,
                        color = primaryColor.copy(alpha = 0.9f),
                        textAlign = composeAlignment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "h3" -> {
                    Text(
                        text = block.content,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        fontFamily = fontFamily,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = composeAlignment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "hr" -> {
                    HorizontalDivider(
                        color = primaryColor.copy(alpha = 0.25f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                "li" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "✦",
                            color = primaryColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                        )
                        Text(
                            text = parseInlineHtml(block.content),
                            fontSize = 13.sp,
                            fontFamily = fontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            textAlign = composeAlignment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    if (block.content.isNotBlank()) {
                        Text(
                            text = parseInlineHtml(block.content),
                            fontSize = 13.sp,
                            fontFamily = fontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                            textAlign = composeAlignment,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

data class HtmlBlock(val tag: String, val content: String)

fun parseHtmlToBlocks(html: String): List<HtmlBlock> {
    val blocks = mutableListOf<HtmlBlock>()
    
    // First, strip outer wrappers like style div
    var cleanHtml = html
        .replace(Regex("(?s)<div[^>]*>"), "")
        .replace("</div>", "")
        
    var index = 0
    while (index < cleanHtml.length) {
        val nextTagStart = cleanHtml.indexOf('<', index)
        if (nextTagStart == -1) {
            val text = cleanHtml.substring(index).trim()
            if (text.isNotEmpty()) {
                blocks.add(HtmlBlock("p", text))
            }
            break
        }
        
        if (nextTagStart > index) {
            val prevText = cleanHtml.substring(index, nextTagStart).trim()
            if (prevText.isNotEmpty()) {
                blocks.add(HtmlBlock("p", prevText))
            }
        }
        
        val nextTagEnd = cleanHtml.indexOf('>', nextTagStart)
        if (nextTagEnd == -1) {
            break
        }
        
        val tagContent = cleanHtml.substring(nextTagStart + 1, nextTagEnd)
        val tagName = tagContent.split(" ")[0].lowercase()
        
        if (tagName == "hr" || tagName == "hr/") {
            blocks.add(HtmlBlock("hr", ""))
            index = nextTagEnd + 1
            continue
        }
        
        val closingTag = "</$tagName>"
        val closingTagStart = cleanHtml.indexOf(closingTag, nextTagEnd + 1)
        if (closingTagStart == -1) {
            index = nextTagEnd + 1
            continue
        }
        
        val innerContent = cleanHtml.substring(nextTagEnd + 1, closingTagStart).trim()
        
        if (tagName == "ul") {
            var liStart = 0
            while (liStart < innerContent.length) {
                val nextLi = innerContent.indexOf("<li>", liStart)
                if (nextLi == -1) break
                val nextLiEnd = innerContent.indexOf("</li>", nextLi)
                if (nextLiEnd == -1) break
                val liText = innerContent.substring(nextLi + 4, nextLiEnd).trim()
                blocks.add(HtmlBlock("li", liText))
                liStart = nextLiEnd + 5
            }
        } else {
            blocks.add(HtmlBlock(tagName, innerContent))
        }
        
        index = closingTagStart + closingTag.length
    }
    
    if (blocks.isEmpty() && html.isNotBlank()) {
        blocks.add(HtmlBlock("p", html))
    }
    
    return blocks
}

fun parseInlineHtml(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var index = 0
        var isBold = false
        var isItalic = false
        
        while (index < text.length) {
            val nextTag = text.indexOf('<', index)
            if (nextTag == -1) {
                append(text.substring(index))
                break
            }
            
            append(text.substring(index, nextTag))
            val tagEnd = text.indexOf('>', nextTag)
            if (tagEnd == -1) {
                append("<")
                index = nextTag + 1
                continue
            }
            
            val tag = text.substring(nextTag + 1, tagEnd).lowercase()
            when (tag) {
                "b", "strong" -> {
                    isBold = true
                }
                "/b", "/strong" -> {
                    isBold = false
                }
                "i", "em" -> {
                    isItalic = true
                }
                "/i", "/em" -> {
                    isItalic = false
                }
                "br", "br/" -> {
                    append("\n")
                }
            }
            
            if (isBold || isItalic) {
                pushStyle(
                    androidx.compose.ui.text.SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                    )
                )
            } else {
                try {
                    pop()
                } catch (e: Exception) {}
            }
            
            index = tagEnd + 1
        }
    }
}

// ══════════════════════════════════════════════════════
// 4. ICD-11 & WHO API INTERACTIVE SCREEN
// ══════════════════════════════════════════════════════
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IcdApiScreen(viewModel: DeeskalationViewModel) {
    val searchQuery by viewModel.icdSearchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.icdSelectedTab.collectAsStateWithLifecycle()
    val isLoading by viewModel.icdIsLoading.collectAsStateWithLifecycle()
    val searchResults by viewModel.icdSearchResults.collectAsStateWithLifecycle()
    val errorMessage by viewModel.icdErrorMessage.collectAsStateWithLifecycle()
    val clientId by viewModel.icdClientId.collectAsStateWithLifecycle()
    val clientSecret by viewModel.icdClientSecret.collectAsStateWithLifecycle()
    val accessTokenUser by viewModel.icdAccessTokenUser.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.icdConnectionStatus.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("icd_header_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KAPITEL 08: KLASSIFIKATION & WHO API",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ICD-11 & ICD-10 API-Integration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dieses Kapitel demonstriert die Integration der International Classification of Diseases (ICD) REST-Schnittstelle der WHO zur schnellen, standardisierten Erfassung von kinderpsychiatrischen Diagnosen und zur Bereitstellung klinischer Deeskalations-Indikatoren.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Horizontal Navigation Tabs for ICD Tool
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>(
                    Triple("OFFLINE", "Offline Handbuch", Icons.Default.List),
                    Triple("LIVE", "Live WHO API Abfrage", Icons.Default.Refresh),
                    Triple("DOCS", "API-Doku & Guidelines", Icons.Default.Build)
                )
                tabs.forEach { (tabId, label, icon) ->
                    val isSelected = selectedTab == tabId
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setIcdSelectedTab(tabId) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("icd_tab_$tabId")
                    )
                }
            }
        }

        if (selectedTab == "DOCS") {
            // API Developer Documentation and Document Reader
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("icd_docs_card_1"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Über die WHO ICD API", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Die WHO stellt die ICD-11 über einen modernen, HTTP-basierten REST-Webservice bereit. Dadurch können Diagnosen, synonyme Krankheitsbezeichnungen und hierarchische Codes dynamisch abgefragt werden. Dies fördert eine einheitliche, standardisierte Dokumentation in Krankenhäusern und EMR-Systemen.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("2. Authentifizierung & Client-Handshake", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Die API ist standardmäßig per OAuth 2.0 (Client Credentials Flow) abgesichert. " +
                                    "Um einen Token anzufordern, senden Sie einen form-urlencoded POST-Request an:\n" +
                                    "▶ URL: https://icdaccessmanagement.who.int/connect/token\n\n" +
                                    "Nutzen Sie die folgenden Parameter im Request-Body:\n" +
                                    "• grant_type = client_credentials\n" +
                                    "• client_id = <Ihre_Client_ID>\n" +
                                    "• client_secret = <Ihr_Client_Secret>\n" +
                                    "• scope = icdapi_access",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3. Semantische Suche (MMS Search)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nach Erhalt des JWT (JSON Web Token) können Suchanfragen per GET-Request durchgeführt werden:\n" +
                                    "▶ URL: https://id.who.int/icd/release/11/2024-01/mms/search?q=<suche>&flatResults=true\n\n" +
                                    "Erforderliche Headers:\n" +
                                    "• Authorization: Bearer <Ihr_Token>\n" +
                                    "• API-Version: v2\n" +
                                    "• Accept: application/json\n" +
                                    "• Accept-Language: de (oder en, fr, etc.)",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("icd_docs_card_2"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kotlin Retrofit Code-Beispiel", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = """
// 1. Definition des API-Schnittstelle
interface IcdSearchApi {
    @GET("icd/release/11/2024-01/mms/search")
    suspend fun search(
        @Header("Authorization") token: String,
        @Header("API-Version") version: String = "v2",
        @Query("q") query: String,
        @Query("flatResults") flat: Boolean = true
    ): IcdSearchQueryResult
}

// 2. Client-Initialisierung
val retrofit = Retrofit.Builder()
    .baseUrl("https://id.who.int/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
                                """.trimIndent(),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color(0xFFD4D4D4)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Button(
                            onClick = {
                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Kotlin API Code", "interface IcdSearchApi {\n    @GET(\"icd/release/11/2024-01/mms/search\")\n    suspend fun search(\n        @Header(\"Authorization\") token: String,\n        @Header(\"API-Version\") version: String = \"v2\",\n        @Query(\"q\") query: String,\n        @Query(\"flatResults\") flat: Boolean = true\n    ): IcdSearchQueryResult\n}")
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Code-Präparat in Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Code-Beispiel kopieren", fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        TextButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://icd.who.int/icdapi/"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Offizielle WHO ICD API Entwicklerseite öffnen", fontSize = 11.sp)
                        }
                    }
                }
            }
        } else if (selectedTab == "LIVE") {
            // Live Connection Credential Settings & Search View
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("icd_credentials_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WHO API Registrierungsdaten", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = clientId,
                            onValueChange = { viewModel.setIcdCredentials(it, clientSecret) },
                            label = { Text("API Client-ID (Optionale Eingabe)", fontSize = 12.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("icd_client_id_input")
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = clientSecret,
                            onValueChange = { viewModel.setIcdCredentials(clientId, it) },
                            label = { Text("API Client-Secret", fontSize = 12.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("icd_client_secret_input")
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("ODER verwenden Sie einen fertig generierten Access-Token:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        OutlinedTextField(
                            value = accessTokenUser,
                            onValueChange = { viewModel.setIcdAccessToken(it) },
                            placeholder = { Text("Bearer eyJhbGci...", fontSize = 11.sp) },
                            label = { Text("Eigener Token direkt eintragen", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("icd_token_input")
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { viewModel.testIcdConnection() },
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("icd_test_conn_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Verbindung testen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        if (connectionStatus != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("icd_conn_result_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (connectionStatus!!.contains("ERFOLGREICH")) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (connectionStatus!!.contains("ERFOLGREICH")) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (connectionStatus!!.contains("ERFOLGREICH")) Color(0xFF166534) else Color(0xFF991B1B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = connectionStatus!!,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (connectionStatus!!.contains("ERFOLGREICH")) Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Live Query Block
            item {
                Text(
                    text = "LIVE ABFRAGE",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setIcdSearchQuery(it) },
                    placeholder = { Text("Geben Sie ein englisches Wort ein (z.B. 'autism', 'bipolar', 'stress')...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("icd_live_query_input")
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("icd_live_error_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Hinweis zum Live-Zugriff:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp)
                            Text(errorMessage!!, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Die Applikation hat automatisch eine hochgradige, lokale Offline-Abfrage zur Verfügung gestellt, damit Sie sofort suchen können.", fontSize = 10.sp, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (searchResults.isEmpty() && !isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("icd_empty_result"), contentAlignment = Alignment.Center) {
                        Text("Keine Treffer in der Live-Datenbank oder offline.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }

            items(searchResults) { entity ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("icd_live_result_item_${entity.theCode ?: "unknown"}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entity.theCode ?: "?",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = parseInlineHtml(entity.title ?: "Nicht benanntes Symptom"),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entity.matchingText ?: "WHO ICD-11 Entity",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // "OFFLINE" - Robust child psychiatry local handbook database
            item {
                Text(
                    text = "KLINISCHE SCHNELLREFERENZ (OFFLINE)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setIcdSearchQuery(it) },
                    placeholder = { Text("Deutsches Schlagwort suchen (z.B. ADHS, Bulimie, Trotz, Trauma)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("icd_offline_query_input")
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("icd_empty_offline_result"), contentAlignment = Alignment.Center) {
                        Text("Kein passendes KJP Diagnosen-Muster lokal gefunden.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }

            items(searchResults) { entity ->
                // Look up offline definition to display beautiful specific tips
                val matchedDiag = com.example.data.IcdApiManager.offlineDb.firstOrNull { it.icd11Code == entity.theCode }
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("icd_offline_result_item_${entity.theCode ?: "unknown"}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ICD-11: ${entity.theCode}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (matchedDiag != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ICD-10: ${matchedDiag.code}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (matchedDiag != null) {
                                Surface(
                                    color = when (matchedDiag.relatedPhase) {
                                        "ROT" -> Color(0xFFFEE2E2)
                                        "GELB" -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFDCFCE7)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = matchedDiag.relatedPhase,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (matchedDiag.relatedPhase) {
                                            "ROT" -> Color(0xFF991B1B)
                                            "GELB" -> Color(0xFF92400E)
                                            else -> Color(0xFF166534)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = parseInlineHtml(entity.title ?: ""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        matchedDiag?.let { d ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = d.description,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Clinical action box
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("icd_action_card_${entity.theCode}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DEESKALIERENDE INTERVENTION:",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = d.deescalationTip,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
