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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * @author Rohtash Singh Lakra
 */
public class HTTPSServer {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPSServer";
    
    private int port;
    private boolean mRunning = false;
    private final Context mContext;
    
    /**
     * @param port
     */
    public HTTPSServer(int port) {
        LogHelper.i(LOG_TAG, "HTTPSServer(" + port + ")");
        this.port = port;
        mContext = HTTPApplication.getInstance().getApplicationContext();
    }
    
    /**
     * Returns true if the server is running.
     *
     * @return
     */
    public boolean isRunning() {
        return mRunning;
    }
    
    /**
     * The mRunning to be set.
     *
     * @param running
     */
    private void setRunning(final boolean running) {
        mRunning = running;
    }
    
    /**
     * This means:
     * 1. You create a Keystore which reads the path of the key created previously.
     * 2. Create an SSLConext, in this case using TLS and finally a SocketFactory will create your socket which
     * happens to be an SSLSocket using an SSLServerSocket.
     * 3. BufferedWriter and BufferedReader are just to write messages back and forth between server and client.
     * Important: after an out.write you must create a newline by “\n”.
     */
    private void runHTTPSServer() {
        try {
            final Context context = HTTPApplication.getInstance().getApplicationContext();
            final InputStream keyStoreStream = LogHelper.readAssets(context, "client.bks");
            final KeyStore keyStore = SSLHelper.initKeyStore(keyStoreStream, SSLHelper.PASSWORD);
            
            // Create key manager
            final KeyManagerFactory keyManagerFactory = SSLHelper.initKeyManager(keyStore, SSLHelper.PASSWORD.toCharArray());
            
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            LogHelper.d(LOG_TAG, "sslContext - Protocol:" + sslContext.getProtocol() + ", Provider:" + sslContext.getProvider());
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            
            // Create server socket factory
            final SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            final SSLServerSocket sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(this.port);
            setRunning(true);
            LogHelper.i(LOG_TAG, "Starting SSL Server ...");
            while(isRunning()) {
                try {
                    final SSLSocket mClientSocket = (SSLSocket) sslServerSocket.accept();
                    //                SSLHelper.logSocketInfo(mClientSocket);
                    
                    // Start the new thread for each client.
                    new ServerThread(mClientSocket).start();
                } catch(Exception ex) {
                    LogHelper.e(LOG_TAG, ex);
                }
            }
            
            sslServerSocket.close();
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
    
    /**
     * Start to run the server
     */
    public void run() {
        LogHelper.i(LOG_TAG, "run()");
        try {
            runHTTPSServer();
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
    
    /**
     * Thread handling the socket from client.
     */
    private class ServerThread extends Thread {
        
        /** mClientSocket */
        private final SSLSocket mClientSocket;
        
        /**
         * @param mClientSocket
         */
        ServerThread(final SSLSocket mClientSocket) {
            LogHelper.d(LOG_TAG, "ServerThread(" + mClientSocket + ")");
            this.mClientSocket = mClientSocket;
        }
        
        /**
         *
         */
        public void run() {
            try {
                mClientSocket.setEnabledCipherSuites(mClientSocket.getSupportedCipherSuites());
                
                // Start handshake
                mClientSocket.startHandshake();
                
                // Start handling application content
                final BufferedReader reqReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                final PrintWriter resWriter = new PrintWriter(new OutputStreamWriter(mClientSocket.getOutputStream()));
                
                String line = reqReader.readLine();
                while(!LogHelper.isNullOrEmpty(line)) {
                    LogHelper.d(LOG_TAG, "Client Message:" + line);
                    line = reqReader.readLine();
                }
                reqReader.close();
                
                // Write data
                resWriter.print("HTTP/1.1 200\r\n");
                resWriter.print("\r\n");
                resWriter.flush();
                resWriter.close();
                
                mClientSocket.close();
            } catch(Exception ex) {
                LogHelper.e(LOG_TAG, ex);
            }
        }
    }
}
