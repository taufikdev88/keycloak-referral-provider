# Referral Code Provider for Keycloak

This project provides a custom Keycloak event listener to automatically generate and assign a unique `referralCode` to new users during registration. The referral codes are stored as user attributes and can be queried using Keycloak's Admin REST API.

## Features

- Automatically generates a unique `referralCode` for every new user.
- Stores the `referralCode` in the user's attributes.
- Ensures uniqueness of `referralCode` by checking existing users.
- Includes proper error handling and logging.
- Add referral code to registration flow, and send webhook to specific url

## Installation

### Prerequisites
- **Keycloak**: Version 25+ (tested with the latest Keycloak distribution).
- **Java**: JDK 21 or higher.
- **Maven**: For building the project.

### Steps

1. **Clone the Repository**:
   ```bash
   git clone <repository_url>
   cd referral-code-provider
   ```

2. **Build the Project**:
   ```bash
   mvn clean package
   ```

3. **Deploy the Provider**:
    - Copy the generated `.jar` file from the `target` directory to the `providers/` directory of your Keycloak installation.
    - Example:
      ```bash
      cp target/keycloak-referral-provider-1.0.0.jar /path/to/keycloak/providers/
      ```

4. **Restart Keycloak**:
   ```bash
   bin/kc.sh start-dev
   ```

5. **Enable the Provider**:
    - Log in to the Keycloak admin console.
    - Navigate to **Events** > **Config**.
    - Under **Event Listeners**, add `keycloak-referral-provider`.

## Usage

### Registration
When a new user registers, the `referralCode` is automatically generated and assigned to the user's attributes.

### Query Users by Referral Code
Use the Keycloak Admin REST API to query users by their `referralCode`:

#### Example:
```bash
curl -X GET \
  "http://localhost:8080/admin/realms/{realm}/users?q=referralCode:{referralCode}" \
  -H "Authorization: Bearer {access_token}"
```

## Configuration

### Logging
The provider uses `org.jboss.logging.Logger` for logging:
- **INFO**: Successful operations.
- **WARN**: Non-critical issues.
- **ERROR**: Critical failures.

Logs can be viewed in Keycloak's console or log files.

## Development

### Project Structure
```plaintext
src/main/java/com/labq/keycloak/provider
    ├── ReferralCodeFormAction.java
    ├── ReferralCodeFormActionFactory.java
    ├── ReferralCodeProvider.java
    ├── ReferralCodeProviderFactory.java
src/main/resources
    ├── META-INF/services/org.keycloak.authentication.FormActionFactory
    ├── META-INF/services/org.keycloak.events.EventListenerProviderFactory
pom.xml
```

### Key Classes
- **ReferralCodeFormAction**: Handles referral code validation when registration submitted.
- **ReferralCodeFormActionFactory**: Creates the `ReferralCodeFormAction` instance.
- **ReferralCodeProvider**: Handles referral code generation and assignment.
- **ReferralCodeProviderFactory**: Creates the `ReferralCodeProvider` instance.

### Build and Test
- **Build**:
  ```bash
  mvn clean package
  ```
- **Test**:
  Deploy the JAR to Keycloak and test user registration and referral code generation.

## Troubleshooting

- **Duplicate Referral Codes**:
  Ensure the `generateUniqueReferralCode` method checks for uniqueness in the user attributes.

- **Provider Not Loading**:
    - Check if the JAR is placed in the correct `providers/` directory.
    - Verify the `META-INF/services` file contains the correct class name.

- **Logs Not Visible**:
  Ensure the logging configuration in Keycloak is set to include `INFO` and `ERROR` levels.

## License
This project is licensed under the [MIT License](LICENSE).

---

Feel free to open issues or submit pull requests for improvements!
