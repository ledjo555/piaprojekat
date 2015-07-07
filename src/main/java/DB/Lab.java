/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
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
@Table(name = "lab")
public class Lab implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;

    private String predmet;
    private String naziv_lab;
    private Timestamp vreme_od;
    private Timestamp vreme_do;
    private String mesto_lab;
    private int id_tip;
    private int max_br;
    private String demonstratori;
    private int zakljuceno;

    public Lab() {
        predmet = naziv_lab = mesto_lab = demonstratori = "bla";
        id_tip = max_br = zakljuceno = 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPredmet() {
        return predmet;
    }

    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }

    public String getNaziv_lab() {
        return naziv_lab;
    }

    public void setNaziv_lab(String naziv_lab) {
        this.naziv_lab = naziv_lab;
    }

    public Timestamp getVreme_od() {
        return vreme_od;
    }

    public void setVreme_od(Timestamp vreme_od) {
        this.vreme_od = vreme_od;
    }

    public Timestamp getVreme_do() {
        return vreme_do;
    }

    public void setVreme_do(Timestamp vreme_do) {
        this.vreme_do = vreme_do;
    }

    public String getMesto_lab() {
        return mesto_lab;
    }

    public void setMesto_lab(String mesto_lab) {
        this.mesto_lab = mesto_lab;
    }

    public int getId_tip() {
        return id_tip;
    }

    public void setId_tip(int id_tip) {
        this.id_tip = id_tip;
    }

    public int getMax_br() {
        return max_br;
    }

    public void setMax_br(int max_br) {
        this.max_br = max_br;
    }

    public String getDemonstratori() {
        return demonstratori;
    }

    public void setDemonstratori(String demonstratori) {
        this.demonstratori = demonstratori;
    }

    public int getZakljuceno() {
        return zakljuceno;
    }

    public void setZakljuceno(int zakljuceno) {
        this.zakljuceno = zakljuceno;
    }

}
