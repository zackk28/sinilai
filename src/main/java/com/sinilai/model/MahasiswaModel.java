package com.sinilai.model;

import com.sinilai.utils.Koneksi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MahasiswaModel {
    private static final Logger LOGGER = Logger.getLogger(MahasiswaModel.class.getName());

    private int id;
    private String nim;
    private String jurusan;
    private String prodi;
    private String jalurSeleksi;
    private String asalSekolah;
    private int semester;
    private Date ttl;
    private String agama;
    private String jk;
    private String alamat;
    private String kota;
    private String prov;
    private String noTelp;
    private String pendidikanAkhir;
    private String statusMenikah;
    private String tempatTinggal;
    private String sumberUang;
    private String nik;
    private String noKk;

    private UserModel user;

    // Constructors
    public MahasiswaModel() {
    }

    public MahasiswaModel(String nim, String jurusan, String prodi) {
        this.nim = nim;
        this.jurusan = jurusan;
        this.prodi = prodi;
    }

    // Getter methods
    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public String getNim() {
        return nim;
    }

    public String getJurusan() {
        return jurusan;
    }

    public String getProdi() {
        return prodi;
    }

    public String getJalurSeleksi() {
        return jalurSeleksi;
    }

    public String getAsalSekolah() {
        return asalSekolah;
    }

    public int getSemester() {
        return semester;
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

    public String getAlamat() {
        return alamat;
    }

    public String getKota() {
        return kota;
    }

    public String getProv() {
        return prov;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public String getPendidikanAkhir() {
        return pendidikanAkhir;
    }

    public String getStatusMenikah() {
        return statusMenikah;
    }

    public String getTempatTinggal() {
        return tempatTinggal;
    }

    public String getSumberUang() {
        return sumberUang;
    }

    public String getNik() {
        return nik;
    }

    public String getNoKk() {
        return noKk;
    }

    // Setter methods with validation
    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID tidak boleh negatif");
        }
        this.id = id;
    }

    public void setNim(String nim) {
        if (nim != null && !nim.trim().isEmpty()) {
            this.nim = nim.trim();
        } else {
            this.nim = null;
        }
    }

    public void setJurusan(String jurusan) {
        this.jurusan = sanitizeString(jurusan);
    }

    public void setProdi(String prodi) {
        this.prodi = sanitizeString(prodi);
    }

    public void setJalurSeleksi(String jalurSeleksi) {
        this.jalurSeleksi = sanitizeString(jalurSeleksi);
    }

    public void setAsalSekolah(String asalSekolah) {
        this.asalSekolah = sanitizeString(asalSekolah);
    }

    public void setSemester(int semester) {
        if (semester < 1 || semester > 14) {
            throw new IllegalArgumentException("Semester harus antara 1-14");
        }
        this.semester = semester;
    }

    public void setTtl(Date ttl) {
        this.ttl = ttl;
    }

    public void setAgama(String agama) {
        this.agama = sanitizeString(agama);
    }

    public void setJk(String jk) {
        if (jk != null) {
            String clean = jk.trim().toUpperCase();
            if (clean.equals("L") || clean.equals("LAKI-LAKI") || clean.equals("PRIA")) {
                this.jk = "L";
            } else if (clean.equals("P") || clean.equals("PEREMPUAN") || clean.equals("WANITA")) {
                this.jk = "P";
            } else {
                this.jk = null; // invalid input
            }
        } else {
            this.jk = null;
        }
    }

    public void setAlamat(String alamat) {
        this.alamat = sanitizeString(alamat);
    }

    public void setKota(String kota) {
        this.kota = sanitizeString(kota);
    }

    public void setProv(String prov) {
        this.prov = sanitizeString(prov);
    }

    public void setNoTelp(String noTelp) {
        if (noTelp != null && !noTelp.trim().isEmpty()) {
            // Remove non-digit characters except + at the beginning
            String cleanPhone = noTelp.replaceAll("[^+\\d]", "");
            this.noTelp = cleanPhone;
        } else {
            this.noTelp = null;
        }
    }

    public void setPendidikanAkhir(String pendidikanAkhir) {
        this.pendidikanAkhir = sanitizeString(pendidikanAkhir);
    }

    public void setStatusMenikah(String statusMenikah) {
        this.statusMenikah = sanitizeString(statusMenikah);
    }

    public void setTempatTinggal(String tempatTinggal) {
        this.tempatTinggal = sanitizeString(tempatTinggal);
    }

    public void setSumberUang(String sumberUang) {
        this.sumberUang = sanitizeString(sumberUang);
    }

    public void setNik(String nik) {
        if (nik != null && !nik.trim().isEmpty()) {
            String cleanNik = nik.replaceAll("\\D", ""); // Remove non-digits
            if (cleanNik.length() == 16) {
                this.nik = cleanNik;
            } else {
                throw new IllegalArgumentException("NIK harus 16 digit");
            }
        } else {
            this.nik = null;
        }
    }

    public void setNoKk(String noKk) {
        if (noKk != null && !noKk.trim().isEmpty()) {
            String cleanNoKk = noKk.replaceAll("\\D", ""); // Remove non-digits
            if (cleanNoKk.length() == 16) {
                this.noKk = cleanNoKk;
            } else {
                throw new IllegalArgumentException("No KK harus 16 digit");
            }
        } else {
            this.noKk = null;
        }
    }

    /**
     * Helper method untuk membersihkan string input
     */
    private String sanitizeString(String input) {
        if (input != null && !input.trim().isEmpty()) {
            return input.trim();
        }
        return null;
    }

    /**
     * Validasi data sebelum update
     */
    public boolean isValid() {
        if (nim == null || nim.trim().isEmpty()) {
            LOGGER.warning("NIM tidak boleh kosong");
            return false;
        }

        if (jurusan == null || jurusan.trim().isEmpty()) {
            LOGGER.warning("Jurusan tidak boleh kosong");
            return false;
        }

        if (prodi == null || prodi.trim().isEmpty()) {
            LOGGER.warning("Program Studi tidak boleh kosong");
            return false;
        }

        if (semester < 1 || semester > 14) {
            LOGGER.warning("Semester tidak valid");
            return false;
        }

        return true;
    }

    /**
     * Update profil mahasiswa ke database
     */
    public boolean updateProfil() {
        if (!isValid()) {
            LOGGER.severe("Data tidak valid, update dibatalkan");
            return false;
        }

        String sql = """
                    UPDATE mahasiswa SET
                        nim=?, jurusan=?, prodi=?, semester=?, jalur_seleksi=?,
                        asal_sekolah=?, ttl=?, agama=?, jk=?, alamat=?,
                        kota=?, prov=?, no_telp=?, pendidikan_akhir=?, status_menikah=?,
                        tempat_tinggal=?, sumber_uang=?, nik=?, no_kk=?
                    WHERE id=?
                """;

        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Start transaction
            conn.setAutoCommit(false);

            try {
                setUpdateParameters(stmt);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    LOGGER.info("Profil mahasiswa berhasil diupdate untuk ID: " + id);
                    return true;
                } else {
                    conn.rollback();
                    LOGGER.warning("Tidak ada data yang diupdate untuk ID: " + id);
                    return false;
                }

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating profil mahasiswa ID: " + id, e);
            return false;
        }
    }

    /**
     * Set parameters for update statement
     */
    private void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, nim);
        stmt.setString(2, jurusan);
        stmt.setString(3, prodi);
        stmt.setInt(4, semester);
        stmt.setString(5, jalurSeleksi);
        stmt.setString(6, asalSekolah);
        stmt.setDate(7, ttl);
        stmt.setString(8, agama);
        stmt.setString(9, jk);
        stmt.setString(10, alamat);
        stmt.setString(11, kota);
        stmt.setString(12, prov);
        stmt.setString(13, noTelp);
        stmt.setString(14, pendidikanAkhir);
        stmt.setString(15, statusMenikah);
        stmt.setString(16, tempatTinggal);
        stmt.setString(17, sumberUang);
        stmt.setString(18, nik);
        stmt.setString(19, noKk);
        stmt.setInt(20, id);
    }

    public boolean loadMahasiswaFromDB(int id) {
        String query = "SELECT * FROM mahasiswa WHERE id = ?";
        try (Connection conn = Koneksi.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                this.id = rs.getInt("id");
                this.nim = rs.getString("nim");
                this.jurusan = rs.getString("jurusan");
                this.prodi = rs.getString("prodi");
                this.jalurSeleksi = rs.getString("jalur_seleksi");
                this.asalSekolah = rs.getString("asal_sekolah");
                this.semester = rs.getInt("semester");
                this.ttl = rs.getDate("ttl");
                this.agama = rs.getString("agama");
                this.jk = rs.getString("jk");
                this.alamat = rs.getString("alamat");
                this.kota = rs.getString("kota");
                this.prov = rs.getString("prov");
                this.noTelp = rs.getString("no_telp");
                this.pendidikanAkhir = rs.getString("pendidikan_akhir");
                this.statusMenikah = rs.getString("status_menikah");
                this.tempatTinggal = rs.getString("tempat_tinggal");
                this.sumberUang = rs.getString("sumber_uang");
                this.nik = rs.getString("nik");
                this.noKk = rs.getString("no_kk");
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil data mahasiswa dari DB", ex);
        }

        return false;
    }

    /**
     * Get display name for gender
     */
    public String getJkDisplay() {
        if (jk == null)
            return "-";
        return switch (jk.toLowerCase()) {
            case "", "laki-laki", "pria" -> "Laki-laki";
            case "p", "perempuan", "wanita" -> "Perempuan";
            default -> jk;
        };
    }

    @Override
    public String toString() {
        return String.format("MahasiswaModel{id=%d, nim='%s', nama='%s', jurusan='%s', prodi='%s'}",
                id, nim, "N/A", jurusan, prodi);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        MahasiswaModel that = (MahasiswaModel) obj;
        return id == that.id &&
                (nim != null ? nim.equals(that.nim) : that.nim == null);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, nim);
    }
}