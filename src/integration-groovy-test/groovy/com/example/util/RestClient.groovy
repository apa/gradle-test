/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.example.util

import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.Response.Status.Family
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap


import org.apache.commons.codec.binary.Base64
import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean
import org.apache.cxf.jaxrs.client.WebClient
import org.apache.cxf.transport.http.HTTPConduit


/**
 * A client to simplify usage of REST service
 */
class RestClient {
    
    boolean use_https = false
    String host = "localhost"
    int port = 8888
    String baseUri = "/"
    String baseServiceName = "book"
    String usr = ""
    String passwd = ""
    String authentication = "NO"
    Map<String, String> headers
    Map<String, String> stsProps
    

    Response getResponse(String resourceUrl) {
        getResponse(resourceUrl, null)
    }

    /**
     * Helper method for lookup of resources using REST api.
     *
     * @param resourceUrl - specifies the resource type to lookup.
     * @param encodeUrl - url need to encode.
     * @return response - response from REST Service.
     */
    Response getResponse(String resourceUrl, String encodeUrl) {
        return getResponse(resourceUrl, encodeUrl, null)
    }

    /**
     * Helper method for lookup of resources using REST api.
     *
     * @param resourceUrl - specifies the resource type to lookup.
     * @param encodeUrl - url need to encode.
     * @param queryParams - params which will be added at the end of url after question mark,
     *          like http://host/my/main/url/?param1=value1&param2=value2 etc..
     * @return response - response from REST Service.
     */
    Response getResponse(String resourceUrl, String encodeUrl, Map queryParams) {
        return doExecuteCall(resourceUrl, encodeUrl, queryParams, {WebClient client ->  client.get() })
    }
    
    
    Response post(String resourceUrl, String encodeUrl, Map queryParams, Object body) {
        Closure clz = {WebClient client ->
            Map hdrs = ["aaa" : "bbb"] //[HttpHeaders.CONTENT_TYPE : MediaType.APPLICATION_JSON]
            client.headers(hdrs)
            client.post(body) }
        return doExecuteCall(resourceUrl, encodeUrl, queryParams, clz)
    }
    
    
    private Response doExecuteCall(String resourceUrl, String encodeUrl, Map queryParams, Closure methodClosure) {
        String requestUrl = ((use_https) ? "https" : "http") + "://$host:$port/$baseUri/$baseServiceName/" + "$resourceUrl"
        
        if (encodeUrl) {
            requestUrl += "/" + URLEncoder.encode("$encodeUrl", "UTF-8")
        }
        
        if ((queryParams != null) && (queryParams.size() > 0)) {
            requestUrl += "/?"  + queryParams.collect {key, value -> "$key=$value"}.join ('&')
        }

        
        println "Url to be called using rest client: $requestUrl"
        
        JAXRSClientFactoryBean factoryBean = new JAXRSClientFactoryBean();
        factoryBean.setThreadSafe(true);
        factoryBean.setAddress(requestUrl);
        WebClient client = null;
        
        if ("BASIC" == authentication) {
            factoryBean.setUsername(usr);
            factoryBean.setPassword(passwd);
            client = factoryBean.createWebClient()
            client.header("Authorization", basicLogin)
        } else {
            client = factoryBean.createWebClient()
        }
        
        if (use_https) {
            configureHttps(client)
        }
        
        if (headers) {
            client.headers(headers)
        }
        

        Response response = methodClosure(client)
        
        response.metaClass.expectOK {
            if (delegate.statusInfo.family.equals(Family.SUCCESSFUL)) {
                return delegate
            } else {
                throw new IllegalStateException("Received response with status: "
                        + "${delegate.status} and reason phrase "
                        + "${delegate.statusInfo.reasonPhrase}, however expected ${Status.OK} status.")
            }
        }
        return response    
    }
    
    String getBasicLogin() {
        return (usr) ? "Basic " + new String(Base64.encodeBase64((usr + ":" + passwd).getBytes())) : ""
    }

    void configureHttps(WebClient webClient) {
        HTTPConduit conduit = WebClient.getConfig(webClient).httpConduit
        WebClient.getConfig(webClient).httpConduit.client.chunkingThreshold = 1024 * 64
        TLSClientParameters tlsClientParams = new TLSClientParameters()
        conduit.tlsClientParameters = tlsClientParams
        def trustManagers = [new FakeX509TrustManager()] as TrustManager[]
        tlsClientParams.trustManagers = trustManagers
        tlsClientParams.disableCNCheck = true
    }

    class FakeX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]
        }
    }
}
