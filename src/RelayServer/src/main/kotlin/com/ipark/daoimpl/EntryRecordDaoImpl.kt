package com.ipark.daoimpl

import com.ipark.dao.EntryRecordDao
import com.ipark.model.EntryRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service
open class EntryRecordDaoImpl: EntryRecordDao {

    @Autowired
    private lateinit var jt:JdbcTemplate

    @Transactional
    override fun queryByCarId(car_id: String): EntryRecord? {


        val sql = "select * from entry_record where car_id = ? ORDER BY ENTRANCE_TIME DESC LIMIT 1"
        return try {
            jt?.queryForObject(sql,BeanPropertyRowMapper<EntryRecord>(EntryRecord::class.java),car_id)
        }catch (e : Exception){
            println("entryrecord error")

            null
        }


    }

    override fun queryAll(): List<EntryRecord> {
        TODO("Not yet implemented")
    }

    override fun insertEntryRecord(er: EntryRecord): Int {
        val sql = "insert into entry_record values(?,?,?,?,?)"
        return jt.update(sql,
            er.car_id,
            er.car_type,
            er.entrance_time,
            er.exit_time,null)
    }

    //更新离开时间
    override fun updateOne(ts: Timestamp,carid:String): Int {
        val sql = "update entry_record set exit_time = ? where car_id = ?"
        return jt.update(sql,ts,carid)
    }


}