package top.jingxc.server.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;

public class OauthSignatureUtil {

    /**
     * 构造Basic Auth认证头信息
     *
     * @param key
     * @param value
     * @return
     */
    public static String headerBasic(String key, String value) {
        String auth = key + ":" + value;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        return authHeader;
    }

}
