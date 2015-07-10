/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.plaid.data.prefs;

/**
 * Created by nickbutcher on 3/29/15.
 */
public class ProductHuntPrefs {

    public static final String PLAID_CLIENT_ID =
            "678ab0081c4b2c56a0e746bda4ca02c316689b4070614dd76381f6696db841e1";
    public static final String PLAID_CLIENT_SECRET =
            "a1dd74b09ca3d7e55c64205459d9ac9c34bbd7afb3fea38a3b504d8dcd34dbe3";
    public static final String LOGIN_CALLBACK = "product-hunt-auth-callback";
    public static final String LOGIN_URL = "https://dribbble.com/oauth/authorize?client_id=" +
            PLAID_CLIENT_ID
            + "&redirect_uri=plaid%3A%2F%2F" + LOGIN_CALLBACK +
            "&scope=public+write+comment+upload";
    public static final String DEVELOPER_TOKEN =
            "2203f31b30da5bd6c84c46e3cd571af62087aa48c829b8feba3814aa47205f63";

}
