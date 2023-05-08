package top.jingxc.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.bean.TOrder;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.mapper.BaseMapper;
import top.jingxc.server.mapper.TOrderInfoMapper;
import top.jingxc.server.service.ShardingSphereService;

@Service
public class ShardingSphereServiceImpl implements ShardingSphereService {

    @Autowired
    private TOrderInfoMapper orderInfoMapper;

    @Autowired
    BaseMapper baseMapper;

    @Override
    public ReturnResult test() {

        for (int i = 100201; i <= 100211; i++) {
            //baseMapper.insertOrder(i + "");
            TOrder build = TOrder.builder().orderId(i + "").build();

            orderInfoMapper.insert(build);
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").build();
    }
}
