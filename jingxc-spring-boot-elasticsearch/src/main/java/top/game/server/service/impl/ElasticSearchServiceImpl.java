package top.game.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.game.server.bean.ProductInfo;
import top.game.server.repository.ProductRepository;
import top.game.server.service.ElasticSearchService;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultSuccess;

import java.util.List;
import java.util.Optional;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Optional<ProductInfo> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public ReturnResult save() {
        ProductInfo productInfo = ProductInfo.builder().id("1").productName("测试商品001").build();
        ProductInfo save = productRepository.save(productInfo);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).data(save).msg("success").count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    public void delete(ProductInfo blog) {
        productRepository.delete(blog);
    }

    @Override
    public Optional<ProductInfo> findOne(String id) {
        return productRepository.findById(id);
    }

    @Override
    public List<ProductInfo> findAll() {
        return (List<ProductInfo>) productRepository.findAll();
    }

}
