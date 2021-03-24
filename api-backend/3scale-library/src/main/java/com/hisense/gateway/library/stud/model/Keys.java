package com.hisense.gateway.library.stud.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Keys implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> key;
}
