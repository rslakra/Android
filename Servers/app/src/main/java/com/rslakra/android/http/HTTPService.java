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

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;

import com.rslakra.android.logger.LogHelper;

/**
 * Android HTTPSServer service
 */
public class HTTPService extends Service {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPService";
    
    /**
     * wifiLock
     */
    private WifiLock wifiLock;
    
    /**
     * mHTTPSServer
     */
    private HTTPSServer mHTTPSServer;
    
    /**
     * mBinder
     */
    private final IBinder mBinder = new LocalBinder();
    
    /**
     * Default Constructor.
     */
    public HTTPService() {
        LogHelper.i(LOG_TAG, "HTTPService()");
    }
    
    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public HTTPService getService() {
            return HTTPService.this;
        }
    }
    
    /**
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.d(LOG_TAG, "Binding from " + intent.getClass().getName());
        return mBinder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    /**
     * @return
     */
    public boolean isRunning() {
        return (mHTTPSServer != null && mHTTPSServer.isRunning());
    }
    
    /**
     * Starts the server.
     */
    public void startServer() {
        if(isRunning()) {
            LogHelper.w(LOG_TAG, "Server is already running!");
        } else {
            mHTTPSServer = new HTTPSServer(Utils.getPort());
            
            /** thread to check server status. */
            new Thread() {
                /**
                 * Check the staus of the server.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    // give a chance to server to run
                    while(!isRunning()) {
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException ex) {
                            // ignore me!
                        }
                    }
                    
                    LogHelper.i(LOG_TAG, "Serve running:" + isRunning());
                    if(isRunning()) {
                        LogHelper.i(LOG_TAG, "HTTPSServer has started!!!");
                    } else {
                        LogHelper.i(LOG_TAG, "HTTPSServer has not started!!!");
                    }
                }
                
            }.start();
            
            /** thread to run the server. */
            new Thread() {
                /**
                 * Starts the web server here.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    try {
                        mHTTPSServer.run();
                    } catch(Throwable throwable) {
                        LogHelper.e(LOG_TAG, throwable);
                    }
                }
            }.start();
        }
    }
    
    
    /**
     * Stops the webServer and all it's services.
     */
    public void stopServer() {
        if(isRunning()) {
            mHTTPSServer = null;
        }
        LogHelper.i(LOG_TAG, "Web Server is stopped successfully!");
    }
    
    /**
     *
     */
    public void startClient() {
        LogHelper.d(LOG_TAG, "startClient()");
        new HTTPSClient(Utils.getHost(), Utils.getPort()).run();
    }
    
    
    /**
     * Destroys the current service.
     */
    @Override
    public void onDestroy() {
        LogHelper.d(LOG_TAG, "Service Destroyed!");
        // just in case, not stoppped yet.
        stopServer();
        super.onDestroy();
    }
}
