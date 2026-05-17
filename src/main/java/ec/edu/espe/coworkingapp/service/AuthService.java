package ec.edu.espe.coworkingapp.service;

public interface AuthService {
    String login(String email, String password);
    void register(String email, String password);
}