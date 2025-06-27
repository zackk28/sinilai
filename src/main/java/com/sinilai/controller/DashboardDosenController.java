package com.sinilai.controller;

import com.sinilai.model.UserModel;
import com.sinilai.utils.Koneksi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardDosenController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardDosenController.class.getName());

    // SQL Queries as constants
    private static final String LOAD_DOSEN_DATA_SQL = "SELECT nidn, tempat_lahir, tanggal_lahir, jk, agama FROM dosen WHERE user_id = ?";

    private static final String LOAD_STUDENT_DATA_SQL = "SELECT DISTINCT m.nama FROM mahasiswa m " +
            "JOIN nilai n ON m.id = n.mahasiswa_id " +
            "JOIN mata_kuliah mk ON n.mata_kuliah_id = mk.id " +
            "WHERE mk.dosen_id = (SELECT id FROM dosen WHERE user_id = ?) " +
            "ORDER BY m.nama";

    private static final String COUNT_STUDENTS_SQL = "SELECT COUNT(DISTINCT m.id) as total FROM mahasiswa m " +
            "JOIN nilai n ON m.id = n.mahasiswa_id " +
            "JOIN mata_kuliah mk ON n.mata_kuliah_id = mk.id " +
            "WHERE mk.dosen_id = (SELECT id FROM dosen WHERE user_id = ?)";

    private static final String COUNT_CLASSES_SQL = "SELECT COUNT(*) as total FROM mata_kuliah mk " +
            "WHERE mk.dosen_id = (SELECT id FROM dosen WHERE user_id = ?)";

    @FXML
    private Label nidnLabel;
    @FXML
    private Button homeButton;
    @FXML
    private Button pengisianButton;
    @FXML
    private Button dataNilaiButton;
    @FXML
    private Button ManajemenMatkulButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ComboBox<String> searchComboBox;
    @FXML
    private Label studentsCountLabel;
    @FXML
    private Label classesCountLabel;

    private UserModel currentUser;
    private volatile boolean isDataComplete = false;
    private volatile boolean isLoading = false;
    private ObservableList<String> studentsList = FXCollections.observableArrayList();
    private ObservableList<String> filteredStudentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        setupSearchComboBox();
        setupButtonStyles();
        initializeStatistics();
    }

    private void setupEventHandlers() {
        // Use method references for cleaner code
        if (homeButton != null) {
            homeButton.setOnAction(e -> handleHomeButton());
        }
        if (pengisianButton != null) {
            pengisianButton.setOnAction(e -> handlePengisianButton());
        }
        if (dataNilaiButton != null) {
            dataNilaiButton.setOnAction(e -> handleDataNilaiButton());
        }
        if (ManajemenMatkulButton != null) {
            ManajemenMatkulButton.setOnAction(e -> handleManajemenMatkulButton());
        }
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogoutButton());
        }
    }

    private void setupSearchComboBox() {
        if (searchComboBox != null) {
            searchComboBox.setPromptText("Cari mahasiswa...");
            searchComboBox.setEditable(true);

            // Initialize the items with filtered list
            searchComboBox.setItems(filteredStudentsList);

            // Add listener for search functionality
            searchComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                if (newText != null) {
                    filterStudents(newText.trim());
                } else {
                    filteredStudentsList.setAll(studentsList);
                }
            });

            // Handle selection
            searchComboBox.setOnAction(e -> {
                String selectedStudent = searchComboBox.getValue();
                if (selectedStudent != null && !selectedStudent.trim().isEmpty()) {
                    LOGGER.info("Student selected: " + selectedStudent);
                    // TODO: Handle student selection
                }
            });
        }
    }

    private void setupButtonStyles() {
        // Add hover effects and disable states based on data completeness
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean shouldDisable = !isDataComplete;

        if (pengisianButton != null) {
            pengisianButton.setDisable(shouldDisable);
            if (shouldDisable) {
                pengisianButton.setStyle(pengisianButton.getStyle() + "; -fx-opacity: 0.6;");
            }
        }
        if (dataNilaiButton != null) {
            dataNilaiButton.setDisable(shouldDisable);
            if (shouldDisable) {
                dataNilaiButton.setStyle(dataNilaiButton.getStyle() + "; -fx-opacity: 0.6;");
            }
        }
        if (ManajemenMatkulButton != null) {
            ManajemenMatkulButton.setDisable(shouldDisable);
            if (shouldDisable) {
                ManajemenMatkulButton.setStyle(ManajemenMatkulButton.getStyle() + "; -fx-opacity: 0.6;");
            }
        }
    }

    private void initializeStatistics() {
        // Initialize with default values
        if (studentsCountLabel != null) {
            studentsCountLabel.setText("0");
        }
        if (classesCountLabel != null) {
            classesCountLabel.setText("0");
        }
    }

    public void setUser(UserModel user) {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Data user tidak boleh null.");
            return;
        }

        this.currentUser = user;
        loadUserDataAsync();
    }

    private void loadUserDataAsync() {
        if (isLoading)
            return;

        isLoading = true;

        CompletableFuture.runAsync(() -> {
            try {
                // Load dosen data first to check completeness
                boolean dataComplete = loadDosenData();

                Platform.runLater(() -> {
                    this.isDataComplete = dataComplete;
                    updateButtonStates();
                    updateDashboardInfo();

                    if (!dataComplete) {
                        // Show modal immediately if data is incomplete
                        showModalInputDataDosen();
                    } else {
                        // Load additional data only if dosen data is complete
                        loadStudentDataAsync();
                        loadStatisticsAsync();
                    }

                    isLoading = false;
                });

                LOGGER.info("User data loaded successfully for: " + currentUser.getNama());
            } catch (Exception e) {
                Platform.runLater(() -> {
                    isLoading = false;
                    LOGGER.log(Level.SEVERE, "Gagal memuat data dashboard", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data: " + e.getMessage());
                });
            }
        });
    }

    private boolean loadDosenData() throws SQLException {
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(LOAD_DOSEN_DATA_SQL)) {

            stmt.setInt(1, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nidn = rs.getString("nidn");
                    String tempatLahir = rs.getString("tempat_lahir");
                    Date tanggalLahir = rs.getDate("tanggal_lahir");
                    String jk = rs.getString("jk");
                    String agama = rs.getString("agama");

                    // Check if data is complete
                    boolean dataComplete = isDataComplete(nidn, tempatLahir, tanggalLahir, jk, agama);

                    if (dataComplete) {
                        Platform.runLater(() -> {
                            if (nidnLabel != null) {
                                nidnLabel.setText("NIDN: " + nidn);
                            }
                        });
                    }

                    return dataComplete;
                } else {
                    // No record found for this user
                    return false;
                }
            }
        }
    }

    private boolean isDataComplete(String nidn, String tempatLahir, Date tanggalLahir, String jk, String agama) {
        return nidn != null && !nidn.isBlank() &&
                tempatLahir != null && !tempatLahir.isBlank() &&
                tanggalLahir != null &&
                jk != null && !jk.isBlank() &&
                agama != null && !agama.isBlank();
    }

    private void updateDashboardInfo() {
        if (currentUser != null) {
            LOGGER.info("Dashboard updated for user: " + currentUser.getNama());
        }
    }

    private void loadStudentDataAsync() {
        if (searchComboBox == null || currentUser == null)
            return;

        CompletableFuture.runAsync(() -> {
            try (Connection conn = Koneksi.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(LOAD_STUDENT_DATA_SQL)) {

                stmt.setInt(1, currentUser.getId());

                try (ResultSet rs = stmt.executeQuery()) {
                    studentsList.clear();

                    while (rs.next()) {
                        String studentName = rs.getString("nama");
                        studentsList.add(studentName);
                    }

                    Platform.runLater(() -> {
                        filteredStudentsList.setAll(studentsList);
                    });
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Gagal memuat data mahasiswa untuk pencarian", e);
            }
        });
    }

    private void loadStatisticsAsync() {
        if (currentUser == null)
            return;

        CompletableFuture.runAsync(() -> {
            try {
                int studentsCount = getStudentsCount();
                int classesCount = getClassesCount();

                Platform.runLater(() -> {
                    if (studentsCountLabel != null) {
                        studentsCountLabel.setText(String.valueOf(studentsCount));
                    }
                    if (classesCountLabel != null) {
                        classesCountLabel.setText(String.valueOf(classesCount));
                    }
                });
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Gagal memuat statistik", e);
            }
        });
    }

    private int getStudentsCount() throws SQLException {
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_STUDENTS_SQL)) {

            stmt.setInt(1, currentUser.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private int getClassesCount() throws SQLException {
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_CLASSES_SQL)) {

            stmt.setInt(1, currentUser.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private void filterStudents(String searchText) {
        if (searchText.isEmpty()) {
            filteredStudentsList.setAll(studentsList);
        } else {
            filteredStudentsList.clear();
            for (String student : studentsList) {
                if (student.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredStudentsList.add(student);
                }
            }
        }
    }

    private void showModalInputDataDosen() {
        if (isLoading)
            return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/ModalDataDosen.fxml"));
            VBox modalPane = loader.load();

            ModalDataDosenController controller = loader.getController();
            controller.setUser(currentUser);

            Stage modalStage = createModalStage(modalPane, "Lengkapi Data Dosen");

            // Make modal non-closable if data is incomplete
            modalStage.setOnCloseRequest(event -> {
                if (!controller.isDataSaved()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Peringatan");
                    alert.setHeaderText(null);
                    alert.setContentText("Anda harus melengkapi data diri terlebih dahulu untuk menggunakan sistem.");
                    alert.showAndWait();
                    event.consume(); // Prevent closing
                }
            });

            modalStage.showAndWait();

            // Reload data after modal is closed
            if (controller.isDataSaved()) {
                loadUserDataAsync();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modal input data dosen", e);
            showAlert(Alert.AlertType.ERROR, "Kesalahan",
                    "Tidak dapat membuka form input data dosen: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error unexpected saat membuka modal", e);
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Terjadi kesalahan tak terduga: " + e.getMessage());
        }
    }

    private Stage createModalStage(VBox modalPane, String title) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.DECORATED);
        modalStage.setTitle(title);
        modalStage.setResizable(false);
        modalStage.setScene(new Scene(modalPane));
        modalStage.setMinWidth(650);
        modalStage.setMinHeight(500);
        modalStage.centerOnScreen();
        return modalStage;
    }

    // Event Handlers
    private void handleHomeButton() {
        if (currentUser != null && !isLoading) {
            loadUserDataAsync();
        }
    }

    private void handlePengisianButton() {
        if (!checkDataComplete())
            return;

        try {
            LOGGER.info("Navigating to Pengisian Nilai");
            // TODO: Implement navigation to pengisian nilai
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Pengisian Nilai akan segera tersedia");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Pengisian Nilai", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman Pengisian Nilai");
        }
    }

    private void handleDataNilaiButton() {
        if (!checkDataComplete())
            return;

        try {
            LOGGER.info("Navigating to Data Nilai");
            // TODO: Implement navigation to data nilai
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Data Nilai akan segera tersedia");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Data Nilai", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman Data Nilai");
        }
    }

    private void handleManajemenMatkulButton() {
        if (!checkDataComplete())
            return;

        try {
            LOGGER.info("Navigating to Manajemen Mata Kuliah");
            // TODO: Implement navigation to manajemen mata kuliah
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Manajemen Mata Kuliah akan segera tersedia");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Manajemen Mata Kuliah", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman Manajemen Mata Kuliah");
        }
    }

    private void handleLogoutButton() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Logout");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Apakah Anda yakin ingin logout?");

        ButtonType logoutBtn = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(logoutBtn, cancelBtn);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == logoutBtn) {
                try {
                    LOGGER.info("User logged out: " + currentUser.getNama());

                    // Close current window
                    Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                    currentStage.close();

                    // TODO: Open login window
                    showAlert(Alert.AlertType.INFORMATION, "Info", "Logout berhasil");

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during logout", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal logout: " + e.getMessage());
                }
            }
        });
    }

    private boolean checkDataComplete() {
        if (!isDataComplete) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Data Tidak Lengkap");
            alert.setHeaderText(null);
            alert.setContentText("Harap lengkapi data diri Anda terlebih dahulu sebelum mengakses fitur ini.");

            ButtonType completeButton = new ButtonType("Lengkapi Data");
            ButtonType cancelButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(completeButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == completeButton) {
                    showModalInputDataDosen();
                }
            });
            return false;
        }
        return true;
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

    // Getters for testing or other purposes
    public UserModel getCurrentUser() {
        return currentUser;
    }

    public boolean isDataComplete() {
        return isDataComplete;
    }

    public boolean isLoading() {
        return isLoading;
    }
}