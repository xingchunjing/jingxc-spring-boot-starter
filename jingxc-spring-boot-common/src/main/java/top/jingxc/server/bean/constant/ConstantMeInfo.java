package top.jingxc.server.bean.constant;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("jingxc_boot_constant_info")
public class ConstantMeInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5513795291514998999L;

    @TableId
    private String id;

    private String gameId;

    private String channelId;

    private String platformId;

    private String returnUrl;

    private String cancelUrl;

    private String appId;

    private String appKey;

    private String appSecret;

    private String userId;

    private String createTime;
}

