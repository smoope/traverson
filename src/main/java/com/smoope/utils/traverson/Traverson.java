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

import com.smoope.utils.traverson.security.TraversonAuthenticator;
import com.smoope.utils.traverson.utils.UriTemplate;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Component for traversing hypermedia APIs
 * Java version of https://github.com/basti1302/traverson
 *
 * @since 1.0.0
 */
@Slf4j
public class Traverson {

    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_CONTENT_LENGHT = "Content-Length";

    private static final String METHOD_POST = "POST";

    private static final String METHOD_PUT = "PUT";

    private final String baseUri;

    private final OkHttpClient client;

    private final Gson serializer;

    private Map<String, String> defaultHeaders;

    /**
     * Constructor with parameters
     *
     * @param baseUri API's base uri
     * @param client HTTP client implementation
     * @param serializer Serializer
     * @param defaultHeaders Default headers
     *
     * @since 1.0.0
     */
    private Traverson(final String baseUri, final OkHttpClient client, final Gson serializer,
                      final Map<String, String> defaultHeaders) {
        this.baseUri = baseUri;
        this.client = client;
        this.serializer = serializer;
        this.defaultHeaders = defaultHeaders;
    }

    /**
     * Adds default header
     *
     * @param name Name
     * @param value Value
     * @since 1.0.0
     */
    public void addDefaultHeader(final String name, final String value) {
        this.defaultHeaders.put(name, value);
    }

    /**
     * Removes default header
     *
     * @param name Name
     * @since 1.0.0
     */
    public void removeDefaultHeader(final String name) {
        if (this.defaultHeaders.containsKey(name)) {
            this.defaultHeaders.remove(name);
        }
    }

    /**
     * Follows specified endpoints
     *
     * @param rels List of endpoints to follow
     * @return Traversing object
     * @since 1.0.0
     */
    public Traversing follow(String... rels) {
        return new Traversing(baseUri).follow(rels);
    }

    /**
     * Follows specified uri
     *
     * @param link Uri to follow
     * @return Traversing object
     * @since 1.0.0
     */
    public Traversing followUri(String link) {
        return new Traversing(baseUri).followUri(link);
    }

    /**
     * Returns a new Traversing with given uri as root
     *
     * @param newBaseUri Uri to set as root
     * @return Traversing object
     * @since 1.0.0
     */
    public Traversing root(String newBaseUri) {
        return new Traversing(newBaseUri);
    }

    public class Traversing {

        private final String rootUri;

        private final List<String> rels;

        private final Map<String, String> headers;

        private final Map<String, Object> templateParameters;

        private boolean follow201Location;

        private boolean traverse = true;

        public Traversing(String rootUri) {
            this.rootUri = rootUri;
            this.rels = new ArrayList<String>();
            this.templateParameters = new HashMap<String, Object>();
            this.headers = new HashMap<String, String>();
        }

        private Request prepareRequest(final String url, final RequestBody object,
                                       final RequestMethod method) {
            HashMap<String, String> allHeaders = new HashMap<String, String>();
            allHeaders.putAll(defaultHeaders);
            allHeaders.putAll(headers);

            Request.Builder request = new Request.Builder()
                    .url(url)
                    .headers(Headers.of(allHeaders));

            switch (method) {
                case GET:
                    return request.get().build();
                case POST:
                    return request.post(object).build();
                case PUT:
                    return request.put(object).build();
                case DELETE:
                    return request.delete().build();
                default:
                    throw new IllegalArgumentException("Not supported request method");
            }
        }

        private Request prepareRequest(final String url, final RequestMethod method) {
            return prepareRequest(url, null, method);
        }

        private <T> T prepareResponse(final CallResult result, Type returnType) throws IOException {
            return serializer.fromJson(result.isResponse() ? result.getResponse().body().string() : result.getEmbedded(), returnType);
        }

