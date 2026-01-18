package ch.goodone.angularai.backend.dto;

public class SystemInfoDTO {
    private String backendVersion;
    private String frontendVersion;
    private String mode;

    public SystemInfoDTO() {}

    public SystemInfoDTO(String backendVersion, String frontendVersion, String mode) {
        this.backendVersion = backendVersion;
        this.frontendVersion = frontendVersion;
        this.mode = mode;
    }

    public String getBackendVersion() {
        return backendVersion;
    }

    public void setBackendVersion(String backendVersion) {
        this.backendVersion = backendVersion;
    }

    public String getFrontendVersion() {
        return frontendVersion;
    }

    public void setFrontendVersion(String frontendVersion) {
        this.frontendVersion = frontendVersion;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
