package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import com.sinilai.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileUpdateController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ProfileUpdateController.class.getName());

    // Constants for validation
    private static final int NIK_LENGTH = 16;
    private static final int KK_LENGTH = 16;
    private static final int MAX_SEMESTER = 14;
    private static final int MIN_AGE = 15;
    private static final int MAX_AGE = 100;

    // FXML Components
    @FXML
    private Label namaMahasiswaLabel;
    @FXML
    private Button homeButton, profilButton, khsButton, settingButton, logoutButton, backButton;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button uploadPhotoButton;
    @FXML
    private Label namaProfilLabel, nimLabel, emailLabel, jurusanLabel, prodiLabel;
    @FXML
    private TextField nimField, jurusanField, prodiField, jalurSeleksiField, asalSekolahField;
    @FXML
    private TextField alamatField, kotaField, provinsiField, noTeleponField, tempatTinggalField;
    @FXML
    private TextField nikField, noKKField;
    @FXML
    private ComboBox<String> kelasComboBox, agamaComboBox, jenisKelaminComboBox;
    @FXML
    private ComboBox<String> pendidikanAkhirComboBox, statusMenikahComboBox, sumberUangComboBox;
    @FXML
    private ComboBox<Integer> semesterComboBox;
    @FXML
    private DatePicker ttlField;
    @FXML
    private Button batalButton, simpanButton;

    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // First check session validity
            if (!isSessionValid()) {
                redirectToLogin();
                return;
            }

            initializeComponents();
            loadSessionData();
            populateFormData();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    // ================= SESSION MANAGEMENT =================
    private boolean isSessionValid() {
        currentUser = Session.getUser();
        currentMahasiswa = Session.getMahasiswa();

        if (currentUser == null || currentMahasiswa == null) {
            LOGGER.warning("Session validation failed: User or Mahasiswa is null");
            return false;
        }

        try {
            // Verify the relationship between user and mahasiswa
            if (currentMahasiswa.getId() != currentUser.getId()) {
                LOGGER.warning("Session validation failed: User-Mahasiswa ID mismatch");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during session validation", e);
            return false;
        }

        return true;
    }

    private void redirectToLogin() {
        Session.clear();
        showAlertAndNavigate("Session Invalid", "Your session has expired. Please login again.", "Login.fxml");
    }

    // ================= UI INITIALIZATION =================
    private void initializeComponents() {
        setupComboBoxes();
        setupNavigation();
        setupFieldValidation();
    }

    private void setupComboBoxes() {
        // Semester ComboBox with Integer values
        semesterComboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return string.isEmpty() ? null : Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        ObservableList<Integer> semesterOptions = FXCollections.observableArrayList();
        for (int i = 1; i <= MAX_SEMESTER; i++) {
            semesterOptions.add(i);
        }
        semesterComboBox.setItems(semesterOptions);

        // Kelas ComboBox
        ObservableList<String> kelasOptions = FXCollections.observableArrayList(
                "TRPL 1A", "TRPL 1B", "TRPL 1C", "TRPL 1D",
                "TRPL 2A", "TRPL 2B", "TRPL 2C", "TRPL 2D",
                "TRPL 3A", "TRPL 3B", "TRPL 3C", "TRPL 3D",
                "TRPL 4A", "TRPL 4B", "TRPL 4C", "TRPL 4D",
                "MI 1A", "MI 1B", "MI 1C",
                "MI 2A", "MI 2B", "MI 2C",
                "MI 3A", "MI 3B", "MI 3C",
                "TK 1A", "TK 1B",
                "TK 2A", "TK 2B",
                "TK 3A", "TK 3B",
                "ANIMASI 1A", "ANIMASI 1B");
        kelasComboBox.setItems(kelasOptions);

        // Other ComboBox setups...
        setupAgamaComboBox();
        setupJenisKelaminComboBox();
        setupPendidikanComboBox();
        setupStatusMenikahComboBox();
        setupSumberUangComboBox();
    }

    private void setupAgamaComboBox() {
        ObservableList<String> agamaOptions = FXCollections.observableArrayList(
                "Islam", "Kristen Protestan", "Kristen Katolik",
                "Hindu", "Buddha", "Khonghucu", "Kepercayaan");
        agamaComboBox.setItems(agamaOptions);
    }

    private void setupJenisKelaminComboBox() {
        ObservableList<String> jenisKelaminOptions = FXCollections.observableArrayList("L", "P");
        jenisKelaminComboBox.setItems(jenisKelaminOptions);
    }

    private void setupPendidikanComboBox() {
        ObservableList<String> pendidikanOptions = FXCollections.observableArrayList(
                "SD/Sederajat", "SMP/Sederajat", "SMA/SMK/Sederajat",
                "D1", "D2", "D3", "D4/S1", "S2", "S3");
        pendidikanAkhirComboBox.setItems(pendidikanOptions);
    }

    private void setupStatusMenikahComboBox() {
        ObservableList<String> statusMenikahOptions = FXCollections.observableArrayList(
                "Belum Menikah", "Menikah", "Cerai Hidup", "Cerai Mati");
        statusMenikahComboBox.setItems(statusMenikahOptions);
    }

    private void setupSumberUangComboBox() {
        ObservableList<String> sumberUangOptions = FXCollections.observableArrayList(
                "Orang Tua", "Wali", "Keluarga", "Ayah", "Ibu", "Kakek/Nenek", "Saudara",
                "Beasiswa Pemerintah", "Beasiswa LPDP", "Beasiswa Bidikmisi",
                "Beasiswa KIP Kuliah", "Beasiswa Unggulan", "Beasiswa Prestasi",
                "Beasiswa Daerah", "Bantuan Pemerintah Daerah", "Dana BOS",
                "Beasiswa Institusi/Yayasan", "Beasiswa Perusahaan", "Beasiswa Bank",
                "Beasiswa Organisasi Kemasyarakatan", "Beasiswa Alumni",
                "Bantuan Sosial", "Zakat/Infaq/Sedekah",
                "Usaha Sendiri", "Kerja Sambilan", "Tabungan Sendiri",
                "Investasi", "Bisnis Online",
                "Pinjaman Bank", "Kredit Pendidikan", "Pinjaman Koperasi",
                "Pinjaman Keluarga", "Arisan",
                "Campuran (Gabungan Beberapa Sumber)", "Lainnya");
        sumberUangComboBox.setItems(sumberUangOptions);
        sumberUangComboBox.setValue("Orang Tua");
    }

    private void setupNavigation() {
        homeButton.setOnAction(e -> navigateTo("Dashboard.fxml"));
        profilButton.setOnAction(e -> navigateTo("ProfilView.fxml"));
        khsButton.setOnAction(e -> navigateTo("KHSView.fxml"));
        settingButton.setOnAction(e -> navigateTo("SettingView.fxml"));
        logoutButton.setOnAction(e -> handleLogout());
        backButton.setOnAction(e -> handleBack());
    }

    private void setupFieldValidation() {
        setupNIKValidation();
        setupKKValidation();
        setupPhoneValidation();
    }

    private void setupNIKValidation() {
        nikField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") || newVal.length() > NIK_LENGTH) {
                nikField.setText(oldVal);
            }
        });
    }

    private void setupKKValidation() {
        noKKField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") || newVal.length() > KK_LENGTH) {
                noKKField.setText(oldVal);
            }
        });
    }

    private void setupPhoneValidation() {
        noTeleponField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9+\\-()\\s]*")) {
                noTeleponField.setText(oldVal);
            }
        });
    }

    // ================= DATA LOADING =================
    private void loadSessionData() {
        if (!isSessionValid()) {
            redirectToLogin();
            return;
        }

        updateUILabels();
    }

    private void updateUILabels() {
        namaMahasiswaLabel.setText(currentUser.getNama());
        namaProfilLabel.setText(currentUser.getNama());
        emailLabel.setText("Email: " + currentUser.getEmail());
        nimLabel.setText("NIM: " + currentMahasiswa.getNim());
        jurusanLabel.setText("Jurusan: " + safeString(currentMahasiswa.getJurusan()));
        prodiLabel.setText("Program Studi: " + safeString(currentMahasiswa.getProdi()));
    }

    private String safeString(String value) {
        return value != null ? value : "-";
    }

    private void populateFormData() {
        if (!isSessionValid()) {
            redirectToLogin();
            return;
        }

        setTextFieldValues();
        setComboBoxValues();
        setDatePickerValue();
    }

    private void setTextFieldValues() {
        nimField.setText(currentMahasiswa.getNim());
        jurusanField.setText(currentMahasiswa.getJurusan());
        prodiField.setText(currentMahasiswa.getProdi());
        jalurSeleksiField.setText(currentMahasiswa.getJalurSeleksi());
        asalSekolahField.setText(currentMahasiswa.getAsalSekolah());
        alamatField.setText(currentMahasiswa.getAlamat());
        kotaField.setText(currentMahasiswa.getKota());
        provinsiField.setText(currentMahasiswa.getProv());
        noTeleponField.setText(currentMahasiswa.getNoTelp());
        tempatTinggalField.setText(currentMahasiswa.getTempatTinggal());
        nikField.setText(currentMahasiswa.getNik());
        noKKField.setText(currentMahasiswa.getNoKk());
    }

    private void setComboBoxValues() {
        kelasComboBox.setValue(currentMahasiswa.getKelas());
        semesterComboBox.setValue(currentMahasiswa.getSemester());
        agamaComboBox.setValue(currentMahasiswa.getAgama());
        jenisKelaminComboBox.setValue(currentMahasiswa.getJk());
        pendidikanAkhirComboBox.setValue(currentMahasiswa.getPendidikanAkhir());
        statusMenikahComboBox.setValue(currentMahasiswa.getStatusMenikah());
        sumberUangComboBox.setValue(currentMahasiswa.getSumberUang());
    }

    private void setDatePickerValue() {
        if (currentMahasiswa.getTtl() != null) {
            ttlField.setValue(currentMahasiswa.getTtl().toLocalDate());
        }
    }

    // ================= EVENT HANDLERS =================
    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        File selectedFile = fileChooser.showOpenDialog(uploadPhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile photo uploaded successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading profile photo", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile photo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSimpan() {
        if (!isSessionValid()) {
            redirectToLogin();
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            MahasiswaModel updatedMahasiswa = createMahasiswaFromForm();
            saveMahasiswaData(updatedMahasiswa);

            Session.setMahasiswa(updatedMahasiswa);
            showAlertAndNavigate("Success", "Data saved successfully.", "ProfilView.fxml");
        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        }
    }

    @FXML
    private void handleBack() {
        showConfirmationDialog("Cancel Changes", "Unsaved changes will be lost. Continue?",
                () -> navigateTo("ProfilView.fxml"));
    }

    @FXML
    private void handleLogout() {
        showConfirmationDialog("Logout", "Are you sure you want to logout?",
                () -> {
                    Session.clear();
                    navigateTo("Login.fxml");
                });
    }

    @FXML
    private void handleBatal() {
        handleBack();
    }

    // ================= DATABASE OPERATIONS =================
    private void saveMahasiswaData(MahasiswaModel mahasiswa) throws SQLException {
        if (!isSessionValid()) {
            throw new SQLException("Invalid session state");
        }

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            if (mahasiswaExists(conn, mahasiswa.getNim())) {
                updateMahasiswa(conn, mahasiswa);
            } else {
                insertMahasiswa(conn, mahasiswa);
            }

            conn.commit();
        }
    }

    private boolean mahasiswaExists(Connection conn, String nim) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mahasiswa WHERE nim = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nim);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void updateMahasiswa(Connection conn, MahasiswaModel mahasiswa) throws SQLException {
        String sql = """
                UPDATE mahasiswa SET
                    kelas=?, jurusan=?, prodi=?, jalur_seleksi=?, asal_sekolah=?, ttl=?,
                    agama=?, jk=?, alamat=?, kota=?, prov=?, no_telp=?,
                    pendidikan_akhir=?, status_menikah=?, tempat_tinggal=?,
                    sumber_uang=?, nik=?, no_kk=?, semester=?
                WHERE nim=?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, mahasiswa.getKelas());
            stmt.setString(paramIndex++, mahasiswa.getJurusan());
            stmt.setString(paramIndex++, mahasiswa.getProdi());
            stmt.setString(paramIndex++, mahasiswa.getJalurSeleksi());
            stmt.setString(paramIndex++, mahasiswa.getAsalSekolah());
            stmt.setDate(paramIndex++, mahasiswa.getTtl());
            stmt.setString(paramIndex++, mahasiswa.getAgama());
            stmt.setString(paramIndex++, mahasiswa.getJk());
            stmt.setString(paramIndex++, mahasiswa.getAlamat());
            stmt.setString(paramIndex++, mahasiswa.getKota());
            stmt.setString(paramIndex++, mahasiswa.getProv());
            stmt.setString(paramIndex++, mahasiswa.getNoTelp());
            stmt.setString(paramIndex++, mahasiswa.getPendidikanAkhir());
            stmt.setString(paramIndex++, mahasiswa.getStatusMenikah());
            stmt.setString(paramIndex++, mahasiswa.getTempatTinggal());
            stmt.setString(paramIndex++, mahasiswa.getSumberUang());
            stmt.setString(paramIndex++, mahasiswa.getNik());
            stmt.setString(paramIndex++, mahasiswa.getNoKk());
            stmt.setInt(paramIndex++, mahasiswa.getSemester());
            stmt.setString(paramIndex++, mahasiswa.getNim());

            stmt.executeUpdate();
        }
    }

    private void insertMahasiswa(Connection conn, MahasiswaModel mahasiswa) throws SQLException {
        String sql = """
                INSERT INTO mahasiswa
                    (nim, kelas, jurusan, prodi, jalur_seleksi, asal_sekolah, ttl, agama, jk,
                     alamat, kota, prov, no_telp, pendidikan_akhir, status_menikah,
                     tempat_tinggal, sumber_uang, nik, no_kk, semester, id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, mahasiswa.getNim());
            stmt.setString(paramIndex++, mahasiswa.getKelas());
            stmt.setString(paramIndex++, mahasiswa.getJurusan());
            stmt.setString(paramIndex++, mahasiswa.getProdi());
            stmt.setString(paramIndex++, mahasiswa.getJalurSeleksi());
            stmt.setString(paramIndex++, mahasiswa.getAsalSekolah());
            stmt.setDate(paramIndex++, mahasiswa.getTtl());
            stmt.setString(paramIndex++, mahasiswa.getAgama());
            stmt.setString(paramIndex++, mahasiswa.getJk());
            stmt.setString(paramIndex++, mahasiswa.getAlamat());
            stmt.setString(paramIndex++, mahasiswa.getKota());
            stmt.setString(paramIndex++, mahasiswa.getProv());
            stmt.setString(paramIndex++, mahasiswa.getNoTelp());
            stmt.setString(paramIndex++, mahasiswa.getPendidikanAkhir());
            stmt.setString(paramIndex++, mahasiswa.getStatusMenikah());
            stmt.setString(paramIndex++, mahasiswa.getTempatTinggal());
            stmt.setString(paramIndex++, mahasiswa.getSumberUang());
            stmt.setString(paramIndex++, mahasiswa.getNik());
            stmt.setString(paramIndex++, mahasiswa.getNoKk());
            stmt.setInt(paramIndex++, mahasiswa.getSemester());
            stmt.setInt(paramIndex++, Session.getUser().getId());

            stmt.executeUpdate();
        }
    }

    // ================= VALIDATION =================
    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        validateRequiredFields(errors);
        validateFieldFormats(errors);
        validateSemester(errors);

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Errors", String.join("\n", errors));
            return false;
        }
        return true;
    }

    private void validateRequiredFields(List<String> errors) {
        if (isFieldEmpty(nimField))
            errors.add("NIM is required");
        if (isFieldEmpty(jurusanField))
            errors.add("Jurusan is required");
        if (isFieldEmpty(prodiField))
            errors.add("Program Studi is required");
        if (isFieldEmpty(alamatField))
            errors.add("Alamat is required");
        if (isFieldEmpty(kotaField))
            errors.add("Kota is required");
        if (isFieldEmpty(provinsiField))
            errors.add("Provinsi is required");
        if (ttlField.getValue() == null)
            errors.add("Tanggal Lahir is required");
        if (agamaComboBox.getValue() == null)
            errors.add("Agama is required");
        if (jenisKelaminComboBox.getValue() == null)
            errors.add("Jenis Kelamin is required");
        if (semesterComboBox.getValue() == null)
            errors.add("Semester is required");
    }

    private void validateFieldFormats(List<String> errors) {
        if (!isFieldEmpty(nikField)) {
            String nik = nikField.getText().trim();
            if (nik.length() != NIK_LENGTH || !nik.matches("\\d+")) {
                errors.add("NIK must be 16 digits");
            }
        }

        if (!isFieldEmpty(noKKField)) {
            String noKK = noKKField.getText().trim();
            if (noKK.length() != KK_LENGTH || !noKK.matches("\\d+")) {
                errors.add("No KK must be 16 digits");
            }
        }

        if (!isFieldEmpty(noTeleponField)) {
            String noTelp = noTeleponField.getText().trim();
            if (!noTelp.matches("[0-9+\\-()\\s]{8,15}")) {
                errors.add("Nomor Telepon format is invalid");
            }
        }
    }

    private void validateSemester(List<String> errors) {
        if (semesterComboBox.getValue() == null) {
            errors.add("Semester is required");
        } else if (semesterComboBox.getValue() < 1 || semesterComboBox.getValue() > MAX_SEMESTER) {
            errors.add("Semester must be between 1 and " + MAX_SEMESTER);
        }
    }

    private boolean isFieldEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    // ================= UTILITY METHODS =================
    private MahasiswaModel createMahasiswaFromForm() {
        MahasiswaModel mahasiswa = new MahasiswaModel();

        // Set fields from form
        mahasiswa.setNim(nimField.getText().trim());
        mahasiswa.setJurusan(jurusanField.getText().trim());
        mahasiswa.setProdi(prodiField.getText().trim());
        mahasiswa.setJalurSeleksi(jalurSeleksiField.getText().trim());
        mahasiswa.setAsalSekolah(asalSekolahField.getText().trim());
        mahasiswa.setAlamat(alamatField.getText().trim());
        mahasiswa.setKota(kotaField.getText().trim());
        mahasiswa.setProv(provinsiField.getText().trim());
        mahasiswa.setNoTelp(noTeleponField.getText().trim());
        mahasiswa.setTempatTinggal(tempatTinggalField.getText().trim());
        mahasiswa.setNik(nikField.getText().trim());
        mahasiswa.setNoKk(noKKField.getText().trim());

        // Set combo box values
        mahasiswa.setKelas(kelasComboBox.getValue());
        mahasiswa.setAgama(agamaComboBox.getValue());
        mahasiswa.setJk(jenisKelaminComboBox.getValue());
        mahasiswa.setPendidikanAkhir(pendidikanAkhirComboBox.getValue());
        mahasiswa.setStatusMenikah(statusMenikahComboBox.getValue());
        mahasiswa.setSumberUang(sumberUangComboBox.getValue());
        mahasiswa.setSemester(semesterComboBox.getValue());

        // Set date
        if (ttlField.getValue() != null) {
            mahasiswa.setTtl(java.sql.Date.valueOf(ttlField.getValue()));
        }

        return mahasiswa;
    }

    private void navigateTo(String fxml) {
        try {
            if (!isSessionValid()) {
                redirectToLogin();
                return;
            }

            Parent root = FXMLLoader.load(getClass().getResource("/com/sinilai/view/" + fxml));
            Stage stage = (Stage) simpanButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load page: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertAndNavigate(String title, String message, String fxml) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
        navigateTo(fxml);
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
    }

    // ================= ERROR HANDLING =================
    private void handleInitializationError(Exception e) {
        LOGGER.log(Level.SEVERE, "Initialization error", e);
        showAlert(Alert.AlertType.ERROR, "Initialization Error",
                "Failed to initialize controller: " + e.getMessage());
    }

    private void handleDatabaseError(SQLException e) {
        LOGGER.log(Level.SEVERE, "Database error", e);
        showAlert(Alert.AlertType.ERROR, "Database Error",
                "Failed to save data: " + e.getMessage());
    }

    private void handleUnexpectedError(Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error", e);
        showAlert(Alert.AlertType.ERROR, "Error",
                "An unexpected error occurred: " + e.getMessage());
    }
}