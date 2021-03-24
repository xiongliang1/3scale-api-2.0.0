package com.hisense.gateway.library.constant;

public enum OperationType {

    CREATE_API(0,"创建_"),
    PROMOTE_API(1,"发布_"),
    UPDATE_API(2,"修改_"),
    DELETE_API(3,"删除_"),
    OFFLINE_API(4,"下线_"),
    UNSUBSCRIBE_API(5,"取消订阅_"),
    PROD_PROMOTE_API(6,"一键发生产_"),
    SUCCESS(7,"成功"),
    FAILURE(8,"失败-");



    private int code;
    private String name;

    OperationType(int code, String name) {
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

    public static OperationType fromCode(Integer code){
        if(CREATE_API.code==code){
            return CREATE_API;
        }
        if(PROMOTE_API.code==code){
            return PROMOTE_API;
        }
        if(UPDATE_API.code==code){
            return UPDATE_API;
        }
        if(DELETE_API.code==code){
            return DELETE_API;
        }
        if(OFFLINE_API.code==code){
            return OFFLINE_API;
        }
        if(UNSUBSCRIBE_API.code==code){
            return UNSUBSCRIBE_API;
        }
        if(PROD_PROMOTE_API.code==code){
            return PROD_PROMOTE_API;
        }
        if(SUCCESS.code==code){
            return SUCCESS;
        }
        if(FAILURE.code==code){
            return FAILURE;
        }
        return CREATE_API;
    }
}
