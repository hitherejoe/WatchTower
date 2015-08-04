package com.hitherejoe.proximityapidemo.android.data.remote;

import com.hitherejoe.proximityapidemo.android.data.model.Attachment;
import com.hitherejoe.proximityapidemo.android.data.model.Beacon;
import com.hitherejoe.proximityapidemo.android.data.model.Diagnostics;
import com.hitherejoe.proximityapidemo.android.data.model.Namespace;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface ProximityApiService {

    String ENDPOINT = "https://proximitybeacon.googleapis.com/v1beta1";

    /**
     * Return a list of beacons
     */
    @GET("/beacons")
    Observable<BeaconsResponse> getBeacons();

    /**
     * Return a single beacon
     */
    @GET("/{beaconName}")
    Observable<Beacon> getBeacon(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Register a beacon
     */
    @POST("/beacons:register")
    Observable<Beacon> registerBeacon(@Body Beacon beacon);

    /**
     * Update a beacon
     */
    @PUT("/{beaconName}")
    Observable<Beacon> updateBeacon(@Path(value="beaconName", encode=false) String beaconName, @Body Beacon beacon);

    /**
     * Activate a beacon
     */
    @POST("/{beaconName}:activate")
    Observable<Beacon> activateBeacon(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Deactivate a beacon
     */
    @POST("/{beaconName}:deactivate")
    Observable<Beacon> deactivateBeacon(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Decomission a beacon
     */
    @POST("/{beaconName}:decommission")
    Observable<Beacon> decomissionBeacon(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Retrieve diagnostics for a beacon
     */
    @GET("/{beaconName}/diagnostics")
    Observable<Diagnostics> beaconDiagnostics(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Create an attachment
     */
    @POST("/{beaconName}/attachments")
    Observable<Attachment> createAttachment(@Path(value="beaconName", encode=false) String beaconName, @Body Attachment attachment);

    /**
     * Delete an attachment
     */
    @DELETE("/{attachmentName}")
    Observable<Void> deleteAttachment(@Path(value="attachmentName", encode=false) String attachmentName);

    /**
     * Delete a batch of attachments
     */
    @DELETE("/{beaconName}/attachments:batchDelete")
    Observable<Void> deleteBatchAttachments(@Path(value="beaconName", encode=false) String beaconName);

    /**
     * Retrieve attachments
     */
    @GET("/{beaconName}/attachments")
    Observable<AttachmentResponse> getAttachments(@Path(value="beaconName", encode=false) String beaconName, @Query("namespacedType") String namespacedType);

    /**
     * Retrieve namespaces
     */
    @GET("/namespaces")
    Observable<NamespacesResponse> getNamespaces();

    class BeaconsResponse {
        public List<Beacon> beacons;
    }

    class NamespacesResponse {
        public List<Namespace> namespaces;
    }

    class AttachmentResponse {
        public List<Attachment> attachments;
    }

}
