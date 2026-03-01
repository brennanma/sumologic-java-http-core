/**
 *    _____ _____ _____ _____    __    _____ _____ _____ _____
 *   |   __|  |  |     |     |  |  |  |     |   __|     |     |
 *   |__   |  |  | | | |  |  |  |  |__|  |  |  |  |-   -|   --|
 *   |_____|_____|_|_|_|_____|  |_____|_____|_____|_____|_____|
 *
 *                UNICORNS AT WARP SPEED SINCE 2010
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.sumologic.http.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

public class HttpProxySettingsCreator {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxySettingsCreator.class);
    private ProxySettings proxySettings;

    public HttpProxySettingsCreator(ProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    private String hostname() {
        String host = "localhost";
        try {
            host = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.error("Unable to obtain local hostname. Defaulting to localhost", e);
        }
        return host;
    }

    private BasicCredentialsProvider createCredentialsProvider() {
        String username = proxySettings.getUsername();
        char[] password = proxySettings.getPassword().toCharArray();
        String domain = proxySettings.getDomain();

        if (ProxySettings.BASIC_AUTH.equals(proxySettings.getAuthType())) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxySettings.getHostname(), proxySettings.getPort()),
                    new UsernamePasswordCredentials(username, password));
            return credsProvider;
        } else if (ProxySettings.NTLM_AUTH.equals(proxySettings.getAuthType())) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxySettings.getHostname(), proxySettings.getPort()),
                    new NTCredentials(username, password, hostname(), domain));
            return credsProvider;
        } else {
            throw new IllegalStateException(
                    "proxyAuth " + proxySettings.getAuthType() + " not supported!");
        }
    }

    public void configureProxySettings(HttpClientBuilder builder) {
        proxySettings.validate();
        String proxyHost = proxySettings.getHostname();
        int proxyPort = proxySettings.getPort();
        String proxyAuth = proxySettings.getAuthType();

        if (proxyHost != null) {
            HttpHost host = new HttpHost(proxyHost, proxyPort);
            builder.setProxy(host);

            if (proxyAuth != null) {
                BasicCredentialsProvider credsProvider = createCredentialsProvider();
                builder.setDefaultCredentialsProvider(credsProvider);
            }
        }
    }
}
