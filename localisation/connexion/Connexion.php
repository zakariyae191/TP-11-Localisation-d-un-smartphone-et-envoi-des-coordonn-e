<?php

class Connexion
{
    private $host = 'localhost';
    private $database = 'localisation';
    private $user = 'root';
    private $password = '';
    private $connexion;

    public function __construct()
    {
        $dsn = 'mysql:host=' . $this->host . ';dbname=' . $this->database . ';charset=utf8mb4';

        $this->connexion = new PDO($dsn, $this->user, $this->password);
        $this->connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        $this->connexion->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    }

    public function getConnexion()
    {
        return $this->connexion;
    }
}
