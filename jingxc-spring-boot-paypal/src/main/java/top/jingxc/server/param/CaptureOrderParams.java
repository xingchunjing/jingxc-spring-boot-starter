package top.jingxc.server.param;

import lombok.Data;

@Data
public class CaptureOrderParams {

    private String gameId;
    private String channelId;
    private String platformOrderId;
}
