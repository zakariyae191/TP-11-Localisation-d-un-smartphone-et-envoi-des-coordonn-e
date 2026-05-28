<?php

header('Content-Type: application/json; charset=utf-8');
ini_set('display_errors', '0');

require_once __DIR__ . '/classe/Position.php';
require_once __DIR__ . '/service/PositionService.php';

function jsonResponse($success, $message, $httpCode = 200)
{
    http_response_code($httpCode);
    echo json_encode([
        'success' => $success,
        'message' => $message
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        jsonResponse(false, 'Méthode non autorisée. Utilisez POST.', 405);
    }

    $requiredFields = ['latitude', 'longitude', 'date_position', 'imei'];

    foreach ($requiredFields as $field) {
        if (!isset($_POST[$field]) || trim($_POST[$field]) === '') {
            jsonResponse(false, 'Paramètres manquants', 400);
        }
    }

    $latitude = filter_var($_POST['latitude'], FILTER_VALIDATE_FLOAT);
    $longitude = filter_var($_POST['longitude'], FILTER_VALIDATE_FLOAT);

    if ($latitude === false || $longitude === false) {
        jsonResponse(false, 'Latitude ou longitude invalide', 400);
    }

    $datePosition = trim($_POST['date_position']);
    $imei = trim($_POST['imei']);

    // L'id est null, car MySQL le genere automatiquement avec AUTO_INCREMENT.
    $position = new Position(null, $latitude, $longitude, $datePosition, $imei);

    $service = new PositionService();
    $created = $service->create($position);

    if ($created) {
        jsonResponse(true, 'Position enregistrée avec succès');
    }

    jsonResponse(false, 'Impossible d enregistrer la position', 500);
} catch (Exception $exception) {
    // En production, le detail de l'exception doit aller dans des logs serveur.
    jsonResponse(false, 'Erreur serveur interne', 500);
}
