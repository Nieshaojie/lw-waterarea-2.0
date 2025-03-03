package com.mskyeye.trace.camera.gpl.enumc;

import com.sun.jna.Library;

public interface EM_LOGIN_SPAC_CAP_TYPE extends Library {

    int EM_LOGIN_SPEC_CAP_TCP = 0; // TCP 登录, 默认方式
    int EM_LOGIN_SPEC_CAP_ANY = 1; // 无条件登录
    int EM_LOGIN_SPEC_CAP_SERVER_CONN = 2; // 主动注册的登录
    int EM_LOGIN_SPEC_CAP_MULTICAST = 3; // 组播登录, 默认方式
    int EM_LOGIN_SPEC_CAP_UDP = 4; // UDP 方式下的登录
    int EM_LOGIN_SPEC_CAP_MAIN_CONN_ONLY = 6;
    // 只建主连接下的登录
    int EM_LOGIN_SPEC_CAP_SSL = 7; // SSL 加密方式登录
    int EM_LOGIN_SPEC_CAP_INTELLIGENT_BOX = 9;
    // 登录智能盒远程设备
    int EM_LOGIN_SPEC_CAP_NO_CONFIG = 10;
    // 登录设备后不做取配置操作
    int EM_LOGIN_SPEC_CAP_U_LOGIN = 11; // 用 U 盾设备的登录
    int EM_LOGIN_SPEC_CAP_LDAP = 12; // LDAP 方式登录
    int EM_LOGIN_SPEC_CAP_AD = 13; // AD（ActiveDirectory）登录方式
    int EM_LOGIN_SPEC_CAP_RADIUS = 14; // Radius 登录方式
    int EM_LOGIN_SPEC_CAP_SOCKET_5 = 15; // Socks5 登录方式
    int EM_LOGIN_SPEC_CAP_CLOUD = 16; // 云登录方式
    int EM_LOGIN_SPEC_CAP_AUTH_TWICE = 17; // 二次鉴权登录方式
    int EM_LOGIN_SPEC_CAP_TS = 18; // TS 码流客户端登录方式
    int EM_LOGIN_SPEC_CAP_P2P = 19; // 为 P2P 登录方式
    int EM_LOGIN_SPEC_CAP_MOBILE = 20; // 手机客户端登录
    int EM_LOGIN_SPEC_CAP_INVALID = 21;// 无效的登录方式

}
