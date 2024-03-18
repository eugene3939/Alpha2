package com.example.alpha2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.alpha2.databinding.ActivityDownloadingBinding
import com.example.alpha2.databinding.ActivityLoginBinding
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
import java.sql.Connection

class Downloading : AppCompatActivity() {

    private var _binding: ActivityDownloadingBinding? = null
    private val binding get() = _binding!!

    //ftp client
    private lateinit var ftpClient: FTPClient

    private var connect: Connection? = null
    private var connectionResult = ""

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading)

        //FTP連線
        GlobalScope.launch(Dispatchers.IO) {
            // 在 IO 調度器中呼叫 connectFTP()
            connectFTP()

            //下載完成跳轉登入頁面
            val intent = Intent(this@Downloading, Login::class.java)
            startActivity(intent)
        }
    }

    private fun connectFTP() {
        ftpClient = FTPClient()
        try {
            //1.連線遠端FTP
            ftpClient.connect("10.60.200.16",21)
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

                downloadDirectory(ftpClient, "", devicePOSDirectory)

            } ?: run {
                Log.e("外部存儲目錄不可用", "無法獲取外部存儲目錄")
            }

        } catch (e: IOException) {
            // FTP連接失敗
            Log.e("FTP 連線失敗", "Failed to connect to FTP server: ${e.message}")
        }
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
                }
            }
        } else {
            Log.e("FTP 下載失敗", "無法獲取文件列表")
        }
    }
}