        private Response handleErrors(final Response response) throws TraversonException {
            switch (response.code()) {
                case 200:
                case 201:
                case 204:
                case 307:
                    return response;
                default: {
                    response.close();

                    throw new TraversonException(response.code(), response.message(), response.request().url().toString());
                }
            }
        }

        private CallResult call(final RequestMethod method, final RequestBody object) throws TraversonException, IOException {
            TraversingResult traversed = traverse
                    ? traverseToFinalUrl()
                    : TraversingResult.url(UriTemplate.fromUri(rels.get(0)).expand(templateParameters).toString());

            if (traversed.isUrl()) {
                return CallResult.response(
                        handleErrors(
                                client.newCall(
                                        prepareRequest(
                                                traversed.getUrl(),
                                                object,
                                                method)
                                ).execute()
                        )
                );
            } else {
                return CallResult.embedded(traversed.getEmbedded());
            }
        }

        private CallResult call(final RequestMethod method) throws TraversonException, IOException {
            return call(method, RequestBody.create(null, new byte[0]));
        }

        private TraversingResult traverseToFinalUrl() throws TraversonException, IOException {
            return getAndFindLinkWithRel(rootUri, rels.iterator());
        }

        private TraversingResult getAndFindLinkWithRel(String url, Iterator<String> rels)
                throws TraversonException, IOException {
            log.debug("Traversing an URL: {}", url);

            if (!rels.hasNext()) {
                return TraversingResult.url(url);
            }

            TraversonResult<Object> response = prepareResponse(
                    CallResult.response(
                            handleErrors(
                                    client.newCall(
                                            prepareRequest(url, RequestMethod.GET)
                                    ).execute()
                            )
                    ),
                    TraversonResult.class
            );

            String next = rels.next();
            if (response.getEmbedded().containsKey(next)) {
                return TraversingResult.embedded(serializer.toJson(response.getEmbedded().get(next)));
            }

            TraversonLink link = response.getLinkForRel(next);
            if (link == null) {
                throw new TraversonException(
                        404,
                        String.format("Couldn't find '%s' in %s", next, response),
                        ""
                );
            }

            UriTemplate template = UriTemplate.fromUri(link.getHref());
            if (template.hasParameters()) {
                return getAndFindLinkWithRel(template.expand(templateParameters).toString(), rels);
            } else {
                return getAndFindLinkWithRel(template.toString(), rels);
            }
        }

        private Response handle201LocationRedirect(Response response) throws IOException {
            if (follow201Location && response.code() == 201 && response.header("Location") != null) {
                return client.newCall(prepareRequest(response.header("Location"), RequestMethod.GET)).execute();
            } else {
                return response;
            }
        }

        public Traversing follow(String... rels) {
            for (String rel : rels) {
                this.rels.add(rel);
            }
            this.traverse = true;

            return this;
        }

        public Traversing followUri(String link) {
            this.rels.add(link);
            this.traverse = false;

            return this;
        }

        public Traversing withHeaders(final Map<String, String> headers) {
            this.headers.putAll(headers);

            return this;
        }

        public Traversing withHeader(final String name, final String value) {
            this.headers.put(name, value);

            return this;
        }

        public Traversing withTemplateParameter(final String name, final String value) {
            this.templateParameters.put(name, value);

            return this;
        }

        public Traversing withTemplateParameters(Map<String, Object> parameters) {
            this.templateParameters.putAll(parameters);

            return this;
        }

        public Traversing follow201Location(final boolean follow201Location) {
            this.follow201Location = follow201Location;

            return this;
        }

        public RequestBody json(final Object body) {
            return RequestBody.create(CONTENT_TYPE_JSON, serializer.toJson(body));
        }

        public Response get() throws TraversonException, IOException {
            return call(RequestMethod.GET).getResponse();
        }

        public <T> T get(Class<T> returnType) throws TraversonException, IOException {
            return prepareResponse(
                    call(RequestMethod.GET),
                    returnType
            );
        }

        public <T> T get(Type type) throws TraversonException, IOException {
            return prepareResponse(
                    call(RequestMethod.GET),
                    type
            );
        }

