package com.mmdkid.mmdkid.server;

/**
 * Created by LIYADONG on 2017/6/9.
 */

public abstract class Connection extends Object {
    public String URL;
    public abstract void Query(final Query query);
}
