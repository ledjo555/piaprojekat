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
import DB.LabAktivnost;
import DB.Predmet;
import DB.Prijava;
import DB.PrijavaBean;
import DB.Tip_aktivnosti;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.hibernate.Query;
import org.hibernate.Session;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;

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

    private StreamedContent slika;

    private List<Lab> demonDetaljiLista;

    private Lab zakljucivanjeLabEdit;

    private String novDemonstratorPredmet;
    private String novDemonstratorIme;
    private List<String> dodajDemonstratoraLista;
    private List<Korisnik> dodajDemonstratoraListaSvih;

    private List<PrijavaBean> prijavaBean;

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

        Query query = session.createQuery("from Lab");
        List<Lab> tempLab = query.list();

        int idNovogLaba = tempLab.get(tempLab.size() - 1).getId();
        idNovogLaba++;

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
            Korisnik korisnikTemp = null;
            for (Korisnik k : unosLabSourceDemonstratori) {
                if (k.getIme().equals(tempIme) && k.getPrezime().equals(tempPrezime) && k.getOdsek().equals(tempOdsek)) {
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
            LabAktivnost l = new LabAktivnost();
            l.setId_lab(idNovogLaba);
            l.setId_kor(korisnikTemp.getId());
            session.save(l);

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

        Query q = session.createQuery("from LabAktivnost where id_lab = '" + zakljucivanjeLabEdit.getId() + "'");
        List<LabAktivnost> tempLabAktivnost = q.list();

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
            Korisnik korisnikTemp = null;
            for (Korisnik k : unosLabSourceDemonstratori) {
                if (k.getIme().equals(tempIme) && k.getPrezime().equals(tempPrezime) && k.getOdsek().equals(tempOdsek)) {
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

            for (LabAktivnost la : tempLabAktivnost) {
                if (korisnikTemp != null && la.getId_kor() == korisnikTemp.getId()) {
                    la.setPotvrdjeno(3);
                    session.update(la);
                }
            }
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
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("from Lab l, LabAktivnost la where la.id_lab = l.id and la.id_kor = '" + demonstrator.getId() + "' and la.potvrdjeno = 3");
        Iterator<Object> iter = q.list().iterator();

        demonDetaljiLista = new ArrayList<>();

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Lab l = (Lab) obj[0];
            LabAktivnost la = (LabAktivnost) obj[1];
            demonDetaljiLista.add(l);
        }

        session.close();

        return "nastavnikDemonstratorDetalji?faces-redirect=true";
    }

    public String toDodajDemonstratora() {
        dodajDemonstratoraLista = new ArrayList<>();
        dodajDemonstratoraListaSvih = new ArrayList<>();
        return "nastavnikDodajDemonstratora?faces-redirect=true";
    }

    public void dodajDemonstratoraUpdateLista() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        dodajDemonstratoraLista.clear();

        Query q = session.createQuery("from Korisnik where tip = :t");
        q.setParameter("t", "Demonstrator");
        List<Korisnik> sviKorisnici = q.list();

        Query qu = session.createQuery("from Angazovanje a, Predmet p where p.id = a.id_predmet and p.akronim = '" + novDemonstratorPredmet + "'");
        Iterator<Object> iterator = qu.list().iterator();

        List<Angazovanje> angazovanje = new ArrayList<>();
        while (iterator.hasNext()) {
            Object[] obj = (Object[]) iterator.next();
            Angazovanje ang = (Angazovanje) obj[0];
            Predmet p = (Predmet) obj[1];
            angazovanje.add(ang);
        }

        for (Korisnik k : sviKorisnici) {
            boolean flag = true;
            for (Angazovanje ang : angazovanje) {
                if (k.getId() == ang.getId_korisnik()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                dodajDemonstratoraListaSvih.add(k);
                String s = k.getIme() + " " + k.getPrezime() + " " + k.getOdsek() + " " + k.getGodina() + ". godina";
                dodajDemonstratoraLista.add(s);
            }
        }
        session.close();
    }

    public void dodajDemonstratora() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        String[] niz = novDemonstratorIme.split(" ");
        String tempIme = niz[0];
        String tempPrezime = niz[1];
        String tempOdsek = niz[2];
        String tempGodina = niz[3];
        Korisnik korisnikTemp = null;
        for (Korisnik k : dodajDemonstratoraListaSvih) {
            if (k.getIme().equals(tempIme) && k.getPrezime().equals(tempPrezime) && k.getOdsek().equals(tempOdsek)) {
                korisnikTemp = k;
                break;
            }
        }

        Query q = session.createQuery("from Predmet where akronim = '" + novDemonstratorPredmet + "'");
        Predmet p = (Predmet) q.list().get(0);

        Angazovanje a = new Angazovanje();
        a.setId_korisnik(korisnikTemp.getId());
        a.setId_predmet(p.getId());

        session.save(a);
        session.getTransaction().commit();

        session.close();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Uspesno dodat demonstrator", "Demonstrator dodat"));
        dodajDemonstratoraLista.remove(novDemonstratorIme);
    }

    public String toPrijava() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        prijavaBean = new ArrayList<>();
        List<Prijava> tempPrijava = new ArrayList<>();
        List<Korisnik> tempKorisnici = new ArrayList<>();
        List<Predmet> tempPredmeti = new ArrayList<>();
        List<Predmet> tempPredmeti2 = new ArrayList<>();

        Query q = session.createQuery("from Prijava p, Predmet pr where p.id_predmet = pr.id and pr.zakljucan = 1");
        if (q.list().isEmpty()) {
            return "nastavnikPrijava?faces-redirect=true";
        }
        Iterator<Object> iterator = q.list().iterator();

        Query qu = session.createQuery("from Prijava p, Korisnik k where p.id_kor = k.id");
        Iterator<Object> iter = qu.list().iterator();

        while (iterator.hasNext()) {
            Object[] obj = (Object[]) iterator.next();
            Prijava p = (Prijava) obj[0];
            Predmet pr = (Predmet) obj[1];
            boolean flag = false;
            for (String s : source) {
                if (s.equals(pr.getAkronim())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                tempPrijava.add(p);
            }
        }

        while (iter.hasNext()) {
            Object[] obj = (Object[]) iter.next();
            Prijava p = (Prijava) obj[0];
            Korisnik k = (Korisnik) obj[1];
            boolean flag = false;
            for (Prijava prijava : tempPrijava) {
                if (prijava.getId() == p.getId()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                tempKorisnici.add(k);
            }
        }

        Query que = session.createQuery("from Predmet where zakljucan = 1");
        tempPredmeti = que.list();
        List<PrijavaBean> tempPrijavaBean = new ArrayList<>();
        for (Prijava p : tempPrijava) {
            for (Predmet predmet : tempPredmeti) {
                if (predmet.getId() == p.getId_predmet()) {
                    tempPredmeti2.add(predmet);
                }
            }
        }

        for (int i = 0; i < tempKorisnici.size(); i++) {
            PrijavaBean p = new PrijavaBean();
            p.setK(tempKorisnici.get(i));
            p.setP(tempPredmeti2.get(i));
//            prijavaBean.add(p);
            tempPrijavaBean.add(p);
        }

        Query query = session.createQuery("from Prijava where zahtev = 0");
        List<Prijava> listaPrijava = query.list();

        for (PrijavaBean pb : tempPrijavaBean) {
            boolean flag = false;
            Predmet pr = pb.getP();
            Korisnik k = pb.getK();
            for (Prijava p : listaPrijava) {
                if (k.getId() == p.getId_kor() && pr.getId() == p.getId_predmet()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                prijavaBean.add(pb);
            }
        }

        session.close();
        return "nastavnikPrijava?faces-redirect=true";
    }

    public void prijavaPotvrda(PrijavaBean p) {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Angazovanje a = new Angazovanje();
        a.setId_korisnik(p.getK().getId());
        a.setId_predmet(p.getP().getId());

        Query query = session.createQuery("from Prijava where zahtev = 0");
        List<Prijava> listaPrijava = query.list();

        Predmet pr = p.getP();
        Korisnik k = p.getK();
        Prijava tempPrijava = null;
        for (Prijava prijava : listaPrijava) {
            if (k.getId() == prijava.getId_kor() && pr.getId() == prijava.getId_predmet()) {
                tempPrijava = prijava;
                break;
            }
        }

        tempPrijava.setZahtev(1);
        session.update(tempPrijava);

        session.save(a);
        session.getTransaction().commit();

        prijavaBean.remove(p);

        session.close();
    }

    public void prijavaOtkaz(PrijavaBean p) {

        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query query = session.createQuery("from Prijava where zahtev = 0");
        List<Prijava> listaPrijava = query.list();

        Predmet pr = p.getP();
        Korisnik k = p.getK();
        Prijava tempPrijava = null;
        for (Prijava prijava : listaPrijava) {
            if (k.getId() == prijava.getId_kor() && pr.getId() == prijava.getId_predmet()) {
                tempPrijava = prijava;
                break;
            }
        }

        tempPrijava.setZahtev(1);
        session.update(tempPrijava);

        session.getTransaction().commit();
        session.close();

        prijavaBean.remove(p);
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

    public StreamedContent getSlika() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes.
//            String imageId = context.getExternalContext().getRequestParameterMap().get("imageId");
            return new DefaultStreamedContent(new ByteArrayInputStream(demonstrator.getSlika()));
        }
    }

    public void setSlika(StreamedContent slika) {
        this.slika = slika;
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

    public String getNovDemonstratorIme() {
        return novDemonstratorIme;
    }

    public void setNovDemonstratorIme(String novDemonstratorIme) {
        this.novDemonstratorIme = novDemonstratorIme;
    }

    public List<PrijavaBean> getPrijavaBean() {
        return prijavaBean;
    }

    public void setPrijavaBean(List<PrijavaBean> prijavaBean) {
        this.prijavaBean = prijavaBean;
    }

    public List<Lab> getDemonDetaljiLista() {
        return demonDetaljiLista;
    }

    public void setDemonDetaljiLista(List<Lab> demonDetaljiLista) {
        this.demonDetaljiLista = demonDetaljiLista;
    }

}
