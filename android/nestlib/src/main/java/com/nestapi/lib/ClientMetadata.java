/**
 * Copyright 2014 Nest Labs Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software * distributed under
 * the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nestapi.lib;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

public final class ClientMetadata implements Parcelable {
    private final String mClientID;
    private final String mStateValue;
    private final String mClientSecret;
    private final String mRedirectURL;

    private ClientMetadata(Builder builder) {
        mClientID = builder.mClientID;
        mStateValue = builder.mStateValue;
        mClientSecret = builder.mClientSecret;
        mRedirectURL = builder.mRedirectURL;
    }

    private ClientMetadata(Parcel in) {
        mClientID = in.readString();
        mStateValue = in.readString();
        mClientSecret = in.readString();
        mRedirectURL = in.readString();
    }

    public String getClientID() {
        return mClientID;
    }

    public String getStateValue() {
        return mStateValue;
    }

    public String getClientSecret() {
        return mClientSecret;
    }

    public String getRedirectURL() {
        return mRedirectURL;
    }

    public static class Builder {
        private String mClientID;
        private String mRedirectURL;
        private String mStateValue;
        private String mClientSecret;

        public Builder setClientID(String clientID) {
            mClientID = clientID;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            mClientSecret = clientSecret;
            return this;
        }

        public Builder setRedirectURL(String redirectURL) {
            mRedirectURL = redirectURL;
            return this;
        }

        private Builder setStateValue(String state) {
            mStateValue = state;
            return this;
        }

        public ClientMetadata build() {
            //Create random state value on each creation
            setStateValue("app-state" + System.nanoTime() + "-" + new Random().nextInt());
            return new ClientMetadata(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mClientID);
        dest.writeString(mStateValue);
        dest.writeString(mClientSecret);
        dest.writeString(mRedirectURL);
    }

    public static final Parcelable.Creator<ClientMetadata> CREATOR = new Parcelable.Creator<ClientMetadata>() {
        @Override
        public ClientMetadata createFromParcel(Parcel source) {
            return new ClientMetadata(source);
        }

        @Override
        public ClientMetadata[] newArray(int size) {
            return new ClientMetadata[size];
        }
    };
}
