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
@Table(name = "korisnik")
public class Angazovanje  implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;
    
    private int id_predmet;
    private int id_korisnik;

    public Angazovanje() {
    }

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

    public int getId_korisnik() {
        return id_korisnik;
    }

    public void setId_korisnik(int id_korisnik) {
        this.id_korisnik = id_korisnik;
    }
    
    
}
