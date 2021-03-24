package com.hisense.gateway.library.constant;

public enum InstancePartition {
    PARTITION_INNER(0, "inner","内网"),
    PARTITION_OUTER(1, "outer","外网");

    private int code;
    private String name;
    private String description;

    InstancePartition(int code, String name,String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public static InstancePartition fromCode(Integer codeArg) {
        if (PARTITION_INNER.code == codeArg) {
            return PARTITION_INNER;
        }

        if (PARTITION_OUTER.code == codeArg) {
            return PARTITION_OUTER;
        }

        return PARTITION_INNER;
    }
}
