package top.jingxc.server.bean.user;

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
@TableName("jingxc_boot_user_info")
public class UserAccountInfo implements Serializable {

    @TableId
    private String id;
    private String gameId;
    private String channelId;
    private String userId;
    private String username;
    private String password;
    private String passsalt;
    private String accountGroupId;
    private String userStatus;
    private String token;
    private String createTime;

}
