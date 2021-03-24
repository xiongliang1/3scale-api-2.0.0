--mingguilai.ex
local env = require 'resty.env'
local format = string.format
local cjson = require "cjson.safe"
local table = require("table_util")
local policy = require('apicast.policy')
local writer = require("log_writer").new({})
local policy_dir = "/opt/app-root/src/src/apicast/policy/hisense_log"
local HLOG_LOG_LEVEL = env.get("HLOG_LOG_LEVEL") or "INFO"
local HLOG_LOG_ENABLE = env.get("HLOG_LOG_ENABLE") or "false"
local HLOG_NGX_WR_TIMEOUT = env.get("HLOG_NGX_WR_TIMEOUT") or 0.5

local _M = policy.new('Hisense log writer')
local new = _M.new
local TRACE = false
local DEBUG = false

local function local_error(...)
    print("API_LOGGER HILOG_WRITER ERROR ", ...)
end

local function local_log(...)
    if TRACE then
        --print(debug.traceback())
    end

    print("API_LOGGER HILOG_WRITER DEBUG ", ...)
end

local function startWriter()
    ngx.timer.at(0, function(premuture)
        writer:start()
    end)
end

local function writeLog(logkey, data)
    if ngx.ctx.log_write then
        if HLOG_LOG_LEVEL == "DEBUG" then
            local_log("dup log")
        end
        return
    end

    ngx.update_time()
    data.costAll = (ngx.now() - ngx.ctx.start_time) * 1000

    ngx.ctx.log_write = true
    ngx.timer.at(HLOG_NGX_WR_TIMEOUT, function(premuture, logkey2, data2)
        data2.id = ngx.md5(cjson.encode(data2))
        writer:post(logkey2, cjson.encode(data2))
    end, logkey, data)
    ngx.ctx.scale_log_data = {};
    ngx.ctx.scale_write_status = { count = 0 };
end

local function format_string(content)
    return tostring(content)
end

local function getUserKey(request)
    if request then
        --local_log("request ", request)
        local from, to = ngx.re.find(request, "user_key=[0-9a-z]+")

        if to and tonumber(to) > string.len("user_key=") then
            return string.sub(request, from + string.len("user_key="), to)
        else
            return nil
        end
    else
        return nil
    end
end

local function getAppId(request)
    if request then
        --local_log("request ", request)
        local from, to = ngx.re.find(request, "app_id=[0-9a-z]+")

        if to and tonumber(to) > string.len("app_id=") then
            return string.sub(request, from + string.len("app_id="), to)
        else
            return nil
        end
    else
        return nil
    end
end

local function getAppKey(request)
    if request then
        --local_log("request ", request)
        local from, to = ngx.re.find(request, "app_key=[0-9a-z]+")

        if to and tonumber(to) > string.len("app_key=") then
            return string.sub(request, from + string.len("app_key="), to)
        else
            return nil
        end
    else
        return nil
    end
end

local function get_hisense_log_config(policy_chain)
    for k, v in pairs(policy_chain) do
        if v and type(v) == "table" then
            if v.name == "hisense_log_config" then
                return v.configuration
            end
        end
    end
end

local function get_log_key()
    return tostring(ngx.ctx.service.serializable.system_name)
end

local function trace_3scale()
    if TRACE then
        local_log("ngx.arg")
        table:PrintTable2(ngx.arg, 2)

        local_log("ngx.ctx")
        table:PrintTable2(ngx.ctx, 2)

        --just function
        local_log("ngx.req")
        table:PrintTable2(ngx.req, 2)
    end

    if DEBUG then
        local_log(phase_str, "ngx.arg type=", type(ngx.header))
        table:PrintTable2(ngx.arg, 2)

        local_log(phase_str, "ngx.header type=", type(ngx.header))
        table:PrintTable2(ngx.header, 2)

        local_log(phase_str, "ngx.resp.get_headers() type=", type(ngx.header))
        table:PrintTable2(ngx.resp.get_headers(), 2)

        local_log("ngx.arg[1]", tostring(ngx.arg[1]))
    end
end

local function need_record_log()
    -- fix bug that crash caused by variable( ngx.ctx.service ) with value of nil
    if not ngx.ctx or not ngx.ctx.service or not ngx.ctx.service.serializable
            or not ngx.ctx.service.serializable.proxy
            or not ngx.ctx.service.serializable.proxy.policy_chain then
        --local_log("Invalid ngx.ctx.service.serializable.proxy.policy_chain")
        return false, null
    end

    local policy_chain = ngx.ctx.service.serializable.proxy.policy_chain
    local log_config = get_hisense_log_config(policy_chain)

    if not log_config or not log_config.enable then
        return false, null
    end

    if TRACE then
        table:PrintTable2(policy_chain, 2)
        table:PrintTable2(log_config, 2)
    end

    return true, log_config
end

