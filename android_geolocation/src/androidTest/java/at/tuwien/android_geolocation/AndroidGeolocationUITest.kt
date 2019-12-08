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
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Result
import at.tuwien.android_geolocation.view.ui.location.LocationActivity
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import com.schibsted.spain.barista.interaction.BaristaListInteractions.scrollListToPosition
import com.tuwien.geolocation_android.R
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@LargeTest
class AndroidGeolocationUITest : KoinTest {
    @get:Rule
    @JvmField
    val rule = ActivityTestRule(LocationActivity::class.java, true, false)

    @get:Rule
    @JvmField
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule(rule)

    lateinit var mockLocationRepository: LocationRepository

    @Before
    fun setUp() {
        mockLocationRepository = mockk(LocationRepository::class.java.name)

        loadKoinModules(module {
            single(override = true) {
                mockLocationRepository
            }
        })
    }

    @After
    fun cleanUp() {
        stopKoin()
    }

    @Test
    fun createAndShowMeasurement() {
        coEvery { mockLocationRepository.getLocations() } returns Result.Success(AndroidGeolocationUITestData.locations)
        coEvery { mockLocationRepository.newLocation(any(), any()) } returns Result.Success(AndroidGeolocationUITestData.locations[0].id)
        coEvery { mockLocationRepository.getLocation(any()) } returns Result.Success(AndroidGeolocationUITestData.locations[0])

        rule.launchActivity(null)

        clickOn(R.id.fab)

        assertContains(R.id.txt_capture_time, AndroidGeolocationUITestData.locations[0].getFormattedTimestamp())
        assertContains(R.id.txt_gps_coodinates, AndroidGeolocationUITestData.locations[0].gps.toString())
        assertContains(R.id.txt_mls_coordinates, AndroidGeolocationUITestData.locations[0].mls.toString())
        assertContains(R.id.txt_diff_gps_mls, AndroidGeolocationUITestData.locations[0].gps.diff(AndroidGeolocationUITestData.locations[0].mls).toString())
        assertContains(R.id.txt_gps_accuracy, AndroidGeolocationUITestData.locations[0].gps.accuracy.toString())
        assertContains(R.id.txt_mls_accuracy, AndroidGeolocationUITestData.locations[0].mls.accuracy.toString())
        assertContains(R.id.txt_mls_parameters, AndroidGeolocationUITestData.locations[0].params.toString())

        coVerify(exactly = 1) { mockLocationRepository.getLocations() }
        coVerify(exactly = 1) { mockLocationRepository.newLocation(any(), any()) }
        coVerify(exactly = 1) { mockLocationRepository.getLocation(AndroidGeolocationUITestData.locations[0].id) }
    }

    @Test
    fun scrollThroughList() {
        coEvery { mockLocationRepository.getLocations() } returns Result.Success(AndroidGeolocationUITestData.locations)

        rule.launchActivity(null)

        assertListItemCount(R.id.item_list, AndroidGeolocationUITestData.locations.size)

        scrollListToPosition(R.id.item_list, 24)
        scrollListToPosition(R.id.item_list, 0)
        scrollListToPosition(R.id.item_list, 1)
        scrollListToPosition(R.id.item_list, 2)
        scrollListToPosition(R.id.item_list, 3)
        scrollListToPosition(R.id.item_list, 4)
        scrollListToPosition(R.id.item_list, 5)
        scrollListToPosition(R.id.item_list, 6)
        scrollListToPosition(R.id.item_list, 7)
        scrollListToPosition(R.id.item_list, 8)
        scrollListToPosition(R.id.item_list, 9)
        scrollListToPosition(R.id.item_list, 10)
        scrollListToPosition(R.id.item_list, 11)
        scrollListToPosition(R.id.item_list, 12)
        scrollListToPosition(R.id.item_list, 13)
        scrollListToPosition(R.id.item_list, 14)
        scrollListToPosition(R.id.item_list, 15)
        scrollListToPosition(R.id.item_list, 16)
        scrollListToPosition(R.id.item_list, 17)
        scrollListToPosition(R.id.item_list, 18)
        scrollListToPosition(R.id.item_list, 19)
        scrollListToPosition(R.id.item_list, 20)
        scrollListToPosition(R.id.item_list, 21)
        scrollListToPosition(R.id.item_list, 22)
        scrollListToPosition(R.id.item_list, 23)
        scrollListToPosition(R.id.item_list, 24)

        coVerify(exactly = 1) { mockLocationRepository.getLocations() }
    }

