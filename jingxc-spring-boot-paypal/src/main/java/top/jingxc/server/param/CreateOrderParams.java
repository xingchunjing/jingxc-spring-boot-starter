package top.jingxc.server.param;

import lombok.Data;

@Data
public class CreateOrderParams {

    private String gameId;
    private String channelId;
    private String productId;
    private String currencyType;
    private String userId;
    private String sign;
}
