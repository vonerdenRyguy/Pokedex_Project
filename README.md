# Pokedex_Project

A Pokedex desktop app written in Java Swing. It connects to a MySQL database with JDBC to load Pokemon data.

**[Try the web version here](https://vonerdenryguy.github.io/Pokedex_Project/)** (no setup needed). It's a browser version of the same app, built from the same data, so anyone can use it without installing MySQL.

<img width="1912" height="991" alt="Pokedex main screen" src="https://github.com/user-attachments/assets/cd1e89c3-f162-4b7c-811a-ce1b4e04ea7f" />

## What it does

- Search for Pokemon by name. Suggestions pop up as you type.
- A table of 890 Pokemon with their sprite, type and base stats. Rows are colored by type.
- Click a Pokemon or search for one to see its second type, stat total and generation.
- Party Maker lets you pick a team of 6. Once the team is full you can pull up a description of each one.
- Trivia gives you a Pokedex description and you have 3 tries to guess which Pokemon it is.

## How I built it

I made this for my databases class (CSE 385). The goal was to take real data, get it into MySQL, and build something that actually uses it instead of just writing queries in a vacuum.

**Getting the data in.** I found three data sets that each had part of what I wanted:

- `Pokemon.csv` has the names, types and stats
- `metadata.csv` has a sprite image URL for each Pokemon
- `pokedex.json` has the Pokedex descriptions

I imported each one into its own table (`pokemon`, `spriteimages` and `pokedex`) using MySQL Workbench's import wizard. They all share the Pokemon's ID number, so that's what everything gets joined on.

**Cleaning it up.** The raw imports weren't usable as-is. The ID column in `pokemon` came in named `ï»¿ID` because the CSV had a byte order mark at the start of the file, so I had to rename it. Columns like `Sp. Atk` and `Sp. Def` had spaces and periods in them, which are annoying to query, so I renamed those to `sp_atk` and `sp_def`. I also dropped columns I didn't need.

The data sets also didn't line up perfectly. The CSV has extra rows for alternate forms (like Mega Evolutions) that reuse the same ID, and the JSON goes past where the other files stop. I used `SELECT DISTINCT` and limited everything to IDs 890 and under so every Pokemon in the app has stats, a sprite and a description.

**Building new tables from the old ones.** For the trivia game I needed each description paired with the right name. I made a `pokemonnames` table with `CREATE TABLE ... AS SELECT DISTINCT` and a join, then filled a `pokeDescription` table with an `INSERT ... SELECT` that joins the descriptions to the names.

The hardest part was that most descriptions say the Pokemon's name, which gives away the trivia answer. I wrote an `UPDATE` with a `CASE` statement that uses `LIKE`, `CONCAT` and `SUBSTRING` to swap the name out for "This Pokemon". That got most of them, but names show up in a lot of different spots in a sentence, so I still had to write 69 more `REPLACE` updates by hand for the ones it missed and for some misspellings in the data.

**Connecting it to Java.** The app talks to MySQL through JDBC. Every query is a `PreparedStatement`, so user input like the search box gets passed in as a parameter instead of being glued into the SQL string. Some of the queries the app runs:

- a join between `spriteimages` and `pokemon` to fill the main table
- a `LIKE '%text%'` query that powers the search suggestions
- `ORDER BY RAND() LIMIT 1` to pick a random trivia question
- an `INSERT` that saves your party into a `partyOfSix` table, so the team stays in the database

**What I'd do differently now.** Every method opens its own database connection, and the username and password are written out 7 different times, so setting it up on a new computer means changing 14 lines. Now I'd put the connection in one helper method and read the login from a config file. The app also downloads every sprite image when it starts up, which is why it takes 35 to 65 seconds to open. Loading the images only when they're on screen would fix that.

## Running the Java version

Everything you need is in `385 submission folder.zip`.

1. Open the `FirstJDBC` folder from the zip as a project in Eclipse.
2. Add `mysql-connector-j-8.2.0.jar` from the zip to the build path (right click the project, Build Path, Configure Build Path, Libraries, Add External JARs).
3. In MySQL Workbench, create a schema called `pokedex`. Use the Table Data Import Wizard to import `metadata.csv` as `spriteimages`, `Pokemon.csv` as `pokemon`, and `pokedex.json` as `pokedex`.
4. Open and run `necessarySQLcode.sql`. I'd run it one section at a time so it doesn't get overloaded.
5. In `PokedexMenu.java`, replace the username `root` and the password with your own MySQL login. They show up in every method that connects to the database.
6. Run `PokedexMenu.java`. It takes about a minute to open because it loads all the sprite images first.

## Files

- `PokedexMenu.java`: the main window, Party Maker, Trivia and all the database queries
- `PokemonQuestion.java`: holds a trivia question and how many tries are left
- `ImageRenderer/`: shows the sprite images in the table
- `Type1ColorRenderer/`: colors rows by type
- `Pokemon/`: the Pokemon class used for the party
- `docs/`: the web version
