package com.catchiz.mapper;

import com.catchiz.pojo.MyFile;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("fileMapper")
public interface FileMapper {
    @Insert("insert into file values(#{fileId},#{filename},#{filePath},#{fileSize},\n" +
            "                        #{isValidFile},#{uploadDate},#{contentType},#{uid},#{pid},#{isChecked})")
    @Options(useGeneratedKeys = true,keyColumn = "fileId",keyProperty = "fileId")
    void storeFile(MyFile file);

    @Select("select * from file where filePath = #{filePath}")
    MyFile getFileByPath(@Param("filePath") String filePath);

    @Delete("delete from file where uid = #{uid}")
    void delFileByUser(int userId);

    @Delete("delete from file where fileId = #{fileId}")
    void delFile(int fileId);

    @Update("update file set isValidFile = #{isValidFile} where fileId = #{fileId}")
    void changeFileValid(@Param("fileId") int fileId,@Param("isValidFile") boolean isValidFile);

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
            "select fileId,filename,fileSize,uploadDate,contentType,uid,pid from file where pid = #{pid} and uid = #{userId}\n" +
                    "        <if test=\"fileName!=null and '%null%'!=fileName\">\n" +
                    "            and fileName like #{fileName}\n" +
                    "        </if>\n" +
                    "        <if test=\"!isIgnoreValid\">\n" +
                    "            and isValidFile = 1\n" +
                    "        </if>\n" +
                    "        <if test=\"pageCut\"> limit #{start} , #{pageSize} </if>",
            "</script>"
    })
    @Results({
            @Result(id = true, column = "fileId", property = "fileId"),
            @Result(column = "filename", property = "filename"),
            @Result(column = "fileSize", property = "fileSize"),
            @Result(column = "uploadDate", property = "uploadDate"),
            @Result(column = "contentType", property = "contentType"),
            @Result(column = "uid", property = "uid"),
            @Result(column = "pid", property = "pid")
    })
    List<MyFile> findByInfo(@Param("pid") int pid,
                            @Param("userId") int userId,
                            @Param("start") int start,
                            @Param("pageSize") int pageSize,
                            @Param("fileName") String fileName,
                            @Param("isIgnoreValid") boolean isIgnoreValid,
                            @Param("pageCut")boolean pageCut);

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

    @Select("select fileId from file where pid = #{pid}")
    List<Integer> getChildFiles(int pid);

    @Select("select fileName from file where fileId = #{fileId}")
    String getFilenameById(int fileId);

    @Update("update file set isChecked = #{isChecked} where fileId = #{fileId}")
    void changeCheck(@Param("fileId") int fileId,@Param("isChecked") boolean isChecked);

    @Select("select * from file where isChecked = #{isChecked}")
    List<MyFile> getAllFileByCheck(boolean isChecked);

    @Update("update file set filename = #{newName} where fileId = #{fileId}")
    void renameFile(@Param("fileId") int fileId,
                    @Param("newName") String newName);

    @Update("update file set filePath = #{filePath} where fileId = #{fileId}")
    void renameFilePath(@Param("fileId")int fileId,@Param("filePath")String filePath);
}
