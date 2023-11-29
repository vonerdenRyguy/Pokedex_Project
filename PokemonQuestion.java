package firstJDBC;

public class PokemonQuestion {
    private final int id;
    private final String description;
    private final String pokemonName;
    private int attempts;

    public PokemonQuestion(int id, String description, String pokemonName, int attempts) {
        this.id = id;
        this.description = description;
        this.pokemonName = pokemonName;
        this.attempts = attempts;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public int getAttempts() {
        return attempts;
    }

    public void decrementAttempts() {
        attempts--;
    }
}
