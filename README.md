# Hotel Booking

Ce projet permet de gérer les réservations d'un petit hôtel depuis un menu en console.
On peut chercher une chambre libre, vérifier le prix d'un séjour, réserver, annuler
et consulter les chiffres de l'hôtel.

C'est un projet de programmation orientée objet en Java, réalisé dans le cadre du M1 à JUNIA.
Les données de départ viennent de fichiers CSV. Les changements restent en mémoire :
ils sont perdus quand on ferme le programme.

## Lancer le projet

Il faut un JDK 17 ou plus récent et une variable `JAVA_HOME` qui pointe vers ce JDK.
Le Maven Wrapper fourni télécharge Maven 3.9.11 au premier lancement.
Il faut donc une connexion Internet pour cette première étape.

Depuis PowerShell, à la racine du projet :

```powershell
.\mvnw.cmd compile exec:java
```

On peut aussi créer le JAR puis le lancer :

```powershell
.\mvnw.cmd package
java -jar target/hotel-booking-1.0.0.jar
```

Sur Linux ou macOS, utiliser `./mvnw` à la place de `.\mvnw.cmd`.
Dans IntelliJ, ouvrir le dossier comme projet Maven, puis lancer
`com.junia.hotel.Application`.

Par défaut, le programme utilise les fichiers de `src/main/resources/data`.
Pour choisir un autre dossier :

```powershell
java -jar target/hotel-booking-1.0.0.jar chemin/vers/les/donnees
```

Ce dossier doit contenir `rooms.csv`, `guests.csv` et `reservations.csv`.

## Utiliser le menu

Le menu est en anglais. Les dates se saisissent au format `YYYY-MM-DD`.

| Option | Ce qu'elle permet de faire |
|---|---|
| 1 | Chercher les chambres libres pour des dates et un nombre de personnes |
| 2 | Obtenir un devis avec la remise appliquée |
| 3 | Réserver une chambre pour un client existant |
| 4 | Annuler une réservation et voir les frais et le remboursement |
| 5 | Voir les réservations d'un client |
| 6 | Voir toutes les réservations |
| 7 | Consulter les revenus, le meilleur client et le taux d'occupation |
| 8 | Voir les réservations et les annulations de la session |
| 9 | Exporter un rapport en CSV |
| 0 | Quitter |

Pour essayer un devis avec les données fournies, utiliser le client `G003`, la chambre `102`,
deux personnes et un séjour du `2026-12-14` au `2026-12-16`. Le total est de 160 EUR.
Pour réserver, il faut aussi que la date d'arrivée ne soit pas passée.
Les données de démonstration couvrent décembre 2026 et janvier 2027.

## Les règles de réservation

Le client et la chambre doivent exister. La chambre doit être assez grande pour le groupe
et libre pendant tout le séjour. Les résultats d'une recherche sont triés par prix de base par nuit.

Le jour du départ ne compte pas comme une nuit. Du 10 au 12 décembre, on paie donc deux nuits.
Un autre client peut arriver le 12 dans la même chambre sans créer de conflit.

| Chambre | Prix par nuit |
|---|---|
| `STANDARD` | Prix de base, quel que soit le jour |
| `SUITE` | Prix de base, majoré de 25 % le vendredi et le samedi ; si le petit-déjeuner est activé, ajout de 15 EUR par personne |
| `FAMILY` | Prix de base pour deux personnes, puis un supplément par personne supplémentaire |

Les clients `SILVER` ont 5 % de remise et les clients `GOLD` ont 10 %.
Un séjour d'au moins sept nuits donne droit à 15 %. Les remises ne s'ajoutent pas :
le programme garde la plus avantageuse. Le prix final est conservé dans la réservation,
même si les tarifs changent ensuite.

### Annuler un séjour

L'annulation reste possible le jour de l'arrivée. Après cette date, elle est refusée.
On ne peut pas annuler deux fois la même réservation.

| Chambre | Frais d'annulation |
|---|---|
| `STANDARD` ou `FAMILY` | Aucun frais au moins deux jours calendaires avant l'arrivée ; 50 % sinon |
| `SUITE` | 100 % du prix, quelle que soit la date |

Les frais sont calculés sur le prix final, remise comprise, et arrondis au centime.
Le menu affiche les frais et le remboursement. La chambre annulée redevient disponible.

Une réservation ou une annulation réussie ajoute une entrée à l'historique de session
et affiche une confirmation qui simule un e-mail. Aucun vrai e-mail n'est envoyé.
Les opérations refusées et les imports au démarrage ne créent pas ces notifications.

## Les fichiers CSV et les rapports

Les fichiers d'entrée utilisent des champs séparés par des virgules, sans champs entre guillemets.
Le lecteur ignore l'en-tête, les lignes vides et celles qui commencent par `#`.

| Fichier | Colonnes |
|---|---|
| `rooms.csv` | `number,type,capacity,basePrice,option` |
| `guests.csv` | `id,name,email,loyaltyTier` |
| `reservations.csv` | `guestId,roomNumber,checkIn,checkOut,guestCount` |

