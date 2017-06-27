package com.apkupdater.model.GitHub;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Release
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("html_url")
    @Expose
    private String htmlUrl;

    @SerializedName("assets_url")
    @Expose
    private String assetsUrl;

    @SerializedName("upload_url")
    @Expose
    private String uploadUrl;

    @SerializedName("tarball_url")
    @Expose
    private String tarballUrl;

    @SerializedName("zipball_url")
    @Expose
    private String zipballUrl;

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("tag_name")
    @Expose
    private String tagName;

    @SerializedName("target_commitish")
    @Expose
    private String targetCommitish;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("draft")
    @Expose
    private Boolean draft;

    @SerializedName("prerelease")
    @Expose
    private Boolean prerelease;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("published_at")
    @Expose
    private String publishedAt;

    @SerializedName("author")
    @Expose
    private Author author;

    @SerializedName("assets")
    @Expose
    private List<Asset> assets = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getAssetsUrl() {
        return assetsUrl;
    }

    public void setAssetsUrl(String assetsUrl) {
        this.assetsUrl = assetsUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getTarballUrl() {
        return tarballUrl;
    }

    public void setTarballUrl(String tarballUrl) {
        this.tarballUrl = tarballUrl;
    }

    public String getZipballUrl() {
        return zipballUrl;
    }

    public void setZipballUrl(String zipballUrl) {
        this.zipballUrl = zipballUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTargetCommitish() {
        return targetCommitish;
    }

    public void setTargetCommitish(String targetCommitish) {
        this.targetCommitish = targetCommitish;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public Boolean getPrerelease() {
        return prerelease;
    }

    public void setPrerelease(Boolean prerelease) {
        this.prerelease = prerelease;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
