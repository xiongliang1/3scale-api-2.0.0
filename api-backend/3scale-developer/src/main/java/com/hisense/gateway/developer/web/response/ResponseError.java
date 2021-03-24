package com.hisense.gateway.developer.web.response;

import lombok.*;

/**
 * @author weiwei@tenxcloud.com
 * @date   2017-07-31
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "code", "message" }, callSuper = false)
@Builder
public class ResponseError {
	
	/**
	 * 错误编码
	 */
	private String code; 
	/**
	 * 错误原因
	 */
	private String message;
	
}
