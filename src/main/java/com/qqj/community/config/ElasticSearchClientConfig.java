package com.qqj.community.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private Integer port;

    @Value("${elasticsearch.scheme}")
    private String scheme;

    @Value("${elasticsearch.connect-timeout}")
    private Integer connectTimeout;

    @Value("${elasticsearch.socket-timeout}")
    private Integer socketTimeout;

    @Bean
    @Qualifier("highLevelClient")
    public RestHighLevelClient restHighLevelClient() {
        // 该方法接收一个RequestConfig.Builder对象，对该对象进行修改后然后返回。
        RestHighLevelClient highLevelClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
                        .setRequestConfigCallback(requestConfigBuilder -> {
                            return requestConfigBuilder.setConnectTimeout(connectTimeout) // 连接超时（默认为1秒）
                                    .setSocketTimeout(socketTimeout);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
                        }));// 调整最大重试超时时间（默认为30秒）.setMaxRetryTimeoutMillis(60000);

        return highLevelClient;
    }
}
