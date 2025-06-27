package com.sinilai.controller;

import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
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

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpLink;

    public void initialize() {
        loginButton.setOnAction(this::handleLogin);
        if (signUpLink != null) {
            signUpLink.setOnAction(this::handleSignUp);
        }
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Email dan Password harus diisi!");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        try {
            UserModel user = loginUser(email, password);
            if (user != null) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Login berhasil! Selamat datang, " + user.getNama());
                openDashboard(user);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Email atau Password salah!");
                passwordField.clear();
                emailField.requestFocus();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Login");
        }
    }

    private UserModel loginUser(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserModel user = new UserModel();
                user.setId(rs.getInt("id"));
                user.setNama(rs.getString("nama"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));

                // Perhatikan: role dibersihkan dari spasi dan diubah ke huruf kecil
                String role = rs.getString("role");
                if (role != null) {
                    user.setRole(role.trim().toLowerCase());
                } else {
                    user.setRole("");
                }

                System.out.println("DEBUG: Login sebagai role: [" + user.getRole() + "]");

                return user;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }

        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error: " + e.getMessage(), e);
        }
    }

    private void openDashboard(UserModel user) {
        try {
<<<<<<< Updated upstream
=======
<<<<<<< Updated upstream
            MahasiswaModel mahasiswa = findMahasiswaByUserId(user.getId());

            // Set session
            setSession(user, mahasiswa);

            // Load halaman dashboard
>>>>>>> Stashed changes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/DashboardView.fxml"));
            Scene scene = new Scene(loader.load());

            DashboardController controller = loader.getController();
            controller.setUser(user);

<<<<<<< Updated upstream
=======
            // Tampilkan scene
=======
            FXMLLoader loader;
            Scene scene;
>>>>>>> Stashed changes
>>>>>>> Stashed changes
            Stage stage = (Stage) loginButton.getScene().getWindow();

            String role = user.getRole(); // Sudah dibersihkan sebelumnya

            if ("mahasiswa".equals(role)) {
                loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/DashboardView.fxml"));
                scene = new Scene(loader.load());

                DashboardController controller = loader.getController();
                controller.setUser(user);

                stage.setTitle("Dashboard Mahasiswa - SINILAI");

            } else if ("dosen".equals(role)) {
                loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/DosenDashboard.fxml"));
                scene = new Scene(loader.load());

                DashboardDosenController controller = loader.getController();
                controller.setUser(user);

                stage.setTitle("Dashboard Dosen - SINILAI");

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Role tidak dikenali: " + role);
                return;
            }

            stage.setScene(scene);
            stage.setMaximized(true);
<<<<<<< Updated upstream
=======
<<<<<<< Updated upstream
            stage.show();
=======
>>>>>>> Stashed changes

>>>>>>> Stashed changes
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/RegisterView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Register - SINILAI");
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman register: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
