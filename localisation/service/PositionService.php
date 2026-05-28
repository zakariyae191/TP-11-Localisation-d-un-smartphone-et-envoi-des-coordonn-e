<?php

require_once __DIR__ . '/../dao/IDao.php';
require_once __DIR__ . '/../classe/Position.php';
require_once __DIR__ . '/../connexion/Connexion.php';

class PositionService implements IDao
{
    private $connexion;

    public function __construct()
    {
        $connexion = new Connexion();
        $this->connexion = $connexion->getConnexion();
    }

    public function create($position)
    {
        // Les requetes preparees protegent contre l'injection SQL.
        $sql = 'INSERT INTO position(latitude, longitude, date_position, imei)
                VALUES(:latitude, :longitude, :date_position, :imei)';

        $statement = $this->connexion->prepare($sql);

        return $statement->execute([
            ':latitude' => $position->getLatitude(),
            ':longitude' => $position->getLongitude(),
            ':date_position' => $position->getDatePosition(),
            ':imei' => $position->getImei()
        ]);
    }

    public function update($obj)
    {
        // TODO: implementer la modification d'une position si le TP evolue.
        return null;
    }

    public function delete($obj)
    {
        // TODO: implementer la suppression d'une position si necessaire.
        return null;
    }

    public function getById($obj)
    {
        // TODO: implementer la recherche par id si necessaire.
        return null;
    }

    public function getAll()
    {
        // TODO: implementer la liste des positions si necessaire.
        return null;
    }
}
