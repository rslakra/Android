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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
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
    
    /**
     * @param port
     */
    public HTTPSServer(int port) {
        LogHelper.i(LOG_TAG, "HTTPSServer(" + port + ")");
        this.port = port;
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
     * Start to run the server
     */
    public void run() {
        LogHelper.i(LOG_TAG, "run()");
        try {
            final SSLContext sslContext = Utils.createServerSSLContext(HTTPApplication.getInstance().getApplicationContext());
//            final SSLContext sslContext = Utils.newSSLContext(HTTPApplication.getInstance().getApplicationContext());
//            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            // Create server socket
//            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
            setRunning(true);
            LogHelper.i(LOG_TAG, "SSL server has started!");
            while(isRunning()) {
                try {
                    final SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                    // Start the server thread
                    new ServerThread(sslSocket).start();
                } catch(Exception ex) {
                    LogHelper.e(LOG_TAG, ex);
                }
            }
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
    }
    
    /**
     * Thread handling the socket from client.
     */
    private class ServerThread extends Thread {
        
        private final SSLSocket mSSLSocket;
        
        /**
         * @param sslSocket
         */
        ServerThread(final SSLSocket sslSocket) {
            LogHelper.d(LOG_TAG, "ServerThread(" + sslSocket + ")");
            this.mSSLSocket = sslSocket;
        }
        
        /**
         *
         */
        public void run() {
            try {
                LogHelper.d(LOG_TAG, "isBound:" + mSSLSocket.isBound());
                LogHelper.d(LOG_TAG, "isClosed:" + mSSLSocket.isClosed());
                LogHelper.d(LOG_TAG, "isConnected:" + mSSLSocket.isConnected());
                LogHelper.d(LOG_TAG, "supportedCipherSuites:" + LogHelper.toString(mSSLSocket.getSupportedCipherSuites(), true));
                LogHelper.d(LOG_TAG, "supportedProtocols:" + LogHelper.toString(mSSLSocket.getSupportedProtocols(), true));
                mSSLSocket.setEnabledCipherSuites(mSSLSocket.getSupportedCipherSuites());
                
                // Start handshake
                mSSLSocket.startHandshake();
                
                // Get session after the connection is established
                final SSLSession mSSLSession = mSSLSocket.getSession();
                
                LogHelper.d(LOG_TAG, "SSLSession:");
                LogHelper.d(LOG_TAG, "Protocol:" + mSSLSession.getProtocol());
                LogHelper.d(LOG_TAG, "Cipher Suite:" + mSSLSession.getCipherSuite());
                
                // Start handling application content
                InputStream inputStream = mSSLSocket.getInputStream();
                OutputStream outputStream = mSSLSocket.getOutputStream();
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                
                String line = null;
                while((line = bufferedReader.readLine()) != null) {
                    LogHelper.d(LOG_TAG, "line:" + line);
                    if(line.trim().isEmpty()) {
                        break;
                    }
                }
                
                // Write data
                printWriter.print("HTTP/1.1 200\r\n");
                printWriter.flush();
                
                mSSLSocket.close();
            } catch(Exception ex) {
                LogHelper.e(LOG_TAG, ex);
            }
        }
    }
}
