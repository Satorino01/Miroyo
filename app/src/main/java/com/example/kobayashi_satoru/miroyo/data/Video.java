package com.example.kobayashi_satoru.miroyo.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

public final class Video {

    @NonNull
    private final String mId;

    @NonNull
    private final String mContentType;

    @Nullable
    private final String mName;

    @Nullable
    private final String mVideoURL;

    @Nullable
    private final String mThumbnailURL;

    @Nullable
    private final int mPlayTime;

    @Nullable
    private final int mByte;

    @Nullable
    private final int mStartTime;

    @Nullable
    private final int mEndTime;


    public Video(@NonNull String Id, @NonNull String contentType, @Nullable String videoName, @Nullable int videoPlayTime, @Nullable int videoByte, @Nullable String videoURL, @Nullable String thumbnailURL) {
        this.mId = Id;
        if(contentType.equals("VideoFile")){
            throw new IllegalArgumentException("VideoFileではありません");
        }
        this.mContentType = contentType;
        this.mName = videoName;
        this.mVideoURL = videoURL;
        this.mThumbnailURL = thumbnailURL;
        this.mPlayTime = videoPlayTime;
        this.mByte = videoByte;
        this.mStartTime = 0;
        this.mEndTime = 0;
    }

    public Video(@NonNull String Id, @NonNull String contentType, @Nullable String videoName, @Nullable String videoURL, @Nullable String thumbnailURL, @Nullable int mStartTime, @Nullable int mEndTime) {
        this.mId = Id;
        if(!contentType.equals("YoutubeURL")){
            throw new IllegalArgumentException("YoutubeURLではありません");
        }
        this.mContentType = contentType;
        this.mName = videoName;
        this.mVideoURL = videoURL;
        this.mThumbnailURL = thumbnailURL;
        this.mPlayTime = 0;
        this.mByte = 0;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
    }

    public Video(@NonNull String Id, @NonNull String contentType, @Nullable String videoName, @Nullable String videoURL, @Nullable String thumbnailURL) {
        this.mId = Id;
        if(!contentType.equals("URL")){
            throw new IllegalArgumentException("URLではありません");
        }
        this.mContentType = contentType;
        this.mName = videoName;
        this.mVideoURL = videoURL;
        this.mThumbnailURL = thumbnailURL;
        this.mPlayTime = 0;
        this.mByte = 0;
        this.mStartTime = 0;
        this.mEndTime = 0;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getContentType() {
        return mContentType;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public int getPlayTime() {
        return mPlayTime;
    }

    @Nullable
    public int getByte() {
        return mByte;
    }

    @Nullable
    public int getmStartTime() {
        return mStartTime;
    }

    @Nullable
    public int getmEndTime() {
        return mEndTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.example.kobayashi_satoru.miroyo.data.Video video = (com.example.kobayashi_satoru.miroyo.data.Video) o;
        return Objects.equal(mId, video.mId) &&
                Objects.equal(mContentType, video.mContentType)&&
                Objects.equal(mName, video.mName)&&
                Objects.equal(mPlayTime, video.mPlayTime)&&
                Objects.equal(mByte, video.mByte)&&
                Objects.equal(mVideoURL, video.mVideoURL)&&
                Objects.equal(mThumbnailURL, video.mThumbnailURL);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mContentType, mName, mPlayTime, mByte, mVideoURL, mThumbnailURL);
    }

    @Override
    public String toString() {
        return "{" + mId + "," + mContentType + "," + mName + "," + String.valueOf(mPlayTime) + "," + String.valueOf(mByte) + "," + mVideoURL + "," + mThumbnailURL + "}";
    }
}
