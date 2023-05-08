package top.jingxc.server.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.bean.OrderInfo;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.mapper.OrderInfoMapper;
import top.jingxc.server.service.ShardingSphereService;

@Service
public class ShardingSphereServiceImpl implements ShardingSphereService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;


    @Override
    public ReturnResult test() {

        for (int i = 100201; i <= 100211; i++) {
            OrderInfo build = OrderInfo.builder().orderId(i).id(IdWorker.getIdStr()).build();

            orderInfoMapper.insert(build);
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").build();
    }
}