        public Response post() throws TraversonException, IOException {
            return call(RequestMethod.POST).getResponse();
        }

        public Response post(Object body) throws TraversonException, IOException {
            return call(RequestMethod.POST, json(body)).getResponse();
        }

        public <T, R> R post(T body, Class<R> returnType) throws TraversonException, IOException {
            return (R) prepareResponse(
                    CallResult.response(handle201LocationRedirect(call(RequestMethod.POST, json(body)).getResponse())),
                    returnType
            );
        }

        public <R> R postForm(Map<String, String> params, Class<R> returnType) throws TraversonException, IOException {
            final MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            params.forEach((name, value) -> requestBody.addFormDataPart(name, value));

            return (R) prepareResponse(
                    CallResult.response(handle201LocationRedirect(call(RequestMethod.POST,
                            requestBody.build()).getResponse())),
                    returnType
            );
        }

        public Response postFile(File file) throws TraversonException, IOException {
            HashMap<String, String> allHeaders = new HashMap<String, String>();
            allHeaders.putAll(defaultHeaders);
            allHeaders.putAll(headers);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                            "file",
                            file.getName(),
                            RequestBody.create(
                                    MediaType.parse(URLConnection.guessContentTypeFromName(file.getName())),
                                    file
                            )
                    )
                    .build();

            return handleErrors(
                    client.newCall(
                            new Request.Builder()
                                    .url(UriTemplate.fromUri(rels.get(0)).expand(templateParameters).toString())
                                    .headers(Headers.of(allHeaders))
                                    .post(requestBody)
                                    .build()
                    ).execute()
            );
        }

        public <T, R> R put(T object, Class<R> returnType) throws TraversonException, IOException {
            return prepareResponse(
                    call(RequestMethod.PUT, json(object)),
                    returnType
            );
        }

