package com.example

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Database(entities = [PoseEntity::class], version = 1, exportSchema = false)
abstract class PoseDatabase : RoomDatabase() {
    abstract fun poseDao(): PoseDao

    companion object {
        @Volatile
        private var INSTANCE: PoseDatabase? = null
        private val dbScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): PoseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PoseDatabase::class.java,
                    "ghostpose_database.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                seedDefaultPosesIfNeeded(instance.poseDao())
                instance
            }
        }

        private fun seedDefaultPosesIfNeeded(dao: PoseDao) {
            dbScope.launch {
                if (dao.getPoseCount() == 0) {
                    val now = System.currentTimeMillis()
                    val hourMs = 3600_000L
                    val dayMs = 24 * hourMs
                    val starterPoses = listOf(
                        PoseEntity(
                            name = "0.5x Flash Mirror",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1Q5J_2hjwSgpaI66ABdUJpU0wbNP299g1uBHdGqr1vJECakfJd-pa9k-q6DHVSirPjbYVSAJZMrZSy25xZSV-rvx5_zOAid8mft836y80iNVhFc_U0UXYaBJaVY9qLuTMH1TbmhClbp5a2avgWtJwHW29HgishtaKtryNUdeJojsHosOoPHUsTZGmNY_MU88dkSYYvMQlpCrn0VBc8TfWurjqZKSRREJTkDc-qkFl",
                            alpha = 0.40f,
                            timestamp = now - 2 * hourMs,
                            isFavorite = true,
                            category = "Aesthetic",
                            tagBadge = "0.5x Wide",
                            isMirrored = true
                        ),
                        PoseEntity(
                            name = "Candid Golden Hour",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuBkni0fyiTYUHWRHUGwV0N75Pk5iARJd3FDbY23wFsEK3Z3hQOLoKvmviacOOtFzPksRX3TwDwTk47IlVoLF2tOGye1tU5lXx3EvkFYdJSVIUG6hnKavjKYEBxvwbinZB31__E02bOJTlfaw-_wrnvYUwDevfb71g8sikQEit0DKeOSnuOkpRJT2_8mh4gmFeMQ8n6XAmh6XzMm7d1ewnjoYoBdFciKrYW7ZcXRgfs8",
                            alpha = 0.45f,
                            timestamp = now - 1 * dayMs,
                            isFavorite = true,
                            category = "Portraits",
                            tagBadge = "Outdoor",
                            isMirrored = false
                        ),
                        PoseEntity(
                            name = "Low Angle Streetwear",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuBEe3uLUzm9nzipL438uNw1R0iaNVYNWduLwhnzdxMNU3V8CmirpY106sKfJFLfU8g4A7EefuWhFhI252H5LsI_uPn5smgQ1VdfKcwBwExcYHR5irWC-qEoTs2GRTu-fWVVySF_h2aqHQHSjfgcgRinaz8hIFtAtiYaEz8HPgZHY4bQZQ_TmgzgGb8WuWK7ADIL6tpYd1ME8qCNnE9b6dLe2R7sqa_wchZXhUgdoWOB",
                            alpha = 0.38f,
                            timestamp = now - 3 * dayMs,
                            isFavorite = false,
                            category = "Full Body",
                            tagBadge = "Full Body",
                            isMirrored = false
                        ),
                        PoseEntity(
                            name = "Cafe Window Silhouette",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuADZX3rDr46a3-YFygGcwf_1wOtpvmUDv-QBvReJ8p_8mwFFr92yJCwbl_q95g_UHXhcZj6SnCr9EWLnjZHlZZEm7Fywx8HsZFLxRb5tY07h6-_7UJlc89Tt_qeH3ZlSsbLGKFE85OPwr31QjZP-TQyyxGLPcsOryug4N8oZxSj9_Wcfl1hwa9gm1cE_L6QabLThSbkbhUL_DgUqqdo1vWH7PGpYPDLiuOylFO3YHZZ",
                            alpha = 0.42f,
                            timestamp = now - 5 * dayMs,
                            isFavorite = true,
                            category = "Aesthetic",
                            tagBadge = "Seated",
                            isMirrored = false
                        ),
                        PoseEntity(
                            name = "Clean Headshot Angles",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuB6_wEXV0_kymuT512LKTsFY0a6obArANE18YxDZVg-SLYSbTrRyWyvakTOnyLPzOyXWyzq8SnP1qImYy6rX5XQdP2ysO2kigM4KcL_PiIowFPnec6C-_Gm8NdJKtl29a_9WZaTj7XdzZZiixva8g2YuGEg-VqWnTyzugIVnRLmzQVnDu9heqDytW7fdK8Hb8-dWuPXvWTl1VgXTTZw2Te-zHSficOJvJ02l0aduUOm",
                            alpha = 0.40f,
                            timestamp = now - 7 * dayMs,
                            isFavorite = false,
                            category = "Portraits",
                            tagBadge = "Studio",
                            isMirrored = false
                        ),
                        PoseEntity(
                            name = "Film Blur Motion",
                            imageUri = "https://lh3.googleusercontent.com/aida-public/AB6AXuCRLfC7guC-0Q1zqhYDSOOIBX3FydFZOV-PJxSgVNvZJ5dc464-5ziFuxPOH3OmCD2orCRJiPp19vsqhvjesPBJfcUsvqOcLpCTbygvX9iwD5fa61azWXfo-9pPaVQmfT2diaBfaiknmSuLgawU2P34Pa6DQk7Ogqq2uK2afkkVEEA3lSCwCYVDkrKCT4E4IROCOgdyoE3fJvcugWpg16Y1X7Rdad13_w_qoT5jFpw0",
                            alpha = 0.35f,
                            timestamp = now - 10 * dayMs,
                            isFavorite = true,
                            category = "Aesthetic",
                            tagBadge = "Motion",
                            isMirrored = false
                        )
                    )
                    dao.insertAll(starterPoses)
                }
            }
        }
    }
}

class PoseRepository(private val poseDao: PoseDao) {
    val allPoses: Flow<List<PoseEntity>> = poseDao.getAllPoses()
    val favoritePoses: Flow<List<PoseEntity>> = poseDao.getFavoritePoses()

    suspend fun insertPose(pose: PoseEntity): Long = poseDao.insertPose(pose)
    suspend fun updatePose(pose: PoseEntity) = poseDao.updatePose(pose)
    suspend fun deletePose(pose: PoseEntity) = poseDao.deletePose(pose)
    suspend fun deletePoseById(id: Long) = poseDao.deletePoseById(id)
    suspend fun getPoseById(id: Long): PoseEntity? = poseDao.getPoseById(id)
}
