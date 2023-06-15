package com.shuxiang.buddymatch.model.enums;


/**
队伍状态枚举
 */
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static TeamStatusEnum getStatusByValue(Integer value){
        TeamStatusEnum res = null;
        if(value == null)
            return res;

        TeamStatusEnum[] teamStatusEnums = TeamStatusEnum.values();

        for(TeamStatusEnum teamStatusEnum : teamStatusEnums){
            if(value.equals(teamStatusEnum.getValue())){
                res = teamStatusEnum;
                break;
            }
        }

        return res;
    }
}
