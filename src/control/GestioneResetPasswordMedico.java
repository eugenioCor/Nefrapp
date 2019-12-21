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

import bean.Amministratore;
import bean.Medico;
import bean.Paziente;
import model.MedicoModel;
import utility.InvioEmailUtility;

/**
 * @author Davide Benedetto Strianese,
 * Questa classe � una servlet che si occupa del reset della password di un medico
 */
@WebServlet("/GestioneResetPasswordMedico")
public class GestioneResetPasswordMedico extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private RequestDispatcher dispatcher;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			if("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				request.setAttribute("notifica", "Errore generato dalla richiesta!");
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(""); //TODO reindirizzamento home
				dispatcher.forward(request, response);
				return;
			}
			
			HttpSession session = request.getSession();
			Medico medico = (Medico) session.getAttribute("medico");
			Paziente paziente = (Paziente) session.getAttribute("paziente");
			Amministratore amministratore = (Amministratore) session.getAttribute("amministratore");
			
			//Controllo per verifica se c'� un utente in sessione, se � presente allora si reindirizza alla home
			if(medico != null || paziente != null || amministratore != null) {
				request.setAttribute("notifica", "Non � possibile effettuare questa operazione se si � loggati");
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
				dispatcher.forward(request, response);
				return;
			}
			
			String operazione = request.getParameter("operazione");
			
			//Viene scelta l'operaizione per richiedere il reset della password
			if(operazione.equals("richiesta")) {
				richiediReset(request, response);
				dispatcher.forward(request, response);
				return;
			}
			
			//Operaizone per effettuare il reset della password
			else if(operazione.equals("reset")) {
				effettuaReset(request, response);
				dispatcher.forward(request, response);
				return;
			}
			
			else {
				request.setAttribute("notifica", "Operazione scelta non valida");
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/paginaErrore.jsp");
				dispatcher.forward(request, response);
			}
			
		}catch(Exception e) {
			request.setAttribute("notifica", "Errore in Gestione reset password medico" + e.getMessage());
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("/paginaErrore.jsp");
			requestDispatcher.forward(request,response);
		}
		
		return;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
		return; 
	}
	
	private void richiediReset(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		
		if(validaMail(email)) {
			try {
				request.setAttribute("notifica", "Richiesta reset password inviata");
				InvioEmailUtility.inviaEmail(email);
				dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
				return;
				
			}catch(Exception e) {
				request.setAttribute("notifica", "Si � veriifcato un'erorre durante l'invio dell'eamil. Riprova");
				dispatcher = getServletContext().getRequestDispatcher("/richiestaResetView.jsp");
				return;
			}
		}
		else{
			request.setAttribute("notifica", "Email inserita non valida");
			dispatcher = getServletContext().getRequestDispatcher("/richiestaResetView.jsp");
			return;
		}
	}
	
	private void effettuaReset(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String email = request.getParameter("email");
		String codiceFiscale = request.getParameter("codiceFiscale");
		String password = request.getParameter("password");
		String confermaPsw = request.getParameter("confermaPsw");
		
		if(validaReset(email, codiceFiscale, password, confermaPsw)) {
			Medico medico = MedicoModel.getMedicoByCF(codiceFiscale);
			
			if(medico.getEmail().equals(email)) {
				//TODO modificare password nel db
				request.setAttribute("notifica", "Reset password avvenuto con successo");
				dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
				return;
			}
			else{
				request.setAttribute("notifica", "Codice fiscale ed indirizzo email non appartengono allo stesso accounto. Riprova");
				dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
				return;
			}
		}
		else {
			request.setAttribute("notifica", "Formato parametri non valido");
			dispatcher = getServletContext().getRequestDispatcher("/richiestaResetView.jsp");
			return;
		}
	}
	
	private boolean validaMail(String email) {
		
		final String expEmail = "^[A-Za-z0-9_.-]+@[a-zA-Z.]{2,}\\.[a-zA-Z]{2,3}$";
		
		if(Pattern.matches(expEmail, email))
			return true;
		
		return false;
	}
	
	private boolean validaReset(String email, String codiceFiscale, String password, String confermaPsw) {
		boolean valido = true;
		
		final String expCodiceFiscale = "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$";
		final String expEmail = "^[A-Za-z0-9_.-]+@[a-zA-Z.]{2,}\\.[a-zA-Z]{2,3}$";
		final String expPassword = "^[a-zA-Z0-9]*$";
		
		if (!Pattern.matches(expCodiceFiscale, codiceFiscale) || codiceFiscale.length() != 16)
			valido = false;
		
		if (!Pattern.matches(expPassword, password) || password.length() < 6 || password.length() > 20)
			valido = false;
		
		if(!Pattern.matches(expEmail, email) || email.length() < 6 || email.length() > 50)
			valido = false;
				
		if(!password.equals(confermaPsw))
			valido = false;
		
		return valido;
	}
}