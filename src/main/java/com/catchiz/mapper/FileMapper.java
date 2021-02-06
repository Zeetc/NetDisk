package com.catchiz.mapper;

import com.catchiz.domain.MyFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface FileMapper {
    @Select("select * from file where uid = #{userId} and file.isValidFile=1 and pid =#{pid}")
    List<MyFile> listAllMyFile(@Param("userId") int userId,@Param("pid") int pid);

    @Insert("insert into file values(#{fileId},#{filename},#{filePath},#{fileSize},\n" +
            "                        #{isValidFile},#{uploadDate},#{contentType},#{uid},#{pid})")
    void storeFile(MyFile file);

    @Delete("delete from file where uid = #{uid}")
    void delFileByUser(int userId);

    @Delete("delete from file where fileId = #{fileId}")
    void delFile(int fileId);

    @Update("update file set isValidFile = #{isValidFile} where fileId = #{fileId}")
    void changeFileValid(@Param("fileId") int fileId,@Param("isValidFile") int isValidFile);

    @Select("select * from file where uid = #{userId} and pid = ${pid}")
    List<MyFile> listAllMyFileIgnoreValid(@Param("userId") int userId,@Param("pid") int pid);

    @Select("select * from file where fileId = #{fileId}")
    MyFile getFileById(int fileId);

    @Select("select pid from file where fileId = #{fileId}")
    int getCurPid(int pid);

    @Select("select filePath from file where fileId = #{fileId}")
    String getPathById(int id);

    @Delete("delete from file where filePath = #{filePath}")
    void delFileByPath(String path);

    @Select({
            "<script>" ,
            "select * from file where pid = #{pid} and uid = #{userId}\n" +
                    "        <if test=\"fileName!=null and '%null%'!=fileName\">\n" +
                    "            and fileName like #{fileName}\n" +
                    "        </if>\n" +
                    "        <if test=\"!isIgnoreValid\">\n" +
                    "            and isValidFile = 1\n" +
                    "        </if>\n" +
                    "        limit #{start} , #{pageSize}",
            "</script>"
    })
    List<MyFile> findByInfo(@Param("pid") int pid, @Param("userId") int userId,
                            @Param("start") int start, @Param("pageSize") int pageSize,
                            @Param("fileName") String fileName, @Param("isIgnoreValid") boolean isIgnoreValid);

    @Select({
            "<script>" ,
            "select count(*) from file where pid = #{pid} and uid = #{userId}\n" +
                    "        <if test=\"fileName!=null and '%null%'!=fileName\">\n" +
                    "            and fileName like #{fileName}\n" +
                    "        </if>\n" +
                    "        <if test=\"!isIgnoreValid\">\n" +
                    "            and isValidFile = 1\n" +
                    "        </if>",
            "</script>"
    })
    int findCountByInfo(@Param("pid") int pid,@Param("userId") int userId,
                        @Param("fileName") String fileName,@Param("isIgnoreValid") boolean isIgnoreValid);

}
