1-环境变量
HLOG_MAX_SHARED_KEY_COUNT 8192 --max key count for shared memory
HLOG_MAX_LOG_LINES_PF     10000 --max log line count per log file
HLOG_OLD_FILE_EXPIRE      5 --expire time for old log file
HLOG_DEL_LOCK_MAX_TIME    5 
HLOG_LOG_FILE_PATH_PREFIX "/logs/api_invoke_record_" -- log file path prefix

HLOG_MAX_SHARED_KEY_COUNT 共享内存,日志key最大8192个,用于写线程循环写日志到文件用的,不要太大
HLOG_MAX_LOG_LINES_PF     100  每1万行一个文件
HLOG_DEL_LOCK_MAX_TIME    5      旧文件列表删除时持锁最大5秒,即5秒内旧的文件要全部删除
HLOG_OLD_FILE_EXPIRE      15      要写入日志到新日志文件时，5秒后删除旧的日志文件
HLOG_LOG_FILE_PATH_PREFIX 日志文件前缀, 运行时, 日志文件名会加上日期,并自动删除旧的
APICAST_PATH_ROUTING      true   self-managed模式下, 对外的url route采用同一个

APICAST_LOG_FILE   /logs/apicast_log_file.txt
APICAST_LOG_LEVEL  info
