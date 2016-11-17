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

package com.smoope.utils.traverson;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Base resource
 *
 * @since 1.0.0
 */
@AllArgsConstructor
public class TraversonResult<T> {

    @SerializedName("_links")
    @Getter
    @Setter
    private Map<String, TraversonLink> links;

    @SerializedName("_embedded")
    protected Map<String, T> embedded;

    public TraversonResult() {
        this.links = new HashMap<String, TraversonLink>(0);
    }

    public TraversonLink getLinkForRel(final String rel) {
        return links.containsKey(rel) ? links.get(rel) : null;
    }

    public TraversonLink getLinkForSelf() {
        return this.getLinkForRel("self");
    }

    public Map<String, T> getEmbedded() {
        return embedded == null ? new HashMap<String, T>(0) : embedded;
    }
}
