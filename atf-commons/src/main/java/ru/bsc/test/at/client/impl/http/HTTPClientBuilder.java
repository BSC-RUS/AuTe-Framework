/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.client.impl.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * @author Pavel Golovkin
 */
@Slf4j
public class HTTPClientBuilder {

  private SSLContext sslContext;
  private CookieStore cookieStore;
  private RequestConfig globalConfig;

  public HTTPClientBuilder withSllContext() {
    try {
      sslContext = SSLContext.getInstance("SSL");

      // set up a TrustManager that trusts everything
      sslContext.init(null, new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
          log.info("checkClientTrusted =============");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
          log.info("checkServerTrusted =============");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }

      }}, new SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      log.error("Error while init SSL context", e);
      sslContext = null;
    }

    return this;
  }

  public HTTPClientBuilder withCookiesStore() {
    cookieStore = new BasicCookieStore();
    return this;
  }

  public HTTPClientBuilder withGlobalConfig() {
    globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
    return this;
  }

  public CloseableHttpClient build() {
    org.apache.http.impl.client.HttpClientBuilder clientBuilder = HttpClients.custom();
    if (cookieStore != null) {
      clientBuilder.setDefaultCookieStore(cookieStore);
    }
    if (sslContext != null) {
      clientBuilder.setSSLContext(sslContext);
    }
    if (globalConfig != null) {
      clientBuilder.setDefaultRequestConfig(globalConfig);
    }

    return clientBuilder.build();
  }
}
