@file:JvmName("Util")
package com.ipark.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.aliyun.ocr20191230.Client
import com.aliyun.ocr20191230.models.RecognizeLicensePlateAdvanceRequest
import com.aliyun.ocr20191230.models.RecognizeLicensePlateResponse
import com.aliyun.teaopenapi.models.Config
import com.aliyun.teautil.models.RuntimeOptions
import com.ipark.daoimpl.MemberDaoImpl
import com.ipark.model.MemberInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.core.io.ClassPathResource
import java.io.*
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.sql.Timestamp
import java.util.*
import kotlin.concurrent.thread

fun calculateCharge(startTime: Timestamp, endTime: Timestamp):Float{
    //时间获取失败的措施
    if (startTime == Timestamp(0L) || endTime== Timestamp(0L)) return 0.0f
    val time:Long = endTime.time.div(1000*60).minus(startTime.time.div(1000*60))
    return if(time > 30) 0.0f else {
        queryTempStandard().toFloat() * time.div(60)
    }

}



/**
 * @paramfile 车牌图片文件
 * @return 车牌号
 */
fun getCarId(file: File, timeout:Int=10000):String {
    val province_dict = mapOf(
        '京' to "BeiJing",
        '津' to "TianJin",
        '冀' to "HeBei",
        '晋' to "ShanXi",
        '蒙' to "NeiMeng",
        '辽' to "LiaoNing",
        '吉' to "JiLin",
        '黑' to "HeiLongJiang",
        '沪' to "ShangHai",
        '苏' to "JiangSu",
        '浙' to "ZheJiang",
        '皖' to "AnHui",
        '闽' to "FuJian",
        '赣' to "JiangXi",
        '鲁' to "ShanDong",
        '豫' to "HeNan",
        '鄂' to "HuBei",
        '湘' to "HuNan",
        '粤' to "GuangDong",
        '桂' to "GuangXi",
        '琼' to "HaiNan",
        '川' to "SiChuan",
        '贵' to "GuiZhou",
        '云' to "YunNan",
        '渝' to "ChongQing",
        '藏' to "XiZang",
        '陕' to "ShaanXi",
        '甘' to "GanSu",
        '青' to "QingHai",
        '宁' to "NingXia",
        '新' to "XinJiang",
        '港' to "HongKong",
        '澳' to "Macao",
        '台' to "TaiWan"
    )

    val input = FileInputStream(file)
    val config = Config()
    config.apply {
        accessKeyId = "LTAI5tBNCeS7r9UCPFX3tTL5"
        accessKeySecret = "tUgo7T1qsenm6W6SM3nVBevaiLgvbL"
        setType("access_key")
        regionId = "cn-shanghai"
    }
    val client = Client(config)
    val runtimeOptions = RuntimeOptions()
    runtimeOptions.connectTimeout = timeout
    val json = getCarIdString(client,runtimeOptions,input)
    var jsonObject = JSONObject.parseObject(json)

    jsonObject = JSON.parseObject(jsonObject["body"].toString())
    jsonObject = JSON.parseObject(jsonObject["data"].toString())
    val jsonArray = JSON.parseArray(jsonObject["plates"].toString())
    val temp =  jsonArray[0] as JSONObject
    var id:String = temp["plateNumber"].toString()
    return "${province_dict[id[0]]?:id[0]}%23${id.subSequence(1,id.length)}"
}
private fun getCarIdString(client: Client, runtimeOptions: RuntimeOptions, input: InputStream): String? {
    var ret:String = "null"
    try {
        val request = RecognizeLicensePlateAdvanceRequest()
        request.imageURLObject = input
        val reponse: RecognizeLicensePlateResponse? = client.recognizeLicensePlateAdvance(request, runtimeOptions)
        ret = JSON.toJSONString(reponse)
        return ret
    } catch (e: Exception) {
        print(ret)
        print("alibaba error")
    }
    return null
}


fun senIp(){
    val ip= getLocalHost()?.hostAddress

    try {

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://dragonnode.local/register?role=mid&port=8080&ip=$ip")
            .build()
        val response = client.newCall(request).execute()
        println(response.body?.string())
    }catch (e : Exception){
        e.printStackTrace()
    }
}

