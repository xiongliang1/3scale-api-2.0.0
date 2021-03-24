package com.hisense.gateway.library.model.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.Result;
import lombok.Data;

import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Data
public class TimeQuery {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT, timezone = "Asia/Shanghai")
    protected Date start;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT, timezone = "Asia/Shanghai")
    private Date end;

    @JsonIgnore
    public String isValid() {
        if (start == null || end == null) {
            return "时间区间错误";
        } else {
            return Result.OK;
        }
    }
}
