/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import DB.DBFactory;
import DB.Korisnik;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.hibernate.Query;
import org.hibernate.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Djole
 */
@ManagedBean
@SessionScoped
public class Login {

    private String username;
    private String password;
    private String ponovljenPassword;
    private Session session;
    private Korisnik korisnik;
    private UploadedFile file;
    private StreamedContent slika;

    public Login() {
    }

    public String login() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        password = digestPassword(password);

        Query q = session.createQuery("from Korisnik where username = :u and password = :p");
        q.setParameter("u", username);
        q.setParameter("p", password);
        if (q.list().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong username or password", "Contact admin."));
            password = "";
            return "";
        }
        korisnik = (Korisnik) q.list().get(0);
        session.close();

        if (korisnik.getZahtev() == 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Vas nalog jos uvek nije aktivan", "Contact admin."));
            korisnik = null;
            password = "";
            return "";
        }

//        InputStream in = new ByteArrayInputStream(korisnik.getSlika());
//        slika = new DefaultStreamedContent(in, "image/jpeg");
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getSessionMap().put("user", korisnik);

        if (korisnik.getTip().equals("Demonstrator")) {

            return "restricted/demonstrator?faces-redirect=true";
        } else if (korisnik.getTip().equals("Nastavnik")) {
            return "restricted/nastavnik?faces-redirect=true";
        } else {
            return "restricted/administrator?faces-redirect=true";
        }
    }

    public String toRegistracija() {
        korisnik = new Korisnik();
        return "/faces/registracija?faces-redirect=true";
    }

    public String registracija() {

        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        Query q = session.createQuery("SELECT username FROM Korisnik WHERE username='" + korisnik.getUsername() + "'");
        if (!q.list().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", "Contact admin."));
            return "";
        }

        if (!korisnik.getPassword().equals(password)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords dont match", "Contact admin."));
            return "";
        }

        try {
            InternetAddress emailAddr = new InternetAddress(korisnik.getEmail());
            emailAddr.validate();
        } catch (AddressException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email not in correct form", "Contact admin."));
            return "";
        }

        String encryptedString = digestPassword(korisnik.getPassword());

        korisnik.setPassword(encryptedString);
//        try {
//            Blob blob = new SerialBlob(file.getContents());
//        } catch (SQLException ex) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cant upload picture", "Contact admin."));
//            return "";
//        }
        korisnik.setSlika(file.getContents());

        session.save(korisnik);
        session.getTransaction().commit();
        session.close();

        username = "";
        korisnik = null;
        return "/faces/index?faces-redirect=true";
    }

    public String toPromenaLozinke() {
        return "/faces/promenaLozinke?faces-redirect=true";
    }

    public String promenaLozinke() {
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();

        password = digestPassword(password);

        Query q = session.createQuery("from Korisnik where username = :u and password = :p");
        q.setParameter("u", username);
        q.setParameter("p", password);
        if (q.list().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong username or password", "Contact admin."));
            password = "";
            return "";
        }

        ponovljenPassword = digestPassword(ponovljenPassword);

        korisnik = (Korisnik) q.list().get(0);
        korisnik.setPassword(ponovljenPassword);
        session.update(korisnik);
        session.getTransaction().commit();

        session.close();

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/faces/index?faces-redirect=true";
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

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Uspesno ste se izlogovali", "User logout."));
        return "/faces/index?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public StreamedContent getSlika() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes.
//            String imageId = context.getExternalContext().getRequestParameterMap().get("imageId");
            return new DefaultStreamedContent(new ByteArrayInputStream(korisnik.getSlika()));
        }
    }

    public void setSlika(StreamedContent slika) {
        this.slika = slika;
    }

    public String getPonovljenPassword() {
        return ponovljenPassword;
    }

    public void setPonovljenPassword(String ponovljenPassword) {
        this.ponovljenPassword = ponovljenPassword;
    }

}
