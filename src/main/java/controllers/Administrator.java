/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.Angazovanje;
import DB.DBFactory;
import DB.Isplata;
import DB.Korisnik;
import DB.KorisnikLabBean;
import DB.Lab;
import DB.LabAktivnost;
import DB.Predmet;
import DB.Tip_aktivnosti;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.sql.rowset.serial.SerialBlob;
import org.hibernate.Query;
import org.hibernate.Session;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Djole
 */
@ManagedBean
@SessionScoped
public class Administrator {

    private Session session;
    private Korisnik korisnik;
    private String ponovljenPassword;
    private UploadedFile file;
    private List<Korisnik> listaKorisnika;
    private Korisnik admin;

    private List<Korisnik> listaNastavnika;
    private List<String> listaNastavnikaString;
    private List<Predmet> listaPredmeta;
    private List<String> listaPredmetaString;
    private String postaviNastavnikaIme;
    private String postaviNastavnikaPredmet;

    private List<KorisnikLabBean> korisnikLab;
     private List<Isplata> sveIsplate;

    private Date datum_od, datum_do;

    private Predmet novPredmet;

    public Administrator() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik where zahtev = 0");
        listaKorisnika = q.list();

        FacesContext context = FacesContext.getCurrentInstance();
        admin = (Korisnik) context.getExternalContext().getSessionMap().get("user");

