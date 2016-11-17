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

package com.smoope.utils.traverson.utils;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.VariableExpansionException;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines hypermedia API's templated link
 *
 * @since 1.0.0
 */
public class UriTemplate {

    private String uri;

    private com.damnhandy.uri.template.UriTemplate template;

    private UriTemplate(final String uri) {
        this.uri = uri;
        try {
            this.template = com.damnhandy.uri.template.UriTemplate.buildFromTemplate(uri).build();
        } catch (MalformedUriTemplateException e) {}
    }

    public static UriTemplate fromUri(final String uri) {
        if (uri.isEmpty())
            return null;

        return new UriTemplate(uri);
    }

    public UriTemplate expand(Map<String, Object> templateParameters) {
        try {
            this.uri = template.expand(templateParameters);
        } catch (VariableExpansionException e) {}

        return this;
    }

    public UriTemplate expand(String... templateParameters) {
        int size =templateParameters.length / 2;
        Map<String, Object> templatesParametersMap = new HashMap<String, Object>(size);

        for (int i = 0; i < size; i++) {
            templatesParametersMap.put(templateParameters[i], templateParameters[i++]);
        }

        return expand(templatesParametersMap);
    }

    public boolean hasParameters() {
        return true;
    }

    public String toString() {
        return uri;
    }
}
