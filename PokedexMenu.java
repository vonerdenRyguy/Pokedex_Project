package firstJDBC;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PokedexMenu extends JFrame {

    private JLabel pokemonImageLabel;
    private JList<ImageIcon> pokemonList;
    private DefaultListModel<ImageIcon> listModel;

    public PokedexMenu() {
        super("Pokemon GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);

        // Panel for the top section with the search bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topPanel.setBackground(Color.RED);
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        pokemonImageLabel = new JLabel();
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(pokemonImageLabel);

        // Panel for the left section with buttons
        JPanel leftPanel = new JPanel(new GridLayout(3, 1));
        JButton partyMakerButton = new JButton("Party Maker");
        partyMakerButton.setBackground(Color.CYAN);
        JButton vsButton = new JButton("V.S.");
        vsButton.setBackground(Color.YELLOW);
        JButton questionsButton = new JButton("Questions");
        questionsButton.setBackground(Color.GREEN);
        leftPanel.add(partyMakerButton);
        leftPanel.add(vsButton);
        leftPanel.add(questionsButton);

        // Panel for the right section with the scrollable list
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        listModel = new DefaultListModel<>();
        pokemonList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(pokemonList);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel for the main content with image and search bar
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        // Add the content panel to the frame
        add(contentPanel);

        //pokemonImageLabel = new JLabel();
        //contentPanel.add(pokemonImageLabel, BorderLayout.SOUTH);

        // Connect to the database and display the Pokémon
        displayPokemon();

        // Add ActionListener for the search button
        searchButton.addActionListener(e -> searchPokemon(searchField.getText()));

        // Add ListSelectionListener for the JList
        pokemonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Fetch the selected image and set the icon
                ImageIcon selectedImageIcon = pokemonList.getSelectedValue();
                if (selectedImageIcon != null) {
                    pokemonImageLabel.setIcon(selectedImageIcon);
                }
            }
        });

        // Add ActionListener for the buttons
        partyMakerButton.addActionListener(e -> handlePartyMaker());
        vsButton.addActionListener(e -> handleVs());
        questionsButton.addActionListener(e -> handleQuestions());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void displayPokemon() {
        // JDBC connection parameters
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // SQL query to retrieve Pokémon details including image URL
            String sql = "SELECT id, sprite_url FROM pokedex.spriteimages";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int pokemonIdFromDB = resultSet.getInt("id");
                        String imageUrl = resultSet.getString("sprite_url");

                        // Fetch the image using the URL and create an ImageIcon
                        Image image = getImageFromUrl(imageUrl);
                        if (image != null) {
                            ImageIcon imageIcon = new ImageIcon(image);

                            // Adding Pokémon image icon to the listModel
                            listModel.addElement(imageIcon);
                        }
                        
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchPokemon(String searchTerm) {
        // Implement search functionality based on searchTerm
        // You may update the listModel accordingly
    }

    private Image getImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., display a placeholder image)
            return null;
        }
    }

    private void handlePartyMaker() {
        // Implement the logic for the Party Maker feature
        // You may open a new frame or perform other actions
        
        System.out.println("Party Maker clicked");
    }

    private void handleVs() {
        // Implement the logic for the V.S. feature
        // You may open a new frame or perform other actions
        System.out.println("V.S. clicked");
    }

    // Methods that handle the Trivia Button.
    
    private void handleQuestions() {
        // Implement the logic for the Questions feature

        // Fetch a random description and associated Pokemon from the database
        PokemonQuestion pokemonQuestion = getRandomPokemonQuestion();

        // Create a new frame for the questions
        JFrame questionsFrame = new JFrame("Pokemon TRIVIA");
        questionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        questionsFrame.setSize(400, 200);
        questionsFrame.setLayout(new BorderLayout());

        // Display the question and input field
        JLabel questionLabel = new JLabel("Who's that Pokemon?");
        JLabel descriptionLabel = new JLabel(pokemonQuestion.getDescription());
        JTextField guessField = new JTextField(20);
        JButton submitButton = new JButton("Submit");
        JLabel resultLabel = new JLabel();

        JPanel questionPanel = new JPanel(new GridLayout(4, 1));
        questionPanel.add(questionLabel);
        questionPanel.add(descriptionLabel);
        questionPanel.add(guessField);
        questionPanel.add(submitButton);

        questionsFrame.add(questionPanel, BorderLayout.NORTH);
        questionsFrame.add(resultLabel, BorderLayout.CENTER);

        // Add ActionListener for the submit button
        submitButton.addActionListener(e -> {
            // Check the user's guess
            String userGuess = guessField.getText().trim();
            boolean isCorrect = userGuess.equalsIgnoreCase(pokemonQuestion.getPokemonName());

            // Update the result label
            if (isCorrect) {
                resultLabel.setText("Correct! Well done!");
            } else {
                resultLabel.setText("Incorrect. Try again!");
            }

            // Decrement the attempts
            pokemonQuestion.decrementAttempts();

            // Check if the user has exhausted attempts
            if (pokemonQuestion.getAttempts() <= 0) {
                resultLabel.setText("Sorry, you've run out of attempts. The correct answer is: " + pokemonQuestion.getPokemonName());
                guessField.setEnabled(false);
                submitButton.setEnabled(false);
                
                //questionsFrame.dispose();
                
             // Add buttons for going back to the main panel or playing again
                JButton playAgainButton = new JButton("Play Again");

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(playAgainButton);

                
                // Add ActionListener for the play again button
                playAgainButton.addActionListener(playAgainEvent -> {
                    // Implement logic to play again
                    questionsFrame.dispose();  // Close the question frame
                    // Add code to play again (refresh or instantiate a new instance)
                    // For example, you can call a method to start a new set of questions
                    handleQuestions();
                });

                // Add the button panel to the questions frame
                questionsFrame.add(buttonPanel, BorderLayout.SOUTH);
            }
        });

        questionsFrame.setLocationRelativeTo(null);
        questionsFrame.setVisible(true);

    }
    

    private PokemonQuestion getRandomPokemonQuestion() {
        String jdbcUrl = "jdbc:mysql://localhost/pokedex?";
        String username = "root";
        String password = "2591";

        List<PokemonQuestion> pokemonQuestions = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String sql = "SELECT id, description, pokemon_name FROM pokedex.pokedescription ORDER BY RAND() LIMIT 1";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String description = resultSet.getString("description");
                        String pokemonName = resultSet.getString("pokemon_name");
                        int attempts = 3; // Set the initial number of attempts

                        PokemonQuestion pokemonQuestion = new PokemonQuestion(id, description, pokemonName, attempts);
                        pokemonQuestions.add(pokemonQuestion);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pokemonQuestions.isEmpty() ? null : pokemonQuestions.get(0);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PokedexMenu());
    }
}




