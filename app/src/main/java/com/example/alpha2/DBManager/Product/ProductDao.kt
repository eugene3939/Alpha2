package com.example.alpha2.DBManager.Product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface ProductDao {
    //尋找全部項目的product
    @Query("SELECT * FROM Products")
    fun getAllProductTable(): MutableList<Product>?

    //尋找符合單一ID項目的Product
    @Query("SELECT * FROM Products WHERE PLU_No = :id")
    fun getProductByID(id: String): Product?

    //尋找符合單一貨號的Product
    @Query("SELECT * FROM Products WHERE pluMagNo = :magNo")
    fun getProductByMagNo(magNo: String): Product?

    //尋找符合單一商品部門(DEP_No)的Product
    @Query("SELECT * FROM Products WHERE DEP_No = :dep")
    fun getProductByDEPNo(dep: String): Product?

    //尋找符合單一商品分類(CAT_No)的Product
    @Query("SELECT * FROM Products WHERE CAT_No = :cat")
    fun getProductByCATNo(cat: String): Product?

    //尋找符合單一商品廠商(VEN_No)的Product
    @Query("SELECT * FROM Products WHERE VEN_No = :ven")
    fun getProductByVENNo(ven: String):  Product?

    //尋找符合單一商品類別(pluType)的Product
    @Query("SELECT * FROM Products WHERE PLU_Type = :plu")
    fun getProductByPluType(plu: String): MutableList<Product>?

    //尋找所有PluType的所有內容中非空值的項目
    @Query("SELECT DISTINCT pType FROM Products WHERE :columnName IS NOT NULL")
    fun getCategoryList(columnName: String): List<String>?

    //尋找所有組合商品 (MAM_CombNo非空)
    @Query("SELECT * FROM Products WHERE MAM_CombNo IS NOT NULL")
    fun getPairedList(): MutableList<Product>?

    @Insert
    suspend fun insert(merchandise: Product)

    @Query("DELETE FROM Products WHERE PLU_No = :id")
    suspend fun delete(id: String)

    //  折扣券 商品
    @Insert
    suspend fun insertCouponMain(coupon: CouponMain)

    @Query("DELETE FROM CouponMains WHERE DISC_PLU_MagNo = :pluMagNo")
    suspend fun deleteCouponMain(pluMagNo: String)

    @Query("SELECT * FROM CouponMains WHERE DISC_PLU_MagNo = :pluMagNo") //尋找符合項目的單一pluMagNo
    fun getCouponMainByPluMagNo(pluMagNo: String): CouponMain?

    //  折扣券 明細檔
    @Insert
    suspend fun insertCouponDetail(coupon: CouponDetail)

    @Query("DELETE FROM CouponDetails WHERE DISC_PLU_MagNo = :pluMagNo")
    suspend fun deleteCouponDetail(pluMagNo: String)

    @Query("DELETE FROM CouponDetails")
    suspend fun deleteAllDetail()

    @Query("SELECT * FROM CouponDetails WHERE DISC_PLU_MagNo = :pluMagNo AND FROM_DATE = :fromDate AND TO_DATE = :toDate AND SEQ_NO = :seqNo") //尋找符合項目的單一pluMagNo
    fun getCouponDetailByFullKeys(pluMagNo: String, fromDate: LocalDateTime, toDate: LocalDateTime, seqNo: Int): MutableList<CouponDetail>?


    @Query("SELECT * FROM CouponDetails WHERE DISC_PLU_MagNo = :pluMagNo") //尋找符合項目的單一pluMagNo
    fun getCouponDetailBypluMagNo(pluMagNo: String): MutableList<CouponDetail>?

    //    組合商品
    @Insert
    suspend fun insertParedSet(cm: PairedProduct)

    @Query("DELETE FROM PairedProducts WHERE CMB_No = :CMB_No")
    suspend fun deleteParedSet(CMB_No: String)

    @Query("SELECT * FROM PairedProducts WHERE CMB_No = :CMB_No AND PLU_No = :PLU_No LIMIT 1") //尋找符合項目的單一組合編號
    fun getParedSetByID(CMB_No: String, PLU_No: String): MutableList<PairedProduct>?

    //找出組合編號對應的貨號
    @Query("SELECT * FROM PairedProducts WHERE CMB_No = :CMB_No") //尋找符合項目的單一組合編號
    fun getParedSetByCMB_No(CMB_No: String): MutableList<PairedProduct>?

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