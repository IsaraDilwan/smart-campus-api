package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 * 
 * The @ApplicationPath annotation sets the base URI for all JAX-RS resources.
 * Combined with the web.xml servlet mapping, all endpoints are served under "/api/v1".
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // JAX-RS automatically discovers resource classes and providers
    // via package scanning configured in web.xml.
}
