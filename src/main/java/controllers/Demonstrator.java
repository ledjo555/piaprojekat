/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.DBFactory;
import DB.Isplata;
import DB.Korisnik;
import DB.Lab;
import DB.LabAktivnost;
import DB.Predmet;
import DB.Prijava;
import DB.ZakljuceniLab;
import java.sql.Timestamp;
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

    private List<Isplata> sveIsplate;

    private List<Predmet> listaPredmeta;
    private List<ZakljuceniLab> zakljuceniLabovi;

    private List<Predmet> prijavaPredmeti;

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
        List<Lab> tempLabAktivnost = new ArrayList<>();

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
                tempLabAktivnost.add(l);
            }
        }

        for (Lab l : tempLabAktivnost) {
            if(l.getDemonstratori().isEmpty()){
                continue;
            }
            String[] niz = l.getDemonstratori().split(",");
            if (niz.length >= l.getMax_br()) {
                noviLabAktivnostListaLab.remove(l);
            }
        }

        session.close();

        izbaciPoklapanja();

        return "demonstratorNoviLab?faces-redirect=true";
    }

    private void izbaciPoklapanja() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        List<LabAktivnost> tempLabAktivnostLista = new ArrayList<>();
        List<Lab> tempLabAktivnostListaLab = new ArrayList<>();

        Query q = session.createQuery("from LabAktivnost where id_kor = '" + demonstrator.getId() + "' and potvrdjeno = 1");
        tempLabAktivnostLista = q.list();

        Query qu = session.createQuery("from Lab where zakljuceno = 0");
        List<Lab> tempLabLista = qu.list();
        List<Lab> tempp = new ArrayList<>();

        for (Lab l : tempLabLista) {
            boolean flag = false;
            for (LabAktivnost la : tempLabAktivnostLista) {
                if (la.getId_lab() == l.getId()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                tempLabAktivnostListaLab.add(l);
                tempp.add(l);
            }
        }

        
        for (Lab l : tempp) {
            String[] niz = l.getDemonstratori().split(",");
            if (niz.length >= l.getMax_br()) {
                tempLabAktivnostListaLab.remove(l);
            }
        }

        List<Lab> tempLab = new ArrayList<>();

        for (Lab lab : noviLabAktivnostListaLab) {
            boolean flag = false;
            for (Lab l : tempLabAktivnostListaLab) {
                boolean provera = proveriVreme(l.getVreme_od(), l.getVreme_do(), lab.getVreme_od(), lab.getVreme_do());
                if (!provera) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                tempLab.add(lab);
            }
        }

        noviLabAktivnostListaLab = tempLab;

        session.close();
    }

    private boolean proveriVreme(Timestamp vreme_od1, Timestamp vreme_do1, Timestamp vreme_od2, Timestamp vreme_do2) {
        boolean flag = true;
        if (vreme_od2.after(vreme_od1) && vreme_od2.before(vreme_do1)) {
            flag = false;
        }
        if (vreme_do2.after(vreme_od1) && vreme_do2.before(vreme_do1)) {
            flag = false;
        }
        if (vreme_od2.before(vreme_od1) && vreme_do2.after(vreme_do1)) {
            flag = false;
        }
        if (vreme_od1.equals(vreme_od2) || vreme_do1.equals(vreme_do2)) {
            flag = false;
        }
        return flag;
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
        String s = demonstrator.getIme() + " " + demonstrator.getPrezime() + " " + demonstrator.getOdsek() + " " + demonstrator.getGodina();
        String b = l.getDemonstratori();
        if (b.isEmpty()) {
            l.setDemonstratori(s);
        }
        else {
            b = b + "," + s;
            l.setDemonstratori(b);
        }
        session.update(l);
        
        tempLabAktivnost.setPotvrdjeno(1);
        session.update(tempLabAktivnost);
        session.getTransaction().commit();
        toNoviLab();
//        session.close();
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
        tempLabAktivnost.setPotvrdjeno(2);
        tempLabAktivnost.setKomentar(noviLabKomentar);
        session.update(tempLabAktivnost);
        session.getTransaction().commit();
        noviLabAktivnostListaLab.remove(labZaBrisanje);

        session.close();
    }

    public String toZavrsenLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from LabAktivnost where id_kor = '" + demonstrator.getId() + "' and potvrdjeno = 3");
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

    public String toIsplata() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Isplata where id_kor = '" + demonstrator.getId() + "'");
        sveIsplate = q.list();

        session.close();
        return "demonstratorIsplata?faces-redirect=true";
    }

    public String toPrijava() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Predmet where zakljucan = 0");
        List<Predmet> tempPrijavaPredmeti = q.list();
        prijavaPredmeti = q.list();

        Query qu = session.createQuery("from Prijava where id_kor = '" + demonstrator.getId() + "'");
        List<Prijava> tempPrijava = qu.list();

        for (Predmet p : tempPrijavaPredmeti) {
            boolean flag = false;
            for (Prijava prijava : tempPrijava) {
                if (prijava.getId_predmet() == p.getId()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                prijavaPredmeti.remove(p);
            }
        }

        session.close();
        return "demonstratorPrijava?faces-redirect=true";
    }

    public void prijavaPredmeta(Predmet p) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Prijava prijava = new Prijava();
        prijava.setId_predmet(p.getId());
        prijava.setId_kor(demonstrator.getId());

        session.save(prijava);
        session.getTransaction().commit();

        prijavaPredmeti.remove(p);

        session.close();
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

    public List<Predmet> getPrijavaPredmeti() {
        return prijavaPredmeti;
    }

    public void setPrijavaPredmeti(List<Predmet> prijavaPredmeti) {
        this.prijavaPredmeti = prijavaPredmeti;
    }

    public List<Isplata> getSveIsplate() {
        return sveIsplate;
    }

    public void setSveIsplate(List<Isplata> sveIsplate) {
        this.sveIsplate = sveIsplate;
    }

}
