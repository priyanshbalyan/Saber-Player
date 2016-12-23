package com.priyanshbalyan.saber;

/**
 * Created by Priyansh on 21-Oct-16.
 */

public interface ServiceCallbacks {
    void updateSeekbar(int pos, boolean playing);
    void playerReady(int seekmax, String trackname, String artistname);
    void finishActivity();
}
