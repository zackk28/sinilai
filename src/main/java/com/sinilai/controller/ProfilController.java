package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import com.sinilai.utils.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfilController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ProfilController.class.getName());
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = { "*.jpg", "*.png", "*.jpeg", "*.gif" };
    private static final String DEFAULT_AVATAR_PATH = "/com/sinilai/images/default-avatar.png";

    @FXML
    private Label namaMahasiswaLabel, namaProfilLabel, nimLabel, emailLabel, jurusanLabel, prodiLabel;
    @FXML
    private TableView<Map.Entry<String, String>> dataTable;
    @FXML
    private TableColumn<Map.Entry<String, String>, String> fieldColumn, valueColumn;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button homeButton, khsButton, settingButton, ubahDataButton, uploadPhotoButton, logoutButton;

    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;
    private volatile boolean isInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Memulai inisialisasi ProfilController");

        try {
            // Ambil data dari session
            this.currentUser = Session.getUser();
            this.currentMahasiswa = Session.getMahasiswa();

            // Validasi data session
            if (!validateSessionData()) {
                LOGGER.severe("Data session tidak valid");
                return;
            }

            // Inisialisasi komponen UI
            initializeComponents();

            // Reload data dari database dengan error handling yang lebih baik
            try {
                reloadDataFromDatabase();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Gagal reload data dari database, menggunakan data session", e);
                // Lanjutkan dengan data yang ada di session
            }

            // Update UI
            updateUI();

            isInitialized = true;
            LOGGER.info("ProfilController berhasil diinisialisasi");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saat inisialisasi", e);
            showAlert("Error", "Gagal memuat halaman profil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateSessionData() {
        if (currentUser == null) {
            LOGGER.warning("User session tidak ditemukan");
            showAlert("Error", "Session user tidak ditemukan. Silakan login kembali.", Alert.AlertType.ERROR);
            redirectToLogin();
            return false;
        }

        if (currentMahasiswa == null) {
            LOGGER.warning("Mahasiswa session tidak ditemukan");
            showAlert("Error", "Data mahasiswa tidak ditemukan. Silakan login kembali.", Alert.AlertType.ERROR);
            redirectToLogin();
            return false;
        }

        if (currentMahasiswa.getId() <= 0) {
            LOGGER.warning("ID mahasiswa tidak valid: " + currentMahasiswa.getId());
            showAlert("Error", "ID mahasiswa tidak valid.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void initializeComponents() {
        LOGGER.info("Menginisialisasi komponen UI");

        // Setup table columns
        setupTableColumns();

        // Setup navigation buttons
        setupNavigationButtons();

        // Setup image view
        setupImageView();

        // Setup button styles
        setupButtonStyles();
    }

    private void setupTableColumns() {
        if (fieldColumn != null && valueColumn != null && dataTable != null) {
            LOGGER.info("Mengatur kolom tabel");

            fieldColumn.setCellValueFactory(param -> {
                if (param.getValue() != null) {
                    return new SimpleStringProperty(param.getValue().getKey());
                }
                return new SimpleStringProperty("");
            });

            valueColumn.setCellValueFactory(param -> {
                if (param.getValue() != null) {
                    return new SimpleStringProperty(param.getValue().getValue());
                }
                return new SimpleStringProperty("");
            });

            // Set column widths
            fieldColumn.setMinWidth(200);
            fieldColumn.setPrefWidth(250);
            valueColumn.setMinWidth(400);
            valueColumn.setPrefWidth(550);

            // Set row factory for better styling
            dataTable.setRowFactory(tv -> {
                TableRow<Map.Entry<String, String>> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        // Double click handling if needed
                    }
                });
                return row;
            });
        } else {
            LOGGER.warning("Table components tidak ditemukan");
        }
    }

    private void setupNavigationButtons() {
        LOGGER.info("Mengatur tombol navigasi");

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
        if (logoutButton != null) {
            logoutButton.setOnAction(this::handleLogout);
        }
    }

    private void setupButtonStyles() {
        List<Button> buttons = Arrays.asList(homeButton, khsButton, settingButton, ubahDataButton, uploadPhotoButton,
                logoutButton);

        for (Button button : buttons) {
            if (button != null) {
                button.setOnMouseEntered(e -> {
                    String currentStyle = button.getStyle();
                    if (!currentStyle.contains("-fx-opacity")) {
                        button.setStyle(currentStyle + "-fx-opacity: 0.8;");
                    }
                });

                button.setOnMouseExited(e -> {
                    String currentStyle = button.getStyle();
                    button.setStyle(currentStyle.replace("-fx-opacity: 0.8;", ""));
                });
            }
        }
    }

    private void setupImageView() {
        if (profileImageView != null) {
            LOGGER.info("Mengatur ImageView profil");

            profileImageView.setPreserveRatio(true);
            profileImageView.setSmooth(true);
            profileImageView.setFitWidth(120);
            profileImageView.setFitHeight(120);
            profileImageView.setStyle("-fx-background-radius: 60; -fx-border-radius: 60;");

            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            // Coba load dari resources
            URL imageUrl = getClass().getResource(DEFAULT_AVATAR_PATH);
            if (imageUrl != null) {
                Image defaultImage = new Image(imageUrl.toExternalForm());
                profileImageView.setImage(defaultImage);
                LOGGER.info("Default avatar berhasil dimuat dari: " + DEFAULT_AVATAR_PATH);
            } else {
                LOGGER.warning("Default avatar tidak ditemukan di: " + DEFAULT_AVATAR_PATH);
                // Buat image placeholder sederhana
                createPlaceholderImage();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Gagal memuat default avatar", e);
            createPlaceholderImage();
        }
    }

    private void createPlaceholderImage() {
        try {
            // Buat image placeholder sederhana (1x1 pixel transparan)
            Image placeholder = new Image(
                    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
            if (profileImageView != null) {
                profileImageView.setImage(placeholder);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Gagal membuat placeholder image", e);
        }
    }

    private void reloadDataFromDatabase() {
        LOGGER.info("Memuat ulang data dari database");

        if (currentMahasiswa == null || currentMahasiswa.getId() <= 0) {
            LOGGER.severe("Data mahasiswa tidak valid untuk reload");
            throw new IllegalStateException("Data mahasiswa tidak valid");
        }

        try {
            // Reload data mahasiswa
            reloadMahasiswaFromDB();

            // Reload data user jika diperlukan dan tabel users ada
            if (currentUser != null && currentUser.getId() > 0) {
                try {
                    reloadUserFromDB();
                } catch (SQLException e) {
                    if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("Table") ||
                            e.getMessage().contains("unknown") || e.getErrorCode() == 1146) {
                        LOGGER.warning("Tabel users tidak ada, skip reload user data: " + e.getMessage());
                        // Lanjutkan tanpa reload user data
                    } else {
                        throw e; // Re-throw jika error lain
                    }
                }
            }

            LOGGER.info("Data berhasil dimuat ulang dari database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal memuat data dari database", e);
            throw new RuntimeException("Gagal memuat data dari database", e);
        }
    }

    private void reloadMahasiswaFromDB() throws SQLException {
        String query = "SELECT * FROM mahasiswa WHERE id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentMahasiswa.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    updateMahasiswaFromResultSet(rs);
                    Session.setMahasiswa(currentMahasiswa);
                    LOGGER.info("Data mahasiswa berhasil dimuat ulang dari database");
                } else {
                    throw new SQLException("Data mahasiswa dengan ID " + currentMahasiswa.getId() + " tidak ditemukan");
                }
            }
        }
    }

    private void reloadUserFromDB() throws SQLException {
        // Cek apakah tabel users ada terlebih dahulu
        if (!checkTableExists("users")) {
            LOGGER.warning("Tabel 'users' tidak ada di database, skip reload user data");
            return;
        }

        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    updateUserFromResultSet(rs);
                    Session.setUser(currentUser);
                    LOGGER.info("Data user berhasil dimuat ulang dari database");
                } else {
                    LOGGER.warning("Data user dengan ID " + currentUser.getId() + " tidak ditemukan");
                }
            }
        }
    }

    /**
     * Mengecek apakah tabel ada di database
     * 
     * @param tableName nama tabel yang akan dicek
     * @return true jika tabel ada, false jika tidak
     */
    private boolean checkTableExists(String tableName) {
        try (Connection conn = Koneksi.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
                boolean exists = rs.next();
                LOGGER.info("Tabel '" + tableName + "' " + (exists ? "ada" : "tidak ada") + " di database");
                return exists;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Gagal mengecek keberadaan tabel: " + tableName, e);
            return false;
        }
    }

    private void updateMahasiswaFromResultSet(ResultSet rs) throws SQLException {
        try {
            currentMahasiswa.setId(rs.getInt("id"));
            currentMahasiswa.setNim(rs.getString("nim"));
            currentMahasiswa.setJurusan(rs.getString("jurusan"));
            currentMahasiswa.setProdi(rs.getString("prodi"));

            int semester = rs.getInt("semester");
            currentMahasiswa.setSemester((semester >= 1 && semester <= 14) ? semester : 1);

            currentMahasiswa.setJalurSeleksi(rs.getString("jalur_seleksi"));
            currentMahasiswa.setAsalSekolah(rs.getString("asal_sekolah"));
            currentMahasiswa.setTtl(rs.getDate("ttl"));
            currentMahasiswa.setAgama(rs.getString("agama"));
            currentMahasiswa.setJk(rs.getString("jk"));
            currentMahasiswa.setAlamat(rs.getString("alamat"));
            currentMahasiswa.setKota(rs.getString("kota"));
            currentMahasiswa.setProv(rs.getString("prov"));
            currentMahasiswa.setNoTelp(rs.getString("no_telp"));
            currentMahasiswa.setPendidikanAkhir(rs.getString("pendidikan_akhir"));
            currentMahasiswa.setStatusMenikah(rs.getString("status_menikah"));
            currentMahasiswa.setTempatTinggal(rs.getString("tempat_tinggal"));
            currentMahasiswa.setSumberUang(rs.getString("sumber_uang"));
            currentMahasiswa.setNik(rs.getString("nik"));
            currentMahasiswa.setNoKk(rs.getString("no_kk"));

            LOGGER.info("Data mahasiswa berhasil diperbarui dari ResultSet");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saat mengupdate mahasiswa dari ResultSet", e);
            throw e;
        }
    }

    private void updateUserFromResultSet(ResultSet rs) throws SQLException {
        try {
            currentUser.setId(rs.getInt("id"));
            currentUser.setNama(rs.getString("nama"));
            currentUser.setEmail(rs.getString("email"));
            // Jangan set password dari database untuk keamanan

            LOGGER.info("Data user berhasil diperbarui dari ResultSet");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saat mengupdate user dari ResultSet", e);
            throw e;
        }
    }

    private void updateUI() {
        LOGGER.info("Memperbarui UI");

        try {
            // Update labels
            updateLabels();

            // Update data table
            updateDataTable();

            LOGGER.info("UI berhasil diperbarui");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saat memperbarui UI", e);
            showAlert("Error", "Gagal memperbarui tampilan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateLabels() {
        LOGGER.info("Memperbarui label");

        if (currentUser != null) {
            safeSetText(namaMahasiswaLabel, currentUser.getNama());
            safeSetText(namaProfilLabel, currentUser.getNama());
            safeSetText(emailLabel, "Email: " + safe(currentUser.getEmail()));
            LOGGER.info("Label user berhasil diperbarui");
        }

        if (currentMahasiswa != null) {
            safeSetText(nimLabel, "NIM: " + safe(currentMahasiswa.getNim()));
            safeSetText(jurusanLabel, "Jurusan: " + safe(currentMahasiswa.getJurusan()));
            safeSetText(prodiLabel, "Program Studi: " + safe(currentMahasiswa.getProdi()));
            LOGGER.info("Label mahasiswa berhasil diperbarui");
        }
    }

    private void updateDataTable() {
        if (dataTable == null) {
            LOGGER.warning("DataTable tidak ditemukan");
            return;
        }

        if (currentMahasiswa == null) {
            LOGGER.warning("Data mahasiswa tidak tersedia untuk tabel");
            return;
        }

        LOGGER.info("Memperbarui data table");

        try {
            Map<String, String> dataMap = createDataMap();
            dataTable.setItems(FXCollections.observableArrayList(dataMap.entrySet()));
            LOGGER.info("Data table berhasil diperbarui dengan " + dataMap.size() + " item");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saat memperbarui data table", e);
            showAlert("Error", "Gagal memperbarui tabel data", Alert.AlertType.ERROR);
        }
    }

    private Map<String, String> createDataMap() {
        Map<String, String> dataMap = new LinkedHashMap<>();

        if (currentMahasiswa != null) {
            dataMap.put("NIM", safe(currentMahasiswa.getNim()));
            dataMap.put("Jurusan", safe(currentMahasiswa.getJurusan()));
            dataMap.put("Program Studi", safe(currentMahasiswa.getProdi()));
            dataMap.put("Semester", String.valueOf(currentMahasiswa.getSemester()));
            dataMap.put("Jalur Seleksi", safe(currentMahasiswa.getJalurSeleksi()));
            dataMap.put("Asal Sekolah", safe(currentMahasiswa.getAsalSekolah()));
            dataMap.put("Tanggal Lahir",
                    currentMahasiswa.getTtl() != null ? currentMahasiswa.getTtl().toString() : "-");
            dataMap.put("Agama", safe(currentMahasiswa.getAgama()));
            dataMap.put("Jenis Kelamin", safe(currentMahasiswa.getJk()));
            dataMap.put("Alamat", safe(currentMahasiswa.getAlamat()));
            dataMap.put("Kota", safe(currentMahasiswa.getKota()));
            dataMap.put("Provinsi", safe(currentMahasiswa.getProv()));
            dataMap.put("No Telepon", safe(currentMahasiswa.getNoTelp()));
            dataMap.put("Pendidikan Akhir", safe(currentMahasiswa.getPendidikanAkhir()));
            dataMap.put("Status Menikah", safe(currentMahasiswa.getStatusMenikah()));
            dataMap.put("Tempat Tinggal", safe(currentMahasiswa.getTempatTinggal()));
            dataMap.put("Sumber Uang", safe(currentMahasiswa.getSumberUang()));
            dataMap.put("NIK", safe(currentMahasiswa.getNik()));
            dataMap.put("No KK", safe(currentMahasiswa.getNoKk()));
        }

        return dataMap;
    }

    @FXML
    private void handleUbahData(ActionEvent event) {
        LOGGER.info("Handling ubah data");

        if (currentUser == null || currentMahasiswa == null) {
            showAlert("Error", "Data pengguna tidak lengkap", Alert.AlertType.ERROR);
            return;
        }

        // Pastikan session up to date
        Session.setUser(currentUser);
        Session.setMahasiswa(currentMahasiswa);

        navigateToView("/com/sinilai/view/ProfileUpdateView.fxml", event);
    }

    @FXML
    private void handleUploadPhoto(ActionEvent event) {
        LOGGER.info("Handling upload photo");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", ALLOWED_EXTENSIONS));

        Stage stage = (Stage) uploadPhotoButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            processSelectedImage(selectedFile);
        }
    }

    private void processSelectedImage(File file) {
        if (!isValidImageFile(file)) {
            showAlert("File Tidak Valid",
                    "File yang dipilih tidak valid atau melebihi ukuran maksimal (5MB)",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            Image newImage = new Image(file.toURI().toString());
            if (profileImageView != null) {
                profileImageView.setImage(newImage);
                showAlert("Berhasil", "Foto profil berhasil diperbarui", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal memuat foto profil", e);
            showAlert("Error", "Gagal memuat foto profil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        if (file.length() > MAX_FILE_SIZE) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LOGGER.info("Handling logout");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Logout");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Apakah Anda yakin ingin keluar dari aplikasi?");

        ButtonType yesButton = new ButtonType("Ya", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Tidak", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            Session.clear();
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        try {
            LOGGER.info("Redirecting to login");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage != null) {
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("SINILAI - Login");
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal redirect ke halaman login", e);
            showAlert("Error", "Tidak dapat kembali ke halaman login", Alert.AlertType.ERROR);
            Platform.exit();
        }
    }

    private Stage getCurrentStage() {
        if (logoutButton != null && logoutButton.getScene() != null) {
            return (Stage) logoutButton.getScene().getWindow();
        }
        return null;
    }

    private void navigateToView(String fxmlPath, ActionEvent event) {
        try {
            LOGGER.info("Navigating to: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal navigasi ke: " + fxmlPath, e);
            showAlert("Error", "Tidak dapat membuka halaman yang diminta: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleHomeNavigation(ActionEvent event) {
        navigateToView("/com/sinilai/view/DashboardView.fxml", event);
    }

    @FXML
    private void handleKhsNavigation(ActionEvent event) {
        showAlert("Info", "Fitur KHS sedang dalam pengembangan", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSettingNavigation(ActionEvent event) {
        showAlert("Info", "Fitur Setting sedang dalam pengembangan", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private String safe(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "-";
    }

    private void safeSetText(Label label, String text) {
        if (label != null) {
            label.setText(text != null ? text : "");
        }
    }

    // Public methods for external access
    public void setMahasiswa(MahasiswaModel mahasiswa) {
        this.currentMahasiswa = mahasiswa;
        this.currentUser = Session.getUser();
        if (isInitialized) {
            Platform.runLater(this::updateUI);
        }
    }

    public void setMahasiswa(MahasiswaModel mahasiswa, UserModel user) {
        this.currentMahasiswa = mahasiswa;
        this.currentUser = user;
        if (isInitialized) {
            Platform.runLater(this::updateUI);
        }
    }

    public MahasiswaModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    // Method untuk refresh data dari luar
    public void refreshData() {
        if (isInitialized) {
            try {
                reloadDataFromDatabase();
                updateUI();
                LOGGER.info("Data berhasil di-refresh");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saat refresh data", e);
                showAlert("Error", "Gagal memuat ulang data: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}