private fun getprop(): Properties {
   return Properties().apply {
        val br: BufferedReader = BufferedReader(FileReader("ipark.properties"))
        this.load(br)
    }

}


fun queryTempStandard():String{
    val prop = getprop()
    return prop.getProperty("tempstandard")
}
fun queryNowAllowance():String{
    val prop = getprop()
    return prop.getProperty("nowallowance")
}

fun updateNowAllowance(newAllowance:String){
    val prop = getprop()
    prop.setProperty("nowallowance",newAllowance)
    FileOutputStream("ipark.properties").use {
        prop.store(it,null)
        it.close()
    }
}

fun updateTempStandard(standard:Float){
    val prop = getprop()
    prop.setProperty("tempstandard",standard.toString())
    FileOutputStream("ipark.properties").use {
        prop.store(it,null)
        it.close()
    }
}
fun  getLocalHost():InetAddress?{
    try {
        var candidateAddress:InetAddress? = null
        val  nI = NetworkInterface.getNetworkInterfaces()
        while (nI.hasMoreElements()) {
            val iface = nI.nextElement()
            // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
            for( inetAddr in iface.inetAddresses){
                // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                if (!inetAddr.isLoopbackAddress) {
                    if (inetAddr.isSiteLocalAddress) {
                        // 如果是site-local地址(site-local全球广播),大部分是真正要获取的地址
                        return inetAddr
                    }
                    // 若不是site-local地址 那就记录下该地址当作候选
                    if (candidateAddress == null) {
                        candidateAddress = inetAddr
                    }
                }
            }
        }
        // 如果出去loopback回环地之外无其它地址了，那就用原始方案
        return candidateAddress ?: InetAddress.getLocalHost()
    } catch (e:Exception) {
        e.printStackTrace();
    }
    return null

}

fun responseCarId():String?{
    val client = OkHttpClient()
    var request = Request.Builder()
        .url("http://dragoncam.local/capture")
        .build()

    val response = client.newCall(request).execute()
    FileOutputStream("./temp.jpg").use { fileOutput ->
        response.body?.byteStream().use { fileInput ->
            var bufferInput = fileInput?.buffered()
            var bufferOutput = fileOutput.buffered()
            bufferInput?.copyTo(bufferOutput)
            bufferOutput.flush()
        }
    }

    var id:String? =  getCarId(File("temp.jpg"))

    request = Request.Builder()
        .url("http://dragonnode.local/welcome?name=$id")
        .build()
    client.newCall(request).execute()

    return id?:"error"
}

fun sendWebCarId(carId: String, port: Int = 5000){
    try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://dragonnode.local/quest?role=front")
            .build()

        val response = client.newCall(request).execute()
        val ip = response.body?.string()
        println("web car id $ip")
        client.newCall(Request.Builder().url("http://$ip:$port/getcar?id=$carId").build()).execute()

    }catch (e : Exception){
        e.printStackTrace()
    }
}



fun sendRemain(type:Int, port:Int=5000, remain: String):Unit{
    //try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://dragonnode.local/quest?role=front")
            .build()
        val response = client.newCall(request).execute()
        val ip = response.body?.string()?:"10.100.34.1"
        client.newCall(Request.Builder().url("http://$ip:$port/out?type=$type&remain=$remain").build()).execute()

//    }catch (e : Exception){
//        e.printStackTrace()
//    }
}

fun queryAndUpdateCarType(memberDaoImpl:MemberDaoImpl, carId: String): Int {
    //若为会员 则 car_type为1
    var type:Int = -1;

    try {
        if (carId == "error") {
            throw IllegalAccessException("car_id not found error")
        } else {
            val m = memberDaoImpl.queryMemberInfoByCarId(carId)
            if (m == null) {
                type = -1
            } else if (m.account_end_time?.time!! < System.currentTimeMillis()) {
                memberDaoImpl.updateMemberType(m, 0) //会员已过期
                type = 0
            } else {
                type = 1
            }
        }
    } catch (e:Exception) {
        e.printStackTrace()
    }
    return type
}