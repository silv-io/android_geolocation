package at.tuwien.android_geolocation

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import at.tuwien.android_geolocation.data.AndroidGeolocationUITestData
import at.tuwien.android_geolocation.util.Result
import at.tuwien.android_geolocation.view.ui.location.LocationActivity
import at.tuwien.android_geolocation.viewmodel.location.LocationListViewModel
import com.tuwien.geolocation_android.R
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@LargeTest
class AndroidGeolocationUITest : KoinTest {
    @Rule
    @JvmField
    val rule = ActivityTestRule(LocationActivity::class.java, true, false)

    lateinit var mockLocationListViewModel: LocationListViewModel

    @Before
    fun setUp() {
        mockLocationListViewModel = mockk(LocationListViewModel::class.java.name)

        loadKoinModules(module {
            viewModel(override = true) {
                mockLocationListViewModel
            }
        })
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun createAndShowMeasurement() {
        coEvery { mockLocationListViewModel.getLocationsFromRepository() } returns Result.Success(AndroidGeolocationUITestData.locations)


        rule.launchActivity(null)

        Thread.sleep(5000)
    }

    @Test
    fun scrollThroughList() {
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("25"))))
    }

    @Test
    fun openDetailMeasurement() {
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("25"))))
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(24, click()))
    }

    @Test
    fun deleteDetailMeasurementFromList() {
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("25"))))
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(24, longClick()))
        onView(withId(R.id.fab)).perform(click())
    }

    @Test
    fun deleteDetailMeasurementFromDetailView() {
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("25"))))
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(24, click()))
        onView(withId(R.id.fab_delete)).perform(click())
    }
}