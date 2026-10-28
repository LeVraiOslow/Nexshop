# NexShop

Plugin de shop pour Paper 26.2 (Java 25). Il gère les catégories, l'achat et la vente avec choix de quantité, et il est compatible avec Nexo, Vault et EssentialsX.

## Compilation
    gradle build        # jar dans build/libs/NexShop-1.0.0.jar
(ou ouvrir le dossier dans IntelliJ et lancer la tâche Gradle `build`)

## Installation
- Placer le jar dans `plugins/`.
- Prérequis : Vault + un plugin d'économie, ou EssentialsX seul. Nexo et RoseStacker sont optionnels.

## Commandes / permissions
- `/shop` : menu principal. `/shop <categorie>` : ouvre directement une catégorie.
- `/shop reload` : recharge toute la configuration (`shop.admin`).
- `shop.use` (défaut : tout le monde), `shop.admin` (défaut : op).
- Permission par catégorie : `permission:` dans `menus/main.yml`.

## Fichiers de configuration
| Fichier | Rôle |
|---|---|
| `config.yml` | Économie, format de la monnaie, lore par défaut, sons |
| `messages.yml` | Tous les messages |
| `menus/main.yml` | Menu des catégories |
| `menus/quantity.yml` | Menu de quantité (1/16/32/64/tout, boutons, titres) |
| `categories/<id>/category.yml` | Réglages du menu de la catégorie : titre, nombre de lignes (`rows`), `content-slots`, boutons de navigation |
| `categories/<id>/items.yml` | Items de la catégorie (prix, noms, commandes). Tous les autres `.yml` du dossier sont lus aussi |

## Points clés
- Clic gauche = acheter, clic droit = vendre.
- Désactiver l'achat ou la vente d'un item : `buyable: false` / `sellable: false`, ou ne pas mettre de `buy-price` / `sell-price`.
- Nexo : n'importe quel `material:` accepte `nexo:mon_item` (items, fillers, boutons, icônes de catégories).
- Interfaces Nexo : mettre le glyph de fond dans `title:` (caractère unicode `"\uE000"` ou `<glyph:nom>`) et passer `filler.enabled: false`.
- Couleurs partout : `&a`, `&l`, `&#RRGGBB` et MiniMessage (`<gradient:red:blue>`).
- Commandes à l'achat : `commands:` (console par défaut, `[player]` en préfixe pour le joueur), avec `%player%` et `%amount%`. Exemple RoseStacker : `rs give spawner %player% zombie %amount%`.
- Un item avec `commands:` ne donne pas d'item physique, sauf si `give-item: true`.

## Pagination
- `rows` dans `category.yml` fixe le nombre de lignes (1 à 6).
- Les items sans `slot` se placent automatiquement dans les `content-slots` (par défaut : tout sauf la dernière ligne) et passent à la page suivante quand c'est plein.
- Boutons de navigation : `action: PREVIOUS_PAGE` / `NEXT_PAGE` dans `items:` de `category.yml`. Ils disparaissent quand il n'y a pas de page à atteindre. `%page%` et `%pages%` sont utilisables dans le titre et les noms.
- Forcer l'emplacement d'un item : `slot: 10` et, si besoin, `page: 2`.

## Ajouter une catégorie
1. Créer `categories/<id>/category.yml` et `categories/<id>/items.yml`.
2. Ajouter l'entrée `<id>` dans `categories:` de `menus/main.yml`.
3. `/shop reload`.

## Mise à jour depuis la version précédente
Les nouveaux fichiers sont créés automatiquement. Supprimer les anciens `categories/*.yml` (fichier unique) devenus inutiles, et ajouter la catégorie `nourritures` dans `menus/main.yml` si votre fichier existe déjà.
