package top.game.server.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Document:
 * indexName = "product":索引名称,类似于mysql中的数据库;
 * type = "doc":文档类型,类似于mysql中的表;
 * shards = 5:分片数量
 * replicas = 1:副本
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product", shards = 5, replicas = 1)
public class ProductInfo {

    @Id
    private String id;

    /**
     * 中文分词设置，前提是您的es已经安装了中文分词ik插件
     * 中文分词有两种形式：
     * ik_max_word：会将文本做最细粒度的拆分
     * ik_smart：会将文本做最粗粒度的拆分
     * type 类型
     * analyzer 存储解析器
     * searchAnalyzer 查询解析器
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String productName;
}
