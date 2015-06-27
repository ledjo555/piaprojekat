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
import javax.faces.bean.RequestScoped;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Djole
 */
@ManagedBean
@RequestScoped
public class Login {

    private String username;
    private String password;
    private Session session;
    private Korisnik korisnik;

    public Login() {
    }
    
    public String login(){
        session = DBFactory.getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery("from Korisnik where username = :u and password = :p");
        q.setParameter("u", username);
        q.setParameter("p", password);
        if(q.list().isEmpty()){
            return "error";
        }
        korisnik = (Korisnik) q.list().get(0);
        session.close();
        
        return "restricted/demonstrator";
        
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
    
    

}
