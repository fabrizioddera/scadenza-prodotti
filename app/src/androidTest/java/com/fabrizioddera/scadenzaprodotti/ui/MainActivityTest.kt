package com.fabrizioddera.scadenzaprodotti.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fabrizioddera.scadenzaprodotti.R
import com.fabrizioddera.scadenzaprodotti.data.AppDatabase
import com.fabrizioddera.scadenzaprodotti.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var db: AppDatabase
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        db = AppDatabase.getInstance(ApplicationProvider.getApplicationContext())
        runBlocking(Dispatchers.IO) { db.clearAllTables() }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
        runBlocking(Dispatchers.IO) { db.clearAllTables() }
    }

    private fun preInsert(name: String, daysToExpiry: Long = 10): Product {
        val p = Product(
            name = name,
            expiryDate = LocalDate.now().plusDays(daysToExpiry).toEpochDay(),
            imageUrl = null
        )
        runBlocking(Dispatchers.IO) { db.productDao().insert(p) }
        Thread.sleep(400)
        return p
    }

    // ── Empty state ────────────────────────────────────────────────────────────

    @Test
    fun emptyState_emptyView_visible() {
        onView(withId(R.id.emptyView)).check(matches(isDisplayed()))
    }

    @Test
    fun emptyState_recyclerView_hidden() {
        onView(withId(R.id.recyclerView)).check(matches(not(hasMinimumChildCount(1))))
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun fab_click_opens_addProduct_screen() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.editName)).check(matches(isDisplayed()))
    }

    // ── Add product ───────────────────────────────────────────────────────────

    @Test
    fun addProduct_withName_appearsInList() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.editName)).perform(typeText("Latte UHT"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())
        Thread.sleep(500)
        onView(withText("Latte UHT")).check(matches(isDisplayed()))
    }

    @Test
    fun addProduct_withName_hidesEmptyView() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.editName)).perform(typeText("Yogurt"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.emptyView)).check(matches(not(isDisplayed())))
    }

    @Test
    fun addProduct_emptyName_staysOnAddScreen() {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.btnSave)).perform(click())
        onView(withId(R.id.editName)).check(matches(isDisplayed()))
    }

    // ── Edit product ──────────────────────────────────────────────────────────

    @Test
    fun clickProduct_opens_editScreen_withPrefilledName() {
        preInsert("Burro")
        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.editName)).check(matches(withText("Burro")))
    }

    @Test
    fun editProduct_updatedName_appearsInList() {
        preInsert("Panna")
        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.editName)).perform(clearText(), typeText("Panna Fresca"), closeSoftKeyboard())
        onView(withId(R.id.btnSave)).perform(click())
        Thread.sleep(500)
        onView(withText("Panna Fresca")).check(matches(isDisplayed()))
        onView(withText("Panna")).check(matches(not(isDisplayed())))
    }

    // ── Delete product ────────────────────────────────────────────────────────

    @Test
    fun deleteProduct_via_button_removesFromList() {
        preInsert("Mozzarella")
        onView(allOf(withId(R.id.btnDelete), isDescendantOfA(withId(R.id.recyclerView))))
            .perform(click())
        Thread.sleep(400)
        onView(withId(R.id.emptyView)).check(matches(isDisplayed()))
    }

    @Test
    fun deleteProduct_showsSnackbar() {
        preInsert("Formaggio")
        onView(allOf(withId(R.id.btnDelete), isDescendantOfA(withId(R.id.recyclerView))))
            .perform(click())
        onView(withText(containsString("Formaggio"))).check(matches(isDisplayed()))
    }

    // ── List state ────────────────────────────────────────────────────────────

    @Test
    fun multipleProducts_allShownInList() {
        preInsert("Latte")
        preInsert("Yogurt")
        preInsert("Burro")
        onView(withText("Latte")).check(matches(isDisplayed()))
        onView(withText("Yogurt")).check(matches(isDisplayed()))
        onView(withText("Burro")).check(matches(isDisplayed()))
    }
}
