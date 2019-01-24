package com.example.kobayashi_satoru.miroyo.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public final class User {

    @NonNull
    private final String mId;

    @Nullable
    private final String mName;

    public User(@NonNull String id, @Nullable String userName) {
        mId = id;
        mName = userName;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(mName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equal(mId, user.mId) &&
                Objects.equal(mName, user.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mName);
    }

    @Override
    public String toString() {
        return "User with name " + mName;
    }
}