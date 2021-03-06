package test.control;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import bean.Annuncio;
import bean.AnnuncioCompleto;
import bean.AnnuncioProxy;
import bean.Medico;
import bean.Paziente;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import control.GestioneAnnunci;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import model.AnnuncioModel;
import model.DriverConnection;
import model.MessaggioModel;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import utility.CreaBeanUtility;
import utility.CriptazioneUtility;

public class GestioneAnnunciTest {
  private static Annuncio annuncio;
  private static Medico medico;
  private static Paziente paziente;
  private static String nomeAllegato = "bG9yZW0taXBzdW0uanBn";
  private static String corpoAllegato = "CWFubnVuY2lvLnNldENvcnBvQWxsZWdhdG8oIkxvcmVtIGlwc3VtIGRvbG9yIHNpdCBhbWV0LCBjb25zZWN0ZXR1ciBhZGlwaXNjaW5nIGVsaXQsIHNlZCBkbyBlaXVzbW9kIHRlbXBvciBpbmNpZGlkdW50IHV0IGxhYm9yZSBldCBkb2xvcmUgbWFnbmEgYWxpcXVhLiBVdCBlbmltIGFkIG1pbmltIHZlbmlhbSwgcXVpcyBub3N0cnVkIGV4ZXJjaXRhdGlvbiB1bGxhbWNvIGxhYm9yaXMgbmlzaSB1dCBhbGlxdWlwIGV4IGVhIGNvbW1vZG8gY29uc2VxdWF0LiBEdWlzIGF1dGUgaXJ1cmUgZG9sb3IgaW4gcmVwcmVoZW5kZXJpdCBpbiB2b2x1cHRhdGUgdmVsaXQgZXNzZSBjaWxsdW0gZG9sb3JlIGV1IGZ1Z2lhdCBudWxsYSBwYXJpYXR1ci4gRXhjZXB0ZXVyIHNpbnQgb2NjYWVjYXQgY3VwaWRhdGF0IG5vbiBwcm9pZGVudCwgc3VudCBpbiBjdWxwYSBxdWkgb2ZmaWNpYSBkZXNlcnVudCBtb2xsaXQgYW5pbSBpZCBlc3QgbGFib3J1bS4iKTsK";
  private static String titolo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit volutpat.";
  private static String testo = "Il file in allegato contiene istruzioni per i pazienti su come effettuare la dialisi peritoneale.";
  private static String CFMedico = "GRMBNN67L11B516R";
  private static String CFPaziente1 = "BNCLRD67A01F205I";
  private static HashMap<String, Boolean> destinatari;
  private GestioneAnnunci servlet;
  private MockMultipartHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    MongoCollection<Document> pazienti = DriverConnection.getConnection().getCollection("Paziente");
    ArrayList<String> campoMedici = new ArrayList<>();
    campoMedici.add(CFMedico);
    String password1 = CriptazioneUtility.criptaConMD5("Fiori5678");
    Document doc1 = new Document("CodiceFiscale", CFPaziente1).append("Password", password1).append("Attivo", true).append("Medici", campoMedici);;
    pazienti.insertOne(doc1);
    paziente = CreaBeanUtility.daDocumentAPaziente(doc1);
    paziente.setMedici(campoMedici);


