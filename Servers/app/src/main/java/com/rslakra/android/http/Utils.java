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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @Author: Rohtash Singh Lakra
 * @Created: 2018/03/27 2:17 PM
 */
public final class Utils {
    
    /**
     * LOG_TAG
     */
    private static final String LOG_TAG = "Utils";
    /**
     * PASSWORD
     */
    private static final String PASSWORD = "password";
    
    /**
     * PROTOCOLS
     */
    public static final String[] PROTOCOLS = new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
    
    
    private Utils() {
    
    }
    
    /**
     * @return
     */
    public static String getHost() {
        return "localhost";
    }
    
    /**
     * @return
     */
    public static int getPort() {
        return 7516;
    }
    
    /**
     * @param host
     * @param port
     * @param sslAllowed
     * @return
     */
    public static URL newURL(String host, int port, boolean sslAllowed) {
        try {
            if(sslAllowed) {
                return new URL("https://" + host + ":" + port);
            } else {
                return new URL("http://" + host + ":" + port);
            }
        } catch(MalformedURLException ex) {
            LogHelper.e(LOG_TAG, ex);
            return null;
        }
    }
    
    /**
     * Returns the package path of the given class.
     *
     * @param className
     * @param withClassName
     * @return
     */
    public static String getPackagePath(Class<?> className, boolean withClassName) {
        if(className != null) {
            String pathString = className.getPackage().getName().replace("", File.separator);
            if(withClassName) {
                pathString += File.separator + className.getSimpleName();
            }
            
            return pathString;
        }
        
        return null;
    }
    
    /**
     * Returns the path of the given class.
     *
     * @param className
     * @return
     */
    public static String filePath(Class<?> className) {
        if(className != null) {
            String path = getPackagePath(className, false);
            if(path != null) {
                URL url = className.getClassLoader().getResource(path);
                if(url != null) {
                    path = url.toExternalForm();
                    path = path.replace(" ", "%20");
                    URI uri = null;
                    try {
                        uri = new URI(path);
                        if(uri.getPath() == null) {
                            path = uri.toString();
                            if(path.startsWith("jar:file:")) {
                                // Update path and define ZIP file
                                path = path.substring(path.indexOf("file:/"));
                                path = path.substring(0, path.toLowerCase().indexOf(".jar") + 4);
                                // Check is UNC path string
                                if(path.startsWith("file://")) {
                                    path = path.substring(path.indexOf("file:/") + 6);
                                }
                                path = new URI(path).getPath();
                            }
                        } else {
                            path = uri.getPath();
                        }
                    } catch(URISyntaxException ex) {
                        LogHelper.e(LOG_TAG, ex);
                    }
                }
            }
            
            return path;
        }
        
        return null;
    }
    
    
    /**
     * @param context
     * @return
     */
    public static SSLContext newSSLContext(final Context context) {
        SSLContext sslContext = null;
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caStream = context.getAssets().open("tjws.crt");
            Certificate ca;
            try {
                ca = certificateFactory.generateCertificate(caStream);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caStream.close();
            }
            
            // Create a KeyStore containing our trusted CAs
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            
            // Create a TrustManager that trusts the CAs in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            
            // Create an SSLContext that uses our TrustManager
            sslContext = SSLContext.getInstance(PROTOCOLS[3]);
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
        
        return sslContext;
    }
    
    /**
     * Create and initialize the SSLContext
     *
     * @param context
     * @return
     */
    public static SSLContext createServerSSLContext(final Context context) {
        try {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            LogHelper.d(LOG_TAG, "keyStoreType:" + keyStore.getType());
            final InputStream keyStoreSteam = LogHelper.readRAWResources(context, "servertruststore");
            keyStore.load(keyStoreSteam, PASSWORD.toCharArray());
            
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            LogHelper.d(LOG_TAG, "keyManagerType:" + keyManagerFactory.getAlgorithm());
            keyManagerFactory.init(keyStore, PASSWORD.toCharArray());
            final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            LogHelper.d(LOG_TAG, "trustManagerType:" + trustManagerFactory.getAlgorithm());
            trustManagerFactory.init(keyStore);
            final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance(PROTOCOLS[3]);
            LogHelper.d(LOG_TAG, "sslContext - Protocol:" + sslContext.getProtocol() + "Provider:" + sslContext.getProvider());
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            
            return sslContext;
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
        
        return null;
    }
    
    
    /**
     * Create and initialize the SSLContext
     *
     * @param context
     * @return
     */
    public static SSLContext createClientSSLContext(final Context context) {
        try {
            LogHelper.d(LOG_TAG, "KeyStoreType:" + KeyStore.getDefaultType());
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final InputStream keyStoreSteam = context.getAssets().open("clienttruststore.bks");
            keyStore.load(keyStoreSteam, PASSWORD.toCharArray());
            
            // Create key manager
            //            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            LogHelper.d(LOG_TAG, "KeyManagerType:" + keyManagerFactory.getAlgorithm());
            keyManagerFactory.init(keyStore, PASSWORD.toCharArray());
            final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            
            // Create trust manager
            //            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            LogHelper.d(LOG_TAG, "TrustManagerType:" + trustManagerFactory.getAlgorithm());
            trustManagerFactory.init(keyStore);
            final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            
            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance(PROTOCOLS[3]);
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            
            return sslContext;
        } catch(Exception ex) {
            LogHelper.e(LOG_TAG, ex);
        }
        
        return null;
    }
}
