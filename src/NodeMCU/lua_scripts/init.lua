if node.LFS.list() == nil then
  node.LFS.reload('lfs3.img')
end

initTimer = tmr.create()
initTimer:register(1000, tmr.ALARM_SINGLE,
    function()
        return pcall(node.LFS._init, "8-224","XCTHRM8-224")
        --return pcall(node.LFS._init, "topsecret","87654321")
    end)
initTimer:start()
