package com.labq.keycloak.provider;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class ReferralCodeFormActionFactory implements FormActionFactory {
    public static final String CONFIG_REQUIRED = "referralRequired";
    public static final String CONFIG_WEBHOOK_URL = "webhookUrl";
    public static final String CONFIG_WEBHOOK_AUTHENTICATION_HEADER = "webhookAuthenticationHeader";
    public static final String CONFIG_WEBHOOK_AUTHENTICATION_VALUE = "webhookAuthenticationValue";

    @Override
    public String getDisplayType() {
        return "Referral Code Validator";
    }

    @Override
    public String getReferenceCategory() {
        return "registration";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED,
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Validates an optional referral code during registration and stores who referred the user.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty required = new ProviderConfigProperty();
        required.setLabel("Require Referral Code");
        required.setHelpText("If enabled, users must provide a valid referral code during registration");
        required.setName(CONFIG_REQUIRED);
        required.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        required.setDefaultValue(false);

        ProviderConfigProperty webhookUrl = new ProviderConfigProperty();
        webhookUrl.setLabel("Webhook URL");
        webhookUrl.setHelpText("The URL to send the referral code to when registration is completed");
        webhookUrl.setName(CONFIG_WEBHOOK_URL);
        webhookUrl.setType(ProviderConfigProperty.STRING_TYPE);

        ProviderConfigProperty webhookAuthenticationHeader = new ProviderConfigProperty();
        webhookAuthenticationHeader.setLabel("Webhook Authentication Header");
        webhookAuthenticationHeader.setHelpText("Http Header for credential");
        webhookAuthenticationHeader.setName(CONFIG_WEBHOOK_AUTHENTICATION_HEADER);
        webhookAuthenticationHeader.setName(ProviderConfigProperty.STRING_TYPE);

        ProviderConfigProperty webhookAuthenticationValue = new ProviderConfigProperty();
        webhookAuthenticationValue.setLabel("Webhook Authentication Value");
        webhookAuthenticationValue.setHelpText("Value for the authentication header");
        webhookAuthenticationValue.setName(CONFIG_WEBHOOK_AUTHENTICATION_VALUE);
        webhookAuthenticationValue.setType(ProviderConfigProperty.STRING_TYPE);

        return List.of(required, webhookUrl, webhookAuthenticationHeader, webhookAuthenticationValue);
    }

    @Override
    public FormAction create(KeycloakSession keycloakSession) {
        return new ReferralCodeFormAction();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "referral-code-form-action";
    }
}
