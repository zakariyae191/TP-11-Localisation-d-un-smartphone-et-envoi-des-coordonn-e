# Lab Android — GPS + Volley + PHP + MySQL

## 1. Objectifs pédagogiques

Ce lab montre comment créer une mini application Android connectée à un backend PHP/MySQL.

Il permet de :

- récupérer latitude et longitude d'un smartphone ;
- comprendre les permissions Android liées à la localisation ;
- envoyer des données Android vers un service PHP ;
- enregistrer les coordonnées dans MySQL ;
- structurer un mini projet mobile connecté à un backend.

## 2. Résultat attendu

À la fin, l'application doit :

- détecter une position géographique ;
- afficher les informations récupérées ;
- envoyer latitude, longitude, date et identifiant appareil au serveur ;
- insérer ces données dans la table `position`.

## 3. Architecture générale

### Partie serveur

Le serveur contient :

- une base de données MySQL `localisation` ;
- une table `position` ;
- une classe PHP `Position` ;
- une classe `Connexion` pour PDO ;
- une interface `IDao` ;
- un service `PositionService` ;
- un script `createPosition.php`.

### Partie mobile

L'application Android contient :

- les permissions de localisation et Internet ;
- une interface simple ;
- `LocationManager` pour récupérer le GPS ;
- Volley pour envoyer une requête HTTP POST.

### Fonctionnement

1. Le smartphone obtient une nouvelle position.
2. L'application prépare une requête HTTP POST.
3. Le serveur PHP reçoit les paramètres.
4. Le serveur crée un objet `Position`.
5. Les données sont enregistrées dans MySQL.

## 4. Installation serveur

1. Installer XAMPP ou WAMP.
2. Lancer Apache et MySQL.
3. Copier le dossier `localisation` dans `htdocs`.
4. Ouvrir phpMyAdmin.
5. Importer le fichier `localisation/database.sql`.
6. Vérifier que la base `localisation` et la table `position` existent.
7. Tester l'URL `http://localhost/localisation/createPosition.php`.

Depuis un navigateur, cette URL répondra que la méthode n'est pas autorisée, car le script accepte uniquement `POST`.

## 5. Installation Android

1. Ouvrir le dossier `LocalisationSmartphone` avec Android Studio.
2. Vérifier la dépendance Volley dans `app/build.gradle` :

```gradle
implementation 'com.android.volley:volley:1.2.1'
```

3. Vérifier les permissions dans `AndroidManifest.xml` :

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

4. Remplacer `VOTRE_IP` dans `MainActivity.java` par l'adresse IP réelle du PC serveur.

Exemple :

```java
private static final String SERVER_URL = "http://192.168.1.10/localisation/createPosition.php";
```

5. Vérifier que `android:usesCleartextTraffic="true"` existe dans le manifest.
6. Connecter le téléphone et le PC au même réseau Wi-Fi.
7. Lancer l'application.

## 6. Explication détaillée du code PHP

### Position.php

`Position.php` représente une ligne de la table `position`. La classe contient les propriétés `id`, `latitude`, `longitude`, `datePosition` et `imei`, avec constructeur, getters et setters.

### Connexion.php

`Connexion.php` centralise la connexion PDO à MySQL. PDO permet d'utiliser une API propre pour communiquer avec la base. Le charset `utf8mb4` évite les problèmes d'encodage.

La ligne suivante active les exceptions :

```php
$this->connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
```

### IDao.php

`IDao.php` définit une interface de base avec les méthodes `create`, `update`, `delete`, `getById` et `getAll`. Même si le lab utilise surtout `create`, l'interface montre une architecture plus propre.

### PositionService.php

`PositionService.php` implémente `IDao`. Sa méthode `create($position)` prépare puis exécute l'insertion SQL :

```sql
INSERT INTO position(latitude, longitude, date_position, imei)
VALUES(:latitude, :longitude, :date_position, :imei)
```

Les requêtes préparées protègent contre l'injection SQL et séparent clairement la requête des valeurs.

