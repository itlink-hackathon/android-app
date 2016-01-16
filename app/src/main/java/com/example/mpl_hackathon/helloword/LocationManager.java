package com.example.mpl_hackathon.helloword;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * <p/>
 * Description : Assure la gestion de la géolocalisation du mobile. Celui-ci est lié
 * à une activité. Les appels aux fonctions onStart(), onResume(), onPause() et onStop() doivent
 * être faits respectivement dans les fonctions onStart(), onResume(), onPause() et onStop() de
 * l'activité. ATTENTION : Cette activité doit gérer les résultats de retour d'une autre
 * activité pour la gestion des réglages concernant le localisation :
 * <pre>
 * {@code
 *
 * @author Maxime NATUREL
 * @version 1.0
 * @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 * switch (requestCode) {
 * // Check for the integer request code originally supplied to startResolutionForResult().
 * case LocationManager.REQUEST_CHECK_SETTINGS:
 * switch (resultCode) {
 * case Activity.RESULT_OK:
 * Log.i(TAG, "User agreed to make required location settings changes.");
 * mLocationManager.tryStartingLocationUpdates();
 * break;
 * case Activity.RESULT_CANCELED:
 * Log.i(TAG, "User chose not to make required location settings changes.");
 * break;
 * }
 * break;
 * }
 * }
 * }
 * </pre>
 * </p>
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location
                .LocationListener, ResultCallback<LocationSettingsResult> {

    /**
     * Constante utilisée dans la boîte de dialogue pour les réglages concernant la localisation.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * Tag pour les logs.
     */
    private static final String TAG = LocationManager.class.getSimpleName();
    /**
     * Intervalles désirée pour la fréquence des mises à jour de la position.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * Fréquence maximale des mises à jour de la position.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * Activité liée au LocationManager.
     */
    private Activity mActivity;
    /**
     * Client Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;
    /**
     * Position courante.
     */
    private Location mCurrentLocation;
    /**
     * Définie les parmètres pour la mise à jour de la position.
     */
    private LocationRequest mLocationRequest;
    /**
     * Définie le type de service utilisé pour la géolocalisation. Utilisé pour vérifier les
     * réglages pour déterminer si l'appareil présente les réglages optimaux concernant la
     * localisation.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Indique si la mise à jour de la position est en cours.
     */
    private boolean mRequestingLocationUpdates;

    /**
     * @param activity activité qui a besoin de connaître la position
     */
    public LocationManager(Activity activity) {
        mActivity = activity;
        mRequestingLocationUpdates = false;
        buildGoogleApiClient(activity);
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
    }

    /**
     * Etablie la connexion avec les services Google Play.
     *
     * @param context contexte utilisé pour la connexion avec les services Google Play
     */
    private synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Définie les paramètres pour la mise à jour de la position.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Vérifie que les réglages de l'appareil concernant la localisation sont suffisants pour les
     * besoins de l'application.
     */
    private void checkLocationSettings() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * Renvoie la position courante.
     *
     * @return la position courante.
     */
    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onStart d'une activité.
     */
    public void onStart() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onResume d'une activité.
     */
    public void onResume() {
        // on démarre la mise à jour de la position si besoin
        tryStartingLocationUpdates();
    }

    /**
     * Démarre la mise à jour de la position à intervalles réguliers si possible.
     */
    public void tryStartingLocationUpdates() {
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Démarre la mise à jour de la position courante à intervalles réguliers.
     */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.i(TAG, "Location updates started");
                mRequestingLocationUpdates = true;
            }
        });
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onPause d'une activité.
     */
    public void onPause() {
        // on arrête la mise à jour de la position pour économiser la batterie
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    /**
     * Arrête la mise à jour de la position courante à intervalles réguliers.
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.i(TAG, "Location updates stopped");
                mRequestingLocationUpdates = false;
            }
        });
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onStop d'une activité.
     */
    public void onStop() {
        mGoogleApiClient.disconnect();
        Log.i(TAG, "Disconnected from GoogleApiClient");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged : " + "lat : " + location.getLatitude() + ", lon : " +
                location.getLongitude());
        mCurrentLocation = location;
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }
}
