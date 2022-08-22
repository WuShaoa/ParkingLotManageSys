package com.ipark.dao

import com.ipark.model.EntryRecord
import java.sql.Timestamp

interface EntryRecordDao {
    /**
     * @param carid
     *
     */
    fun queryByCarId(carid:String):com.ipark.model.EntryRecord?
    fun queryAll():List<com.ipark.model.EntryRecord>
    fun insertEntryRecord(entryRecord:EntryRecord):Int
    fun updateOne(ts:Timestamp,carid:String):Int
}