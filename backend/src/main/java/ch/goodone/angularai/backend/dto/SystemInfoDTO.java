package ch.goodone.angularai.backend.dto;

public class SystemInfoDTO {
    private String version;
    private String mode;

    public SystemInfoDTO() {}

    public SystemInfoDTO(String version, String mode) {
        this.version = version;
        this.mode = mode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
