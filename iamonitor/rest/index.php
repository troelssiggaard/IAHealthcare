<?php
require_once("database.php");

$api = new API();

class API {
	
	public $path = "";
	protected $tablename = "";
	protected $id = "";
	protected $attribute = "";

	function __construct() {

		$this->response = $this->database = new DB("user", "pass", "database", "host");
		
		$path = explode("/", substr(@$_SERVER["PATH_INFO"], 1));
		
		switch(self::getRequestMethod()) {
			case "POST":
				self::post($path);
				break;
			case "GET":
				self::get($path);
				break;
			case "PUT":
				self::put($path);
				break;
			case "DELETE":
				self::delete($path);
				break;
			default:
				self::error($path);
				break;	
		}
		
	}
	
	public function getRequestMethod() {
			return $_SERVER["REQUEST_METHOD"];
	}
	
	public function get($path){
		$this->tablename = $path[0]; // Table name : localhost/rest/path[0]/path[1]/path[2]/
		
		if($path[1] == "") {
			$sqlres = $this->response->query("SELECT * FROM ".$this->tablename.";");		
		}else if($path[1] == "app"){
			$sqlres = $this->response->query("SELECT id, interruptibility, activity, location, timestamp FROM ".$this->tablename.";");	
		}else{
			if($path[0] == "doctor" && $path[1] == "app") {
				$sqlres = $this->response->query("SELECT interruptibility, activity, location, timestamp FROM ".$this->tablename." WHERE id=\"".$path[1]."\";");		
			}else{
				$sqlres = $this->response->query("SELECT * FROM ".$this->tablename." WHERE id='".$path[1]."';");	
			}
		}
		
		if($sqlres != NULL) {
			$json = json_encode($sqlres);
			echo $json;
		}else{
			echo "";
		}
		
	
	}
	
	public function post($path){
		
		$json = file_get_contents("php://input");
		$result = json_decode($json,true);
		
		var_dump($result);
		
		if($result["activity"] == "") {
			$act = "";
		}else{
			$act = "activity=\"".$result['activity']."\",";
		}
		
		if($result["location"] == "") {
			$loc = "";
		}else{
			$loc = "location=\"".$result['location']."\",";	
		}
		
		if($result["interruptibility"] == "") {
			$int = "";
		}else{
			$int = "interruptibility=\"".$result['interruptibility']."\",";
		}
		
		$ts = $result["timestamp"];
		//	error_log("test: ".$act." id".$path[2]." app: ".$path[1]." doctor:".$path[0]);
	
			if($path[0] == "doctor") {
				
				switch($path[1]) {
					case "app":
					$sqlres = $this->response->query("UPDATE doctor SET ".$act." ".$loc." ".$int." timestamp=\"".$ts."\" WHERE id=\"".$path[2]."\";");
					break;
					case "activity":
					$sqlres = $this->response->query("UPDATE doctor SET ".$act." timestamp=\"".$ts."\" WHERE id=\"".$path[1]."\";");
					break;
					case "location":
					$sqlres = $this->response->query("UPDATE doctor SET ".$loc." timestamp=\"".$ts."\" WHERE id=\"".$path[1]."\";");
					break;
					case 'interrupt':
					$sqlres = $this->response->query("UPDATE doctor SET ".$int." timestamp=\"".$ts."\" WHERE id=\"".$path[1]."\";");
					break;
					default:
					echo "";
					break;
				}
			}
	}
	
	public function delete($path){
		echo "Not implemented"; 
	}
	
	public function error($path){
		echo "Not implemented";
	}
	
	public function sqlToJSON($sqldata) {
    	$rows = array();

		while($row = mysqli_fetch_assoc($sqldata)) {
    		$rows[] = $row;
		}
		
		return json_encode($rows);
	}	
	
}
?>