package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:PatrolUserInfo
 * @Description:巡护用户信息
 * @Author:R.Gong
 * @Date:2024/5/30 14:12
 * @Version:1.0
 **/
@Data
public class PatrolUserInfo {

    private Long id;
    private String user_id;
    private String account;
    private String name;
    private String gender;
    private String email;
    private String phone;
    private String org;
    private String del;
    private String org_id;
    private String sort;
    private String pos;
}
