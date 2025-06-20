package com.sinilai.controller;

import com.sinilai.model.UserModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label namaMahasiswaLabel;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private Button homeButton;

    @FXML
    private Button profilButton;

    @FXML
    private Button khsButton;

    @FXML
    private Button settingButton;

    private UserModel currentMahasiswa;

    public void initialize() {
        // Setup aksi tombol navigasi
        homeButton.setOnAction(e -> handleHome());
        profilButton.setOnAction(e -> handleProfil());
        khsButton.setOnAction(e -> handleKHS());
        settingButton.setOnAction(e -> handleSetting());
    }

    public void setMahasiswa(UserModel mahasiswa) {
        this.currentMahasiswa = mahasiswa;

        // Tampilkan nama mahasiswa di label dashboard
        if (namaMahasiswaLabel != null && mahasiswa != null) {
            namaMahasiswaLabel.setText(mahasiswa.getNama());
        }
    }

    @FXML
    private void handleHome() {
        System.out.println("Home button clicked");
        // Tambahkan implementasi halaman Home
    }

    @FXML
    private void handleProfil() {
        System.out.println("Profil button clicked");
        if (currentMahasiswa != null) {
            System.out.println("Menampilkan profil: " + currentMahasiswa.getNama());
        }
    }

    @FXML
    private void handleKHS() {
        System.out.println("KHS button clicked");
        // Tambahkan implementasi halaman KHS
    }

    @FXML
    private void handleSetting() {
        System.out.println("Setting button clicked");
        // Tambahkan implementasi halaman setting
    }

    public UserModel getCurrentMahasiswa() {
        return currentMahasiswa;
    }
}
