# Pokedex_Project

A Pokedex desktop app written in Java Swing. It connects to a MySQL database with JDBC to load Pokemon data.

<img width="1912" height="991" alt="Pokedex main screen" src="https://github.com/user-attachments/assets/cd1e89c3-f162-4b7c-811a-ce1b4e04ea7f" />

## What it does

- Search for Pokemon by name. The table filters as you type.
- Shows each Pokemon's sprite, type and base stats.
- Rows are colored by type.
- Party Maker lets you put together a team.
- Questions is a small Pokemon quiz.

## Running it

1. Install MySQL and create a database called `pokedex` with the Pokemon data.
2. Add the MySQL Connector/J driver to your classpath.
3. In `PokedexMenu.java`, change the username and password to match your MySQL setup.
4. Run `PokedexMenu`.

## Files

- `PokedexMenu.java`: the main window (search, table, buttons)
- `PokemonQuestion.java`: the quiz
- `ImageRenderer/`: shows the sprite images in the table
- `Type1ColorRenderer/`: colors rows by type
- `Pokemon/`: the Pokemon data classes

I made this for my databases class.
