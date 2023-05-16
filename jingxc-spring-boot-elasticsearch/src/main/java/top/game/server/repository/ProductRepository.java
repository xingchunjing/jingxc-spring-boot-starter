package top.game.server.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import top.game.server.bean.ProductInfo;

@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductInfo, String> {

}
