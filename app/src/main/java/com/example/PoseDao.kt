package com.example

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PoseDao {
    @Query("SELECT * FROM poses ORDER BY timestamp DESC")
    fun getAllPoses(): Flow<List<PoseEntity>>

    @Query("SELECT * FROM poses WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePoses(): Flow<List<PoseEntity>>

    @Query("SELECT * FROM poses WHERE id = :id LIMIT 1")
    suspend fun getPoseById(id: Long): PoseEntity?

    @Query("SELECT COUNT(*) FROM poses")
    suspend fun getPoseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPose(pose: PoseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(poses: List<PoseEntity>)

    @Update
    suspend fun updatePose(pose: PoseEntity)

    @Delete
    suspend fun deletePose(pose: PoseEntity)

    @Query("DELETE FROM poses WHERE id = :id")
    suspend fun deletePoseById(id: Long)
}
