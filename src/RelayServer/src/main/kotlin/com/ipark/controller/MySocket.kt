package com.ipark.controller

import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value="/socket")
open class MySocket {
    private lateinit var session:Session
    @OnOpen
    fun onOpen(s:Session):Unit{
        session = s


    }
}