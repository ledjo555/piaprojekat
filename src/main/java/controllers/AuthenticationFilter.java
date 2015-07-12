package controllers;


import DB.Korisnik;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AuthenticationFilter implements Filter {
  private FilterConfig config;
  private static  Map<String,String[]> lista=new HashMap<String, String[]>();
  static{
      lista.put("demonstrator.xhtml",new String[] {"Demonstrator"});
      lista.put("demonstratorIsplata.xhtml",new String[] {"Demonstrator"});
      lista.put("demonstratorNoviLab.xhtml",new String[] {"Demonstrator"});
      lista.put("demonstratorPrijava.xhtml",new String[] {"Demonstrator"});
      lista.put("demonstratorZavrsenLab.xhtml",new String[] {"Demonstrator"});
      lista.put("nastavnik.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikArhivaLab.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikDemonstratorDetalji.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikDodajDemonstratora.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikUnosLab.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikZakljuciLab.xhtml",new String[] {"Nastavnik"});
      lista.put("nastavnikZakljuciLabEdit.xhtml",new String[] {"Nastavnik"});
      lista.put("administrator.xhtml",new String[] {"Administrator"});
      lista.put("administratorRegistracija.xhtml",new String[] {"Administrator"});
      lista.put("administratorDodajPredmet.xhtml",new String[] {"Administrator"});
      lista.put("administratorPostaviNastavnika.xhtml",new String[] {"Administrator"});
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
      
    if (((HttpServletRequest) req).getSession().getAttribute("user") == null) {
      ((HttpServletResponse) resp).sendRedirect("../index.xhtml");
    } else {
        Korisnik user=(Korisnik) ((HttpServletRequest) req).getSession().getAttribute("user");
       System.out.println(user.getUsername());
       String url = ((HttpServletRequest)req).getRequestURL().toString();
       String[] parts=url.split("/");
       String[] level=lista.get(parts[parts.length-1]);
       
      if(level!=null){
          if(isInList(level,user.getTip())){
              chain.doFilter(req, resp);
              return;
          }
      }
              
     ((HttpServletResponse) resp).sendRedirect("../error.xhtml");
    }
     
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    this.config = config;
  }

  @Override
  public void destroy() {
    config = null;
  }
  
  private boolean isInList(String[] list,String val){
      
      for(String i:list)
          if(i.equals(val)) return true;
      
      return false;
  }
}
