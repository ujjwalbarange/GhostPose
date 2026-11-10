package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    private lateinit var database: PoseDatabase
    private lateinit var poseDao: PoseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PoseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        poseDao = database.poseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GhostPose", appName)
    }

    @Test
    fun `poseDao insert update and delete flow works`() = runTest {
        val pose = PoseEntity(
            name = "0.5x Flash Mirror",
            imageUri = "https://example.com/pose.jpg",
            alpha = 0.4f,
            timestamp = 1000L,
            isFavorite = true
        )
        val id = poseDao.insertPose(pose)
        assertTrue(id > 0)

        val all = poseDao.getAllPoses().first()
        assertEquals(1, all.size)
        assertEquals("0.5x Flash Mirror", all.first().name)

        poseDao.updatePose(all.first().copy(name = "Updated Pose", alpha = 0.75f))
        val updated = poseDao.getPoseById(id)
        assertEquals("Updated Pose", updated?.name)
        assertEquals(0.75f, updated?.alpha ?: 0f, 0.001f)

        poseDao.deletePoseById(id)
        assertEquals(0, poseDao.getPoseCount())
    }

    @Test
    fun `extractUrlFromText extracts Pinterest shortlink from share text`() {
        val sharedText = "Check out this pose reference! https://pin.it/3xYzAbC9 via @Pinterest"
        val extracted = LinkResolver.extractUrlFromText(sharedText)
        assertEquals("https://pin.it/3xYzAbC9", extracted)
    }
}
