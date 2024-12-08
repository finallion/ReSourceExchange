package com.resexchange.app.configuration;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaypalConfig {

    @Value("${paypal.client.app}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public OAuthTokenCredential authTokenCredential(){
        HashMap<String, String> sdkConfig = new HashMap<>();
        sdkConfig.put("mode", mode);
        return new OAuthTokenCredential(clientId, clientSecret, sdkConfig);
    }

    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        APIContext apiContext = new APIContext(authTokenCredential().getAccessToken());
        apiContext.setConfigurationMap(getPayPalConfig());
        return apiContext;
    }

    private HashMap<String, String> getPayPalConfig(){
        HashMap<String, String> config = new HashMap<>();
        config.put("mode", mode);
        config.put("connectionTimeout", "30000");
        config.put("requestRetries", "1");
        return config;
    }

}
