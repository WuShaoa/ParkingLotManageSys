package com.ipark.util



import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress

import java.sql.Timestamp
import java.util.*


fun main() {

    responseCarId()
//    println(Timestamp(System.currentTimeMillis()))
  //  println("GuiZhou#C797J2".replace("#", "ttt"))



}

fun onIrDetected(cb:()->Unit){
    Thread {
        val client = OkHttpClient()
        var request = Request.Builder()
            .url("http://dragonnode.local/ir/status")
            .build()
        while (true) {  //阻塞,获取红外传感器上下文
            val response = client.newCall(request).execute() //DDos???
            if (response.body?.string() == "on") cb()
        }
    }
}
class Solution {

    fun longest(text1: String, text2: String): Int {


        var dp:Array<IntArray> = Array(text1.length+1){IntArray(text2.length+1)}

        for(i in 1..text1.length){
            for(j in 1..text2.length){


                if(text1[i-1] == text2[j-1]) dp[i][j] = 1 + dp[i-1][j-1]
                else if(text1[i-1] != text2[j-1]) dp[i][j] =
                    if (dp[i-1][j]>dp[i][j-1]) dp[i-1][j] else dp[i][j-1]

            }
        }
        return dp[text1.length][text2.length]

    }



    fun _package(n:Int,V:Int):Unit{

        var record = 0
        var value = IntArray(n)
        var weight = IntArray(n)
        val read = Scanner(System.`in`)
        for(i in 0 until n){
            weight[i] = read.nextInt()
            value[i] = read.nextInt()

        }
        var dp = Array<IntArray>(n+1){IntArray(V+1)}
        for(i in 1..n){
            for(j in 1..V){
                if(j < weight[i-1]) dp[i][j] = dp[i-1][j]
                else{
                    dp[i][j] = if(dp[i-1][j] > dp[i-1][j-weight[i-1]] + value[i-1]){
                        dp[i-1][j]

                    } else{
                        record += weight[i-1]
                        dp[i-1][j-weight[i-1]] + value[i-1]
                    }
                }
            }
        }
        print(dp[n][V])
        print(" ")
        print(if(record < V) 0 else record)


    }
}

