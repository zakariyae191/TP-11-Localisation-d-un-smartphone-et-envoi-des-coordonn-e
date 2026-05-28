# Serveur PHP/MySQL - Lab localisation

Ce dossier doit être placé dans `htdocs` avec XAMPP ou dans le répertoire web équivalent avec WAMP.

## Installation rapide

1. Démarrer Apache et MySQL.
2. Importer le fichier `database.sql` dans phpMyAdmin ou avec MySQL.
3. Vérifier que la base `localisation` et la table `position` existent.
4. Tester `http://localhost/localisation/createPosition.php` avec Postman en méthode POST.

## Paramètres POST

```text
latitude=31.64467319
longitude=-8.01915503
date_position=2026-04-29 17:02:23
imei=test-device-001
```

## Réponse attendue

```json
{
  "success": true,
  "message": "Position enregistrée avec succès"
}
```
