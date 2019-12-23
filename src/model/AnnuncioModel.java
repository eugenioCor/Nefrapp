package model;


import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import java.util.ArrayList;

import org.apache.taglibs.standard.lang.jstl.AndOperator;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import bean.Annuncio;
import bean.Messaggio;
import bean.Paziente;
import utility.CreaBeanUtility;

public class AnnuncioModel {
	
	public static Annuncio getAnnuncioById(String idAnnuncio) {
		MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
		Document annuncioDoc = annunci.find(eq("_id", new ObjectId(idAnnuncio))).first();
		if (annuncioDoc != null) {
			Annuncio messaggio = CreaBeanUtility.daDocumentAAnnuncio(annuncioDoc);
			return messaggio;
		}
		return null;
	}
	
	public static void addAnnuncio(Annuncio daAggiungere) {
		MongoCollection<Document> annuncioDB = DriverConnection.getConnection().getCollection("Annuncio");
		
		ArrayList<String> codiciFiscaliPazienti = new ArrayList<String>();
		for (Paziente paziente: daAggiungere.getPazienti()) {
			codiciFiscaliPazienti.add(paziente.getCodiceFiscale());
		}
		
		Document doc = new Document("MedicoCodiceFiscale", daAggiungere.getMedico().getCodiceFiscale())
				.append("PazientiCodiceFiscale", codiciFiscaliPazienti)
				.append("Titolo", daAggiungere.getTitolo())
				.append("Testo", daAggiungere.getTesto())
				.append("Allegato", daAggiungere.getAllegato())
				.append("Data", daAggiungere.getData().toInstant());
		annuncioDB.insertOne(doc);
	}
	
	public static ArrayList<Annuncio> getAnnunciByCFMedico(String codiceFiscaleMedico) {
		MongoCollection<Document> annuncioDB = DriverConnection.getConnection().getCollection("Annuncio");
		ArrayList<Annuncio> annunci = new ArrayList<>();
		MongoCursor<Document> documenti = annuncioDB.find(eq("MedicoCodiceFiscale", codiceFiscaleMedico)).iterator();

		while (documenti.hasNext()) {
			annunci.add(CreaBeanUtility.daDocumentAAnnuncio(documenti.next()));
		}
		
		return annunci;
	}
	
	public static ArrayList<Annuncio> getAnnuncioByCFPaziente(String codiceFiscalePaziente) {
		MongoCollection<Document> annuncioDB = DriverConnection.getConnection().getCollection("Annuncio");
		ArrayList<Annuncio> annunci = new ArrayList<>();
		MongoCursor<Document> documenti = annuncioDB.find(eq("PazientiCodiceFiscale", codiceFiscalePaziente)).iterator();

		while (documenti.hasNext()) {
			annunci.add(CreaBeanUtility.daDocumentAAnnuncio(documenti.next()));
		}
		
		return annunci;
	}
	
	/*
	 * public static String getIdAnnuncio(Annuncio daOttenere) {
		MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
		ArrayList<String> codiciFiscaliPazienti = new ArrayList<String>();
		for (Paziente paziente: daOttenere.getPazienti()) {
			codiciFiscaliPazienti.add(paziente.getCodiceFiscale());
		}
		
		Document annuncioDoc = annunci.find(and(eq("MedicoCodiceFiscale", daOttenere.getMedico().getCodiceFiscale()), eq("PazientiCodiceFiscale",codiciFiscaliPazienti), eq("Titolo", daOttenere.getTitolo()), eq("Testo", daOttenere.getTesto()), eq("Data", daOttenere.getData().toInstant()))).first();
		if (annuncioDoc != null) {
			return annuncioDoc.get("_id").toString();
		}
		return null;
		
	}
	*/
	
	
	
}	
	