        session.close();
    }

    public void dodajKorisnika(Korisnik kor) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        kor.setZahtev(1);
        session.update(kor);
        session.getTransaction().commit();

        listaKorisnika.remove(kor);

        session.close();
    }

    public void ukloniKorisnika(Korisnik kor) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        session.delete(kor);
        session.getTransaction().commit();
        listaKorisnika.remove(kor);

        session.close();

    }

    public String toRegistracija() {
        korisnik = new Korisnik();
        return "administratorRegistracija?faces-redirect=true";
    }

    public void registracija() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("SELECT username FROM Korisnik WHERE username='" + korisnik.getUsername() + "'");
        if (!q.list().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", "Contact admin."));
            return;
        }

        if (!korisnik.getPassword().equals(ponovljenPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords dont match", "Contact admin."));
            return;
        }

        try {
            InternetAddress emailAddr = new InternetAddress(korisnik.getEmail());
            emailAddr.validate();
        } catch (AddressException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email not in correct form", "Contact admin."));
            return;
        }

        String encryptedString = digestPassword(korisnik.getPassword());

        korisnik.setPassword(encryptedString);
        try {
            Blob blob = new SerialBlob(file.getContents());
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cant upload picture", "Contact admin."));
            return;
        }
        korisnik.setSlika(file.getContents());
        korisnik.setZahtev(1);
        session.save(korisnik);
        session.getTransaction().commit();
        session.close();

        korisnik = null;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Korisnik je uspesno dodat.", "Uspesno dodat korisnik."));
    }

    private String digestPassword(String password) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Internal error", "Contact admin."));
            return "";
        }

        messageDigest.update(password.getBytes());
        String encryptedString = new String(messageDigest.digest());
        return encryptedString;
    }

    public String toDodajPredmet() {
        novPredmet = new Predmet();
        return "administratorDodajPredmet?faces-redirect=true";
    }

    public void dodajPredmet() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        session.save(novPredmet);
        session.getTransaction().commit();

        novPredmet = null;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Predmet je uspesno dodat.", "Predmet dodat korisnik."));
        session.close();
    }

    public String toPostaviNastavnika() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik where zahtev = 1 and tip = 'Nastavnik'");
        listaNastavnika = q.list();

        listaNastavnikaString = new ArrayList<>();

        for (Korisnik k : listaNastavnika) {
            listaNastavnikaString.add(k.getIme() + " " + k.getPrezime());
        }

        session.close();
        return "administratorPostaviNastavnika?faces-redirect=true";
    }

    public void postaviPredmete() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        listaPredmeta = new ArrayList<>();
        listaPredmetaString = new ArrayList<>();

        Query q = session.createQuery("from Korisnik k, Angazovanje a where k.id = a.id_korisnik");
        Iterator<Object> iter = q.list().iterator();

        Query qu = session.createQuery("from Predmet");
        List<Predmet> tempPredmetLista = qu.list();

        List<Angazovanje> tempAngazovanje = new ArrayList<>();

        String[] niz = postaviNastavnikaIme.split(" ");
        String ime = niz[0];
        String prezime = niz[1];

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Korisnik k = (Korisnik) obj[0];
            Angazovanje ang = (Angazovanje) obj[1];
            if (k.getIme().equals(ime) && k.getPrezime().equals(prezime)) {
                tempAngazovanje.add(ang);
            }
        }

        for (Predmet p : tempPredmetLista) {
            boolean flag = true;
            for (Angazovanje a : tempAngazovanje) {
                if (p.getId() == a.getId_predmet()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                listaPredmeta.add(p);
                listaPredmetaString.add(p.getAkronim());
            }
        }

        session.close();
    }

    public void postaviNastavnika() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Korisnik korisnik = null;
        Predmet predmet = null;

        String[] niz = postaviNastavnikaIme.split(" ");
        String ime = niz[0];
        String prezime = niz[1];

        for (Korisnik k : listaNastavnika) {
            if (k.getIme().equals(ime) && k.getPrezime().equals(prezime)) {
                korisnik = k;
                break;
            }
        }

        for (Predmet p : listaPredmeta) {
            if (p.getAkronim().equals(postaviNastavnikaPredmet)) {
                predmet = p;
                break;
            }
        }

        Angazovanje a = new Angazovanje();
        a.setId_korisnik(korisnik.getId());
        a.setId_predmet(predmet.getId());

        session.save(a);
        session.getTransaction().commit();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Nastavnik je uspesno postavljen.", "Nastavnik je uspesno postavljen."));
        session.close();
    }

    public String toZatvoriPrijave() {
        return "administratorZatvoriPrijave?faces-redirect=true";
    }

    public void zatvoriPrijave() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Predmet where zakljucan = 0");
        List<Predmet> temp = q.list();

        for (Predmet p : temp) {
            p.setZakljucan(1);
            session.update(p);
        }

        session.getTransaction().commit();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Prijave uspesno zatvorene.", "Prijave uspesno zatvorene."));

        session.close();
    }

    public String toObracun() {
        korisnikLab = new ArrayList<>();
        datum_od = null;
        datum_do = null;
        return "administratorIsplata?faces-redirect=true";
    }

    public void updateListaObracun() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        
        korisnikLab.clear();

        Query q = session.createQuery("from Lab where zakljuceno = 1");
        List<Lab> tempLab = q.list();
        List<Lab> tempLab2 = new ArrayList<>();

        for (Lab l : tempLab) {
            boolean flag = false;
            if (l.getVreme_od().after(datum_od) && l.getVreme_do().before(datum_do)) {
                flag = true;
            }
            if (flag) {
                tempLab2.add(l);
            }
        }

        Query qu = session.createQuery("from Korisnik k, LabAktivnost a where k.id = a.id_kor and a.potvrdjeno = 3");
        Iterator<Object> iter = qu.list().iterator();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Korisnik k = (Korisnik) obj[0];
            LabAktivnost ang = (LabAktivnost) obj[1];
            Lab lab = null;
            boolean flag = false;
            for(Lab l: tempLab2){
                if(ang.getId_lab() == l.getId()){
                    lab = l;
                    flag = true;
                    break;
                }
            }
            if(flag){
                KorisnikLabBean korLab = new KorisnikLabBean();
                korLab.setK(k);
                korLab.setL(lab);
                korisnikLab.add(korLab);
            }
        }

        session.close();
    }
    
    public String obracunaj(){
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query q = session.createQuery("from LabAktivnost");
        List<LabAktivnost> labAktivnost = q.list();
        List<LabAktivnost> labAktivnost2 = new ArrayList<>();
        
        for(LabAktivnost la:labAktivnost){
            boolean flag = false;
            for(KorisnikLabBean kb: korisnikLab){
                Lab l = kb.getL();
                if(la.getId_lab() == l.getId() && kb.getK().getId() == la.getId_kor()){
                    flag = true;
                    break;
                }
            }
            if(flag){
                labAktivnost2.add(la);
            }
        }
        
        for(LabAktivnost la:labAktivnost2){
            la.setIsplata("Isplaceno");
            session.update(la);
        }
        
        Query query = session.createQuery("from Tip_aktivnosti");
        List<Tip_aktivnosti> tempTip = query.list();
        
        for(KorisnikLabBean kb: korisnikLab){
                Lab l = kb.getL();
                Korisnik k = kb.getK();
                Isplata isplata = new Isplata();
                
                isplata.setId_kor(k.getId());
                isplata.setVreme_od(l.getVreme_od());
                isplata.setVreme_do(l.getVreme_do());
                
                double suma = 0;
                
                double koeficijent;
                
                for(Tip_aktivnosti t: tempTip){
                    if(t.getNaziv().equals(l.getTip())){
                        koeficijent = t.getKoeficijent();
                        break;
                    }
                }
                
                long vreme = l.getVreme_do().getTime() - l.getVreme_od().getTime();
                double res = vreme/1000/45;
                
                suma = res*10;
                
                isplata.setSuma(suma);
                session.save(isplata);
            }
        
        session.getTransaction().commit();
        session.close();
        return "administratorObracuni?faces-redirect=true";
    }
    
    public String toIsplate(){
        
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query q = session.createQuery("from Isplata");
        sveIsplate = q.list();

        session.close();
        
        return "administratorObracuni?faces-redirect=true";
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public String getPonovljenPassword() {
        return ponovljenPassword;
    }

    public void setPonovljenPassword(String ponovljenPassword) {
        this.ponovljenPassword = ponovljenPassword;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<Korisnik> getListaKorisnika() {
        return listaKorisnika;
    }

    public void setListaKorisnika(List<Korisnik> listaKorisnika) {
        this.listaKorisnika = listaKorisnika;
    }

    public Predmet getNovPredmet() {
        return novPredmet;
    }

    public void setNovPredmet(Predmet novPredmet) {
        this.novPredmet = novPredmet;
    }

    public List<Korisnik> getListaNastavnika() {
        return listaNastavnika;
    }

    public void setListaNastavnika(List<Korisnik> listaNastavnika) {
        this.listaNastavnika = listaNastavnika;
    }

    public List<String> getListaNastavnikaString() {
        return listaNastavnikaString;
    }

    public void setListaNastavnikaString(List<String> listaNastavnikaString) {
        this.listaNastavnikaString = listaNastavnikaString;
    }

    public String getPostaviNastavnikaIme() {
        return postaviNastavnikaIme;
    }

    public void setPostaviNastavnikaIme(String postaviNastavnikaIme) {
        this.postaviNastavnikaIme = postaviNastavnikaIme;
    }

    public List<Predmet> getListaPredmeta() {
        return listaPredmeta;
    }

    public void setListaPredmeta(List<Predmet> listaPredmeta) {
        this.listaPredmeta = listaPredmeta;
    }

    public List<String> getListaPredmetaString() {
        return listaPredmetaString;
    }

    public void setListaPredmetaString(List<String> listaPredmetaString) {
        this.listaPredmetaString = listaPredmetaString;
    }

    public String getPostaviNastavnikaPredmet() {
        return postaviNastavnikaPredmet;
    }

    public void setPostaviNastavnikaPredmet(String postaviNastavnikaPredmet) {
        this.postaviNastavnikaPredmet = postaviNastavnikaPredmet;
    }

    public Date getDatum_od() {
        return datum_od;
    }

    public void setDatum_od(Date datum_od) {
        this.datum_od = datum_od;
    }

    public Date getDatum_do() {
        return datum_do;
    }

    public void setDatum_do(Date datum_do) {
        this.datum_do = datum_do;
    }

    public List<KorisnikLabBean> getKorisnikLab() {
        return korisnikLab;
    }

    public void setKorisnikLab(List<KorisnikLabBean> korisnikLab) {
        this.korisnikLab = korisnikLab;
    }

    public List<Isplata> getSveIsplate() {
        return sveIsplate;
    }

    public void setSveIsplate(List<Isplata> sveIsplate) {
        this.sveIsplate = sveIsplate;
    }

    
}
