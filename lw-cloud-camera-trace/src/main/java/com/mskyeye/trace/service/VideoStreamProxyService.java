package com.mskyeye.trace.service;

import com.mskyeye.trace.model.YzAiCruiseInfo;
import org.geotools.ows.ServiceException;

/**
 * @author nie
 * @Description:
 * @create 2025/3/4 10:09
 */
public interface VideoStreamProxyService {

    public String startStream(String id) throws ServiceException;
}
