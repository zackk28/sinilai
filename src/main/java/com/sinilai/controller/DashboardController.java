package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class DashboardController {

    @FXML
    private Label namaMahasiswaLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label nimLabel;

    @FXML
    private Label namaDataLabel;

    @FXML
    private Label emailDataLabel;

    @FXML
    private Label roleDataLabel;

    @FXML
    private Label idDataLabel;

    @FXML
    private Label ipLabel;

    @FXML
    private Label semesterLabel;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private Button homeButton;

    @FXML
    private Button profilButton;

    @FXML
    private Button khsButton;

    @FXML
    private Button settingButton;

    @FXML
    private Button logoutButton;

    private UserModel currentUser;
    private MahasiswaModel currentMahasiswa;

    public void initialize() {
        homeButton.setOnAction(e -> handleHome());
        profilButton.setOnAction(e -> handleProfil());
        khsButton.setOnAction(e -> handleKHS());
        settingButton.setOnAction(e -> handleSetting());

        if (ipLabel != null)
            ipLabel.setText("0.00");
        if (semesterLabel != null)
            semesterLabel.setText("I");
    }

    // Digunakan dari ProfilController
    public void setMahasiswa(MahasiswaModel mahasiswa, UserModel user) {
        setMahasiswaAndUser(mahasiswa, user);
    }

    // Digunakan dari LoginController
    public void setMahasiswaAndUser(MahasiswaModel mahasiswa, UserModel user) {
        this.currentMahasiswa = mahasiswa;
        this.currentUser = user;
        updateDashboardInfo();
    }

    // Digunakan jika hanya user (tanpa mahasiswa)
    public void setUser(UserModel user) {
        this.currentUser = user;
        updateDashboardInfo();
    }

    private void updateDashboardInfo() {
        if (currentUser != null) {
            if (namaMahasiswaLabel != null)
                namaMahasiswaLabel.setText(currentUser.getNama());
            if (welcomeLabel != null)
                welcomeLabel.setText("Hello " + currentUser.getNama());
            if (nimLabel != null)
                nimLabel.setText("ID: " + currentUser.getId());
            if (namaDataLabel != null)
                namaDataLabel.setText(currentUser.getNama());
            if (emailDataLabel != null)
                emailDataLabel.setText(currentUser.getEmail());
            if (roleDataLabel != null)
                roleDataLabel.setText(currentUser.getRole());
            if (idDataLabel != null)
                idDataLabel.setText(String.valueOf(currentUser.getId()));
        }
    }

    @FXML
    private void handleHome() {
        System.out.println("Home button clicked");
        updateActiveButton(homeButton);
    }

    @FXML
    private void handleProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/ProfilView.fxml"));
            Scene scene = new Scene(loader.load());

            ProfilController controller = loader.getController();
            controller.setMahasiswa(currentMahasiswa, currentUser);

            Stage stage = (Stage) profilButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Profil Mahasiswa");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKHS() {
        System.out.println("KHS button clicked");
        updateActiveButton(khsButton);
        // TODO: Load KhsView.fxml
    }

    @FXML
    private void handleSetting() {
        System.out.println("Setting button clicked");
        updateActiveButton(settingButton);
        // TODO: Load SettingView.fxml
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
                Scene scene = new Scene(loader.load());

                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setTitle("Login - SINILAI");
                stage.setScene(scene);
                stage.setMaximized(false);
                stage.setMinWidth(400);
                stage.setMinHeight(500);

                System.out.println("User logged out successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal logout: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateActiveButton(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent;";
        String activeStyle = "-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);";

        homeButton.setStyle(inactiveStyle);
        profilButton.setStyle(inactiveStyle);
        khsButton.setStyle(inactiveStyle);
        settingButton.setStyle(inactiveStyle);

        activeButton.setStyle(activeStyle);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public MahasiswaModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }

    public void updateIP(double ip) {
        if (ipLabel != null)
            ipLabel.setText(String.format("%.2f", ip));
    }

    public void updateSemester(String semester) {
        if (semesterLabel != null)
            semesterLabel.setText(semester);
    }
}
