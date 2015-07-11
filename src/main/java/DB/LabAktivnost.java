/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Djole
 */
@Entity
@Table(name = "labaktivnost")
public class LabAktivnost implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;
    private int id_lab;
    private int id_kor;
    private int potvrdjeno;
    private int isplata;
    private String komentar;

    public LabAktivnost() {
        isplata = 0;
        potvrdjeno = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_lab() {
        return id_lab;
    }

    public void setId_lab(int id_lab) {
        this.id_lab = id_lab;
    }

    public int getId_kor() {
        return id_kor;
    }

    public void setId_kor(int id_kor) {
        this.id_kor = id_kor;
    }

    public int getIsplata() {
        return isplata;
    }

    public void setIsplata(int isplata) {
        this.isplata = isplata;
    }

    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }

    public int getPotvrdjeno() {
        return potvrdjeno;
    }

    public void setPotvrdjeno(int potvrdjeno) {
        this.potvrdjeno = potvrdjeno;
    }

    
}
