package id.tkalfalah.rapor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RaporDatabase(context: Context) : SQLiteOpenHelper(context, "rapor_tk_alfalah.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE students(id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, nis TEXT, group_name TEXT, dob TEXT)")
        db.execSQL("CREATE TABLE assessments(id TEXT PRIMARY KEY NOT NULL, student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, element TEXT, context_note TEXT, observation TEXT, stimulation TEXT, level TEXT, date TEXT)")
        db.execSQL("CREATE INDEX idx_assessments_student ON assessments(student_id)")
        db.execSQL("CREATE TABLE growth(id TEXT PRIMARY KEY NOT NULL, student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, date TEXT, weight TEXT, height TEXT)")
        db.execSQL("CREATE INDEX idx_growth_student ON growth(student_id)")
        db.execSQL("CREATE TABLE reports(student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, period TEXT NOT NULL, auto_description TEXT, teacher_note TEXT, parent_note TEXT, PRIMARY KEY(student_id, period))")
        db.execSQL("CREATE TABLE settings(setting_key TEXT PRIMARY KEY NOT NULL, setting_value TEXT)")
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE assessments_v2(id TEXT PRIMARY KEY NOT NULL, student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, element TEXT, context_note TEXT, observation TEXT, stimulation TEXT, level TEXT, date TEXT)")
            db.execSQL("INSERT INTO assessments_v2(id,student_id,element,context_note,observation,stimulation,level,date) SELECT id,student_id,element,context_note,observation,stimulation,level,date FROM assessments")
            db.execSQL("DROP TABLE assessments")
            db.execSQL("ALTER TABLE assessments_v2 RENAME TO assessments")
            db.execSQL("CREATE INDEX idx_assessments_student ON assessments(student_id)")

            db.execSQL("CREATE TABLE growth_v2(id TEXT PRIMARY KEY NOT NULL, student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, date TEXT, weight TEXT, height TEXT)")
            db.execSQL("INSERT INTO growth_v2(id,student_id,date,weight,height) SELECT CAST(id AS TEXT),student_id,date,weight,height FROM growth")
            db.execSQL("DROP TABLE growth")
            db.execSQL("ALTER TABLE growth_v2 RENAME TO growth")
            db.execSQL("CREATE INDEX idx_growth_student ON growth(student_id)")

            db.execSQL("CREATE TABLE reports_v2(student_id TEXT NOT NULL REFERENCES students(id) ON DELETE CASCADE, period TEXT NOT NULL, auto_description TEXT, teacher_note TEXT, parent_note TEXT, PRIMARY KEY(student_id, period))")
            db.execSQL("INSERT OR REPLACE INTO reports_v2(student_id,period,auto_description,teacher_note,parent_note) SELECT student_id,COALESCE(period,''),auto_description,teacher_note,parent_note FROM reports")
            db.execSQL("DROP TABLE reports")
            db.execSQL("ALTER TABLE reports_v2 RENAME TO reports")
        }
    }

    private fun readRows(db: SQLiteDatabase, sql: String, fields: Array<String>): JSONArray {
        val result = JSONArray()
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val row = JSONObject()
                fields.forEachIndexed { index, name ->
                    row.put(name, if (cursor.isNull(index)) "" else cursor.getString(index))
                }
                result.put(row)
            }
        }
        return result
    }

    @Synchronized
    fun loadData(): String {
        val db = readableDatabase
        val root = JSONObject()
        root.put("students", readRows(db,
            "SELECT id,name,nis,group_name,dob FROM students ORDER BY name COLLATE NOCASE",
            arrayOf("id", "name", "nis", "group", "dob")))
        root.put("assessments", readRows(db,
            "SELECT id,student_id,element,context_note,observation,stimulation,level,date FROM assessments ORDER BY date,id",
            arrayOf("id", "student", "element", "context", "observation", "stimulation", "level", "date")))
        root.put("growth", readRows(db,
            "SELECT id,student_id,date,weight,height FROM growth ORDER BY date",
            arrayOf("id", "student", "date", "weight", "height")))
        root.put("reports", readRows(db,
            "SELECT student_id,auto_description,teacher_note,parent_note,period FROM reports",
            arrayOf("student", "autoDescription", "teacherNote", "parentNote", "period")))
        val settings = JSONObject()
        db.rawQuery("SELECT setting_key,setting_value FROM settings", null).use { cursor ->
            while (cursor.moveToNext()) settings.put(cursor.getString(0), cursor.getString(1) ?: "")
        }
        root.put("settings", settings)
        return root.toString()
    }

    @Synchronized
    fun saveData(json: String): Boolean {
        return try {
            val root = JSONObject(json)
            val students = root.optJSONArray("students") ?: JSONArray()
            val assessments = root.optJSONArray("assessments") ?: JSONArray()
            val growth = root.optJSONArray("growth") ?: JSONArray()
            val reports = root.optJSONArray("reports") ?: JSONArray()
            val settings = root.optJSONObject("settings") ?: JSONObject()
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete("assessments", null, null)
                db.delete("growth", null, null)
                db.delete("reports", null, null)
                db.delete("students", null, null)
                db.delete("settings", null, null)

                for (i in 0 until students.length()) {
                    val x = students.optJSONObject(i) ?: continue
                    val v = ContentValues().apply {
                        put("id", x.optString("id"))
                        put("name", x.optString("name"))
                        put("nis", x.optString("nis"))
                        put("group_name", x.optString("group"))
                        put("dob", x.optString("dob"))
                    }
                    db.insertOrThrow("students", null, v)
                }
                for (i in 0 until assessments.length()) {
                    val x = assessments.optJSONObject(i) ?: continue
                    val v = ContentValues().apply {
                        put("id", x.optString("id"))
                        put("student_id", x.optString("student"))
                        put("element", x.optString("element"))
                        put("context_note", x.optString("context"))
                        put("observation", x.optString("observation"))
                        put("stimulation", x.optString("stimulation"))
                        put("level", x.optString("level"))
                        put("date", x.optString("date"))
                    }
                    db.insertOrThrow("assessments", null, v)
                }
                for (i in 0 until growth.length()) {
                    val x = growth.optJSONObject(i) ?: continue
                    val v = ContentValues().apply {
                        put("id", x.optString("id").ifBlank { UUID.randomUUID().toString() })
                        put("student_id", x.optString("student"))
                        put("date", x.optString("date"))
                        put("weight", x.optString("weight"))
                        put("height", x.optString("height"))
                    }
                    db.insertOrThrow("growth", null, v)
                }
                for (i in 0 until reports.length()) {
                    val x = reports.optJSONObject(i) ?: continue
                    val v = ContentValues().apply {
                        put("student_id", x.optString("student"))
                        put("period", x.optString("period"))
                        put("auto_description", x.optString("autoDescription"))
                        put("teacher_note", x.optString("teacherNote"))
                        put("parent_note", x.optString("parentNote"))
                    }
                    db.insertOrThrow("reports", null, v)
                }
                val keys = settings.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val v = ContentValues().apply {
                        put("setting_key", key)
                        put("setting_value", settings.optString(key))
                    }
                    db.insertOrThrow("settings", null, v)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
