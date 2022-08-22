package com.ipark.controller

import com.ipark.constraint.LoginType
import com.ipark.daoimpl.BlackListDaoImpl
import com.ipark.daoimpl.EntryRecordDaoImpl
import com.ipark.daoimpl.MemberDaoImpl
import com.ipark.daoimpl.UserDaoImpl
import com.ipark.model.BlackList
import com.ipark.model.EntryRecord
import com.ipark.model.MemberInfo
import com.ipark.util.*


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception


import java.sql.Timestamp


@RestController
@RequestMapping("/login")
open class ServerController {


    @Autowired
    lateinit var memberDaoImpl: MemberDaoImpl

    @Autowired
    lateinit var entryRecordDaoImpl: EntryRecordDaoImpl

    @Autowired
    lateinit var blackListDaoImpl: BlackListDaoImpl

    @Autowired
    lateinit var userDaoImpl: UserDaoImpl

    @GetMapping("/user")
    open fun getUserType(
        @RequestParam("username") user_account: String,
        @RequestParam("password") user_password: String
    ): LoginType? {

        return userDaoImpl.queryUserByAccount(user_account).let {
            if (it?.user_password == user_password)
                when (it.user_role) {
                    0 -> LoginType.OrdinaryUser
                    1 -> LoginType.Boss
                    else -> null
                } else null
        }

    }


    /**
     * 返回给前端页面余量信息
     */
    @GetMapping("/remain")
    fun reponse(): String {
        return queryNowAllowance()
    }

    /**
     * 修改费用标准
     */
    @GetMapping("/standard")
    fun updateTempFee(@RequestParam("fee") fee: Float) {
        updateTempStandard(fee)
    }

    /**
     * 新增会员信息
     * @requestparam carid:车牌号
     * @requestparam time :时常
     */

    @GetMapping("/vip")
    fun updateMember(
        @RequestParam("carid") carId: String,
        @RequestParam("num") num: Long,
        @RequestParam("type") type: String
    ) {
        val _carId = carId.replace("#","%23")

        var endTime: Long = memberDaoImpl.queryMemberInfoByCarId(_carId)?.account_end_time?.time ?: 0L
        var ms: Long

        if (endTime > System.currentTimeMillis()) {
            ms = endTime;
        } else {
            ms = System.currentTimeMillis()
        }
        //计算ms
        //todo:ms不能为负值
        when (type) {
            "Day" -> {
                ms += 86400000L * num //times library
            }
            "Month" -> {
                ms += 30L * 86400000L * num
            }
        }

        val mI = MemberInfo(car_id = _carId.replace("#", "%23"), account_end_time = Timestamp(ms), car_type = 1)
        memberDaoImpl.updataMemberInfo(mI)


    }


    /**
     * 红外感应到需要抓拍，然后调用responseCarId得到车牌号
     * 得到车牌号后回传给前端
     *并且将车牌号信息记录在数据库中
     *
     */


    /**
     * 此处逻辑交由红外传感检测部分调用：当检测到车来到时调用
     * 返回-0xfffffff则为进入
     * 返回其他则为离开的金额
     */
    @GetMapping("/cardetected")
    fun sendCarIdToFront(@RequestParam(value = "state") state: String){
        //state作为自动开关闸依据
        try {
            //调用车牌识别模块,该模块获取车牌并发送给nodeMCU
            val _carId = responseCarId() ?: "error"
            //将车牌发送给前端
            if (_carId == "error") throw Exception()
            println("web car id $_carId")
            sendWebCarId(carId = _carId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @GetMapping("/car")
    fun enterOrExit(@RequestParam(value = "carid") carId: String,
                    @RequestParam(value = "type") state: String){
        val CAR_IN = "1"
        val CAR_OUT = "0"
        val _carId = carId.replace("#","%23")
        var type = queryAndUpdateCarType(memberDaoImpl, _carId)
        //进入停车场
        //先判断停车场余量,若余量不足则不执行开闸任务,并查询是否为黑名单用户
        when (state) {
            CAR_IN -> {
                println("入停车场")
                if (queryNowAllowance().toInt() != 0 &&
                    blackListDaoImpl.queryByCarId(_carId) == null) {
                    entryRecordDaoImpl.insertEntryRecord(EntryRecord(
                        car_id = _carId,
                        car_type = type,
                        entrance_time = Timestamp(System.currentTimeMillis())
                    ))
                    //修改余量
                    updateNowAllowance("${queryNowAllowance().toInt() - 1}")
                } else {
                    //黑名单
                }
            } //in
            //出停车场
            CAR_OUT -> {
                println("出停车场")
                val exit_time = System.currentTimeMillis()
                val account_end_time = memberDaoImpl.queryMemberInfoByCarId(_carId)?.account_end_time?.time ?: 0
                entryRecordDaoImpl.updateOne(Timestamp(exit_time), _carId)//记录出场时间
                var fee = 0F
                val er = entryRecordDaoImpl.queryByCarId(_carId)
                val real_type = er?.car_type ?: -1 //出场时一定可以查到

                if (real_type == 1 || account_end_time > exit_time) { //出场时是会员
                    //开闸放行
                } else if (real_type == -1 && type == -1) { //出入场时都非会员
                    fee = calculateCharge(er?.entrance_time ?: Timestamp(0L), Timestamp(exit_time))
                } else {//出场时非会员，但曾是会员
                    if (account_end_time > er?.entrance_time?.time ?: 0) {
                        fee = calculateCharge(Timestamp(account_end_time), Timestamp(exit_time))
                    } else {
                        fee = calculateCharge(er?.entrance_time ?: Timestamp(0L), Timestamp(exit_time))
                    }
                }
                sendRemain(
                    type = if (real_type <= 0) 0 else 1,
                    remain = if (real_type <= 0) fee.toString()
                    else (account_end_time - exit_time).div(86400000L).toString()
                )
                //修改余量
                updateNowAllowance("${queryNowAllowance().toInt() + 1}")
            } //out
            else -> {}
        }
    }

    @GetMapping("/black")
    fun recordBlack(
        @RequestParam("carid") carId: String,
        @RequestParam("reason") reason: String?
    ) {
        val bl =
            BlackList(car_id = carId.replace("#","%23"), violation_reason = reason, violation_time = Timestamp(System.currentTimeMillis()))
        blackListDaoImpl.insertRecord(bl)
    }

    @GetMapping("/test")
    fun test(): String {
        return "test"

    }


}
