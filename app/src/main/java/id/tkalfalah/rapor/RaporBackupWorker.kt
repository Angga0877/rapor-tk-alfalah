package id.tkalfalah.rapor

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RaporBackupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("backup_config", Context.MODE_PRIVATE)
        val tree = prefs.getString("drive_tree_uri", null) ?: return Result.failure()
        val protectedPassword = prefs.getString("backup_password", null) ?: return Result.failure()
        return try {
            val password = BackupCrypto.revealPassword(protectedPassword)
            val payload = BackupCrypto.encrypt(RaporDatabase(applicationContext).loadData(), password)
            val folder = DocumentFile.fromTreeUri(applicationContext, Uri.parse(tree))
                ?: return Result.retry()
            if (!folder.canWrite()) return Result.retry()
            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            val name = "Rapor-TK-Alfalah-$stamp.rta"
            val file = folder.createFile("text/plain", name) ?: return Result.retry()
            applicationContext.contentResolver.openOutputStream(file.uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
                it.write(payload)
            } ?: return Result.retry()
            // Keep the newest 10 successful snapshots.
            val backups = folder.listFiles()
                .filter { it.isFile && it.name?.startsWith("Rapor-TK-Alfalah-") == true && it.name?.endsWith(".rta") == true }
                .sortedByDescending { it.lastModified() }
            backups.drop(10).forEach { it.delete() }
            prefs.edit().putString("last_backup_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id", "ID")).format(Date())).apply()
            Result.success()
        } catch (_: SecurityException) {
            Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
