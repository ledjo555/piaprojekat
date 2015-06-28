/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.Angazovanje;
import DB.DBFactory;
import DB.Korisnik;
import DB.Predmet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
            Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and k.id = a.id_korisnik");
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
            Query q = session.createQuery("from Korisnik k, Angazovanje a where tip = :t and k.id = a.id_korisnik");
            q.setParameter("t", "Demonstrator");
            Iterator<Object> iter = q.list().iterator();

            Query qu = session.createQuery("from Angazovanje a where a.id_korisnik = '" + nastavnik.getId() + "'");
            List<Angazovanje> tempAngazovanje = qu.list();
            
            List<String> tempTarget = predmeti.getTarget();
            
//            for(Angazovanje an:tempAngazovanje){
//                for(String s:tempTarget){
//                    if
//                }
//            }

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

    public String toDetaljnije(Korisnik kor) {
        demonstrator = kor;
        return "nastavnikDemonstratorDetalji?faces-redirect=true";
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

}
