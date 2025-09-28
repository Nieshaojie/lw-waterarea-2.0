package com.mskyeye.dataDb.config;

/**
 * @ClassName:InfluxDBConfig
 * @Description:InfluxDB配置类
 * @Author:R.Gong
 * @Date:2022/12/21 10:09
 * @Version:1.0
 **/

import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.dataDb.model.TrackInfo;
import lombok.Data;
import okhttp3.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mskyeye.dataDb.common.GlobalResources.bQueryTrackTable;
import static com.mskyeye.dataDb.common.GlobalResources.trackTableNameList;

/**
 * InfluxDB builder
 */
@Configuration
@RefreshScope
@Data
public class InfluxDBConfig {

    @Value("${influx_ip}")
    private String influxIp;

    @Value("${influx_port}")
    private String influxPort;

    private String url;

    private String DATABASE_NAME = "track_db";

    public InfluxDB influxDB = null;

    /**
     * 创建influx客户端
     *
     * @return
     */
    @Bean
    public void influxClientBuilder() {
        url = "http://" + influxIp + ":" + influxPort;
        influxDB = InfluxDBFactory.connect(url);
        try {
            // 检查数据库是否存在
            boolean databaseExists = checkDatabaseExists(influxDB, DATABASE_NAME);
            if (!databaseExists) {
                // 创建数据库
                influxDB.createDatabase(DATABASE_NAME);
            }
            influxDB.setDatabase(DATABASE_NAME);

            trackTableNameList.clear();
            //查询所有现存的表名
            if (StringUtil.isNotEmpty(influxDB)) {
                trackTableNameList.addAll(queryTableNames());
                bQueryTrackTable = true;
            }
            String tableName = getTodayTableName();
            createTableIfNotExists(url, tableName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkDatabaseExists(InfluxDB influxDB, String databaseName) {
        QueryResult queryResult = influxDB.query(new Query("SHOW DATABASES"));
        for (QueryResult.Result result : queryResult.getResults()) {
            for (QueryResult.Series series : result.getSeries()) {
                for (List<Object> values : series.getValues()) {
                    if (values.contains(databaseName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getTodayTableName() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        return "track_" + currentDate.format(formatter);
    }

    public void createTableIfNotExists(String url, String tableName) throws Exception {
        //确保已经查询过航迹表
        if (bQueryTrackTable == true && (trackTableNameList.isEmpty() || !trackTableNameList.contains(tableName))) {
            // 构建HTTP请求
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/octet-stream");
            //新建表,声明保留三个月
            String createTableQuery = String.format(
                    "CREATE TABLE IF NOT EXISTS %s (time TIMESTAMP, id Integer, station_id Integer," +
                            "source Integer,mmsi Integer,lat Double,lon Double,course Float,speed Float," +
                            "head Float,status Integer,alarm String,name String,shipType String,country String) " +
                            "WITH DURATION 90d", tableName);
            RequestBody body = RequestBody.create(mediaType, createTableQuery);
            Request request = new Request.Builder()
                    .url(url + "/query?q=" + createTableQuery)
                    .method("POST", body)
//                    .addHeader("Authorization", Credentials.basic(username, password))
                    .build();
            // 执行HTTP请求
            try {
                Response response = client.newCall(request).execute();
                System.out.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            trackTableNameList.add(tableName);
        }
    }

    public List<String> queryTableNames() {
        Query query = new Query("SHOW MEASUREMENTS");
        QueryResult queryResult = influxDB.query(query);
        List<String> list = new ArrayList<>();
        for (QueryResult.Result result : queryResult.getResults()) {
            if (result.getSeries() != null) {
                for (QueryResult.Series series : result.getSeries()) {
                    for (List<Object> values : series.getValues()) {
                        String existingTableName = (String) values.get(0);
                        list.add(existingTableName);
                    }
                }
            }
        }
        return list;
    }

    public void storeTrackData(String tableName, LocalDateTime localDateTime, TrackInfo trackInfo) {
        // 获取中国时区
        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
        Point point = Point.measurement(tableName)
                .time(localDateTime.atZone(chinaZone).toInstant().toEpochMilli() * 1_000_000, TimeUnit.NANOSECONDS)
                .addField("id", trackInfo.getId())
                .addField("station_id", trackInfo.getStationId())
                .addField("source", trackInfo.getSource())
                .addField("mmsi", trackInfo.getMmsi() != null ? trackInfo.getMmsi() : -1)
                .addField("lat", trackInfo.getLat())
                .addField("lon", trackInfo.getLon())
                .addField("course", trackInfo.getCourse() != null ? trackInfo.getCourse() : -1)
                .addField("speed", trackInfo.getSpeed() != null ? trackInfo.getSpeed() : -1)
                .addField("head", trackInfo.getHead()!= null ?trackInfo.getHead() : -1)
                .addField("status", trackInfo.getStatus()!= null ? trackInfo.getStatus() : -1)
                .addField("alarm", trackInfo.getAlarm() != null ? trackInfo.getAlarm() : "")
                .addField("name", trackInfo.getName() != null ? trackInfo.getName() : "")
                .addField("shipType", trackInfo.getShipType() != null ? trackInfo.getShipType() : "")
                .addField("country", trackInfo.getCountry() != null ? trackInfo.getCountry() : "")
                .build();

        influxDB.write(point);
    }

}
