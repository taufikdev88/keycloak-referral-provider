package com.labq.keycloak.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

@Slf4j
public class ReferralCodeProvider implements EventListenerProvider {
    private static final Logger logger = Logger.getLogger(ReferralCodeProvider.class);
    private final KeycloakSession session;
    private final String referralKey = "referralCode";

    public ReferralCodeProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() != EventType.LOGIN) {
            return;
        }

        try {
            RealmModel realm = session.realms().getRealm(event.getRealmId());
            UserModel user = session.users().getUserById(realm, event.getUserId());

            var referralCode = user.getFirstAttribute(referralKey);
            if (referralCode != null && !referralCode.isEmpty()) {
                return;
            }

            var code = GenerateRandomCode(realm);
            if (code == null || code.isEmpty()) {
                return;
            }

            user.setSingleAttribute(referralKey, code);
            logger.info(String.format("Successfully assigned referral code %s to user %s.", code, user.getId()));
        } catch (Exception e) {
            logger.error(String.format("Error when trying to modify user referral code attribute: %s.", e.getMessage()));
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }

    private String GenerateRandomCode(RealmModel realm) {
        try {
            String referralCode;
            boolean isUnique;

            do {
                referralCode = RandomStringUtils.randomAlphanumeric(8);
                isUnique = session.users().searchForUserByUserAttributeStream(realm, referralKey, referralCode)
                        .findAny()
                        .isEmpty();

            } while (!isUnique);

            return referralCode;
        } catch (Exception e) {
            logger.error(String.format("Error when generating random referral code %s.", e.getMessage()));
            return null;
        }
    }
}
