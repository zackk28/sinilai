package com.sinilai.controller;

import com.sinilai.model.UserModel;
import com.sinilai.model.DosenModel;
import com.sinilai.utils.Koneksi;
import com.sinilai.utils.Session;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardDosenController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardDosenController.class.getName());

    // SQL Queries
    private static final String LOAD_STUDENT_DATA_SQL = "SELECT DISTINCT u.nama FROM user u " +
            "JOIN mahasiswa m ON u.id = m.id " +
            "JOIN kelas_mahasiswa km ON m.id = km.id_mahasiswa " +
            "JOIN kelas k ON km.id_kelas = k.id " +
            "WHERE k.id_dosen = ?";

    private static final String COUNT_STUDENTS_SQL = "SELECT COUNT(DISTINCT m.id) as total FROM mahasiswa m " +
            "JOIN kelas_mahasiswa km ON m.id = km.id_mahasiswa " +
            "JOIN kelas k ON km.id_kelas = k.id " +
            "WHERE k.id_dosen = ?";

    private static final String COUNT_CLASSES_SQL = "SELECT COUNT(*) as total FROM kelas " +
            "WHERE id_dosen = ?";

    // FXML Components
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label welcomeSubLabel;
    @FXML
    private Label nidnLabel;
    @FXML
    private Button homeButton;
    @FXML
    private Button pengisianButton;
    @FXML
    private Button dataNilaiButton;
    @FXML
    private Button manajemenMatkulButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button ubahDataButton;
    @FXML
    private ComboBox<String> searchComboBox;
    @FXML
    private Label studentsCountLabel;
    @FXML
    private Label matkulCountLabel;
    @FXML
    private Label namaDataLabel;
    @FXML
    private Label emailDataLabel;
    @FXML
    private Label roleDataLabel;
    @FXML
    private Label nidnDataLabel;
    @FXML
    private Label ttlDataLabel;
    @FXML
    private Label jkDataLabel;
    @FXML
    private Label agamaDataLabel;

    // Class variables
    private UserModel currentUser;
    private DosenModel currentDosen;
    private volatile boolean isDataComplete = false;
    private volatile boolean isLoading = false;
    private final ObservableList<String> studentsList = FXCollections.observableArrayList();
    private final ObservableList<String> filteredStudentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            LOGGER.info("Initializing DashboardDosenController...");
            initializeUIComponents();
            setupEventHandlers();

            currentUser = Session.getUser();

            if (currentUser != null) {
                updateUserInfo();
                loadUserDataAsync();
            } else {
                LOGGER.warning("No user session found");
                showAlertAndRedirect("Session expired", "Please login again");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Initialization error", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to initialize: " + e.getMessage());
        }
    }

    private void initializeUIComponents() {
        if (searchComboBox != null) {
            searchComboBox.setPromptText("Cari mahasiswa...");
            searchComboBox.setItems(filteredStudentsList);
        } else {
            LOGGER.severe("searchComboBox is not properly injected from FXML!");
        }

        Platform.runLater(() -> {
            if (studentsCountLabel != null)
                studentsCountLabel.setText("0");
            if (matkulCountLabel != null)
                matkulCountLabel.setText("0");
        });
    }

    private void setupEventHandlers() {
        if (homeButton != null)
            homeButton.setOnAction(this::handleHome);
        if (pengisianButton != null)
            pengisianButton.setOnAction(this::handlePengisian);
        if (dataNilaiButton != null)
            dataNilaiButton.setOnAction(this::handleDataNilai);
        if (manajemenMatkulButton != null)
            manajemenMatkulButton.setOnAction(this::handleManajemenMatkul);
        if (logoutButton != null)
            logoutButton.setOnAction(this::handleLogout);
        if (ubahDataButton != null)
            ubahDataButton.setOnAction(this::handleUbahData);

        if (searchComboBox != null && searchComboBox.getEditor() != null) {
            searchComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
                filterStudents(newVal);
            });
        }
    }

    @FXML
    private void handleHome(ActionEvent event) {
        loadUserDataAsync();
    }

    @FXML
    private void handlePengisian(ActionEvent event) {
        if (checkDataComplete()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Pengisian Nilai");
        }
    }

    @FXML
    private void handleDataNilai(ActionEvent event) {
        if (checkDataComplete()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Data Nilai");
        }
    }

    @FXML
    private void handleManajemenMatkul(ActionEvent event) {
        if (checkDataComplete()) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Manajemen Mata Kuliah");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin logout?");

        ButtonType logoutButton = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(logoutButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == logoutButton) {
                performLogout();
            }
        });
    }

    @FXML
    private void handleUbahData(ActionEvent event) {
        showModalInputDataDosen();
    }

    private void performLogout() {
        Session.setUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout", e);
            Platform.exit();
        }
    }

    private void loadUserDataAsync() {
        if (isLoading || currentUser == null)
            return;
        isLoading = true;

        CompletableFuture.runAsync(() -> {
            try {
                DosenModel dosen = DosenModel.getByUserId(currentUser.getId());
                Platform.runLater(() -> {
                    if (dosen != null)
                        setDosen(dosen);
                    checkDataCompleteness();
                    if (!isDataComplete)
                        showModalInputDataDosen();
                    isLoading = false;
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    isLoading = false;
                    LOGGER.log(Level.SEVERE, "Error loading user data", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user data: " + e.getMessage());
                });
            }
        });
    }

    private void loadStudentDataAsync() {
        if (currentDosen == null)
            return;

        CompletableFuture.runAsync(() -> {
            try (Connection conn = Koneksi.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(LOAD_STUDENT_DATA_SQL)) {

                stmt.setInt(1, currentDosen.getId());
                ResultSet rs = stmt.executeQuery();

                studentsList.clear();
                while (rs.next()) {
                    studentsList.add(rs.getString("nama"));
                }

                Platform.runLater(() -> filteredStudentsList.setAll(studentsList));

            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error loading student data", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Error", "Failed to load student data"));
            }
        });
    }

    private void loadStatisticsAsync() {
        if (currentDosen == null)
            return;

        CompletableFuture.runAsync(() -> {
            try {
                int studentsCount = getStudentsCount();
                int classesCount = getClassesCount();

                Platform.runLater(() -> {
                    if (studentsCountLabel != null)
                        studentsCountLabel.setText(String.valueOf(studentsCount));
                    if (matkulCountLabel != null)
                        matkulCountLabel.setText(String.valueOf(classesCount));
                });

            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error loading statistics", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Error", "Failed to load statistics"));
            }
        });
    }

    private int getStudentsCount() throws SQLException {
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_STUDENTS_SQL)) {
            stmt.setInt(1, currentDosen.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private int getClassesCount() throws SQLException {
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(COUNT_CLASSES_SQL)) {
            stmt.setInt(1, currentDosen.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private void animateLabelUpdate(javafx.scene.Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.play();
    }

    private void filterStudents(String filter) {
        if (filter == null || filter.isEmpty()) {
            filteredStudentsList.setAll(studentsList);
        } else {
            filteredStudentsList.clear();
            String lowerFilter = filter.toLowerCase();
            for (String student : studentsList) {
                if (student.toLowerCase().contains(lowerFilter)) {
                    filteredStudentsList.add(student);
                }
            }
        }
    }

    private void showModalInputDataDosen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/ModalDataDosenView.fxml"));
            VBox modalPane = loader.load();
            ModalDataDosenController controller = loader.getController();
            controller.setUser(currentUser);
            if (currentDosen != null)
                controller.setDosen(currentDosen);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Lengkapi Data Dosen");
            modalStage.setScene(new Scene(modalPane));

            if (ubahDataButton != null && ubahDataButton.getScene() != null)
                modalStage.initOwner(ubahDataButton.getScene().getWindow());

            modalStage.showAndWait();

            if (controller.isDataSaved()) {
                loadUserDataAsync();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening modal", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open input form");
        }
    }

    private boolean checkDataComplete() {
        if (!isDataComplete) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Data Tidak Lengkap");
            alert.setHeaderText(null);
            alert.setContentText("Lengkapi data diri terlebih dahulu");

            ButtonType completeButton = new ButtonType("Lengkapi", ButtonBar.ButtonData.OK_DONE);
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

    private void checkDataCompleteness() {
        this.isDataComplete = currentDosen != null &&
                currentDosen.getNidn() != null && !currentDosen.getNidn().isEmpty() &&
                currentDosen.getTtl() != null &&
                currentDosen.getJk() != null && !currentDosen.getJk().isEmpty() &&
                currentDosen.getAgama() != null && !currentDosen.getAgama().isEmpty();
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

    private void showAlertAndRedirect(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
        performLogout();
    }

    public void setDosen(DosenModel dosen) {
        this.currentDosen = dosen;
        Platform.runLater(() -> {
            updateDosenDisplay(dosen);
            checkDataCompleteness();
            if (isDataComplete) {
                loadStudentDataAsync();
                loadStatisticsAsync();
            }
        });
    }

    public void setUser(UserModel user) {
        this.currentUser = user;
        Platform.runLater(this::updateUserInfo);
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            if (welcomeLabel != null)
                welcomeLabel.setText(currentUser.getNama());
            if (welcomeSubLabel != null)
                welcomeSubLabel.setText("Hello " + currentUser.getNama());
            if (namaDataLabel != null)
                namaDataLabel.setText(currentUser.getNama());
            if (emailDataLabel != null)
                emailDataLabel.setText(currentUser.getEmail());
            if (roleDataLabel != null)
                roleDataLabel.setText(currentUser.getRole() != null ? currentUser.getRole() : "Dosen");
        }
    }

    private void updateDosenDisplay(DosenModel dosen) {
        if (dosen != null) {
            if (nidnLabel != null)
                nidnLabel.setText("NIDN: " + (dosen.getNidn() != null ? dosen.getNidn() : "-"));
            if (nidnDataLabel != null)
                nidnDataLabel.setText(dosen.getNidn() != null ? dosen.getNidn() : "-");
            if (ttlDataLabel != null) {
                Date ttl = dosen.getTtl();
                if (ttl != null) {
                    LocalDate localDate = ttl.toLocalDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
                    ttlDataLabel.setText(localDate.format(formatter));
                } else {
                    ttlDataLabel.setText("-");
                }
            }
            if (jkDataLabel != null) {
                String jk = dosen.getJk();
                if (jk != null) {
                    if (jk.equalsIgnoreCase("L")) {
                        jkDataLabel.setText("Laki-laki");
                    } else if (jk.equalsIgnoreCase("P")) {
                        jkDataLabel.setText("Perempuan");
                    } else {
                        jkDataLabel.setText(jk);
                    }
                } else {
                    jkDataLabel.setText("-");
                }
            }
            if (agamaDataLabel != null)
                agamaDataLabel.setText(dosen.getAgama() != null ? dosen.getAgama() : "-");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
