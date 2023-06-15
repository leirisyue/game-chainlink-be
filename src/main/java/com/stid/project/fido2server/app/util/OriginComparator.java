package com.stid.project.fido2server.app.util;

import com.webauthn4j.data.client.Origin;

import java.util.Comparator;

public class OriginComparator {

    public static Comparator<Origin> getComparator() {
        return Comparator
                .comparing(OriginComparator::compareSubdomain)
                .thenComparing(OriginComparator::compareAlphabet)
                .thenComparing(OriginComparator::comparePort);
    }

    private static int compareSubdomain(Origin origin) {
        return origin.toString().split("\\.").length;
    }

    private static String compareAlphabet(Origin origin) {
        return origin.toString();
    }

    private static int comparePort(Origin origin) {
        return origin.getPort() != null ? origin.getPort() : -1;
    }
}
