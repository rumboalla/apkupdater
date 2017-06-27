package com.apkupdater.model.GitHub;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Asset
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("browser_download_url")
    @Expose
    private String browserDownloadUrl;

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("label")
    @Expose
    private String label;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("content_type")
    @Expose
    private String contentType;

    @SerializedName("size")
    @Expose
    private Integer size;

    @SerializedName("download_count")
    @Expose
    private Integer downloadCount;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    @SerializedName("uploader")
    @Expose
    private Uploader uploader;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }

    public void setBrowserDownloadUrl(String browserDownloadUrl) {
        this.browserDownloadUrl = browserDownloadUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Uploader getUploader() {
        return uploader;
    }

    public void setUploader(Uploader uploader) {
        this.uploader = uploader;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////