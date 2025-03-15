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
package io.swagger.swaggerhub.v2.tasks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import io.swagger.swaggerhub.v2.client.SwaggerHubClient;
import io.swagger.swaggerhub.v2.client.SwaggerHubRequest;

import lombok.extern.slf4j.Slf4j;

/** Uploads API definition to SwaggerHub */
@Slf4j
public class UploadTask extends DefaultTask {
    private String owner;
    private String api;
    private String version;
    private String token;
    private String inputFile;
    private boolean isPrivate = false; // Geändert zu boolean
    private String host = "api.swaggerhub.com";
    private Integer port = 443;
    private String protocol = "https";
    private String format = "json";
    private String oas = "2.0";
    private Boolean onPremise = false;
    private String onPremiseAPISuffix = "v1";

    private SwaggerHubClient swaggerHubClient;

    @Input
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Input
    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    @Input
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Input
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @InputFile
    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    @Input
    public boolean isPrivate() {
        return isPrivate;
    }

    public void isPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Input
    @Optional
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Input
    @Optional
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Input
    @Optional
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Input
    @Optional
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Input
    @Optional
    public String getOas() {
        return oas;
    }

    public void setOas(String oas) {
        this.oas = oas;
    }

    @Input
    @Optional
    public Boolean getOnPremise() {
        return onPremise;
    }

    public void setOnPremise(Boolean onPremise) {
        this.onPremise = onPremise;
    }

    @Input
    @Optional
    public String getOnPremiseAPISuffix() {
        return onPremiseAPISuffix;
    }

    public void setOnPremiseAPISuffix(String onPremiseAPISuffix) {
        this.onPremiseAPISuffix = onPremiseAPISuffix;
    }

    @TaskAction
    public void uploadDefinition() throws GradleException {

        swaggerHubClient =
                new SwaggerHubClient(host, port, protocol, token, onPremise, onPremiseAPISuffix);

        log.info(
                "Uploading to {}: api: {}, owner: {}, version: {}, inputFile: {}, format: {},"
                        + " isPrivate: {}, oas: {}, onPremise: {}, onPremiseAPISuffix: {} ",
                host,
                api,
                owner,
                version,
                inputFile,
                format,
                isPrivate,
                oas,
                onPremise,
                onPremiseAPISuffix);

        try {
            String content =
                    new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);

            SwaggerHubRequest swaggerHubRequest =
                    new SwaggerHubRequest.Builder(api, owner, version)
                            .swagger(content)
                            .format(format)
                            .isPrivate(isPrivate)
                            .oas(oas)
                            .build();

            swaggerHubClient.saveDefinition(swaggerHubRequest);
        } catch (IOException | GradleException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
