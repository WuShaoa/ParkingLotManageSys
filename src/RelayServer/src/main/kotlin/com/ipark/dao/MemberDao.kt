package com.ipark.dao

import com.ipark.model.MemberInfo

interface MemberDao {
    fun queryMemberInfoByCarId(car_id:String): MemberInfo?

    /**
     * 更新会员信息时必须要实现更新整个表的功能：从表中删除会员到期的用户
     * 更新会员信息包括添加和修改信息
     */
    fun updataMemberInfo(memberInfo:MemberInfo,updataAll:()->Boolean):Boolean

    fun updataMemberInfo(memberInfo:MemberInfo)

    fun updateMemberType(memberInfo: MemberInfo,type:Int)





}