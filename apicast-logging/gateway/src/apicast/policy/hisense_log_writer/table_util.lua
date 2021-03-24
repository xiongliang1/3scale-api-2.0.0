--mingguilai.ex
local _M = {}
function _M:log(...)
    print("API_LOGGER ",...)
end

function _M:traceback()
    print(debug.traceback())
end

function _M:PrintTable2( tbl , level, filteDefault)
    local msg = ""
    filteDefault = filteDefault or true --默认过滤关键字（DeleteMe, _class_type）
    level = level or 1
    local indent_str = ""
    for i = 1, level do
        indent_str = indent_str.."  "
    end

    self:log(indent_str .. "{")
    for k,v in pairs(tbl) do
        if filteDefault then
            if k ~= "_class_type" and k ~= "DeleteMe" then
                local item_str = string.format("%s%s = %s", indent_str .. " ",tostring(k), tostring(v))
                self:log(item_str)
                if v and type(v) == "table" then
                    self:PrintTable2(v, level + 1)
                end
            end
        else
            local item_str = string.format("%s%s = %s", indent_str .. " ",tostring(k), tostring(v))
            self:log(item_str)
            if v and type(v) == "table" then
                self:PrintTable2(v, level + 1)
            end
        end
    end
    self:log(indent_str .. "}")
end

return _M