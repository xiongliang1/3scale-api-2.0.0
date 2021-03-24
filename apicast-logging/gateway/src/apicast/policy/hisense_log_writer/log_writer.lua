--mingguilai.ex
local env = require 'resty.env'

local HLOG_MAX_SHARED_KEY_COUNT = env.get("HLOG_MAX_SHARED_KEY_COUNT") or 8192 --max key count for shared memory
local HLOG_MAX_LOG_LINES_PF = env.get("HLOG_MAX_LOG_LINES_PF") or 10000 --max log line count per log file
local HLOG_OLD_FILE_EXPIRE = env.get("HLOG_OLD_FILE_EXPIRE") or 5 --expire time for old log file
local HLOG_DEL_LOCK_MAX_TIME = env.get("HLOG_DEL_LOCK_MAX_TIME") or 5
local HLOG_LOG_FILE_PATH_PREFIX = env.get("HLOG_LOG_FILE_PATH_PREFIX") or "/logs/api_invoke_record_" -- log file path prefix

local lock_options = { timeout = HLOG_DEL_LOCK_MAX_TIME }
local resty_lock = require 'resty.lock'

local logWriter = {
    stopped = false,
    inited = false,
    line_index = 0,
    last_log_file = nil,
    last_log_day = ngx.today()
}

local del_file_block = {
    lock = nil,
    queue = {},
    index = 1,
    key = "del_files_key",
    del_last_file_shced = false
}

local function local_log(...)
    if TRACE then
        print(debug.traceback())
    end
    print("API_LOGGER HILOG_WRITER DEBUG ", ...)
end

local function append_del_file(file_name)
    if not file_name then
        local_log("append_del_file 1 ", file_name)
        return
    end

    local lock = del_file_block.lock
    local elapsed, lock_err = lock:lock(del_file_block.key)
    if not elapsed then
        ngx.log(ngx.ERR, "API_LOGGER failed to acquire the lock: ", lock_err)
        return
    end

    del_file_block.queue[del_file_block.index] = file_name
    del_file_block.index = del_file_block.index + 1

    --local_log("append_del_file 3 ", del_file_block.index)

    local ok, unlock_err = lock:unlock()
    if not ok then
        ngx.log(ngx.ERR, 'API_LOGGER failed to unlock: ', unlock_err)
        return
    end
    --local_log("append_del_file add old file to queue ", file_name)
end

