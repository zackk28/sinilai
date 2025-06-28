package com.sinilai.model;

public class MatkulModel {
  private int id;
  private String kodeMatkul;
  private String namaMatkul;
  private int sks;

  // Getter dan Setter

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getKodeMatkul() {
    return kodeMatkul;
  }

  public void setKodeMatkul(String kodeMatkul) {
    this.kodeMatkul = kodeMatkul;
  }

  public String getNamaMatkul() {
    return namaMatkul;
  }

  public void setNamaMatkul(String namaMatkul) {
    this.namaMatkul = namaMatkul;
  }

  public int getSks() {
    return sks;
  }

  public void setSks(int sks) {
    this.sks = sks;
  }
}
