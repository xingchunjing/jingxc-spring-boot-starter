package top.jingxc.server.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BaseMapper {

    @Insert("insert into t_order set order_id = #{orderId}")
    void insertOrder(@Param("orderId") String orderId);
}
