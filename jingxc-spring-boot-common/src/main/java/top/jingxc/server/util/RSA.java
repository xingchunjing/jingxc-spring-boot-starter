package top.jingxc.server.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jingxc
 * @date 2021/05/10
 */
public class RSA {
    /**
     * SIGN_ALGORITHMS
     */
    public static final String SIGN_ALGORITHMSMD5 = "MD5WithRSA";
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    public static final String SIGN_ALGORITHMS256 = "SHA256WithRSA";
    public static final String RSA256 = "RSA256";
    public static final String RSAMD5 = "RSAMD5";

    /**
     * 解密
     *
     * @param content 密文
     * @param key     商户私钥
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String key) throws Exception {
        PrivateKey prikey = getPrivateKey(key);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);

        InputStream ins = new ByteArrayInputStream(LocalBase64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        // rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;

            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }
            writer.write(cipher.doFinal(block));
        }
        return new String(writer.toByteArray(), "utf-8");
    }

    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = LocalBase64.decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * RSA签名
     *
     * @param content    待签名数据
     * @param privateKey 商户私钥
     * @param signtype   签名类型
     * @return 签名值
     */
    public static String sign(String content, String privateKey, String signtype) {
        String charset = "utf-8";
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(LocalBase64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = null;
            if (RSA256.equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS256);
            } else if (RSAMD5.equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMSMD5);
            } else {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
            }

            signature.initSign(priKey);
            signature.update(content.getBytes(charset));

            byte[] signed = signature.sign();

            return LocalBase64.encode(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * RSA验签名检查
     *
     * @param params    待签名数据(Map)
     * @param sign      签名值
     * @param publicKey 公钥
     * @param signtype  签名类型
     * @return 布尔值
     */
    public static boolean rsaDoCheck(Map<String, Object> params, String sign, String publicKey, String signtype) {
        String content = RSA.getSignData(params);
        System.out.println("The content for sign is : " + content.toString());
        System.out.println("The sign is : " + sign);
        System.out.println("The pubkey is : " + publicKey);

        return RSA.doCheck(content, sign, publicKey, signtype, false);
    }

    /**
     * RSA验签名检查
     *
     * @param content   待签名数据
     * @param sign      签名值
     * @param publicKey 公钥
     * @param signtype  签名类型
     * @return 布尔值
     */
    public static boolean doCheck(String content, String sign, String publicKey, String signtype, boolean flag) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = LocalBase64.decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = null;
            if (RSA256.equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS256);
            } else if (RSAMD5.equals(signtype)) {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMSMD5);
            } else {
                signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
            }

            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));

            boolean bverify = false;
            if (flag) {
                bverify = signature.verify(Base64.decodeBase64(sign));
            } else {
                bverify = signature.verify(LocalBase64.decode(sign));
            }

            return bverify;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getSignData(Map<String, Object> params) {
        StringBuffer content = new StringBuffer();

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if ("sign".equals(key)) {
                continue;
            }
            if ("signType".equals(key)) {
                continue;
            }
            if ("signtype".equals(key)) {
                continue;
            }
            if ("sign_type".equals(key)) {
                continue;
            }
            if ("rtnSign".equals(key)) {
                continue;
            }
            String value = String.valueOf(params.get(key));
            if (value != null) {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            } else {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }
        return content.toString();
    }

    public static String getSignData2(Map<String, String> params) {
        StringBuffer content = new StringBuffer();

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if ("sign".equals(key)) {
                continue;
            }
            if ("sign_type".equals(key)) {
                continue;
            }
            if ("signature".equals(key)) {
                continue;
            }
            if ("rtnSign".equals(key)) {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null) {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            }
        }
        return content.toString();
    }

    public static String getNoSortSignData(Map<String, Object> params) {
        StringBuffer content = new StringBuffer();

        List<String> keys = new ArrayList<String>(params.keySet());

        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if ("sign".equals(key)) {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null) {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            } else {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }
        return content.toString();
    }

    public static String checkSign(Map<String, String> signMap, String code) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : signMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            sb.append(mapKey + "=" + mapValue + "&");
        }
        sb.append(code);
        return MD5Util.getMD5(sb.toString());
    }
}
