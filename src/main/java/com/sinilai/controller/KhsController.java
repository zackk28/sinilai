package com.sinilai.controller;

import com.sinilai.model.KhsModel;
import com.sinilai.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KhsController {
  private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

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
  private Label namaMahasiswaLabel;
  @FXML
  private Label namaKhsLabel;
  @FXML
  private Label totalSksLabel;
  @FXML
  private Label ipSemesterLabel;
  @FXML
  private ChoiceBox<String> tahunAjaranChoiceBox;
  @FXML
  private Button searchButton;
  @FXML
  private TableView<KhsModel> khsTableView;
  @FXML
  private TableColumn<KhsModel, String> kodeColumn;
  @FXML
  private TableColumn<KhsModel, String> namaMataKuliahColumn;
  @FXML
  private TableColumn<KhsModel, Integer> sksColumn;
  @FXML
  private TableColumn<KhsModel, Double> nilaiMutuColumn;
  @FXML
  private TableColumn<KhsModel, String> nilaiHurufColumn;

  private ObservableList<KhsModel> khsData = FXCollections.observableArrayList();

  private String convertToHuruf(double skor) {
    if (skor >= 85)
      return "A";
    else if (skor >= 75)
      return "B+";
    else if (skor >= 70)
      return "B";
    else if (skor >= 65)
      return "C+";
    else if (skor >= 60)
      return "C";
    else if (skor >= 50)
      return "D";
    else
      return "E";
  }

  private double convertToMutu(double skor, int sks) {
    double bobot;
    if (skor >= 85)
      bobot = 4.0;
    else if (skor >= 75)
      bobot = 3.5;
    else if (skor >= 70)
      bobot = 3.0;
    else if (skor >= 65)
      bobot = 2.5;
    else if (skor >= 60)
      bobot = 2.0;
    else if (skor >= 50)
      bobot = 1.0;
    else
      bobot = 0.0;

    return bobot * sks;
  }

  @FXML
  public void initialize() {
    namaMahasiswaLabel.setText(Session.getUser().getNama());
    namaKhsLabel.setText(Session.getUser().getNama());

    setupTable();
    loadTahunAjaran();
  }

  private void setupTable() {
    kodeColumn.setCellValueFactory(new PropertyValueFactory<>("kode"));
    namaMataKuliahColumn.setCellValueFactory(new PropertyValueFactory<>("namaMatkul"));
    sksColumn.setCellValueFactory(new PropertyValueFactory<>("sks"));
    nilaiMutuColumn.setCellValueFactory(new PropertyValueFactory<>("nilaiMutu"));
    nilaiHurufColumn.setCellValueFactory(new PropertyValueFactory<>("nilaiHuruf"));

    khsTableView.setItems(khsData);
  }

  private void loadTahunAjaran() {
    // Contoh data statis, bisa diambil dari DB juga
    tahunAjaranChoiceBox.setItems(FXCollections.observableArrayList("2022/2023", "2023/2024", "2024/2025"));
    tahunAjaranChoiceBox.setValue("2023/2024");
  }

  @FXML
  private void handleSearch(ActionEvent event) {
    String tahunDipilih = tahunAjaranChoiceBox.getValue();
    if (tahunDipilih == null)
      return;

    khsData.clear();

    String sql = """
            SELECT
                m.kode_matkul AS kode,
                m.nama_matkul,
                m.sks,
                n.total_skor
            FROM nilai n
            JOIN kelas k ON n.id_kelas = k.id
            JOIN matkul m ON k.id_matkul = m.id
            WHERE n.id_mahasiswa = ? AND k.tahun_ajaran = ?
        """;

    try (Connection conn = com.sinilai.utils.Koneksi.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, Session.getMahasiswa().getId()); // Pastikan ini id, bukan nim
      stmt.setString(2, tahunDipilih);

      ResultSet rs = stmt.executeQuery();

      int totalSks = 0;
      double totalMutu = 0.0;

      while (rs.next()) {
        String kode = rs.getString("kode");
        String namaMatkul = rs.getString("nama_matkul");
        int sks = rs.getInt("sks");
        double totalSkor = rs.getDouble("total_skor");

        String huruf = convertToHuruf(totalSkor);
        double mutu = convertToMutu(totalSkor, sks);

        KhsModel model = new KhsModel();
        model.setKode(kode);
        model.setNamaMatkul(namaMatkul);
        model.setSks(sks);
        model.setNilaiHuruf(huruf);
        model.setNilaiMutu(mutu);

        totalSks += sks;
        totalMutu += mutu;

        khsData.add(model);
      }

      totalSksLabel.setText(String.valueOf(totalSks));
      double ip = totalSks == 0 ? 0.0 : totalMutu / totalSks;
      ipSemesterLabel.setText(String.format("%.2f", ip));

    } catch (SQLException e) {
      showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat data KHS: " + e.getMessage());
    }
  }

  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Handle logout dengan konfirmasi
   */
  @FXML
  private void handleLogout(ActionEvent event) {
    try {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Konfirmasi Logout");
      alert.setHeaderText(null);
      alert.setContentText("Apakah Anda yakin ingin logout?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        performLogout();
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error during logout process", e);
      showAlert(Alert.AlertType.ERROR, "Error",
          "Terjadi kesalahan saat logout: " + e.getMessage());
    }
  }

  /**
   * Perform actual logout operation
   */
  private void performLogout() {
    try {
      // Clear current data
      Session.setUser(null);
      Session.setMahasiswa(null);

      // Load login view
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sinilai/view/LoginView.fxml"));
      Scene scene = new Scene(loader.load());

      Stage stage = (Stage) logoutButton.getScene().getWindow();
      stage.setTitle("Login - SINILAI");
      stage.setScene(scene);
      stage.setMaximized(false);
      stage.setMinWidth(400);
      stage.setMinHeight(500);

      LOGGER.info("User logged out successfully");

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error loading login view during logout", e);
      showAlert(Alert.AlertType.ERROR, "Error",
          "Gagal logout: " + e.getMessage());
    }
  }
}
