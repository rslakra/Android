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
package com.rslakra.android.servers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.rslakra.android.http.HTTPService;
import com.rslakra.android.logger.LogHelper;
import com.rslakra.android.logger.LogType;

/**
 * @author Rohtash Singh Lakra
 * @date 03/15/2018 03:39:08 PM
 */
public class HTTPApplication extends Application {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "HTTPApplication";
    
    /**
     * SPLASH_WAIT_TIME
     */
    private static final int SPLASH_WAIT_TIME = 1000;
    
    /**
     * instance
     */
    private static HTTPApplication sInstance;
    
    /**
     * sHTTPService
     */
    private static HTTPService sHTTPService;
    
    /**
     * Default Constructor.
     */
    public HTTPApplication() {
    }
    
    /**
     * @return
     */
    public static HTTPApplication getInstance() {
        return sInstance;
    }
    
    /**
     * @return
     */
    public static HTTPService getHTTPService() {
        return sHTTPService;
    }
    
    /**
     * Returns the package name of the application.
     * <p>
     * This package name must match as per the AndroidManifest.xml
     * file 'package="com.rslakra.android.servers"'
     *
     * @return
     */
    public String getAppPackageName() {
        return getInstance().getApplicationContext().getPackageName();
    }
    
    /**
     * Returns the application's home directory path.
     *
     * @return
     * @throws Exception
     */
    public String getAppHomeDir() {
        return getInstance().getApplicationContext().getFilesDir().getAbsolutePath();
    }
    
    
    /**
     * Returns the application's home directory path.
     *
     * @return
     * @throws Exception
     */
    public String getLogsFolder() {
        return LogHelper.pathString(getAppHomeDir(), "logs");
    }
    
    /**
     * @return
     */
    public String getDeployFolder() {
        return LogHelper.pathString(getAppHomeDir(), "webapps");
    }
    
    /**
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the singleton instance.
        if(getInstance() == null) {
            synchronized(HTTPApplication.class) {
                if(getInstance() == null) {
                    sInstance = this;
                }
            }
        }
        
        if(Thread.getDefaultUncaughtExceptionHandler() == null) {
            
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread thread, Throwable ex) {
                    LogHelper.e(LOG_TAG, "Unhandled exception " + ex + " in the thread: " + thread, ex);
                }
            });
        }
        
        
        // initialize logger
        LogHelper.log4jConfigure(getInstance().getLogsFolder(), LogType.DEBUG);
        LogHelper.i(LOG_TAG, "onCreate()");
        
        // initialize service
        initService();
    }
    
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogHelper.d(LOG_TAG, "onServiceConnected(" + className + ", " + service + ")");
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            HTTPService.LocalBinder binder = (HTTPService.LocalBinder) service;
            sHTTPService = (HTTPService) binder.getService();
            // can send notification to activities here serviceControl = service;
            sHTTPService.startServer();
            startClient();
        }
        
        /**
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogHelper.d(LOG_TAG, "onServiceDisconnected(" + componentName + ")");
            sHTTPService = null;
        }
    };
    
    /**
     * Stats the service.
     */
    public void initService() {
        // sanity
        if(getHTTPService() != null) {
            return;
        }
        
        final Thread serviceCreationThread = new Thread(new Runnable() {
            public void run() {
                /** This starts only if t is not currently running. So we don't need to worry about multiple starts.*/
                final Intent serviceIntent = new Intent(getInstance(), HTTPService.class);
                if(!isServiceRunning(HTTPService.class)) {
                    getInstance().startService(serviceIntent);
                }
                /** Defines callbacks for service binding, passed to bindService() */
                bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });
        serviceCreationThread.run();
    }
    
    
    private void startClient() {
        LogHelper.d(LOG_TAG, "startClient()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogHelper.d(LOG_TAG, "Waiting for 2000 milliseconds ...");
                    Thread.sleep(2000);
                } catch(InterruptedException ex) {
                }
                //start client
                sHTTPService.startClient();
            }
        }).start();
    }
    
    /**
     * Returns true if the given class service is running otherwise false.
     *
     * @param serviceClass
     * @return
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * Send myself a message with a correctly-formatted Intent.
     * To do that, I have to "start" myself, even though I'm already
     * started. No problem.
     * <p>
     * This method is intended to be used by the
     * NetworkConnectivityTester's BroadcastReceiver when it hears
     * about an actual network-reachability change.
     *
     * @param mContext The context passed to the broadcast receiver. No idea what it is, but I don't
     *                 care, because it lets me send myself a message.
     */
    public static void checkReachability(final Context mContext) {
        // only the key matters to me, not the payload. See onStartCommand().
        mContext.startService(new Intent(mContext, HTTPService.class));
    }
    
}