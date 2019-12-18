package control;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import bean.Amministratore;
import bean.Medico;
import bean.Paziente;
import model.MedicoModel;
import model.PazienteModel;
import utility.AlgoritmoCriptazioneUtility;

/**
 * @author Luca Esposito, Antonio Donnarumma, Davide Benedetto Strianese,
 * Questa classe � una servlet che si occupa della registrazione di un utente del sistema.
 */
@WebServlet("/GestioneRegistrazione")
public class GestioneRegistrazione extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
	@Override
	/*
	 * @precondition La richiesta deve essere sincrona
	 * 				 codiceFiscale != null && codiceFiscale.matches("^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$") && codiceFiscale.lenght()==16,
	 * 				 nome != null && nome.matches("^[A-Z][a-zA-Z ]*$") && (nome.lenght()>1 && nome.lenght() < 31),
	 * 				 cognome != null && cognome.matches("^[A-Z][a-zA-Z ]*$") && (cognome.lenght() > 1 && cognome.lenght() < 31),
	 * 				 sesso != null && sesso.matches("^[MF]$") && sesso.lenght() == 1,
	 * 				 email != null && email.matches("^[A-Za-z0-9_.-]+@[a-zA-Z.]{2,}\\.[a-zA-Z]{2,3}$"),
	 * 				 password != null && password.matches("^[a-zA-Z0-9]*$") && (password.lenght() > 4 && password.lenght() < 21),
	 * @postcondition L'utente � stato registrato nel sistema
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try {
			if("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				request.setAttribute("notification", "Errore generato dalla richiesta!");
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(""); //TODO reindirizzamento home
				dispatcher.forward(request, response);
				return;
			}
			
			HttpSession session = request.getSession();
			String operazione = request.getParameter("operazione");
			
			if(operazione.equals("registraMedico")) {
				Amministratore amministratore = (Amministratore) session.getAttribute("amministratore");
				if(amministratore != null) {
					registraMedico(request);
				}
			}else if(operazione.equals("registraPazienteMedico")) { //registrazione paziente per il medico
					Medico medicoLoggato = (Medico) session.getAttribute("medico");
					if(medicoLoggato != null) {
						String registrato = request.getParameter("registrato");
							if(registrato.equals("No")) { //paziente non registrato
								ArrayList<String> medici = new ArrayList<String>();
								medici.add(medicoLoggato.getCodiceFiscale());
								registraPaziente(request, medici);
								
							}else { // solo aggiunta del cf del medico tra i seguiti (paziente già registrato)
									String codiceFiscale = request.getParameter("codiceFiscale");
									if(PazienteModel.getPazienteByCF(codiceFiscale) != null) {
										Paziente paziente = PazienteModel.getPazienteByCF(codiceFiscale);
										paziente.addMedico(medicoLoggato.getCodiceFiscale());
										PazienteModel.updateMediciDelPaziente(paziente);
									}else {
										//TODO gestione errore nel caso in cui paziente non registrato
									}
							}
					}
			}else if(operazione.equals("registraPazienteAmministratore")) { //registrazione
				String codiceFiscalePaziente = request.getParameter("codiceFiscale");
				Paziente paziente = PazienteModel.getPazienteByCF(codiceFiscalePaziente);
				if ( paziente == null) { //aggiunta paziente
					registraPaziente(request, new ArrayList<String>());
				}else{
					//TODO errore paziente già presente
				}
			}
			
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.jsp");
			requestDispatcher.forward(request, response);
		} catch(Exception e) {
			System.out.println("Errore in Inserisci indirizzo:");
			e.printStackTrace();
		}
		
		return;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		doGet(request, response);
		return;
	}
	
	private void registraMedico(HttpServletRequest request) {
		
			String codiceFiscale = request.getParameter("codiceFiscale");
			String nome = request.getParameter("nome");
			String cognome = request.getParameter("cognome");
			String sesso = request.getParameter("sesso");
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			
			if (validazione(codiceFiscale, nome, cognome, sesso, email, password)) {
				//TODO controllare esistenza all'interno del db
				Medico medico = new Medico(sesso, "", null, codiceFiscale, nome, cognome, email,"");
				password = AlgoritmoCriptazioneUtility.criptaConMD5(password);//serve a criptare la pasword in MD5 prima di registrarla nel db ps.non cancellare il commento quando spostate la classe
				MedicoModel.addMedico(medico, password);
			}else {
			//TODO gestione errore validazione
			return;
		}
	}
	
	private void registraPaziente(HttpServletRequest request, ArrayList<String> medici) {
		//TODO gestione della data
		String codiceFiscale = request.getParameter("codiceFiscale");
		if(PazienteModel.getPazienteByCF(codiceFiscale) == null) {
			String nome = request.getParameter("nome");
			String cognome = request.getParameter("cognome");
			String sesso = request.getParameter("sesso");
			System.out.println(sesso);
			String email = request.getParameter("email");
			String password = AlgoritmoCriptazioneUtility.criptaConMD5(request.getParameter("password"));
			String residenza = request.getParameter("residenza");
			String luogoDiNascita=request.getParameter("luogoDiNascita");
			String dataDiNascita = request.getParameter("dataDiNascita");
			Paziente paziente = null;
			
			if (validazione(codiceFiscale, nome, cognome, sesso, email, password)) {
					paziente = new Paziente(sesso, codiceFiscale, nome, cognome, email, residenza, luogoDiNascita, LocalDate.parse(dataDiNascita), true, medici);
					PazienteModel.addPaziente(paziente,password);
			}else {
				System.out.print("errore");
				//TODO gestire caso di errore di validazione
			} 
		} else {
			//TODO errore nel caso in cui paziente già presente
		}
	}
	
	
	
	
	private boolean validazione(String codiceFiscale, String nome, String cognome,String sesso, String email,String password) {
		boolean valido = true;
		String expCodiceFiscale = "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z][0-9]{2}[a-zA-Z][0-9]{3}[a-zA-Z]$";
		String expNome = "^[A-Z][a-zA-Z ]*$";
		String expCognome = "^[A-Z][a-zA-Z ]*$";
		String expSesso = "^[MF]$";
		String expEmail = "^[A-Za-z0-9_.-]+@[a-zA-Z.]{2,}\\.[a-zA-Z]{2,3}$";
		String expPassword = "^[a-zA-Z0-9]*$";
		
		if (!Pattern.matches(expCodiceFiscale, codiceFiscale) || codiceFiscale.length() != 16)
			valido = false;
		if (!Pattern.matches(expNome, nome) || nome.length() < 2 || nome.length() > 30)
			valido = false;
		if (!Pattern.matches(expCognome, cognome) || cognome.length() < 2 || cognome.length() > 30)
			valido = false;
		if (!Pattern.matches(expPassword, password) || password.length() < 6 || password.length() > 20)
			valido = false;
		if (!Pattern.matches(expSesso, sesso) || sesso.length() != 1)
			valido = false;
		if (!Pattern.matches(expEmail, email))
			valido = false;
		return valido;
	}
}