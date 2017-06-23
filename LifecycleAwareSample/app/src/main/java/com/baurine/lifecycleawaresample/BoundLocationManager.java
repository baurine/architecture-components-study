package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by baurine on 6/23/17.
 */

public class BoundLocationManager {
    public static void bindLocationListenerIn(LifecycleOwner lifecycleOwner,
                                              LocationListener listener, Context context) {
        new BoundLocationListener(lifecycleOwner, listener, context);
    }

    @SuppressWarnings("MissingPermission")
    static class BoundLocationListener implements LifecycleObserver {

        private final Context mContext;
        private LocationManager mLocationManager;
        private final LocationListener mListener;

        public BoundLocationListener(LifecycleOwner owner, LocationListener listener, Context
                context) {
            mContext = context;
            mListener = listener;
            owner.getLifecycle().addObserver(this);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        void addLocationListener() {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mListener);
            Log.i("BoundLocationMgr", "Listener added");

            Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager
                    .NETWORK_PROVIDER);
            if (lastLocation != null) {
                mListener.onLocationChanged(lastLocation);
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void removeLocationListener() {
            if (mLocationManager == null) {
                return;
            }
            mLocationManager.removeUpdates(mListener);
            Log.i("BoundLocationMgr", "Listener removed");
        }
    }
}
