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

import com.rslakra.android.logger.LogHelper;
import com.rslakra.android.servers.HTTPApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * This HTTPSClient diables the host name verification for the demo purposes. If
 * you wish to use this code in production, make sure you verifiy the host as
 * per your certificate.
 *
 * @author Rohtash Singh Lakra
 *         <pre>
 *                     https://127.0.0.1:7516/
 *                 </pre>
 */
public class TestURLConnection {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPSClient";
    
    /**
     * @throws IOException
     */
    public void testConnection(final boolean useSSLFactory) throws IOException {
        LogHelper.d(LOG_TAG, "testConnection()");
        HttpsURLConnection urlConnection = null;
        URL url = Utils.newURL(Utils.getHost(), Utils.getPort(), true);
        if(useSSLFactory) {
            SSLContext sslContext = Utils.createClientSSLContext(HTTPApplication.getInstance().getApplicationContext());
            //        SSLContext sslContext = Utils.newSSLContext(HTTPApplication.getInstance().getApplicationContext());
            // Create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslSocketFactory);
            urlConnection.connect();
        } else {
            // Initialize configuration
            System.setProperty("javax.net.ssl.trustStore", "res/raw/servertruststore.bks");
            //        System.setProperty("javax.net.ssl.trustStoreType", "bks");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setHostnameVerifier(new DefaultHostNameVerifier());
        }
        
        LogHelper.i(LOG_TAG, "ResponseCode:" + urlConnection.getResponseCode());
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        //            final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        //
        //            // Write data
        //            printWriter.println("/html");
        //            printWriter.println();
        //            printWriter.flush();
        
        
        if(bufferedReader != null) {
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                LogHelper.d(LOG_TAG, "line:" + line);
                //                if(line.trim().equals("HTTP/1.1 200\r\n")) {
                //                    break;
                //                }
            }
            
            bufferedReader.close();
        }
    }
}