    MongoCollection<Document> medici = DriverConnection.getConnection().getCollection("Medico");
    String password2 = CriptazioneUtility.criptaConMD5("Quadri1234");
    Document doc2 = new Document("CodiceFiscale", CFMedico).append("Password", password2).append("Attivo", true);
    medici.insertOne(doc2);
    medico = CreaBeanUtility.daDocumentAMedico(doc2);
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception {
    MongoCollection<Document> pazienti = DriverConnection.getConnection().getCollection("Paziente");
    FindIterable<Document> docs = pazienti.find(eq("CodiceFiscale", paziente.getCodiceFiscale()));
    for (Document d : docs) {
      pazienti.deleteOne(d);
    }

    MongoCollection<Document> medici = DriverConnection.getConnection().getCollection("Medico");
    FindIterable<Document> docs2 = medici.find(eq("CodiceFiscale", medico.getCodiceFiscale()));
    for (Document d : docs2) {
      medici.deleteOne(d);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new GestioneAnnunci();
    request = new MockMultipartHttpServletRequest();
    response = new MockHttpServletResponse();

    destinatari = new HashMap<String, Boolean>();
    destinatari.put(CFPaziente1, false);
    annuncio = new AnnuncioCompleto(CFMedico,titolo,testo,nomeAllegato,corpoAllegato,ZonedDateTime.now(ZoneId.of("Europe/Rome")), destinatari);

    MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
    ArrayList<Document> destinatariView = new ArrayList<Document>();
    Iterator it = annuncio.getPazientiView().entrySet().iterator();

    if (!it.hasNext()) {
      Document coppia = new Document();
      coppia.append("CFDestinatario", null).append("Visualizzazione", false);
      destinatariView.add(coppia);
    } else {
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        Document coppia = new Document();
        coppia.append("CFDestinatario", pair.getKey()).append("Visualizzazione", pair.getValue());
        destinatariView.add(coppia);
      }
    }

    Document allegato = new Document("NomeAllegato", annuncio.getNomeAllegato()).append("CorpoAllegato",
        annuncio.getCorpoAllegato());

    Document doc = new Document("MedicoCodiceFiscale", annuncio.getMedico())
        .append("Titolo", annuncio.getTitolo()).append("Testo", annuncio.getTesto())
        .append("Allegato", allegato).append("Data", annuncio.getData().toInstant())
        .append("PazientiView", destinatariView);
    annunci.insertOne(doc);

    ObjectId idObj = (ObjectId)doc.get("_id");
    annuncio.setIdAnnuncio(idObj.toString());
  }

  @AfterEach
  void tearDown() throws Exception {
    MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
    FindIterable<Document> messaggioDoc2 = annunci.find(eq("MedicoCodiceFiscale", CFMedico))
        .projection(Projections.include("_id"));
    for (Document d : messaggioDoc2) {
      annunci.deleteOne(d);
    }
    annuncio = null;
    request.getSession().invalidate();
  }

  @Test
  void testAnnuncioSenzaOperazione() throws ServletException, IOException {
    servlet.doGet(request, response);
    assertEquals("./paginaErrore.jsp?notifica=noOperazione", response.getRedirectedUrl());
  }
  
  @Test
  void testAnnuncioOperazioneInvalida() throws ServletException, IOException {
    request.setParameter("operazione", "operazioneInvalida");
    servlet.doGet(request, response);
    assertEquals("./paginaErrore.jsp?notifica=noOperazione", response.getRedirectedUrl());
  }

  @Test
  void TC_GM_9_1_InvioAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.setParameter("operazione", "caricaAllegato");
    request.setParameter("selectPaziente", CFPaziente1);
    servlet.doPost(request, response);

