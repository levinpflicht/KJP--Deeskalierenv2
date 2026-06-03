package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ══════════════════════════════════════════════════════
// 1. DATA MODELS FOR WHO ICD-11 API
// ══════════════════════════════════════════════════════

@JsonClass(generateAdapter = true)
data class IcdTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "token_type") val tokenType: String
)

@JsonClass(generateAdapter = true)
data class IcdSearchQueryResult(
    @Json(name = "destinationEntities") val destinationEntities: List<IcdEntity>? = null,
    @Json(name = "errorMessage") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class IcdEntity(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "theCode") val theCode: String? = null,
    @Json(name = "matchingText") val matchingText: String? = null
)

// Elegant Offline Diagnosis model with custom clinic-oriented de-escalation tips
data class OfflineDiagnosis(
    val code: String,
    val icd11Code: String,
    val titleDe: String,
    val titleEn: String,
    val synonyms: List<String>,
    val description: String,
    val deescalationTip: String,
    val relatedPhase: String // e.g. GELB, ROT
)

// ══════════════════════════════════════════════════════
// 2. RETROFIT REST INTERFACES
// ══════════════════════════════════════════════════════

interface IcdAuthApi {
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("scope") scope: String = "icdapi_access"
    ): IcdTokenResponse
}

interface IcdSearchApi {
    // Standard WHO ICD-11 MMS search endpoint
    @GET("icd/release/11/2024-01/mms/search")
    suspend fun searchMms(
        @Header("Authorization") authHeader: String,
        @Header("API-Version") apiVersion: String = "v2",
        @Header("Accept") accept: String = "application/json",
        @Header("Accept-Language") acceptLanguage: String = "de",
        @Query("q") query: String,
        @Query("flatResults") flatResults: Boolean = true
    ): IcdSearchQueryResult
}

// ══════════════════════════════════════════════════════
// 3. SERVICE ORCHESTRATOR
// ══════════════════════════════════════════════════════

object IcdApiManager {
    private const val AUTH_BASE_URL = "https://icdaccessmanagement.who.int/"
    private const val API_BASE_URL = "https://id.who.int/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val apiRetrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val authApi: IcdAuthApi = authRetrofit.create(IcdAuthApi::class.java)
    val searchApi: IcdSearchApi = apiRetrofit.create(IcdSearchApi::class.java)

