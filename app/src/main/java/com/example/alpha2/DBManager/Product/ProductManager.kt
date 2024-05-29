package com.example.alpha2.DBManager.Product

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class ProductManager(context: Context) {
    private val db: ProductDatabase = Room.databaseBuilder(
        context,
        ProductDatabase::class.java, "product-database"
    ).fallbackToDestructiveMigration().build()

    private val productDao: ProductDao = db.ProductDao()

    //-------一般商品-----------
    //新增商品
    fun insert(m: Product) {
        runBlocking {
            productDao.insert(merchandise = m)
        }
    }

    //刪除商品
    fun delete(id: String){
        runBlocking {
            productDao.delete(id)
        }
    }

    //尋找商品id
    fun getProductByID(id: String): Product?{
        return runBlocking {
            productDao.getProductByID(id)
        }
    }

    //尋找符合單一貨號的Product
    fun getProductByMagNo(magNo: String): Product?{
        return runBlocking {
            productDao.getProductByMagNo(magNo)
        }
    }

    //尋找符合單一商品部門(DEP_No)的Product
    fun getProductByDEPNo(dep: String): Product?{
        return runBlocking {
            productDao.getProductByDEPNo(dep)
        }
    }

    //尋找符合單一商品分類(CAT_No)的Product
    fun getProductByCATNo(dep: String): Product?{
        return runBlocking {
            productDao.getProductByCATNo(dep)
        }
    }

    //尋找符合單一商品廠商(VEN_No)的Product
    fun getProductByVENNo(dep: String): Product?{
        return runBlocking {
            productDao.getProductByVENNo(dep)
        }
    }

    //尋找符合商品類別(PluType)的Product
    fun getProductByPluType(plu: String): MutableList<Product>?{
        return runBlocking {
            productDao.getProductByPluType(plu)
        }
    }

    //尋找該欄位不重複的所有內容
    fun getCategoryList(columName: String): List<String>?{
        return runBlocking {
            productDao.getCategoryList(columName)
        }
    }

    //尋找全部商品
    fun getAllProductTable(): MutableList<Product>?{
        return runBlocking {
            productDao.getAllProductTable()
        }
    }

    //尋找折扣商品(主項目)
    fun getPairedList(): MutableList<Product>?{
        return runBlocking {
            productDao.getPairedList()
        }
    }

    //----------以下為折價券 商品------------
    //新增商品
    fun insertCouponMain(dm: CouponMain) {
        runBlocking {
            productDao.insertCouponMain(dm)
        }
    }

    //刪除商品
    fun deleteCouponMain(pluMagNo: String){
        runBlocking {
            productDao.delete(pluMagNo)
        }
    }

    //尋找商品id
    fun getCouponMainByPluMagNo(pluMagNo: String): CouponMain?{
        return runBlocking {
            productDao.getCouponMainByPluMagNo(pluMagNo)
        }
    }

    //---------以下為折價券明細檔---------
    //新增商品
    fun insertCouponDetail(cd: CouponDetail) {
        runBlocking {
            productDao.insertCouponDetail(cd)
        }
    }

    //刪除商品
    fun deleteCouponDetail(id: String){
        runBlocking {
            productDao.deleteCouponDetail(id)
        }
    }

    //刪除整個table
    fun deleteAllDetail(){
        runBlocking {
            productDao.deleteAllDetail()
        }
    }

    //依照複合主鍵值尋找商品id
    fun getCouponDetailBypluMagNo(id: String): MutableList<CouponDetail>?{
        return runBlocking {
            productDao.getCouponDetailBypluMagNo(id)
        }
    }

    //尋找商品id
    fun getCouponDetailByFullKeys(id: String, fromDate: LocalDateTime, toDate: LocalDateTime, seqNo: Int): MutableList<CouponDetail>?{
        return runBlocking {
            productDao.getCouponDetailByFullKeys(id,fromDate, toDate, seqNo)
        }
    }

    //---------以下為組合商品(子項目)---------

    //新增商品
    fun insertParedSet(cm: PairedProduct) {
        runBlocking {
            productDao.insertParedSet(cm)
        }
    }

    //刪除商品
    fun deleteParedSet(id: String){
        runBlocking {
            productDao.deleteParedSet(id)
        }
    }

    //尋找商品id
    fun getParedSetByID(CMB_No: String, PLU_No: String): MutableList<PairedProduct>?{
        return runBlocking {
            productDao.getParedSetByID(CMB_No,PLU_No)
        }
    }

    //---------以下為掃描商品---------
    //新增商品
    fun insertScan(cm: ScanProduct) {
        runBlocking {
            productDao.insertScan(cm)
        }
    }

    //刪除商品
    fun deleteScan(id: String){
        runBlocking {
            productDao.deleteScan(id)
        }
    }

    //尋找商品id
    fun getScanByID(id: String): ScanProduct?{
        return runBlocking {
            productDao.getScanByID(id)
        }
    }

    //尋找全部id
    fun getAllScan(): MutableList<ScanProduct>?{
        return runBlocking {
            productDao.getAllScan()
        }
    }
}