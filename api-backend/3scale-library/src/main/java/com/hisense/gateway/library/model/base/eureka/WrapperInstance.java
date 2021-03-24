package com.hisense.gateway.library.model.base.eureka;

import lombok.Data;

/**
 * 单独拉取指定Instance时
 */
@Data
public class WrapperInstance {
    EurekaInstance instance;
}
