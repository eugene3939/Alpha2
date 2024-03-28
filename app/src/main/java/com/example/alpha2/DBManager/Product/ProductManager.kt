package com.example.alpha2.DBManager.Product

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking

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

    //----------以下為折扣商品------------
    //新增商品
    fun insertDiscount(dm: DiscountProduct) {
        runBlocking {
            productDao.insertDiscount(dm)
        }
    }

    //刪除商品
    fun deleteDiscount(id: String){
        runBlocking {
            productDao.delete(id)
        }
    }

    //尋找商品id
    fun getDiscountByID(id: String): DiscountProduct?{
        return runBlocking {
            productDao.getDiscountByID(id)
        }
    }

    //---------以下為配對商品---------
    //新增商品
    fun insertCluster(cm: ClusterProduct) {
        runBlocking {
            productDao.insertCluster(cm)
        }
    }

    //刪除商品
    fun deleteCluster(id: String){
        runBlocking {
            productDao.deleteCluster(id)
        }
    }

    //尋找商品id
    fun getClusterByID(id: String): ClusterProduct?{
        return runBlocking {
            productDao.getClusterByID(id)
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