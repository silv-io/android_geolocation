package at.tuwien.android_geolocation

import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import at.tuwien.android_geolocation.data.AndroidGeolocationUITestData
import at.tuwien.android_geolocation.service.repository.LocationRepository
import at.tuwien.android_geolocation.util.Result
import at.tuwien.android_geolocation.view.ui.location.LocationActivity
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import com.schibsted.spain.barista.interaction.BaristaListInteractions.scrollListToPosition
import com.tuwien.geolocation_android.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
    @Rule
    @JvmField
    val rule = ActivityTestRule(LocationActivity::class.java, true, false)

    @Rule
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
        val elementId: Long = 24

        coEvery { mockLocationRepository.getLocations() } returns Result.Success(AndroidGeolocationUITestData.locations)
        coEvery { mockLocationRepository.getLocation(any()) } returns Result.Success(AndroidGeolocationUITestData.locations.find { it.id == elementId }!! )

        rule.launchActivity(null)

        clickListItem(R.id.item_list, AndroidGeolocationUITestData.locations.indexOfFirst { it.id == elementId })

        assertContains(R.id.txt_capture_time, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.getFormattedTimestamp())
        assertContains(R.id.txt_gps_coodinates, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.toString())
        assertContains(R.id.txt_mls_coordinates, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls.toString())
        assertContains(R.id.txt_diff_gps_mls, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.diff(AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls).toString())
        assertContains(R.id.txt_gps_accuracy, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.accuracy.toString())
        assertContains(R.id.txt_mls_accuracy, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls.accuracy.toString())
        assertContains(R.id.txt_mls_parameters, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.params.toString())

        coVerify(exactly = 1) { mockLocationRepository.getLocations() }
        coVerify(exactly = 1) { mockLocationRepository.getLocation(AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.id) }
    }

    @Test
    fun deleteDetailMeasurement() {
        val elementId: Long = 3
        val tempList = AndroidGeolocationUITestData.locations.toMutableList()

        coEvery { mockLocationRepository.getLocations() } returns Result.Success(tempList)
        coEvery { mockLocationRepository.deleteLocation(elementId) } answers { tempList.removeAll {it.id == elementId} }
        coEvery { mockLocationRepository.getLocation(elementId) } returns Result.Success(tempList.find { it.id == elementId }!!)

        rule.launchActivity(null)

        clickListItem(R.id.item_list, AndroidGeolocationUITestData.locations.indexOfFirst { it.id == elementId })

        assertContains(R.id.txt_capture_time, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.getFormattedTimestamp())
        assertContains(R.id.txt_gps_coodinates, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.toString())
        assertContains(R.id.txt_mls_coordinates, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls.toString())
        assertContains(R.id.txt_diff_gps_mls, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.diff(AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls).toString())
        assertContains(R.id.txt_gps_accuracy, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.gps.accuracy.toString())
        assertContains(R.id.txt_mls_accuracy, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.mls.accuracy.toString())
        assertContains(R.id.txt_mls_parameters, AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.params.toString())

        clickOn(R.id.fab_delete)

        assertListItemCount(R.id.item_list, AndroidGeolocationUITestData.locations.size - 1)

        coVerify(exactly = 2) { mockLocationRepository.getLocations() }
        coVerify(exactly = 1) { mockLocationRepository.getLocation(AndroidGeolocationUITestData.locations.find { it.id == elementId }!!.id) }
    }
}