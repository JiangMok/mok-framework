package com.mok.framework.operationLog.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.mok.framework.common.PageParam;
import com.mok.framework.common.PageResult;
import com.mok.framework.model.entity.OperationLogEntity;
import com.mok.framework.operationLog.repository.OperationLogRepository;
import com.mok.framework.operationLog.service.OperationLogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ElasticSearch 操作日志service实现类 ES保存
 *
 * @author: mok
 * @date: 2026/3/27
 */
@Service
@ConditionalOnProperty(name = "operationlogImpl.save-location.type", havingValue = "es")
public class OperationLogESServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public OperationLogESServiceImpl(OperationLogRepository operationLogRepository,
                                     ElasticsearchOperations elasticsearchOperations) {
        this.operationLogRepository = operationLogRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public PageResult<OperationLogEntity> getPageList(PageParam param) {
        //1.使用BoolQuery构建条件容器
        // 创建BoolQuery的构建器,用于组合多个查询条件(must、should、filter 等)
        // BoolQuery 是ElasticSearch 的布尔查询,用于将多个子查询组合成复杂条件查询
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        //2.1添加固定过滤条件(status,businessType)
        Map<String, Object> params = param.getParams();
        if (params != null) {
            // 处理 status（整数精确匹配）
            Object statusObj = params.get("status");
            if (statusObj != null && StringUtils.hasText(statusObj.toString())) {
                // status 是整数，使用 term 查询
                int status = Integer.parseInt(statusObj.toString());
                boolBuilder.filter(q -> q.term(t -> t.field("status").value(status)));
            }
            // 处理 businessType（字符串精确匹配）
            Object businessTypeObj = params.get("businessType");
            if (businessTypeObj != null && StringUtils.hasText(businessTypeObj.toString())) {
                String businessType = businessTypeObj.toString();
                boolBuilder.filter(q -> q.term(t -> t.field("businessType").value(businessType)));
            }
        }

        //2.2关键词模糊查询
        if (StringUtils.hasText(param.getKeyword())) {
            //提取关键词
            String keyword = param.getKeyword();

            //对 title 和 operatorName 进行 multi_match(分词匹配) 或 wildcard(通配符)
            // 这里使用 wildcard 对 keyword 子字段进行模糊匹配（推荐，不依赖分词器）
            // 创建针对 title.keyword 字段的通配符查询对象,匹配任意位置包含关键词的文档
            // Query 是ElasticSearch 查询的顶层抽象,此处使用lambda构建 WildcardQuery
            Query titleQuery = Query.of(q -> q.wildcard(
                    w -> w.field("title").value("*" + keyword + "*")));
            // 创建针对 operatorName.keyword 字段的通配符查询对象，同样匹配任意位置包含关键词
            Query operatorQuery = Query.of(q -> q.match(
                    m -> m.field("operatorName").query(keyword)));

            // 将两个通配符查询组合成一个should 子句(满足任意一个即可)
            // must() 方法向 boolBuilder 添加一个must 条件,必须满足该条件
            // 这里内部创建了一个新的 bool 查询,将两个 wildcard 查询放入 should中,并设置至少匹配1个
            boolBuilder.must(q -> q.bool(
                    builder ->
                            builder.should(titleQuery)//添加 should 子句 : title 匹配
                                    .should(operatorQuery)//添加 should 子句 : operatorName  匹配
                                    .minimumShouldMatch("1")));//设置至少满足1个 should 子句
        }

        //3.构建NativeSearchQuery,加入分页和排序
        param.setOrderBy("operTime");
        param.setOrder("desc");
        Pageable pageable = param.toPageable();

        // 使用 NativeQuery 的构建器构建最终的 ElasticSearch 查询对象
        // NativeQuery 是 Spring Data ElasticSearch 提供的封装,可以设置查询体、分页、排序等
        NativeQuery searchQuery = NativeQuery.builder()
                // 建构建好的 BoolQuery 转换为 Query 对象并设置为主查询
                .withQuery(boolBuilder.build()._toQuery())
                //设置分页信息和排序信息
                .withPageable(pageable)
                .build();

        //4.执行搜索
        // 通过 elasticsearchOperations (Spring Data ElasticSearch 的核心操作模板)执行查询
        // 传入 searchQuery 和实体类类型,返回 SearchHits 对象,其中包含匹配的文档列表和总命中数
        SearchHits<OperationLogEntity> searchHits =
                elasticsearchOperations.search(searchQuery, OperationLogEntity.class);

        //5.转换为pageResult
        // 从 searchHits 中获取所有命中的文档(SearchHit列表),并将每个SearchHit 转换为实体对象
        // 使用流处理,提取每个hit 中的content(即 OperationLogEntity 对象)
        List<OperationLogEntity> operationLogEntityList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        //获取总记录数
        Long total = searchHits.getTotalHits();

        return PageResult.success(operationLogEntityList, total, param.getPageNum(), param.getPageSize());
    }

    @Override
    public void saveOperationLog(OperationLogEntity operationLogEntity) {
        operationLogRepository.save(operationLogEntity);
    }

    @Override
    public OperationLogEntity findById(String id) {
        return operationLogRepository.findById(id).orElse(null);
    }

    @Override
    public int cleanLogsBefore(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }

        String formattedDateTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String jsonQuery = "{\"range\":{\"operTime\":{\"lt\":\"" + formattedDateTime + "\"}}}";

        // 构建查询条件
        StringQuery stringQuery = new StringQuery(jsonQuery);

        // 关键：使用 DeleteQuery 包装，而不是直接传 StringQuery
        DeleteQuery deleteQuery = DeleteQuery.builder(stringQuery)
                // 立即刷新
                .withRefresh(true)
                .build();

        // 执行删除，返回 ByQueryResponse
        ByQueryResponse response = elasticsearchOperations.delete(deleteQuery, OperationLogEntity.class);

        return (int) response.getDeleted();
    }

    @Override
    public void deleteById(String id) {
        operationLogRepository.deleteById(id);
    }

    @Override
    public boolean checkOperationLogExistsById(String id) {
        OperationLogEntity operationLogEntity = operationLogRepository.findById(id).orElse(null);
        return operationLogEntity != null;
    }
}
