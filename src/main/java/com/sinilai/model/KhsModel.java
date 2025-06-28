package com.sinilai.model;

public class KhsModel {
  private String kode;
  private String namaMatkul;
  private int sks;
  private double nilaiMutu;
  private String nilaiHuruf;

  public String getKode() {
    return kode;
  }

  public void setKode(String kode) {
    this.kode = kode;
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

  public double getNilaiMutu() {
    return nilaiMutu;
  }

  public void setNilaiMutu(double nilaiMutu) {
    this.nilaiMutu = nilaiMutu;
  }

  public String getNilaiHuruf() {
    return nilaiHuruf;
  }

  public void setNilaiHuruf(String nilaiHuruf) {
    this.nilaiHuruf = nilaiHuruf;
  }
}
