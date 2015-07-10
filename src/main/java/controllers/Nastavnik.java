/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.Angazovanje;
import DB.DBFactory;
import DB.Korisnik;
import DB.Lab;
import DB.Predmet;
import DB.Tip_aktivnosti;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Query;
import org.hibernate.Session;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Djole
 */
@ManagedBean
@SessionScoped
public class Nastavnik {

    private Session session;
    private List<Korisnik> lista;
    private Korisnik demonstrator, nastavnik;
    private DualListModel<String> predmeti;
    private List<String> source;
    private List<String> target;
    private List<Lab> labovi;
    private DualListModel<String> unosLabDemonstratori;
    private List<Korisnik> unosLabSourceDemonstratori;

    private String izabranPredmetLab;
    private List<String> tipoviAktivnosti;

    private Lab zakljucivanjeLabEdit;
    
    private String novDemonstratorPredmet;
    private String novDemonstratorIme;
    private List<String> dodajDemonstratoraLista;

    private Lab noviLab;
    private Date datum_od, datum_do;

    private String searchIme;
    private String searchPrezime;
    private String filter;

    public Nastavnik() {

        filter = "1";

        lista = new ArrayList<>();

        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik where tip = :t");
        q.setParameter("t", "Demonstrator");
        lista = q.list();

        source = new ArrayList<>();
        target = new ArrayList<>();

        FacesContext context = FacesContext.getCurrentInstance();
        nastavnik = (Korisnik) context.getExternalContext().getSessionMap().get("user");

        Query qu = session.createQuery("from Predmet p, Angazovanje a where p.id=a.id_predmet and a.id_korisnik = '" + nastavnik.getId() + "'");

        Iterator<Object> iter = qu.list().iterator();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Predmet p = (Predmet) obj[0];
            source.add(p.getAkronim());
        }

        predmeti = new DualListModel<>(source, target);

        session.close();
    }

    public void updateLista() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        if (filter.equals("1")) {
            Query q = session.createQuery("from Korisnik where tip = :t and ime like '%" + searchIme + "%' and prezime like '%" + searchPrezime + "%'");
            q.setParameter("t", "Demonstrator");
            lista = q.list();

        } else if (filter.equals("2")) {

            lista.clear();
            Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and ime like '%" + searchIme + "%' and prezime like '%" + searchPrezime + "%' and k.id = a.id_korisnik");
            q.setParameter("t", "Demonstrator");
            Iterator<Object> iter = q.list().iterator();

            Query qu = session.createQuery("from Angazovanje a where a.id_korisnik = '" + nastavnik.getId() + "'");
            List<Angazovanje> tempAngazovanje = qu.list();

            while (iter.hasNext()) {
                Object[] obj = (Object[]) iter.next();
                Korisnik k = (Korisnik) obj[0];
                Angazovanje ang = (Angazovanje) obj[1];
                boolean flag = false;
                for (Angazovanje a : tempAngazovanje) {
                    if (a.getId_predmet() == ang.getId_predmet()) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    lista.add(k);
                }
            }

        } else {
            lista.clear();
            Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and ime like '%" + searchIme + "%' and prezime like '%" + searchPrezime + "%' and k.id = a.id_korisnik");
            q.setParameter("t", "Demonstrator");
            Iterator<Object> iter = q.list().iterator();

            Query qu = session.createQuery("from Angazovanje a, Predmet p where a.id_korisnik = '" + nastavnik.getId() + "' and p.id = a.id_predmet");

            Iterator<Object> iterator = qu.list().iterator();

            List<String> tempTarget = predmeti.getTarget();

            List<Angazovanje> tempAngazovanje = new ArrayList<>();

            while (iterator.hasNext()) {
                Object[] obj = (Object[]) iterator.next();
                Angazovanje ang = (Angazovanje) obj[0];
                Predmet p = (Predmet) obj[1];
                boolean flag = false;
                for (String s : tempTarget) {
                    if (p.getAkronim().equals(s)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    tempAngazovanje.add(ang);
                }
            }

            while (iter.hasNext()) {
                Object[] obj = (Object[]) iter.next();
                Korisnik k = (Korisnik) obj[0];
                Angazovanje ang = (Angazovanje) obj[1];
                boolean flag = false;
                for (Angazovanje a : tempAngazovanje) {
                    if (a.getId_predmet() == ang.getId_predmet()) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    lista.add(k);
                }
            }
        }

        session.close();
    }

    public String toUnosLab() {
        noviLab = new Lab();
        tipoviAktivnosti = new ArrayList<>();

        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Tip_aktivnosti");
        List<Tip_aktivnosti> aktivnosti = q.list();

        for (Tip_aktivnosti t : aktivnosti) {
            tipoviAktivnosti.add(t.getNaziv());
        }

        unosLabDemonstratori = new DualListModel<>();
        unosLabSourceDemonstratori = new ArrayList<>();
        datum_od = null;
        datum_do = null;

        return "nastavnikUnosLab?faces-redirect=true";
    }

    public void unosLabListaDemonstratori() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and k.id = a.id_korisnik");
        q.setParameter("t", "Demonstrator");
        Iterator<Object> iter = q.list().iterator();

        Query qu = session.createQuery("from Predmet p where p.akronim = '" + noviLab.getPredmet() + "'");

        Predmet p = (Predmet) qu.list().get(0);
        List<String> sourceKor = new ArrayList<>();
        List<String> targetKor = new ArrayList<>();

        unosLabSourceDemonstratori.clear();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Korisnik k = (Korisnik) obj[0];
            Angazovanje ang = (Angazovanje) obj[1];
            if (ang.getId_predmet() == p.getId()) {
                unosLabSourceDemonstratori.add(k);
                String s = k.getIme() + " " + k.getPrezime() + " " + k.getOdsek() + " " + k.getGodina() + ". godina";
                sourceKor.add(s);
            }
        }
        unosLabDemonstratori.setSource(sourceKor);
        unosLabDemonstratori.setTarget(targetKor);
        session.close();
    }

    public void napraviLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        noviLab.setVreme_od(new Timestamp(datum_od.getTime()));
        noviLab.setVreme_do(new Timestamp(datum_do.getTime()));

        List<String> temp = unosLabDemonstratori.getTarget();
