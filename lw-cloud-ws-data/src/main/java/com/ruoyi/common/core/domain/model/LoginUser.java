package com.ruoyi.common.core.domain.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mskyeye.ws.model.SysUser;

import java.util.Set;

/**
 * 登录用户身份权限
 * 
 * @author ruoyi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @JSONField(name = "userId")
    private Long userId;

    /**
     * 部门ID
     */
    @JSONField(name = "deptId")
    private Long deptId;

    /**
     * 用户唯一标识
     */
    @JSONField(name = "token")
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    @JSONField(name = "expireTime")
    private Long expireTime;

    /**
     * 登录IP地址
     */
    @JSONField(name = "ipaddr")
    private String ipaddr;

    /**
     * 登录地点
     */
    @JSONField(name = "loginLocation")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @JSONField(name = "browser")
    private String browser;

    /**
     * 操作系统
     */
    @JSONField(name = "os")
    private String os;

    /**
     * 权限列表
     */
    @JSONField(name = "permissions")
    private Set<String> permissions;

    /**
     * 用户信息
     */
    @JSONField(name = "user")
    private SysUser user;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public LoginUser()
    {
    }

    public LoginUser(SysUser user, Set<String> permissions)
    {
        this.user = user;
        this.permissions = permissions;
    }

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions)
    {
        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
    }


    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation()
    {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation)
    {
        this.loginLocation = loginLocation;
    }

    public String getBrowser()
    {
        return browser;
    }

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public SysUser getUser()
    {
        return user;
    }

    public void setUser(SysUser user)
    {
        this.user = user;
    }

}
