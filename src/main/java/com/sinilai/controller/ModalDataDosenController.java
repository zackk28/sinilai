package com.sinilai.controller;

import com.sinilai.model.DosenModel;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModalDataDosenController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ModalDataDosenController.class.getName());

    // Thread pool for background tasks
    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    // Constants
    private static final int NIDN_LENGTH = 10;

    @FXML
    private TextField nidnField;
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
    private DosenModel currentDosen;
    private boolean dataSaved = false;
    private boolean isLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupEventHandlers();
    }

    public void setUser(UserModel user) {
        this.currentUser = user;
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    public void setDosen(DosenModel dosen) {
        this.currentDosen = dosen;
        if (dosen != null) {
            Platform.runLater(() -> {
                nidnField.setText(dosen.getNidn());
                if (dosen.getTtl() != null) {
                    tanggalLahirPicker.setValue(dosen.getTtl().toLocalDate());
                }
                jkComboBox.setValue(convertJkToDisplay(dosen.getJk()));
                agamaComboBox.setValue(dosen.getAgama());
            });
        }
    }

    public boolean isDataSaved() {
        return dataSaved;
    }

    private String convertJkToDisplay(String jk) {
        if (jk == null)
            return null;
        return jk.equalsIgnoreCase("L") ? "Laki-laki" : "Perempuan";
    }

    private String convertJkToDatabase(String jk) {
        if (jk == null)
            return null;
        return jk.equalsIgnoreCase("Laki-laki") ? "L" : "P";
    }

    private void setupComboBoxes() {
        jkComboBox.getItems().addAll("Laki-laki", "Perempuan");
        jkComboBox.setPromptText("Pilih jenis kelamin");

        agamaComboBox.getItems().addAll(
                "Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu");
        agamaComboBox.setPromptText("Pilih agama");
    }

    private void setupEventHandlers() {
        simpanButton.setOnAction(e -> handleSimpan());
        batalButton.setOnAction(e -> handleBatal());

        Platform.runLater(() -> {
            Stage stage = (Stage) simpanButton.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                if (!dataSaved) {
                    e.consume();
                    handleBatal();
                }
            });
        });
    }

    @FXML
    private void handleSimpan() {
        if (validateInput()) {
            setLoading(true);

            Task<Boolean> saveTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return saveDosenData();
                }

                @Override
                protected void succeeded() {
                    dataSaved = true;
                    setLoading(false);
                    closeWindow();
                }

                @Override
                protected void failed() {
                    setLoading(false);
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Gagal menyimpan data: " + getException().getMessage());
                }
            };

            executorService.submit(saveTask);
        }
    }

    @FXML
    private void handleBatal() {
        if (dataSaved || !hasChanges()) {
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi");
            alert.setHeaderText(null);
            alert.setContentText("Ada perubahan yang belum disimpan. Yakin ingin keluar?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    closeWindow();
                }
            });
        }
    }

    private boolean validateInput() {
        // Validate NIDN
        String nidn = nidnField.getText().trim();
        if (nidn.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "NIDN tidak boleh kosong");
            return false;
        }
        if (nidn.length() != NIDN_LENGTH || !nidn.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Peringatan",
                    "NIDN harus terdiri dari " + NIDN_LENGTH + " digit angka");
            return false;
        }

        // Validate date of birth
        if (tanggalLahirPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tanggal lahir harus diisi");
            return false;
        }

        // Validate gender
        if (jkComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jenis kelamin harus dipilih");
            return false;
        }

        // Validate religion
        if (agamaComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Agama harus dipilih");
            return false;
        }

        return true;
    }

    private boolean hasChanges() {
        if (currentDosen == null)
            return true;

        return !nidnField.getText().trim().equals(currentDosen.getNidn()) ||
                !tanggalLahirPicker.getValue().equals(currentDosen.getTtl().toLocalDate()) ||
                !jkComboBox.getValue().equals(convertJkToDisplay(currentDosen.getJk())) ||
                !agamaComboBox.getValue().equals(currentDosen.getAgama());
    }

    private boolean saveDosenData() throws SQLException {
        String nidn = nidnField.getText().trim();
        LocalDate ttl = tanggalLahirPicker.getValue();
        String jk = convertJkToDatabase(jkComboBox.getValue());
        String agama = agamaComboBox.getValue();

        if (currentDosen == null) {
            // Insert new record
            String sql = "INSERT INTO dosen (id, nidn, ttl, jk, agama) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = Koneksi.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, currentUser.getId());
                stmt.setString(2, nidn);
                stmt.setDate(3, Date.valueOf(ttl));
                stmt.setString(4, jk);
                stmt.setString(5, agama);

                return stmt.executeUpdate() > 0;
            }
        } else {
            // Update existing record
            String sql = "UPDATE dosen SET nidn = ?, ttl = ?, jk = ?, agama = ? WHERE id = ?";
            try (Connection conn = Koneksi.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nidn);
                stmt.setDate(2, Date.valueOf(ttl));
                stmt.setString(3, jk);
                stmt.setString(4, agama);
                stmt.setInt(5, currentUser.getId());

                return stmt.executeUpdate() > 0;
            }
        }
    }

    private void setLoading(boolean loading) {
        this.isLoading = loading;
        Platform.runLater(() -> {
            nidnField.setDisable(loading);
            tanggalLahirPicker.setDisable(loading);
            jkComboBox.setDisable(loading);
            agamaComboBox.setDisable(loading);
            simpanButton.setDisable(loading);
            batalButton.setDisable(loading);
            simpanButton.setText(loading ? "Menyimpan..." : "Simpan");
        });
    }

    private void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) simpanButton.getScene().getWindow();
            stage.close();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }
}