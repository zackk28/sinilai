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
import java.util.ResourceBundle;

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
    private TextField tempatTglLahirField;
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
    private String currentNim;
    private String originalImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupNavigationButtons();
        loadUserData();
        setupChoiceBox();
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
        homeButton.setOnAction(e -> handleNavigation("Home.fxml"));
        profilButton.setOnAction(e -> handleNavigation("Profile.fxml"));
        khsButton.setOnAction(e -> handleNavigation("KHS.fxml"));
        settingButton.setOnAction(e -> handleNavigation("Setting.fxml"));
    }

    private void setupChoiceBox() {
        ObservableList<String> choiceItems = FXCollections.observableArrayList(
                "Profile", "Logout");
        choiceBox.setItems(choiceItems);

        choiceBox.setOnAction(e -> {
            String selected = choiceBox.getSelectionModel().getSelectedItem();
            if ("Logout".equals(selected)) {
                handleLogout();
            } else if ("Profile".equals(selected)) {
                handleNavigation("Profile.fxml");
            }
        });
    }

    private void loadUserData() {
        // Simulate loading user data from database or session
        // In real implementation, you would get this from your data service

        // Sample data - replace with actual data loading logic
        namaMahasiswaLabel.setText("John Doe");
        namaProfilLabel.setText("John Doe");
        nimLabel.setText("NIM: 2024001001");
        emailLabel.setText("Email: john.doe@student.pnp.ac.id");
        jurusanLabel.setText("Jurusan: Teknologi Informasi");
        prodiLabel.setText("Program Studi: Teknik Informatika");

        currentNim = "2024001001";

        // Load existing form data if available
        loadExistingFormData();
    }

    private void loadExistingFormData() {
        // Simulate loading existing form data
        // In real implementation, fetch from database based on currentNim

        // Sample existing data
        tempatTglLahirField.setText("Padang, 15 Januari 2000");
        nomorKKField.setText("1371010101010001");
        agamaComboBox.setValue("Islam");
        nikField.setText("1371010101010001");
        jenisKelaminComboBox.setValue("Laki-laki");
        sumberUangSekolahComboBox.setValue("Orang Tua");
        alamatSekarangField.setText("Jl. Sudirman No. 123");
        pendidikanTerakhirComboBox.setValue("SMA/SMK");
        kotaKabupatenField.setText("Padang");
        nomorTeleponField.setText("08123456789");
        provinsiField.setText("Sumatera Barat");
        statusMenikahComboBox.setValue("Belum Menikah");
        statusTempatTinggalField.setText("Kos");
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
                originalImagePath = selectedFile.getAbsolutePath();

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
                // Simulate saving data to database
                saveUserData();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil disimpan!");

                // Navigate back to profile page
                handleNavigation("Profile.fxml");

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
            handleNavigation("Profile.fxml");
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
        StringBuilder errors = new StringBuilder();

        // Validate required fields
        if (tempatTglLahirField.getText().trim().isEmpty()) {
            errors.append("- Tempat/Tanggal Lahir harus diisi\n");
        }

        if (nikField.getText().trim().isEmpty()) {
            errors.append("- NIK harus diisi\n");
        } else if (nikField.getText().trim().length() != 16) {
            errors.append("- NIK harus 16 digit\n");
        }

        if (nomorKKField.getText().trim().isEmpty()) {
            errors.append("- Nomor KK harus diisi\n");
        } else if (nomorKKField.getText().trim().length() != 16) {
            errors.append("- Nomor KK harus 16 digit\n");
        }

        if (agamaComboBox.getValue() == null) {
            errors.append("- Agama harus dipilih\n");
        }

        if (jenisKelaminComboBox.getValue() == null) {
            errors.append("- Jenis Kelamin harus dipilih\n");
        }

        if (alamatSekarangField.getText().trim().isEmpty()) {
            errors.append("- Alamat Sekarang harus diisi\n");
        }

        if (kotaKabupatenField.getText().trim().isEmpty()) {
            errors.append("- Kota/Kabupaten harus diisi\n");
        }

        if (provinsiField.getText().trim().isEmpty()) {
            errors.append("- Provinsi harus diisi\n");
        }

        if (nomorTeleponField.getText().trim().isEmpty()) {
            errors.append("- Nomor Telepon harus diisi\n");
        } else if (!nomorTeleponField.getText().trim().matches("^[0-9+()-\\s]+$")) {
            errors.append("- Format Nomor Telepon tidak valid\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal",
                    "Mohon perbaiki kesalahan berikut:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void saveUserData() {
        // In real implementation, save to database
        // This is just a simulation

        System.out.println("Saving user data for NIM: " + currentNim);
        System.out.println("Tempat/Tgl Lahir: " + tempatTglLahirField.getText());
        System.out.println("NIK: " + nikField.getText());
        System.out.println("Nomor KK: " + nomorKKField.getText());
        System.out.println("Agama: " + agamaComboBox.getValue());
        System.out.println("Jenis Kelamin: " + jenisKelaminComboBox.getValue());
        System.out.println("Alamat: " + alamatSekarangField.getText());
        System.out.println("Kota/Kabupaten: " + kotaKabupatenField.getText());
        System.out.println("Provinsi: " + provinsiField.getText());
        System.out.println("Nomor Telepon: " + nomorTeleponField.getText());
        System.out.println("Sumber Uang Sekolah: " + sumberUangSekolahComboBox.getValue());
        System.out.println("Pendidikan Terakhir: " + pendidikanTerakhirComboBox.getValue());
        System.out.println("Status Menikah: " + statusMenikahComboBox.getValue());
        System.out.println("Status Tempat Tinggal: " + statusTempatTinggalField.getText());

        if (originalImagePath != null) {
            System.out.println("Image Path: " + originalImagePath);
        }

        // Here you would typically:
        // 1. Create a User/Student object with the form data
        // 2. Call your database service to update the record
        // 3. Handle any database exceptions
    }

    private void clearSession() {
        // Clear any session data, user preferences, etc.
        currentNim = null;
        originalImagePath = null;
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
        return currentNim;
    }

    public void setCurrentNim(String nim) {
        this.currentNim = nim;
        loadUserData(); // Reload data when NIM changes
    }
}