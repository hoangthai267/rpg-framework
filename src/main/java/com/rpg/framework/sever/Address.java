package com.rpg.framework.sever;

import java.net.InetSocketAddress;

import io.netty.handler.codec.http.HttpRequest;

public class Address {
	public static String getRemoteAddress (InetSocketAddress isa)
    {
        return isa.getAddress().getHostAddress();
    }
    
    public static String getRemoteIP (InetSocketAddress isa)
    {
        String fullAddress = getRemoteAddress(isa);
        // Address resolves to /x.x.x.x:zzzz we only want x.x.x.x
        if (fullAddress.startsWith("/"))
        {
            fullAddress = fullAddress.substring(1);
        }
        int i = fullAddress.indexOf(":");
        if (i != -1)
        {
            fullAddress = fullAddress.substring(0, i);
        }
        return fullAddress;
    }
    
    public static String getXForwardedFor (HttpRequest request)
    {        
        String fw = request.headers().get("X-Forwarded-For");
        if (fw != null)
        {
            int pos = fw.indexOf(',');
            return (pos > 0) ? fw.substring(0, pos) : fw;
        }
        return "";
    }
    
    public static InetSocketAddress getInetSocketAddress (String inetHost, int inetPort)
    {
        return (inetHost == null || inetHost.isEmpty() || inetHost.length() <= 1) 
                ? new InetSocketAddress(inetPort)
                : new InetSocketAddress(inetHost, inetPort);
    }
}
