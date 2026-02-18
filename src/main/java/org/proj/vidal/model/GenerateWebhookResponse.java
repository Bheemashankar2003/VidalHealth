package org.proj.vidal.model;

public class GenerateWebhookResponse {

    private String webHook;
    private String accessToken;

    public String getWebHook(){
        return  webHook;
    }

    public void setWebhook(String webHook){
        this.webHook = webHook;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }
}
