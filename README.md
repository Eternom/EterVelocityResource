# EterVelocityResource

Les **mondes ressources** côté **proxy** : serveurs jetables créés et supprimés sur Pterodactyl par l'orchestrateur
d'**EterVelocityLib** (famille `eterresource`). Document développeur, à tenir à jour avec le code.
L'accès (créneau payé ou clé), les règles du monde et les bonus de métier sont dans **EterResource**, côté Paper.

## Fonctionnement

- **Chaque serveur = un monde neuf**, généré au premier démarrage. **Pas de modèle nécessaire** : le moteur écrit
  les fichiers de base (voir EterVelocityLib), avec `orchestrator.server-properties` (difficulté difficile...) ; la
  config d'EterLib est celle commune (`plugins/etervelocitylib/EterLib-config.yml`). Un `template.zip` reste possible
  (LuckPerms...), SANS monde. Au bout de `max-lifetime-hours` (12), le serveur est vidé et remplacé par un neuf.
- **Règles** (moteur commun, voir le README d'EterVelocityLib) : au moins `minimum` (1) serveur à jour, un de plus
  quand ils sont remplis à `scale-up-at`, au plus `maximum` (4) ; un serveur vidé est supprimé quand il est vide ou
  après `drain-timeout-minutes` (60) : ses joueurs vont sur un autre monde ressource (leur temps continue), sinon le
  lobby les reprend (EterVelocityLobby).
- **Sécurité** : seulement les serveurs de `eterresource_servers`, du compte dédié, identifiant externe
  `eterresource:<nom>` : ni les serveurs des clients, ni les lobbys (`eterlobby:`) ne peuvent être touchés.
- À la suppression, ses lignes de `eter_servers` et `eterresource_worlds` sont retirées.
- `/eterresourcepool list | status | create | drain <serveur>` (`etervelocityresource.admin`) ; `/eterresource` est la commande Paper d'EterResource (clés, temps).

## Technique

- Dépend d'**EterVelocityLib** (`@Dependency`) : config, langues communes, palette, orchestrateur.
- La version est aussi écrite dans `@Plugin` : à garder identique à `gradle.properties`.
