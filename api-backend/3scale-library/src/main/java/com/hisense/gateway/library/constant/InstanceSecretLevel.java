package com.hisense.gateway.library.constant;

/**
 * API密级等级
 * 低，中，高
 * liyouzhi 2020/10/21
 */
public enum InstanceSecretLevel {
    LEVEL_LOW(0, "低"),
    LEVEL_MID(1, "中"),
    LEVEL_HIGH(2, "高");

    private int code;
    private String name;


    InstanceSecretLevel(int code, String name) {
        this.code = code;
        this.name = name;
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

    public static InstanceSecretLevel fromCode(Integer codeArg) {
        if(null == codeArg){
            return LEVEL_LOW;
        }else if (LEVEL_LOW.code == codeArg) {
            return LEVEL_LOW;
        }else if (LEVEL_MID.code == codeArg) {
            return LEVEL_MID;
        }else if (LEVEL_HIGH.code == codeArg) {
            return LEVEL_HIGH;
        }else{
            return LEVEL_LOW;
        }
    }


    public static InstanceSecretLevel fromName(String name) {
        if (LEVEL_LOW.name.equals(name)) {
            return LEVEL_LOW;
        }else if (LEVEL_MID.name.equals(name)) {
            return LEVEL_MID;
        }else if (LEVEL_HIGH.name.equals(name)) {
            return LEVEL_HIGH;
        }else{
            return LEVEL_LOW;
        }
    }
}
