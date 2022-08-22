package com.ipark.dao

import com.ipark.model.BlackList


interface BlackListDao {
    /**
     * @param carid
     * @return 通过carid查询黑名单车主信息
     */
    fun queryByCarId(carid:String): BlackList?


    /**
     * @param blacklist 黑名单信息
     * @return 添加是否成功
     */
    fun insertRecord(blackllist : BlackList):Boolean
}