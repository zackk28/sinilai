package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Session;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller untuk halaman dashboard utama aplikasi SINILAI
 * Menangani navigasi antar halaman dan menampilkan informasi user
 */
public class DashboardController {
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    // Navigation labels
    @FXML
    private Label namaMahasiswaLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label nimLabel;

    // Profile data labels
    @FXML
    private Label namaDataLabel;
    @FXML
    private Label emailDataLabel;
    @FXML
    private Label roleDataLabel;
    @FXML
    private Label idDataLabel;

    // Academic info labels
    @FXML
    private Label ipLabel;
    @FXML
    private Label semesterLabel;

    // UI Controls
    @FXML
    private ChoiceBox<String> choiceBox;

    // Navigation buttons
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

    // Data models
    private UserModel currentUser;
    private MahasiswaModel currentMahasiswa;

    // Constants
    private static final String DEFAULT_IP = "0.00";
    private static final String DEFAULT_SEMESTER = "I";
    private static final String INACTIVE_BUTTON_STYLE = "-fx-background-color: transparent;";
    private static final String ACTIVE_BUTTON_STYLE = "-fx-background-color: #FFFFFF; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);";

    /**
     * Initialize method dipanggil setelah FXML dimuat
     */
    @FXML
    private void initialize() {
        try {
            LOGGER.info("Initializing DashboardController...");

            setupEventHandlers();
            initializeDefaultValues();

            currentUser = Session.getUser();
            currentMahasiswa = Session.getMahasiswa();

            if (Session.getUser() != null) {
                updateDashboardInfo(); // langsung update
            } else {
                LOGGER.warning("Session kosong, redirect ke login...");
                redirectToLogin();
                return;
            }

            LOGGER.info("DashboardController initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during DashboardController initialization", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal menginisialisasi dashboard: " + e.getMessage());
        }
    }

    /**
     * Setup event handlers untuk buttons
     */
    private void setupEventHandlers() {
        if (homeButton != null) {
            homeButton.setOnAction(e -> handleHome());
        }
        if (profilButton != null) {
            profilButton.setOnAction(e -> handleProfil());
        }
        if (khsButton != null) {
            khsButton.setOnAction(e -> handleKHS());
        }
        if (settingButton != null) {
            settingButton.setOnAction(e -> handleSetting());
        }
        if (logoutButton != null) {
            logoutButton.setOnAction(this::handleLogout);
        }
    }

    /**
     * Initialize default values
     */
    private void initializeDefaultValues() {
        updateLabel(ipLabel, DEFAULT_IP);
        updateLabel(semesterLabel, DEFAULT_SEMESTER);
    }

    /**
     * Set data mahasiswa dan user (dari ProfilController)
     */
    public void setMahasiswa(MahasiswaModel mahasiswa, UserModel user) {
        setMahasiswaAndUser(mahasiswa, user);
    }

    /**
     * Set data mahasiswa dan user (dari LoginController)
     */
    public void setMahasiswaAndUser(MahasiswaModel mahasiswa, UserModel user) {
        try {
            validateInputData(mahasiswa, user);

            this.currentMahasiswa = mahasiswa;
            this.currentUser = user;

            Platform.runLater(this::updateDashboardInfo);

            LOGGER.info("Dashboard data set successfully for user: " +
                    (user != null ? user.getNama() : "unknown"));

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid data provided to dashboard", e);
            showAlert(Alert.AlertType.WARNING, "Warning", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting dashboard data", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal memuat data dashboard: " + e.getMessage());
        }
    }

    /**
     * Validate input data
     */
    private void validateInputData(MahasiswaModel mahasiswa, UserModel user) {
        if (user == null) {
            throw new IllegalArgumentException("Data user tidak boleh null");
        }
        if (user.getNama() == null || user.getNama().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama user tidak boleh kosong");
        }
        // Mahasiswa bisa null untuk beberapa kasus
    }

    /**
     * Update informasi dashboard
     */
    private void updateDashboardInfo() {
        if (currentUser == null) {
            currentUser = Session.getUser(); // ambil dari session kalau belum diset
        }

        if (currentUser == null) {
            LOGGER.warning("Cannot update dashboard info - user data is null");
            return;
        }

        try {
            String userName = currentUser.getNama();
            String userEmail = currentUser.getEmail();
            String userRole = currentUser.getRole();
            int userId = currentUser.getId();

            // Update labels dengan null safety
            updateLabel(namaMahasiswaLabel, userName);
            updateLabel(welcomeLabel, "Hello " + userName);
            updateLabel(nimLabel, "ID: " + userId);
            updateLabel(namaDataLabel, userName);
            updateLabel(emailDataLabel, userEmail);
            updateLabel(roleDataLabel, userRole != null ? userRole : "User");
            updateLabel(idDataLabel, String.valueOf(userId));

            LOGGER.info("Dashboard info updated successfully");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating dashboard info", e);
        }
    }

    /**
     * Helper method untuk update label dengan null safety
     */
    private void updateLabel(Label label, String text) {
        if (label != null && text != null) {
            label.setText(text);
        }
    }

    /**
     * Handle home button click
     */
    @FXML
    private void handleHome() {
        LOGGER.info("Home button clicked");
        updateActiveButton(homeButton);
    }

    /**
     * Handle profil button click - navigate to profile view
     */
    @FXML
    private void handleProfil() {
        try {
            LOGGER.info("Profil button clicked");

            UserModel user = Session.getUser();
            MahasiswaModel mahasiswa = Session.getMahasiswa();

            if (user == null) {
                showAlert(Alert.AlertType.WARNING, "Warning",
                        "Data user tidak tersedia. Silakan login ulang.");
                redirectToLogin();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/ProfilView.fxml"));
            Scene scene = new Scene(loader.load());

            ProfilController controller = loader.getController();

            if (mahasiswa != null) {
                controller.setMahasiswa(mahasiswa, user);
            } else {
                LOGGER.warning("Mahasiswa data not available for profile view");
                showAlert(Alert.AlertType.INFORMATION, "Info",
                        "Data mahasiswa belum lengkap. Beberapa informasi mungkin tidak tersedia.");
            }

            Stage stage = (Stage) profilButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Profil Mahasiswa - SINILAI");
            stage.setMaximized(true);
            stage.show();

            updateActiveButton(profilButton);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading profile view", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal membuka halaman profil: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in handleProfil", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Terjadi kesalahan tidak terduga: " + e.getMessage());
        }
    }

    /**
     * Handle KHS button click
     */
    @FXML
    private void handleKHS() {
        LOGGER.info("KHS button clicked");
        updateActiveButton(khsButton);

        // TODO: Implement KHS view loading
        showAlert(Alert.AlertType.INFORMATION, "Info",
                "Fitur KHS akan segera tersedia");
    }

    /**
     * Handle setting button click
     */
    @FXML
    private void handleSetting() {
        LOGGER.info("Setting button clicked");
        updateActiveButton(settingButton);

        // TODO: Implement Settings view loading
        showAlert(Alert.AlertType.INFORMATION, "Info",
                "Fitur pengaturan akan segera tersedia");
    }

    /**
     * Handle logout dengan konfirmasi
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin logout?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                performLogout();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during logout process", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Terjadi kesalahan saat logout: " + e.getMessage());
        }
    }

    /**
     * Perform actual logout operation
     */
    private void performLogout() {
        try {
            // Clear current data
            Session.setUser(null);
            Session.setMahasiswa(null);

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Login - SINILAI");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setMinWidth(400);
            stage.setMinHeight(500);

            LOGGER.info("User logged out successfully");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login view during logout", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal logout: " + e.getMessage());
        }
    }

    /**
     * Update visual state of navigation buttons
     */
    private void updateActiveButton(Button activeButton) {
        try {
            // Reset all buttons to inactive state
            Button[] buttons = { homeButton, profilButton, khsButton, settingButton };

            for (Button button : buttons) {
                if (button != null) {
                    button.setStyle(INACTIVE_BUTTON_STYLE);
                }
            }

            // Set active button style
            if (activeButton != null) {
                activeButton.setStyle(ACTIVE_BUTTON_STYLE);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating button styles", e);
        }
    }

    /**
     * Helper method untuk menampilkan alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        try {
            if (Platform.isFxApplicationThread()) {
                showAlertInternal(type, title, message);
            } else {
                Platform.runLater(() -> showAlertInternal(type, title, message));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing alert", e);
        }
    }

    /**
     * Internal method to show alert
     */
    private void showAlertInternal(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getter methods
    public UserModel getCurrentUser() {
        return currentUser;
    }

    public MahasiswaModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }

    /**
     * Update IP value dengan validasi
     */
    public void updateIP(double ip) {
        try {
            if (ip < 0.0 || ip > 4.0) {
                LOGGER.warning("Invalid IP value: " + ip);
                return;
            }
            updateLabel(ipLabel, String.format("%.2f", ip));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating IP", e);
        }
    }

    /**
     * Update semester dengan validasi
     */
    public void updateSemester(String semester) {
        try {
            if (semester == null || semester.trim().isEmpty()) {
                LOGGER.warning("Invalid semester value");
                return;
            }
            updateLabel(semesterLabel, semester.trim());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating semester", e);
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) namaMahasiswaLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - SINILAI");
            stage.show();
        } catch (IOException e) {
            LOGGER.severe("Gagal redirect ke login: " + e.getMessage());
        }
    }
}