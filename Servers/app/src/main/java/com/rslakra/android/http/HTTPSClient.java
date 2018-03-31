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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author Rohtash Singh Lakra
 *         <p>
 *         https://127.0.0.1:7516/
 */
public class HTTPSClient {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPSClient";
    
    private String host;
    private int port;
    
    /**
     * @param host
     * @param port
     */
    public HTTPSClient(String host, int port) {
        LogHelper.i(LOG_TAG, "HTTPSClient(" + host + ", " + port + ")");
        this.host = host;
        this.port = port;
    }
    
    // Start to run the server
    public void run() {
        try {
            final boolean useSocket = true;
            if(useSocket) {
                LogHelper.d(LOG_TAG, "SSL client started");
                // Open SSLSocket
                SocketFactory socketFactory = SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) socketFactory.createSocket(this.host, this.port);
                new ClientThread(sslSocket).start();
            } else {
                new TestURLConnection().testConnection(false);
            }
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
    
    // Thread handling the socket to server
    private class ClientThread extends Thread {
        /**
         * sslSocket
         */
        private final SSLSocket mSSLSocket;
        
        ClientThread(final SSLSocket sslSocket) {
            LogHelper.d(LOG_TAG, "ClientThread(" + sslSocket + ")");
            this.mSSLSocket = sslSocket;
        }
        
        public void run() {
            try {
                LogHelper.d(LOG_TAG, "supportedCipherSuites:" + LogHelper.toString(mSSLSocket.getSupportedCipherSuites(), true));
                LogHelper.d(LOG_TAG, "supportedProtocols:" + LogHelper.toString(mSSLSocket.getSupportedProtocols(), true));
                mSSLSocket.setEnabledCipherSuites(mSSLSocket.getSupportedCipherSuites());
                
                // Start handshake
                mSSLSocket.startHandshake();
                
                // Get session after the connection is established
                final SSLSession sslSession = mSSLSocket.getSession();
                LogHelper.d(LOG_TAG, "SSLSession:");
                LogHelper.d(LOG_TAG, "Protocol:" + sslSession.getProtocol());
                LogHelper.d(LOG_TAG, "Cipher Suite:" + sslSession.getCipherSuite());
                
                // Start handling application content
                InputStream inputStream = mSSLSocket.getInputStream();
                OutputStream outputStream = mSSLSocket.getOutputStream();
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                
                // Write data
                printWriter.println("Hello Server");
                printWriter.println();
                printWriter.flush();
                
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    LogHelper.d(LOG_TAG, "line:" + line);
                    if(line.trim().equals("HTTP/1.1 200\r\n")) {
                        break;
                    }
                }
                
                mSSLSocket.close();
            } catch(Exception ex) {
                LogHelper.e(LOG_TAG, ex);
            }
        }
    }
}