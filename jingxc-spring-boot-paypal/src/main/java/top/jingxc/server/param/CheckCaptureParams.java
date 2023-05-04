package top.jingxc.server.param;

import lombok.Data;

@Data
public class CheckCaptureParams {

    private String gameId;
    private String channelId;
    private String paymentId;
}
