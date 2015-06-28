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
@Table(name = "predmet")
public class Predmet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;

    private String akronim;
    private String naziv;
    private String semestar;
    private String godina;

    public Predmet() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAkronim() {
        return akronim;
    }

    public void setAkronim(String akronim) {
        this.akronim = akronim;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getSemestar() {
        return semestar;
    }

    public void setSemestar(String semestar) {
        this.semestar = semestar;
    }

    public String getGodina() {
        return godina;
    }

    public void setGodina(String godina) {
        this.godina = godina;
    }

}
