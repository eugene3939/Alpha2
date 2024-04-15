package com.example.alpha2.DBManager.Product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {
    //尋找全部項目的product
    @Query("SELECT * FROM Products")
    fun getAllProductTable(): MutableList<Product>?

    //尋找符合單一ID項目的Product
    @Query("SELECT * FROM Products WHERE pId = :id")
    fun getProductByID(id: String): Product?

    //尋找符合單一貨號的Product
    @Query("SELECT * FROM Products WHERE pluMagNo = :magNo")
    fun getProductByMagNo(magNo: String): Product?

    //尋找符合單一商品類別(pluType)的Product
    @Query("SELECT * FROM Products WHERE pluType = :plu")
    fun getProductByPluType(plu: String): MutableList<Product>?

    //尋找所有Ptype的所有內容中非空值的項目
    @Query("SELECT DISTINCT pType FROM Products WHERE :columnName IS NOT NULL")
    fun getCategoryList(columnName: String): List<String>?

    @Insert
    suspend fun insert(merchandise: Product)

    @Query("DELETE FROM Products WHERE pId = :id")
    suspend fun delete(id: String)

    //  折扣券 商品
    @Insert
    suspend fun insertCouponMain(coupon: CouponMain)

    @Query("DELETE FROM CouponMains WHERE disPluMagNo = :pluMagNo")
    suspend fun deleteCouponMain(pluMagNo: String)

    @Query("SELECT * FROM CouponMains WHERE disPluMagNo = :pluMagNo") //尋找符合項目的單一pluMagNo
    fun getCouponMainByPluMagNo(pluMagNo: String): CouponMain?

    //  折扣券 明細檔
    @Insert
    suspend fun insertCouponDetail(coupon: CouponDetail)

    @Query("DELETE FROM CouponDetails WHERE DISC_PLU_MagNo = :pluMagNo")
    suspend fun deleteCouponDetail(pluMagNo: String)

    @Query("SELECT * FROM CouponDetails WHERE DISC_PLU_MagNo = :pluMagNo") //尋找符合項目的單一pluMagNo
    fun getCouponDetailBypluMagNo(pluMagNo: String): CouponDetail?

    //    組合商品
    @Insert
    suspend fun insertCluster(cm: ClusterProduct)

    @Query("DELETE FROM ClusterProducts WHERE pId = :id")
    suspend fun deleteCluster(id: String)

    @Query("SELECT * FROM ClusterProducts WHERE pId = :id") //尋找符合項目的單一id
    fun getClusterByID(id: String): ClusterProduct?

    //掃描商品
    @Insert
    suspend fun insertScan(cm: ScanProduct)

    @Query("DELETE FROM ScanProducts WHERE pId = :id")
    suspend fun deleteScan(id: String)

    @Query("SELECT * FROM ScanProducts WHERE pId = :id") //尋找符合項目的單一id
    fun getScanByID(id: String): ScanProduct?

    @Query("SELECT * FROM ScanProducts")
    fun getAllScan(): MutableList<ScanProduct>?     //找所有ScanProduct
}