package top.jingxc.server.param;

import lombok.Data;

@Data
public class RefundCaptureParams {

    private String gameId;
    private String userId;
    private String orderId;
    private String channelId;
    private String noteToPayer;
}
