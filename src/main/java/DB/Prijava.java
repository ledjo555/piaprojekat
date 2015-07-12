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
@Table(name = "prijava")
public class Prijava implements Serializable  {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;
    
    private int id_predmet;
    private int id_kor;
    private int zahtev;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_predmet() {
        return id_predmet;
    }

    public void setId_predmet(int id_predmet) {
        this.id_predmet = id_predmet;
    }

    public int getId_kor() {
        return id_kor;
    }

    public void setId_kor(int id_kor) {
        this.id_kor = id_kor;
    }

    public int getZahtev() {
        return zahtev;
    }

    public void setZahtev(int zahtev) {
        this.zahtev = zahtev;
    }
    
    
    
}
