package com.sinilai.controller;

import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModalDataDosenController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ModalDataDosenController.class.getName());

    // Constants
    private static final int NIDN_LENGTH = 10;
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 100;

    @FXML
    private TextField nidnField;
    @FXML
    private TextField tempatLahirField;
    @FXML
    private DatePicker tanggalLahirPicker;
    @FXML
    private ComboBox<String> jkComboBox;
    @FXML
    private ComboBox<String> agamaComboBox;
    @FXML
    private Button batalButton;
    @FXML
    private Button simpanButton;

    private UserModel currentUser;
    private boolean dataSaved = false;
    private boolean isLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupValidation();
        setupDatePicker();
    }

    private void setupComboBoxes() {
        // Setup Jenis Kelamin ComboBox
        jkComboBox.getItems().addAll("Laki-laki", "Perempuan");
        jkComboBox.setPromptText("Pilih jenis kelamin");

        // Setup Agama ComboBox
        agamaComboBox.getItems().addAll(
                "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu");
        agamaComboBox.setPromptText("Pilih agama");
    }

    private void setupValidation() {
        // Validasi NIDN - hanya angka dengan validasi real-time
        nidnField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nidnField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // Batasi maksimal 10 digit
            if (newValue.length() > NIDN_LENGTH) {
                nidnField.setText(newValue.substring(0, NIDN_LENGTH));
            }
        });

        // Validasi Tempat Lahir - huruf, spasi, dan beberapa karakter khusus
        tempatLahirField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s.-]*")) {
                tempatLahirField.setText(newValue.replaceAll("[^a-zA-Z\\s.-]", ""));
            }
            // Batasi panjang maksimal
            if (newValue.length() > 50) {
                tempatLahirField.setText(newValue.substring(0, 50));
            }
        });
    }

    private void setupDatePicker() {
        // Set batas tanggal lahir yang masuk akal
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.minusYears(MIN_AGE); // Minimal 18 tahun
        LocalDate minDate = today.minusYears(MAX_AGE); // Maksimal 100 tahun

        tanggalLahirPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(maxDate) || date.isBefore(minDate));
            }
        });
    }

    public void setUser(UserModel user) {
        this.currentUser = user;
        loadExistingData();
    }

    private void loadExistingData() {
        if (currentUser == null)
            return;

        setLoading(true);

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadDataFromDatabase();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> setLoading(false));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    LOGGER.log(Level.SEVERE, "Error loading existing data", getException());
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data existing");
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void loadDataFromDatabase() throws SQLException {
        String sql = "SELECT nidn, tempat_lahir, tanggal_lahir, jk, agama FROM dosen WHERE user_id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extract data from ResultSet first
                    String nidn = rs.getString("nidn");
                    String tempatLahir = rs.getString("tempat_lahir");
                    Date tanggalLahir = rs.getDate("tanggal_lahir");
                    String jk = rs.getString("jk");
                    String agama = rs.getString("agama");

                    // Then update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        if (nidn != null && !nidn.trim().isEmpty()) {
                            nidnField.setText(nidn);
                        }
                        if (tempatLahir != null && !tempatLahir.trim().isEmpty()) {
                            tempatLahirField.setText(tempatLahir);
                        }
                        if (tanggalLahir != null) {
                            tanggalLahirPicker.setValue(tanggalLahir.toLocalDate());
                        }
                        if (jk != null && !jk.trim().isEmpty()) {
                            jkComboBox.setValue(jk);
                        }
                        if (agama != null && !agama.trim().isEmpty()) {
                            agamaComboBox.setValue(agama);
                        }
                    });
                }
            }
        }
    }

    @FXML
    private void handleSimpan() {
        if (isLoading)
            return;

        if (!validateInput()) {
            return;
        }

        setLoading(true);

        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isDosenExists()) {
                    updateDosenData();
                } else {
                    insertDosenData();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    dataSaved = true;
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data dosen berhasil disimpan!");
                    closeModal();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    LOGGER.log(Level.SEVERE, "Error saving dosen data", getException());
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan data: " + getException().getMessage());
                });
            }
        };

        new Thread(saveTask).start();
    }

    @FXML
    private void handleBatal() {
        if (hasUnsavedChanges()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Konfirmasi");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Ada perubahan yang belum disimpan. Yakin ingin keluar?");
            confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            if (confirmAlert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                closeModal();
            }
        } else {
            closeModal();
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Validasi NIDN
        String nidn = nidnField.getText().trim();
        if (nidn.isEmpty()) {
            errors.append("• NIDN tidak boleh kosong\n");
        } else if (nidn.length() != NIDN_LENGTH) {
            errors.append("• NIDN harus tepat ").append(NIDN_LENGTH).append(" digit\n");
        } else if (!nidn.matches("\\d{" + NIDN_LENGTH + "}")) {
            errors.append("• NIDN harus berupa angka\n");
        }

        // Validasi Tempat Lahir
        String tempatLahir = tempatLahirField.getText().trim();
        if (tempatLahir.isEmpty()) {
            errors.append("• Tempat lahir tidak boleh kosong\n");
        } else if (tempatLahir.length() < 2) {
            errors.append("• Tempat lahir minimal 2 karakter\n");
        }

        // Validasi Tanggal Lahir
        LocalDate tanggalLahir = tanggalLahirPicker.getValue();
        if (tanggalLahir == null) {
            errors.append("• Tanggal lahir tidak boleh kosong\n");
        } else {
            LocalDate today = LocalDate.now();
            if (tanggalLahir.isAfter(today)) {
                errors.append("• Tanggal lahir tidak boleh di masa depan\n");
            } else {
                int age = Period.between(tanggalLahir, today).getYears();
                if (age < MIN_AGE) {
                    errors.append("• Usia minimal ").append(MIN_AGE).append(" tahun\n");
                } else if (age > MAX_AGE) {
                    errors.append("• Usia tidak boleh lebih dari ").append(MAX_AGE).append(" tahun\n");
                }
            }
        }

        // Validasi Jenis Kelamin
        if (jkComboBox.getValue() == null || jkComboBox.getValue().trim().isEmpty()) {
            errors.append("• Jenis kelamin harus dipilih\n");
        }

        // Validasi Agama
        if (agamaComboBox.getValue() == null || agamaComboBox.getValue().trim().isEmpty()) {
            errors.append("• Agama harus dipilih\n");
        }

        // Cek duplikasi NIDN
        if (!nidn.isEmpty() && nidn.length() == NIDN_LENGTH && isNidnExists(nidn)) {
            errors.append("• NIDN sudah digunakan oleh dosen lain\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Data Tidak Valid", errors.toString());
            return false;
        }

        return true;
    }

    private boolean isNidnExists(String nidn) {
        String sql = "SELECT COUNT(*) FROM dosen WHERE nidn = ? AND user_id != ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nidn);
            stmt.setInt(2, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking NIDN existence", e);
        }

        return false;
    }

    private boolean isDosenExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dosen WHERE user_id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private void insertDosenData() throws SQLException {
        String sql = "INSERT INTO dosen (user_id, nidn, tempat_lahir, tanggal_lahir, jk, agama) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            stmt.setString(2, nidnField.getText().trim());
            stmt.setString(3, tempatLahirField.getText().trim());
            stmt.setDate(4, Date.valueOf(tanggalLahirPicker.getValue()));
            stmt.setString(5, jkComboBox.getValue());
            stmt.setString(6, agamaComboBox.getValue());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Dosen data inserted successfully for user: " + currentUser.getId());
            } else {
                throw new SQLException("No rows affected during insert");
            }
        }
    }

    private void updateDosenData() throws SQLException {
        String sql = "UPDATE dosen SET nidn = ?, tempat_lahir = ?, tanggal_lahir = ?, jk = ?, agama = ? WHERE user_id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nidnField.getText().trim());
            stmt.setString(2, tempatLahirField.getText().trim());
            stmt.setDate(3, Date.valueOf(tanggalLahirPicker.getValue()));
            stmt.setString(4, jkComboBox.getValue());
            stmt.setString(5, agamaComboBox.getValue());
            stmt.setInt(6, currentUser.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Dosen data updated successfully for user: " + currentUser.getId());
            } else {
                throw new SQLException("No rows affected during update");
            }
        }
    }

    private boolean hasUnsavedChanges() {
        return !nidnField.getText().trim().isEmpty() ||
                !tempatLahirField.getText().trim().isEmpty() ||
                tanggalLahirPicker.getValue() != null ||
                jkComboBox.getValue() != null ||
                agamaComboBox.getValue() != null;
    }

    private void setLoading(boolean loading) {
        this.isLoading = loading;
        simpanButton.setDisable(loading);
        batalButton.setDisable(loading);

        if (loading) {
            simpanButton.setText("Menyimpan...");
        } else {
            simpanButton.setText("Simpan");
        }
    }

    private void closeModal() {
        Stage stage = (Stage) simpanButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Set appropriate icon based on alert type
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (stage != null) {
            stage.setAlwaysOnTop(true);
        }

        alert.showAndWait();
    }

    public boolean isDataSaved() {
        return dataSaved;
    }
}