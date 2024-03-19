package com.example.alpha2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.alpha2.databinding.ActivityDownloadingBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Downloading : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadingBinding
    //ftp client
    private lateinit var ftpClient: FTPClient

    private var totalFiles: Int = 0         //總檔案數
    private var downloadedFiles: Int = 0    //目前下載檔案數

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.downloadProBar.progress = 0

        //FTP連線
        GlobalScope.launch(Dispatchers.IO) {
            // 在 IO 調度器中呼叫 connectFTP()
            connectFTP()
        }
    }

    private fun connectFTP() {
        ftpClient = FTPClient()
        try {
            //1.連線遠端FTP
            ftpClient.connect("10.60.200.18",21)
            ftpClient.login("tester","eugenemiku")
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

            // FTP連接成功
            Log.d("FTP 連線成功", "Connected to FTP server")

            //請下載的檔案不要有中文
            // 確保外部存儲目錄可用
            val externalFilesDir = this.getExternalFilesDir(null)   //儲存到sd card
            //val externalFilesDir = requireContext().filesDir //儲存內部私有空間
            externalFilesDir?.let { externalDir ->
                // 2. 創建 DevicePOS 資料夾在外部存儲目錄中
                val devicePOSDirectory = File(externalDir, "devicePOS")
                if (!devicePOSDirectory.exists()) {
                    devicePOSDirectory.mkdirs() // 如果目錄不存在，則創建它
                } else {
                    Log.d("絕對路徑名稱", devicePOSDirectory.absolutePath)
                }
                downloadDirectoryCount(ftpClient, "")   //計算總檔案數

                downloadDirectory(ftpClient, "", devicePOSDirectory)

            } ?: run {
                Log.e("外部存儲目錄不可用", "無法獲取外部存儲目錄")
            }

        } catch (e: IOException) {
            // FTP連接失敗
            Log.e("FTP 連線失敗", "Failed to connect to FTP server: ${e.message}")
        }
    }

    private fun downloadDirectoryCount(ftpClient: FTPClient, remoteDirPath: String){
        totalFiles += countFiles(ftpClient, remoteDirPath) // 獲得總檔案數量
        Log.d("檔案總數", totalFiles.toString())
    }

    private fun downloadDirectory(ftpClient: FTPClient, remoteDirPath: String, localDir: File) {
        val files = ftpClient.listFiles(remoteDirPath)
        if (files != null) {
            for (file in files) {
                val remoteFilePath = remoteDirPath + "/" + file.name
                val localFile = File(localDir, file.name)
                if (file.isDirectory) {
                    if (!localFile.exists()) {
                        localFile.mkdirs()
                    }
                    downloadDirectory(ftpClient, remoteFilePath, localFile)
                } else {
                    val outputStream = BufferedOutputStream(FileOutputStream(localFile))
                    ftpClient.retrieveFile(remoteFilePath, outputStream)
                    outputStream.close()

                    downloadedFiles++
                    updateProgressBar(downloadedFiles, totalFiles)

                    //Log.d("下載檔案數", downloadedFiles.toString())
                }
            }
        } else {
            Log.e("FTP 下載失敗", "無法獲取文件列表")
        }
    }

    //算總檔案數
    private fun countFiles(ftpClient: FTPClient, remoteDirPath: String): Int {
        var count = 0
        val files = ftpClient.listFiles(remoteDirPath)
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    count += countFiles(ftpClient, remoteDirPath + "/" + file.name)
                } else {
                    count++
                }
            }
        }
        return count
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressBar(downloadCount: Int, totalFilesCount: Int) {
        runOnUiThread {
            try {
                if (totalFilesCount != 0 ){
                    val progress = (downloadCount.toFloat() * 100 / totalFilesCount).toInt()
                    binding.downloadProBar.incrementProgressBy(progress)
                    binding.txPercentage.text = "$progress %"

                    Log.d("進度","$progress")
                }
            } catch (e: Exception) {
                println(e)
            }

            //下載完畢，進行跳轉
            if (downloadCount == totalFilesCount) {
                Log.d("下載完畢","切換頁面")
                startActivity(Intent(this@Downloading, Login::class.java))
                finish() // 結束當前的 Activity
            }
        }
    }

}