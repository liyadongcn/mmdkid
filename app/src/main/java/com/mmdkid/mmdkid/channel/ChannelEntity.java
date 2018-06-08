package com.mmdkid.mmdkid.channel;

/**
 * 频道实体类
 * Created by YoKeyword on 15/12/29.
 */
public class ChannelEntity {

    private long id;
    private String name;
    private String keyWords;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }
}
