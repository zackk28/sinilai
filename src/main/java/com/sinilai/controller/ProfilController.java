package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfilController {
    private static final Logger LOGGER = Logger.getLogger(ProfilController.class.getName());

    @FXML
    private Label namaMahasiswaLabel;
    @FXML
    private Label namaProfilLabel;
    @FXML
    private Label nimLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label jurusanLabel;
    @FXML
    private Label prodiLabel;
    @FXML
    private TableView<Map.Entry<String, String>> dataTable;
    @FXML
    private TableColumn<Map.Entry<String, String>, String> fieldColumn;
    @FXML
    private TableColumn<Map.Entry<String, String>, String> valueColumn;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button homeButton;
    @FXML
    private Button khsButton;
    @FXML
    private Button settingButton;
    @FXML
    private Button ubahDataButton;

    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;
    private volatile boolean isInitialized = false;

    /**
     * Initialize method yang dipanggil setelah FXML dimuat
     */
    @FXML
    private void initialize() {
        try {
            LOGGER.info("Initializing ProfilController...");
            setupTableColumns();
            setupNavigationButtons();
            setupImageView();
            isInitialized = true;
            LOGGER.info("ProfilController initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            showAlert("Error", "Gagal menginisialisasi controller: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Setup kolom tabel untuk menampilkan data mahasiswa
     */
    private void setupTableColumns() {
        try {
            if (fieldColumn == null || valueColumn == null || dataTable == null) {
                throw new IllegalStateException("Table components not properly injected");
            }

            fieldColumn.setCellValueFactory(
                    param -> new javafx.beans.property.SimpleStringProperty(
                            param.getValue() != null ? param.getValue().getKey() : ""));
            valueColumn.setCellValueFactory(
                    param -> new javafx.beans.property.SimpleStringProperty(
                            param.getValue() != null ? param.getValue().getValue() : ""));

            // Set column widths
            fieldColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.3));
            valueColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.7));

            // Prevent column reordering
            fieldColumn.setReorderable(false);
            valueColumn.setReorderable(false);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to setup table columns", e);
        }
    }

    /**
     * Setup navigation buttons
     */
    private void setupNavigationButtons() {
        try {
            if (homeButton != null) {
                homeButton.setOnAction(this::handleHomeNavigation);
            }
            if (khsButton != null) {
                khsButton.setOnAction(this::handleKhsNavigation);
            }
            if (settingButton != null) {
                settingButton.setOnAction(this::handleSettingNavigation);
            }
            if (ubahDataButton != null) {
                ubahDataButton.setOnAction(this::handleUbahData);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting up navigation buttons", e);
        }
    }

    /**
     * Setup image view properties
     */
    private void setupImageView() {
        try {
            if (profileImageView != null) {
                profileImageView.setPreserveRatio(true);
                profileImageView.setSmooth(true);
                profileImageView.setCache(true);

                // Set default image if available
                try {
                    Image defaultImage = new Image(
                            getClass().getResourceAsStream("/com/sinilai/images/default-avatar.png"));
                    if (!defaultImage.isError()) {
                        profileImageView.setImage(defaultImage);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not load default avatar image", e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error setting up image view", e);
        }
    }

    /**
     * Set data mahasiswa dan user dengan improved error handling
     */
    public void setMahasiswa(MahasiswaModel mahasiswa, UserModel user) {
        LOGGER.info("Setting mahasiswa data...");

        try {
            // Validate input parameters
            if (mahasiswa == null) {
                throw new IllegalArgumentException("Data mahasiswa tidak boleh null");
            }
            if (user == null) {
                throw new IllegalArgumentException("Data user tidak boleh null");
            }

            // Wait for controller to be initialized if needed
            if (!isInitialized) {
                LOGGER.warning("Controller not yet initialized, waiting...");
                Platform.runLater(() -> setMahasiswa(mahasiswa, user));
                return;
            }

            // Validate required fields
            validateMahasiswaData(mahasiswa);
            validateUserData(user);

            this.currentMahasiswa = mahasiswa;
            this.currentUser = user;

            // Refresh data on JavaFX Application Thread
            if (Platform.isFxApplicationThread()) {
                refreshData();
            } else {
                Platform.runLater(this::refreshData);
            }

            LOGGER.info("Mahasiswa data set successfully for NIM: " + mahasiswa.getNim());

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Invalid data provided", e);
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error setting mahasiswa data", e);
            showAlert("Error", "Gagal memuat data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Validate mahasiswa data
     */
    private void validateMahasiswaData(MahasiswaModel mahasiswa) {
        if (mahasiswa.getNim() == null || mahasiswa.getNim().trim().isEmpty()) {
            throw new IllegalArgumentException("NIM mahasiswa tidak boleh kosong");
        }
        // Add more validation as needed
    }

    /**
     * Validate user data
     */
    private void validateUserData(UserModel user) {
        if (user.getNama() == null || user.getNama().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama user tidak boleh kosong");
        }
        // Add more validation as needed
    }

    /**
     * Refresh tampilan data dengan improved error handling
     */
    public void refreshData() {
        if (!isInitialized) {
            LOGGER.warning("Cannot refresh data - controller not initialized");
            return;
        }

        if (currentMahasiswa == null || currentUser == null) {
            LOGGER.warning("Cannot refresh data - missing mahasiswa or user data");
            return;
        }

        try {
            LOGGER.info("Refreshing data display...");

            // Update components safely
            updateLabels();
            updateTableData();

            LOGGER.info("Data display refreshed successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing data display", e);
            showAlert("Error", "Gagal memuat data profil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Update labels dengan data user dan mahasiswa
     */
    private void updateLabels() {
        try {
            if (namaMahasiswaLabel != null) {
                namaMahasiswaLabel.setText(getValueOrDefault(currentUser.getNama(), "Nama tidak tersedia"));
            }
            if (namaProfilLabel != null) {
                namaProfilLabel.setText(getValueOrDefault(currentUser.getNama(), "Nama tidak tersedia"));
            }
            if (nimLabel != null) {
                nimLabel.setText("NIM: " + getValueOrDefault(currentMahasiswa.getNim(), "-"));
            }
            if (emailLabel != null) {
                emailLabel.setText("Email: " + getValueOrDefault(currentUser.getEmail(), "-"));
            }
            if (jurusanLabel != null) {
                jurusanLabel.setText("Jurusan: " + getValueOrDefault(currentMahasiswa.getJurusan(), "-"));
            }
            if (prodiLabel != null) {
                prodiLabel.setText("Program Studi: " + getValueOrDefault(currentMahasiswa.getProdi(), "-"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating labels", e);
        }
    }

    /**
     * Update data tabel dengan improved error handling
     */
    private void updateTableData() {
        try {
            if (dataTable == null) {
                LOGGER.warning("DataTable is null, cannot update table data");
                return;
            }

            Map<String, String> dataMap = createDataMap();
            ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(dataMap.entrySet());
            dataTable.setItems(items);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating table data", e);
            throw new RuntimeException("Failed to update table data", e);
        }
    }

    /**
     * Membuat map data mahasiswa dengan null safety
     */
    private Map<String, String> createDataMap() {
        Map<String, String> map = new LinkedHashMap<>();

        try {
            map.put("NIM", getValueOrDefault(currentMahasiswa.getNim(), "-"));
            map.put("Jurusan", getValueOrDefault(currentMahasiswa.getJurusan(), "-"));
            map.put("Program Studi", getValueOrDefault(currentMahasiswa.getProdi(), "-"));
            map.put("Jalur Seleksi", getValueOrDefault(currentMahasiswa.getJalurSeleksi(), "-"));
            map.put("Asal Sekolah", getValueOrDefault(currentMahasiswa.getAsalSekolah(), "-"));
            map.put("Semester", String.valueOf(currentMahasiswa.getSemester()));

            // Handle date safely
            String ttlString = "-";
            try {
                if (currentMahasiswa.getTtl() != null) {
                    ttlString = currentMahasiswa.getTtl().toString();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error formatting TTL", e);
            }
            map.put("Tanggal Lahir", ttlString);

            map.put("Agama", getValueOrDefault(currentMahasiswa.getAgama(), "-"));
            map.put("Jenis Kelamin", getValueOrDefault(currentMahasiswa.getJkDisplay(), "-"));
            map.put("Alamat", getValueOrDefault(currentMahasiswa.getAlamat(), "-"));
            map.put("Kota", getValueOrDefault(currentMahasiswa.getKota(), "-"));
            map.put("Provinsi", getValueOrDefault(currentMahasiswa.getProv(), "-"));
            map.put("No Telepon", getValueOrDefault(currentMahasiswa.getNoTelp(), "-"));
            map.put("Pendidikan Akhir", getValueOrDefault(currentMahasiswa.getPendidikanAkhir(), "-"));
            map.put("Status Menikah", getValueOrDefault(currentMahasiswa.getStatusMenikah(), "-"));
            map.put("Tempat Tinggal", getValueOrDefault(currentMahasiswa.getTempatTinggal(), "-"));
            map.put("Sumber Uang", getValueOrDefault(currentMahasiswa.getSumberUang(), "-"));
            map.put("NIK", getValueOrDefault(currentMahasiswa.getNik(), "-"));
            map.put("No KK", getValueOrDefault(currentMahasiswa.getNoKk(), "-"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating data map", e);
            // Return minimal data map in case of error
            map.clear();
            map.put("Error", "Gagal memuat data");
        }

        return map;
    }

    /**
     * Helper method untuk menangani nilai null dengan improved safety
     */
    private String getValueOrDefault(String value, String defaultValue) {
        try {
            return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing value", e);
            return defaultValue;
        }
    }

    /**
     * Handle upload foto profil
     */
    @FXML
    private void handleUploadPhoto(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Foto Profil");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

            // Set initial directory to user's pictures folder
            String userHome = System.getProperty("user.home");
            File picturesDir = new File(userHome, "Pictures");
            if (picturesDir.exists()) {
                fileChooser.setInitialDirectory(picturesDir);
            }

            File file = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
            if (file != null) {
                // Validasi ukuran file (maksimal 5MB)
                if (file.length() > 5 * 1024 * 1024) {
                    showAlert("Error", "Ukuran file terlalu besar. Maksimal 5MB", Alert.AlertType.WARNING);
                    return;
                }

                Image image = new Image(file.toURI().toString());
                if (image.isError()) {
                    showAlert("Error", "File gambar tidak valid atau rusak", Alert.AlertType.ERROR);
                    return;
                }

                profileImageView.setImage(image);
                showAlert("Success", "Foto profil berhasil diubah", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading photo", e);
            showAlert("Error", "Gagal mengunggah foto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle ubah data
     */
    @FXML
    private void handleUbahData(ActionEvent event) {
        // TODO: Implement edit data functionality
        showAlert("Info", "Fitur ubah data akan segera tersedia", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle logout dengan konfirmasi
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Optional<ButtonType> result = showConfirmation("Konfirmasi Logout",
                    "Apakah Anda yakin ingin keluar dari aplikasi?");

            if (result.isPresent() && result.get() == ButtonType.OK) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) namaProfilLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("SINILAI - Login");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            showAlert("Error", "Gagal membuka halaman login: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during logout", e);
            showAlert("Error", "Terjadi kesalahan tidak terduga: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Navigation handlers
     */
    @FXML
    private void handleHomeNavigation(ActionEvent event) {
        // TODO: Implement navigation to home
        LOGGER.info("Navigate to Home requested");
    }

    @FXML
    private void handleKhsNavigation(ActionEvent event) {
        // TODO: Implement navigation to KHS
        LOGGER.info("Navigate to KHS requested");
    }

    @FXML
    private void handleSettingNavigation(ActionEvent event) {
        // TODO: Implement navigation to Settings
        LOGGER.info("Navigate to Settings requested");
    }

    /**
     * Helper method untuk menampilkan alert dengan thread safety
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        try {
            if (Platform.isFxApplicationThread()) {
                showAlertInternal(title, message, type);
            } else {
                Platform.runLater(() -> showAlertInternal(title, message, type));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing alert", e);
        }
    }

    /**
     * Internal method to show alert
     */
    private void showAlertInternal(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method untuk menampilkan konfirmasi
     */
    private Optional<ButtonType> showConfirmation(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            return alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing confirmation", e);
            return Optional.empty();
        }
    }

    /**
     * Get current mahasiswa data
     */
    public MahasiswaModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }

    /**
     * Get current user data
     */
    public UserModel getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if controller is properly initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}