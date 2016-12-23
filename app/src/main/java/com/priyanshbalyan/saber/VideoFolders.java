package com.priyanshbalyan.saber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class VideoFolders {
    String foldername ;
    long foldersize ;
    List<String> vnif;
    List<String> vpif;
    List<String> vrif ;
    List<Long> vidif ;

    public VideoFolders(){
        foldersize = 0 ;
        vnif = new ArrayList<>();
        vpif = new ArrayList<>();
        vrif = new ArrayList<>();
        vidif = new ArrayList<>();
    }

    public static String filesize(long fs){
        if(fs < 1024)
            return fs+" B";
        else if(fs < 1048576)
            return fs/1024 + " KB";
        else if(fs < 1073741824)
            return fs/1024/1024 + " MB" ;
        else
            return String.format("%.02f GB",(float)(fs/1024.0/1024.0/1024.0)) ;

    }
}
