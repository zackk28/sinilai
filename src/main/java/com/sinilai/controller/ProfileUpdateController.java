package com.sinilai.controller;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import com.sinilai.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

/**
 * Controller for Profile Update functionality
 * Handles student profile data modification and persistence
 */
public class ProfileUpdateController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ProfileUpdateController.class.getName());

    // Constants for validation
    private static final int NIK_LENGTH = 16;
    private static final int KK_LENGTH = 16;
    private static final int MAX_SEMESTER = 14;

    // Header Labels
    @FXML
    private Label namaMahasiswaLabel;

    // Navigation Buttons
    @FXML
    private Button homeButton, profilButton, khsButton, settingButton, logoutButton, backButton;

    // Profile Section
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button uploadPhotoButton;
    @FXML
    private Label namaProfilLabel, nimLabel, emailLabel, jurusanLabel, prodiLabel;

    // Form Fields
    @FXML
    private TextField nimField;
    @FXML
    private TextField jurusanField;
    @FXML
    private TextField prodiField;
    @FXML
    private ComboBox<String> semesterComboBox;
    @FXML
    private TextField jalurSeleksiField;
    @FXML
    private TextField asalSekolahField;
    @FXML
    private DatePicker ttlField;
    @FXML
    private ComboBox<String> agamaComboBox;
    @FXML
    private ComboBox<String> jenisKelaminComboBox;
    @FXML
    private TextField alamatField;
    @FXML
    private TextField kotaField;
    @FXML
    private TextField provinsiField;
    @FXML
    private TextField noTeleponField;
    @FXML
    private ComboBox<String> pendidikanAkhirComboBox;
    @FXML
    private ComboBox<String> statusMenikahComboBox;
    @FXML
    private TextField tempatTinggalField;
    @FXML
    private ComboBox<String> sumberUangComboBox;
    @FXML
    private TextField nikField;
    @FXML
    private TextField noKKField;

    // Action Buttons
    @FXML
    private Button batalButton, simpanButton;

    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setupComboBoxes();
            setupNavigation();
            loadSessionData();
            populateFormData();
            setupFieldValidation();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            showAlert(Alert.AlertType.ERROR, "Initialization Error",
                    "Failed to initialize the form: " + e.getMessage());
        }
    }

    /**
     * Setup all ComboBox options with proper data
     */
    private void setupComboBoxes() {
        setupSemesterComboBox();
        setupAgamaComboBox();
        setupJenisKelaminComboBox();
        setupPendidikanComboBox();
        setupStatusMenikahComboBox();
        setupSumberUangComboBox();
    }

    private void setupSemesterComboBox() {
        if (semesterComboBox != null) {
            ObservableList<String> semesterOptions = FXCollections.observableArrayList();
            for (int i = 1; i <= MAX_SEMESTER; i++) {
                semesterOptions.add("Semester " + i);
            }
            semesterComboBox.setItems(semesterOptions);
        }
    }

    private void setupAgamaComboBox() {
        if (agamaComboBox != null) {
            ObservableList<String> agamaOptions = FXCollections.observableArrayList(
                    "Islam", "Kristen Protestan", "Kristen Katolik",
                    "Hindu", "Buddha", "Khonghucu", "Kepercayaan");
            agamaComboBox.setItems(agamaOptions);
        }
    }

    private void setupJenisKelaminComboBox() {
        if (jenisKelaminComboBox != null) {
            ObservableList<String> jenisKelaminOptions = FXCollections.observableArrayList("L", "P");
            jenisKelaminComboBox.setItems(jenisKelaminOptions);
        }
    }

    private void setupPendidikanComboBox() {
        if (pendidikanAkhirComboBox != null) {
            ObservableList<String> pendidikanOptions = FXCollections.observableArrayList(
                    "SD/Sederajat", "SMP/Sederajat", "SMA/SMK/Sederajat",
                    "D1", "D2", "D3", "D4/S1", "S2", "S3");
            pendidikanAkhirComboBox.setItems(pendidikanOptions);
        }
    }

    private void setupStatusMenikahComboBox() {
        if (statusMenikahComboBox != null) {
            ObservableList<String> statusMenikahOptions = FXCollections.observableArrayList(
                    "Belum Menikah", "Menikah", "Cerai Hidup", "Cerai Mati");
            statusMenikahComboBox.setItems(statusMenikahOptions);
        }
    }

    private void setupSumberUangComboBox() {
        if (sumberUangComboBox != null) {
            ObservableList<String> sumberUangOptions = FXCollections.observableArrayList(
                    // Family sources
                    "Orang Tua", "Wali", "Keluarga", "Ayah", "Ibu", "Kakek/Nenek", "Saudara",

                    // Government scholarships
                    "Beasiswa Pemerintah", "Beasiswa LPDP", "Beasiswa Bidikmisi",
                    "Beasiswa KIP Kuliah", "Beasiswa Unggulan", "Beasiswa Prestasi",
                    "Beasiswa Daerah", "Bantuan Pemerintah Daerah", "Dana BOS",

                    // Institutional support
                    "Beasiswa Institusi/Yayasan", "Beasiswa Perusahaan", "Beasiswa Bank",
                    "Beasiswa Organisasi Kemasyarakatan", "Beasiswa Alumni",
                    "Bantuan Sosial", "Zakat/Infaq/Sedekah",

                    // Self-funded
                    "Usaha Sendiri", "Kerja Sambilan", "Tabungan Sendiri",
                    "Investasi", "Bisnis Online",

                    // Loans
                    "Pinjaman Bank", "Kredit Pendidikan", "Pinjaman Koperasi",
                    "Pinjaman Keluarga", "Arisan",

                    // Others
                    "Campuran (Gabungan Beberapa Sumber)", "Lainnya");

            sumberUangComboBox.setItems(sumberUangOptions);
            sumberUangComboBox.setValue("Orang Tua"); // Default value
        }
    }

    /**
     * Setup navigation event handlers
     */
    private void setupNavigation() {
        if (homeButton != null)
            homeButton.setOnAction(e -> navigateToPage("Dashboard.fxml"));
        if (profilButton != null)
            profilButton.setOnAction(e -> navigateToPage("ProfilView.fxml"));
        if (khsButton != null)
            khsButton.setOnAction(e -> navigateToPage("KHSView.fxml"));
        if (settingButton != null)
            settingButton.setOnAction(e -> navigateToPage("SettingView.fxml"));
    }

    /**
     * Setup real-time field validation
     */
    private void setupFieldValidation() {
        // NIK validation
        if (nikField != null) {
            nikField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*") || newVal.length() > NIK_LENGTH) {
                    nikField.setText(oldVal);
                }
            });
        }

        // KK validation
        if (noKKField != null) {
            noKKField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*") || newVal.length() > KK_LENGTH) {
                    noKKField.setText(oldVal);
                }
            });
        }

        // Phone number validation
        if (noTeleponField != null) {
            noTeleponField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("[0-9+\\-()\\s]*")) {
                    noTeleponField.setText(oldVal);
                }
            });
        }
    }

    /**
     * Load session data and validate user authentication
     */
    private void loadSessionData() {
        currentUser = Session.getUser();
        currentMahasiswa = Session.getMahasiswa();

        if (currentUser == null) {
            showAlert(Alert.AlertType.WARNING, "Session Expired", "Please login again.");
            navigateToPage("Login.fxml");
            return;
        }

        updateUILabels();
    }

    /**
     * Update UI labels with current user data
     */
    private void updateUILabels() {
        if (currentUser != null) {
            setLabelText(namaMahasiswaLabel, currentUser.getNama());
            setLabelText(namaProfilLabel, currentUser.getNama());
            setLabelText(emailLabel, "Email: " + currentUser.getEmail());
        }

        if (currentMahasiswa != null) {
            setLabelText(nimLabel, "NIM: " + currentMahasiswa.getNim());
            setLabelText(jurusanLabel, "Jurusan: " +
                    (currentMahasiswa.getJurusan() != null ? currentMahasiswa.getJurusan() : "-"));
            setLabelText(prodiLabel, "Program Studi: " +
                    (currentMahasiswa.getProdi() != null ? currentMahasiswa.getProdi() : "-"));
        }
    }

    /**
     * Helper method to safely set label text
     */
    private void setLabelText(Label label, String text) {
        if (label != null && text != null) {
            label.setText(text);
        }
    }

    /**
     * Populate form fields with current mahasiswa data
     */
    private void populateFormData() {
        if (currentMahasiswa == null)
            return;

        try {
            setTextFieldValue(nimField, currentMahasiswa.getNim());
            setTextFieldValue(jurusanField, currentMahasiswa.getJurusan());
            setTextFieldValue(prodiField, currentMahasiswa.getProdi());
            setTextFieldValue(jalurSeleksiField, currentMahasiswa.getJalurSeleksi());
            setTextFieldValue(asalSekolahField, currentMahasiswa.getAsalSekolah());
            setTextFieldValue(alamatField, currentMahasiswa.getAlamat());
            setTextFieldValue(kotaField, currentMahasiswa.getKota());
            setTextFieldValue(provinsiField, currentMahasiswa.getProv());
            setTextFieldValue(noTeleponField, currentMahasiswa.getNoTelp());
            setTextFieldValue(tempatTinggalField, currentMahasiswa.getTempatTinggal());
            setTextFieldValue(nikField, currentMahasiswa.getNik());
            setTextFieldValue(noKKField, currentMahasiswa.getNoKk());

            setComboBoxValue(agamaComboBox, currentMahasiswa.getAgama());
            setComboBoxValue(jenisKelaminComboBox, currentMahasiswa.getJk());
            setComboBoxValue(pendidikanAkhirComboBox, currentMahasiswa.getPendidikanAkhir());
            setComboBoxValue(statusMenikahComboBox, currentMahasiswa.getStatusMenikah());
            setComboBoxValue(sumberUangComboBox, currentMahasiswa.getSumberUang());

            if (ttlField != null && currentMahasiswa.getTtl() != null) {
                ttlField.setValue(currentMahasiswa.getTtl().toLocalDate());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error populating form data", e);
        }
    }

    /**
     * Helper method to safely set TextField value
     */
    private void setTextFieldValue(TextField field, String value) {
        if (field != null && value != null) {
            field.setText(value);
        }
    }

    /**
     * Helper method to safely set ComboBox value
     */
    private void setComboBoxValue(ComboBox<String> comboBox, String value) {
        if (comboBox != null && value != null) {
            comboBox.setValue(value);
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        File file = fileChooser.showOpenDialog(uploadPhotoButton.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                if (profileImageView != null) {
                    profileImageView.setImage(image);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Photo uploaded successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading photo", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load photo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToPage("ProfilView.fxml");
    }

    @FXML
    private void handleSimpan() {
        if (!validateForm()) {
            return;
        }

        try {
            MahasiswaModel updatedMahasiswa = createMahasiswaFromForm();
            updateMahasiswaInDatabase(updatedMahasiswa);

            // Update session with new data
            Session.setMahasiswa(updatedMahasiswa);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Data saved successfully.");
            navigateToPage("ProfilView.fxml");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while saving", e);
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to save data to database: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while saving", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Create MahasiswaModel from form data
     */
    private MahasiswaModel createMahasiswaFromForm() {
        MahasiswaModel mahasiswa = new MahasiswaModel();

        mahasiswa.setNim(getTextFieldValue(nimField));
        mahasiswa.setJurusan(getTextFieldValue(jurusanField));
        mahasiswa.setProdi(getTextFieldValue(prodiField));
        mahasiswa.setJalurSeleksi(getTextFieldValue(jalurSeleksiField));
        mahasiswa.setAsalSekolah(getTextFieldValue(asalSekolahField));
        mahasiswa.setAlamat(getTextFieldValue(alamatField));
        mahasiswa.setKota(getTextFieldValue(kotaField));
        mahasiswa.setProv(getTextFieldValue(provinsiField));
        mahasiswa.setNoTelp(getTextFieldValue(noTeleponField));
        mahasiswa.setTempatTinggal(getTextFieldValue(tempatTinggalField));
        mahasiswa.setNik(getTextFieldValue(nikField));
        mahasiswa.setNoKk(getTextFieldValue(noKKField));

        mahasiswa.setAgama(getComboBoxValue(agamaComboBox));
        mahasiswa.setJk(getComboBoxValue(jenisKelaminComboBox));
        mahasiswa.setPendidikanAkhir(getComboBoxValue(pendidikanAkhirComboBox));
        mahasiswa.setStatusMenikah(getComboBoxValue(statusMenikahComboBox));
        mahasiswa.setSumberUang(getComboBoxValue(sumberUangComboBox));

        if (ttlField != null && ttlField.getValue() != null) {
            mahasiswa.setTtl(java.sql.Date.valueOf(ttlField.getValue()));
        }

        return mahasiswa;
    }

    /**
     * Helper method to safely get TextField value
     */
    private String getTextFieldValue(TextField field) {
        return (field != null && field.getText() != null) ? field.getText().trim() : "";
    }

    /**
     * Helper method to safely get ComboBox value
     */
    private String getComboBoxValue(ComboBox<String> comboBox) {
        return (comboBox != null) ? comboBox.getValue() : null;
    }

    /**
     * Update mahasiswa data in database
     */
    private void updateMahasiswaInDatabase(MahasiswaModel mahasiswa) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM mahasiswa WHERE nim = ?";
        String updateSql = """
                UPDATE mahasiswa SET
                    jurusan=?, prodi=?, jalur_seleksi=?, asal_sekolah=?, ttl=?,
                    agama=?, jk=?, alamat=?, kota=?, prov=?, no_telp=?,
                    pendidikan_akhir=?, status_menikah=?, tempat_tinggal=?,
                    sumber_uang=?, nik=?, no_kk=?
                WHERE nim=?
                """;
        String insertSql = """
                INSERT INTO mahasiswa
                    (nim, jurusan, prodi, jalur_seleksi, asal_sekolah, ttl, agama, jk,
                     alamat, kota, prov, no_telp, pendidikan_akhir, status_menikah,
                     tempat_tinggal, sumber_uang, nik, no_kk, id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            try {
                boolean exists = checkRecordExists(conn, checkSql, mahasiswa.getNim());

                if (exists) {
                    executeUpdate(conn, updateSql, mahasiswa);
                } else {
                    executeInsert(conn, insertSql, mahasiswa);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Check if mahasiswa record exists
     */
    private boolean checkRecordExists(Connection conn, String sql, String nim) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nim);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Execute update statement
     */
    private void executeUpdate(Connection conn, String sql, MahasiswaModel mahasiswa) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setMahasiswaParameters(stmt, mahasiswa, false);
            stmt.setString(18, mahasiswa.getNim()); // WHERE clause
            stmt.executeUpdate();
        }
    }

    /**
     * Execute insert statement
     */
    private void executeInsert(Connection conn, String sql, MahasiswaModel mahasiswa) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setMahasiswaParameters(stmt, mahasiswa, true);
            stmt.executeUpdate();
        }
    }

    /**
     * Set parameters for mahasiswa prepared statement
     */
    private void setMahasiswaParameters(PreparedStatement stmt, MahasiswaModel m, boolean isInsert)
            throws SQLException {
        int idx = 1;

        if (isInsert) {
            stmt.setString(idx++, m.getNim());
        }

        stmt.setString(idx++, m.getJurusan());
        stmt.setString(idx++, m.getProdi());
        stmt.setString(idx++, m.getJalurSeleksi());
        stmt.setString(idx++, m.getAsalSekolah());
        stmt.setDate(idx++, m.getTtl());
        stmt.setString(idx++, m.getAgama());
        stmt.setString(idx++, m.getJk());
        stmt.setString(idx++, m.getAlamat());
        stmt.setString(idx++, m.getKota());
        stmt.setString(idx++, m.getProv());
        stmt.setString(idx++, m.getNoTelp());
        stmt.setString(idx++, m.getPendidikanAkhir());
        stmt.setString(idx++, m.getStatusMenikah());
        stmt.setString(idx++, m.getTempatTinggal());
        stmt.setString(idx++, m.getSumberUang());
        stmt.setString(idx++, m.getNik());
        stmt.setString(idx++, m.getNoKk());

        if (isInsert) {
            stmt.setInt(idx++, Session.getUser().getId());
        }
    }

    @FXML
    private void handleBatal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Cancel changes?");
        alert.setContentText("Unsaved data will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigateToPage("ProfilView.fxml");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Sign out from account?");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.clear();
            navigateToPage("Login.fxml");
        }
    }

    /**
     * Comprehensive form validation
     */
    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        validateRequiredFields(errors);
        validateDataFormats(errors);
        validateBusinessRules(errors);

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Failed", String.join("\n", errors));
            return false;
        }

        return true;
    }

    /**
     * Validate required fields
     */
    private void validateRequiredFields(List<String> errors) {
        if (isFieldEmpty(nimField)) {
            errors.add("NIM must be filled.");
        }

        if (ttlField == null || ttlField.getValue() == null) {
            errors.add("Birth date must be filled.");
        }

        if (getComboBoxValue(agamaComboBox) == null) {
            errors.add("Religion must be selected.");
        }

        if (getComboBoxValue(jenisKelaminComboBox) == null) {
            errors.add("Gender must be selected.");
        }

        if (isFieldEmpty(alamatField)) {
            errors.add("Address must be filled.");
        }

        if (isFieldEmpty(kotaField)) {
            errors.add("City must be filled.");
        }

        if (isFieldEmpty(provinsiField)) {
            errors.add("Province must be filled.");
        }
    }

    /**
     * Validate data formats
     */
    private void validateDataFormats(List<String> errors) {
        String nik = getTextFieldValue(nikField);
        if (!nik.isEmpty() && (nik.length() != NIK_LENGTH || !nik.matches("\\d{16}"))) {
            errors.add("NIK must be exactly 16 digits.");
        }

        String noKK = getTextFieldValue(noKKField);
        if (!noKK.isEmpty() && (noKK.length() != KK_LENGTH || !noKK.matches("\\d{16}"))) {
            errors.add("Family Card number must be exactly 16 digits.");
        }

        String noTelp = getTextFieldValue(noTeleponField);
        if (!noTelp.isEmpty() && !noTelp.matches("[0-9+\\-()\\s]+")) {
            errors.add("Phone number format is invalid.");
        }
    }

    /**
     * Validate business rules
     */
    private void validateBusinessRules(List<String> errors) {
        if (ttlField != null && ttlField.getValue() != null) {
            if (ttlField.getValue().isAfter(LocalDate.now())) {
                errors.add("Birth date cannot be in the future.");
            }

            if (ttlField.getValue().isBefore(LocalDate.now().minusYears(100))) {
                errors.add("Birth date seems unrealistic.");
            }
        }
    }

    /**
     * Check if text field is empty
     */
    private boolean isFieldEmpty(TextField field) {
        return field == null || field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Navigate to specified FXML page
     */
    private void navigateToPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/sinilai/view/" + fxml));
            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed to " + fxml, e);
            showAlert(Alert.AlertType.ERROR, "Navigation Failed",
                    "Failed to open page: " + e.getMessage());
        }
    }

    /**
     * Get current stage from any component
     */
    private Stage getCurrentStage() {
        if (simpanButton != null && simpanButton.getScene() != null) {
            return (Stage) simpanButton.getScene().getWindow();
        }
        return null;
    }

    /**
     * Show alert dialog with specified parameters
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}