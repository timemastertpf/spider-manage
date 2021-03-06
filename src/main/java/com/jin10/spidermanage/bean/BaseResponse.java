package com.jin10.spidermanage.bean;

import cn.hutool.core.util.ObjectUtil;
import com.jin10.spidermanage.enums.ResultCodeEnum;
import lombok.Data;

import java.io.Serializable;


@Data
public class BaseResponse implements Serializable {
    private static final String DEFAULT_SUCCESS_MESSAGE = null;
    private static final String DEFAULT_ERROR_MESSAGE = "error";


    private int status;
    private String message;
    private Object data;

    public BaseResponse() {

    }

    public BaseResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public BaseResponse(ResultCodeEnum resultCodeEnum) {
        this(resultCodeEnum.getCode(), resultCodeEnum.getMessage());
    }

    public BaseResponse(Object data) {
        this.status = 200;
        this.message = "访问成功";
        this.data = data;
    }

    public BaseResponse(ResultCodeEnum resultCodeEnum, Object data) {
        this(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    public static BaseResponse ok() {
        return new BaseResponse(ResultCodeEnum.SUCCESS);
    }

    public static BaseResponse ok(Object data) {
        return new BaseResponse(ResultCodeEnum.SUCCESS, data);
    }


    public static BaseResponse error() {

        return new BaseResponse(ResultCodeEnum.DEFAULT_ERROR);
    }


    public static BaseResponse error(String message) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.status = ResultCodeEnum.DEFAULT_ERROR.getCode();
        baseResponse.message = message;
        return baseResponse;
    }

    public static BaseResponse error(ResultCodeEnum resultCodeEnum) {
        return new BaseResponse(resultCodeEnum.getCode(), resultCodeEnum.getMessage());
    }


    public static BaseResponse error(int errorCode, String message) {
        return new BaseResponse(errorCode, message);
    }


    public static BaseResponse error(Object data) {
        return new BaseResponse(ResultCodeEnum.DEFAULT_ERROR, data);
    }


}
