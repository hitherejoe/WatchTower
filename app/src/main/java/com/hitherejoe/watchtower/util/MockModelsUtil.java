package com.hitherejoe.watchtower.util;

import com.hitherejoe.watchtower.data.model.AdvertisedId;
import com.hitherejoe.watchtower.data.model.Attachment;
import com.hitherejoe.watchtower.data.model.Beacon;
import com.hitherejoe.watchtower.data.model.LatLng;
import com.hitherejoe.watchtower.data.model.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MockModelsUtil {

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    public static Beacon createMockUnregisteredBeacon() {
        Beacon beacon = new Beacon();
        beacon.placeId = "1234";
        beacon.status = Beacon.Status.ACTIVE;
        beacon.expectedStability = Beacon.Stability.MOBILE;
        beacon.description = "This is a description";
        beacon.latLng = new LatLng();
        beacon.latLng.latitude = 54.331;
        beacon.latLng.longitude = -12.435;
        beacon.advertisedId = new AdvertisedId();
        beacon.advertisedId.id = "IDIDIDIDID";
        beacon.advertisedId.type = AdvertisedId.Type.EDDYSTONE;
        return beacon;
    }

    public static Beacon createMockRegisteredBeacon() {
        Beacon beacon = createMockUnregisteredBeacon();
        beacon.beaconName = "beaconName/namenamename";
        return beacon;
    }

    public static Beacon createMockIncompleteBeacon() {
        Beacon beacon = new Beacon();
        beacon.beaconName = "beaconName/namenamename";
        beacon.advertisedId = new AdvertisedId();
        beacon.status = Beacon.Status.ACTIVE;
        beacon.advertisedId.id = "IDIDIDIDID";
        return beacon;
    }

    public static Attachment createMockAttachment() {
        Attachment attachment = new Attachment();
        attachment.namespacedType = "proximity-api/text";
        attachment.data = "attachmentData";
        return attachment;
    }

    public static Namespace createMockNamespace() {
        Namespace namespace = new Namespace();
        namespace.namespaceName = "proximity-api";
        namespace.servingVisibility = Namespace.Visibility.PUBLIC;
        return namespace;
    }

    public static List<Namespace> createMockListOfNamespaces(int num) {
        ArrayList<Namespace> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Namespace namespace = new Namespace();
            namespace.namespaceName =  "proximity-api/" + i;
            namespace.servingVisibility = Namespace.Visibility.PUBLIC;
            result.add(namespace);
        }
        return result;
    }

    public static List<Beacon> createMockListOfBeacons(int num) {
        ArrayList<Beacon> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Beacon beacon = new Beacon();
            beacon.beaconName = generateRandomString();
            AdvertisedId advertisedId = new AdvertisedId();
            advertisedId.type = AdvertisedId.Type.EDDYSTONE;
            advertisedId.id = generateRandomString();
            beacon.advertisedId = advertisedId;
            beacon.description = "Descroption " + i;
            beacon.status = Beacon.Status.ACTIVE;
            beacon.expectedStability = Beacon.Stability.MOBILE;
            beacon.placeId = generateRandomString();
            LatLng latLng = new LatLng();
            latLng.latitude = 1.00 + num;
            latLng.longitude = 1.00 - num;
            beacon.latLng = latLng;
            result.add(beacon);
        }
        return result;
    }

    public static List<Attachment> createMockListOfAttachments(String beaconName, int num) {
        ArrayList<Attachment> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Attachment attachment = new Attachment();
            attachment.attachmentName = beaconName + "attachments/" + i;
            attachment.data = "Data";
            attachment.namespacedType = "proximity-api/text";
            result.add(attachment);
        }
        return result;
    }

}