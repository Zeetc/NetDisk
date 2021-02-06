package com.catchiz.mapper;

import com.catchiz.domain.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserMapper {
    @Select("select * from user")
    List<User> getAllUser();

    @Insert("insert into user values(#{id},#{username},#{password},#{email},#{registerDate},#{isManager})")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    void register(User user) throws SQLIntegrityConstraintViolationException;

    @Select("select * from user where username = #{username} and password = #{password}")
    User login(User user);

    @Delete("delete from user where id = #{id}")
    void delUser(int userId);

    @Select("select * from user where id = #{id} and password =#{password} and isManager = 1")
    User managerLogin(User user);

    @Select("select * from user where id = #{id}")
    User getUserById(int userId);
}
