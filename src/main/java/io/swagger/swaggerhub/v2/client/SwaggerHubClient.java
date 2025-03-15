/*
 * Copyright 2020 SmartBear Software Inc.
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
 *
 * ########################################################################
 *
 * Copyright 2025 Ludy87
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
package io.swagger.swaggerhub.v2.client;

import java.io.IOException;

import org.gradle.api.GradleException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SwaggerHubClient {
    private static final String DOWNLOAD_FAILED_ERROR = "Failed to download API definition: ";
    private static final String UPLOAD_FAILED_ERROR = "Failed to upload API definition: ";
    private final OkHttpClient client;
    private final String host;
    private final int port;
    private final String token;
    private final String protocol;
    private final Boolean onPremise;
    private final String onPremiseAPISuffix;
    private static final String APIS = "apis";

    public SwaggerHubClient(
            String host,
            int port,
            String protocol,
            String token) { // TODO: Worth removing after due consideration
        this(host, port, protocol, token, false, null);
    }

    public SwaggerHubClient(
            String host,
            int port,
            String protocol,
            String token,
            Boolean onPremise,
            String onPremiseAPISuffix) {
        client = new OkHttpClient();
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.token = token;
        this.onPremise = onPremise;
        this.onPremiseAPISuffix = onPremiseAPISuffix;
    }

    public String getDefinition(SwaggerHubRequest swaggerHubRequest) throws GradleException {
        HttpUrl httpUrl = getDownloadUrl(swaggerHubRequest);
        MediaType mediaType = getMediaType(swaggerHubRequest);
        Request requestBuilder = buildGetRequest(httpUrl, mediaType);

        try (Response response = client.newCall(requestBuilder).execute()) {
            if (response.body() == null) {
                throw new GradleException(DOWNLOAD_FAILED_ERROR + "Response body is empty");
            } else if (!response.isSuccessful()) {
                throw new GradleException(DOWNLOAD_FAILED_ERROR + response.body().string());
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new GradleException(DOWNLOAD_FAILED_ERROR, e);
        }
    }

    private Request buildGetRequest(HttpUrl httpUrl, MediaType mediaType) {
        Request.Builder requestBuilder =
                new Request.Builder()
                        .url(httpUrl)
                        .addHeader("Accept", mediaType.toString())
                        .addHeader("User-Agent", "swaggerhub-gradle-plugin");
        if (token != null) {
            requestBuilder.addHeader("Authorization", token);
        }
        return requestBuilder.build();
    }

    public void saveDefinition(SwaggerHubRequest swaggerHubRequest) throws GradleException {
        HttpUrl httpUrl = getUploadUrl(swaggerHubRequest);
        MediaType mediaType = getMediaType(swaggerHubRequest);
        Request httpRequest = buildPostRequest(httpUrl, mediaType, swaggerHubRequest.getSwagger());

        try (Response response = client.newCall(httpRequest).execute()) {
            if (response.body() == null) {
                throw new GradleException(UPLOAD_FAILED_ERROR + "Response body is empty");
            } else if (!response.isSuccessful()) {
                throw new GradleException(UPLOAD_FAILED_ERROR + response.body().string());
            }
        } catch (IOException e) {
            throw new GradleException(UPLOAD_FAILED_ERROR, e);
        }
    }

    private Request buildPostRequest(HttpUrl httpUrl, MediaType mediaType, String content) {
        return new Request.Builder()
                .url(httpUrl)
                .addHeader("Content-Type", mediaType.toString())
                .addHeader("Authorization", token)
                .addHeader("User-Agent", "swaggerhub-gradle-plugin")
                .post(RequestBody.create(content, mediaType))
                .build();
    }

    private HttpUrl getDownloadUrl(SwaggerHubRequest swaggerHubRequest) {
        return getBaseUrl(swaggerHubRequest.getOwner(), swaggerHubRequest.getApi())
                .addEncodedPathSegment(swaggerHubRequest.getVersion())
                .addQueryParameter("resolved", String.valueOf(swaggerHubRequest.resolved()))
                .build();
    }

    private HttpUrl getUploadUrl(SwaggerHubRequest swaggerHubRequest) {
        return getBaseUrl(swaggerHubRequest.getOwner(), swaggerHubRequest.getApi())
                .addEncodedQueryParameter("version", swaggerHubRequest.getVersion())
                .addEncodedQueryParameter(
                        "isPrivate", Boolean.toString(swaggerHubRequest.isPrivate()))
                .addEncodedQueryParameter("oas", swaggerHubRequest.getOas())
                .build();
    }

    private HttpUrl.Builder getBaseUrl(String owner, String api) {
        return new HttpUrl.Builder()
                .scheme(protocol)
                .host(host)
                .port(port)
                .addPathSegment(onPremise ? onPremiseAPISuffix : "")
                .addPathSegment(APIS)
                .addEncodedPathSegment(owner)
                .addEncodedPathSegment(api);
    }

    private MediaType getMediaType(SwaggerHubRequest swaggerHubRequest) {
        String headerFormat = "application/%s; charset=utf-8";
        MediaType mediaType =
                MediaType.parse(String.format(headerFormat, swaggerHubRequest.getFormat()));
        if (mediaType == null) {
            mediaType = MediaType.parse(String.format(headerFormat, "json"));
        }
        return mediaType;
    }
}
