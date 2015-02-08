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
package com.nestapi.lib.API;

import android.os.Parcel;

final class ParcelUtils {
    private ParcelUtils() {
        throw new AssertionError("Utility class; do not instantiate.");
    }

    static void writeBoolean(Parcel out, boolean value) {
        out.writeInt(value ? 1 : 0);
    }

    static boolean readBoolean(Parcel in) {
        return in.readInt() != 0;
    }
}
