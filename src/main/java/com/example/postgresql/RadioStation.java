package com.example.postgresql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RadioStation {
    private int id;
    private String name;
    @JsonProperty("stream_url") private String streamUrl;
    @JsonProperty("cover_url") private String coverUrl;

    public RadioStation() {
    }

    public RadioStation(int id, String name, String streamUrl, String coverUrl) {
        this.id = id;
        this.name = name;
        this.streamUrl = streamUrl;
        this.coverUrl = coverUrl;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}