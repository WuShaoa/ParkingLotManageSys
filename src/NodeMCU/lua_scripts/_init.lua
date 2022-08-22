--
-- File: _init.lua
--[[

  This is a template for the LFS equivalent of the SPIFFS init.lua.

  It is a good idea to such an _init.lua module to your LFS and do most of the LFS
  module related initialisaion in this. This example uses standard Lua features to
  simplify the LFS API.

  For Lua 5.1, the first section adds a 'LFS' table to _G and uses the __index
  metamethod to resolve functions in the LFS, so you can execute the main
  function of module 'fred' by executing LFS.fred(params), etc.
  It also implements some standard readonly properties:

  LFS._time    The Unix Timestamp when the luac.cross was executed.  This can be
               used as a version identifier.

  LFS._config  This returns a table of useful configuration parameters, hence
                 print (("0x%6x"):format(LFS._config.lfs_base))
               gives you the parameter to use in the luac.cross -a option.

  LFS._list    This returns a table of the LFS modules, hence
                 print(table.concat(LFS._list,'\n'))
               gives you a single column listing of all modules in the LFS.

   For Lua 5.3 LFS table is populated by the LFS implementation in C so this part
   of the code is skipped.
---------------------------------------------------------------------------------]]
ssid, pwd, delta = ...
local lfsindex = node.LFS and node.LFS.get or node.flashindex
local G=_ENV or getfenv()
local lfs_t
if _VERSION == 'Lua 5.1' then
    lfs_t = {
    __index = function(_, name)
        local fn_ut, ba, ma, size, modules = lfsindex(name)
        if not ba then
          return fn_ut
        elseif name == '_time' then
          return fn_ut
        elseif name == '_config' then
          local fs_ma, fs_size = file.fscfg()
          return {lfs_base = ba, lfs_mapped = ma, lfs_size = size,
                  fs_mapped = fs_ma, fs_size = fs_size}
        elseif name == '_list' then
          return modules
        else
          return nil
        end
      end,

    __newindex = function(_, name, value) -- luacheck: no unused
        error("LFS is readonly. Invalid write to LFS." .. name, 2)
      end,
    }

    setmetatable(lfs_t,lfs_t)
    G.module       = nil    -- disable Lua 5.0 style modules to save RAM
    package.seeall = nil
else
    lfs_t = node.LFS
end
G.LFS = lfs_t

--[[-------------------------------------------------------------------------------
  The second section adds the LFS to the require searchlist, so that you can
  require a Lua module 'jean' in the LFS by simply doing require "jean". However
  note that this is at the search entry following the FS searcher, so if you also
  have jean.lc or jean.lua in SPIFFS, then this SPIFFS version will get loaded into
  RAM instead of using. (Useful, for development).

  See docs/en/lfs.md and the 'loaders' array in app/lua/loadlib.c for more details.

---------------------------------------------------------------------------------]]

package.loaders[3] = function(module) -- loader_flash
  return lfs_t[module]
end

--[[----------------------------------------------------------------------------
  These replace the builtins loadfile & dofile with ones which preferentially
  load from the filesystem and fall back to LFS.  Flipping the search order
  is an exercise left to the reader.-
------------------------------------------------------------------------------]]

local lf = loadfile
G.loadfile = function(n)
  if file.exists(n) then return lf(n) end
  local mod = n:match("(.*)%.l[uc]a?$")
  local fn  = mod and lfsindex(mod)
  return (fn or error (("Cannot find '%s' in FS or LFS"):format(n))) and fn
end

-- Lua's dofile (luaB_dofile) reaches directly for luaL_loadfile; shim instead
G.dofile = function(n) return assert(loadfile(n))() end

-- init.lua

----------------------
--define
---------------------
local led1 = 0
local led2 = 4
--local tmr_wifi = tmr.create()
local tmr_blink = tmr.create()
--local tmr_btn = tmr.create()
local upper_computer = {
  mid = {
    ip = "192.168.0.1",
    port = "80",
    registered = false
  },
  front = {
    ip = "192.168.0.1",
    port = "80",
    registered = false
  }
}

gpio.mode(led1, gpio.OUTPUT)
gpio.mode(led1, gpio.OUTPUT)
gpio.mode(led2, gpio.OUTPUT)
gpio.write(led1, gpio.HIGH)
gpio.write(led2, gpio.HIGH)

---------------------
-- lcd1602A
---------------------
local backend_meta = require "lc-i2c4bit"
local lc_meta = require "liquidcrystal"

