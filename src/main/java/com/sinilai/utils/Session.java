package com.sinilai.utils;

import com.sinilai.model.MahasiswaModel;
import com.sinilai.model.UserModel;

public class Session {
  private static UserModel currentUser;
  private static MahasiswaModel currentMahasiswa;

  public static void setUser(UserModel user) {
    currentUser = user;
  }

  public static UserModel getUser() {
    return currentUser;
  }

  public static void setMahasiswa(MahasiswaModel mahasiswa) {
    currentMahasiswa = mahasiswa;
  }

  public static MahasiswaModel getMahasiswa() {
    return currentMahasiswa;
  }

  public static void clear() {
    currentUser = null;
    currentMahasiswa = null;
  }
}
