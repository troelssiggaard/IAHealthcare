<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title>IA Monitor</title>
  <link href="css/materialize.css" type="text/css" rel="stylesheet" media="screen,projection"/>
  <link href="css/style.css" type="text/css" rel="stylesheet" media="screen,projection"/>
  <link rel="icon" type="image/png" href="favicon.png"/>
  <!-- // SCRIPTS -->
  <script src="https://code.jquery.com/jquery-2.1.1.min.js"></script>
  <script src="js/updatedata.js"></script>
  <script src="js/materialize.js"></script>
  <script src="js/init.js"></script>
  <!-- // END OF SCRIPTS -->
</head>
<!-- 
//
//	IA Monitor by Troels Siggaard (Thesis Prototype/Demo)
// 
//  Design made with the Materialize CSS Framework (www.materializecss.com)
//  Uses jQuery (v2.1.1) for Clock and JSON (AJAX) updating of information (interruptibility, location & activity)
//  Small changes made to the materialize.css file to get the right layout
//
//  Images (profile pictures) used are by stockimages at FreeDigitalPhotos.net
//
-->
<body>

  <!-- // TITLE, CLOCK AND NAVIGATION -->
  <nav class="blue" role="navigation">
    <div class="nav-wrapper container"><a id="logo-container" href="#" class="brand-logo blue-text text-lighten-5">IA Monitor</a>
      <ul class="right">
      	<li><a class="dropdown-button" href="#" data-activates="dropdown1">Emergency Room<i class="right mdi-navigation-chevron-left"></i></a></li>
      </ul>
      <ul id="dropdown1" class="dropdown-content">
        <li><a href="#">Deparment of Neurology</a></li> <!-- // Dummy link to other department -->
        <li class="divider"></li>
		<li><a href="#">Department of Cardiology</a></li> <!-- // Dummy link to other department -->
        <li class="divider"></li>
        <li><a href="#">Day Surgery Deparment</a></li> <!-- // Dummy link to other department -->
        <li class="divider"></li>
        <li><a href="#">Intensive Care Unit</a></li> <!-- // Dummy link to other department -->
        <li class="divider"></li>
      </ul>
      <div id="thisClock" class="center-align center brand-logo"></div> <!-- // Center clock -->
    </div>
  </nav>
  <!-- // EMD OF NAVIGATION -->
  
  <!-- // CONTENT -->
   <div class="section">
      <div class="row s10"><!-- // ROW 1 -->
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p1.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Josefine F. Brussgaard</h5>
 			<i class="grey-text">Emergency physician, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" id="user1" src="images/green.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text" id="minutes">X</i><br/>
            Activity: <b><i id="activity">X</i></b><br/>
			Location: <b><i id="location">X</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p3.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Hans Ø. Jensen</h5>
 			<i class="grey-text">Orthopaedic Surgeon, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/red.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>12</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Diagnosing</i></b><br/>
			Location: <b><i>Operating Theater, DS</i></b></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p4.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Liselotte Fries</h5>
 			<i class="grey-text">Anesteciologist, AD</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/green.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>3</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Unknown/Idle</i></b><br/>
			Location: <b><i>Office, ER</i></b>
            </div>
            </div>
        </div>
		</div> <!-- // END OF ROW 1 -->
        
        <div class="row s10"><!-- // ROW 2 -->
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p9.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Malthe Hende</h5>
 			<i class="grey-text">Physician, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/red.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">‎
            <i class="blue-text"><b>9</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Treating</i></b><br/>
			Location: <b><i>Patient Room 6, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p7.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Jimmey Allan</h5>
 			<i class="grey-text">Physical Therapy, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/red.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>23</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Reporting</i></b><br/>
			Location: <b><i>Office, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p2.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Martin B. Olsen</h5>
 			<i class="grey-text">Senior Medial Advisor</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/yellow.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">‎
            <i class="blue-text"><b>4</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Unknown/Idle</i></b><br/>
			Location: <b><i>Staff Room, ER</i></b>
            </div>
            </div>
        </div>
		</div> <!-- // END OF ROW 2 -->
        
        <div class="row s10"><!-- // ROW 3 -->
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p5.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Jason Hattford</h5>
 			<i class="grey-text">Orthopaedic Advisor, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/red.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>5</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Unknown</i></b><br/>
			Location: <b><i>Meeting Room 2, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p8.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Richard W. White</h5>
 			<i class="grey-text">Physician, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/yellow.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">‎<i class="blue-text"><b>10</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Walking</i></b><br/>
			Location: <b><i>Patient Room 8, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p6.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Thorbjorn Larsson</h5>
 			<i class="grey-text">Orthopaedic Surgeon, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/red.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>13</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Reporting</i></b></b><br/>
			Location: <b><i>Office, ER</i></b>
            </div>
            </div>
        </div>
		</div> <!-- // END OF ROW 3 -->
        
        <div class="row s10"> <!-- // ROW 4 -->
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text"><img src="images/p10.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Sophie M. Nielsen</h5>
 			<i class="grey-text">Orthopaedic Advisor, ER</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/green.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>13</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Unknown</i></b><br/>
			Location: <b><i>Office, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text">
			<img src="images/p13.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Clara Wissenberg</h5>
 			<i class="grey-text">Physician</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/green.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px;min-width:200px">
            <i class="blue-text"><b>2</b> MINUTES AGO:</i><br/>
            Activity: <b><i>Unknown/Idle</i></b><br/>
			Location: <b><i>Office, ER</i></b>
            </div>
            </div>
        </div>
        <div class="col s10 offset-l1 l4 card-panel grey lighten-5 z-depth-1">
            <div class="col s5">
            <h2 class="center light-blue-text">
			<img src="images/p12.png" /></h2>
            </div>
            <div class="col s7">
            <h5>Jeff B&oslash;rgesen</h5>
 			<i class="grey-text">Physician</i><br/><br/>
            <img style="width:50px;height:50px;float:left;position:relative;left:-86px;top:2px;opacity:0.7" src="images/yellow.png">‎
            <div style="position:relative;float:left;left:-47px;top:-5px">
            <i class="blue-text"><b>1</b> MINUTE AGO:</i><br/>
            Activity: <b><i>Walking</i></b><br/>
			Location: <b><i>Unknown in ER</i></b>
            </div>
            </div>
        </div>
		</div> <!-- // END OF ROW 4 -->
      </div> <!-- // END OF SECTION -->      
      <!-- // END OF CONTENT -->

  <!-- // FOOTER -->
  <footer class="page-footer blue darken-3">
      <div class="row">
        <div class="col l12 s12">
          <div class="grey-text text-lighten-1"><i class="center">Note: Information provided is only an estimate of the current location, activity and interruptibility state. Information should help to establish social awareness and give an idea of interruptibility of co-located staff in the hospital.<br/><small style="color:#FFF">Images by stockimages at FreeDigitalPhotos.net</small></i><br/><br/></div>
        </div>
      </div>
    <div class="footer-copyright center">
      IA Monitor for Healthcare by Troels Siggaard
    </div>
  </footer>
  <!-- // END OF FOOTER -->
  </body>
</html>
