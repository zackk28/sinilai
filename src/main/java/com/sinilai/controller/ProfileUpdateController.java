package com.sinilai.controller;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import com.sinilai.utils.Session;

public class ProfileUpdateController implements Initializable {

    // Header Components
    @FXML
    private Label namaMahasiswaLabel;
    @FXML
    private ChoiceBox<String> choiceBox;

    // Navigation Components
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
    @FXML
    private Button backButton;

    // Profile Info Components
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button uploadPhotoButton;
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

    // Form Components
    @FXML
    private DatePicker tempatTglLahirField;
    @FXML
    private TextField nomorKKField;
    @FXML
    private ComboBox<String> agamaComboBox;
    @FXML
    private TextField nikField;
    @FXML
    private ComboBox<String> jenisKelaminComboBox;
    @FXML
    private ComboBox<String> sumberUangSekolahComboBox;
    @FXML
    private TextField alamatSekarangField;
    @FXML
    private ComboBox<String> pendidikanTerakhirComboBox;
    @FXML
    private TextField kotaKabupatenField;
    @FXML
    private TextField nomorTeleponField;
    @FXML
    private TextField provinsiField;
    @FXML
    private ComboBox<String> statusMenikahComboBox;
    @FXML
    private TextField statusTempatTinggalField;

    // Action Buttons
    @FXML
    private Button batalButton;
    @FXML
    private Button simpanButton;

    // Data storage for current user
    // private String originalImagePath;
    private MahasiswaModel currentMahasiswa;
    private UserModel currentUser;

    private static final Logger LOGGER = Logger.getLogger(ProfileUpdateController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupNavigationButtons();
        setupChoiceBox();
        loadSessionData();
    }

