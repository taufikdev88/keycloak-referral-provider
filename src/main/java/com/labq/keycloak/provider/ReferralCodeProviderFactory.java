package com.labq.keycloak.provider;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ReferralCodeProviderFactory implements EventListenerProviderFactory {
    private static final Logger logger = Logger.getLogger(ReferralCodeProviderFactory.class);

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new ReferralCodeProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("Initialize ReferralCodeProviderFactory");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {
        logger.info("Closing ReferralCodeProviderFactory");
    }

    @Override
    public String getId() {
        return "keycloak-referral-provider";
    }
}
