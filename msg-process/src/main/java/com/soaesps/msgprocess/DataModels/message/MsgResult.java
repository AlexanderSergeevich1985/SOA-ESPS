package com.soaesps.msgprocess.DataModels.message;

public class MsgResult extends MsgHeader {
    private boolean isSuccess;

    private String errDesc;

    public MsgResult() {}

    public MsgResult(MsgHeader msgHeader) {
        super(msgHeader);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrDesc() {
        return errDesc;
    }

    public void setErrDesc(String errDesc) {
        this.errDesc = errDesc;
    }
}