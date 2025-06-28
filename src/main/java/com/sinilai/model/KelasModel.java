package com.sinilai.model;

public class KelasModel {
  private int id;
  private int idMatkul;
  private int idDosen;
  private String namaKelas;
  private int semester;
  private String tahunAjaran;

  // Getter dan Setter

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getIdMatkul() {
    return idMatkul;
  }

  public void setIdMatkul(int idMatkul) {
    this.idMatkul = idMatkul;
  }

  public int getIdDosen() {
    return idDosen;
  }

  public void setIdDosen(int idDosen) {
    this.idDosen = idDosen;
  }

  public String getNamaKelas() {
    return namaKelas;
  }

  public void setNamaKelas(String namaKelas) {
    this.namaKelas = namaKelas;
  }

  public int getSemester() {
    return semester;
  }

  public void setSemester(int semester) {
    this.semester = semester;
  }

  public String getTahunAjaran() {
    return tahunAjaran;
  }

  public void setTahunAjaran(String tahunAjaran) {
    this.tahunAjaran = tahunAjaran;
  }
}
