package top.game.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.game.server.service.ElasticSearchService;
import top.jingxc.server.controller.BaseController;

@RestController
@RequestMapping
public class ElasticSearchController extends BaseController {

    @Autowired
    private ElasticSearchService elasticSearchServiceImpl;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @GetMapping("/save")
    public void Save() {
        elasticSearchServiceImpl.save();
    }

}
