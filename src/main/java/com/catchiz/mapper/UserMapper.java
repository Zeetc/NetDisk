package com.catchiz.mapper;

import com.catchiz.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Component("userMapper")
public interface UserMapper {
    @Select("select * from user")
    List<User> getAllUser();

    @Insert("insert into user values(#{id},#{username},#{password},#{email},#{registerDate},#{isManager})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void register(User user) throws SQLIntegrityConstraintViolationException;

    @Delete("delete from user where id = #{id}")
    void delUser(int userId);

    @Select("select * from user where id = #{id}")
    User getUserById(int userId);

    @Update("update user set password = #{password} where id = #{id}")
    void resetPassword(@Param("id") Integer id,@Param("password") String password);

    @Select("select count(*) from user where email = #{email}")
    Integer getEmailCount(@Param("email") String email);

    @Update("update user set email = #{email} where id = #{id}")
    int resetEmail(@Param("id") int id, @Param("email") String email);

    @Update("update user set username = #{username} where id = #{id}")
    int resetUsername(@Param("id")int id, @Param("username")String username);

    @Select("select email from user where id = #{id}")
    String getEmailById(int id);

    @Select("select count(*) from user where id = #{userId} and password = #{password}")
    int checkPassword(@Param("userId") int userId, @Param("password") String password);
}
