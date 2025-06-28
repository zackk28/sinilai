package com.sinilai.model;

public class NilaiModel {
  private int id;
  private int idKelas;
  private int idMahasiswa;
  private Float uts;
  private Float tugas;
  private Float quiz;
  private Float uas;
  private Float totalSkor;

  // Getter dan Setter
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getIdKelas() {
    return idKelas;
  }

  public void setIdKelas(int idKelas) {
    this.idKelas = idKelas;
  }

  public int getIdMahasiswa() {
    return idMahasiswa;
  }

  public void setIdMahasiswa(int idMahasiswa) {
    this.idMahasiswa = idMahasiswa;
  }

  public Float getUts() {
    return uts;
  }

  public void setUts(Float uts) {
    this.uts = uts;
  }

  public Float getTugas() {
    return tugas;
  }

  public void setTugas(Float tugas) {
    this.tugas = tugas;
  }

  public Float getQuiz() {
    return quiz;
  }

  public void setQuiz(Float quiz) {
    this.quiz = quiz;
  }

  public Float getUas() {
    return uas;
  }

  public void setUas(Float uas) {
    this.uas = uas;
  }

  public Float getTotalSkor() {
    return totalSkor;
  }

  public void setTotalSkor(Float totalSkor) {
    this.totalSkor = totalSkor;
  }
}
