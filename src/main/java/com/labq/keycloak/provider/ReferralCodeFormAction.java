package com.labq.keycloak.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReferralCodeFormAction implements FormAction {
    private static final Logger logger = Logger.getLogger(ReferralCodeFormAction.class);
    private static final String REFERRAL_CODE_PARAM = "referralCode";
    private static final String REFERRAL_CODE_REQUIRED_PARAM = "referralCodeRequired";

    @Override
    public void buildPage(FormContext formContext, LoginFormsProvider loginFormsProvider) {
        AuthenticatorConfigModel config = formContext.getAuthenticatorConfig();

        loginFormsProvider.setAttribute(REFERRAL_CODE_PARAM, "");

        if (config != null) {
            loginFormsProvider.setAttribute(REFERRAL_CODE_REQUIRED_PARAM, Boolean.parseBoolean(config.getConfig().get(ReferralCodeFormActionFactory.CONFIG_REQUIRED)));
        } else {
            logger.warn("Null config!");
            loginFormsProvider.setAttribute(REFERRAL_CODE_REQUIRED_PARAM, false);
        }
    }

    @Override
    public void validate(ValidationContext validationContext) {
        MultivaluedMap<String, String> formData = validationContext.getHttpRequest().getDecodedFormParameters();
        String referralCode = formData.getFirst(REFERRAL_CODE_PARAM);

        if (referralCode != null && !referralCode.trim().isEmpty()) {
            logger.info("User registered using referral code");

            RealmModel realm = validationContext.getRealm();
            KeycloakSession session = validationContext.getSession();

            UserModel referringUser = session.users()
                    .searchForUserByUserAttributeStream(realm, REFERRAL_CODE_PARAM, referralCode)
                    .findFirst().orElse(null);

            if (referringUser == null) {
                validationContext.error("Invalid referral code");
                validationContext.validationError(formData, List.of(new FormMessage(REFERRAL_CODE_PARAM, "Invalid referral code")));
                return;
            }

            AuthenticationSessionModel authenticationSession = validationContext.getAuthenticationSession();
            authenticationSession.setClientNote("referralCode", referralCode);
            authenticationSession.setClientNote("referringUserId", referringUser.getId());
        } else {
            logger.info("User registered without referral code");
        }

        validationContext.success();
    }

    @Override
    public void success(FormContext formContext) {
        String referralCode = formContext.getAuthenticationSession().getClientNote("referralCode");
        String referringUserId = formContext.getAuthenticationSession().getClientNote("referringUserId");

        UserModel user = formContext.getUser();
        if (user != null) {
            user.setSingleAttribute("referredBy", referralCode);
        }

        RealmModel realm = formContext.getRealm();
        KeycloakSession session = formContext.getSession();

        EventBuilder event = new EventBuilder(realm, session, session.getContext().getConnection());
        event.event(EventType.REGISTER)
                .user(user)
                .detail("used_referral_code", referralCode)
                .detail("referring_user_id", referringUserId)
                .success();

        AuthenticatorConfigModel config = formContext.getAuthenticatorConfig();
        if (config == null) {
            logger.info("Config for referral webhook is missing, skipping...");
            return;
        }

        if (user == null) {
            logger.warn("Registered user is null, make sure to place the referral code verification in the right place");
            return;
        }

        String webhookUrl = config.getConfig().get(ReferralCodeFormActionFactory.CONFIG_WEBHOOK_URL);
        String webhookAuthenticationHeader = config.getConfig().get(ReferralCodeFormActionFactory.CONFIG_WEBHOOK_AUTHENTICATION_HEADER);
        String webhookAuthenticationValue = config.getConfig().get(ReferralCodeFormActionFactory.CONFIG_WEBHOOK_AUTHENTICATION_VALUE);

        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            logger.warn("Webhook url is not configured yet, skipping...");
            return;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(webhookUrl.trim());

            // set headers
            post.setHeader("Content-Type", "application/json");
            if (webhookAuthenticationHeader != null && !webhookAuthenticationHeader.trim().isEmpty()) {
                post.setHeader(webhookAuthenticationHeader.trim(), webhookAuthenticationValue);
            }

            // prepare JSON body
            Map<String, String> entityInput = new HashMap<>();
            entityInput.put("registeredUserId", user.getId());
            entityInput.put("referringUserId", referringUserId);
            entityInput.put("usedReferralCode", referralCode);

            ObjectMapper mapper = new ObjectMapper();
            String entityJson = mapper.writeValueAsString(entityInput);
            post.setEntity(new StringEntity(entityJson, "UTF-8"));

            logger.info(String.format("Sending webhook to %s", post.toString()));
            logger.info(entityJson);

            // execute request and read response
            try (CloseableHttpResponse response = client.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = "";

                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                }
                logger.info("Webhook response status: " + statusCode);
                logger.info("Webhook response body: " + responseBody);
            }

            logger.info("Webhook sent successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
