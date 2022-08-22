package com.ipark.daoimpl

import com.ipark.dao.BlackListDao
import com.ipark.model.BlackList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
open class BlackListDaoImpl: BlackListDao {

    @Autowired
    private lateinit var jt: JdbcTemplate


    @Transactional
    override fun queryByCarId(car_id: String): BlackList? {
        val sql = "select * from black_list where car_id = ?"

        return try {
            jt?.queryForObject(sql,BeanPropertyRowMapper<BlackList>(BlackList::class.java),car_id)
        }catch (e : Exception){
            println("blacklist error: $car_id")
            null
        }


    }

    @Transactional
    override fun insertRecord(bl: BlackList): Boolean {
        var sql = "insert into black_list values(?,?,?,?)"
        return jt?.update(
            sql,
            null,
            bl.car_id,
            bl.violation_time,
            bl.violation_reason).let {
                                    when (it){
                                        0 -> false
                                        else -> true
                                    }
        }
    }
}