package com.stid.project.fido2server.fido2.util;

import com.webauthn4j.data.client.Origin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class ServletUtil {

    private ServletUtil() {
    }

    /**
     * Returns {@link Origin} corresponding {@link HttpServletRequest} url
     *
     * @param request http servlet request
     * @return the {@link Origin}
     */
    public static Origin getOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        String url = origin != null ? origin : String.format("%s://%s:%s", request.getScheme(), request.getServerName(), request.getServerPort());
        return new Origin(url);
    }
}