    request.setParameter("oggetto", "Lorem ipsum dolor sit amet, consectetur adipiscing elit volutpat and blabla and blablabla.");
    request.setParameter("testo", testo);
    request.setParameter("operazione", "inviaAnnuncio");
    servlet.doPost(request, response);
    assertEquals("./dashboard.jsp?notifica=comunicazioneNonInviata", response.getRedirectedUrl());
  }

  @Test
  void TC_GM_9_2_InvioAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.setParameter("operazione", "caricaAllegato");
    request.setParameter("selectPaziente", CFPaziente1);
    servlet.doPost(request, response);

    request.setParameter("oggetto", titolo);
    request.setParameter("testo", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In sollicitudin nisi sit amet nibh congue scelerisque. Donec ornare pharetra erat, at lobortis tellus commodo eu. Integer gravida nulla non risus aliquam, elementum mollis turpis fringilla. Sed vel commodo ante. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Vestibulum viverra diam fermentum sapien cursus scelerisque. Praesent porta ac diam eu tempus. Pellentesque pellentesque nisi enim, non pellentesque mauris maximus nec. Nunc et enim eu purus porta molestie eget sed libero. Donec ullamcorper ligula orci, eu molestie lorem pharetra at. Nulla tortor tellus, varius sagittis congue quis, lobortis et metus. Sed elementum varius justo, et sollicitudin lectus porttitor at.\r\n" 
    + "Integer porta diam nec commodo consequat. Pellentesque pharetra vel lacus nec condimentum. Vivamus metus tortor, mattis non pulvinar in, mollis quis nibh. Cras ultrices vel orci eu bibendum. In pulvinar, lacus vitae cras amet.\r\n" 
    + "");
    request.setParameter("operazione", "inviaAnnuncio");
    servlet.doPost(request, response);
    assertEquals("./dashboard.jsp?notifica=comunicazioneNonInviata", response.getRedirectedUrl());
  }

  @Test
  void TC_GM_9_3_InvioAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    final String fileName = "test.txt";
    final byte[] content = "Hallo Word".getBytes();
    MockMultipartFile mockMultipartFile = 
        new MockMultipartFile("content", fileName, "image/jpeg", content);

    request.addFile(mockMultipartFile);

    request.setParameter("operazione", "caricaAllegato");
    servlet.doPost(request, response);

    request.setParameter("oggetto", titolo);
    request.setParameter("testo", testo); 
    request.setParameter("operazione", "inviaAnnuncio");
    servlet.doPost(request, response);
    if (request.getAttribute("erroreCaricamento") != null) {
      assertEquals(request.getAttribute("erroreCaricamento"), true);
    } else {
      fail("il caricamento è andato a buon fine");
    }
  }

  @Test
  void TC_GM_9_4_InvioAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    Part dimensioneErrata = null;

    request.setParameter("operazione", "caricaAllegato");
    servlet.doPost(request, response);

    request.setParameter("oggetto", titolo);
    request.setParameter("testo", testo);
    request.setParameter("operazione", "inviaAnnuncio");
    servlet.doPost(request, response);
    if (request.getAttribute("erroreCaricamento") != null) {
      assertEquals(request.getAttribute("erroreCaricamento"), true);
    } else {
      fail("il caricamento è andato a buon fine");
    }
  }
  
  @Test
  void TC_GM_9_5_InvioAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);

    request.setParameter("oggetto", titolo);
    request.setParameter("testo", testo);
    request.setParameter("operazione", "inviaAnnuncio");
    request.setParameter("selectPaziente", paziente.getCodiceFiscale());
    servlet.doPost(request, response);
    
    assertEquals("./dashboard.jsp?notifica=annuncioInviato", response.getRedirectedUrl());
  }

  @Test
  void TC_GP_10_RicezioneAnnunci() throws ServletException, IOException {
    request.getSession().setAttribute("utente", paziente);
    request.getSession().setAttribute("isPaziente", true);
    request.getSession().setAttribute("accessDone", true);

    ArrayList<AnnuncioProxy> annunciP = new ArrayList<>();

    Annuncio secondoAnnuncio = new AnnuncioCompleto(CFMedico,titolo,testo,null,null,ZonedDateTime.now(ZoneId.of("Europe/Rome")), annuncio.getPazientiView());
    MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
    ArrayList<Document> destinatariView = new ArrayList<Document>();
    Iterator it = secondoAnnuncio.getPazientiView().entrySet().iterator();

    if (!it.hasNext()) {
      Document coppia = new Document();
      coppia.append("CFDestinatario", null).append("Visualizzazione", false);
      destinatariView.add(coppia);
    } else {
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        Document coppia = new Document();
        coppia.append("CFDestinatario", pair.getKey()).append("Visualizzazione", pair.getValue());
        destinatariView.add(coppia);
      }
    }

    Document allegato = new Document("NomeAllegato", secondoAnnuncio.getNomeAllegato()).append("CorpoAllegato",
        secondoAnnuncio.getCorpoAllegato());


    Document doc = new Document("MedicoCodiceFiscale", secondoAnnuncio.getMedico())
        .append("Titolo", secondoAnnuncio.getTitolo()).append("Testo", secondoAnnuncio.getTesto())
        .append("Allegato", allegato).append("Data", secondoAnnuncio.getData().toInstant())
        .append("PazientiView", destinatariView);
    annunci.insertOne(doc);

    ObjectId idObj = (ObjectId)doc.get("_id");
    secondoAnnuncio.setIdAnnuncio(idObj.toString());

    AnnuncioProxy primo = new AnnuncioProxy(annuncio.getMedico(), annuncio.getTitolo(),annuncio.getTesto(),annuncio.getNomeAllegato(),
        annuncio.getData(), annuncio.getPazientiView());
    AnnuncioProxy secondo = new AnnuncioProxy(secondoAnnuncio.getMedico(), secondoAnnuncio.getTitolo(),secondoAnnuncio.getTesto(),secondoAnnuncio.getNomeAllegato(),
        secondoAnnuncio.getData(), secondoAnnuncio.getPazientiView());
    primo.setIdAnnuncio(annuncio.getIdAnnuncio());
    secondo.setIdAnnuncio(secondoAnnuncio.getIdAnnuncio());
    //a questo punto non ha ancora settato la visualizzazione
    primo.setVisualizzato(null);
    secondo.setVisualizzato(null);
    //l'ordine di inserimento va invertito rispetto all'ordine di aggiunta al database
    //perché nel mostrare la lista il model sceglie prima i messaggi più recenti

    annunciP.add(secondo);
    annunciP.add(primo);

    request.setParameter("operazione", "visualizzaPersonali");
    servlet.doGet(request, response);

    assertEquals(annunciP.toString(), request.getAttribute("annunci").toString());
  }
  
  @Test
  void LetturaAnnunciMedico() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);

    ArrayList<AnnuncioProxy> annunciP = new ArrayList<>();

    Annuncio secondoAnnuncio = new AnnuncioCompleto(CFMedico,titolo,testo,null,null,ZonedDateTime.now(ZoneId.of("Europe/Rome")), annuncio.getPazientiView());
    MongoCollection<Document> annunci = DriverConnection.getConnection().getCollection("Annuncio");
    ArrayList<Document> destinatariView = new ArrayList<Document>();
    Iterator it = secondoAnnuncio.getPazientiView().entrySet().iterator();

    if (!it.hasNext()) {
      Document coppia = new Document();
      coppia.append("CFDestinatario", null).append("Visualizzazione", false);
      destinatariView.add(coppia);
    } else {
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        Document coppia = new Document();
        coppia.append("CFDestinatario", pair.getKey()).append("Visualizzazione", pair.getValue());
        destinatariView.add(coppia);
      }
    }

    Document allegato = new Document("NomeAllegato", secondoAnnuncio.getNomeAllegato()).append("CorpoAllegato",
        secondoAnnuncio.getCorpoAllegato());


    Document doc = new Document("MedicoCodiceFiscale", secondoAnnuncio.getMedico())
        .append("Titolo", secondoAnnuncio.getTitolo()).append("Testo", secondoAnnuncio.getTesto())
        .append("Allegato", allegato).append("Data", secondoAnnuncio.getData().toInstant())
        .append("PazientiView", destinatariView);
    annunci.insertOne(doc);

    ObjectId idObj = (ObjectId)doc.get("_id");
    secondoAnnuncio.setIdAnnuncio(idObj.toString());

    AnnuncioProxy primo = new AnnuncioProxy(annuncio.getMedico(), annuncio.getTitolo(),annuncio.getTesto(),annuncio.getNomeAllegato(),
        annuncio.getData(), annuncio.getPazientiView());
    AnnuncioProxy secondo = new AnnuncioProxy(secondoAnnuncio.getMedico(), secondoAnnuncio.getTitolo(),secondoAnnuncio.getTesto(),secondoAnnuncio.getNomeAllegato(),
        secondoAnnuncio.getData(), secondoAnnuncio.getPazientiView());
    primo.setIdAnnuncio(annuncio.getIdAnnuncio());
    secondo.setIdAnnuncio(secondoAnnuncio.getIdAnnuncio());
    //a questo punto non ha ancora settato la visualizzazione
    primo.setVisualizzato(null);
    secondo.setVisualizzato(null);
    //l'ordine di inserimento va invertito rispetto all'ordine di aggiunta al database
    //perché nel mostrare la lista il model sceglie prima i messaggi più recenti

    annunciP.add(secondo);
    annunciP.add(primo);

    request.setParameter("operazione", "visualizzaPersonali");
    servlet.doGet(request, response);

    assertEquals(annunciP.toString(), request.getAttribute("annunci").toString());
  }
  
  @Test
  void LetturaAnnunciListaVuota() throws Exception {
    tearDown();
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);

    ArrayList<AnnuncioProxy> annunciP = new ArrayList<>();

    request.setParameter("operazione", "visualizzaPersonali");
    servlet.doGet(request, response);

    assertEquals(annunciP.toString(), request.getAttribute("annunci").toString());
  }

  @Test
  void testCaricaDestinatariAnnuncio() throws ServletException, IOException {
    ArrayList<Paziente> pazienti = new ArrayList<Paziente>();
    pazienti.add(paziente);

    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);

    request.setParameter("operazione", "caricaDestinatariAnnuncio");
    servlet.doGet(request, response);

    System.out.println(request.getAttribute("pazientiSeguiti").toString());

    assertEquals(pazienti.toString(), request.getAttribute("pazientiSeguiti").toString());
  }

  @Test
  void testRimuoviAllegato() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);
    request.getSession().setAttribute("id",annuncio.getIdAnnuncio());

    request.setParameter("operazione", "rimuoviAllegato");
    servlet.doPost(request, response);

    assertNull(AnnuncioModel.getAnnuncioById(annuncio.getIdAnnuncio()));
  }

  @Test
  void testRimuoviAnnuncio() throws ServletException, IOException {
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);
    request.setParameter("id",annuncio.getIdAnnuncio());

    request.setParameter("operazione", "rimuoviAnnuncio");
    servlet.doGet(request, response);

    assertNull(MessaggioModel.getMessaggioById(annuncio.getIdAnnuncio()));
  }
  
  @Test
  void testRimuoviAnnuncioIncompleto() throws ServletException, IOException {
    MongoCollection<Document> annunci = 
        DriverConnection.getConnection().getCollection("Annuncio");
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);
    request.getSession().setAttribute("id", annuncio.getIdAnnuncio());

    Document d = 
        annunci.find(eq("_id", new ObjectId(annuncio.getIdAnnuncio())))
        .projection(Projections.exclude("_id"))
        .first().append("MedicoCodiceFiscale", null);
    annunci.insertOne(d);
    ObjectId id = (ObjectId)d.get("_id");

    request.setParameter("operazione", "rimuoviAnnuncioIncompleto");
    servlet.doPost(request, response);

    assertNull(AnnuncioModel.getAnnuncioById(id.toString()));
  }
  
  @Test
  void testGeneraDownload() throws ServletException, IOException {
    PrintWriter pw;
    request.getSession().setAttribute("utente", medico);
    request.getSession().setAttribute("isMedico", true);
    request.getSession().setAttribute("accessDone", true);
    request.setParameter("id",annuncio.getIdAnnuncio());

    request.setParameter("operazione", "generaDownload");
    servlet.doGet(request, response);

    pw = response.getWriter();
    assertNotNull(pw);
  }
  
  

}
