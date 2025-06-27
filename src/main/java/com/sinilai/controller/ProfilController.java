package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Session;

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
import java.net.URL;
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
    @FXML
    private Button uploadPhotoButton;

    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;
    private boolean isInitialized = false;

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

            // Ambil dari session
            currentUser = Session.getUser();
            currentMahasiswa = Session.getMahasiswa();

            if (currentUser != null || currentMahasiswa != null) {
                refreshData();
            } else {
                LOGGER.warning("User atau Mahasiswa di session kosong");
            }

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
        if (fieldColumn == null || valueColumn == null || dataTable == null) {
            LOGGER.warning("Table components not properly injected");
            return;
        }

        // Setup cell value factories
        fieldColumn.setCellValueFactory(param -> {
            if (param.getValue() != null && param.getValue().getKey() != null) {
                return new javafx.beans.property.SimpleStringProperty(param.getValue().getKey());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        valueColumn.setCellValueFactory(param -> {
            if (param.getValue() != null && param.getValue().getValue() != null) {
                return new javafx.beans.property.SimpleStringProperty(param.getValue().getValue());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // Set column properties
        fieldColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.35));
        valueColumn.prefWidthProperty().bind(dataTable.widthProperty().multiply(0.65));

        fieldColumn.setReorderable(false);
        valueColumn.setReorderable(false);
        fieldColumn.setResizable(true);
        valueColumn.setResizable(true);

        // Set column headers
        fieldColumn.setText("Field");
        valueColumn.setText("Value");
    }

    /**
     * Setup navigation buttons
     */
    private void setupNavigationButtons() {
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
        if (uploadPhotoButton != null) {
            uploadPhotoButton.setOnAction(this::handleUploadPhoto);
        }
    }

    /**
     * Setup image view properties
     */
    private void setupImageView() {
        if (profileImageView != null) {
            profileImageView.setPreserveRatio(true);
            profileImageView.setSmooth(true);
            profileImageView.setCache(true);
            profileImageView.setFitWidth(150);
            profileImageView.setFitHeight(150);

            // Set default image
            loadDefaultImage();
        }
    }

    /**
     * Load default profile image
     */
    private void loadDefaultImage() {
        try {
            // Try to load default avatar from resources
            String defaultImagePath = "/com/sinilai/images/default-avatar.png";
            var imageStream = getClass().getResourceAsStream(defaultImagePath);

            if (imageStream != null) {
                Image defaultImage = new Image(imageStream);
                if (!defaultImage.isError()) {
                    profileImageView.setImage(defaultImage);
                    return;
                }
            }

            // If default image not found, create a simple placeholder
            createPlaceholderImage();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load default avatar image", e);
            createPlaceholderImage();
        }
    }

    /**
     * Create placeholder image when default image is not available
     */
    private void createPlaceholderImage() {
        try {
            // Create a simple colored rectangle as placeholder
            javafx.scene.paint.Color color = javafx.scene.paint.Color.LIGHTGRAY;
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(150, 150);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(color);
            gc.fillRect(0, 0, 150, 150);
            gc.setFill(javafx.scene.paint.Color.DARKGRAY);
            gc.fillText("No Image", 50, 75);

            javafx.scene.image.WritableImage placeholderImage = new javafx.scene.image.WritableImage(150, 150);
            canvas.snapshot(null, placeholderImage);
            profileImageView.setImage(placeholderImage);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not create placeholder image", e);
        }
    }

    /**
     * Set data mahasiswa dan user
     */
    public void setMahasiswa(MahasiswaModel mahasiswa, UserModel user) {
        if (mahasiswa == null || user == null) {
            LOGGER.warning("Mahasiswa or User data is null");
            showAlert("Error", "Data mahasiswa atau user tidak tersedia", Alert.AlertType.ERROR);
            return;
        }

        this.currentUser = user;
        this.currentMahasiswa = mahasiswa;

        // Jika memang ingin ubah session juga
        Session.setUser(user);
        Session.setMahasiswa(mahasiswa);

        LOGGER.info("Mahasiswa data set successfully for: " + user.getNama());

        // Jalankan refresh di thread JavaFX
        Platform.runLater(this::refreshData);
    }

    /**
     * Refresh tampilan data
     */
    public void refreshData() {
        if (!isInitialized || currentUser == null) {
            LOGGER.warning("Cannot refresh data - not properly initialized");
            return;
        }

        if (currentMahasiswa == null) {
            LOGGER.info("Mahasiswa data is empty, prompting user to lengkapi data");
            showAlert("Lengkapi Data", "Anda belum melengkapi data mahasiswa. Silakan klik 'Ubah Data'.",
                    Alert.AlertType.INFORMATION);
        }

        try {
            Platform.runLater(() -> {
                updateLabels();
                updateTableData();
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing data", e);
            showAlert("Error", "Gagal memuat data profil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Update labels dengan data user dan mahasiswa
     */
    private void updateLabels() {
        try {
            LOGGER.info("[ProfilController] updateLabels called");

            String nama = currentUser != null && currentUser.getNama() != null ? currentUser.getNama()
                    : "Nama tidak tersedia";
            String nim = "-";
            String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "-";
            String jurusan = "-";
            String prodi = "-";

            if (currentMahasiswa != null) {
                nim = currentMahasiswa.getNim() != null ? currentMahasiswa.getNim() : "-";
                jurusan = currentMahasiswa.getJurusan() != null ? currentMahasiswa.getJurusan() : "-";
                prodi = currentMahasiswa.getProdi() != null ? currentMahasiswa.getProdi() : "-";
            }

            if (namaMahasiswaLabel != null)
                namaMahasiswaLabel.setText(nama);
            if (namaProfilLabel != null)
                namaProfilLabel.setText(nama);
            if (nimLabel != null)
                nimLabel.setText("NIM: " + nim);
            if (emailLabel != null)
                emailLabel.setText("Email: " + email);
            if (jurusanLabel != null)
                jurusanLabel.setText("Jurusan: " + jurusan);
            if (prodiLabel != null)
                prodiLabel.setText("Program Studi: " + prodi);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating labels", e);
        }
    }

    /**
     * Update data tabel
     */
    private void updateTableData() {
        if (dataTable == null) {
            LOGGER.warning("DataTable is null");
            return;
        }

        try {
            Map<String, String> dataMap = createDataMap();
            ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(dataMap.entrySet());
            dataTable.setItems(items);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating table data", e);
            showAlert("Error", "Gagal memuat data tabel", Alert.AlertType.ERROR);
        }
    }

    /**
     * Membuat map data mahasiswa
     */
    private Map<String, String> createDataMap() {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("Nama", safeGetValue(currentUser.getNama()));
        map.put("Email", safeGetValue(currentUser.getEmail()));

        if (currentMahasiswa != null) {
            map.put("NIM", safeGetValue(currentMahasiswa.getNim()));
            map.put("Jurusan", safeGetValue(currentMahasiswa.getJurusan()));
            map.put("Program Studi", safeGetValue(currentMahasiswa.getProdi()));
            map.put("Semester", String.valueOf(currentMahasiswa.getSemester()));

            if (currentMahasiswa.getJalurSeleksi() != null)
                map.put("Jalur Seleksi", safeGetValue(currentMahasiswa.getJalurSeleksi()));
            if (currentMahasiswa.getAsalSekolah() != null)
                map.put("Asal Sekolah", safeGetValue(currentMahasiswa.getAsalSekolah()));
            if (currentMahasiswa.getTtl() != null)
                map.put("Tanggal Lahir", currentMahasiswa.getTtl().toString());
            if (currentMahasiswa.getAgama() != null)
                map.put("Agama", safeGetValue(currentMahasiswa.getAgama()));
            if (currentMahasiswa.getJkDisplay() != null)
                map.put("Jenis Kelamin", safeGetValue(currentMahasiswa.getJkDisplay()));
            if (currentMahasiswa.getAlamat() != null)
                map.put("Alamat", safeGetValue(currentMahasiswa.getAlamat()));
            if (currentMahasiswa.getKota() != null)
                map.put("Kota", safeGetValue(currentMahasiswa.getKota()));
            if (currentMahasiswa.getProv() != null)
                map.put("Provinsi", safeGetValue(currentMahasiswa.getProv()));
            if (currentMahasiswa.getNoTelp() != null)
                map.put("No Telepon", safeGetValue(currentMahasiswa.getNoTelp()));

        } else {
            map.put("NIM", "-");
            map.put("Jurusan", "-");
            map.put("Program Studi", "-");
            map.put("Semester", "-");
        }

        return map;
    }

    /**
     * Helper method untuk nilai safe
     */
    private String safeGetValue(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : "-";
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
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

            // Set initial directory
            String userHome = System.getProperty("user.home");
            File picturesDir = new File(userHome, "Pictures");
            if (picturesDir.exists() && picturesDir.isDirectory()) {
                fileChooser.setInitialDirectory(picturesDir);
            }

            Stage stage = (Stage) profileImageView.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Validate file size (max 5MB)
                long maxSize = 5 * 1024 * 1024; // 5MB
                if (selectedFile.length() > maxSize) {
                    showAlert("Peringatan", "Ukuran file terlalu besar. Maksimal 5MB", Alert.AlertType.WARNING);
                    return;
                }

                // Load and set image
                try {
                    Image image = new Image(selectedFile.toURI().toString());
                    if (image.isError()) {
                        showAlert("Error", "File gambar tidak valid atau rusak", Alert.AlertType.ERROR);
                        return;
                    }

                    profileImageView.setImage(image);
                    showAlert("Sukses", "Foto profil berhasil diubah", Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    showAlert("Error", "Gagal memuat gambar: " + e.getMessage(), Alert.AlertType.ERROR);
                }
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
        try {
            Session.setUser(currentUser);
            Session.setMahasiswa(currentMahasiswa);

            URL fxmlUrl = getClass().getResource("/com/sinilai/view/ProfileUpdateView.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ProfileUpdateController controller = loader.getController();
            if (currentMahasiswa != null) {
                controller.setMahasiswa(currentMahasiswa);
            }

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Maaf", "Terjadi kesalahan saat membuka halaman. Silakan coba lagi nanti.",
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin keluar dari aplikasi?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Navigate to login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("SINILAI - Login");
                stage.setMaximized(false);
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            showAlert("Error", "Gagal membuka halaman login: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Navigation handlers
     */
    @FXML
    private void handleHomeNavigation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/DashboardView.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setMahasiswaAndUser(currentMahasiswa, currentUser);

            Stage stage = (Stage) homeButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - SINILAI");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to home", e);
            showAlert("Error", "Gagal membuka halaman dashboard", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleKhsNavigation(ActionEvent event) {
        // TODO: Implement KHS navigation
        showAlert("Info", "Fitur KHS sedang dalam pengembangan", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSettingNavigation(ActionEvent event) {
        // TODO: Implement Settings navigation
        showAlert("Info", "Fitur Setting sedang dalam pengembangan", Alert.AlertType.INFORMATION);
    }

    /**
     * Show alert dengan thread safety
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Internal method untuk menampilkan alert
     */
    private void showAlertInternal(String title, String message, Alert.AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing alert", e);
        }
    }

    // Getter methods
    public MahasiswaModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}