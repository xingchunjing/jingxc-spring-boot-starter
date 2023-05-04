package top.jingxc.server.param;

import lombok.Data;

@Data
public class CreateTokenParams {

    private String gameId;
    private String channelId;
    private String userId;
    private String currencyType;
    private String email;
}
