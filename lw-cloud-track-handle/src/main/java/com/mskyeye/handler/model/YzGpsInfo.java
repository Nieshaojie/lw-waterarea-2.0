package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:YzGpsInfo
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/10/22 20:45
 * @Version:1.0
 **/
@Data
public class YzGpsInfo {

    private String userName;

    private String pwd;

    private String loginId;//登陆ID

    private String mds;//令牌用户身份标识
}
