package top.jingxc.server.bean.product;

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
@TableName("jingxc_boot_product_info")
public class ProductMeInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4859760148941248647L;

    @TableId
    private String id;

    private String gameId;

    private String channelId;

    private String productId;

    private String productName;

    private String productDesc;

    private String productIcon;

    private String channelProductId;

    private String amount;

    private String currencyType;

    private String productStatus;
}