Dans `rooms.csv`, `option` reste vide pour une chambre standard.
Pour une suite, elle vaut `true` ou `false` pour le petit-déjeuner.
Pour une chambre familiale, elle indique le supplément par personne et par nuit.

Une ligne mal formée arrête le chargement avec le nom du fichier et le numéro de ligne.
Une réservation bien formée mais impossible est signalée, puis le chargement continue.
La dernière réservation du fichier fourni chevauche volontairement la première dans la chambre `201` :
elle est rejetée au démarrage. L'import accepte les séjours passés, mais vérifie toujours
la capacité et les conflits.

Les rapports donnent les revenus totaux, les revenus par type de chambre et le meilleur client
sur toutes les réservations actives. Seul le taux d'occupation dépend de la période saisie.
Les réservations annulées et leurs frais sont exclus : le rapport décrit les réservations,
il ne suit pas les encaissements.

L'option 9 crée `reports/report-<date>.csv`. Un nouvel export le même jour remplace ce fichier.
Le CSV est en UTF-8 avec les colonnes `metric,value`. Il contient aussi les types de chambre
sans revenu, le taux d'occupation entre 0 et 1 et le meilleur client (`None` s'il n'y en a pas).
Les nombres utilisent un point décimal et les noms sont protégés par des guillemets.
Le dossier est créé si besoin et les rapports générés sont ignorés par Git.

## Organisation du code

| Partie | Rôle |
|---|---|
| `Application` | Crée les objets, les relie et lance le menu |
| `model` | Représente les clients, les chambres, les séjours et les réservations |
| `pricing` | Calcule les prix, les remises et les frais d'annulation |
| `repository` | Stocke les objets en mémoire et permet de les retrouver par identifiant |
| `service` | Gère les réservations, les disponibilités, les imports, l'historique et les rapports |
| `io` | Lit les fichiers CSV et exporte les rapports |
| `exception` | Regroupe les erreurs à traiter |
| `ui` | Lit les saisies et affiche les résultats |

Les liens entre les principales classes sont dans [docs/architecture.md](docs/architecture.md).

### Les choix de conception

`Room` est abstraite, car toutes les chambres ont un numéro, une capacité et un prix de base.
`StandardRoom`, `Suite` et `FamilyRoom` en héritent et calculent chacune le prix d'une nuit.
`Room.priceForStay()` additionne ces nuits sans tester le type de chambre :
c'est le polymorphisme utilisé dans le calcul du prix.

Les remises passent par `DiscountPolicy`. On peut donc ajouter une remise sans modifier
`PricingService`. Les frais d'annulation suivent le même principe avec `CancellationPolicy`.
`CancellationPolicyResolver` choisit la règle selon le type de chambre.
Pour ajouter un type de chambre, il faut aussi mettre à jour `RoomType`, `RoomFactory`
et cette correspondance.

`ReservationListener` prévient `ConsoleNotifier` et `ReservationHistory` après une opération réussie.
C'est le principe Observer : le service prévient ses abonnés sans gérer lui-même leur affichage
ou leur historique. Un abonné peut être ajouté ou retiré, sans doublon.

Les dépendances sont reçues par constructeur. Par exemple, l'import utilise `HotelDataSource`
et le menu utilise `ReportExporter`. Les tests peuvent leur fournir une fausse source
ou un exporteur de test. Un même `Clock` est transmis au service et au menu pour contrôler
la date des réservations, des annulations et des exports.

Les principes SOLID se retrouvent dans ces choix : chaque partie a un rôle précis,
les remises et les exporteurs passent par des interfaces courtes, les trois types de chambre
respectent le contrat de `Room`, et les services utilisent des abstractions pour le stockage
et la source des données.

Les erreurs métier héritent de `BookingException`, une exception contrôlée interceptée par le menu.
Cela couvre par exemple une chambre occupée, une capacité dépassée ou un identifiant inconnu.
Une période invalide déclenche `InvalidDateRangeException`, qui hérite de `IllegalArgumentException`.
Une double annulation déclenche `IllegalStateException`. Les erreurs de lecture et d'export
passent par `DataLoadingException` et `DataExportException`, en conservant leur cause d'origine.

Enfin, `Report` copie sa table de revenus et `ReservationHistory` renvoie une liste non modifiable.
On peut consulter ces résultats sans modifier leurs collections depuis l'extérieur.

## Vérifier le projet

```powershell
.\mvnw.cmd test
```

Les 116 tests JUnit 5 couvrent les prix, les remises, les conflits, la capacité, les annulations,
les notifications, les fichiers et les rapports. Ils vérifient aussi les limites : deux séjours
qui se suivent, exactement sept nuits, une annulation deux jours avant l'arrivée ou le jour même.
Les tests du menu utilisent des saisies préparées et une date fixe.

Pour repartir d'une compilation propre et recréer le JAR :

```powershell
.\mvnw.cmd clean package
```
