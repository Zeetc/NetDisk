package com.catchiz.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class FileTree {
    private int fileId;
    private Map<String,FileTree> childFiles=new HashMap<>();

    public FileTree(int fileId) {
        this.fileId = fileId;
    }

    public FileTree search(String[] dir,FileTree fileTree){
        for (String s : dir) {
            if(fileTree.getChildFiles().get(s)==null)return null;
            fileTree=fileTree.getChildFiles().get(s);
        }
        return fileTree;
    }
}
