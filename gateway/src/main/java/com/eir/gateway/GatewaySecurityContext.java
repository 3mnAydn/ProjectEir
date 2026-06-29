package com.eir.gateway;

public class GatewaySecurityContext
{
    private String userId;
    private String token;
    private String requestIp;
    private boolean isAuthenticated;
    public String getUserId()
    {
        return userId;
    }
    public String getToken()
    {
        return token;
    }
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    public void setToken(String token) //Draft it will be deleted
    {
        this.token = token;
    }

    public boolean isAuthenticated()
    {
        return isAuthenticated;
    }
    public void setAuthenticated(boolean isAuthenticated)
    {
        this.isAuthenticated = isAuthenticated;
    }
    public String getRequestIp()
    {
        return requestIp;
    }
    public void setRequestIp(String requestIp)
    {
        this.requestIp = requestIp;
    }
}
