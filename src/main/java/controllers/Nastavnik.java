/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.DBFactory;
import DB.Korisnik;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Djole
 */
@ManagedBean
@SessionScoped
public class Nastavnik {

    private Session session;
    private List<Korisnik> lista;
    private Korisnik korisnik;

    public Nastavnik() {
        lista = new ArrayList<>();

        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik where tip = :t");
        q.setParameter("t", "Demonstrator");
        lista = q.list();
        session.close();
    }

    public String toDetaljnije(Korisnik kor) {
        korisnik = kor;
        return "nastavnikDemonstratorDetalji?faces-redirect=true";
    }

    public List<Korisnik> getLista() {
        return lista;
    }

    public void setLista(List<Korisnik> lista) {
        this.lista = lista;
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

}