local function remove_del_files()
    --local_log("try to remove 1 ", #del_file_block.queue, " files")
    local lock = del_file_block.lock
    local elapsed, lock_err = lock:lock(del_file_block.key)
    if not elapsed then
        ngx.log(ngx.ERR, "API_LOGGER failed to acquire the lock: ", lock_err)
        return
    end

    --local_log("try to remove 2 ", #del_file_block.queue, " files")
    for index = 1, #del_file_block.queue do
        local file_path = del_file_block.queue[index]
        local result = os.remove(file_path)
        del_file_block.queue[index] = nil
        local_log(file_path, " removed ", result)
    end

    del_file_block.queue = {}
    del_file_block.index = 1

    local ok, unlock_err = lock:unlock()
    if not ok then
        ngx.log(ngx.ERR, 'API_LOGGER failed to unlock: ', unlock_err)
        return
    end
end

local function split(input, delimiter)
    input = tostring(input)
    delimiter = tostring(delimiter)
    if (delimiter == "") then
        return false
    end
    local pos, arr = 0, {}
    for st, sp in function()
        return string.find(input, delimiter, pos, true)
    end do
        table.insert(arr, string.sub(input, pos, st - 1))
        pos = sp + 1
    end
    table.insert(arr, string.sub(input, pos))
    return arr
end

local function remove_log_files()
    local handle = assert(io.popen("ls " .. HLOG_LOG_FILE_PATH_PREFIX .. "*", 'r'))
    local file_list = assert(handle:read('*a'))
    handle:close()

    file_list = string.gsub(file_list, '^%s+', '')
    file_list = string.gsub(file_list, '%s+$', '')
    file_list = string.gsub(file_list, '[\n\r]+', ',')

    local_log("file_list ", file_list)

    for key, value in ipairs(split(file_list, ",")) do
        if value and string.len(value) >= 5 then
            local_log("Try to delete " .. tostring(value))
            os.remove(tostring(value))
        end
    end
end

function logWriter:new(object, config)
    object = object or {}
    self.__index = self
    setmetatable(object, self)
    return object
end

function logWriter:start()
    if not self.inited then
        local spawn = ngx.thread.spawn
        self.notify = require("ngx.semaphore").new(0)

        local runnable = function()
            while not self.stopped do
                local ok, err = self.notify:wait(1000)
                if not ok then
                    --print("API_LOGGER consumer failed: ", err)
                    ngx.sleep(0.1)
                    goto continue
                end
                --print("API_LOGGER consumer resume")
                self:saveData()
                :: continue ::
            end
        end

        self.thread = spawn(runnable)
        print("API_LOGGER started ")

        self.dataDict = ngx.shared.dict_hisense_log_buffer_main

        local lock, new_lock_err = resty_lock:new('dict_hisense_log_del_file', lock_options)
        if not lock then
            ngx.log(ngx.ERR, 'API_LOGGER failed to create lock: ', new_lock_err)
            return
        end
        del_file_block.lock = lock

        self.inited = true
    end
end

function logWriter:stop()
end

function logWriter:post(logkey, data)
    self.dataDict:lpush(logkey, data)
    self.notify:post()
end

function logWriter:saveData()
    local keys = self.dataDict:get_keys(HLOG_MAX_SHARED_KEY_COUNT)
    if not keys then
        local_log("No keys found")
        return
    end

    --print(string.format("API_LOGGER consumer try to save [%s] items", len))
    for index = 1, #keys do
        local key = keys[index]
        local len, err = self.dataDict:llen(key)
        if err then
            goto continue
        end

        for index2 = 1, len do
            local data = self.dataDict:lpop(key)
            --print("API_LOGGER write data", tostring(data))
            self:log_process(key, data)
        end
        :: continue ::
    end
end

function logWriter:log_process(api_key, content)
    local file_path = self:get_filepath(api_key)
    local _file = assert(io.open(file_path, 'a+'))
    _file:write(content)
    _file:write("\n")
    _file:close()
end

function logWriter:get_filepath(api_key)
    local today = ngx.today()
    if self.last_log_day ~= today then
        self.line_index = 0
        self.last_log_day = today
    end

    local current_file = string.format("%s_%s_%s.log",
            HLOG_LOG_FILE_PATH_PREFIX, today, math.modf(self.line_index / HLOG_MAX_LOG_LINES_PF))

    if not self.last_log_file then
        self.last_log_file = current_file
    end

    if self.last_log_file and current_file ~= self.last_log_file then
        --local_log("append old file ", self.last_log_file, " to delete queue")
        append_del_file(self.last_log_file)

        self.last_log_file = current_file
        if del_file_block.del_last_file_shced == false then
            del_file_block.del_last_file_shced = true
            --local_log("shchedule remove files after ", HLOG_OLD_FILE_EXPIRE, " seconds")
            ngx.timer.at(HLOG_OLD_FILE_EXPIRE, function(premuture)
                local_log("shcheduled to start remove files")
                remove_del_files()
                del_file_block.del_last_file_shced = false
            end)
        end
    end

    self.line_index = self.line_index + 1
    return current_file
end

local _M = {
}

function _M.new(config)
    print("API_LOGGER initialized")
    return logWriter:new(config)
end

local_log("HLOG_MAX_SHARED_KEY_COUNT=", HLOG_MAX_SHARED_KEY_COUNT)
local_log("HLOG_MAX_LOG_LINES_PF    =", HLOG_MAX_LOG_LINES_PF)
local_log("HLOG_OLD_FILE_EXPIRE     =", HLOG_OLD_FILE_EXPIRE)
local_log("HLOG_DEL_LOCK_MAX_TIME   =", HLOG_DEL_LOCK_MAX_TIME)
local_log("HLOG_LOG_FILE_PATH_PREFIX=", HLOG_LOG_FILE_PATH_PREFIX)

remove_log_files()

return _M

