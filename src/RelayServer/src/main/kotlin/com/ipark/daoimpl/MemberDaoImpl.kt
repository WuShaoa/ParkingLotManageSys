package com.ipark.daoimpl

import com.ipark.dao.MemberDao
import com.ipark.model.MemberInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 会员信息表，只存储注册的会员信息
 */
@Service
open class MemberDaoImpl: MemberDao {
    @Autowired
    private lateinit var jt:JdbcTemplate

    @Transactional
    override fun queryMemberInfoByCarId(car_id: String): MemberInfo? {
        val sql = "select * from `member_info` where `car_id` = ?"
        val mI:MemberInfo?
        mI = try{
            jt?.queryForObject(sql,BeanPropertyRowMapper(MemberInfo::class.java),car_id)
        }catch (e : Exception){
            println("memberinfo error: $car_id")
            null
        }

        return mI
    }


    /**
     * 更新会员信息时必须要实现更新整个表的功能：从表中删除会员到期的用户
     * 更新会员信息包括添加和修改信息
     * update():更新已存在用户或删除过期用户
     * memberInfo.car_id!! 不能为空
     *
     */
    @Transactional
    override fun updataMemberInfo(mI: MemberInfo, updataAll: () -> Boolean):Boolean {
        return if(updataAll()){
            if(queryMemberInfoByCarId(mI.car_id!!)!=null){
                //插入会员信息
                val sql = "insert into member_info values(?,?,?,?)"
                jt.update(sql,
                    null,
                    mI.car_id,
                    mI.car_type,
                    mI.account_end_time
                )

            }else{
                //更新该会员
                val sql = "update member_info set account_end_time = ? where car_id = ?"
                jt.update(sql,mI.account_end_time,mI.car_id)
            }

            true
        }else{
            false
        }
    }

    override fun updataMemberInfo(mI: MemberInfo){
        if(queryMemberInfoByCarId(mI.car_id!!)==null){
            //插入会员信息
            val sql = "insert into member_info values(?,?,?,?)"
            jt.update(sql,
                null,
                mI.car_id,
                mI.car_type,
                mI.account_end_time
            )

        }else{
            //更新该会员
            val sql = "update member_info set account_end_time = ? where car_id = ?"
            jt.update(sql,mI.account_end_time,mI.car_id)
        }
    }

    override fun updateMemberType(mI: MemberInfo, type:Int){
            //更新该会员身份
            val sql = "update member_info set car_type = ? where car_id = ?"
            jt.update(sql,type,mI.car_id)
    }

}