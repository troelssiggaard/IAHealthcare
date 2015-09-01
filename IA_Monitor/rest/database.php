<?php
class DB {
		public function __construct($user, $password, $database, $host) {
			$this->user = $user;
			$this->password = $password;
			$this->database = $database;
			$this->host = $host;
		}
		
		protected function connect() {
			return new mysqli($this->host, $this->user, $this->password, $this->database);
		}
		
		public function query($query) {
			$db = $this->connect();
			mysqli_set_charset($db,"utf8");
			$result = $db->query($query);
			
			while ( $row = mysqli_fetch_assoc($result) ) {
				$results[] = $row;
			}
			
			return $results;
			
		}
		
	}
?>