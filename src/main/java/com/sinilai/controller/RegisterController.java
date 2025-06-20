package com.sinilai.controller;

import com.sinilai.utils.Koneksi;
import com.sinilai.model.UserModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void initialize() {
        registerButton.setOnAction(this::handleRegister);
        loginLink.setOnAction(this::handleLoginLink);
    }

    private void handleRegister(ActionEvent event) {
        String nama = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!validateInput(nama, email, password))
            return;

        if (isEmailExists(email)) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Email sudah terdaftar!");
            return;
        }

        UserModel user = new UserModel();
        user.setNama(nama);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("mahasiswa"); // default role

        if (registerUser(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Registrasi berhasil! Silakan login.");
            clearFields();
            try {
                openLoginPage();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman login: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Registrasi gagal! Silakan coba lagi.");
        }
    }

    private boolean registerUser(UserModel user) {
        String sql = "INSERT INTO user (nama, email, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getNama());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashPassword(user.getPassword()));
            stmt.setString(4, user.getRole());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan data: " + e.getMessage());
            return false;
        }
    }

    private boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal cek email: " + e.getMessage());
            return true;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // fallback (tidak direkomendasikan)
        }
    }

    private boolean validateInput(String nama, String email, String password) {
        if (nama.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama harus diisi!");
            usernameField.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Email harus diisi!");
            emailField.requestFocus();
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Format email tidak valid!");
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Password minimal 6 karakter!");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
    }

    private void openLoginPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setTitle("Login - SINILAI");
        stage.setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLoginLink(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setTitle("Login - SINILAI");
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman login: " + e.getMessage());
        }
    }
}
