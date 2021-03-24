package com.hisense.gateway.library.web.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"code", "message"}, callSuper = false)
@Builder
public class ResponseError {
    private String code;//错误码
    private String message;//错误原因
}
