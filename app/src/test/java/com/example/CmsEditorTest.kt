package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.CmsSection
import com.example.ui.AdminCmsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class CmsEditorTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testFormValidationAndStateChanges() {
        var savedId = -1
        var savedTitle = ""
        var savedDesc = ""
        var savedContent = ""
        var savedUrl = ""
        var savedColor = ""
        var savedPhase = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                AdminCmsScreen(
                    cmsSections = emptyList(),
                    onSaveSection = { id, title, desc, content, url, color, phase ->
                        savedId = id
                        savedTitle = title
                        savedDesc = desc
                        savedContent = content
                        savedUrl = url
                        savedColor = color
                        savedPhase = phase
                        println("TEST_DEBUG: onSaveSection callback invoked with title=$title")
                    },
                    onDeleteSection = {}
                )
            }
        }

        // Initially, save button should be disabled because fields are empty
        composeTestRule.onNodeWithTag("save_cms_section_btn").performScrollTo().assertIsNotEnabled()

        // Fill in the title
        composeTestRule.onNodeWithTag("cms_form_title").performScrollTo().performTextInput("Deeskalationstraining")
        composeTestRule.onNodeWithTag("cms_form_title").assertTextContains("Deeskalationstraining")

        // Fill in the subtitle / description
        composeTestRule.onNodeWithTag("cms_form_desc").performScrollTo().performTextInput("Klassische KJP Deeskalationstechniken")
        composeTestRule.onNodeWithTag("cms_form_desc").assertTextContains("Klassische KJP Deeskalationstechniken")

        // Fill in the content
        composeTestRule.onNodeWithTag("cms_form_content").performScrollTo().performTextInput("Hier stehen sehr hilfreiche Schritte zur Beruhigung.")
        composeTestRule.onNodeWithTag("cms_form_content").assertTextContains("Hier stehen sehr hilfreiche Schritte zur Beruhigung.")

        // After filling required fields, save button should be enabled
        composeTestRule.onNodeWithTag("save_cms_section_btn").performScrollTo().assertIsEnabled()

        // Click on preset image
        composeTestRule.onNodeWithTag("preset_image_chip_Atem").performScrollTo().performClick()

        // Choose a phase
        composeTestRule.onNodeWithTag("phase_chip_GELB").performScrollTo().performClick()

        // Click save
        composeTestRule.onNodeWithTag("save_cms_section_btn").performScrollTo().performClick()

        // Wait for idle to process potential asynchronous composition updates
        composeTestRule.waitForIdle()

        // Verify the callback values
        assertEquals(0, savedId)
        assertEquals("Deeskalationstraining", savedTitle)
        assertEquals("Klassische KJP Deeskalationstechniken", savedDesc)
        assertEquals("Hier stehen sehr hilfreiche Schritte zur Beruhigung.", savedContent)
        assertEquals("https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=400", savedUrl)
        assertEquals("GELB", savedPhase)
    }

    @Test
    fun testTextFormattingToolbarHelper() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AdminCmsScreen(
                    cmsSections = emptyList(),
                    onSaveSection = { _, _, _, _, _, _, _ -> },
                    onDeleteSection = {}
                )
            }
        }

        // Initially content is empty
        composeTestRule.onNodeWithTag("cms_form_content").performScrollTo().assertTextContains("")

        // Click formatting chip for Aufzählung
        composeTestRule.onNodeWithTag("format_chip_•_aufzählung").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cms_form_content").assertTextContains("\n• ")

        // Click delete format chip to clear
        composeTestRule.onNodeWithTag("format_chip_löschen").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cms_form_content").assertTextContains("")
    }
}
