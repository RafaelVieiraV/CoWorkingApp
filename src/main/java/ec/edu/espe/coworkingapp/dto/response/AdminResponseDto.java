package ec.edu.espe.coworkingapp.dto.response;

public class AdminResponseDto {
    private Long id;
    private String email;
    private String message;

    public AdminResponseDto() {}

    public AdminResponseDto(String message) {
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}