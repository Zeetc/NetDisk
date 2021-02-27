package com.catchiz.mapper;

import com.catchiz.domain.User;
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

    @Select("select * from user where username = #{username} and password = #{password}")
    User login(User user);

    @Delete("delete from user where id = #{id}")
    void delUser(int userId);

    @Select("select * from user where id = #{id} and password =#{password} and isManager = 1")
    User managerLogin(User user);

    @Select("select * from user where id = #{id}")
    User getUserById(int userId);

    @Update("update user set password = '#{password}' where id = #{id}")
    void resetPassword(@Param("id") Integer id,@Param("password") String password);

    @Select("select count(*) from user where email = #{email}")
    Integer getEmailCount(@Param("email") String email);
}
