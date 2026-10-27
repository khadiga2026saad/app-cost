package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.RevenueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevenueDao {

    @Query("SELECT * FROM revenues ORDER BY date DESC, revenueId DESC")
    fun getAllRevenues(): Flow<List<RevenueEntity>>

    @Query("SELECT * FROM revenues WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, revenueId DESC")
    fun getRevenuesBetween(startDate: Long, endDate: Long): Flow<List<RevenueEntity>>

    @Query("SELECT * FROM revenues WHERE sourceType = :sourceType ORDER BY date DESC")
    fun getRevenuesBySource(sourceType: String): Flow<List<RevenueEntity>>

    @Query("SELECT * FROM revenues WHERE revenueId = :id LIMIT 1")
    suspend fun getRevenueById(id: Long): RevenueEntity?

    @Query("SELECT * FROM revenues WHERE cycleKey = :cycleKey LIMIT 1")
    suspend fun getRevenueByCycleKey(cycleKey: String): RevenueEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRevenue(revenue: RevenueEntity): Long

    @Update
    suspend fun updateRevenue(revenue: RevenueEntity)

    @Delete
    suspend fun deleteRevenue(revenue: RevenueEntity)

    @Query("DELETE FROM revenues WHERE revenueId = :id")
    suspend fun deleteRevenueById(id: Long)
}