### createPosition.php

`createPosition.php` reçoit les données envoyées par Android via `$_POST`. Il vérifie la méthode HTTP, contrôle les champs obligatoires, crée un objet `Position`, puis appelle `PositionService`.

Le script retourne toujours une réponse JSON claire :

```json
{
  "success": true,
  "message": "Position enregistrée avec succès"
}
```

## 7. Explication détaillée du code Android

### LocationManager

`LocationManager` permet d'utiliser le GPS du téléphone. Le lab utilise :

```java
LocationManager.GPS_PROVIDER
```

### requestLocationUpdates

La méthode `requestLocationUpdates` demande à Android de fournir une position dès qu'elle est disponible, puis à chaque mise à jour significative.

Dans ce lab :

- temps minimum : `60000` ms ;
- distance minimum : `150` m.

### onLocationChanged

`onLocationChanged` est appelée quand une position est reçue. Elle récupère latitude, longitude, altitude, précision, date et identifiant appareil, puis déclenche l'envoi au serveur.

### Volley

Volley simplifie les requêtes HTTP Android. Le lab utilise un `StringRequest` en méthode `POST`.

### getParams

`getParams` fournit les paramètres envoyés au serveur :

- `latitude` ;
- `longitude` ;
- `date_position` ;
- `imei`.

Même si le code utilise `ANDROID_ID`, le nom du paramètre reste `imei` pour respecter la structure historique du TP.

### Settings.Secure.ANDROID_ID

Les anciens TP utilisaient souvent l'IMEI. Sur Android moderne, l'accès à l'IMEI demande `READ_PHONE_STATE`, une permission sensible et limitée. Il est donc préférable d'utiliser :

```java
Settings.Secure.ANDROID_ID
```

### Permissions runtime

Depuis Android 6, les permissions dangereuses comme la localisation doivent être demandées pendant l'exécution. Le code vérifie donc les permissions puis appelle `requestPermissions` si nécessaire.

## 8. Tests à faire

### Test serveur avec Postman

- Méthode : `POST`
- URL : `http://localhost/localisation/createPosition.php`
- Body : `x-www-form-urlencoded`

Paramètres :

```text
latitude = 31.64467319
longitude = -8.01915503
date_position = 2026-04-29 17:02:23
imei = test-device-001
```

Résultat attendu :

```json
{
  "success": true,
  "message": "Position enregistrée avec succès"
}
```

### Test Android

1. Connecter le téléphone au même réseau Wi-Fi que le PC.
2. Remplacer `VOTRE_IP` par l'IP du PC.
3. Activer le GPS.
4. Accepter les permissions.
5. Cliquer sur `Récupérer et envoyer la position`.
6. Vérifier dans phpMyAdmin qu'une nouvelle ligne est insérée.

## 9. Problèmes fréquents

- Erreur réseau Android : vérifier l'IP, le pare-feu et Apache.
- `localhost` ne marche pas depuis le téléphone : utiliser l'IP du PC.
- Permission refusée : vérifier `AndroidManifest.xml` et la demande runtime.
- GPS ne donne rien : activer la localisation ou tester sur un vrai téléphone.
- Cleartext HTTP blocked : ajouter `android:usesCleartextTraffic="true"`.
- Table introuvable : vérifier la base `localisation` et la table `position`.

## 10. Améliorations possibles

- Remplacer `LocationManager` par `FusedLocationProviderClient`.
- Ajouter une carte Google Maps.
- Ajouter une authentification.
- Stocker l'historique localement avec Room.
- Afficher la liste des positions enregistrées.
- Sécuriser l'API avec un token.
- Remplacer PHP simple par Laravel ou Node.js.

## 11. Conclusion

Ce lab montre une chaîne complète :

```text
Android -> HTTP POST -> PHP -> MySQL
```

L'application mobile récupère la position GPS, l'affiche, puis l'envoie au serveur. Le serveur PHP reçoit les données, les valide et les insère dans MySQL.
