<div align="center">

# Java Programming — Group 1

**Project EFREI I1 · FALL 2026 · Java Proramming**

![Java](https://img.shields.io/badge/Java-27%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/Licence-EFREI-orange?style=for-the-badge)

</div>

---

## 📖 Présentation

Ce projet implémente en Python un **système complet de traitement d'automates finis**.  
À partir d'un automate encodé dans un fichier `.txt`, le programme enchaîne automatiquement plusieurs étapes de traitement algorithmique : analyse, standardisation, déterminisation, minimisation, reconnaissance de mots et construction du langage complémentaire.

L'ensemble des traitements est enveloppé dans une **interface interactive colorée** permettant de travailler sur plusieurs automates successivement, sans jamais relancer le programme. Chaque session génère automatiquement une **trace d'exécution** dans le dossier `traces_execution/`.

---

## 👥 Équipe

| Nom | GitHub |
|-----|--------|
| Karl Anthony **Garozzo** | [@KarlGarozzo](https://github.com/KarlGarozzo) |
| Gwendal **Lavolee** | [@gwendal-lvl](https://github.com/gwendal-lvl) |
| Pablo **Biran** | [@PabBir06](https://github.com/PabBir06) |
| Clément **Davoine** | [@ClementDavoine](https://github.com/ClementDavoine) |

> 🔗 Dépôt : [github.com/KarlGarozzo/Projet-Automates-Finis-Equipe4](https://github.com/KarlGarozzo/Projet-Automates-Finis-Equipe4)

---

## 📁 Structure du projet

```
Projet-Automates-Finis-Equipe4/
│
├── main.py                        # Point d'entrée — menu interactif complet
│
├── core/
│   ├── automate.py                # Classe Automate
│   ├── lecture.py                 # Lecture des fichiers .txt
│   ├── determinisation.py         # Déterminisation & complétion
│   ├── determinisation.py       # Fonctions déterminisation (interface publique)
│   ├── standardisation.py       # Fonctions standardisation (interface publique)
│   ├── minimisation.py                # Algorithme de minimisation par partitions
│   ├── reconnaissance.py              # Reconnaissance de mots
│   ├── complementaire.py              # Construction du langage complémentaire
│   └── standardisation.py         # Standardisation
│
│
├── assets/
│   ├── 01.txt                     # Automates de test (format .txt)
│   ├── 02.txt
│   └── ...
│
└── traces_execution/              # Généré automatiquement
    └── trace_automate_XX_YYYYMMDD_HHMMSS.txt
```

---

## ⚙️ Installation

### Prérequis

- Python **3.10** ou supérieur
- pip

### Dépendances

```bash
pip install colorama prettytable
```

### Lancement

```bash
python main.py
```

---

## 📄 Format des fichiers automates

Les automates sont décrits dans des fichiers `.txt` placés dans le dossier `assets/`, nommés `01.txt`, `02.txt`, etc.

```
Alphabet: <n> (<a>, <b>, ...)
Etats: <n>
Initiaux: <n> (<état>, ...)
Finaux: <n> (<état>, ...)
Transitions: <n>
---
(<état_source>, <symbole>, <état_destination>)
...
```

**Exemple** — automate sur `{a, b}`, 5 états, 1 initial `{0}`, 1 final `{4}`, 6 transitions :

```
Alphabet: 2 (a, b)
Etats: 5
Initiaux: 1 (0)
Finaux: 1 (4)
Transitions: 6
---
(0, a, 0)
(0, b, 0)
(0, a, 1)
(1, b, 2)
(2, a, 3)
(3, a, 4)
```

> **Conventions imposées par l'énoncé** : l'alphabet utilise les premières lettres de l'alphabet latin (`a`, `b`, `c`…) et les états sont numérotés à partir de `0` sans rupture de séquence.

---

## Fonctionnement

Le menu principal propose les options suivantes, dans l'ordre recommandé par le sujet :

| Option | Action |
|--------|--------|
| `[1]` | Charger un automate depuis `assets/` |
| `[2]` | Afficher la table de transitions |
| `[3]` | Informations — déterministe ? complet ? standard ? |
| `[4]` | Standardisation *(à la demande)* |
| `[5]` | Déterminisation & Complétion → **AFDC** |
| `[6]` | Minimisation → **AFDCM** |
| `[7]` | Reconnaissance de mots |
| `[8]` | Automate complémentaire |
| `[9]` | **Pipeline complet** — enchaîne les étapes 3 à 8 d'un seul coup |
| `[0]` | Quitter |

---

## 🔬 Détail des algorithmes

### 1 · Lecture et affichage
L'automate est chargé en mémoire et sa table de transitions est affichée, avec indication explicite des états initiaux (`E`), finaux (`S`) et mixtes (`E/S`).

### 2 · Tests
Le programme détermine si l'automate est **déterministe**, **standard** et **complet**.  
En cas de réponse négative, les raisons sont affichées (transitions ambiguës, flèches entrant sur l'initial, paires état/symbole manquantes).

### 3 · Standardisation
Si l'automate n'est pas standard, l'utilisateur peut demander sa standardisation.  
Un nouvel état initial est créé ; il hérite de toutes les transitions sortantes des anciens états initiaux.

### 4 · Déterminisation + complétion
Selon le résultat des tests :
- **Déjà AFDC** → aucune transformation, l'automate est conservé tel quel.
- **Déterministe mais incomplet** → complétion par ajout d'un état puits avec boucles.
- **Non déterministe** → algorithme des **sous-ensembles**, avec affichage de la table de déterminisation et de la correspondance entre les états de l'AFDC et ceux de l'AF d'origine (notation `1.2.3` pour les états composés, `P` pour le puits).

### 5 · Minimisation
Minimisation de l'AFDC par l'**algorithme des partitions** (Moore).  
Les partitions successives sont affichées et numérotées. Si l'automate est déjà minimal, un message le signale explicitement. Une table de correspondance `AFDCM ↔ AFDC` est affichée.

### 6 · Reconnaissance de mots
Boucle de saisie interactive : chaque mot saisi est testé sur l'automate minimal jusqu'à la saisie du mot-clé `fin`. Le mot est lu en entier avant vérification (pas de lecture caractère par caractère).

### 7 · Automate complémentaire
Construction de l'automate reconnaissant le **langage complémentaire** par inversion des états finaux et non-finaux. Le programme indique explicitement à partir de quel automate (AFDC ou AFDCM) le complémentaire est calculé.

---

## 📝 Traces d'exécution

À chaque chargement d'un automate, un fichier de trace est créé automatiquement :

```
traces_execution/trace_automate_07_20260329_143201.txt
```

Ce fichier enregistre l'intégralité des sorties du programme pour cet automate (tables, partitions, résultats de reconnaissance…), conformément aux exigences du sujet.

---

## 📚 Références

- [PrettyTable — GeeksforGeeks](https://www.geeksforgeeks.org/python/creating-tables-with-prettytable-library-python/)
- [Colorama — GeeksforGeeks](https://www.geeksforgeeks.org/python/introduction-to-python-colorama/)
- Cours & TD *Automates Finis et Expressions Rationnelles* — EFREI P2 2025/2026

---

<div align="center">

*Projet réalisé dans le cadre du cursus ingénieur EFREI Paris — Promotion 2028*

</div>