//        session.get
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (String s : temp) {
            if (flag) {
                sb.append(",");
            }
            String[] niz = s.split(" ");
            String tempIme = niz[0];
            String tempPrezime = niz[1];
            String tempOdsek = niz[2];
            String tempGodina = niz[3];
            Korisnik korisnikTemp;
            for (Korisnik k : unosLabSourceDemonstratori) {
                if (k.getIme().equals(tempIme) && k.getPrezime().equals(tempPrezime) && k.getOdsek().equals(tempOdsek) && k.getGodina().equals(tempGodina)) {
                    korisnikTemp = k;
                    break;
                }
            }
            sb.append(tempIme);
            sb.append(" ");
            sb.append(tempPrezime);
            sb.append(" ");
            sb.append(tempOdsek);
            sb.append(" ");
            sb.append(tempGodina);

            // TODO: javiti nekako demonstratoru za novi lab
            flag = true;
        }
        noviLab.setDemonstratori(sb.toString());

        session.save(noviLab);
        session.getTransaction().commit();
        session.close();

        unosLabSourceDemonstratori.clear();
        unosLabDemonstratori.getSource().clear();
        unosLabDemonstratori.getTarget().clear();
        datum_od = null;
        datum_do = null;
        noviLab = new Lab();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspesno dodata lab vezba", "Lab vezba dodata"));
    }

    public String toArhiva() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        labovi = new ArrayList<>();
        List<Lab> temp_labovi = new ArrayList<>();

        Query query_lab = session.createQuery("from Lab where zakljuceno = 1");
        temp_labovi = query_lab.list();

        for (Lab l : temp_labovi) {
            for (String s : source) {
                if (l.getPredmet().equals(s)) {
                    labovi.add(l);
                    break;
                }
            }
        }

        session.close();

        return "nastavnikArhivaLab?faces-redirect=true";
    }

    public String toZakljuciLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        labovi = new ArrayList<>();
        List<Lab> temp_labovi = new ArrayList<>();

        Query query_lab = session.createQuery("from Lab where zakljuceno = 0");
        temp_labovi = query_lab.list();

        for (Lab l : temp_labovi) {
            for (String s : source) {
                if (l.getPredmet().equals(s)) {
                    labovi.add(l);
                    break;
                }
            }
        }

        session.close();
        return "nastavnikZakljuciLab?faces-redirect=true";
    }

    public String toZakljuciLabEdit(Lab lab) {
        zakljucivanjeLabEdit = lab;
        datum_od = lab.getVreme_od();
        datum_do = lab.getVreme_do();
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and k.id = a.id_korisnik");
        q.setParameter("t", "Demonstrator");
        Iterator<Object> iter = q.list().iterator();

        Query qu = session.createQuery("from Predmet p where p.akronim = '" + lab.getPredmet() + "'");

        Predmet p = (Predmet) qu.list().get(0);
        List<String> sourceKor = new ArrayList<>();
        List<String> targetKor = new ArrayList<>();

        unosLabSourceDemonstratori = new ArrayList<>();
        unosLabDemonstratori = new DualListModel<>();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Korisnik k = (Korisnik) obj[0];
            Angazovanje ang = (Angazovanje) obj[1];
            if (ang.getId_predmet() == p.getId()) {
                unosLabSourceDemonstratori.add(k);
                String s = k.getIme() + " " + k.getPrezime() + " " + k.getOdsek() + " " + k.getGodina() + ". godina";
                sourceKor.add(s);
            }
        }

        String[] niz = lab.getDemonstratori().split(",");
        for (int i = 0; i < niz.length; i++) {
            for (String ss : sourceKor) {
                if (ss.contains(niz[i])) {
                    targetKor.add(ss);
                    sourceKor.remove(ss);
                    break;
                }
            }
        }

        unosLabDemonstratori.setSource(sourceKor);
        unosLabDemonstratori.setTarget(targetKor);
        session.close();
        return "nastavnikZakljuciLabEdit?faces-redirect=true";
    }

    public void zakljuciLab(Lab lab) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        lab.setZakljuceno(1);
        session.update(lab);
        session.getTransaction().commit();
        session.close();

        labovi.remove(lab);
    }

    public String zakljuciLab() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        zakljucivanjeLabEdit.setVreme_od(new Timestamp(datum_od.getTime()));
        zakljucivanjeLabEdit.setVreme_do(new Timestamp(datum_do.getTime()));
        List<String> temp = unosLabDemonstratori.getTarget();
