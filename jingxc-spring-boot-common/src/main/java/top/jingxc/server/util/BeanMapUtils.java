package top.jingxc.server.util;

import top.jingxc.server.exception.ReturnCode200Exception;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

public class BeanMapUtils {

    /**
     * 实体类转map
     *
     * @param object
     * @return
     */
    public static Map<String, String> entityToMapString(Object object) {
        Map<String, String> map = new TreeMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                String o = String.valueOf(field.get(object));
                map.put(field.getName(), o);
                field.setAccessible(flag);
            } catch (Exception e) {
                throw new ReturnCode200Exception("实体类转Map发生错误");
            }
        }

        return map;
    }

    /**
     * 实体类转map
     *
     * @param object
     * @return
     */
    public static Map<String, Object> entityToMapObject(Object object) {
        Map<String, Object> map = new TreeMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                boolean flag = field.isAccessible();
                field.setAccessible(true);
                Object o = field.get(object);
                map.put(field.getName(), o);
                field.setAccessible(flag);
            } catch (Exception e) {
                throw new ReturnCode200Exception("实体类转Map发生错误");
            }
        }

        return map;
    }

    /**
     * map转实体类
     *
     * @param <T>
     * @param map
     * @param entity
     * @return
     */
    public static <T> T mapToEntity(Map<Object, Object> map, Class<T> entity) {
        T t = null;
        try {
            t = entity.newInstance();
            for (Field field : entity.getDeclaredFields()) {
                if (map.containsKey(field.getName())) {
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    Object object = map.get(field.getName());
                    if (object != null && field.getType().isAssignableFrom(object.getClass())) {
                        field.set(t, object);
                    }
                    field.setAccessible(flag);
                }
            }
            return t;
        } catch (Exception e) {
            throw new ReturnCode200Exception("Map转实体类发生错误");
        }
    }

}
