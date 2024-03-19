package com.example.alpha2.DBManager.Product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {
    @Query("SELECT * FROM Products")
    fun getAllMerchandiseTable(): MutableList<Product>?

    @Query("SELECT * FROM Products WHERE pId = :id") //尋找符合項目的單一id
    fun getMerchandiseByID(id: String): Product?

    @Query("SELECT * FROM Products WHERE :column = :value")  //篩選特定欄位
    fun getMerchandiseByColumn(column: String,value: String): MutableList<Product>?

    @Insert
    suspend fun insert(merchandise: Product)

    @Query("DELETE FROM Products WHERE pId = :id")
    suspend fun delete(id: String)

    //  折扣商品
    @Insert
    suspend fun insertDiscount(dm: DiscountProduct)

    @Query("DELETE FROM DiscountProducts WHERE pId = :id")
    suspend fun deleteDiscount(id: String)

    @Query("SELECT * FROM DiscountProducts WHERE pId = :id") //尋找符合項目的單一id
    fun getDiscountByID(id: String): DiscountProduct?

    //    組合商品
    @Insert
    suspend fun insertCluster(cm: ClusterProduct)

    @Query("DELETE FROM ClusterProducts WHERE pId = :id")
    suspend fun deleteCluster(id: String)

    @Query("SELECT * FROM ClusterProducts WHERE pId = :id") //尋找符合項目的單一id
    fun getClusterByID(id: String): ClusterProduct?
}