package firstJDBC;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;

public class PokemonQuestion {
    private int id;
    private String description;
    private String pokemonName;
    private int attempts;
    private String imageUrl;

    public PokemonQuestion(int id, String description, String pokemonName, int attempts, String imageUrl) {
        this.id = id;
        this.description = description;
        this.pokemonName = pokemonName;
        this.attempts = attempts;
        this.imageUrl = imageUrl;
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

    public Image getImage() {
        return loadImage(imageUrl);
    }

    private Image loadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ImageIcon getImageIcon() {
        Image image = getImage();
        return new ImageIcon(image);
    }
}
