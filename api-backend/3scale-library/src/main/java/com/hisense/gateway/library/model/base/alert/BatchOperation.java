package com.hisense.gateway.library.model.base.alert;

import lombok.Data;

import java.util.List;

@Data
public class BatchOperation {
    List<Integer> deleteIds;
    List<Integer> bindApiIds;
    List<Integer> unBindApiIds;
}
