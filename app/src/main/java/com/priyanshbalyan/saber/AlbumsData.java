package com.priyanshbalyan.saber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public class AlbumsData {
    String albumname ;
    String artistname ;
    String albumart ;
    String year ;

    List<String> mnia ;
    List<String> mpia ;
    List<String> maia ;
    List<String> mdia ;

    public AlbumsData(){
        mnia = new ArrayList<>();
        mpia = new ArrayList<>();
        maia = new ArrayList<>();
        mdia = new ArrayList<>();
    }
}
