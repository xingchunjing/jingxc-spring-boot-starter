package top.jingxc.server.param;

import lombok.Data;

@Data
public class RefundOrderParams {

    private String gameId;
    private String channelId;
    private String transcationId;
    private String orderId;
    private String userId;
    private String desc;
}
