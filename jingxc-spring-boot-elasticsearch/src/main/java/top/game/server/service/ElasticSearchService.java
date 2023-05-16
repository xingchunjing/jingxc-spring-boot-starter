package top.game.server.service;

import top.game.server.bean.ProductInfo;
import top.jingxc.server.config.ReturnResult;

import java.util.List;
import java.util.Optional;

public interface ElasticSearchService {

    Optional<ProductInfo> findById(String id);

    ReturnResult save();

    void delete(ProductInfo blog);

    Optional<ProductInfo> findOne(String id);

    List<ProductInfo> findAll();
}
