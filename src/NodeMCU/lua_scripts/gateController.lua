-- the gate controller

local gateController = {
    OPEN = "OPEN",
    CLOSE = "CLOSE",
    CONSTANT_OPEN = "CONSTANT_OPEN",
    CONSTANT_CLOSE = "CONSTANT_CLOSE",
}

function gateController:init_gate_controller(stepper_motor, delta_rotate)
    self.stepperMotor = stepper_motor

    self.state= self.CLOSE
    self.total_rotate = 0
    self.delta_rotate = delta_rotate or 2000
end

function gateController:step_move(step)
    self.stepperMotor:step(step)
    self.total_rotate = self.total_rotate + step -- for recording
    return true
end

function gateController:open_gate()
    if (self.state == self.CLOSE) then
        self:step_move(self.delta_rotate)
        self.state = self.OPEN
        return true
    elseif (self.state == self.OPEN) then
        return "GATE CONTROLLER ERROR: gate is already open."
    elseif (self.state == self.CONSTANT_OPEN or self.state == self.CONSTANT_CLOSE) then
        return "GATE CONTROLLER ERROR: blocked gate."
    else
        return "GATE CONTROLLER ERROR: unknown state."
    end
    return false
end

function gateController:close_gate()
    if (self.state == self.CLOSE) then
        return "GATE CONTROLLER ERROR: gate is already closed."
    elseif (self.state == self.OPEN) then
        self:step_move(-self.delta_rotate)
        self.state = self.CLOSE
        return true
    elseif (self.state == self.CONSTANT_CLOSE or self.state == self.CONSTANT_OPEN) then
        return "GATE CONTROLLER ERROR: blocked gate."
    else
        return "GATE CONTROLLER ERROR: unknown state."
    end
    return false
end

function gateController:auto_gate(t)
    local ret = false
    ret = self:open_gate()
    tmr.create():delay(t * 1000) -- in ms
    ret = self:close_gate()
    return ret
end

function gateController:constant_close_enable(on)
    if (self.state == self.CONSTANT_CLOSE) then
        return "GATE CONTROLLER ERROR: blocked gate."
    end
    if (on == "on" or on == "true" or on == "1") then
        self.state= self.CONSTANT_CLOSE
        if self.total_rotate ~= 0 then 
            self:step_move(-self.total_rotate)
        end
    else
        if self.total_rotate ~= 0 then
            self:step_move(-self.total_rotate)
        end
        self.state= self.CLOSE
    end
    return tostring(on) .. " ok"
end

function gateController:constant_open_enable(on)
    if (self.state == self.CONSTANT_CLOSE) then
        return "GATE CONTROLLER ERROR: blocked gate."
    end
    if (on == "on" or on == "true" or on == "1") then
        self.state= self.CONSTANT_OPEN
        if self.total_rotate == 0 then
            self:step_move(self.delta_rotate)
        end
    else
        if self.total_rotate ~= 0 then
            self:step_move(-self.total_rotate)
        end
        self.state= self.CLOSE
    end
    return tostring(on) .. "  ok"
end

function gateController:get_state()
    return self.state
end

return gateController
