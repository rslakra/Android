/******************************************************************************
 * Copyright (C) Devamatre Inc. 2009 - 2018. All rights reserved.
 *
 * This code is licensed to Devamatre under one or more contributor license
 * agreements. The reproduction, transmission or use of this code, in source
 * and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * 	  notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Devamatre reserves the right to modify the technical specifications and or
 * features without any prior notice.
 *****************************************************************************/
package com.rslakra.android.http;

import android.content.Context;

import com.rslakra.android.logger.LogHelper;
import com.rslakra.android.servers.HTTPApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author Rohtash Singh Lakra
 * <p>
 * https://127.0.0.1:7516/
 */
public class HTTPSClient {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPSClient";
    
    private String host;
    private int port;
    private final Context mContext;
    
    /**
     * @param host
     * @param port
     */
    public HTTPSClient(String host, int port) {
        LogHelper.i(LOG_TAG, "HTTPSClient(" + host + ", " + port + ")");
        this.host = host;
        this.port = port;
        mContext = HTTPApplication.getInstance().getApplicationContext();
    }
    
    /**
     * @return
     * @throws Exception
     */
    private SSLContext makeSSLContext() throws Exception {
        // Load CAs from an InputStream
        final InputStream certStream = LogHelper.readAssets(mContext, "client.pem");
        // Create a KeyStore containing our trusted CAs
        final KeyStore trustStore = SSLHelper.loadPEMTrustStore(certStream);
        
        // Create a TrustManager that trusts the CAs in our KeyStore
        TrustManagerFactory trustManagerFactory = SSLHelper.initTrustManager(trustStore);
        
        // Create an SSLContext that uses our TrustManager
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        LogHelper.d(LOG_TAG, "sslContext - Protocol:" + sslContext.getProtocol() + ", Provider:" + sslContext.getProvider());
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        
        return sslContext;
    }
    
    /**
     * @throws Exception
     */
    private void testSSLSocketClient() {
        try {
            // Open SSLSocket
            final SocketFactory socketFactory = makeSSLContext().getSocketFactory();
            final SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(this.host, this.port);
            
            // Start handshake
            sslSocket.startHandshake();
            
            /**
             * Verify that the certificate hostname is for [localhost],
             * This is due to lack of SNI support in the current SSLSocket.
             */
            final HostnameVerifier hostNameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            final SSLSession sslSession = sslSocket.getSession();
            if(!hostNameVerifier.verify("localhost", sslSession)) {
                throw new SSLHandshakeException("Expected [localhost], found:" + sslSession.getPeerPrincipal());
            }
            
            SSLHelper.logServerCertificate(sslSocket);
            SSLHelper.logSocketInfo(sslSocket);
            
            final PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
            
            // Send request to server.
            outWriter.println("Hello Server");
            outWriter.println();
            outWriter.flush();
            
            LogHelper.d(LOG_TAG, "Response:" + SSLHelper.readStream(sslSocket.getInputStream(), true));
            sslSocket.close();
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
    
    /**
     * @throws IOException
     */
    private void testURLConnection(final boolean useSocketFactory) throws IOException {
        try {
            LogHelper.d(LOG_TAG, "testConnection()");
            final URL url = Utils.newURL(Utils.getHost(), Utils.getPort(), true);
            final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            
            // Create socket factory
            if(useSocketFactory) {
                urlConnection.setSSLSocketFactory(makeSSLContext().getSocketFactory());
            } else {
                LogHelper.i(LOG_TAG, "This will fail because the connection is opened with self-signed certificate.");
            }
            // Initialize configuration
            //            urlConnection.setHostnameVerifier(new DefaultHostNameVerifier());
            
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            
            LogHelper.i(LOG_TAG, "ResponseCode:" + urlConnection.getResponseCode());
            LogHelper.d(LOG_TAG, "Response:" + SSLHelper.readStream(urlConnection.getInputStream(), true));
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
        
    }
    
    // Start to run the server
    public void run() {
        try {
            LogHelper.i(LOG_TAG, "\n\n");
            LogHelper.i(LOG_TAG, "TEST SSL SOCKET CONNECTION");
            LogHelper.i(LOG_TAG, "\n\n");
            testSSLSocketClient();
            
            LogHelper.i(LOG_TAG, "\n\n");
            LogHelper.i(LOG_TAG, "TEST SSL URL CONNECTION");
            LogHelper.i(LOG_TAG, "\n\n");
            testURLConnection(true);
            
            LogHelper.i(LOG_TAG, "\n\n");
            LogHelper.i(LOG_TAG, "TEST SSL URL CONNECTION WITHOUT SSL FACTORY.");
            LogHelper.i(LOG_TAG, "\n\n");
            /** This will fail because the connection is opened with self-signed certificate. */
            testURLConnection(false);
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
}