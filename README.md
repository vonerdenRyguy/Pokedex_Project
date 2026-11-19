# Pokédex

A desktop Pokédex app built in **Java Swing** and backed by a **SQL database over JDBC**. You can search Pokémon, view their stats and sprites, build a party, and take a Pokémon quiz.

<img width="1912" height="991" alt="Pokédex app main screen" src="https://github.com/user-attachments/assets/cd1e89c3-f162-4b7c-811a-ce1b4e04ea7f" />

## Features

- **Live search** that filters the Pokémon table as you type
- **Stat table** showing ID, name, type, HP, Attack, Defense, Sp. Atk, Sp. Def and Speed, with sprite images
- **Type-colored rows**, using a custom table cell renderer
- **Party Maker** for building a team
- **Questions** mode, a Pokémon quiz

## Tech Stack

| Layer    | Technology            |
|----------|-----------------------|
| UI       | Java Swing            |
| Data     | MySQL via JDBC        |
| Language | Java                  |

## Project Structure

```
PokedexMenu.java       # Main window: search, table, navigation
PokemonQuestion.java   # Quiz mode
ImageRenderer/         # Renders sprite images inside table cells
Type1ColorRenderer/    # Colors rows by Pokémon type
Pokemon/               # Pokémon model / data classes
```

## Running It

1. Install **MySQL** and create a database named `pokedex` with the Pokémon data loaded.
2. Add the MySQL JDBC driver (Connector/J) to the classpath.
3. Set your MySQL username and password in the `jdbcUrl` / `username` / `password` fields in `PokedexMenu.java`.
4. Compile and run `PokedexMenu`.

## Background

Built as a database course project to practice relational data modeling and connecting a Java GUI to a SQL backend.
