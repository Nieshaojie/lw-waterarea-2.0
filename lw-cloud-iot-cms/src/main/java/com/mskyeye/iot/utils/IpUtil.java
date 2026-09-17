package com.mskyeye.iot.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mskyeye.iot.model.IpVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * @ClassName:IpUtil
 * @Description:IP工具类
 * @Author:R.Gong
 * @Date:2022/12/2 18:05
 * @Version:1.0
 **/
@Slf4j
public class IpUtil {
    /**
     * 获取访问者的ip地址
     * 注：要外网访问才能获取到外网地址，如果你在局域网甚至本机上访问，获得的是内网或者本机的ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            //X-Forwarded-For：Squid 服务代理
            String ipAddresses = request.getHeader("X-Forwarded-For");

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //Proxy-Client-IP：apache 服务代理
                ipAddresses = request.getHeader("Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //WL-Proxy-Client-IP：weblogic 服务代理
                ipAddresses = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //HTTP_CLIENT_IP：有些代理服务器
                ipAddresses = request.getHeader("HTTP_CLIENT_IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //X-Real-IP：nginx服务代理
                ipAddresses = request.getHeader("X-Real-IP");
            }

            //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
            if (ipAddresses != null && ipAddresses.length() != 0) {
                ipAddress = ipAddresses.split(",")[0];
            }

            //还是不能获取到，最后再通过request.getRemoteAddr();获取
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * IPv4 正则校验
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    /**
     * IPv6 正则校验（简化版，覆盖常见格式）
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
                    + "|^([0-9a-fA-F]{1,4}:){1,7}:$"
                    + "|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"
                    + "|^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$"
                    + "|^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$"
                    + "|^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$"
                    + "|^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$"
                    + "|^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$"
                    + "|^:((:[0-9a-fA-F]{1,4}){1,7}|:)$");

    /**
     * 校验 IP 地址格式是否合法（IPv4 或 IPv6）
     */
    public static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        if (IPV4_PATTERN.matcher(ip).matches()) {
            return true;
        }
        if (IPV6_PATTERN.matcher(ip).matches()) {
            return true;
        }
        // 兜底：尝试 InetAddress 解析（兼容带区域标识的 IPv6，如 fe80::1%eth0）
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断是否为内网/保留 IP（RFC 1918、回环、链路本地、组播等）
     * 内网 IP 没有地理位置信息，应跳过外部查询
     */
    public static boolean isPrivateIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        // IPv6 内网/保留范围
        if (IPV6_PATTERN.matcher(ip).matches() || ip.contains(":")) {
            try {
                InetAddress addr = InetAddress.getByName(ip);
                return addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()
                        || addr.isAnyLocalAddress();
            } catch (UnknownHostException e) {
                return false;
            }
        }
        // IPv4 快速判断
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            // 10.0.0.0/8
            if (first == 10) return true;
            // 172.16.0.0/12 → 172.16 ~ 172.31
            if (first == 172 && second >= 16 && second <= 31) return true;
            // 192.168.0.0/16
            if (first == 192 && second == 168) return true;
            // 127.0.0.0/8 回环
            if (first == 127) return true;
            // 169.254.0.0/16 链路本地
            if (first == 169 && second == 254) return true;
            // 0.0.0.0/8 保留
            if (first == 0) return true;
            // 224.0.0.0/4 组播、240.0.0.0/4 保留
            if (first >= 224) return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 统一的 HTTP GET 请求方法
     */
    private static String httpGet(String urlStr, String charset) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlStr).openConnection();
        urlConnection.setConnectTimeout(3000);
        urlConnection.setReadTimeout(3000);
        urlConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        if (charset != null) {
            urlConnection.setRequestProperty("Charset", charset);
        }

        try {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), charset))) {
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * 调用太平洋网络IP地址查询Web接口（http://whois.pconline.com.cn/）
     * 返回的 JSON 可直接反序列化为 IpVo
     */
    private static IpVo queryByPconline(String ip) {
        try {
            String url = StringUtils.hasText(ip)
                    ? "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip
                    : "http://whois.pconline.com.cn/ipJson.jsp?json=true";
            String raw = httpGet(url, "GBK");
            if (!StringUtils.hasText(raw)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            return mapper.readValue(raw, IpVo.class);
        } catch (Exception e) {
            log.warn("太平洋 IP 查询接口失败, ip={}, 原因={}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 调用备用接口 ip-api.com（免费、稳定、国内可访问）
     * 字段与 IpVo 不同，需要手动映射
     */
    private static IpVo queryByIpApi(String ip) {
        try {
            String url = StringUtils.hasText(ip)
                    ? "http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=status,message,country,regionName,city,isp,org,query"
                    : "http://ip-api.com/json/?lang=zh-CN&fields=status,message,country,regionName,city,isp,org,query";
            String raw = httpGet(url, "UTF-8");
            if (!StringUtils.hasText(raw)) {
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            java.util.Map<?, ?> map = mapper.readValue(raw, java.util.Map.class);

            // 接口返回失败
            if (!"success".equals(map.get("status"))) {
                log.warn("ip-api.com 查询失败, ip={}, message={}", ip, map.get("message"));
                return null;
            }

            IpVo vo = new IpVo();
            String query = (String) map.get("query");
            String country = (String) map.get("country");
            String regionName = (String) map.get("regionName");
            String city = (String) map.get("city");
            String isp = (String) map.get("isp");

            vo.setIp(query);
            vo.setPro(country);        // 国家 → 省（降级）
            vo.setCity(regionName);    // 省/州 → 市（降级）
            vo.setRegion(city);        // 城市 → 区（降级）

            // 拼接详细地址 + 运营商
            StringBuilder addr = new StringBuilder();
            if (country != null) addr.append(country);
            if (regionName != null) addr.append(" ").append(regionName);
            if (city != null) addr.append(" ").append(city);
            if (isp != null) addr.append(" ").append(isp);
            vo.setAddr(addr.length() > 0 ? addr.toString() : null);

            return vo;
        } catch (Exception e) {
            log.warn("ip-api.com 查询异常, ip={}, 原因={}", ip, e.getMessage());
            return null;
        }
    }

    /**
     * 获取 IP 地理位置：先查太平洋，失败则降级到 ip-api.com
     * 内网 IP 直接跳过，不发起外部请求
     */
    public static IpVo getIpVo(String ip) {
        // 空参数 → 查本机，放行
        if (!StringUtils.hasText(ip)) {
            return queryByPconline(null);
        }

        // IP 格式校验
        if (!isValidIp(ip)) {
            log.warn("跳过 IP 地理位置查询：非法 IP 格式 [{}]", ip);
            return null;
        }

        // 内网 / 保留 IP 直接跳过，无地理位置信息
        if (isPrivateIp(ip)) {
            log.debug("跳过 IP 地理位置查询：内网/保留 IP [{}]", ip);
            return null;
        }

        // 主接口：太平洋
        IpVo result = queryByPconline(ip);
        if (result != null) {
            return result;
        }

        // 降级：ip-api.com
        log.info("太平洋接口不可用，降级到 ip-api.com, ip={}", ip);
        return queryByIpApi(ip);
    }

    /**
     * 直接根据访问者的Request，返回ip、地理位置
     */
    public static IpVo getIpVoByRequest(HttpServletRequest request){
        return IpUtil.getIpVo(IpUtil.getIpAddr(request));
    }

    /*
        终极大法：java获取不了，就用js来获取
        <!-- js获取客户ip -->
        <script src="http://whois.pconline.com.cn/ipJson.jsp"></script>
     */

    /*//测试
    public static void main(String[] args) {
        //获取本机ip
        System.out.println(getIpVo(null));

        //获取指定ip
        System.out.println(getIpVo("115.48.58.106"));
    }*/
}

