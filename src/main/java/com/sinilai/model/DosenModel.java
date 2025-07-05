package com.sinilai.model;

import com.sinilai.utils.Koneksi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DosenModel {
    private static final Logger LOGGER = Logger.getLogger(DosenModel.class.getName());

    private int id;
    private String nidn;
    private Date ttl;
    private String agama;
    private String jk;

    // Constructor
    public DosenModel() {
    }

    public DosenModel(String nidn) {
        this.nidn = nidn;
    }

    // Getter
    public int getId() {
        return id;
    }

    public String getNidn() {
        return nidn;
    }

    public Date getTtl() {
        return ttl;
    }

    public String getAgama() {
        return agama;
    }

    public String getJk() {
        return jk;
    }

    public String getJkDisplay() {
        if (jk == null)
            return "-";
        return switch (jk.toUpperCase()) {
            case "L" -> "Laki-laki";
            case "P" -> "Perempuan";
            default -> jk;
        };
    }

    // Setter
    public void setId(int id) {
        if (id < 0)
            throw new IllegalArgumentException("ID tidak boleh negatif");
        this.id = id;
    }

    public void setNidn(String nidn) {
        this.nidn = (nidn != null && !nidn.trim().isEmpty()) ? nidn.trim() : null;
    }

    public void setTtl(Date ttl) {
        this.ttl = ttl;
    }

    public void setAgama(String agama) {
        this.agama = (agama != null && !agama.trim().isEmpty()) ? agama.trim() : null;
    }

    public void setJk(String jk) {
        if (jk != null) {
            String jkTrim = jk.trim().toUpperCase();
            if (jkTrim.equals("L") || jkTrim.equals("LAKI-LAKI")) {
                this.jk = "L";
            } else if (jkTrim.equals("P") || jkTrim.equals("PEREMPUAN")) {
                this.jk = "P";
            } else {
                this.jk = jkTrim;
            }
        } else {
            this.jk = null;
        }
    }

    // Update ke database
    public boolean updateProfil() {
        String sql = "UPDATE dosen SET nidn=?, ttl=?, agama=?, jk=? WHERE id=?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nidn);
            stmt.setDate(2, ttl);
            stmt.setString(3, agama);
            stmt.setString(4, jk);
            stmt.setInt(5, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LOGGER.info("Profil dosen berhasil diupdate.");
                return true;
            } else {
                LOGGER.warning("Tidak ada data dosen yang diupdate.");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal update profil dosen", e);
            return false;
        }
    }

    public static DosenModel getByUserId(int userId) {
        String query = "SELECT * FROM dosen WHERE id = ?";

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                DosenModel dosen = new DosenModel();
                dosen.setId(rs.getInt("id"));
                dosen.setNidn(rs.getString("nidn"));
                dosen.setTtl(rs.getDate("ttl"));
                dosen.setAgama(rs.getString("agama"));
                dosen.setJk(rs.getString("jk"));
                // Tambahkan properti lain jika ada

                return dosen;
            }

        } catch (SQLException e) {
            e.printStackTrace(); // atau pakai Logger jika kamu pakai
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("DosenModel{id=%d, nidn='%s', jk='%s'}", id, nidn, getJkDisplay());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DosenModel that))
            return false;
        return id == that.id && nidn.equals(that.nidn);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, nidn);
    }
}