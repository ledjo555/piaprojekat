/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.DBFactory;
import DB.Korisnik;
import DB.Lab;
import DB.LabAktivnost;
import DB.Predmet;
import DB.ZakljuceniLab;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Djole
 */
@ManagedBean
@SessionScoped
public class Demonstrator {

    private Session session;
    private Korisnik demonstrator;
    private List<LabAktivnost> noviLabAktivnostLista;
    private List<Lab> noviLabAktivnostListaLab;
    private String noviLabKomentar;
    private Lab labZaBrisanje;

    private List<Predmet> listaPredmeta;
    private List<ZakljuceniLab> zakljuceniLabovi;

    public Demonstrator() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        listaPredmeta = new ArrayList<>();

        FacesContext context = FacesContext.getCurrentInstance();
        demonstrator = (Korisnik) context.getExternalContext().getSessionMap().get("user");

        Query qu = session.createQuery("from Predmet p, Angazovanje a where p.id=a.id_predmet and a.id_korisnik = '" + demonstrator.getId() + "'");

        Iterator<Object> iter = qu.list().iterator();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Predmet p = (Predmet) obj[0];
            listaPredmeta.add(p);
        }

        session.close();
    }

    public String toNoviLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        noviLabAktivnostLista = new ArrayList<>();
        noviLabAktivnostListaLab = new ArrayList<>();

        Query q = session.createQuery("from LabAktivnost where id_kor = '" + demonstrator.getId() + "' and potvrdjeno = 0");
        noviLabAktivnostLista = q.list();

        Query qu = session.createQuery("from Lab where zakljuceno = 0");
        List<Lab> tempLabLista = qu.list();

        for (Lab l : tempLabLista) {
            boolean flag = false;
            for (LabAktivnost la : noviLabAktivnostLista) {
                if (la.getId_lab() == l.getId()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                noviLabAktivnostListaLab.add(l);
            }
        }

        for (Lab l : noviLabAktivnostListaLab) {
            String[] niz = l.getDemonstratori().split(",");
            if (niz.length >= l.getMax_br()) {
                noviLabAktivnostListaLab.remove(l);
            }
        }

        session.close();
        return "demonstratorNoviLab?faces-redirect=true";
    }

    public void potvrdiNoviLab(Lab l) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        LabAktivnost tempLabAktivnost = null;

        for (LabAktivnost lab : noviLabAktivnostLista) {
            if (lab.getId_kor() == demonstrator.getId() && lab.getId_lab() == l.getId()) {
                tempLabAktivnost = lab;
                break;
            }
        }
        tempLabAktivnost.setPotvrdjeno(1);
        session.update(tempLabAktivnost);
        session.getTransaction().commit();
        noviLabAktivnostListaLab.remove(l);
        session.close();
    }

    public void setLabOtkazi(Lab l) {
        labZaBrisanje = l;
    }

    public void otkaziNoviLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        LabAktivnost tempLabAktivnost = null;

        for (LabAktivnost lab : noviLabAktivnostLista) {
            if (lab.getId_kor() == demonstrator.getId() && lab.getId_lab() == labZaBrisanje.getId()) {
                tempLabAktivnost = lab;
                break;
            }
        }
        tempLabAktivnost.setPotvrdjeno(1);
        tempLabAktivnost.setKomentar(noviLabKomentar);
        session.update(tempLabAktivnost);
        session.getTransaction().commit();
        noviLabAktivnostListaLab.remove(labZaBrisanje);

        session.close();
    }

    public String toZavrsenLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from LabAktivnost where id_kor = '" + demonstrator.getId() + "'");
        List<LabAktivnost> tempLabAktivnost = q.list();

        Query qu = session.createQuery("from Lab where zakljuceno = 1");
        List<Lab> tempLabLista = qu.list();
        List<Lab> tempLabLista2 = new ArrayList<>();
        zakljuceniLabovi = new ArrayList<>();

        for (Lab l : tempLabLista) {
            String[] niz = l.getDemonstratori().split(",");
            for (int i = 0; i < niz.length; i++) {
                String[] nizz = niz[i].split(" ");
                String tempIme = nizz[0];
                String tempPrezime = nizz[1];
                String tempOdsek = nizz[2];
                if (demonstrator.getIme().equals(tempIme) && demonstrator.getPrezime().equals(tempPrezime) && demonstrator.getOdsek().equals(tempOdsek)) {
                    tempLabLista2.add(l);
                }
            }
        }

        for (Lab l : tempLabLista2) {
            for (LabAktivnost la : tempLabAktivnost) {
                if (l.getId() == la.getId_lab()) {
                    ZakljuceniLab z = new ZakljuceniLab();
                    z.setLab(l);
                    z.setLabAktivnost(la);
                    zakljuceniLabovi.add(z);
                }
            }
        }

        session.close();
        return "demonstratorZavrsenLab?faces-redirect=true";
    }

    public List<Predmet> getListaPredmeta() {
        return listaPredmeta;
    }

    public void setListaPredmeta(List<Predmet> listaPredmeta) {
        this.listaPredmeta = listaPredmeta;
    }

    public List<Lab> getNoviLabAktivnostListaLab() {
        return noviLabAktivnostListaLab;
    }

    public void setNoviLabAktivnostListaLab(List<Lab> noviLabAktivnostListaLab) {
        this.noviLabAktivnostListaLab = noviLabAktivnostListaLab;
    }

    public String getNoviLabKomentar() {
        return noviLabKomentar;
    }

    public void setNoviLabKomentar(String noviLabKomentar) {
        this.noviLabKomentar = noviLabKomentar;
    }

    public List<ZakljuceniLab> getZakljuceniLabovi() {
        return zakljuceniLabovi;
    }

    public void setZakljuceniLabovi(List<ZakljuceniLab> zakljuceniLabovi) {
        this.zakljuceniLabovi = zakljuceniLabovi;
    }

}
