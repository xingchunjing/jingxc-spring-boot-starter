package top.jingxc.server.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnResultSuccess implements ReturnResult {

    private int code;
    private String msg;
    private Object data;
    private Integer count;
}
