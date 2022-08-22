package com.ipark.dao

import com.ipark.model.User

interface UserDao {
    /**
     * 返回一个用户object
     */
    fun queryUserByAccount(userAccount:String): User?
}