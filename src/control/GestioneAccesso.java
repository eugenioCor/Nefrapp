package control;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import bean.Amministratore;
import model.AmministratoreModel;
import utility.AlgoritmoCriptazioneUtility;

@WebServlet("/LoginAmministratore")
public class GestioneAccesso extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//Verifica del tipo di chiamata alla servlet (sincrona o asinconrona)(sincrona ok)
		try {
			if("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
				resp.setContentType("application/json");
				resp.setHeader("Cache-Control", "no-cache");
				resp.getWriter().write(new Gson().toJson("Errore generato dalla richiesta!"));
				return;
			}
			
			String codiceFiscale = req.getParameter("codiceFiscale");
			String password = req.getParameter("password");
			String flag = req.getParameter("flag");
			HttpSession session = req.getSession();
			synchronized (session) {
				if(flag.equalsIgnoreCase("admin"))
				{
					Amministratore amministratore = null;
					if(controllaParametri(codiceFiscale, password))
					{
						password = AlgoritmoCriptazioneUtility.criptaConMD5(password);
						amministratore =AmministratoreModel.checkLogin(codiceFiscale, password);
						if(amministratore != null)
						{
							session.setAttribute("amministratore", amministratore);
							resp.sendRedirect("paginaAmministratore.jsp");
						}
						else
						{
							resp.sendRedirect("loginAmministratore.jsp");
						}
					}
				}	
			
			}
		} catch (Exception e) {
			System.out.println("Errore in gestione parametri:");
			e.printStackTrace();		
		}
		
		return;	
	
	}
	
	
	public boolean controllaParametri(String codiceFiscale, String password)
	{
		boolean valido=true;
		String expCodiceFiscale="^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$";
		String expPassword="^[a-zA-Z0-9]*$";
		
		if (!Pattern.matches(expCodiceFiscale, codiceFiscale)||codiceFiscale.length()!=16)
			valido=false;
		if (!Pattern.matches(expPassword, password)||password.length()<6||password.length()>20)
			valido=false;
		return valido;
	}
	
}