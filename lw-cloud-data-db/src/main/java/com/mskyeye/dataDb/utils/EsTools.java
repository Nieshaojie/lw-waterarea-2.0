package com.mskyeye.dataDb.utils;


import com.mskyeye.dataDb.config.EsConfig;
import lombok.Data;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName:EsTools
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2022/12/21 10:12
 * @Version:1.0
 **/
@Component
@RefreshScope
@Data
public class EsTools {
    public static final String INDEX = "search";

    private RestClientBuilder restClientBuilder;

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private EsConfig esConfig;

//    @PostConstruct
    public void EsToolsInit(){
        restHighLevelClient = esConfig.getRestHighLevelClient();
    }


    /**
     * 通过id获取数据
     */
    public GetResponse get(String id) throws IOException {
        GetRequest request = new GetRequest(INDEX, id);
        return restHighLevelClient.get(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除document
     */
    public DeleteResponse delete(String index, String id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX);
        deleteRequest.id(id);
        return restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引，新版ES插入数据时自动创建
     */
    public CreateIndexResponse createIndex(String index) throws IOException{
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        return restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引
     */
    public AcknowledgedResponse deleteIndex(String index) throws IOException{
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
        return restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 插入json数据
     */
    public IndexResponse insertJson(String index,String content) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.source(content, XContentType.JSON);
        // indexRequest.id("4");
        return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 批量插入json数据
     */
    public BulkResponse insertBatchJson(String index,List<String> contentList) throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest;
        for(String item : contentList){
            indexRequest = new IndexRequest(index);
            indexRequest.source(item, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    public SearchResponse search(String index,QueryBuilder queryBuilder) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        request.source(searchSourceBuilder);

        return restHighLevelClient.search(request, RequestOptions.DEFAULT);

    }

    public QueryBuilder searchBuild(String query){
        /**
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *         {
         *           "multi_match":{
         *             "query":"担保",
         *             "fields": ["title", "descript"]
         *           }
         *         },
         *         {
         *           "term":{
         *             "search_type":"batch"
         *           }
         *         }
         *       ]
         *     }
         *   }
         * }
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("search_type", "task");
        boolQueryBuilder.filter(termQueryBuilder);

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(query, "title", "descript");
        boolQueryBuilder.must(multiMatchQueryBuilder);
        return boolQueryBuilder;
    }

}
