package com.mskyeye.trace.constant;

/**
 * @author huangkun
 * @date 2023/4/25 11:13
 * 第三方服务常量接口
 */
public interface ThirdServiceConst {


    String SYS_ERROR_PARAMS_EMPTY = "系统错误，参数为空";

    String UNIQUE_ID_EMPTY = "唯一id不能为空";


    String PARAMS_PARSE_ERROR = "第三方服务解析参数错误，请检查参数";


    String INPUT_DATA_ERROR = "第三方输入数据错误，请检查数据流";


    String INFER_ERROR = "第三方模型推理错误，请检查模型";

    String CALL_BACK_ERROR = "回调次数超过阈值";


    String REQUEST_PREFIX = "http://";

    String REQUEST_HTTPS_PREFIX = "https://";


    String RTMP_PREFIX = "rtmp://";



    interface ErrMsgType {

        /**
         * 启动成功
         */
        String START_SUCCESS = "SUCCESS";

        /**
         * 参数解析错误
         */
        String PARAMS_PARSE_ERROR = "PARSE PARAM FAILS";

        /**
         * 输入数据错误
         */
        String INPUT_DATA_FAILS = "INPUT DATA FAILS";

        /**
         * 推理模型未正确运行
         */
        String INFER_FAILS = "INFER FAILS";
    }

    String RED_SPOT_PARAMS_KEY = "热斑阈值";

    interface RedSpotCacheConst{

        /**
         * 热斑缓存的前缀
         */
        String RED_SPOT_PREFIX = "RED_SPOT_PREFIX:";


    }


    interface  RedSpotRespErrType{

        String ERR_MSG_KEY = "errMsg";

        String RESULT_KEY = "result";

        String RESULT_PATH = "resultImgRoot";

        String TIME_FORMAT = "yyyy-MM-dd";

    }

    interface CallBackCode{
        Integer CALLBACK_EXCEED = 505;
    }

    interface LiveConst{

        String LIVE_CALLBACK_KEY = "code";

        String LIVE_RESULT_KEY = "result";

        String LIVE_SATUS_KEY = "status";

        String LIVE_ORIGINAL_STREAM_KEY = "originUrl";

        String LIVE_VHOST_KEY = "vhost";

        String LIVE_APP_KEY = "app";

        String LIVE_STREAM_KEY = "stream";

        String LIVE_URL_KEY = "url";

        String LIVE_RESP_DATA_KEY = "data";

        String LIVE_RESP_KEY = "key";

        Integer LIVE_SUCCESS = 0;  // 0标识成功

        Integer NO_LIVE_STREAM_PROXY = -500; // 没有该直播流
    }
    }
