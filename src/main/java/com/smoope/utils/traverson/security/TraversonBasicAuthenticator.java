/*
 * Copyright 2016 smoope GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smoope.utils.traverson.security;

import lombok.RequiredArgsConstructor;

import okhttp3.Credentials;

/**
 * Basic HTTP authentication implementation
 *
 * @since 1.2.0
 */
@RequiredArgsConstructor
public class TraversonBasicAuthenticator implements TraversonAuthenticator {

    private final String username;

    private final String password;

    /**
     * Retrieves the credentials
     *
     * @return Credentials
     * @since 1.2.0
     */
    public String getCredentials() {
        return Credentials.basic(username, password);
    }
}
