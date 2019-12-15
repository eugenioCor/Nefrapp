<%@  taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
    	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    	<meta name="description" content="">
    	<meta name="author" content="">
		<title>Pagina bianca</title>
    	
    	<!-- Custom fonts for this template-->
    	<link href="../vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">
    	<link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">

    	<!-- Custom styles for this template-->
   	 	<link href="../css/sb-admin-2.min.css" rel="stylesheet">	
	</head>
	
	<body id="page-top">
	<jsp:include page="/parametri">
	 <jsp:param name="codiceFiscale" value="RSSGPP95L18I483O"/>
	 <jsp:param name="flag" value="1"/>
	</jsp:include>
	
		<!-- Page Wrapper -->
	    <div id="wrapper">
			<%@include file="../includes/sidebar.jsp" %>		
	
	        <!-- Content Wrapper -->
	        <div id="content-wrapper" class="d-flex flex-column">
	
	            <!-- Main Content -->
	            <div id="content">
					<%@include file="../includes/header.jsp" %>
	
	
	                <!-- Begin Page Content -->
        <div class="container-fluid">

          <!-- Page Heading -->
          <h1 class="h3 mb-2 text-gray-800">Parametri inseriti dal paziente (Nome, cognome) </h1>
          
          <!-- DataTales Example -->
          <div class="card shadow mb-4">
            
            <div class="card-body">
              <div class="table-responsive">
                <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
                  <thead>
                    <tr>
                      <th>Peso(Kg)</th>
                      <th>Pressione arteriosa minima(mmHg)</th>
                      <th>Pressione arteriosa massima(mmHg)</th>
                      <th>Scarico iniziale(ml)</th>
                      <th>Ultrafiltrato(ml)</th>
                      <th>Tempo di sosta</th>
                      <th>Carico diurno(ml)</th>
                      <th>Scarico diurno(ml)</th>
                      <th>Data</th>
                      <!-- Possibile aggiunta di atri campi -->
                    </tr>
                  </thead>
                  <tfoot> <!-- Sarebbe il footer della tabella-->
                    <tr>
                      <th>Peso(Kg)</th>
                      <th>Pressione arteriosa minima(mmHg)</th>
                      <th>Pressione arteriosa massima(mmHg)</th>
                      <th>Scarico iniziale(ml)</th>
                      <th>Ultrafiltrato(ml)</th>
                      <th>Tempo di sosta</th>
                      <th>Carico diurno(ml)</th>
                      <th>Scarico diurno(ml)</th>
                      <th>Data</th>
                      <!-- Possibile aggiunta di altri campi -->
                    </tr>
                  </tfoot>
                  <tbody>
                  	
                  	<c:set var="scheda" value='${requestScope["schedaParametri"]}'/>
                  	
                  	<!-- Inizio iterazione dei risultati ottenuti dalla servlet (parametri inseriti dal paziente) -->
             		<c:forEach items="${scheda}" var="item">
                    <tr>
                      <td>${item.peso}</td>
                      <td>${item.paMin}</td>
                      <td>${item.paMax}</td>
                      <td>${item.scaricoIniziale}</td>
                      <td>${item.UF}</td>
                      <td>${item.tempoSosta}</td>
                      <td>${item.scarico}</td>
                      <td>${item.carico}</td>
                      <td>${item.data}</td>
                    </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

        </div>
	                <!-- /.container-fluid -->
	
	            </div>
	            <!-- End of Main Content -->
				<%@include file="../includes/footer.jsp" %>	
	        </div>
	        <!-- End of Content Wrapper -->
	
	    </div>
	    <!-- End of Page Wrapper -->
	
	    <!-- Scroll to Top Button-->
	    <a class="scroll-to-top rounded" href="#page-top">
	        <i class="fas fa-angle-up"></i>
	    </a>
	
	    <!-- Logout Modal-->
	    <div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
	        <div class="modal-dialog" role="document">
	            <div class="modal-content">
	                <div class="modal-header">
	                    <h5 class="modal-title" id="exampleModalLabel">Ready to Leave?</h5>
	                    <button class="close" type="button" data-dismiss="modal" aria-label="Close">
	            <span aria-hidden="true">�</span>
	          </button>
	                </div>
	                <div class="modal-body">Select "Logout" below if you are ready to end your current session.</div>
	                <div class="modal-footer">
	                    <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
	                    <a class="btn btn-primary" href="login.html">Logout</a>
	                </div>
	            </div>
	        </div>
	    </div>
	
	</body>
</html>