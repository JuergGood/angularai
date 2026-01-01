package ch.goodone.angularai.backend.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String login;
    private String password;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String address;

    public UserDTO() {}

    public UserDTO(Long id, String firstName, String lastName, String login, String email, LocalDate birthDate, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