    private void loadSessionData() {
        currentUser = Session.getUser();
        currentMahasiswa = Session.getMahasiswa();

        if (currentUser != null) {
            if (currentMahasiswa != null) {
                loadUserData(); // hanya load jika mahasiswa tidak null
            } else {
                // hanya isi nama/email, dll dari user
                namaMahasiswaLabel.setText(currentUser.getNama());
                namaProfilLabel.setText(currentUser.getNama());
                emailLabel.setText("Email: " + currentUser.getEmail());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Sesi Habis", "Silakan login ulang.");
            handleNavigation("LoginView.fxml");
        }
    }

    private void setupComboBoxes() {
        // Setup Agama ComboBox
        ObservableList<String> agamaList = FXCollections.observableArrayList(
                "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Khonghucu");
        agamaComboBox.setItems(agamaList);

        // Setup Jenis Kelamin ComboBox
        ObservableList<String> jenisKelaminList = FXCollections.observableArrayList(
                "Laki-laki", "Perempuan");
        jenisKelaminComboBox.setItems(jenisKelaminList);

        // Setup Sumber Uang Sekolah ComboBox
        ObservableList<String> sumberUangSekolahList = FXCollections.observableArrayList(
                "Orang Tua", "Beasiswa", "Pinjaman", "Kerja Sambilan", "Lainnya");
        sumberUangSekolahComboBox.setItems(sumberUangSekolahList);

        // Setup Pendidikan Terakhir ComboBox
        ObservableList<String> pendidikanTerakhirList = FXCollections.observableArrayList(
                "SMA/SMK", "D3", "S1", "S2", "S3");
        pendidikanTerakhirComboBox.setItems(pendidikanTerakhirList);

        // Setup Status Menikah ComboBox
        ObservableList<String> statusMenikahList = FXCollections.observableArrayList(
                "Belum Menikah", "Menikah", "Cerai");
        statusMenikahComboBox.setItems(statusMenikahList);
    }

    private void setupNavigationButtons() {
        homeButton.setOnAction(e -> handleNavigation("Dashboard.fxml"));
        profilButton.setOnAction(e -> handleNavigation("ProfileView.fxml"));
        khsButton.setOnAction(e -> handleNavigation("KHSView.fxml"));
        settingButton.setOnAction(e -> handleNavigation("SettingView.fxml"));
    }

    private void setupChoiceBox() {
        if (choiceBox == null) {
            System.err.println("❌ ERROR: choiceBox masih null di setupChoiceBox()");
            return;
        }

        ObservableList<String> choiceItems = FXCollections.observableArrayList("Profile", "Logout");
        choiceBox.setItems(choiceItems);

        choiceBox.setOnAction(e -> {
            String selected = choiceBox.getSelectionModel().getSelectedItem();
            if ("Logout".equals(selected)) {
                handleLogout();
            } else if ("Profile".equals(selected)) {
                handleNavigation("ProfileView.fxml");
            }
        });
    }

    private void loadUserData() {

        loadUserProfile();
        loadFormData();
    }

    private void loadUserProfile() {
        if (namaMahasiswaLabel != null)
            namaMahasiswaLabel.setText(currentUser.getNama());
        if (namaProfilLabel != null)
            namaProfilLabel.setText(currentUser.getNama());
        if (nimLabel != null)
            nimLabel.setText("NIM: " + currentMahasiswa.getNim());
        if (emailLabel != null)
            emailLabel.setText("Email: " + currentUser.getEmail());
        if (jurusanLabel != null)
            jurusanLabel.setText("Jurusan: " + currentMahasiswa.getJurusan());
        if (prodiLabel != null)
            prodiLabel.setText("Program Studi: " + currentMahasiswa.getProdi());
    }

    private void loadFormData() {
        MahasiswaModel mhs = getMahasiswaFromDB();

        if (mhs == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Data mahasiswa tidak ditemukan.");
            return;
        }

        tempatTglLahirField.setValue(mhs.getTtl().toLocalDate());
        nomorKKField.setText(mhs.getNoKk());
        agamaComboBox.setValue(mhs.getAgama());
        nikField.setText(mhs.getNik());
        jenisKelaminComboBox.setValue(mhs.getJkLabel());
        sumberUangSekolahComboBox.setValue(mhs.getSumberUang());
        alamatSekarangField.setText(mhs.getAlamat());
        pendidikanTerakhirComboBox.setValue(mhs.getPendidikanAkhir());
        kotaKabupatenField.setText(mhs.getKota());
        nomorTeleponField.setText(mhs.getNoTelp());
        provinsiField.setText(mhs.getProv());
        statusMenikahComboBox.setValue(mhs.getStatusMenikah());
        statusTempatTinggalField.setText(mhs.getTempatTinggal());
    }

    private MahasiswaModel getMahasiswaFromDB() {
        String sql = "SELECT * FROM mahasiswa WHERE nim = ?";
        MahasiswaModel mhs = null;

        try (Connection conn = com.sinilai.utils.Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, Session.getMahasiswa().getNim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                mhs = new MahasiswaModel();
                mhs.setNim(rs.getString("nim"));
                mhs.setTtl(rs.getDate("ttl"));
                mhs.setNik(rs.getString("nik"));
                mhs.setNoKk(rs.getString("no_kk"));
                mhs.setAgama(rs.getString("agama"));
                mhs.setJk(rs.getString("jk"));
                mhs.setAlamat(rs.getString("alamat"));
                mhs.setKota(rs.getString("kota"));
                mhs.setProv(rs.getString("prov"));
                mhs.setNoTelp(rs.getString("no_telp"));
                mhs.setSumberUang(rs.getString("sumber_uang"));
                mhs.setPendidikanAkhir(rs.getString("pendidikan_akhir"));
                mhs.setStatusMenikah(rs.getString("status_menikah"));
                mhs.setTempatTinggal(rs.getString("tempat_tinggal"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil data mahasiswa dari DB", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Terjadi kesalahan saat mengambil data mahasiswa: " + e.getMessage());
        }

        return mhs;
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");

        // Set extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);

        Stage stage = (Stage) uploadPhotoButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                // originalImagePath = selectedFile.getAbsolutePath();

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Foto berhasil diupload!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal mengupload foto: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSimpan() {
        if (validateForm()) {
            try {
                LocalDate tanggal = tempatTglLahirField.getValue();
                if (tanggal == null) {
                    showAlert(Alert.AlertType.WARNING, "Format Salah", "Tanggal lahir belum dipilih.");
                    return;
                }

                java.sql.Date sqlDate = java.sql.Date.valueOf(tanggal);

                MahasiswaModel updated = new MahasiswaModel();
                updated.setNim("03030300556"); // ganti nanti
                updated.setTtl(sqlDate);
                updated.setNik(nikField.getText());
                updated.setNoKk(nomorKKField.getText());
                updated.setAgama(agamaComboBox.getValue());
                String jkPilihan = jenisKelaminComboBox.getValue();
                System.out.println("Pilihan ComboBox: " + jkPilihan); // debug
                if ("Laki-laki".equals(jkPilihan)) {
                    updated.setJk("L");
                } else if ("Perempuan".equals(jkPilihan)) {
                    updated.setJk("P");
                } else {
                    updated.setJk(null); // atau tampilkan alert kalau wajib dipilih
                }
                updated.setAlamat(alamatSekarangField.getText());
                updated.setKota(kotaKabupatenField.getText());
                updated.setProv(provinsiField.getText());
                updated.setNoTelp(nomorTeleponField.getText());
                updated.setSumberUang(sumberUangSekolahComboBox.getValue());
                updated.setPendidikanAkhir(pendidikanTerakhirComboBox.getValue());
                updated.setStatusMenikah(statusMenikahComboBox.getValue());
                updated.setTempatTinggal(statusTempatTinggalField.getText());

                System.out.println("DATA YANG AKAN DISIMPAN:");
                System.out.println("NIM: " + updated.getNim());
                System.out.println("TTL: " + updated.getTtl());
                System.out.println("NIK: " + updated.getNik());
                System.out.println("No KK: " + updated.getNoKk());
                System.out.println("Agama: " + updated.getAgama());
                System.out.println("JK: " + updated.getJk());
                System.out.println("Alamat: " + updated.getAlamat());
                System.out.println("Kota: " + updated.getKota());
                System.out.println("Provinsi: " + updated.getProv());
                System.out.println("No Telp: " + updated.getNoTelp());
                System.out.println("Sumber Uang: " + updated.getSumberUang());
                System.out.println("Pendidikan Akhir: " + updated.getPendidikanAkhir());
                System.out.println("Status Menikah: " + updated.getStatusMenikah());
                System.out.println("Tempat Tinggal: " + updated.getTempatTinggal());

                updateAtauInsertMahasiswaToDB(updated);

                handleNavigation("ProfilView.fxml");

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBatal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText("Batalkan Perubahan?");
        alert.setContentText("Apakah Anda yakin ingin membatalkan perubahan? Data yang belum disimpan akan hilang.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            handleNavigation("ProfilView.fxml");
        }
    }

    @FXML
    private void handleBack() {
        handleBatal(); // Same logic as cancel button
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Logout dari Aplikasi?");
        alert.setContentText("Apakah Anda yakin ingin logout?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Clear session data
                clearSession();

                // Navigate to login page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal logout: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        List<String> errors = collectValidationErrors();

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal",
                    "Mohon perbaiki kesalahan berikut:\n\n" + String.join("\n", errors));
            return false;
        }
        return true;
    }

    private List<String> collectValidationErrors() {
        List<String> errors = new ArrayList<>();

        if (isEmpty(tempatTglLahirField))
            errors.add("- Tempat/Tanggal Lahir harus diisi");
        if (!isValidNIK(nikField.getText()))
            errors.add("- NIK harus 16 digit");
        if (!isValidNomorKK(nomorKKField.getText()))
            errors.add("- Nomor KK harus 16 digit");
        if (agamaComboBox.getValue() == null)
            errors.add("- Agama harus dipilih");
        if (jenisKelaminComboBox.getValue() == null)
            errors.add("- Jenis Kelamin harus dipilih");
        if (isEmpty(alamatSekarangField))
            errors.add("- Alamat Sekarang harus diisi");
        if (isEmpty(kotaKabupatenField))
            errors.add("- Kota/Kabupaten harus diisi");
        if (isEmpty(provinsiField))
            errors.add("- Provinsi harus diisi");
        if (!isValidNomorTelepon(nomorTeleponField.getText()))
            errors.add("- Format Nomor Telepon tidak valid");

        return errors;
    }

    private boolean isEmpty(DatePicker picker) {
        return picker.getValue() == null;
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private boolean isValidNIK(String nik) {
        return nik != null && nik.trim().length() == 16;
    }

    private boolean isValidNomorKK(String kk) {
        return kk != null && kk.trim().length() == 16;
    }

    private boolean isValidNomorTelepon(String nomor) {
        return nomor != null && nomor.trim().matches("^[0-9+()\\s-]+$");
    }

    private void updateAtauInsertMahasiswaToDB(MahasiswaModel mhs) {
        if (mhs.getNim() == null || mhs.getNim().isEmpty()) {
            UserModel currentUser = Session.getUser();
            if (currentUser != null) {
                mhs.setNim(currentUser.getNama());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User belum login. Tidak bisa menyimpan data.");
                return;
            }
        }

        String updateSql = "UPDATE mahasiswa SET ttl = ?, nik = ?, no_kk = ?, agama = ?, jk = ?, alamat = ?, kota = ?, prov = ?, no_telp = ?, sumber_uang = ?, pendidikan_akhir = ?, status_menikah = ?, tempat_tinggal = ? WHERE nim = ?";
        String insertSql = "INSERT INTO mahasiswa (ttl, nik, no_kk, agama, jk, alamat, kota, prov, no_telp, sumber_uang, pendidikan_akhir, status_menikah, tempat_tinggal, id, nim) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection()) {
            // Coba UPDATE dulu
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                isiStatementMahasiswa(stmt, mhs, 1); // isi kolom 1..14
                stmt.setString(14, mhs.getNim()); // param 15 = WHERE nim = ?
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil diperbarui.");
                    Session.setMahasiswa(mhs);
                    return;
                }
            }

            // Kalau tidak ada baris yang kena update, lakukan INSERT
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                isiStatementMahasiswa(stmt, mhs, 1); // isi param 1..14
                stmt.setString(15, mhs.getNim()); // param 15 = nim
                int inserted = stmt.executeUpdate();
                if (inserted > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil ditambahkan.");
                    Session.setMahasiswa(mhs);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Gagal", "Data tidak berhasil ditambahkan.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error", "Kesalahan saat menyimpan ke database:\n" + e.getMessage());
        }
    }

    private void isiStatementMahasiswa(PreparedStatement stmt, MahasiswaModel mhs, int startIndex) throws SQLException {
        stmt.setDate(startIndex, mhs.getTtl());
        stmt.setString(startIndex + 1, mhs.getNik());
        stmt.setString(startIndex + 2, mhs.getNoKk());
        stmt.setString(startIndex + 3, mhs.getAgama());
        stmt.setString(startIndex + 4, mhs.getJk());
        stmt.setString(startIndex + 5, mhs.getAlamat());
        stmt.setString(startIndex + 6, mhs.getKota());
        stmt.setString(startIndex + 7, mhs.getProv());
        stmt.setString(startIndex + 8, mhs.getNoTelp());
        stmt.setString(startIndex + 9, mhs.getSumberUang());
        stmt.setString(startIndex + 10, mhs.getPendidikanAkhir());
        stmt.setString(startIndex + 11, mhs.getStatusMenikah());
        stmt.setString(startIndex + 12, mhs.getTempatTinggal());

        UserModel user = Session.getUser();
        stmt.setInt(startIndex + 13, user.getId()); // ID di akhir
    }

    private void clearSession() {
        // Clear any session data, user preferences, etc.
        currentMahasiswa = null;
        // originalImagePath = null;
    }

    private void handleNavigation(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) homeButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getter methods for accessing current data (if needed by other controllers)
    public String getCurrentNim() {
        return currentMahasiswa != null ? currentMahasiswa.getNim() : null;
    }

    public void setMahasiswa(MahasiswaModel mahasiswa) {
        this.currentMahasiswa = mahasiswa;
        loadUserData();
    }
}