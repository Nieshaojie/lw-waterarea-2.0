package com.mskyeye.common.utils;

/**
 * @ClassName:JsonPageData
 * @Description:分页返回数据包装类
 * @Author:R.Gong
 * @Date:2022/8/8 13:45
 * @Version:1.0
 **/
public class JsonPageData {

    /**
     * 状态码 0表示成功过，1表示处理中，-1 表示失败
     */
    private Integer code;

    /**
     * 业务数据
     */
    private Object data;

    /**
     * 信息表示
     */
    private String msg;

    /**
     * 总数
     */
    private Integer total;

    public  JsonPageData(){}

    public  JsonPageData(Integer code, Object data, String msg,Integer total){
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.total = total;
    }


    /**
     * 成功，返回数据
     * @param data
     * @return
     */
    public static JsonPageData buildSuccess(Object data,Integer total){
        return new JsonPageData(ResultCode.SUCCESS.getCode(),data,ResultCode.SUCCESS.getMessage(),total);
    }


    /**
     * 失败，固定状态码
     * @param msg
     * @return
     */
    public static JsonPageData buildError(String  msg){
        return new JsonPageData(ResultCode.COMMON_FAIL.getCode() ,null,
                StringUtil.isEmpty(msg)?ResultCode.COMMON_FAIL.getMessage():msg,-1);
    }


    /**
     * 失败，自定义错误码和信息
     * @param code
     * @param msg
     * @return
     */
    public static JsonPageData buildError(Integer code , String  msg,Integer total){
        return new JsonPageData(code ,null,msg,total);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
