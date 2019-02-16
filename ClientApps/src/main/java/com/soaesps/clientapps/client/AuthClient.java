package com.soaesps.clientapps.client;

import com.soaesps.core.DataModels.security.Login;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthClient {
    @POST("/token/login")
    Call<?> issueToken(@Body Login credentials);

    @GET("/token/refresh")
    Call<?> refreshToken(@Body Login credentials);

    @GET("/token/revoke")
    Call<?> revokeToken(@Body Login credentials);
}