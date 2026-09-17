package com.mskyeye.ws.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 第三方无人机平台(cmii)实时数据对接配置。
 * token 由 yz_biz_sys 权威维护并写入统一 Redis，本模块仅读取复用。
 * 机库/无人机清单不再静态配置，改为定时调用 get_dock_drone_sn 接口动态获取。
 *
 * @author mskyeye
 */
@Component
@ConfigurationProperties(prefix = "cmii")
public class CmiiProperties {

    /** 数据面：无人机/机库实时数据查询 */
    private String dataUrl;

    /** 请求头 tenant-id */
    private String tenantId;

    /** 请求头 x-uas-type */
    private String uasType;

    /** 统一 token 的 Redis key */
    private String tokenRedisKey;

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUasType() {
        return uasType;
    }

    public void setUasType(String uasType) {
        this.uasType = uasType;
    }

    public String getTokenRedisKey() {
        return tokenRedisKey;
    }

    public void setTokenRedisKey(String tokenRedisKey) {
        this.tokenRedisKey = tokenRedisKey;
    }
}