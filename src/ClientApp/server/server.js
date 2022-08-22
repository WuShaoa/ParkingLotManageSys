const WebSocketServer = require('ws').Server
const wss = new WebSocketServer({ port: 8181 });
const express = require('express')
const app = express()
const request = require('request')

request.get({
   'url': 'http://dragonnode.local/register?role=front&port=5000&ip=10.100.34.1',
   method: 'GET',
   timeout: 5000,
}, (error, response, body) => {
   if (error) {
      console.log(error, response, body)
   }
})

app.get('/getcar', function (req, res) {
   res.send('ok')
   console.log(req.query)
   wss.on('connection', function (ws) {
      console.log('服务端：客户端已连接');
      let num = JSON.stringify({ carid: req.query.id })
      ws.send(num)
      ws.on('close', function () {
         console.log('close')
      })
   });
})

app.get('/out', function (req, res) {
   console.log(req.query)
   res.send('ok')
   wss.on('connection', function (ws) {
      console.log('服务端：客户端已连接');
      let obj = JSON.stringify({ type: req.query.type, remain: req.query.remain })
      ws.send(obj)
      setTimeout(() => {
         ws.on('close', function () {
            console.log('close')
         })
      }, 1000)
   });
})

// 启动服务器
app.listen(5000, () => console.log(':5000'))