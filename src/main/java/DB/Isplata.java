/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.io.Serializable;
import java.sql.Timestamp;
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
@Table(name = "isplata")
public class Isplata implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private int id;

    private int id_kor;
    private Timestamp vreme_od;
    private Timestamp vreme_do;
    private double suma;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_kor() {
        return id_kor;
    }

    public void setId_kor(int id_kor) {
        this.id_kor = id_kor;
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

    public double getSuma() {
        return suma;
    }

    public void setSuma(double suma) {
        this.suma = suma;
    }

   
    
    
}
