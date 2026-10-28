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
| `categories/*.yml` | Une catégorie = un fichier ; chaque item avec ses prix, slots, noms, commandes |

## Points clés
- Clic droit = acheter, clic gauche = vendre.
- Désactiver l'achat ou la vente d'un item : `buyable: false` / `sellable: false`, ou ne pas mettre de `buy-price` / `sell-price`.
- Nexo : n'importe quel `material:` accepte `nexo:mon_item` (items, fillers, boutons, icônes de catégories).
- Interfaces Nexo : mettre le glyph de fond dans `title:` (caractère unicode `"\uE000"` ou `<glyph:nom>`) et passer `filler.enabled: false`.
- Couleurs partout : `&a`, `&l`, `&#RRGGBB` et MiniMessage (`<gradient:red:blue>`).
- Commandes à l'achat : `commands:` (console par défaut, `[player]` en préfixe pour le joueur), avec `%player%` et `%amount%`. Exemple RoseStacker : `rs give spawner %player% zombie %amount%`.
- Un item avec `commands:` ne donne pas d'item physique, sauf si `give-item: true`.