local function write_log_data(phase, data, write_status)
    local record, log_config = need_record_log()

    if not record or not data or not write_status then
        if HLOG_LOG_LEVEL == "DEBUG" then
            local_log("invalid parameters ")
        end
        return
    end

    --local_log("write_status ", write_status.count)

    if phase == "rewrite" then
        --local_log("rewrite")
        ngx.update_time()
        ngx.ctx.start_time = ngx.now()
        data.startTime = ngx.localtime()
        data.timestamp = ngx.ctx.start_time * 1000
        data.apiId = log_config.serviceId

        local req_headers = ngx.req.get_headers()
        if req_headers then
            data.reqHeaders = req_headers
            data.bytesSent = req_headers["content-length"]
        end

        if log_config.advanceLog and data.bytesSent and tonumber(data.bytesSent) > 0 then
            if TRACE then
                local_log("try to read request body")
            end

            ngx.req.read_body()

            local body_data = ngx.req.get_body_data()
            local body_file = ngx.req.get_body_file()
            if not body_data then
                body_data = assert(io.open(body_file)):read('*a')
            end

            data.requestBody = body_data
            data.requestBodyReadCost = ngx.now() - ngx.ctx.start_time;
        end
    elseif phase == "access" then
        --local_log("access")
        data.serviceId = format_string(ngx.ctx.service.id)
        data.apiName = format_string(ngx.ctx.service.serializable.name)
        data.clientId = format_string(ngx.var['arg_app_id'])
        data.clientIp = format_string(ngx.var.remote_addr)
        data.remoteUser = format_string(ngx.var.remote_user)
        data.domain = format_string(ngx.var.host)
        data.requestMethod = format_string(ngx.var.request_method)
        data.requestUri = format_string(ngx.var.request_uri)
        data.httpVersion = format_string(ngx.var.server_protocol)
        --bytes_sent = format_string(ngx.var.bytes_sent)
        --bytes_received = format_string(ngx.var.bytes_received)
        data.connectionsActive = format_string(ngx.var.connections_active)
        data.httpReferer = format_string(ngx.var.http_referer)
        data.userAgent = format_string(ngx.var.http_user_agent)
        data.upstreamAddr = format_string(ngx.var.upstream_addr)
        data.httpXForwardedFor = format_string(ngx.var.http_x_forwarded_for)
        data.userKey = getUserKey(format_string(ngx.var.request_uri))
        data.appId = getAppId(format_string(ngx.var.request_uri))
        data.appKey = getAppKey(format_string(ngx.var.request_uri))
    elseif phase == "header_filter" then
        --local_log("header_filter")
        ngx.update_time()
        ngx.ctx.response_recv_time = ngx.now()
        data.responseTime = ngx.ctx.response_recv_time - ngx.ctx.request_post_time
        data.responseCode = format_string(ngx.var.status)
        data.requestTime = format_string(ngx.var.request_time)

        local response_header = ngx.resp.get_headers()
        if response_header then
            data.respHeader = response_header
            data.respConnection = response_header["connection"]
            data.respContentType = response_header["content-type"]
            data.bytesReceived = response_header["content-length"]
        end

        if not log_config.advanceLog then
            data.phase = phase
            writeLog(get_log_key(), data)
        end

        if data.responseCode ~= "200" then
            data.phase = phase
            writeLog(get_log_key(), data)
        end

    elseif phase == "body_filter" then
        if log_config.advanceLog then
            local resp_body = ngx.arg[1] or ""

            if string.len(data.responseBody or "") >= tonumber(log_config.maxLength or 0) then
                if ngx.arg[2] then
                    data.phase = phase
                    data.bytesReceived = string.len(data.responseBody);

                    ngx.update_time()
                    data.responseBodyReadCost = ngx.now() - ngx.ctx.response_recv_time

                    writeLog(get_log_key(), data)
                end

                return
            end

            data.responseBody = (data.responseBody or "") .. resp_body

            if HLOG_LOG_LEVEL == "DEBUG" then
                local_log("ngx.arg[2]=", ngx.arg[2])
                local_log("resp_body=", resp_body)
                local_log("responseBody=", data.responseBody)
            end

            if ngx.arg[2] then
                data.phase = phase
                data.bytesReceived = string.len(data.responseBody);

                ngx.update_time()
                data.responseBodyReadCost = ngx.now() - ngx.ctx.response_recv_time

                writeLog(get_log_key(), data)
            end
        end
    end
end

function _M:init()
    local_log("M:init ")
end

function _M:rewrite()
    -- change the request before it reaches upstream
    --mlocal_log("M:rewrite")
    --trace_3scale()
    if HLOG_LOG_ENABLE == "true" then
        startWriter()

        ngx.ctx.log_write = false;
        ngx.ctx.scale_log_data = {};
        ngx.ctx.scale_write_status = { count = 0 };

        ngx.update_time()
        ngx.ctx.request_post_time = ngx.now()
        write_log_data("rewrite", ngx.ctx.scale_log_data, ngx.ctx.scale_write_status)
    end
end

function _M:access()
    -- ability to deny the request before it is sent upstream
    if HLOG_LOG_ENABLE == "true" then
        write_log_data("access", ngx.ctx.scale_log_data, ngx.ctx.scale_write_status)
    end
end

function _M:content()
    -- can create content instead of connecting to upstream
    --write_log("content")
end

function _M:post_action()
    -- do something after the response was sent to the client
end

function _M:header_filter()
    -- can change response headers
    if HLOG_LOG_ENABLE == "true" then
        write_log_data("header_filter", ngx.ctx.scale_log_data, ngx.ctx.scale_write_status)
    end
end

function _M:body_filter()
    -- can read and change response body
    if HLOG_LOG_ENABLE == "true" then
        write_log_data("body_filter", ngx.ctx.scale_log_data, ngx.ctx.scale_write_status)
    end
end

function _M:log()
    --write_log("log")
end

function _M:balancer()
    --write_log("balancer")
end

local_log("loaded")
return _M