    @Test
    fun openDetailMeasurement() {
        val elementId = 24

        coEvery { mockLocationRepository.getLocations() } returns Result.Success(AndroidGeolocationUITestData.locations)
        coEvery { mockLocationRepository.getLocation(any()) } returns Result.Success(AndroidGeolocationUITestData.locations[elementId])

        rule.launchActivity(null)

        clickListItem(R.id.item_list, elementId)

        assertContains(R.id.txt_capture_time, AndroidGeolocationUITestData.locations[elementId].getFormattedTimestamp())
        assertContains(R.id.txt_gps_coodinates, AndroidGeolocationUITestData.locations[elementId].gps.toString())
        assertContains(R.id.txt_mls_coordinates, AndroidGeolocationUITestData.locations[elementId].mls.toString())
        assertContains(R.id.txt_diff_gps_mls, AndroidGeolocationUITestData.locations[elementId].gps.diff(AndroidGeolocationUITestData.locations[elementId].mls).toString())
        assertContains(R.id.txt_gps_accuracy, AndroidGeolocationUITestData.locations[elementId].gps.accuracy.toString())
        assertContains(R.id.txt_mls_accuracy, AndroidGeolocationUITestData.locations[elementId].mls.accuracy.toString())
        assertContains(R.id.txt_mls_parameters, AndroidGeolocationUITestData.locations[elementId].params.toString())

        coVerify(exactly = 1) { mockLocationRepository.getLocations() }
        coVerify(exactly = 1) { mockLocationRepository.getLocation(AndroidGeolocationUITestData.locations[elementId].id) }
    }

    @Test
    fun deleteDetailMeasurementFromList() {
        val elementId = 3

        coEvery { mockLocationRepository.getLocations() } returns Result.Success(AndroidGeolocationUITestData.locations)
        coEvery { mockLocationRepository.deleteLocation(elementId.toLong()) }
        coEvery { mockLocationRepository.getLocation(elementId.toLong()) } returns Result.Success(AndroidGeolocationUITestData.locations[elementId])

        rule.launchActivity(null)

        clickListItem(R.id.item_list, elementId)

        assertContains(R.id.txt_capture_time, AndroidGeolocationUITestData.locations[elementId].getFormattedTimestamp())
        assertContains(R.id.txt_gps_coodinates, AndroidGeolocationUITestData.locations[elementId].gps.toString())
        assertContains(R.id.txt_mls_coordinates, AndroidGeolocationUITestData.locations[elementId].mls.toString())
        assertContains(R.id.txt_diff_gps_mls, AndroidGeolocationUITestData.locations[elementId].gps.diff(AndroidGeolocationUITestData.locations[elementId].mls).toString())
        assertContains(R.id.txt_gps_accuracy, AndroidGeolocationUITestData.locations[elementId].gps.accuracy.toString())
        assertContains(R.id.txt_mls_accuracy, AndroidGeolocationUITestData.locations[elementId].mls.accuracy.toString())
        assertContains(R.id.txt_mls_parameters, AndroidGeolocationUITestData.locations[elementId].params.toString())

        clickOn(R.id.fab_delete)

        scrollListToPosition(R.id.item_list, elementId)

        assertListItemCount(R.id.item_list, AndroidGeolocationUITestData.locations.size)

        assertNotDisplayed(R.id.item_list, elementId.toString())

        coVerify(exactly = 2) { mockLocationRepository.getLocations() }
        coVerify(exactly = 1) { mockLocationRepository.getLocation(AndroidGeolocationUITestData.locations[elementId].id) }
    }

    @Test
    fun deleteDetailMeasurementFromDetailView() {
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText("25"))))
        onView(withId(R.id.item_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(24, click()))
        onView(withId(R.id.fab_delete)).perform(click())
    }
}