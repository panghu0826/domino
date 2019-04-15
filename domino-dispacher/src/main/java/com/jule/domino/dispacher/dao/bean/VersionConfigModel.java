package com.jule.domino.dispacher.dao.bean;

import lombok.Data;

@Data
public class VersionConfigModel {
    private Integer id;

    private String version;
    private String down_platform;
    private String downloadLink;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }
}