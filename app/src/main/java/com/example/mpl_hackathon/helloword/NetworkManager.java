package com.example.mpl_hackathon.helloword;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * <p>
 * Description : Singleton assurant la gestion de l'envoi des requêtes HTTP au Web server..
 * </p>
 *
 * @author Maxime NATUREL
 * @version 1.0
 */
public class NetworkManager {

    /**
     * Nom du serveur hôte.
     */
    public static final String HOSTNAME = "vps237031.ovh.net/";
    /**
     * Tag pour les logs.
     */
    private static final String TAG = NetworkManager.class.getSimpleName();
    /**
     * Mot de passe du keystore.
     */
    private static char[] KEYSTORE_PASSWORD = "!ipsis?35BrF".toCharArray();
    /**
     * Instance du singleton.
     */
    private static NetworkManager mInstance;
    /**
     * Contexte de l'application
     */
    private static Context mCtx;
    /**
     * File d'attente pour la gestion des requêtes.
     */
    private RequestQueue mRequestQueue;

    private NetworkManager(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    /**
     * Renvoie la file d'attente gérant les requêtes.
     *
     * @return file d'attente gérant les requêtes envoyés au web serveur.
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Renvoie une instance du singleton assurant la gestion de l'envoi des requêtes HTTPS au Web
     * server.
     *
     * @param context
     * @return instance du singleton assurant la gestion de l'envoi des requêtes HTTPS au Web server
     */
    public static synchronized NetworkManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkManager(context);
        }
        return mInstance;
    }

    /**
     * Ajoute une nouvelle requête dans la file d'attente.
     *
     * @param req
     * @param <T>
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
