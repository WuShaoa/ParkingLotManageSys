local stepperMotor = {
    inputPins = {},
    duration = 50
}
function stepperMotor:init(In1, In2, In3, In4, duration)
--     Record pin numbers in the inputPins array
    self.inputPins[1] = In1
    self.inputPins[2] = In2
    self.inputPins[3] = In3
    self.inputPins[4] = In4
    self.duration = duration or self.duration

--     Iterate through the inputPins array, setting each one to output mode
    for _, pin in pairs(self.inputPins) do
        gpio.mode(pin, gpio.OUTPUT, gpio.PULLUP)
    end

end

function stepperMotor:setStepDuration(duration)
    self.duration = duration or 50
end

local abs
function abs (num)
    if num >= 0 then return num else return -num end
end

function stepperMotor:step(noOfSteps)

--     /*
--         The following 2D array represents the sequence that must be
--         used to acheive rotation. The rows correspond to each step, and
--         the columns correspond to each input. L
--     */
    local s1 = {gpio.LOW, gpio.LOW, gpio.LOW, gpio.HIGH }
    local s2 = {gpio.LOW, gpio.LOW, gpio.HIGH, gpio.HIGH}
    local s3 = {gpio.LOW, gpio.LOW, gpio.HIGH, gpio.LOW }
    local s4 = {gpio.LOW, gpio.HIGH, gpio.HIGH, gpio.LOW}
    local s5 = {gpio.LOW, gpio.HIGH, gpio.LOW, gpio.LOW }
    local s6 = {gpio.HIGH, gpio.HIGH, gpio.LOW, gpio.LOW}
    local s7 = {gpio.HIGH, gpio.LOW, gpio.LOW, gpio.LOW }
    local s8 = {gpio.HIGH, gpio.LOW, gpio.LOW, gpio.HIGH}
    local sequence = {s1, s2, s3, s4, s5, s6, s7, s8}
    local position                     
    local factor = abs(noOfSteps) / noOfSteps    -- If noOfSteps is +, factor = 1. If noOfSteps is -, factor = -1 
    local noOfSteps = abs(noOfSteps)    -- If noOfSteps was in fact negative, make positive for future operations
--     /* 
--         The following algorithm runs through the sequence the specified number 
--         of times
--     */
    for sequenceNum = 1, noOfSteps/8 do
        position = 0
        while  position < 8  and  position < ( noOfSteps - sequenceNum*8 ) do       
            tmr.delay(self.duration)
            for index, pin in pairs(self.inputPins) do
                --print(self.duration)
                --print(4.5 - (3.5*factor) + (factor*position))
                --print(sequence)
                --tmr.delay(self.duration)
                gpio.write(pin, sequence[4.5 - (3.5*factor) + (factor*position)][index])
            end     
            position = position + 1
        end
    end
end

return stepperMotor