        public void delete() throws TraversonException, IOException {
            call(RequestMethod.DELETE);
        }
    }

    /**
     * Builder implementation
     *
     * @since 1.2.0
     */
    public static class Builder {

        private final String baseUri;

        private final OkHttpClient.Builder client;

        private Gson serializer;

        private Map<String, String> defaultHeaders;

        /**
         * Constructor with parameters
         *
         * @param baseUri API's base uri
         * @since 1.2.0
         */
        public Builder(final String baseUri) {
            this.baseUri = baseUri;
            this.client = new OkHttpClient.Builder();
            this.serializer = new Gson();
            this.defaultHeaders = new HashMap<String, String>();
        }

        /**
         * Sets default cache
         *
         * @param path Cache directory's path
         * @param size Cache's size
         * @return Builder object
         * @since 1.2.0
         */
        public Builder cache(final File path, final int size) {
            this.client.cache(new Cache(path, size));

            return this;
        }

        /**
         * Disables the cache
         *
         * @return Builder object
         * @since 1.2.0
         */
        public Builder disableCache() {
            this.client.cache(null);

            return this;
        }

        /**
         * Sets default read timeout
         *
         * @param timeout Timeout
         * @param unit Time unit
         * @return Builder object
         * @since 1.2.0
         */
        public Builder readTimeout(final long timeout, final TimeUnit unit) {
            this.client.readTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets default write timeout
         *
         * @param timeout Timeout
         * @param unit Time unit
         * @return Builder object
         * @since 1.2.0
         */
        public Builder writeTimeout(final long timeout, final TimeUnit unit) {
            this.client.writeTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets default connection timeout
         *
         * @param timeout Timeout
         * @param unit Time unit
         * @return Builder object
         * @since 1.2.0
         */
        public Builder connectTimeout(final long timeout, final TimeUnit unit) {
            this.client.connectTimeout(timeout, unit);

            return this;
        }

        /**
         * Sets default serializer
         *
         * @param serializer Serializer
         * @return Builder object
         * @since 1.2.0
         */
        public Builder serializer(final Gson serializer) {
            this.serializer = serializer;

            return this;
        }

        /**
         * Sets default headers
         *
         * @param defaultHeaders Map of key-value values
         * @return Builder object
         * @since 1.2.0
         */
        public Builder defaultHeaders(final Map<String, String> defaultHeaders) {
            this.defaultHeaders.putAll(defaultHeaders);

            return this;
        }

        /**
         * Sets default header
         *
         * @param name Name
         * @param value Value
         * @return Builder object
         * @since 1.2.0
         */
        public Builder defaultHeader(final String name, final String value) {
            this.defaultHeaders.put(name, value);

            return this;
        }

        /**
         * Sets default authenticator
         *
         * @param authenticator Authenticator
         * @return Builder object
         * @since 1.2.0
         */
        public Builder authenticator(final TraversonAuthenticator authenticator) {
            return authenticator(authenticator, false);
        }

        /**
         * Sets default authenticator
         *
         * @param authenticator Authenticator
         * @param preemptive Pre-authenticate requests
         * @return Builder object
         * @since 1.4.0
         */
        public Builder authenticator(final TraversonAuthenticator authenticator, final boolean preemptive) {
            this.client.authenticator(new okhttp3.Authenticator() {

                public Request authenticate(Route route, Response response) throws IOException {
                    String credentials = authenticator.getCredentials();
                    if (credentials.equals(response.request().header("Authorization"))) {
                        throw new TraversonException(401, "Unauthorized", response.request().url().toString());
                    } else {
                        defaultHeader("Authorization", credentials);

                        Request.Builder newRequest = response.request().newBuilder()
                                .headers(Headers.of(defaultHeaders));

                        if (METHOD_POST.equalsIgnoreCase(response.request().method()) || METHOD_PUT.equalsIgnoreCase(response.request().method())) {
                            newRequest.addHeader(HEADER_CONTENT_TYPE, response.request().header(HEADER_CONTENT_TYPE))
                                    .addHeader(HEADER_CONTENT_LENGHT, response.request().header(HEADER_CONTENT_LENGHT))
                                    .method(response.request().method(), response.request().body());
                        }

                        return newRequest.build();
                    }
                }
            });
            if (preemptive) {
                this.client.interceptors().add(0, new okhttp3.Interceptor() {

                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        if (request.header("Authorization") == null) {
                            defaultHeader("Authorization", authenticator.getCredentials());
                            request = request.newBuilder()
                                    .headers(Headers.of(defaultHeaders))
                                    .build();
                        }

                        return chain.proceed(request);
                    }
                });
            }

            return this;
        }

        /**
         * Builds Traverson object
         *
         * @return Traverson object
         * @since 1.2.0
         */
        public Traverson build() {
            return new Traverson(this.baseUri, this.client.build(), this.serializer,
                    this.defaultHeaders);
        }
    }

    /**
     * Available HTTP methods
     *
     * @since 1.0.0
     */
    public enum RequestMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * Defines HTTP exception
     *
     * @since 1.0.0
     */
    public static class TraversonException extends RuntimeException {

        private final int code;

        public TraversonException(final int code, final String message, final String url) {
            super(String.format("HTTP response's code: %d - %s at %s", code, message, url));

            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TraversingResult {

        private final String url;

        private final String embedded;

        public boolean isUrl() {
            return url != null;
        }

        public boolean isEmbedded() {
            return embedded != null;
        }

        public static TraversingResult url(String url) {
            return new TraversingResult(url, null);
        }

        public static TraversingResult embedded(String embedded) {
            return new TraversingResult(null, embedded);
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CallResult {

        private final Response response;

        private final String embedded;

        public boolean isResponse() {
            return response != null;
        }

        public boolean isEmbedded() {
            return embedded != null;
        }

        public static CallResult response(Response response) {
            return new CallResult(response, null);
        }

        public static CallResult embedded(String embedded) {
            return new CallResult(null, embedded);
        }
    }
}
