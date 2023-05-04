package top.jingxc.server.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CheckParamUtils {

    /**
     * 验证参数准确性
     *
     * @param sign
     * @param sign
     * @param openKey
     */
    public static boolean checkSign(Map<String, String> signMap, String sign, String openKey) {
        StringBuffer sb = new StringBuffer();
        signMap.remove("sign");
        for (Map.Entry<String, String> entry : signMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            sb.append(mapKey + "=" + mapValue + "&");
        }
        sb.append(openKey);
        String md5Sign = MD5Util.getMD5(sb.toString());

        return StringUtils.isNotBlank(sign) && sign.equals(md5Sign);
    }

}
