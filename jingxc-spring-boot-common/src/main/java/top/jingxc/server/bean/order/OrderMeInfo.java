package top.jingxc.server.bean.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("jingxc_boot_order_info")
public class OrderMeInfo {

    @TableId
    private String id;
    private String gameId;
    private String channelId;
    private String platformId;
    private String productId;
    private String userId;
    private String serverId;
    private String roleId;
    private String amount;
    private String currencyType;
    private String requestId;
    private String orderId;
    private String platformOrderId;
    private String paymentId;
    private String refundId;
    private int orderStatus;
}
