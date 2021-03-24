--mingguilai

local format = string.format
local cjson = require("cjson.safe")
local table = require("apicast.policy.hisense_log_writer.table_util")
local policy = require("apicast.policy")
local policy_dir = "/opt/app-root/src/src/apicast/policy/hisense_log_config"

local _M = policy.new('Hisense Log Config')
local new = _M.new

local function local_log(...)
    print("API_LOGGER HILOG_CONFIG ", ...)
end

local function read_manifest(path)
    local handle = io.open(format('%s/%s', path, 'apicast-policy.json'))
    if handle then
        local contents = handle:read('*a')
        handle:close()
        return cjson.decode(contents)
    end
end

function _M.new(config)
  local self = new(config)

  local conf = config or {}

  self.enable = conf.enable
  self.maxLength = conf.maxLength
  self.serviceId = conf.serviceId
  self.advanceLog = conf.advanceLog
  return self
end

function _M:init()
end

return _M