//        session.get
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (String s : temp) {
            if (flag) {
                sb.append(",");
            }
            String[] niz = s.split(" ");
            String tempIme = niz[0];
            String tempPrezime = niz[1];
            String tempOdsek = niz[2];
            String tempGodina = niz[3];
            Korisnik korisnikTemp;
            for (Korisnik k : unosLabSourceDemonstratori) {
                if (k.getIme().equals(tempIme) && k.getPrezime().equals(tempPrezime) && k.getOdsek().equals(tempOdsek) && k.getGodina().equals(tempGodina)) {
                    korisnikTemp = k;
                    break;
                }
            }
            sb.append(tempIme);
            sb.append(" ");
            sb.append(tempPrezime);
            sb.append(" ");
            sb.append(tempOdsek);
            sb.append(" ");
            sb.append(tempGodina);

            // TODO: javiti nekako demonstratoru za novi lab
            flag = true;
        }
        zakljucivanjeLabEdit.setDemonstratori(sb.toString());
        zakljucivanjeLabEdit.setZakljuceno(1);
        session.update(zakljucivanjeLabEdit);
        session.getTransaction().commit();
        session.close();
        return toZakljuciLab();
    }

    public String toDetaljnije(Korisnik kor) {
        demonstrator = kor;
        return "nastavnikDemonstratorDetalji?faces-redirect=true";
    }

    public String toDodajDemonstratora() {
        dodajDemonstratoraLista = new ArrayList<>();
        return "nastavnikDodajDemonstratora?faces-redirect=true";
    }
    
    public void dodajDemonstratoraUpdateLista(){
        
        dodajDemonstratoraLista.add("bla");
    }

    public List<Korisnik> getLista() {
        return lista;
    }

    public void setLista(List<Korisnik> lista) {
        this.lista = lista;
    }

    public Korisnik getDemonstrator() {
        return demonstrator;
    }

    public void setDemonstrator(Korisnik korisnik) {
        this.demonstrator = korisnik;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public DualListModel<String> getPredmeti() {
        return predmeti;
    }

    public void setPredmeti(DualListModel<String> predmeti) {
        this.predmeti = predmeti;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public List<String> getTarget() {
        return target;
    }

    public void setTarget(List<String> target) {
        this.target = target;
    }

    public Korisnik getNastavnik() {
        return nastavnik;
    }

    public void setNastavnik(Korisnik nastavnik) {
        this.nastavnik = nastavnik;
    }

    public String getSearchIme() {
        return searchIme;
    }

    public void setSearchIme(String searchIme) {
        this.searchIme = searchIme;
    }

    public String getSearchPrezime() {
        return searchPrezime;
    }

    public void setSearchPrezime(String searchPrezime) {
        this.searchPrezime = searchPrezime;
    }

    public String getIzabranPredmetLab() {
        return izabranPredmetLab;
    }

    public void setIzabranPredmetLab(String izabranPredmetLab) {
        this.izabranPredmetLab = izabranPredmetLab;
    }

    public List<Lab> getLabovi() {
        return labovi;
    }

    public void setLabovi(List<Lab> labovi) {
        this.labovi = labovi;
    }

    public Lab getNoviLab() {
        return noviLab;
    }

    public void setNoviLab(Lab noviLab) {
        this.noviLab = noviLab;
    }

    public List<String> getTipoviAktivnosti() {
        return tipoviAktivnosti;
    }

    public void setTipoviAktivnosti(List<String> tipoviAktivnosti) {
        this.tipoviAktivnosti = tipoviAktivnosti;
    }

    public DualListModel<String> getUnosLabDemonstratori() {
        return unosLabDemonstratori;
    }

    public void setUnosLabDemonstratori(DualListModel<String> unosLabDemonstratori) {
        this.unosLabDemonstratori = unosLabDemonstratori;
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

    public Lab getZakljucivanjeLabEdit() {
        return zakljucivanjeLabEdit;
    }

    public void setZakljucivanjeLabEdit(Lab zakljucivanjeLabEdit) {
        this.zakljucivanjeLabEdit = zakljucivanjeLabEdit;
    }

    public String getNovDemonstratorPredmet() {
        return novDemonstratorPredmet;
    }

    public void setNovDemonstratorPredmet(String novDemonstratorPredmet) {
        this.novDemonstratorPredmet = novDemonstratorPredmet;
    }

    public List<String> getDodajDemonstratoraLista() {
        return dodajDemonstratoraLista;
    }

    public void setDodajDemonstratoraLista(List<String> dodajDemonstratoraLista) {
        this.dodajDemonstratoraLista = dodajDemonstratoraLista;
    }
    
    
}
