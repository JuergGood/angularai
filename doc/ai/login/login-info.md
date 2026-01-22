### Plan: Enhance Login Data Collection and Action Logging

#### 1. Objective
Collect additional context for each successful login attempt to improve security monitoring and user analytics. This includes requester IP address, geographic location, and device/browser information.

#### 2. Data to Collect
- **IP Address**: The origin IP of the request.
- **Location**: Country, City, and Region (derived from IP).
- **User-Agent**: Information about the browser, operating system, and device type.
- **Login Method**: Currently "Basic Auth", but useful if more methods are added.

#### 3. Implementation Steps

##### Phase A: Backend Infrastructure
1.  **Enhance `ActionLog` Entity**:
    - Add explicit columns for `ip_address`, `country`, `city`, `latitude`, `longitude`, and `user_agent` to `ActionLog` to allow better filtering and reporting.
2.  **IP Location Service (Cloud API)**:
    - Since self-hosting is excluded, a Cloud-based REST API will be used.
    - **Recommended Providers**:
        - **ipstack.com**: Very reliable, free tier available (100 requests/month), requires API key.
        - **ipapi.co**: Simple to use, free tier (30,000 requests/month, 1,000/day), HTTPS requires paid plan.
        - **abstractapi.com**: Excellent documentation, good free tier.
    - **Implementation Details**:
        - Use Spring's `RestTemplate` or `WebClient` to call the external API.
        - **Security**: Store the API key in `application.properties` or environment variables (not in code).
        - **Fallback**: Implement graceful failure. If the service is down, log the IP but leave location fields empty.
        - **Caching**: Cache results for the same IP (e.g., using `@Cacheable`) to minimize API calls and improve performance.
3.  **User-Agent Parsing**:
    - Add a library like `uap-java` (User Agent Picker) to reliably parse the `User-Agent` header into human-readable browser/OS names.

##### Phase B: Controller Integration
1.  **Modify `AuthController.login`**:
    - Inject `jakarta.servlet.http.HttpServletRequest` into the `login` method.
    - Extract the IP address (handling `X-Forwarded-For` if behind a proxy like Nginx).
    - Extract the `User-Agent` header.
2.  **Async Logging (Optional but Recommended)**:
    - GeoIP lookups can be slow. Move the lookup and logging process to an `@Async` method in `ActionLogService` to avoid delaying the user's login response.

##### Phase C: Data Protection & Privacy
1.  **IP Anonymization**: Consider masking the last octet of the IP (e.g., `192.168.1.0`) if strictly adhering to some privacy regulations.
2.  **Consent**: Ensure the privacy policy reflects the collection of this metadata.

#### 4. Proposed Code Snippet (Concept)

```java
// AuthController.java
@PostMapping("/login")
public ResponseEntity<UserDTO> login(Authentication authentication, HttpServletRequest request) {
    // ... authentication logic ...
    
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null) ip = request.getRemoteAddr();
    String ua = request.getHeader("User-Agent");
    
    // Pass metadata to service - this should be @Async to avoid blocking
    actionLogService.logLogin(user.getLogin(), ip, ua);
    
    return ResponseEntity.ok(UserDTO.fromEntity(user));
}

// ActionLogService.java
@Async
public void logLogin(String login, String ip, String userAgent) {
    // 1. Call Cloud API (e.g., ipapi.co)
    GeoLocation location = locationService.lookup(ip);
    
    // 2. Parse User-Agent
    Client client = uaParser.parse(userAgent);
    String uaDetails = String.format("%s on %s", client.userAgent.family, client.os.family);
    
    // 3. Save to ActionLog with explicit columns
    ActionLog log = new ActionLog();
    log.setLogin(login);
    log.setAction("USER_LOGIN");
    log.setIpAddress(ip);
    log.setCountry(location.getCountry());
    log.setCity(location.getCity());
    log.setUserAgent(uaDetails);
    log.setTimestamp(LocalDateTime.now());
    
    actionLogRepository.save(log);
}
```

#### 5. Verification
- Verify that `ActionLog` entries contain the correct metadata after login.
- Ensure the login flow remains performant (GeoIP lookup doesn't block response).
- Test behind the Docker Nginx proxy to confirm IP detection works correctly.