gpio.mode(1,gpio.OPENDRAIN,gpio.PULLUP)
gpio.mode(2,gpio.OPENDRAIN,gpio.PULLUP)
-- create display object
local lc = lc_meta(backend_meta({sda=1,scl=2}), false, true, 16)
backend_meta = nil
lc_meta = nil;
-- define custom characters
lc:customChar(1, {0,4,31,21,14,31,10,17}) -- jia
lc:customChar(0, {0,14,10,14,10,14,8,8}) -- dan'er
lc:customChar(2, {0,15,2,15,10,14,8,15}) -- xi-l
lc:customChar(3, {0,30,8,30,10,14,2,30}) --xi-r
lc:customChar(4, {0,27,27,31,31,14,4,0}) --xin
lc:customChar(6, {0,4,31,14,21,21,21,4}) --mu
lc:customChar(5, {0,0,31,16,31,16,16,31}) --wang-l
lc:customChar(7, {0,1,2,29,2,29,2,28}) --pie'3

--lc:customChar(4, {0,0,31,1,31,1,1,31}) --wang-l

lc:clear() -- clear display
lc:blink(true) -- enable cursor blinking
lc:home() -- reset cursor position
lc:write("hello",", ", 0, 1, 2, 3) -- write string
lc:cursorMove(1, 2) -- move cursor to second line
lc:write("shaan'xi"," ", 4) -- mix text strings and characters
----lc:cursorMove(1, 2)
----lc:write("Battery level ", 2, 3, 4, 5)
lc:home()
lc:blink(false)

--for i=1,20 do print(lc:read()) end -- read back first line
--lc:home()
--for _, d in ipairs(lc:readCustom(0)) do print(d) end -- read back umbrella char
--for _, d in ipairs(lc:readCustom(1)) do print(d) end -- read back note char

---------------------
-- blink
---------------------
local blink = nil

tmr_blink:register( 100, tmr.ALARM_AUTO, function()
    gpio.write(led1, blink.i % 2)
    tmr_blink:interval( blink[blink.i + 1])
    blink.i = (blink.i + 1) % #blink
end)


function blinking(param)
    if type(param) == 'table' then
        blink = param
        blink.i = 0
        tmr_blink:interval(1)
        running, _ = tmr_blink:state()
        if running ~= true then
            tmr_blink.start()
        end
    else
        tmr_blink:stop()
        gpio.write(led1, param or gpio.LOW)
    end
end

---------------------
-- stepperMotor
---------------------
local stepperMotor = require("stepperMotor")
stepperMotor:init(0,4,6,7,1000)

---------------------
-- gateController
---------------------
local gateController = require("gateController")
gateController:init_gate_controller(stepperMotor, delta or 2000)

---------------------
-- wifi
---------------------

print('Setting up WIFI...')
wifi.setmode(wifi.STATION)
local station_cfg={}
station_cfg.ssid= ssid or "topsecret"
station_cfg.pwd= pwd or "87654321"
station_cfg.save=false
wifi.sta.config(station_cfg) 
wifi.sta.autoconnect(1)

local tmr_temp = tmr.create()
tmr_temp:alarm( 1000, tmr.ALARM_AUTO, function()
    if wifi.sta.getip() == nil then
        print('Waiting for IP ...')
    else
        print('IP is ' .. wifi.sta.getip())
        tmr_temp:stop()
    end
end)

local status=nil

-- wifi.eventmon.register(wifi.eventmon.STA_WRONGPWD, function()
--     blinking({100, 100 , 100, 500})
--     status = 'STA_WRONGPWD'
--     print(status)
-- end)

-- wifi.eventmon.register(wifi.eventmon.STA_APNOTFOUND, function()
--     blinking({2000, 2000})
--     status = 'STA_APNOTFOUND'
--     print(status)
-- end)

-- wifi.eventmon.register(wifi.eventmon.STA_CONNECTING, function(previous_State)
--     blinking({300, 300})
--     status = 'STA_CONNECTING'
--     print(status)
-- end)

wifi.eventmon.register(wifi.eventmon.STA_GOT_IP, function()
    blinking()
    status = 'STA_GOT_IP'
    print(status, wifi.sta.getip())
    mdns.register("dragonnode", {description="dragon", service="http"}) --local DNS
end)