    // Curated high-fidelity child and youth psychiatry database for offline lookup and fallback
    val offlineDb = listOf(
        OfflineDiagnosis(
            code = "F90.0 / F90.1",
            icd11Code = "6A05",
            titleDe = "Aufmerksamkeitsdefizit-/Hyperaktivitätsstörung (ADHS)",
            titleEn = "Attention Deficit Hyperactivity Disorder",
            synonyms = listOf("ADHS", "Hyperaktivität", "Inattentive", "Zappelphilipp", "Zappel", "ADS", "Konzentration"),
            description = "Häufige entwicklungsbezogene Störung charakterisiert durch ausgeprägte Unaufmerksamkeit, Überaktivität und Impulsivität, die in mehreren Lebensbereichen auftritt.",
            deescalationTip = "Kurze, klare Arbeitsaufträge geben. Reizreduzierte Arbeitsumgebung schaffen. Hyperaktiven Drang nicht unterdrücken, sondern kontrollierte Bewegungsventile bieten (z.B. Erledigungen).",
            relatedPhase = "GELB"
        ),
        OfflineDiagnosis(
            code = "F84.0 / F84.5",
            icd11Code = "6A02",
            titleDe = "Autismus-Spektrum-Störung (ASS)",
            titleEn = "Autism Spectrum Disorder",
            synonyms = listOf("ASS", "Autismus", "Asperger", "Kanner", "Frühkindlich", "Spektrum", "Wiederholungsverhalten", "Reizüberflutung"),
            description = "Einschränkungen in der sozialen Kommunikation und Interaktion sowie stereotype, repetitive Verhaltensmuster und ausgeprägtes Bedürfnis nach Vorhersehbarkeit.",
            deescalationTip = "Metaphernfreie, wörtliche Sprache nutzen. Augenkontakt nicht erzwingen. Rückzug in einen absolut reizarmen Raum ermöglichen ('Snoezelen' oder Krisenbox). Visuelle Pläne einsetzen.",
            relatedPhase = "GELB"
        ),
        OfflineDiagnosis(
            code = "F91.3",
            icd11Code = "6A06",
            titleDe = "Störung mit oppositionellem Trotzverhalten (ODD)",
            titleEn = "Oppositional Defiant Disorder",
            synonyms = listOf("ODD", "Trotzphase", "Oppositionell", "Trotz", "Feindselig", "Aggression", "Widerstand"),
            description = "Wiederkehrendes Muster aus trotzigem, ungehorsamem, feindseligem Verhalten gegenüber Autoritätspersonen ohne schwere Verletzung der Rechte anderer.",
            deescalationTip = "Machtkämpfe konsequent vermeiden. Keine Sofort-Gehorsam-Fantasien ausleben. Dem Jugendlichen Wahlmöglichkeiten lassen (z.B. 'Wir machen das jetzt oder in 5 Minuten, du entscheidest').",
            relatedPhase = "GELB"
        ),
        OfflineDiagnosis(
            code = "F43.1",
            icd11Code = "6B40",
            titleDe = "Posttraumatische Belastungsstörung (PTBS)",
            titleEn = "Post-Traumatic Stress Disorder",
            synonyms = listOf("PTBS", "PTSD", "Trauma", "Trigger", "Flashback", "Dissoziation", "Schreckhaftigkeit", "Missbrauch"),
            description = "Verzögerte Reaktion auf ein extrem bedrohliches oder katastrophales Ereignis, begleitet von intrusiven Erinnerungen, Vermeidungsverhalten und Hyperarousal.",
            deescalationTip = "Bei Dissoziation/Flashback: Sanfte, aber bestimmte Re-Orientierung im Hier und Jetzt (5-4-3-2-1 Methode). Körpergrenzen wahren, keine unangekündigte Berührung.",
            relatedPhase = "ROT"
        ),
        OfflineDiagnosis(
            code = "F60.31",
            icd11Code = "6D11 / Borderline",
            titleDe = "Emotionell instabile Persönlichkeitsstörung (Borderline-Typ)",
            titleEn = "Emotionally Unstable Personality Disorder (Borderline)",
            synonyms = listOf("Borderline", "BPS", "EIPS", "Selbstverletzung", "Stimmungsschwankungen", "Leere", "SVV", "Skills", "Anspannung"),
            description = "Muster von Instabilität in zwischenmenschlichen Beziehungen, dem Selbstbild und den Affekten sowie deutliche Impulsivität und selbstschädigendes Verhalten.",
            deescalationTip = "DBT-Stresstoleranz-Skills anbieten (z.B. Ammoniak-Riechstäbchen, Igelball, Kältepack). Ruhige, validierende Gesprächskonstanz signalisieren ('Ich sehe dich und bleibe hier'). Splitting-Versuche im Team besprechen.",
            relatedPhase = "GELB"
        ),
        OfflineDiagnosis(
            code = "F20.0",
            icd11Code = "6A20",
            titleDe = "Schizophrenie / Akute Psychose",
            titleEn = "Schizophrenia or Acute Psychotic Episode",
            synonyms = listOf("Psychose", "Schizophrenie", "Wahn", "Halluzination", "Stimmen", "Paranoia", "Realitätsverlust"),
            description = "Schwere psychische Störung mit tiefgreifenden Desorganisationen des Denkens, der Wahrnehmung (z.B. Stimmenhören) und des Realitätsbezugs.",
            deescalationTip = "Wahninhalte weder bestätigen noch ausreden ('Ich glaube dir, dass du das so hörst, mir macht es aber keine Angst'). Einfache, kurze Sätze sprechen. Ausreichenden physischen Abstand halten.",
            relatedPhase = "ROT"
        ),
        OfflineDiagnosis(
            code = "F50.0",
            icd11Code = "6C50",
            titleDe = "Anorexia nervosa (Magersucht)",
            titleEn = "Anorexia Nervosa",
            synonyms = listOf("Anorexie", "Magersucht", "Kachexie", "Essstörung", "Hungern"),
            description = "Absichtlich herbeigeführter Gewichtsverlust mit ausgeprägter Angst vor Gewichtszunahme und Körperschemainstabilität.",
            deescalationTip = "Thema Essen und Gewicht aus der Alltagsdiskussion heraushalten. Fokus auf Lebensbewältigung, Ressourcen und kognitive Umstrukturierung legen.",
            relatedPhase = "WEISS"
        ),
        OfflineDiagnosis(
            code = "F32 / F33",
            icd11Code = "6A70 / 6A71",
            titleDe = "Depressive Episode (Kindes- & Jugendalter)",
            titleEn = "Depressive Episode in Children & Adolescents",
            synonyms = listOf("Depression", "Traurigkeit", "Antriebslos", "Rückzug", "Hoffnungslos", "Suizidalität"),
            description = "Gedrückte Stimmung, Interessenverlust, verminderter Antrieb und verminderte Konzentration, im Jugendalter oft hinter Reizbarkeit maskiert.",
            deescalationTip = "Aktivierende Ansprache mit geringer Hürde. Kleine Erfolge feiern. Suizidgedanken direkt und entlastend ansprechen. Für Sicherheit sorgen.",
            relatedPhase = "GELB"
        ),
        OfflineDiagnosis(
            code = "F91",
            icd11Code = "6A04",
            titleDe = "Störung des Sozialverhaltens (Verhaltenstherapeutisch)",
            titleEn = "Conduct-Dissocial Disorder",
            synonyms = listOf("Sozialverhalten", "Aggression", "Stehlen", "Lügen", "Prügel", "Vandalismus", "Dissozial"),
            description = "Wiederholt anhaltendes Muster von asozialem, aggressivem oder aufsässigem Verhalten, das grundlegende Rechte anderer verletzt.",
            deescalationTip = "Grenzen absolut klar, ruhig und unemotional definieren. Konsequenzen sofort, stringent und unaufgeregt umsetzen. Positives Verhalten verstärken.",
            relatedPhase = "GELB"
        )
    )

    /**
     * Searches our highly-designed local Child and Youth Psychiatry registry
     */
    fun searchOffline(query: String): List<IcdEntity> {
        if (query.isBlank()) {
            return offlineDb.map { it.toIcdEntity() }
        }
        val lower = query.lowercase().trim()
        return offlineDb.filter { diag ->
            diag.titleDe.lowercase().contains(lower) ||
            diag.titleEn.lowercase().contains(lower) ||
            diag.icd11Code.lowercase().contains(lower) ||
            diag.code.lowercase().contains(lower) ||
            diag.synonyms.any { syn -> syn.lowercase().contains(lower) }
        }.map { it.toIcdEntity() }
    }

    private fun OfflineDiagnosis.toIcdEntity(): IcdEntity {
        return IcdEntity(
            id = "offline://icd11/mms/$icd11Code",
            title = "$titleDe ($titleEn)",
            theCode = icd11Code,
            matchingText = "Klas. Code: $code • Deeskalation: $deescalationTip"
        )
    }
}
