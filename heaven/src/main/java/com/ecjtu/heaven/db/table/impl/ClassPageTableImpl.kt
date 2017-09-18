package com.ecjtu.heaven.db.table.impl

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import com.ecjtu.netcore.model.PageModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ethan_Xiang on 2017/9/15.
 */
class ClassPageTableImpl : BaseTableImpl() {
    override val sql: String = "CREATE TABLE tb_class_page (\n" +
            "    _id       INTEGER PRIMARY KEY ASC AUTOINCREMENT,\n" +
            "    next_page STRING  UNIQUE,\n" +
            "    time      STRING\n" +
            ");\n"

    companion object {
        const val TABLE_NAME = "tb_class_page"
    }
    override fun createTable(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(sql)
    }

    override fun deleteTable(sqLiteDatabase: SQLiteDatabase) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateTable(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createTable(sqLiteDatabase)
    }

    fun addPage(sqLiteDatabase: SQLiteDatabase, pageModel: PageModel) {
        val value = ContentValues()
        val format = SimpleDateFormat("yyyy-MM-dd")
        if (TextUtils.isEmpty(pageModel.nextPage)) {
            pageModel.nextPage = ""
        }
        value.put("next_page", pageModel.nextPage)
        value.put("time", format.format(Date()))
        val id = sqLiteDatabase.insert(TABLE_NAME, null, value)
        pageModel.id = id.toInt()
        val pageListTable = ClassPageListTableImpl()
        pageListTable.addPageList(sqLiteDatabase, pageModel)
    }

    fun deletePage(sqLiteDatabase: SQLiteDatabase, id: Int) {
        sqLiteDatabase.delete(TABLE_NAME, "_id=?", arrayOf(id.toString()))
    }


}