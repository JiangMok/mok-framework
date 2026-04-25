package com.mok.framework.runner;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.mok.framework.common.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * 数据源配置检查器
 * 应用启动时检查所有数据源配置是否正确
 *
 * @author mok
 * @date 2026/04/22
 */
@Component
public class DataSourceCheckerRunner implements ApplicationRunner {

    private static final Logger log = LogUtils.getLogger(DataSourceCheckerRunner.class);

    private final DataSource dataSource;

    public DataSourceCheckerRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1.判断数据源是否是多数据源,如果是多数据源,获取当前所有数据源
        if(dataSource instanceof DynamicRoutingDataSource dynamicRoutingDataSource){
            // 2.输出多数据基本信息
            Set<String> dataSourceSet = dynamicRoutingDataSource.getDataSources().keySet();
            log.info("========== 动态数据源配置检查 ==========");
            log.info("========== 已配置的数据源数量: {}", dataSourceSet.size());
            log.info("========== 数据源列表: {}", dataSourceSet);

            // 3.遍历所有数据源，检查数据源配置是否正确
            for(String dataSourceName : dataSourceSet){
                try{
                    // 3.1 获取单个具体数据源
                    DataSource dataSource =
                            dynamicRoutingDataSource.getDataSource(dataSourceName);
                    // 3.2 进行检查 --- 查看连接状态
                    Connection connection = dataSource.getConnection();
                    // 3.3 是否可用
                    boolean isValid = connection.isValid(5000);
                    // 3.4 关闭连接
                    connection.close();
                    // 3.5 输出结果
                    if(isValid){
                        log.info("========== 数据源 >>> {} 配置正确", dataSourceName);
                    }else{
                        log.error("========== 数据源 >>> {} 配置错误: 数据源连接无效", dataSourceName);
                    }
                } catch (SQLException sqlException) {
                    log.error("========== 数据源 >>> {} 配置错误: {}", dataSourceName, sqlException.getMessage());
                }
            }

        }else{
            log.info("========== 未配置多数据源 ==========");
        }
    }
}
