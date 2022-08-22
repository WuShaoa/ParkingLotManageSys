package com.ipark.daoimpl

import com.ipark.dao.UserDao
import com.ipark.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class UserDaoImpl:UserDao {
    @Autowired
    private lateinit var jt: JdbcTemplate
    @Autowired
    private lateinit var user:User


    override fun queryUserByAccount(userAccount: String): User? {

        val sql:String = "select * from user_info where user_account = ?"
        try {
            return jt.queryForObject(sql,BeanPropertyRowMapper(User::class.java),userAccount)

        }catch (e : Exception){
            println("user test")
            return null
        }


    }
}