---------------------
-- IR sensor
---------------------
local ir_pin = 5
gpio.mode(ir_pin, gpio.INT)
-- call back mid (RelayServer)
local function ir_callback(level, pulse)
  if upper_computer.mid.registered then
      local ip = "http://" .. upper_computer.mid.ip .. ":" .. upper_computer.mid.port .. "/login/cardetected"
      http.get(ip .. "?state=" .. tostring(level) , nil, function(code, data)
      print("Response from mid:")
      if (code < 0) then
        print("HTTP request failed")
      else
        print(code, data)
      end
      print("mid response end.")
    end)
  end
  if(level == 0 or level == "0" or level == false)then
    gateController:close_gate()
  end
  -- if upper_computer.front.registered then
  --     local ip = "http://" .. upper_computer.front.ip .. ":" .. upper_computer.front.port .. "/cardetected"
  --     http.get(ip .. "?state=" .. tostring(level) , nil, function(code, data)
  --     print("Response from front:")
  --     if (code < 0) then
  --       print("HTTP request failed")
  --     else
  --       print(code, data)
  --     end
  --     print("front response end.")
  --   end)
  -- end
      print(level, pulse)
end

gpio.trig(ir_pin, "both", ir_callback)
---------------------
-- http server
---------------------
local httpServer = require("httpServer")
httpServer:listen(80)

-- Custom API
-- Get text/html

httpServer:use('/welcome', function(req, res)
    lc:blink(true) -- enable cursor blinking
    lc:home() -- reset cursor position
       if(req.query.pin) then
          if(req.query.pin  == "ON1")then
              gpio.write(led1, gpio.LOW);  
            elseif(req.query.pin  == "OFF1")then
              gpio.write(led1, gpio.HIGH);  
            elseif(req.query.pin  == "ON2")then
              gpio.write(led2, gpio.LOW);  
            elseif(req.query.pin  == "OFF2")then
              gpio.write(led2, gpio.HIGH);  
            end
            lc:clear() -- clear display
            lc:write('Hello ' .. req.query.pin) -- write string
            lc:home()
            lc:blink(false)
	        res:send('Hello ' .. req.query.pin) 
        elseif(req.query.name) then 
          lc:clear() -- clear display
          local line1, line2 = string.match(req.query.name,"^(.*)#(.*)$")
          if line1 then
            lc:write(line1)
            lc:cursorMove(1, 2)
            lc:write(line2)
          else lc:write('Hello ' .. req.query.name) -- write string
          end
          lc:home()
          lc:blink(false)
          res:send('Hello ' .. req.query.name)
        elseif(req.query.step) then 
          local sn = tonumber(req.query.step)
          if sn then
            stepperMotor:step(sn)
            if sn >= 0 then
            res:send('Stepper Motor positive ' .. req.query.step)
            else 
            res:send('Stepper Motor negative ' .. req.query.step)  
            end
          end
          res:send('Stepper Motor ' .. req.query.step)
        end
        lc:home()
        lc:blink(false)
        res:send('Hello')
end)

-- Get file
httpServer:use('/doge', function(req, res)
	res:sendFile('doge.jpg')
end)

-- Get json
httpServer:use('/json', function(req, res)
	res:type('application/json')
	res:send('{"doge": "smile"}')
end)

-- Redirect
httpServer:use('/redirect', function(req, res)
	res:redirect('doge.jpg')
end)

-- Get upper-computers' IP
httpServer:use('/register', function(req, res)
  if(req.query.role  == "mid")then  
    upper_computer.mid.ip = req.query.ip
    upper_computer.mid.port = req.query.port or 80
    upper_computer.mid.registered = true
  elseif(req.query.role  == "front")then
    upper_computer.front.ip = req.query.ip
    upper_computer.front.port = req.query.port or 80
    upper_computer.front.registered = true
  end
  res:send(req.query.ip .. " ok")
end)

-- quest ip
httpServer:use('/quest', function(req, res)
  if(req.query.role  == "mid")then
    if (upper_computer.mid.registered) then
      res:send(upper_computer.mid.ip)
    end
  elseif(req.query.role  == "front")then
    if (upper_computer.front.registered) then
      res:send(upper_computer.front.ip)
    end
  else
    res:send("No ip found.")
  end
end)

-- gate
httpServer:use('/gate', function(req, res)
  local r = nil
  if(req.query.command  == "step")then
    if (req.query.step) then
      r = gateController:step_move(tonumber(req.query.step))
    end
  elseif(req.query.command  == "open")then
    r = gateController:open_gate()
  elseif(req.query.command  == "close")then
    r = gateController:close_gate()
  elseif(req.query.command  == "constantopen")then
    if (req.query.on) then
      r = gateController:constant_open_enable(req.query.on)
    end
  elseif(req.query.command  == "constantclose")then
    if (req.query.on) then
      r = gateController:constant_close_enable(req.query.on)
    end
  elseif(req.query.command  == "auto")then
    if (req.query.delay) then
      r = gateController:auto_gate(tonumber(req.query.delay))
    end
  elseif(req.query.command  == "getstate")then
    res:send(gateController:get_state())
  end
  if r then
    res:send(tostring(r))
  end
  res:send("some error happened.")
end)